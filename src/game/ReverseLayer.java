package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReverseLayer extends Layer {
    @Override public String getName() { return "REVERSE"; }
    @Override public String getDescription() { return "Repeat the pattern BACKWARDS."; }

    @Override
    public List<Integer> expectedInput(List<Integer> sequence) {
        List<Integer> copy = new ArrayList<>(sequence);
        Collections.reverse(copy);
        return copy;
    }
}
