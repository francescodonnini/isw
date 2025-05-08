package io.github.francescodonnini.csv;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import io.github.francescodonnini.csv.entities.IssueLocalEntity;
import io.github.francescodonnini.model.Issue;
import io.github.francescodonnini.model.Release;
import io.github.francescodonnini.utils.FileUtils;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CsvIssueApi {
    private final Logger logger = Logger.getLogger(CsvIssueApi.class.getName());
    private final String defaultPath;
    private final Map<String, Release> releases;
    private final Map<String, RevCommit> commits;

    public CsvIssueApi(String defaultPath, List<Release> releases, List<RevCommit> commits) {
        this.defaultPath = defaultPath;
        this.releases = releases.stream().collect(Collectors.toMap(Release::id, r -> r));
        this.commits = commits.stream().collect(Collectors.toMap(c -> c.getId().getName(), c -> c));
    }

    public List<Issue> getLocal(String path) throws FileNotFoundException {
        return getIssues(path);
    }

    public List<Issue> getLocal() throws FileNotFoundException {
        return getIssues(defaultPath);
    }

    private List<Issue> getIssues(String path) throws FileNotFoundException {
        var beans = new CsvToBeanBuilder<IssueLocalEntity>(new FileReader(path))
                .withType(IssueLocalEntity.class)
                .withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_QUOTES)
                .build()
                .parse();
        return beans.stream()
                .map(this::fromCsv)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public void saveLocal(List<Issue> issues, String path) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        save(issues, path);
    }

    public void saveLocal(List<Issue> issues) throws CsvRequiredFieldEmptyException, CsvDataTypeMismatchException, IOException {
        save(issues, defaultPath);
    }

    private void save(List<Issue> issues, String path) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        var beans = issues.stream().map(this::toCsv).toList();
        FileUtils.createFileIfNotExists(path);
        try (var writer = new FileWriter(path)) {
            var beanToCsv = new StatefulBeanToCsvBuilder<IssueLocalEntity>(writer).build();
            for (var b : beans) {
                beanToCsv.write(b);
            }
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

    private Optional<Issue> fromCsv(IssueLocalEntity bean) {
        List<Release> affectedVersions = new ArrayList<>();
        if (bean.getAffectedVersions() != null) {
            affectedVersions.addAll(bean.getAffectedVersions().stream()
                    .map(this::getReleaseById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList());
        }
        var fixVersion = getReleaseById(bean.getFixVersion());
        var openingVersion = getReleaseById(bean.getOpeningVersion());
        if (fixVersion.isEmpty() || openingVersion.isEmpty()) {
            return Optional.empty();
        }
        var commitList = new ArrayList<RevCommit>();
        if (bean.getCommits() != null) {
            commitList.addAll(bean.getCommits().stream()
                    .map(this::getByObjectId)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList());
        }
        if (commitList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Issue(
                affectedVersions,
                bean.getCreated(),
                fixVersion.get(),
                openingVersion.get(),
                commitList,
                bean.getKey(),
                bean.getProject()
        ));
    }

    private Optional<RevCommit> getByObjectId(String id) {
        var c = commits.get(id);
        if (c == null) {
            logger.info(() -> "cannot retrieve commit with id %s".formatted(id));
        }
        return Optional.ofNullable(c);
    }

    private Optional<Release> getReleaseById(String id) {
        var r = releases.get(id);
        if (r == null) {
            logger.info(() -> "cannot retrieve release with id %s".formatted(id));
        }
        return Optional.ofNullable(r);
    }
}
