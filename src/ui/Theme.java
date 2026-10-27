package ui;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JMenuItem;
import javax.swing.JTable;

public final class Theme {
    public static final Color BG = new Color(8, 12, 10);
    public static final Color PANEL = new Color(14, 22, 18);
    public static final Color GREEN = new Color(0, 255, 102);
    public static final Color CYAN = new Color(0, 229, 255);
    public static final Color MAGENTA = new Color(255, 0, 229);
    public static final Color AMBER = new Color(255, 204, 0);
    public static final Color RED = new Color(255, 40, 60);
    public static final Color DIM = new Color(0, 110, 50);

    public static final Color[] NODE_COLORS = {GREEN, CYAN, MAGENTA, AMBER};
    public static final String[] NODE_NAMES = {"ALPHA", "BRAVO", "CHARLIE", "DELTA"};

    private Theme() { }

    public static Font mono(int style, int size) {
        return new Font(Font.MONOSPACED, style, size);
    }

    public static Color dim(Color c) {
        return new Color(c.getRed() / 4, c.getGreen() / 4, c.getBlue() / 4);
    }

    public static <T extends JMenuItem> T style(T item) {
        item.setOpaque(true);
        item.setBackground(PANEL);
        item.setForeground(GREEN);
        item.setFont(mono(Font.PLAIN, 13));
        return item;
    }

    public static void styleTable(JTable t) {
        t.setBackground(PANEL);
        t.setForeground(GREEN);
        t.setGridColor(DIM);
        t.setFont(mono(Font.PLAIN, 13));
        t.setRowHeight(26);
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(DIM);
        t.setSelectionForeground(Color.WHITE);
        t.getTableHeader().setBackground(BG);
        t.getTableHeader().setForeground(CYAN);
        t.getTableHeader().setFont(mono(Font.BOLD, 13));
    }
}
