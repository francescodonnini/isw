package io.github.francescodonnini.sqlite;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteApi {
    public interface ResultSetFactory<M> {
        Optional<M> fromResultSet(ResultSet resultSet) throws SQLException;
    }
    public interface LocalEntityFactory<E, M> {
        E toLocalEntity(M model);
    }

    public interface PreparedStatementFactory<E> {
        PreparedStatement create(Connection connection) throws SQLException;
        void prepare(PreparedStatement preparedStatement, E entity) throws SQLException;
    }
    private static final String JDBC_DRIVER = "jdbc:sqlite:";
    private final Path dbPath;

    public SQLiteApi(Path dbPath) throws SQLException {
        this.dbPath = dbPath;
        createTablesIfNotExist();
    }

    private String getConnectionString(Path path) {
        return JDBC_DRIVER + path;
    }

    private void createTablesIfNotExist() throws SQLException {
        try (var connection = DriverManager.getConnection(getConnectionString(dbPath))) {
            createClassesTable(connection);
            createMethodsTable(connection);
        }
    }

    private void createClassesTable(Connection connection) throws SQLException {
        try (var stmt = connection.createStatement()) {
            var sql = """
                CREATE TABLE IF NOT EXISTS classes (
                    path TEXT,
                    number INTEGER,
                    parent TEXT,
                    content TEXT,
                    PRIMARY KEY(path, number));
                """;
            stmt.execute(sql);
        }
    }

    private void createMethodsTable(Connection connection) throws SQLException {
        try (var stmt = connection.createStatement()) {
            var sql = """
                CREATE TABLE IF NOT EXISTS methods (
                    buggy       INTEGER,
                    signature   TEXT,
                    classPath   TEXT,
                    classNumber INTEGER,
                    content     TEXT,
                    PRIMARY KEY(signature, classPath, classNumber),
                    FOREIGN KEY(classPath, classNumber) REFERENCES classes(path, number));""";
            stmt.execute(sql);
        }
    }

    public <T> List<T> getLocal(String entity, ResultSetFactory<T> factory) throws SQLException {
        try (var connection = DriverManager.getConnection(getConnectionString(dbPath));
            var stmt = connection.prepareStatement("SELECT * FROM " + entity)) {
            if (stmt.execute()) {
                var rs = stmt.getResultSet();
                List<T> list = new ArrayList<>();
                while (rs.next()) {
                    factory.fromResultSet(rs).ifPresent(list::add);
                }
                return list;
            }
            return List.of();
        }
    }

    public <M, E> void saveLocal(List<M> classes, LocalEntityFactory<E, M> localEntityFactory, PreparedStatementFactory<E> statementFactory) throws SQLException {
        try (var conn = DriverManager.getConnection(getConnectionString(dbPath));
             var psql = statementFactory.create(conn)) {
            conn.setAutoCommit(false);
            var counter = 0;
            var it = classes.stream().map(localEntityFactory::toLocalEntity).iterator();
            while (it.hasNext()) {
                var bean = it.next();
                statementFactory.prepare(psql, bean);
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
}