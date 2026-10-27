package game;

import java.util.ArrayList;
import java.util.List;

public class NormalLayer extends Layer {
    @Override public String getName() { return "NORMAL"; }
    @Override public String getDescription() { return "Repeat the pattern in the same order."; }

    @Override
    public List<Integer> expectedInput(List<Integer> sequence) {
        return new ArrayList<>(sequence);
    }
}
