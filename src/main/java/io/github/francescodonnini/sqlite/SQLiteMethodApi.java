package io.github.francescodonnini.sqlite;

import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.JavaMethod;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SQLiteMethodApi implements SQLiteApi.PreparedStatementFactory<JavaMethodLocalEntity>, SQLiteApi.LocalEntityFactory<JavaMethodLocalEntity, JavaMethod>, SQLiteApi.ResultSetFactory<JavaMethod> {
    private final SQLiteApi api;
    private final List<JavaClass> classes;

    public SQLiteMethodApi(SQLiteApi api, List<JavaClass> classes) {
        this.api = api;
        this.classes = classes;
    }

    @Override
    public PreparedStatement create(Connection connection) throws SQLException {
        return connection.prepareStatement("INSERT INTO methods(buggy, signature, classPath, releaseId, content) VALUES (?, ?, ?, ?, ?)");
    }

    @Override
    public void prepare(PreparedStatement preparedStatement, JavaMethodLocalEntity entity) throws SQLException {
        preparedStatement.setInt(1, entity.isBuggy() ? 1 : 0);
        preparedStatement.setString(2, entity.getSignature());
        preparedStatement.setString(3, entity.getPath().toString());
        preparedStatement.setString(4, entity.getReleaseId());
        preparedStatement.setString(5, entity.getContent());
    }

    @Override
    public Optional<JavaMethod> fromResultSet(ResultSet rs) throws SQLException {
        var buggy = rs.getInt("buggy") == 1;
        var signature = rs.getString("signature");
        var path = Path.of(rs.getString("classPath"));
        var release = rs.getString("releaseId");
        var content = rs.getString("content");
        var oc = classes.stream().filter(c -> filter(c, path, release)).findFirst();
        if (oc.isEmpty()) {
            return Optional.empty();
        }
        var clazz = oc.get();
        return Optional.of(new JavaMethod(buggy, clazz, signature, content));
    }

    private boolean filter(JavaClass clazz, Path path, String releaseId) {
        return clazz.getPath().equals(path) && clazz.getRelease().id().equals(releaseId);
    }

    @Override
    public JavaMethodLocalEntity toLocalEntity(JavaMethod model) {
        var bean = new JavaMethodLocalEntity();
        bean.setBuggy(model.isBuggy());
        bean.setSignature(model.getSignature());
        bean.setContent(model.getContent());
        bean.setPath(model.getJavaClass().getPath());
        bean.setReleaseId(model.getJavaClass().getRelease().id());
        return bean;
    }

    public List<JavaMethod> getLocal() throws SQLException {
        return api.getLocal("methods", this);
    }

    public void saveLocal(List<JavaMethod> classes) throws SQLException {
        api.saveLocal(classes, this, this);
    }
}
