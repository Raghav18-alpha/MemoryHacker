package db;

import game.Leaderboard;
import game.ScoreRecord;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** JDBC layer. Uses a local SQLite file (memoryhacker.db), no server needed. */
public class Database {
    private static final String URL = "jdbc:sqlite:memoryhacker.db";

    public Database() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found. Put sqlite-jdbc.jar in lib/", e);
        }
        createTable();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS scores ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "player_name TEXT NOT NULL,"
                + "score INTEGER NOT NULL,"
                + "layer_reached INTEGER,"
                + "longest_sequence INTEGER,"
                + "result TEXT,"
                + "played_at TEXT)";
        try (Connection con = connect(); Statement st = con.createStatement()) {
            st.execute(sql);
        }
    }

    public void saveScore(ScoreRecord r) throws SQLException {
        String sql = "INSERT INTO scores(player_name, score, layer_reached, longest_sequence, result, played_at) "
                + "VALUES (?,?,?,?,?,?)";
        try (Connection con = connect(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, r.getPlayerName());
            ps.setInt(2, r.getScore());
            ps.setInt(3, r.getLayerReached());
            ps.setInt(4, r.getLongestSequence());
            ps.setString(5, r.getResult());
            ps.setString(6, r.getTimestamp());
            ps.executeUpdate();
        }
    }

    public int getBestScore() throws SQLException {
        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(score), 0) FROM scores")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Leaderboard<ScoreRecord> loadLeaderboard() throws SQLException {
        Leaderboard<ScoreRecord> board = new Leaderboard<>();
        String sql = "SELECT player_name, score, layer_reached, longest_sequence, result, played_at "
                + "FROM scores ORDER BY score DESC LIMIT 100";
        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                board.add(new ScoreRecord(rs.getString(1), rs.getInt(2), rs.getInt(3),
                        rs.getInt(4), rs.getString(5), rs.getString(6)));
            }
        }
        return board;
    }
}
