package oxff.org.persistence;

import oxff.org.model.Arg;
import oxff.org.model.ArgType;
import oxff.org.model.AutoUpdateType;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersistenceManager {

    private final String dbPath;
    private Connection connection;

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS environment (
                id              INTEGER PRIMARY KEY,
                name            TEXT    NOT NULL UNIQUE,
                type            TEXT    NOT NULL DEFAULT 'ALL',
                auto_update_type TEXT   NOT NULL DEFAULT 'NONE',
                length          INTEGER NOT NULL DEFAULT 0,
                default_value   TEXT    DEFAULT '',
                value           TEXT    DEFAULT '',
                code_path       TEXT    DEFAULT '',
                enabled         INTEGER NOT NULL DEFAULT 1,
                description     TEXT    DEFAULT '',
                persistent      INTEGER NOT NULL DEFAULT 1
            );
            """;

    private static final String INSERT_SQL = """
            INSERT INTO environment (id, name, type, auto_update_type, length, default_value, value, code_path, enabled, description, persistent)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                name = excluded.name,
                type = excluded.type,
                auto_update_type = excluded.auto_update_type,
                length = excluded.length,
                default_value = excluded.default_value,
                value = excluded.value,
                code_path = excluded.code_path,
                enabled = excluded.enabled,
                description = excluded.description,
                persistent = excluded.persistent;
            """;

    private static final String SELECT_ALL_SQL = "SELECT * FROM environment";

    private static final String DELETE_BY_ID_SQL = "DELETE FROM environment WHERE id = ?";

    private static final String DELETE_ALL_SQL = "DELETE FROM environment";

    public PersistenceManager(String dbPath) {
        this.dbPath = dbPath;
    }

    public void initDatabase() throws SQLException {
        File dbFile = new File(dbPath);
        File parentDir = dbFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(CREATE_TABLE_SQL);
        }
    }

    public List<Arg> loadAll() throws SQLException {
        List<Arg> args = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            while (rs.next()) {
                Arg arg = new Arg();
                arg.setId(rs.getInt("id"));
                arg.setName(rs.getString("name"));
                arg.setType(ArgType.getArgType(rs.getString("type")));
                arg.setAutoUpdateType(AutoUpdateType.getAutoUpdateType(rs.getString("auto_update_type")));
                arg.setLength(rs.getInt("length"));
                arg.setDefaultValue(rs.getString("default_value"));
                arg.setValue(rs.getString("value"));
                arg.setCodePath(rs.getString("code_path"));
                arg.setEnabled(rs.getInt("enabled") == 1);
                arg.setDescription(rs.getString("description"));
                arg.setPersistent(rs.getInt("persistent") == 1);
                args.add(arg);
            }
        }
        return args;
    }

    synchronized public void save(Arg arg) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_SQL)) {
            ps.setInt(1, arg.getId());
            ps.setString(2, arg.getName());
            ps.setString(3, arg.getType().toString());
            ps.setString(4, arg.getAutoUpdateType().toString());
            ps.setInt(5, arg.getLength());
            ps.setString(6, arg.getDefaultValue() != null ? arg.getDefaultValue() : "");
            ps.setString(7, arg.getValue() != null ? arg.getValue() : "");
            ps.setString(8, arg.getCodePath() != null ? arg.getCodePath() : "");
            ps.setInt(9, arg.isEnabled() ? 1 : 0);
            ps.setString(10, arg.getDescription() != null ? arg.getDescription() : "");
            ps.setInt(11, arg.isPersistent() ? 1 : 0);
            ps.executeUpdate();
        }
    }

    synchronized public void delete(int id) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_BY_ID_SQL)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    synchronized public void deleteAll() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(DELETE_ALL_SQL);
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }
}
