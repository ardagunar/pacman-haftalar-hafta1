package pacman;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;

/**
 * Kiraz (5. hafta). GameObject'ten kalıtım alır, tıpkı Food gibi.
 * Pac-Man kirazı yiyince hayaletler 7 saniye boyunca yenebilir olur
 * (süre ve hayaletlerin korkutulması PowerManager'da).
 *
 * Sorumlu: Kaan Avşar
 */
public class Cherry extends GameObject {

    public static final int POINTS = 50;

    private static final Color RED = new Color(230, 25, 45);
    private static final Color DARK_RED = new Color(150, 10, 30);
    private static final Color STEM = new Color(120, 190, 70);
    private static final Color SHINE = new Color(255, 255, 255, 190);

    private boolean eaten = false;

    public Cherry(int row, int col) {
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
        double cx = x + TILE / 2.0;
        double cy = y + TILE / 2.0;

        // Saplar: yukarıdaki ortak noktadan iki kiraza iner
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(STEM);
        g.draw(new QuadCurve2D.Double(cx + 3, cy - 10, cx - 3, cy - 6, cx - 5, cy + 1));
        g.draw(new QuadCurve2D.Double(cx + 3, cy - 10, cx + 6, cy - 4, cx + 5, cy + 3));
        // Yaprak
        g.fill(new Ellipse2D.Double(cx + 2, cy - 12, 7, 4));

        // İki kiraz tanesi
        drawBerry(g, cx - 5.5, cy + 4, 5.5);
        drawBerry(g, cx + 5, cy + 6, 5);
    }

    private void drawBerry(Graphics2D g, double bx, double by, double r) {
        g.setColor(DARK_RED);
        g.fill(new Ellipse2D.Double(bx - r, by - r, 2 * r, 2 * r));
        g.setColor(RED);
        g.fill(new Ellipse2D.Double(bx - r, by - r, 2 * r - 1.5, 2 * r - 1.5));
        g.setColor(SHINE);
        g.fill(new Ellipse2D.Double(bx - r * 0.55, by - r * 0.6, r * 0.55, r * 0.55));
    }
}
