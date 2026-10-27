package game;

import exceptions.WrongPatternException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Pure game logic. No Swing code in here. */
public class GameEngine {
    /** Rounds in each firewall: firewall 1 = 3 rounds, firewall 2 = 2 rounds, firewall 3 = 1 (final) round. */
    public static final int[] FIREWALL_ROUNDS = {3, 2, 1};
    public static final int FIREWALLS = FIREWALL_ROUNDS.length;
    /** Rounds per firewall once Endless Mode goes past the last firewall. */
    private static final int ENDLESS_ROUNDS = 3;
    public static final int TOTAL_ROUNDS = sum(FIREWALL_ROUNDS);
    public static final int NODES = 4;

    // Layer order for the firewalls (endless mode cycles through them)
    private static final Layer[] LAYERS = {
        new NormalLayer(), new ReverseLayer(), new HiddenLayer()
    };

    private final Random rng = new Random();
    private final List<Integer> sequence = new ArrayList<>();
    private PowerUp replay = new Replay();
    private PowerUp slowMo = new SlowMo();

    private int round, score, position;
    private int correctClicks, totalClicks, longestSequence, powerUpsUsed;
    private long reactionSumNanos, lastPromptNanos, startNanos;
    private int reactionCount;
    private boolean endless, slowMoActive, replayPending;

    public GameEngine() { reset(); }

    public synchronized void reset() {
        sequence.clear();
        replay = new Replay();
        slowMo = new SlowMo();
        round = 0; score = 0; position = 0;
        correctClicks = 0; totalClicks = 0; longestSequence = 0; powerUpsUsed = 0;
        reactionSumNanos = 0; reactionCount = 0;
        endless = false; slowMoActive = false; replayPending = false;
        startNanos = System.nanoTime();
    }

    public synchronized void nextRound() {
        round++;
        sequence.add(rng.nextInt(NODES));
        position = 0;
        longestSequence = Math.max(longestSequence, sequence.size());
    }

    public synchronized void beginInput() {
        position = 0;
        lastPromptNanos = System.nanoTime();
    }

    /**
     * Checks one click against the expected order.
     * @return true when the whole pattern has been entered correctly
     */
    public synchronized boolean submit(int node) throws WrongPatternException {
        List<Integer> expected = getLayer().expectedInput(sequence);
        totalClicks++;
        int want = expected.get(position);
        if (node != want) {
            throw new WrongPatternException("Wrong node: expected "
                    + nodeName(want) + " but got " + nodeName(node), want, node);
        }
        correctClicks++;
        long now = System.nanoTime();
        reactionSumNanos += now - lastPromptNanos;
        reactionCount++;
        lastPromptNanos = now;
        score += 2 * getLayerNumber();
        position++;
        if (position == expected.size()) {
            score += 20;
            return true;
        }
        return false;
    }

    private String nodeName(int id) {
        return new String[]{"ALPHA", "BRAVO", "CHARLIE", "DELTA"}[id];
    }

    // ---- layer info ----
    private static int sum(int[] a) { int t = 0; for (int x : a) t += x; return t; }
    private static int roundsIn(int firewall) {
        return firewall <= FIREWALLS ? FIREWALL_ROUNDS[firewall - 1] : ENDLESS_ROUNDS;
    }
    /** Last round number (1-based, whole game) that belongs to the given firewall. */
    private static int lastRoundOf(int firewall) {
        int end = 0;
        for (int f = 1; f <= firewall; f++) end += roundsIn(f);
        return end;
    }
    public synchronized int getLayerNumber() {
        int fw = 1;
        while (round > lastRoundOf(fw)) fw++;
        return fw;
    }
    public synchronized Layer getLayer() { return LAYERS[(getLayerNumber() - 1) % LAYERS.length]; }
    /** Layer used by firewall number n (1-based). Endless mode cycles. */
    public Layer getLayerByNumber(int n) { return LAYERS[(Math.max(1, n) - 1) % LAYERS.length]; }
    public synchronized boolean isFirstRoundOfLayer() { return round == lastRoundOf(getLayerNumber() - 1) + 1; }
    public synchronized boolean isLastRoundOfLayer() { return round == lastRoundOf(getLayerNumber()); }
    public synchronized boolean isVictory() { return !endless && round >= TOTAL_ROUNDS; }

    // ---- speed / timer ----
    /** Delay per node in ms. Consumes an armed Slow-mo. */
    public synchronized int displayDelayMs() {
        int d = Math.max(250, 700 - round * 40);
        if (slowMoActive) {
            d = (int) (d * 1.8);
            slowMoActive = false;
        }
        return d;
    }

    public synchronized long timeLimitMs(double difficulty) {
        long base = Math.max(1800, 5000 - (getLayerNumber() - 1) * 600L);
        return (long) (base * difficulty);
    }

    public synchronized int applyTimeBonus() {
        long seconds = (System.nanoTime() - startNanos) / 1_000_000_000L;
        int bonus = (int) Math.max(0, 300 - seconds * 3);
        score += bonus;
        return bonus;
    }

    public synchronized List<Integer> shuffledOrder() {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < NODES; i++) order.add(i);
        Collections.shuffle(order, rng);
        return order;
    }

    // ---- power-ups ----
    public PowerUp getReplay() { return replay; }
    public PowerUp getSlowMo() { return slowMo; }
    public synchronized void setReplayPending(boolean v) { replayPending = v; }
    public synchronized boolean consumeReplay() { boolean v = replayPending; replayPending = false; return v; }
    public synchronized void setSlowMo(boolean v) { slowMoActive = v; }
    public synchronized void countPowerUp() { powerUpsUsed++; }

    // ---- getters / stats ----
    public synchronized List<Integer> getSequence() { return new ArrayList<>(sequence); }
    public synchronized int getRound() { return round; }
    public synchronized int getScore() { return score; }
    public synchronized int getLongestSequence() { return longestSequence; }
    public synchronized int getPowerUpsUsed() { return powerUpsUsed; }
    public synchronized boolean isEndless() { return endless; }
    public synchronized void setEndless(boolean v) { endless = v; }
    public synchronized int getAccuracy() { return totalClicks == 0 ? 100 : 100 * correctClicks / totalClicks; }
    public synchronized double getAvgReactionSeconds() {
        return reactionCount == 0 ? 0 : reactionSumNanos / (double) reactionCount / 1e9;
    }
    public synchronized String getRank() { return Ranks.titleFor(score); }
}
