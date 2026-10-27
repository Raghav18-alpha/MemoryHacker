package exceptions;

/** Thrown when the player enters a wrong node or runs out of time. */
public class WrongPatternException extends Exception {
    private final int expected;
    private final int actual;
    private final boolean timeout;

    public WrongPatternException(String message, int expected, int actual) {
        super(message);
        this.expected = expected;
        this.actual = actual;
        this.timeout = false;
    }

    // Overloaded constructor used when the hack timer expires
    public WrongPatternException(String message) {
        super(message);
        this.expected = -1;
        this.actual = -1;
        this.timeout = true;
    }

    public int getExpected() { return expected; }
    public int getActual() { return actual; }
    public boolean isTimeout() { return timeout; }
}
