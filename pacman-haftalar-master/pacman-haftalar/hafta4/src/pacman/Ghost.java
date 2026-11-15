package pacman;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Hayalet.
 *
 * Sorumlular:
 *   Meriç Kavuncuoğlu -> sınıf, konum/hız/yön, çizim (4. hafta: dalgalı etek, yöne bakan gözler)
 *   Egemen Çavuş      -> hareket; 4. hafta: akıcı hareket ve kavşakta yön seçimi
 */
public class Ghost extends MovingObject {

    private static final int[][] DIRECTIONS = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

    private final Color color;
    private final Random random = new Random();
    private int animFrame = 0;

    /** speed: piksel/frame, 24'ü tam bölmeli (1, 2, 3, 4, 6...). */
    public Ghost(int row, int col, Color color, int speed) {
        super(row, col, speed);
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    // ---------- Egemen: hareket ----------
    @Override
    public void update(Maze maze) {
        if (isAtTileCenter()) {
            chooseDirection(maze);
        }
        step();
        animFrame++;
    }

    /**
     * 3. haftada hayalet sadece duvara çarpınca yön değiştiriyordu ve aynı
     * koridorda ileri geri gidip gelebiliyordu. 4. haftada her kavşakta,
     * geri dönüş hariç açık yönlerden rastgele biri seçiliyor; böylece
     * labirentin her yerini dolaşıyor. Çıkmaz yolda ise geri dönüyor.
     */
    private void chooseDirection(Maze maze) {
        List<int[]> options = new ArrayList<>();
        for (int[] d : DIRECTIONS) {
            boolean isReverse = isMoving() && d[0] == -dRow && d[1] == -dCol;
            if (!isReverse && canGo(maze, d[0], d[1])) {
                options.add(d);
            }
        }
        if (options.isEmpty()) {
            // Çıkmaz yol: geri dön
            dRow = -dRow;
            dCol = -dCol;
            return;
        }
        int[] chosen = options.get(random.nextInt(options.size()));
        dRow = chosen[0];
        dCol = chosen[1];
    }

    // ---------- Meriç: çizim ----------
    @Override
    public void draw(Graphics2D g) {
        double w = TILE + 2;
        double gx = x + TILE / 2.0 - w / 2;
        double gy = y + TILE / 2.0 - w / 2;

        // Gövde: üstü yarım daire, altı dalgalı etek
        Path2D body = new Path2D.Double();
        body.moveTo(gx, gy + w / 2);
        body.append(new Arc2D.Double(gx, gy, w, w, 180, -180, Arc2D.OPEN), true);
        body.lineTo(gx + w, gy + w);

        int teeth = 6;
        double seg = w / teeth;
        int phase = (animFrame / 8) % 2; // etek hareket ediyormuş gibi
        for (int i = teeth - 1; i >= 0; i--) {
            double px = gx + i * seg;
            double py = ((i + phase) % 2 == 0) ? gy + w : gy + w - 4;
            body.lineTo(px, py);
        }
        body.closePath();

        g.setPaint(new GradientPaint((float) gx, (float) gy, color.brighter(),
                                     (float) gx, (float) (gy + w), color.darker()));
        g.fill(body);

        // Gözler: baktığı yöne doğru kayıyor
        double eyeW = w * 0.28;
        double eyeH = w * 0.34;
        double eyeY = gy + w * 0.22;
        double leftEyeX = gx + w * 0.17;
        double rightEyeX = gx + w * 0.55;
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(leftEyeX, eyeY, eyeW, eyeH));
        g.fill(new Ellipse2D.Double(rightEyeX, eyeY, eyeW, eyeH));

        double pupil = w * 0.15;
        double offX = dCol * eyeW * 0.25;
        double offY = dRow * eyeH * 0.25;
        g.setColor(new Color(20, 30, 140));
        g.fill(new Ellipse2D.Double(leftEyeX + (eyeW - pupil) / 2 + offX, eyeY + (eyeH - pupil) / 2 + offY, pupil, pupil));
        g.fill(new Ellipse2D.Double(rightEyeX + (eyeW - pupil) / 2 + offX, eyeY + (eyeH - pupil) / 2 + offY, pupil, pupil));
    }
}
