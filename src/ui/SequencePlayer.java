package ui;

import java.util.List;
import javax.swing.SwingUtilities;

/** Thread that lights the nodes one by one. Input stays locked until it finishes. */
public class SequencePlayer extends Thread {
    public interface Display {
        void light(int node, boolean on);
        void finished();
    }

    private final List<Integer> sequence;
    private final int delayMs;
    private final Display display;

    public SequencePlayer(List<Integer> sequence, int delayMs, Display display) {
        super("sequence-player");
        this.sequence = sequence;
        this.delayMs = delayMs;
        this.display = display;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(500);
            for (int node : sequence) {
                SwingUtilities.invokeLater(() -> display.light(node, true));
                Thread.sleep(delayMs);
                SwingUtilities.invokeLater(() -> display.light(node, false));
                Thread.sleep(Math.max(100, delayMs / 2));
            }
        } catch (InterruptedException e) {
            return; // cancelled
        }
        SwingUtilities.invokeLater(display::finished);
    }
}
