package ui;

import db.Database;
import game.Leaderboard;
import game.ScoreRecord;

import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class LeaderboardScreen extends JPanel {
    private final HackerFrame frame;
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"#", "CODENAME", "SCORE", "LAYER", "LONGEST", "RESULT", "DATE"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JLabel message = new JLabel(" ", SwingConstants.CENTER);

    public LeaderboardScreen(HackerFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("TOP HACKERS", SwingConstants.CENTER);
        title.setFont(Theme.mono(Font.BOLD, 30));
        title.setForeground(Theme.CYAN);
        add(title, BorderLayout.NORTH);

        JTable table = new JTable(model);
        Theme.styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Theme.PANEL);
        add(sp, BorderLayout.CENTER);

        message.setForeground(Theme.AMBER);
        message.setFont(Theme.mono(Font.PLAIN, 13));
        HackButton back = new HackButton("[ BACK ]");
        back.addActionListener(e -> frame.showMenu());
        HackButton play = new HackButton("[ NEW BREACH ]");
        play.addActionListener(e -> frame.startGame());
        JPanel south = new JPanel(new BorderLayout(0, 8));
        south.setOpaque(false);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btns.setOpaque(false);
        btns.add(back);
        btns.add(play);
        south.add(message, BorderLayout.NORTH);
        south.add(btns, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }

    public void refresh() {
        model.setRowCount(0);
        message.setText(" ");
        Database db = frame.getDatabase();
        try {
            if (db == null) throw new SQLException("database offline");
            Leaderboard<ScoreRecord> board = db.loadLeaderboard();
            int rank = 1;
            for (ScoreRecord r : board.top(20)) {
                model.addRow(new Object[]{rank++, r.getPlayerName(), r.getScore(),
                        "FW" + r.getLayerReached(), r.getLongestSequence(), r.getResult(), r.getTimestamp()});
            }
            if (board.size() == 0) message.setText("No runs recorded yet. Be the first.");
        } catch (SQLException e) {
            message.setText("DB ERROR: " + e.getMessage());
        }
    }
}
