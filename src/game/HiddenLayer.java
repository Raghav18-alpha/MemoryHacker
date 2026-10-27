package game;

import java.util.ArrayList;
import java.util.List;

public class HiddenLayer extends Layer {
    @Override public String getName() { return "HIDDEN"; }
    @Override public String getDescription() { return "Nodes shuffle positions after the display. Remember the COLORS."; }
    @Override public boolean shufflesButtons() { return true; }

    @Override
    public List<Integer> expectedInput(List<Integer> sequence) {
        return new ArrayList<>(sequence);
    }
}
