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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;

public class CsvIssueApi {
    private final String defaultPath;
    private final List<Release> releases;
    private final List<RevCommit> commits;

    public CsvIssueApi(String defaultPath, List<Release> releases, List<RevCommit> commits) {
        this.defaultPath = defaultPath;
        this.releases = releases;
        this.commits = commits;
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
        return beans.stream().map(this::fromCsv).toList();
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
        bean.setAffectedVersions(model.affectedVersions().stream().map(Release::number).toList());
        bean.setCommits(model.commits().stream().map(c -> c.getId().getName()).toList());
        bean.setCreated(model.created());
        bean.setFixVersion(model.fixVersion().number());
        bean.setOpeningVersion(model.openingVersion().number());
        bean.setKey(model.key());
        bean.setProject(model.project());
        return bean;
    }

    private Issue fromCsv(IssueLocalEntity bean) {
        List<Release> affectedVersions = new ArrayList<>();
        if (bean.getAffectedVersions() != null) {
            affectedVersions.addAll(bean.getAffectedVersions().stream().map(this::getByReleaseNumber).toList());
        }
        var fixVersion = getByReleaseNumber(bean.getFixVersion());
        var openingVersion = getByReleaseNumber(bean.getOpeningVersion());
        var commitList = new ArrayList<RevCommit>();
        if (bean.getCommits() != null) {
            commitList.addAll(bean.getCommits().stream().map(this::getByObjectId).toList());
        }
        return new Issue(
                affectedVersions,
                bean.getCreated(),
                fixVersion,
                openingVersion,
                commitList,
                bean.getKey(),
                bean.getProject()
        );
    }

    private RevCommit getByObjectId(String id) {
        var objectId = ObjectId.fromString(id);
        return commits.stream().filter(c -> c.getId().equals(objectId)).findFirst().orElse(null);
    }

    private Release getByReleaseNumber(int releaseNumber) {
        return binarySearch(releases, release -> Integer.compare(release.number(), releaseNumber));
    }

    private static <T> T binarySearch(List<T> releases, ToIntFunction<T> comparator) {
        var low = 0;
        var mid = -1;
        var high = releases.size() - 1;
        while (low <= high) {
            mid = low + high >>> 1;
            var midVal = releases.get(mid);
            var cmp = comparator.applyAsInt(midVal);
            if (cmp < 0) {
                low = mid + 1;
            } else {
                if (cmp == 0) {
                    return midVal;
                }
                high = mid - 1;
            }
        }
        return null;
    }
}
