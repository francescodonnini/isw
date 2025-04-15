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
        return connection.prepareStatement("INSERT INTO methods(signature, classPath, classNumber, content) VALUES (?, ?, ?, ?)");
    }

    @Override
    public void prepare(PreparedStatement preparedStatement, JavaMethodLocalEntity entity) throws SQLException {
        preparedStatement.setString(1, entity.getSignature());
        preparedStatement.setString(2, entity.getPath().toString());
        preparedStatement.setInt(3, entity.getReleaseNumber());
        preparedStatement.setString(4, entity.getContent());
    }

    @Override
    public Optional<JavaMethod> fromResultSet(ResultSet rs) throws SQLException {
        var signature = rs.getString("signature");
        var path = Path.of(rs.getString("classPath"));
        var release = rs.getInt("classNumber");
        var content = rs.getString("content");
        var oc = classes.stream().filter(c -> filter(c, path, release)).findFirst();
        if (oc.isEmpty()) {
            return Optional.empty();
        }
        var clazz = oc.get();
        return Optional.of(new JavaMethod(clazz, signature, content));
    }

    private boolean filter(JavaClass clazz, Path path, int release) {
        return clazz.getPath().equals(path) && clazz.getRelease().number() == release;
    }

    @Override
    public JavaMethodLocalEntity toLocalEntity(JavaMethod model) {
        var bean = new JavaMethodLocalEntity();
        bean.setSignature(model.getSignature());
        bean.setContent(model.getContent());
        bean.setPath(model.getJavaClass().getPath());
        bean.setReleaseNumber(model.getJavaClass().getRelease().number());
        return bean;
    }

    public List<JavaMethod> getLocal() throws SQLException {
        return api.getLocal("method", this);
    }

    public void saveLocal(List<JavaMethod> classes) throws SQLException {
        api.saveLocal(classes, this, this);
    }
}
