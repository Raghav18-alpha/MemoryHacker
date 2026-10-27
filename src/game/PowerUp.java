package game;

/** Contract for every power-up in the game. */
public interface PowerUp {
    String getName();
    int getUsesLeft();
    default boolean isAvailable() { return getUsesLeft() > 0; }
    /** Activates the power-up and returns a terminal message. */
    String activate(GameEngine engine);
}
