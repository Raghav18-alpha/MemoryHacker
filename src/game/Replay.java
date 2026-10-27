package game;

public class Replay implements PowerUp {
    private int usesLeft = 1;

    @Override public String getName() { return "REPLAY"; }
    @Override public int getUsesLeft() { return usesLeft; }

    @Override
    public String activate(GameEngine engine) {
        usesLeft--;
        engine.setReplayPending(true);
        engine.countPowerUp();
        return "> POWER-UP: Replay loaded. Re-sending pattern...";
    }
}
