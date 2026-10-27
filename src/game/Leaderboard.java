package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Generic leaderboard: works with any Comparable item. */
public class Leaderboard<T extends Comparable<T>> {
    private final List<T> items = new ArrayList<>();

    public void add(T item) { items.add(item); }

    public List<T> top(int n) {
        List<T> sorted = new ArrayList<>(items);
        Collections.sort(sorted, Collections.reverseOrder());
        return sorted.subList(0, Math.min(n, sorted.size()));
    }

    public int size() { return items.size(); }
}
