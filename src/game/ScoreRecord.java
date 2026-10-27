package game;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScoreRecord implements Comparable<ScoreRecord> {
    private final String playerName;
    private final int score;
    private final int layerReached;
    private final int longestSequence;
    private final String result;     // WON / LOST / ENDLESS
    private final String timestamp;

    public ScoreRecord(String playerName, int score, int layerReached,
                       int longestSequence, String result, String timestamp) {
        this.playerName = playerName;
        this.score = score;
        this.layerReached = layerReached;
        this.longestSequence = longestSequence;
        this.result = result;
        this.timestamp = timestamp;
    }

    // Overloaded constructor: timestamp is "now"
    public ScoreRecord(String playerName, int score, int layerReached,
                       int longestSequence, String result) {
        this(playerName, score, layerReached, longestSequence, result,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public int getLayerReached() { return layerReached; }
    public int getLongestSequence() { return longestSequence; }
    public String getResult() { return result; }
    public String getTimestamp() { return timestamp; }

    @Override
    public int compareTo(ScoreRecord other) {
        return Integer.compare(this.score, other.score);
    }
}
