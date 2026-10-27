package ui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Matrix-style falling code. Animation is driven by a daemon thread. */
public class MatrixRain extends JPanel {
    private static final String CHARS = "01ABCDEF<>/\\|{}[]#$%&*+=";
    private static final int CELL = 16;

    private final String caption;
    private final Random rnd = new Random();
    private BufferedImage buffer;
    private int[] drops;
    private Thread worker;
    private volatile boolean running;

    public MatrixRain(String caption) {
        this.caption = caption;
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(520, 400));
    }

    public void startRain() {
        running = true;
        worker = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(60);
                } catch (InterruptedException e) {
                    return;
                }
                SwingUtilities.invokeLater(this::step);
            }
        }, "matrix-rain");
        worker.setDaemon(true);
        worker.start();
    }

    public void stopRain() {
        running = false;
        if (worker != null) worker.interrupt();
    }

    private void step() {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        if (buffer == null || buffer.getWidth() != w || buffer.getHeight() != h) {
            buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            drops = new int[w / CELL + 1];
            for (int i = 0; i < drops.length; i++) drops[i] = -rnd.nextInt(30);
        }
        Graphics2D g = buffer.createGraphics();
        g.setColor(new Color(0, 0, 0, 40));
        g.fillRect(0, 0, w, h);
        g.setFont(Theme.mono(Font.BOLD, CELL));
        for (int i = 0; i < drops.length; i++) {
            if (drops[i] >= 0) {
                char c = CHARS.charAt(rnd.nextInt(CHARS.length()));
                g.setColor(rnd.nextInt(10) == 0 ? Color.WHITE : Theme.GREEN);
                g.drawString(String.valueOf(c), i * CELL, drops[i] * CELL + CELL);
            }
            if (drops[i] * CELL > h && rnd.nextInt(40) == 0) drops[i] = 0;
            else drops[i]++;
        }
        g.dispose();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (buffer != null) g.drawImage(buffer, 0, 0, null);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(Theme.mono(Font.BOLD, 22));
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(caption);
        int x = (getWidth() - tw) / 2, y = getHeight() / 2;
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(x - 20, y - fm.getAscent() - 10, tw + 40, fm.getHeight() + 20);
        g2.setColor(Theme.GREEN);
        g2.drawString(caption, x, y);
        g2.dispose();
    }
}
