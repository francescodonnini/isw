package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.entities.IssueLocalEntity;
import io.github.francescodonnini.data.ReleaseApi;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.utils.FileUtils;
import io.github.francescodonnini.utils.GitUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CsvIssueApi {
    private final Logger logger = Logger.getLogger(CsvIssueApi.class.getName());
    private final ReleaseApi releaseApi;
    private final Path cache;
    private final Path source;

    public CsvIssueApi(ReleaseApi releaseApi, Path cache, Path source) {
        this.releaseApi = releaseApi;
        this.cache = cache;
        this.source = source;
    }

    public List<Issue> getLocal(String projectName) throws IOException, GitAPIException {
        var commits = GitUtils.getCommits(source.resolve(projectName.toLowerCase())).stream()
                .map(c -> Map.entry(c.getName(), c))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var releases = releaseApi.getReleases(projectName).stream()
                .map(r -> Map.entry(r.id(), r))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return getIssues(commits, releases, cache.resolve(projectName).resolve("issues.csv"));
    }

    private List<Issue> getIssues(Map<String, RevCommit> commits, Map<String, Release> releases, Path path) throws FileNotFoundException {
        var beans = new CsvToBeanBuilder<IssueLocalEntity>(new FileReader(path.toFile()))
                .withType(IssueLocalEntity.class)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .build()
                .parse();
        return beans.stream()
                .map(b -> fromCsv(b, commits, releases))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public void saveLocal(String project, List<Issue> issues) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        if (issues.isEmpty()) {
            return;
        }
        save(issues, cache.resolve(project.toUpperCase()));
    }

    private void save(List<Issue> issues, Path path) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        var beans = issues.stream().map(this::toCsv).toList();
        FileUtils.createDirectory(path);
        try (var writer = new FileWriter(path.resolve("issues.csv").toFile())) {
            var beanToCsv = new StatefulBeanToCsvBuilder<IssueLocalEntity>(writer).build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
            logger.log(Level.INFO, "Saved {0} issues", beans.size());
        }
    }

    private IssueLocalEntity toCsv(Issue model) {
        var bean = new IssueLocalEntity();
        bean.setAffectedVersions(model.affectedVersions().stream().map(Release::id).toList());
        bean.setCommits(model.commits().stream().map(c -> c.getId().getName()).toList());
        bean.setCreated(model.created());
        bean.setFixVersion(model.fixVersion().id());
        bean.setOpeningVersion(model.openingVersion().id());
        bean.setKey(model.key());
        bean.setProject(model.project());
        return bean;
    }

    private Optional<Issue> fromCsv(IssueLocalEntity bean, Map<String, RevCommit> commits, Map<String, Release> releases) {
        List<Release> affectedVersions = new ArrayList<>();
        if (bean.getAffectedVersions() != null) {
            affectedVersions.addAll(bean.getAffectedVersions().stream()
                    .map(releases::get)
                    .filter(Objects::nonNull)
                    .toList());
        }
        var fixVersion = releases.get(bean.getFixVersion());
        var openingVersion = releases.get(bean.getOpeningVersion());
        if (fixVersion == null || openingVersion == null) {
            return Optional.empty();
        }
        var commitList = new ArrayList<RevCommit>();
        if (bean.getCommits() != null) {
            commitList.addAll(bean.getCommits().stream()
                    .map(commits::get)
                    .filter(Objects::nonNull)
                    .toList());
        }
        if (commitList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Issue(
                affectedVersions,
                bean.getCreated(),
                fixVersion,
                openingVersion,
                commitList,
                bean.getKey(),
                bean.getProject()
        ));
    }
}
