package pacman;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;

/**
 * Oyuncunun kontrol ettiği karakter.
 *
 * Sorumlular:
 *   Meriç Kavuncuoğlu -> sınıf, yön yapısı, çizim; 4. hafta: akıcı hareket,
 *                        yön tamponu, ağız animasyonu, parlak görünüm
 *   Egemen Çavuş      -> canMove(): duvar çarpışma kontrolü
 */
public class Pacman extends MovingObject {

    public static final int SPEED = 3; // piksel / frame (60 FPS'te saniyede ~7.5 kare)

    // ---------- Meriç: yön tamponu ----------
    // Oyuncunun basıp henüz uygulanamayan yönü. Örneğin koridordayken yukarı
    // basılırsa, Pac-Man yukarı açılan ilk kavşakta kendiliğinden döner.
    private int nextDRow = 0;
    private int nextDCol = 0;

    // Çizimde ağzın baktığı yön (dururken de son yöne bakar)
    private int faceRow = 0;
    private int faceCol = 1;

    private int animFrame = 0;

    public Pacman(int row, int col) {
        super(row, col, SPEED);
    }

    public void setDirection(int dRow, int dCol) {
        this.nextDRow = dRow;
        this.nextDCol = dCol;
    }

    // ---------- Egemen: duvar çarpışması ----------
    public boolean canMove(Maze maze, int dr, int dc) {
        return canGo(maze, dr, dc);
    }

    // ---------- Meriç: akıcı hareket ----------
    @Override
    public void update(Maze maze) {
        // Ters yöne dönüş her an yapılabilir (geldiği yol zaten boş)
        if (isMoving() && nextDRow == -dRow && nextDCol == -dCol) {
            dRow = nextDRow;
            dCol = nextDCol;
        }

        // Diğer dönüşler ve duvarda durma sadece kare ortasında
        if (isAtTileCenter()) {
            if ((nextDRow != 0 || nextDCol != 0) && canMove(maze, nextDRow, nextDCol)) {
                dRow = nextDRow;
                dCol = nextDCol;
            }
            if (!canMove(maze, dRow, dCol)) {
                dRow = 0;
                dCol = 0;
            }
        }

        if (isMoving()) {
            faceRow = dRow;
            faceCol = dCol;
            step();
            animFrame++;
        }
    }

    @Override
    public void resetToStart() {
        super.resetToStart();
        nextDRow = 0;
        nextDCol = 0;
        faceRow = 0;
        faceCol = 1;
        animFrame = 0;
    }

    // ---------- Meriç: çizim ----------
    @Override
    public void draw(Graphics2D g) {
        int size = TILE + 2; // karakter karenin biraz üstüne taşsın, daha dolgun dursun
        float cx = x + TILE / 2f;
        float cy = y + TILE / 2f;
        int left = Math.round(cx - size / 2f);
        int top = Math.round(cy - size / 2f);

        // Ağız açılıp kapanıyor (dururken yarı açık)
        int mouth = isMoving() ? (int) (Math.abs(Math.sin(animFrame * 0.3)) * 42) + 3 : 25;

        int base;
        if (faceCol == 1)       base = 0;    // sağ
        else if (faceRow == -1) base = 90;   // yukarı
        else if (faceCol == -1) base = 180;  // sol
        else                    base = 270;  // aşağı

        // Işık efekti: sol üstte parlak, kenarda koyu sarı
        g.setPaint(new RadialGradientPaint(cx - size * 0.15f, cy - size * 0.2f, size * 0.7f,
                new float[] {0f, 1f},
                new Color[] {new Color(255, 250, 140), new Color(240, 190, 0)}));
        g.fillArc(left, top, size, size, base + mouth, 360 - 2 * mouth);

        // Göz
        double eyeAngle = Math.toRadians(base + 70);
        int ex = (int) Math.round(cx + Math.cos(eyeAngle) * size * 0.25);
        int ey = (int) Math.round(cy - Math.sin(eyeAngle) * size * 0.25);
        g.setColor(new Color(30, 20, 0));
        g.fillOval(ex - 2, ey - 2, 4, 4);
    }
}
