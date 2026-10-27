package game;

public class SlowMo implements PowerUp {
    private int usesLeft = 1;

    @Override public String getName() { return "SLOW-MO"; }
    @Override public int getUsesLeft() { return usesLeft; }

    @Override
    public String activate(GameEngine engine) {
        usesLeft--;
        engine.setSlowMo(true);
        engine.countPowerUp();
        return "> POWER-UP: Slow-mo armed. Next pattern display is slowed.";
    }
}
