package game;

import java.util.TreeMap;

public final class Ranks {
    private static final TreeMap<Integer, String> TABLE = new TreeMap<>();
    static {
        TABLE.put(0, "SCRIPT KIDDIE");
        TABLE.put(51, "PACKET SNIFFER");
        TABLE.put(151, "BLACK HAT");
        TABLE.put(301, "ROOT ACCESS LEGEND");
    }

    private Ranks() { }

    public static String titleFor(int score) {
        return TABLE.floorEntry(Math.max(0, score)).getValue();
    }
}
