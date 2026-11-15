package pacman;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Oyun alanı: oyun döngüsü, çizim ve klavye.
 *
 * Sorumlular:
 *   Meriç Kavuncuoğlu -> kurulum, 60 FPS oyun döngüsü, oyun durumları;
 *                        5. hafta: bölüm geçişi (loadLevel, LEVEL_CLEAR durumu)
 *   Egemen Çavuş      -> klavye; 5. hafta: gerçek zaman ölçümü (deltaMs),
 *                        bölüme göre hayalet hızları
 *   Arda Günar        -> 5. hafta: hayaletlerin haritadaki 'G' karelerinden oluşturulması
 *   Kaan Avşar        -> skor/can çubuğu, Game Over, TEBRİKLER ekranı;
 *                        5. hafta: güç süresi çubuğu, bölüm geçiş ekranı
 */
public class GamePanel extends JPanel implements ActionListener {

    private static final int TILE = GameObject.TILE;
    private static final int HUD_HEIGHT = 48;

    private static final int FRAME_MS = 16;          // ~60 FPS
    private static final int READY_MS = 1500;        // "HAZIR!" bekleme süresi
    private static final int LEVEL_CLEAR_MS = 3000;  // bölüm geçiş ekranının süresi

    // Hayalet renkleri ve bölüme göre hızları (piksel/frame). Bölüm 2'de hayaletler daha hızlı.
    private static final Color[] GHOST_COLORS = {
        new Color(255, 40, 40),    // Kırmızı
        new Color(255, 150, 200),  // Pembe
        new Color(40, 230, 255),   // Turkuaz
        new Color(255, 170, 40),   // Turuncu
    };
    private static final int[][] GHOST_SPEEDS = {
        {3, 2, 2, 2},  // Bölüm 1
        {3, 3, 3, 2},  // Bölüm 2
    };

    /** Oyunun hangi aşamada olduğu. */
    private enum GameState { READY, PLAYING, LEVEL_CLEAR, WON, GAME_OVER }

    private int level;
    private Maze maze;
    private Pacman pacman;
    private ScoreManager scoreManager;
    private CollisionManager collisionManager;
    private PowerManager powerManager;
    private final List<Ghost> ghosts = new ArrayList<>();

    private GameState state;
    private int stateLeftMs;   // READY ve LEVEL_CLEAR için geri sayım
    private int frameCount = 0;
    private long lastFrameNanos = System.nanoTime();

    private final Timer timer;

