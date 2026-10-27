package ui;

import db.Database;
import db.LogWriter;
import exceptions.WrongPatternException;
import game.GameEngine;
import game.Layer;
import game.ScoreRecord;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/** The main play screen. */
public class GameScreen extends JPanel implements HackTimer.Listener {
    private final HackerFrame frame;
    private final GameEngine engine = new GameEngine();
    private final NodeButton[] nodes = new NodeButton[GameEngine.NODES];
    private final JPanel grid = new JPanel(new GridLayout(2, 2, 14, 14)) {
        @Override
        protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            paintBanner(g, getWidth(), getHeight());
        }
    };
    private String bannerTitle, bannerSub;
    private Color bannerColor = Theme.GREEN;
    private javax.swing.Timer bannerTimer;
    private final JLabel scoreLabel = hudLabel("SCORE: 0");
    private final JLabel layerLabel = hudLabel("FIREWALL 1");
    private final JLabel roundLabel = hudLabel("ROUND 0");
    private final JLabel bestLabel = hudLabel("BEST: 0");
    private int best;
    private final JLabel statusLabel = new JLabel("[ STANDBY ]", SwingConstants.CENTER);
    private final JTextArea log = new JTextArea();
    private final JProgressBar timerBar = new JProgressBar(0, 1000);
    private final HackButton replayBtn = new HackButton("REPLAY");
    private final HackButton slowBtn = new HackButton("SLOW-MO");
    private final HackTimer hackTimer = new HackTimer(this);

    private SequencePlayer player;
    private int playToken;
    private javax.swing.Timer pendingNext;
    private boolean inputEnabled;

    public GameScreen(HackerFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setFocusable(true);

        // top: HUD + status
        JPanel hud = new JPanel(new GridLayout(1, 4));
        hud.setOpaque(false);
        bestLabel.setForeground(Theme.AMBER);
        hud.add(scoreLabel);
        hud.add(layerLabel);
        hud.add(roundLabel);
        hud.add(bestLabel);
        statusLabel.setFont(Theme.mono(Font.BOLD, 16));
        statusLabel.setForeground(Theme.CYAN);
        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(hud, BorderLayout.NORTH);
        top.add(statusLabel, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // center: node grid
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(10, 10, 10, 10));
        for (int i = 0; i < nodes.length; i++) nodes[i] = new NodeButton(i);
        restoreOrder();
        add(grid, BorderLayout.CENTER);

        // east: terminal log
        log.setEditable(false);
        log.setFocusable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        log.setBackground(Color.BLACK);
        log.setForeground(Theme.GREEN);
        log.setFont(Theme.mono(Font.PLAIN, 12));
        JPopupMenu popup = new JPopupMenu();
        JMenuItem clear = Theme.style(new JMenuItem("Clear log"));
        clear.addActionListener(e -> log.setText(""));
        popup.add(clear);
        log.setComponentPopupMenu(popup);
        JScrollPane scroll = new JScrollPane(log);
        scroll.setPreferredSize(new Dimension(300, 100));
        scroll.setBorder(new LineBorder(Theme.DIM));
        add(scroll, BorderLayout.EAST);

        // south: timer bar + power-ups
        timerBar.setStringPainted(true);
        timerBar.setString("HACK TIMER");
        timerBar.setBackground(Color.BLACK);
        timerBar.setForeground(Theme.GREEN);
        timerBar.setValue(1000);
        replayBtn.addActionListener(e -> onReplay());
        slowBtn.addActionListener(e -> onSlowMo());
        JPanel powers = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        powers.setOpaque(false);
        powers.add(replayBtn);
        powers.add(slowBtn);
        JPanel bottom = new JPanel(new BorderLayout(12, 0));
        bottom.setOpaque(false);
        bottom.add(powers, BorderLayout.WEST);
        bottom.add(timerBar, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // keyboard: keys 1-4 press the node in that grid position
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int pos = e.getKeyChar() - '1';
                if (pos >= 0 && pos < nodes.length && grid.getComponentCount() == nodes.length) {
                    ((NodeButton) grid.getComponent(pos)).doClick(0);
                }
            }
        });

        hackTimer.start();
        setPowerButtons(false);
    }

    private static JLabel hudLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(Theme.mono(Font.BOLD, 15));
        l.setForeground(Theme.GREEN);
        return l;
    }

    // ------------------------------------------------------------------ game flow

    public void startGame() {
        cancelPlayer();
        if (pendingNext != null) pendingNext.stop();
        hackTimer.disarm();
        clearBanner();
        engine.reset();
        for (NodeButton n : nodes) n.setLit(false);
        restoreOrder();
        log.setText("");
        println("> Initializing breach...");
        println("> Target: MAINFRAME-X // " + GameEngine.FIREWALLS + " firewalls detected");
        println("> Operator: " + frame.getPlayerName());
        println("> Keys 1-4 also work (top-left to bottom-right).");
        LogWriter.log("New game started by " + frame.getPlayerName());
        best = 0;
        Database db = frame.getDatabase();
        if (db != null) {
            try {
                best = db.getBestScore();
            } catch (SQLException ignored) {
                // leave best at 0
            }
        }
        updateHud();
        requestFocusInWindow();
        Layer first = engine.getLayerByNumber(1);
        setStatus("[ CONNECTING... ]", Theme.CYAN);
        showBanner("BREACH INITIATED",
                "Firewall 1: " + first.getName() + " - " + first.getDescription(), Theme.CYAN, 1900);
        pendingNext = new javax.swing.Timer(2000, e -> startRound());
        pendingNext.setRepeats(false);
        pendingNext.start();
    }

    /** Called after the victory screen when the player picks Endless Mode. */
    public void continueEndless() {
        engine.setEndless(true);
        println("> ENDLESS MODE: layers keep cycling, speed keeps climbing.");
        requestFocusInWindow();
        startRound();
    }

    private void startRound() {
        engine.nextRound();
        updateHud();
        Layer layer = engine.getLayer();
        if (engine.isFirstRoundOfLayer()) {
            boolean fin = !engine.isEndless() && engine.getLayerNumber() == GameEngine.FIREWALLS;
            println("> " + (fin ? "FINAL FIREWALL" : "FIREWALL " + engine.getLayerNumber())
                    + " detected: " + layer.getName());
            println("> " + layer.getDescription());
        }
        println("> Round " + engine.getRound() + ": watch the pattern...");
        playSequence(true);
    }

    private void playSequence(boolean shuffleAfter) {
        inputEnabled = false;
        hackTimer.disarm();
        setPowerButtons(false);
        timerBar.setValue(1000);
        timerBar.setForeground(Theme.GREEN);
        timerBar.setString("HACK TIMER");
        setStatus("[ TRANSMITTING PATTERN ]", Theme.CYAN);
        cancelPlayer();
        final int token = ++playToken;
        player = new SequencePlayer(engine.getSequence(), engine.displayDelayMs(), new SequencePlayer.Display() {
            @Override public void light(int node, boolean on) {
                if (token == playToken) {
                    nodes[node].setLit(on);
                    if (on) SoundFx.node(node);
                }
            }
            @Override public void finished() {
                if (token == playToken) onSequenceDone(shuffleAfter);
            }
        });
        player.start();
    }

    private void onSequenceDone(boolean shuffle) {
        Layer layer = engine.getLayer();
        if (shuffle && layer.shufflesButtons()) {
            shuffleNodes();
            println("> Nodes scrambled! Go by COLOR, not position.");
        }
        engine.beginInput();
        inputEnabled = true;
        setPowerButtons(true);
        setStatus(layer.getName().equals("REVERSE") ? "[ ENTER PATTERN BACKWARDS ]" : "[ ENTER PATTERN ]", Theme.GREEN);
        armTimer();
        requestFocusInWindow();
    }

    private void onNode(int id) {
        if (!inputEnabled) return;
        flash(id);
        SoundFx.node(id);
        try {
            boolean complete = engine.submit(id);
            println("> " + Theme.NODE_NAMES[id] + " ... ok");
            updateHud();
            if (complete) roundCleared();
            else armTimer();
        } catch (WrongPatternException ex) {
            failure(ex);
        }
    }

    private void roundCleared() {
        inputEnabled = false;
        hackTimer.disarm();
        setPowerButtons(false);
        println("> Pattern accepted.");
        boolean layerDone = engine.isLastRoundOfLayer();
        if (engine.isVictory()) SoundFx.win();
        else if (layerDone) SoundFx.breach();
        else SoundFx.success();
        if (layerDone) {
            println("> Firewall " + engine.getLayerNumber() + " breached...");
        }
        if (engine.isVictory()) {
            victory();
            return;
        }
        int pause = 900;
        if (layerDone) {
            int done = engine.getLayerNumber();
            Layer next = engine.getLayerByNumber(done + 1);
            setStatus("[ FIREWALL " + done + " BREACHED ]", Theme.GREEN);
            showBanner("FIREWALL " + done + " BREACHED",
                    ((done + 1 == GameEngine.FIREWALLS && !engine.isEndless()) ? "Entering the FINAL FIREWALL: " : "Entering FIREWALL " + (done + 1) + ": ")
                            + next.getName() + " - " + next.getDescription(),
                    Theme.GREEN, 2800);
            pause = 3000;
        } else {
            setStatus("[ PATTERN ACCEPTED ]", Theme.GREEN);
        }
        pendingNext = new javax.swing.Timer(pause, e -> startRound());
        pendingNext.setRepeats(false);
        pendingNext.start();
    }

    private void victory() {
        int bonus = engine.applyTimeBonus();
        updateHud();
        println("> FIREWALL " + GameEngine.FIREWALLS + " BREACHED");
        println("> Root access obtained...");
        println("> ACCESS GRANTED. SYSTEM COMPROMISED.");
        println("> Time bonus: +" + bonus);
        setStatus("[ ACCESS GRANTED ]", Theme.GREEN);
        saveScore("WON");
        javax.swing.Timer t = new javax.swing.Timer(1500, e -> frame.showEnd(engine, true, ""));
        t.setRepeats(false);
        t.start();
    }

    private void failure(WrongPatternException ex) {
        inputEnabled = false;
        hackTimer.disarm();
        setPowerButtons(false);
        if (pendingNext != null) pendingNext.stop();
        SoundFx.fail();
        println("> INTRUSION DETECTED");
        println("> " + ex.getMessage());
        setStatus("[ LOCKED OUT ]", Theme.RED);
        saveScore(engine.isEndless() ? "ENDLESS" : "LOST");
        final String cause = ex.isTimeout() ? "Hack timer expired." : ex.getMessage();
        glitch(() -> frame.showEnd(engine, false, cause));
    }

    private void saveScore(String result) {
        ScoreRecord rec = new ScoreRecord(frame.getPlayerName(), engine.getScore(),
                engine.getLayerNumber(), engine.getLongestSequence(), result);
        Database db = frame.getDatabase();
        try {
            if (db == null) throw new SQLException("database offline");
            db.saveScore(rec);
            println("> Score saved to database.");
        } catch (SQLException e) {
            println("> DB ERROR: " + e.getMessage());
        } finally {
            LogWriter.log("Game ended: " + result + ", score=" + rec.getScore()
                    + ", layer=" + rec.getLayerReached());
        }
    }

    // ------------------------------------------------------------------ power-ups

    private void onReplay() {
        if (!inputEnabled || !engine.getReplay().isAvailable()) return;
        println(engine.getReplay().activate(engine));
        engine.consumeReplay();
        playSequence(false); // pattern is shown again; input restarts from step 1
    }

    private void onSlowMo() {
        if (!inputEnabled || !engine.getSlowMo().isAvailable()) return;
        println(engine.getSlowMo().activate(engine));
        setPowerButtons(true);
    }

    private void setPowerButtons(boolean active) {
        replayBtn.setEnabled(active && engine.getReplay().isAvailable());
        slowBtn.setEnabled(active && engine.getSlowMo().isAvailable());
        replayBtn.setText("REPLAY x" + engine.getReplay().getUsesLeft());
        slowBtn.setText("SLOW-MO x" + engine.getSlowMo().getUsesLeft());
    }

    // ------------------------------------------------------------------ timer callbacks

    private void armTimer() {
        if (frame.isTimerEnabled()) {
            hackTimer.arm(engine.timeLimitMs(frame.getDifficultyMultiplier()));
        } else {
            timerBar.setValue(1000);
            timerBar.setString("TIMER OFF");
        }
    }

    @Override
    public void onTick(long msLeft, long totalMs) {
        if (!inputEnabled) return;
        int value = (int) (1000 * msLeft / totalMs);
        timerBar.setValue(value);
        timerBar.setForeground(value < 300 ? Theme.RED : Theme.GREEN);
        timerBar.setString(String.format("HACK TIMER %.1fs", msLeft / 1000.0));
    }

    @Override
    public void onTimeout() {
        if (!inputEnabled) return;
        failure(new WrongPatternException("Hack timer expired. Trace complete."));
    }

    // ------------------------------------------------------------------ helpers / effects

    private void cancelPlayer() {
        playToken++;
        if (player != null) player.interrupt();
    }

    private void restoreOrder() {
        grid.removeAll();
        for (NodeButton n : nodes) grid.add(n);
        grid.revalidate();
        grid.repaint();
    }

    private void shuffleNodes() {
        List<Integer> order = engine.shuffledOrder();
        grid.removeAll();
        for (int id : order) grid.add(nodes[id]);
        grid.revalidate();
        grid.repaint();
    }

    private void flash(int id) {
        nodes[id].setLit(true);
        javax.swing.Timer t = new javax.swing.Timer(140, e -> nodes[id].setLit(false));
        t.setRepeats(false);
        t.start();
    }

    /** Red flash + window shake, then runs 'done'. */
    private void glitch(Runnable done) {
        final Point origin = frame.getLocation();
        final int[] ticks = {0};
        javax.swing.Timer t = new javax.swing.Timer(35, null);
        t.addActionListener(e -> {
            ticks[0]++;
            setBackground(ticks[0] % 2 == 0 ? Theme.BG : new Color(90, 0, 10));
            frame.setLocation(origin.x + (int) (Math.random() * 16 - 8),
                    origin.y + (int) (Math.random() * 16 - 8));
            if (ticks[0] >= 20) {
                t.stop();
                setBackground(Theme.BG);
                frame.setLocation(origin);
                done.run();
            }
        });
        t.start();
    }

    private void showBanner(String title, String sub, Color color, int ms) {
        bannerTitle = title;
        bannerSub = sub;
        bannerColor = color;
        grid.repaint();
        if (bannerTimer != null) bannerTimer.stop();
        bannerTimer = new javax.swing.Timer(ms, e -> clearBanner());
        bannerTimer.setRepeats(false);
        bannerTimer.start();
    }

    private void clearBanner() {
        if (bannerTimer != null) bannerTimer.stop();
        bannerTitle = null;
        bannerSub = null;
        grid.repaint();
    }

    private void paintBanner(Graphics g, int w, int h) {
        if (bannerTitle == null) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int bandH = 150, y = (h - bandH) / 2;
        g2.setColor(new Color(0, 0, 0, 225));
        g2.fillRect(0, y, w, bandH);
        g2.setColor(bannerColor);
        g2.drawLine(0, y, w, y);
        g2.drawLine(0, y + bandH, w, y + bandH);
        g2.setFont(Theme.mono(Font.BOLD, 34));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(bannerTitle, (w - fm.stringWidth(bannerTitle)) / 2, y + 60);
        g2.setFont(Theme.mono(Font.BOLD, 15));
        g2.setColor(Theme.CYAN);
        fm = g2.getFontMetrics();
        String[] lines = bannerSub.split(" - ", 2);   // headline / rule on two lines
        for (int i = 0; i < lines.length; i++) {
            g2.drawString(lines[i], Math.max(8, (w - fm.stringWidth(lines[i])) / 2), y + 98 + i * 24);
        }
        g2.dispose();
    }

    private void updateHud() {
        scoreLabel.setText("SCORE: " + engine.getScore());
        layerLabel.setText("FIREWALL " + engine.getLayerNumber() + " [" + engine.getLayer().getName() + "]");
        roundLabel.setText("ROUND " + engine.getRound());
        bestLabel.setText("BEST: " + Math.max(best, engine.getScore()));
    }

    private void setStatus(String text, Color c) {
        statusLabel.setText(text);
        statusLabel.setForeground(c);
    }

    private void println(String line) {
        log.append(line + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    // ------------------------------------------------------------------ inner class

    /** One colored node. Inner class so it can call onNode() of the screen. */
    private class NodeButton extends JButton {
        private final int id;
        private boolean lit;
        private boolean hover;

        NodeButton(int id) {
            this.id = id;
            setFocusable(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            addActionListener(e -> onNode(this.id));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
            });
        }

        void setLit(boolean v) {
            lit = v;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color c = Theme.NODE_COLORS[id];
            Color fill = lit ? c : (hover && inputEnabled ? Theme.dim(c).brighter() : Theme.dim(c));
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
            g2.setStroke(new BasicStroke(lit ? 4f : 2f));
            g2.setColor(c);
            g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 28, 28);
            g2.setFont(Theme.mono(Font.BOLD, 20));
            g2.setColor(lit ? Color.BLACK : c);
            String label = Theme.NODE_NAMES[id];
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, (getWidth() - fm.stringWidth(label)) / 2,
                    (getHeight() + fm.getAscent()) / 2 - 4);
            g2.dispose();
        }
    }
}
