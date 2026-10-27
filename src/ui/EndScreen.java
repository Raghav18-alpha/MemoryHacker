package ui;

import game.GameEngine;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/** Shared ending screen: lockout (loss) or victory. Shows the stats table. */
public class EndScreen extends JPanel {
    private final HackerFrame frame;
    private final JLabel title = new JLabel("", SwingConstants.CENTER);
    private final JPanel center = new JPanel(new BorderLayout());
    private final DefaultTableModel model = new DefaultTableModel(new Object[]{"STAT", "VALUE"}, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final HackButton endlessBtn = new HackButton("[ ENDLESS MODE ]");
    private MatrixRain rain;
    private javax.swing.Timer typer;

    public EndScreen(HackerFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(14, 14, 14, 14));

        title.setFont(Theme.mono(Font.BOLD, 32));
        add(title, BorderLayout.NORTH);

        center.setOpaque(false);
        add(center, BorderLayout.CENTER);

        JTable table = new JTable(model);
        Theme.styleTable(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(340, 100));
        sp.getViewport().setBackground(Theme.PANEL);
        add(sp, BorderLayout.EAST);

        HackButton retry = new HackButton("[ RETRY BREACH ]");
        HackButton board = new HackButton("[ VIEW LEADERBOARD ]");
        HackButton menu = new HackButton("[ MENU ]");
        retry.addActionListener(e -> frame.startGame());
        board.addActionListener(e -> frame.showLeaderboard());
        menu.addActionListener(e -> frame.showMenu());
        endlessBtn.addActionListener(e -> frame.startEndless());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);
        buttons.add(retry);
        buttons.add(board);
        buttons.add(endlessBtn);
        buttons.add(menu);
        add(buttons, BorderLayout.SOUTH);
    }

    public void present(GameEngine e, boolean won, String cause) {
        if (rain != null) { rain.stopRain(); rain = null; }
        if (typer != null) typer.stop();
        center.removeAll();

        model.setRowCount(0);
        model.addRow(new Object[]{"Result", won ? "WON" : (e.isEndless() ? "ENDLESS RUN OVER" : "LOST")});
        model.addRow(new Object[]{"Final score", e.getScore()});
        model.addRow(new Object[]{"Highest layer", "Firewall " + e.getLayerNumber() + " (" + e.getLayer().getName() + ")"});
        model.addRow(new Object[]{"Longest sequence", e.getLongestSequence() + " steps"});
        model.addRow(new Object[]{"Accuracy", e.getAccuracy() + "%"});
        model.addRow(new Object[]{"Avg reaction", String.format("%.2fs", e.getAvgReactionSeconds())});
        model.addRow(new Object[]{"Power-ups used", e.getPowerUpsUsed()});
        model.addRow(new Object[]{"Rank", e.getRank()});

        if (won) {
            title.setText("ACCESS GRANTED");
            title.setForeground(Theme.GREEN);
            rain = new MatrixRain("YOU HAVE HACKED THE MAINFRAME");
            center.add(rain, BorderLayout.CENTER);
            rain.startRain();
        } else {
            title.setText("INTRUSION DETECTED");
            title.setForeground(Theme.RED);
            JTextArea term = new JTextArea();
            term.setEditable(false);
            term.setBackground(Color.BLACK);
            term.setForeground(Theme.RED);
            term.setFont(Theme.mono(Font.BOLD, 15));
            center.add(new JScrollPane(term), BorderLayout.CENTER);
            typeLines(term, Arrays.asList(
                    "> INTRUSION DETECTED",
                    "> " + cause,
                    "> Trace complete. Connection terminated.",
                    "> You reached Firewall " + e.getLayerNumber() + ", Round " + e.getRound()
                            + ". Score: " + e.getScore(),
                    "> Rank: " + e.getRank()));
        }
        endlessBtn.setVisible(won && !e.isEndless());
        center.revalidate();
        center.repaint();
    }

    /** Typewriter effect: one line every 350 ms. */
    private void typeLines(JTextArea area, List<String> lines) {
        final int[] i = {0};
        typer = new javax.swing.Timer(350, null);
        typer.addActionListener(ev -> {
            if (i[0] < lines.size()) area.append(lines.get(i[0]++) + "\n");
            else typer.stop();
        });
        typer.setInitialDelay(100);
        typer.start();
    }
}
