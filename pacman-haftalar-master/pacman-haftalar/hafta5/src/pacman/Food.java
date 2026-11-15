package pacman;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Pac-Man'in topladığı yem.
 *
 * Sorumlu: Kaan Avşar (yem sınıfı; 4. hafta: parlayan yem görünümü)
 */
public class Food extends GameObject {

    public static final int POINTS = 10;

    private static final Color GLOW = new Color(255, 200, 160, 60);
    private static final Color DOT = new Color(255, 214, 180);

    private boolean eaten = false;

    public Food(int row, int col) {
        super(row, col);
    }

    public boolean isEaten() {
        return eaten;
    }

    public void setEaten(boolean eaten) {
        this.eaten = eaten;
    }

    @Override
    public void draw(Graphics2D g) {
        if (eaten) {
            return;
        }
        int cx = x + TILE / 2;
        int cy = y + TILE / 2;
        g.setColor(GLOW);
        g.fillOval(cx - 5, cy - 5, 10, 10);
        g.setColor(DOT);
        g.fillOval(cx - 2, cy - 2, 5, 5);
    }
}
