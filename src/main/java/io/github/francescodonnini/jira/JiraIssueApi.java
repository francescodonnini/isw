package io.github.francescodonnini.jira;

import io.github.francescodonnini.jira.json.issue.IssueNetworkEntity;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import org.eclipse.jgit.revwalk.RevCommit;

import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class JiraIssueApi {
    private final Logger logger = Logger.getLogger(JiraIssueApi.class.getName());
    private final String projectName;
    private final String pattern;
    private final List<RevCommit> commits;
    private final RestApi restApi;
    private final List<Release> releases;
    private int droppedIssues = 0;
    private int totalIssues = 0;

    public JiraIssueApi(String projectName, String pattern, RestApi restApi, List<Release> releases, List<RevCommit> commits) {
        this.projectName = projectName;
        this.pattern = pattern;
        this.commits = commits;
        this.restApi = restApi;
        this.releases = new ArrayList<>(releases);
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
    public List<Issue> getIssues() {
        try {
            // Ordino le releases in ordine crescente rispetto alla data di creazione
            releases.sort(Comparator.comparing(Release::releaseDate));
            var mapping = getTicketCommitMapping(pattern);
            var issueNetworkEntities = restApi.getIssues("project='%s' AND type=bug AND (status=closed OR status=resolved) AND resolution=fixed".formatted(projectName))
                    .getIssueList().stream()
                    // Prendo solamente i ticket di Jira che hanno un commit che ne cita la chiave
                    .filter(i -> mapping.containsKey(i.getKey()))
                    .toList();
            var issues = new ArrayList<Issue>();
            for (var i : issueNetworkEntities) {
                ++totalIssues;
                var affectedVersions = getAffectedVersions(i);
                // La fixVersion è la prima release la cui data è maggiore della data dell'ultimo commit che ha fixato
                // l'issue
                var o1 = getFixVersion(mapping, i.getKey());
                // L'opening version è la release relativa alla data di creazione del ticket.
                var o2 = getOpeningVersion(i.getFields().getCreated());
                if (o1.isEmpty() || o2.isEmpty()) continue;
                // Bisogna controllare che:
                // 1. IV < FV (cioè il bug non viene fixato nella stessa release in cui è stato trovato).
                // 2. IV <= OV
                // 3. OV <= FV (il fix del bug deve avvenire almeno nella stessa release (o comunque successiva) alla release in cui è stato scoperto)
                // Tutti e 3 i vincoli devono essere verificati solamente negli issue che hanno il campo `affectedVersion` non vuoto.
                // Non è presente una release con data di pubblicazione >= alla data di creazione del ticket
                var fixVersion = o1.get();
                var openingVersion = o2.get();
                createIssue(i, mapping, affectedVersions, openingVersion, fixVersion).ifPresent(issues::add);
            }
            logger.log(Level.INFO, () -> "dropped %d issues of %d".formatted(droppedIssues, totalIssues));
            return issues;
        } catch (URISyntaxException e) {
            logger.log(Level.SEVERE, e.getMessage());
            return List.of();
        }
    }

    /**
     * getTicketCommitMapping costruisce una mappa la cui chiave è l'identificativo del ticket che viene citato
     * nel messaggio di un commit (quest'ultimo è il valore della chiave).
     * @param pattern espressione regolare che descrive come è fatta la chiave del ticket che si cerca nei messaggi dei
     *                commit.
     * @return una mappa chiave ticket, commit il cui messaggio contiene la chiave del ticket.
     */
    private Map<String, List<RevCommit>> getTicketCommitMapping(String pattern) {
        var p = Pattern.compile(pattern);
        var mapping = new HashMap<String, List<RevCommit>>();
        for (var commit : commits) {
            var matcher = p.matcher(commit.getFullMessage());
            if (matcher.find()) {
                mapping.computeIfAbsent(matcher.group(), _ -> new ArrayList<>()).add(commit);
            }
        }
        return mapping;
    }

    private List<Release> getAffectedVersions(IssueNetworkEntity issue) {
        var affectedVersions = new ArrayList<Release>();
        for (var v : releases) {
            for (var affectedVersion : issue.getFields().getAffectedVersions()) {
                if (v.id().equals(affectedVersion.getId())) {
                    affectedVersions.add(v);
                }
            }
        }
        return affectedVersions;
    }

    private Optional<Release> getFixVersion(Map<String, List<RevCommit>> mapping, String key) {
        var o = getLatestCommitDate(mapping, key);
        if (o.isEmpty()) {
            return Optional.empty();
        }
        return releases.stream()
                .filter(r -> !r.releaseDate().isBefore(o.get()))
                .min((x, y) -> Comparator.comparing(Release::releaseDate).compare(x, y));
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

    private Optional<Release> getOpeningVersion(LocalDateTime created) {
        return releases.stream()
                .filter(r -> r.releaseDate().isAfter(created.toLocalDate()))
                .min((x, y) -> Comparator.comparing(Release::releaseDate).compare(x, y));
    }

    private Optional<Issue> createIssue(IssueNetworkEntity i, Map<String, List<RevCommit>> commits, List<Release> affectedVersions, Release openingVersion, Release fixVersion) {
        if (!affectedVersions.isEmpty() && !checkForConsistency(affectedVersions, openingVersion, fixVersion)) {
            ++droppedIssues;
            logger.log(Level.INFO, () -> "dropping issue %s due to inconsistent versions".formatted(i.getKey()));
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
    // 1. IV < FV
    // 2. IV <= OV
    // 3. OV <= FV
    // 4. AV <= FV
    private boolean checkForConsistency(List<Release> affectedVersions, Release openingVersion, Release fixVersion) {
        var o = getInjectedVersion(affectedVersions);
        if (o.isEmpty()) {
            logger.log(Level.INFO, () -> "issue has no injected version");
            return false;
        }
        var injected = o.get();
        if (!injected.isBefore(fixVersion)) {
            logger.log(Level.INFO, () -> "injected version %s is not before fix version %s".formatted(injected.name(), fixVersion.name()));
            return false;
        }
        if (injected.isAfter(openingVersion)) {
            logger.log(Level.INFO, () -> "injected version %s is after opening version %s".formatted(injected.name(), openingVersion.name()));
            return false;
        }
        if (openingVersion.isAfter(fixVersion)) {
            logger.log(Level.INFO, () -> "opening version %s is after fix version %s".formatted(openingVersion.name(), fixVersion.name()));
            return false;
        }
        if (affectedVersions.stream().anyMatch(r -> !r.isBefore(fixVersion))) {
            logger.log(Level.INFO, () -> "there exists an affected version that is after fix version %s".formatted(fixVersion.name()));
            return false;
        }
        return true;
    }

    private Optional<Release> getInjectedVersion(List<Release> affectedVersions) {
        return affectedVersions.stream()
                .min((x, y) -> Comparator.comparing(Release::releaseDate).compare(x, y));
    }
}
