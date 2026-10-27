package ui;

import javax.swing.SwingUtilities;

/** Background thread that counts down the time allowed per step. */
public class HackTimer extends Thread {
    public interface Listener {
        void onTick(long msLeft, long totalMs);
        void onTimeout();
    }

    private final Listener listener;
    private boolean armed;
    private long deadline;
    private long total = 1;
    private volatile boolean running = true;

    public HackTimer(Listener listener) {
        super("hack-timer");
        this.listener = listener;
        setDaemon(true);
    }

    public synchronized void arm(long ms) {
        total = Math.max(1, ms);
        deadline = System.currentTimeMillis() + ms;
        armed = true;
    }

    public synchronized void disarm() { armed = false; }

    public void shutdown() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return;
            }
            boolean isArmed;
            long left = 0;
            long tot;
            boolean fire = false;
            synchronized (this) {
                isArmed = armed;
                tot = total;
                if (isArmed) {
                    left = deadline - System.currentTimeMillis();
                    if (left <= 0) {
                        armed = false;
                        fire = true;
                    }
                }
            }
            if (!isArmed) continue;
            final long shown = Math.max(0, left);
            final long totalMs = tot;
            final boolean timedOut = fire;
            SwingUtilities.invokeLater(() -> {
                listener.onTick(shown, totalMs);
                if (timedOut) listener.onTimeout();
            });
        }
    }
}
