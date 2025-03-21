package io.github.francescodonnini.sqlite;

import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.Release;

import java.nio.file.Path;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SQLiteClassApi {
    private static final String JDBC_DRIVER = "jdbc:sqlite:";
    private final Path dbPath;
    private final List<Release> releases;

    public SQLiteClassApi(Path dbPath, List<Release> releases) throws SQLException {
        this.dbPath = dbPath;
        createClassesTableIfNotExists();
        this.releases = releases;
    }

    private String getConnectionString(Path path) {
        return JDBC_DRIVER + path;
    }

    private void createClassesTableIfNotExists() throws SQLException {
        try (var connection = DriverManager.getConnection(getConnectionString(dbPath))) {
            var sql = """
                CREATE TABLE IF NOT EXISTS classes (
                    path TEXT,
                    number INTEGER,
                    buggy INTEGER DEFAULT 0,
                    content TEXT,
                    PRIMARY KEY(path, number));""";
            var stmt = connection.createStatement();
            stmt.execute(sql);
        }
    }

    public List<JavaClass> getLocal() throws SQLException {
        try (var connection = DriverManager.getConnection(getConnectionString(dbPath))) {
            var sql = "SELECT path, number, buggy, content FROM classes;";
            var stmt = connection.createStatement();
            var rs = stmt.executeQuery(sql);
            List<JavaClass> classes = new ArrayList<>();
            while (rs.next()) {
                var path = rs.getString("path");
                var number = rs.getInt("number");
                var o = releases.stream().filter(r -> r.number() == number).findFirst();
                if (o.isEmpty()) {
                    continue;
                }
                var buggy = rs.getInt("buggy") == 1;
                var content = rs.getString("content");
                classes.add(new JavaClass(buggy, path, o.get(), content));
            }
            return classes;
        }
    }

    public void saveLocal(List<JavaClass> classes) throws SQLException {
        try (var conn = DriverManager.getConnection(getConnectionString(dbPath))) {
            conn.setAutoCommit(false);
            var counter = 0;
            var psql = conn.prepareStatement("INSERT INTO classes(path, number, buggy, content) VALUES(?, ?, ?, ?)");
            var it = classes.stream().map(this::toLocalEntity).iterator();
            while (it.hasNext()) {
                var bean = it.next();
                psql.setString(1, bean.getPath());
                psql.setInt(2, bean.getReleaseNumber());
                psql.setInt(3, bean.isBuggy() ? 1 : 0);
                psql.setString(4, bean.getContent());
                psql.addBatch();
                ++counter;
                if (counter % 1024 == 0) {
                    psql.executeBatch();
                }
            }
            psql.executeBatch();
            conn.commit();
        }
    }

    private JavaClassLocalEntity toLocalEntity(JavaClass jc) {
        var bean = new JavaClassLocalEntity();
        bean.setBuggy(jc.isBuggy());
        bean.setPath(jc.getPath());
        bean.setReleaseNumber(jc.getRelease().number());
        bean.setContent(jc.getContent());
        return bean;
    }
}
