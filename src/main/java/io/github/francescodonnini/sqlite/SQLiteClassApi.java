package io.github.francescodonnini.sqlite;

import io.github.francescodonnini.model.JavaClass;
import io.github.francescodonnini.model.Release;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SQLiteClassApi implements SQLiteApi.PreparedStatementFactory<JavaClassLocalEntity>, SQLiteApi.LocalEntityFactory<JavaClassLocalEntity, JavaClass>, SQLiteApi.ResultSetFactory<JavaClass> {
    private final SQLiteApi api;
    private final List<Release> releases;

    public SQLiteClassApi(SQLiteApi api, List<Release> releases) {
        this.api = api;
        this.releases = releases;

    }

    @Override
    public PreparedStatement create(Connection connection) throws SQLException {
        return connection.prepareStatement("INSERT INTO classes(path, number, parent, content) VALUES(?, ?, ?, ?);");
    }

    @Override
    public void prepare(PreparedStatement preparedStatement, JavaClassLocalEntity entity) throws SQLException {
        preparedStatement.setString(1, entity.getPath());
        preparedStatement.setInt(2, entity.getReleaseNumber());
        preparedStatement.setString(3, entity.getParent());
        preparedStatement.setString(4, entity.getContent());
    }

    @Override
    public Optional<JavaClass> fromResultSet(ResultSet rs) throws SQLException {
        var path = rs.getString("path");
        var number = rs.getInt("number");
        var o = releases.stream().filter(r -> r.number() == number).findFirst();
        if (o.isEmpty()) {
            return Optional.empty();
        }
        var parent = rs.getString("parent");
        var content = rs.getString("content");
        return Optional.of(new JavaClass(parent, path, o.get(), content));
    }

    @Override
    public JavaClassLocalEntity toLocalEntity(JavaClass jc) {
        var bean = new JavaClassLocalEntity();
        bean.setPath(jc.getPath().toString());
        bean.setParent(jc.getParent().toString());
        bean.setReleaseNumber(jc.getRelease().number());
        bean.setContent(jc.getContent());
        return bean;
    }

    public List<JavaClass> getLocal() throws SQLException {
        return api.getLocal("classes", this);
    }

    public void saveLocal(List<JavaClass> classes) throws SQLException {
        api.saveLocal(classes, this, this);
    }
}
