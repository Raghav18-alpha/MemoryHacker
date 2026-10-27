package game;

import java.util.List;

/** A security layer (firewall). Each subclass changes the rules. */
public abstract class Layer {
    public abstract String getName();
    public abstract String getDescription();

    /** The order in which the player must click the nodes. */
    public abstract List<Integer> expectedInput(List<Integer> sequence);

    /** Hidden-mode layers scramble button positions before input. */
    public boolean shufflesButtons() { return false; }
}
