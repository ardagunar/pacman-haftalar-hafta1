package pacman;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
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
 * 5. haftada hayalete durum eklendi (GhostState):
 *   NORMAL -> (kiraz yenir) -> FRIGHTENED -> (süre biter) -> NORMAL
 *                                  |
 *                         (Pac-Man yer) -> DEAD -> (5 saniye) -> NORMAL, başlangıç noktasında
 *
 * Sorumlular:
 *   Meriç Kavuncuoğlu -> sınıf, konum/hız/yön, çizim;
 *                        5. hafta: durumlar (GhostState), korkmuş ve canlanan hayalet çizimi
 *   Egemen Çavuş      -> hareket; 5. hafta: korkunca yavaşlama ve geri dönme,
 *                        5 saniyelik canlanma süresi
 */
public class Ghost extends MovingObject {

    /** Yenen hayaletin yeniden canlanma süresi: 5 saniye. */
    public static final int RESPAWN_MS = 5000;

    /** Canlanmadan önceki son 1,5 saniyede hayalet başlangıç noktasında yanıp söner. */
    private static final int RESPAWN_WARNING_MS = 1500;

    /** Korkmuş hayaletin hızı (piksel/frame). Normalden yavaş, yakalaması kolay olsun. */
    public static final int FRIGHTENED_SPEED = 1;

    private static final int[][] DIRECTIONS = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

    private static final Color FRIGHT_BLUE = new Color(40, 60, 230);
    private static final Color FRIGHT_WHITE = new Color(235, 235, 255);

    private final Color color;
    private final int normalSpeed;
    private final Random random = new Random();

    private GhostState state = GhostState.NORMAL;
    private int respawnLeftMs = 0;
    private boolean frightEnding = false; // yenebilme süresi bitmek üzere mi (yanıp sönme)
    private int animFrame = 0;

    /** speed: piksel/frame, 24'ü tam bölmeli (1, 2, 3, 4, 6...). */
    public Ghost(int row, int col, Color color, int speed) {
        super(row, col, speed);
        this.color = color;
        this.normalSpeed = speed;
    }

    public Color getColor() { return color; }
    public GhostState getState() { return state; }
    public int getRespawnLeftMs() { return respawnLeftMs; }

    // ---------- Meriç: durum geçişleri ----------

    /** Pac-Man kiraz yedi: hayalet yenebilir olur. Ölü hayalet etkilenmez. */
    public void frighten() {
        if (state != GhostState.NORMAL) {
            return; // zaten korkmuş ya da ölü
        }
        state = GhostState.FRIGHTENED;
        // Egemen: korkan hayalet geldiği yöne geri döner
        dRow = -dRow;
        dCol = -dCol;
    }

    /** Yenebilme süresi bitti: korkmuş hayalet normale döner. */
    public void calm() {
        if (state == GhostState.FRIGHTENED) {
            state = GhostState.NORMAL;
        }
        frightEnding = false;
    }

    /** Pac-Man hayaleti yedi: hayalet kaybolur, 5 saniye sonra başlangıç noktasında canlanır. */
    public void eaten() {
        super.resetToStart(); // konumu şimdiden başlangıca al, orada canlanacak
        state = GhostState.DEAD;
        respawnLeftMs = RESPAWN_MS;
        frightEnding = false;
    }

    public void setFrightEnding(boolean ending) {
        this.frightEnding = ending;
    }

    /** Can kaybı ya da yeni bölüm: hayalet canlı ve normal olarak başa döner. */
    @Override
    public void resetToStart() {
        super.resetToStart();
        state = GhostState.NORMAL;
        respawnLeftMs = 0;
        frightEnding = false;
        setSpeed(normalSpeed);
    }

    // ---------- Egemen: canlanma süresi ----------
    /** Her frame çağrılır. deltaMs: bir önceki frame'den bu yana geçen süre. */
    public void tick(int deltaMs) {
        animFrame++;
        if (state != GhostState.DEAD) {
            return;
        }
        respawnLeftMs -= deltaMs;
        if (respawnLeftMs <= 0) {
            resetToStart(); // canlandı: normal, yani yine tehlikeli
        }
    }

    // ---------- Egemen: hareket ----------
    @Override
    public void update(Maze maze) {
        if (state == GhostState.DEAD) {
            return;
        }
        if (isAtTileCenter()) {
            // Hız sadece kare ortasında değişir (bkz. MovingObject.setSpeed)
            setSpeed(state == GhostState.FRIGHTENED ? FRIGHTENED_SPEED : normalSpeed);
            chooseDirection(maze);
        }
        step();
    }

    /**
     * Her kavşakta, geri dönüş hariç açık yönlerden rastgele biri seçilir.
     * Çıkmaz yolda geri döner.
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
        switch (state) {
            case DEAD:
                // Canlanmasına az kaldıysa başlangıç noktasında soluk soluk yanıp söner:
                // oyuncu orada duruyorsa kaçması için uyarı.
                if (respawnLeftMs <= RESPAWN_WARNING_MS && (animFrame / 8) % 2 == 0) {
                    Composite old = g.getComposite();
                    g.setComposite(AlphaComposite.SrcOver.derive(0.35f));
                    drawBody(g, color);
                    g.setComposite(old);
                }
                break;

            case FRIGHTENED:
                // Süre bitmek üzereyken mavi-beyaz yanıp söner
                boolean white = frightEnding && (animFrame / 10) % 2 == 0;
                drawBody(g, white ? FRIGHT_WHITE : FRIGHT_BLUE);
                drawScaredFace(g, white ? new Color(220, 40, 40) : new Color(255, 220, 200));
                break;

            default:
                drawBody(g, color);
                drawEyes(g);
                break;
        }
    }

    private double bodySize() { return TILE + 2; }
    private double bodyX()    { return x + TILE / 2.0 - bodySize() / 2; }
    private double bodyY()    { return y + TILE / 2.0 - bodySize() / 2; }

    /** Gövde: üstü yarım daire, altı dalgalı etek. */
    private void drawBody(Graphics2D g, Color bodyColor) {
        double w = bodySize();
        double gx = bodyX();
        double gy = bodyY();

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

        g.setPaint(new GradientPaint((float) gx, (float) gy, bodyColor.brighter(),
                                     (float) gx, (float) (gy + w), bodyColor.darker()));
        g.fill(body);
    }

    /** Normal gözler: baktığı yöne doğru kayar. */
    private void drawEyes(Graphics2D g) {
        double w = bodySize();
        double gx = bodyX();
        double gy = bodyY();

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

    /** Korkmuş yüz: iki küçük göz ve zikzak ağız. */
    private void drawScaredFace(Graphics2D g, Color faceColor) {
        double w = bodySize();
        double gx = bodyX();
        double gy = bodyY();

        g.setColor(faceColor);
        double eye = w * 0.16;
        g.fill(new Ellipse2D.Double(gx + w * 0.27, gy + w * 0.30, eye, eye));
        g.fill(new Ellipse2D.Double(gx + w * 0.57, gy + w * 0.30, eye, eye));

        Path2D mouth = new Path2D.Double();
        double my = gy + w * 0.68;
        mouth.moveTo(gx + w * 0.18, my);
        for (int i = 1; i <= 6; i++) {
            mouth.lineTo(gx + w * (0.18 + i * 0.107), (i % 2 == 1) ? my - 3 : my);
        }
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(mouth);
    }
}