    public GamePanel() {
        newGame();

        setPreferredSize(new Dimension(maze.getWidth(), maze.getHeight() + HUD_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);

        setupKeyboard();

        timer = new Timer(FRAME_MS, this);
        timer.setCoalesce(true);
        timer.start();
    }

    // ---------- Meriç: yeni oyun ve bölüm yükleme ----------
    private void newGame() {
        scoreManager = new ScoreManager();
        collisionManager = new CollisionManager();
        loadLevel(1);
    }

    /**
     * Verilen bölümün haritasını kurar. Skor ve canlar değişmez;
     * böylece Bölüm 2'ye kaldığı skor ve canla geçilir.
     */
    private void loadLevel(int newLevel) {
        level = newLevel;
        maze = new Maze(level);
        pacman = new Pacman(maze.getPacmanStartRow(), maze.getPacmanStartCol());
        scoreManager.placeFoods(maze);
        powerManager = new PowerManager();
        powerManager.placeCherries(maze);
        createGhosts();
        startReady();
    }

    private void startReady() {
        state = GameState.READY;
        stateLeftMs = READY_MS;
    }

    // ---------- Arda + Egemen: hayaletler ----------
    private void createGhosts() {
        ghosts.clear();
        List<int[]> starts = maze.getGhostStarts();          // Arda: konumlar haritadan
        int[] speeds = GHOST_SPEEDS[level - 1];              // Egemen: bölüme göre hız
        for (int i = 0; i < starts.size(); i++) {
            int[] cell = starts.get(i);
            ghosts.add(new Ghost(cell[0], cell[1],
                    GHOST_COLORS[i % GHOST_COLORS.length],
                    speeds[i % speeds.length]));
        }
    }

    // ---------- Egemen: klavye ----------
    private void setupKeyboard() {
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:    case KeyEvent.VK_W: pacman.setDirection(-1, 0); break;
                    case KeyEvent.VK_DOWN:  case KeyEvent.VK_S: pacman.setDirection(1, 0);  break;
                    case KeyEvent.VK_LEFT:  case KeyEvent.VK_A: pacman.setDirection(0, -1); break;
                    case KeyEvent.VK_RIGHT: case KeyEvent.VK_D: pacman.setDirection(0, 1);  break;
                    case KeyEvent.VK_R:
                        if (state == GameState.WON || state == GameState.GAME_OVER) {
                            newGame();
                        }
                        break;
                    case KeyEvent.VK_ENTER:
                    case KeyEvent.VK_SPACE:
                        if (state == GameState.LEVEL_CLEAR) {
                            loadLevel(level + 1); // beklemeden sonraki bölüme geç
                        }
                        break;
                    default: break;
                }
            }
        });
    }

    // ---------- Meriç: oyun döngüsü ----------
    @Override
    public void actionPerformed(ActionEvent e) {
        frameCount++;

        // Egemen: bir önceki frame'den bu yana geçen gerçek süre.
        // 7 ve 5 saniyelik süreler frame sayısına değil buna göre işliyor.
        long now = System.nanoTime();
        int deltaMs = (int) Math.min(100, (now - lastFrameNanos) / 1_000_000);
        // Kullanılan milisaniye kadar ilerlet; küsurat bir sonraki frame'e kalır.
        // (Pencere uzun süre donarsa en fazla 100 ms sayılır, kalan süre atılır.)
        lastFrameNanos = deltaMs == 100 ? now : lastFrameNanos + deltaMs * 1_000_000L;

        switch (state) {
            case READY:
                stateLeftMs -= deltaMs;
                if (stateLeftMs <= 0) {
                    state = GameState.PLAYING;
                }
                break;

            case PLAYING:
                updatePlaying(deltaMs);
                break;

            case LEVEL_CLEAR:
                stateLeftMs -= deltaMs;
                if (stateLeftMs <= 0) {
                    loadLevel(level + 1);
                }
                break;

            default:
                break; // WON / GAME_OVER: oyun donuk, R bekleniyor
        }

        repaint();
        Toolkit.getDefaultToolkit().sync(); // Linux'ta takılmayı önler
    }

    private void updatePlaying(int deltaMs) {
        pacman.update(maze);                                          // Meriç + Egemen
        scoreManager.checkFood(pacman);                               // Kaan
        powerManager.checkCherry(pacman, ghosts, scoreManager);       // Kaan: kiraz

        // Tüm yemler bitti: sonraki bölüm varsa ona geç, yoksa oyun kazanıldı
        if (scoreManager.allFoodsEaten()) {
            powerManager.stop(ghosts);
            if (level < Maze.LEVEL_COUNT) {
                state = GameState.LEVEL_CLEAR;
                stateLeftMs = LEVEL_CLEAR_MS;
            } else {
                state = GameState.WON;
            }
            return;
        }

        // Arda: çarpışma (hareketten önce ve sonra, iç içe geçmesinler)
        if (checkHit()) {
            return;
        }
        for (Ghost ghost : ghosts) {                                  // Egemen
            ghost.tick(deltaMs);   // canlanma süresi
            ghost.update(maze);    // hareket
        }
        powerManager.update(deltaMs, ghosts);                         // Kaan: 7 sn süresi
        checkHit();
    }

    /** @return Pac-Man can kaybettiyse true */
    private boolean checkHit() {
        if (!collisionManager.checkCollision(pacman, ghosts, scoreManager)) {
            return false;
        }
        powerManager.stop(ghosts); // can gidince güç süresi de biter
        if (collisionManager.isGameOver()) {
            state = GameState.GAME_OVER;
        } else {
            startReady(); // herkes başa döndü, kısa bekleme
        }
        return true;
    }

    // ---------- Çizim ----------
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        maze.draw(g);                                      // Arda
        for (Food food : scoreManager.getFoods()) {        // Kaan
            food.draw(g);
        }
        for (Cherry cherry : powerManager.getCherries()) { // Kaan
            cherry.draw(g);
        }
        for (Ghost ghost : ghosts) {                       // Meriç
            ghost.draw(g);
        }
        if (state != GameState.GAME_OVER) {
            pacman.draw(g);                                // Meriç
        }

        drawHud(g);                                        // Kaan

        switch (state) {                                   // Kaan
            case READY:
                drawReady(g);
                break;
            case LEVEL_CLEAR:
                drawOverlay(g, "TEBRİKLER!", new Color(255, 220, 60),
                        "Bölüm " + level + " tamamlandı  •  Skor: " + scoreManager.getScore(),
                        "Bölüm " + (level + 1) + " başlıyor... " + (stateLeftMs + 999) / 1000);
                break;
            case WON:
                drawOverlay(g, "TEBRİKLER!", new Color(255, 220, 60),
                        "Tüm bölümleri bitirdin  •  Skor: " + scoreManager.getScore(),
                        "Yeniden oynamak için R");
                break;
            case GAME_OVER:
                drawOverlay(g, "GAME OVER", new Color(255, 60, 60),
                        "Bölüm " + level + "  •  Skor: " + scoreManager.getScore(),
                        "Yeniden oynamak için R");
                break;
            default:
                break;
        }
    }

    // ---------- Kaan: skor/can çubuğu ----------
    private void drawHud(Graphics2D g) {
        int top = maze.getHeight();
        int w = maze.getWidth();

        g.setPaint(new GradientPaint(0, top, new Color(18, 18, 50), 0, top + HUD_HEIGHT, Color.BLACK));
        g.fillRect(0, top, w, HUD_HEIGHT);
        Color edge = maze.getEdgeColor();
        g.setColor(new Color(edge.getRed(), edge.getGreen(), edge.getBlue(), 120));
        g.fillRect(0, top, w, 2);

        int textY = top + HUD_HEIGHT / 2 + 7;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.setColor(new Color(170, 180, 220));
        g.drawString("SKOR", 14, textY);
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(scoreManager.getScore()), 76, textY);

        // Ortada: güç süresi varsa kalan saniye, yoksa bölüm ve kalan yem
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        String center;
        if (powerManager.isActive()) {
            int tenths = (powerManager.getPowerLeftMs() + 99) / 100;
            center = "Hayalet yeme süresi: " + tenths / 10 + "," + tenths % 10 + " sn";

            // Kalan süreyi gösteren çubuk; son 2 saniyede kırmızıya döner
            boolean ending = powerManager.getPowerLeftMs() <= PowerManager.WARNING_MS;
            g.setColor(ending ? new Color(255, 120, 120) : new Color(120, 200, 255));
            int barW = (int) ((long) w * powerManager.getPowerLeftMs() / PowerManager.POWER_MS);
            g.fillRect(0, top, barW, 4);
        } else {
            center = "Bölüm " + level + "   •   Kalan yem: " + scoreManager.getRemaining();
            g.setColor(new Color(170, 180, 220));
        }
        FontMetrics fm = g.getFontMetrics();
        g.drawString(center, (w - fm.stringWidth(center)) / 2, textY - 1);

        // Canlar: küçük Pac-Man simgeleri
        g.setColor(new Color(255, 215, 0));
        for (int i = 0; i < collisionManager.getLives(); i++) {
            int iconX = w - 34 - i * 30;
            g.fillArc(iconX, top + HUD_HEIGHT / 2 - 11, 22, 22, 35, 290);
        }
    }

    /** "BÖLÜM n - HAZIR!" yazısı, Pac-Man'in hemen altında yanıp söner. */
    private void drawReady(Graphics2D g) {
        if ((frameCount / 15) % 2 != 0) {
            return;
        }
        String text = "BÖLÜM " + level + "  -  HAZIR!";
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(text);
        int centerY = (maze.getPacmanStartRow() + 1) * TILE + TILE / 2;
        int x = (maze.getWidth() - textW) / 2;

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x - 10, centerY - 11, textW + 20, 22, 12, 12);
        g.setColor(new Color(255, 230, 60));
        g.drawString(text, x, centerY + fm.getAscent() / 2 - 2);
    }

    // ---------- Kaan: TEBRİKLER / GAME OVER ekranı ----------
    private void drawOverlay(Graphics2D g, String title, Color titleColor, String line1, String line2) {
        int w = maze.getWidth();
        int h = maze.getHeight();

        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, w, h);

        int boxW = Math.min(w - 60, 460);
        int boxH = 190;
        int boxX = (w - boxW) / 2;
        int boxY = (h - boxH) / 2;

        g.setColor(new Color(10, 12, 45, 235));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 28, 28);
        g.setStroke(new BasicStroke(3f));
        g.setColor(titleColor);
        g.drawRoundRect(boxX, boxY, boxW, boxH, 28, 28);

        // Başlık hafifçe büyüyüp küçülüyor
        float pulse = 44f + (float) Math.sin(frameCount * 0.08) * 3f;
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.round(pulse)));
        FontMetrics fm = g.getFontMetrics();
        int ty = boxY + 72;
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(title, (w - fm.stringWidth(title)) / 2 + 3, ty + 3);
        g.setColor(titleColor);
        g.drawString(title, (w - fm.stringWidth(title)) / 2, ty);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        fm = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString(line1, (w - fm.stringWidth(line1)) / 2, boxY + 120);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
        fm = g.getFontMetrics();
        g.setColor(new Color(170, 180, 220));
        g.drawString(line2, (w - fm.stringWidth(line2)) / 2, boxY + 158);
    }
}
