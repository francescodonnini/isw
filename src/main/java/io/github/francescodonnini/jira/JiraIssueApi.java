package io.github.francescodonnini.jira;

import io.github.francescodonnini.data.ReleaseApi;
import io.github.francescodonnini.jira.json.issue.IssueNetworkEntity;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.utils.ApacheProjects;
import io.github.francescodonnini.utils.GitUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JiraIssueApi {
    private final Logger logger = Logger.getLogger(JiraIssueApi.class.getName());
    private static final String PATTERN = "%s-\\d+";
    private final RestApi restApi;
    private final ReleaseApi releaseApi;
    private final Path source;
    private int noOpeningVersion = 0;
    private int fixBeforeOpening = 0;
    private int noFixVersion = 0;
    private int noPostReleaseFix = 0;
    private int injectedAfterFix = 0;
    private int injectedAfterOpeningVersion = 0;
    private int affectedNotBeforeFixVersion = 0;
    private int totalIssues = 0;

    public JiraIssueApi(RestApi restApi, ReleaseApi releaseApi, Path source) {
        this.restApi = restApi;
        this.releaseApi = releaseApi;
        this.source = source;
    }

    /**
     * Prende gli issues da Jira relativi al progetto @projectName che hanno un commit col pattern @pattern.
     * @return la lista degli issue che soddisfano le seguenti proprietà:
     * - sono stati recuperati dalla query: "project='<Project Name>' AND type=bug AND (status=closed OR status=resolved) AND resolution=fixed"
     * - ogni ticket viene citato (tramite identificativo) in almeno un commit della relativa repository.
     * - IV < FV
     * - IV <= OV
     * - OV <= FV
     */
    public List<Issue> getIssues(String projectName) {
        try {
            // Ordino le releases in ordine crescente rispetto alla data di creazione
            var releases = new ArrayList<>(releaseApi.getReleases(projectName.toUpperCase()));
            if (releases.isEmpty()) {
                logger.log(Level.WARNING, "No releases found for project: {0}", projectName);
                return List.of();
            }
            releases.sort(Comparator.comparing(Release::releaseDate));
            var mapping = getTicketCommitMapping(source.resolve(projectName.toLowerCase()), PATTERN.formatted(ApacheProjects.jiraKey(projectName)));
            var result = restApi.getIssues("project='%s' AND type=bug AND (status=closed OR status=resolved) AND resolution=fixed".formatted(projectName));
            if (result == null || result.getIssueList() == null) {
                logger.log(Level.WARNING, "No issues found for project: {0}",  projectName);
                return List.of();
            }
            var issueNetworkEntities = result.getIssueList().stream()
                    // Prendo solamente i ticket di Jira che hanno un commit che ne cita la chiave
                    .filter(i -> mapping.containsKey(i.getKey()))
                    .toList();
            var releaseMap = releases.stream()
                    .map(r -> Map.entry(r.id(), r))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            var issues = new ArrayList<Issue>();
            for (var i : issueNetworkEntities) {
                ++totalIssues;
                // La fixVersion è la prima release la cui data è maggiore della data dell'ultimo commit che ha fixato
                // l'issue
                var o1 = getFixVersion(mapping, i.getKey(), releases);
                // L'opening version è la release relativa alla data di creazione del ticket.
                var o2 = getOpeningVersion(i.getFields().getCreated(), releases);
                if (o1.isEmpty()) {
                    noFixVersion++;
                    logger.log(Level.INFO, "Issue {0} has no fix version", i);
                } else if (o2.isEmpty()) {
                    noOpeningVersion++;
                    logger.log(Level.INFO, "Issue {0} has no opening version", i);
                }
                if (o1.isEmpty() || o2.isEmpty()) {
                    continue;
                }
                // Bisogna controllare che:
                // 1. IV < FV (cioè il bug non viene fixato nella stessa release in cui è stato trovato).
                // 2. IV <= OV
                // 3. OV < FV (il fix del bug deve avvenire almeno nella release successiva alla release in cui è stato scoperto)
                // Tutti e 3 i vincoli devono essere verificati solamente negli issue che hanno il campo `affectedVersion` non vuoto.
                // Non è presente una release con data di pubblicazione >= alla data di creazione del ticket
                var affectedVersions = getAffectedVersions(releaseMap, i);
                var fixVersion = o1.get();
                var openingVersion = o2.get();
                if (fixVersion.isBefore(openingVersion)) {
                    fixBeforeOpening++;
                } else {
                    createIssue(i, mapping, affectedVersions, openingVersion, fixVersion).ifPresent(issues::add);
                }
            }
            logDroppedIssues(projectName, totalIssues);
            return issues;
        } catch (URISyntaxException | GitAPIException | IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return List.of();
        }
    }

    private void logDroppedIssues(String projectName, int totalIssues) {
        var total = noOpeningVersion
                + noFixVersion
                + noPostReleaseFix
                + injectedAfterFix
                + fixBeforeOpening
                + affectedNotBeforeFixVersion
                + injectedAfterOpeningVersion;
        var s = projectName +
                " dropped a total of " + total + "/" + totalIssues + " issues\n" +
                "No Fix version:" + noFixVersion + '\n' +
                "No Opening version:" + noOpeningVersion + '\n' +
                "No Post release fix:" + noPostReleaseFix + '\n' +
                "IV > OV:" + injectedAfterOpeningVersion + '\n' +
                "OV > IV:" + fixBeforeOpening + '\n' +
                "IV >= FV:" + injectedAfterFix + '\n' +
                "AV >= FV:" + affectedNotBeforeFixVersion + '\n';
        logger.log(Level.INFO, () -> s);
    }

    /**
     * getTicketCommitMapping costruisce una mappa la cui chiave è l'identificativo del ticket che viene citato
     * nel messaggio di un commit (quest'ultimo è il valore della chiave).
     * @param pattern espressione regolare che descrive come è fatta la chiave del ticket che si cerca nei messaggi dei
     *                commit.
     * @return una mappa chiave ticket, commit il cui messaggio contiene la chiave del ticket.
     */
    private Map<String, List<RevCommit>> getTicketCommitMapping(Path path, String pattern) throws GitAPIException, IOException {
        var p = Pattern.compile(pattern);
        var mapping = new HashMap<String, List<RevCommit>>();
        for (var commit : GitUtils.getCommits(path)) {
            var matcher = p.matcher(commit.getFullMessage());
            if (matcher.find()) {
                mapping.computeIfAbsent(matcher.group(), m -> new ArrayList<>()).add(commit);
            }
        }
        return mapping;
    }

    private List<Release> getAffectedVersions(Map<String, Release> map, IssueNetworkEntity issue) {
        var v = issue.getFields().getAffectedVersions();
        if (v == null) {
            return List.of();
        }
        return v.stream()
                .map(r -> map.get(r.getId()))
                .filter(Objects::nonNull)
                .toList();
    }

    private Optional<Release> getFixVersion(Map<String, List<RevCommit>> mapping, String key, List<Release> releases) {
        var o = getLatestCommitDate(mapping, key);
        return o.flatMap(localDate -> releases.stream()
                .filter(r -> !r.releaseDate().isBefore(localDate))
                .min((x, y) -> Comparator.comparing(Release::releaseDate).compare(x, y)));
    }

    private Optional<LocalDate> getLatestCommitDate(Map<String, List<RevCommit>> mapping, String key) {
        var commitList = mapping.get(key);
        if (commitList == null || commitList.isEmpty()) {
            return Optional.empty();
        }
        return commitList.stream()
                .map(this::getCommitDate)
                .max(Comparator.naturalOrder());
    }


    private LocalDate getCommitDate(RevCommit commit) {
        return commit.getAuthorIdent().getWhen().toInstant()
                .atZone(commit.getAuthorIdent().getTimeZone().toZoneId())
                .toLocalDate();
    }

    private Optional<Release> getOpeningVersion(LocalDateTime created, List<Release> releases) {
        return releases.stream()
                .filter(r -> r.releaseDate().isAfter(created.toLocalDate()))
                .min((x, y) -> Comparator.comparing(Release::releaseDate).compare(x, y));
    }

    private Optional<Issue> createIssue(IssueNetworkEntity i, Map<String, List<RevCommit>> commits, List<Release> affectedVersions, Release openingVersion, Release fixVersion) {
        if (!checkForConsistency(affectedVersions, openingVersion, fixVersion)) {
            return Optional.empty();
        }
        var issue = new Issue(
            affectedVersions,
            i.getFields().getCreated(),
            fixVersion,
            openingVersion,
            commits.get(i.getKey()),
            i.getKey(),
            i.getFields().getProject().getName()
        );
        return Optional.of(issue);
    }

    // checkForConsistency controlla se la tripla (affectedVersions, openingVersion, fixVersion) è consistente, cioè:
    // - IV < FV
    // - IV <= OV
    // - AV <= FV
    private boolean checkForConsistency(List<Release> affectedVersions, Release openingVersion, Release fixVersion) {
        if (affectedVersions.isEmpty()) {
            return true;
        }
        var injectedVersion = affectedVersions.stream().min(Comparator.comparing(Release::releaseDate)).orElseThrow();
        if (!injectedVersion.isBefore(fixVersion)) {
            if (injectedVersion.equals(fixVersion)) {
                noPostReleaseFix++;
            } else {
                injectedAfterFix++;
            }
            logger.log(Level.INFO, () -> "injected version %s is not before fix version %s".formatted(injectedVersion.name(), fixVersion.name()));
            return false;
        }
        if (injectedVersion.isAfter(openingVersion)) {
            logger.log(Level.INFO, () -> "injected version %s is after opening version %s".formatted(injectedVersion.name(), openingVersion.name()));
            injectedAfterOpeningVersion++;
            return false;
        }
        if (affectedVersions.stream().anyMatch(r -> !r.isBefore(fixVersion))) {
            logger.log(Level.INFO, () -> "there exists an affected version that is after fix version %s".formatted(fixVersion.name()));
            affectedNotBeforeFixVersion++;
            return false;
        }
        return true;
    }
}
