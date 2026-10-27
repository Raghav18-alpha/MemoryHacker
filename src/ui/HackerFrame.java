package ui;

import db.Database;
import game.GameEngine;

import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;

/** Main window. Uses CardLayout to switch between screens. */
public class HackerFrame extends JFrame {
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private Database database;
    private String dbError = "";

    private final JTextField nameField = new JTextField("GHOST", 14);
    private final JCheckBoxMenuItem timerItem = new JCheckBoxMenuItem("Hack timer enabled", true);
    private final JCheckBoxMenuItem soundItem = new JCheckBoxMenuItem("Sound effects", true);
    private final JRadioButtonMenuItem easyItem = new JRadioButtonMenuItem("Easy (more time)");
    private final JRadioButtonMenuItem normalItem = new JRadioButtonMenuItem("Normal", true);
    private final JRadioButtonMenuItem hardItem = new JRadioButtonMenuItem("Hard (less time)");

    private final GameScreen gameScreen;
    private final EndScreen endScreen;
    private final LeaderboardScreen boardScreen;

    public HackerFrame() {
        super("MEMORY HACKER");
        try {
            database = new Database();
        } catch (SQLException e) {
            database = null;
            dbError = e.getMessage();
        }

        gameScreen = new GameScreen(this);
        endScreen = new EndScreen(this);
        boardScreen = new LeaderboardScreen(this);

        root.setBackground(Theme.BG);
        root.add(buildMenuScreen(), "menu");
        root.add(gameScreen, "game");
        root.add(endScreen, "end");
        root.add(boardScreen, "board");
        setContentPane(root);
        setJMenuBar(buildMenuBar());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 660);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);
    }

    // ---------------------------------------------------------------- navigation

    public void showMenu() { cards.show(root, "menu"); }

    public void startGame() {
        cards.show(root, "game");
        gameScreen.startGame();
    }

    public void startEndless() {
        cards.show(root, "game");
        gameScreen.continueEndless();
    }

    public void showEnd(GameEngine engine, boolean won, String cause) {
        endScreen.present(engine, won, cause);
        cards.show(root, "end");
    }

    public void showLeaderboard() {
        boardScreen.refresh();
        cards.show(root, "board");
    }

    // ---------------------------------------------------------------- settings

    public Database getDatabase() { return database; }
    public boolean isTimerEnabled() { return timerItem.isSelected(); }

    public double getDifficultyMultiplier() {
        if (easyItem.isSelected()) return 1.5;
        if (hardItem.isSelected()) return 0.7;
        return 1.0;
    }

    public String getPlayerName() {
        String n = nameField.getText().trim().toUpperCase();
        if (n.isEmpty()) n = "ANON";
        return n.length() > 16 ? n.substring(0, 16) : n;
    }

    // ---------------------------------------------------------------- UI builders

    private JPanel buildMenuScreen() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Theme.BG);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.insets = new Insets(8, 0, 8, 0);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("MEMORY HACKER", SwingConstants.CENTER);
        title.setFont(Theme.mono(Font.BOLD, 46));
        title.setForeground(Theme.GREEN);
        JLabel sub = new JLabel("> breach 3 firewalls. remember everything.", SwingConstants.CENTER);
        sub.setFont(Theme.mono(Font.PLAIN, 15));
        sub.setForeground(Theme.CYAN);

        JLabel nameLbl = new JLabel("CODENAME:", SwingConstants.CENTER);
        nameLbl.setForeground(Theme.AMBER);
        nameLbl.setFont(Theme.mono(Font.BOLD, 14));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setBackground(Color.BLACK);
        nameField.setForeground(Theme.GREEN);
        nameField.setCaretColor(Theme.GREEN);
        nameField.setFont(Theme.mono(Font.BOLD, 16));

        HackButton start = new HackButton("[ START BREACH ]");
        HackButton board = new HackButton("[ LEADERBOARD ]");
        HackButton exit = new HackButton("[ EXIT ]");
        start.addActionListener(e -> startGame());
        board.addActionListener(e -> showLeaderboard());
        exit.addActionListener(e -> System.exit(0));

        p.add(title, gc);
        p.add(sub, gc);
        gc.insets = new Insets(24, 0, 4, 0);
        p.add(nameLbl, gc);
        gc.insets = new Insets(4, 80, 18, 80);
        p.add(nameField, gc);
        gc.insets = new Insets(6, 60, 6, 60);
        p.add(start, gc);
        p.add(board, gc);
        p.add(exit, gc);

        if (database == null) {
            JLabel warn = new JLabel("DB OFFLINE: " + dbError, SwingConstants.CENTER);
            warn.setForeground(Theme.RED);
            warn.setFont(Theme.mono(Font.PLAIN, 12));
            gc.insets = new Insets(18, 0, 0, 0);
            p.add(warn, gc);
        }
        return p;
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(Theme.PANEL);

        JMenu game = styledMenu("Game");
        JMenuItem newGame = Theme.style(new JMenuItem("New breach"));
        JMenuItem board = Theme.style(new JMenuItem("Leaderboard"));
        JMenuItem mainMenu = Theme.style(new JMenuItem("Main menu"));
        JMenuItem exit = Theme.style(new JMenuItem("Exit"));
        newGame.addActionListener(e -> startGame());
        board.addActionListener(e -> showLeaderboard());
        mainMenu.addActionListener(e -> showMenu());
        exit.addActionListener(e -> System.exit(0));
        game.add(newGame);
        game.add(board);
        game.add(mainMenu);
        game.addSeparator();
        game.add(exit);

        JMenu settings = styledMenu("Settings");
        ButtonGroup group = new ButtonGroup();
        for (JRadioButtonMenuItem r : new JRadioButtonMenuItem[]{easyItem, normalItem, hardItem}) {
            group.add(r);
            settings.add(Theme.style(r));
        }
        settings.addSeparator();
        settings.add(Theme.style(timerItem));
        settings.add(Theme.style(soundItem));
        soundItem.addActionListener(e -> SoundFx.setEnabled(soundItem.isSelected()));

        JMenu help = styledMenu("Help");
        JMenuItem how = Theme.style(new JMenuItem("How to play"));
        how.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Watch the nodes light up, then repeat the pattern.\n\n"
                        + "Firewall layers:\n"
                        + "  NORMAL  - same order\n"
                        + "  REVERSE - backwards\n"
                        + "  HIDDEN  - buttons shuffle, go by color\n\n"
                        + "Firewall 1 = 3 rounds, firewall 2 = 2 rounds, firewall 3 = the final round.\n"
                        + "Clear all 3 firewalls to win.\n"
                        + "Keys 1-4 press the nodes (top-left to bottom-right).\n"
                        + "REPLAY re-shows the pattern, SLOW-MO slows the next display.",
                "How to play", JOptionPane.INFORMATION_MESSAGE));
        help.add(how);

        bar.add(game);
        bar.add(settings);
        bar.add(help);
        return bar;
    }

    private JMenu styledMenu(String text) {
        JMenu m = new JMenu(text);
        m.setOpaque(true);
        m.setBackground(Theme.PANEL);
        m.setForeground(Theme.GREEN);
        m.setFont(Theme.mono(Font.BOLD, 13));
        return m;
    }
}
