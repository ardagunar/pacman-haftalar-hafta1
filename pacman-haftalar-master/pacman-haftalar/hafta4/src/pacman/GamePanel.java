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
 *   Meriç Kavuncuoğlu -> kurulum, oyun döngüsü; 4. hafta: 60 FPS döngü, oyun durumları
 *   Egemen Çavuş      -> klavye (4. hafta: WASD ve R ile yeniden başlama), hayalet konumları
 *   Kaan Avşar        -> skor/can çubuğu, Game Over; 4. hafta: TEBRİKLER ekranı
 */
public class GamePanel extends JPanel implements ActionListener {

    private static final int TILE = GameObject.TILE;
    private static final int HUD_HEIGHT = 48;

    // ---------- Meriç: 60 FPS ----------
    // 3. haftada oyun 150 ms'de bir güncelleniyordu (~7 FPS) ve karakterler
    // kareden kareye zıplıyordu. Artık ~16 ms'de bir (~60 FPS) güncelleniyor
    // ve karakterler her frame'de birkaç piksel kayıyor.
    private static final int FRAME_MS = 16;
    private static final int READY_FRAMES = 90; // "HAZIR!" yazısı ~1.5 sn

    /** Oyunun hangi aşamada olduğu. */
    private enum GameState { READY, PLAYING, WON, GAME_OVER }

    private Maze maze;
    private Pacman pacman;
    private ScoreManager scoreManager;
    private CollisionManager collisionManager;
    private final List<Ghost> ghosts = new ArrayList<>();

    private GameState state;
    private int readyCounter;
    private int frameCount = 0;

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

    // ---------- Meriç: yeni oyun ----------
    private void newGame() {
        maze = new Maze();
        pacman = new Pacman(maze.getPacmanStartRow(), maze.getPacmanStartCol());
        scoreManager = new ScoreManager();
        scoreManager.placeFoods(maze);
        collisionManager = new CollisionManager();
        createGhosts();
        startReady();
    }

    private void startReady() {
        state = GameState.READY;
        readyCounter = READY_FRAMES;
    }

    // ---------- Egemen: hayaletlerin başlangıç konumları ----------
    private void createGhosts() {
        ghosts.clear();
        ghosts.add(new Ghost(12, 13, new Color(255, 40, 40), 3));    // Kırmızı - Pac-Man kadar hızlı
        ghosts.add(new Ghost(12, 11, new Color(255, 150, 200), 2));  // Pembe
        ghosts.add(new Ghost(12, 15, new Color(40, 230, 255), 2));   // Turkuaz
        ghosts.add(new Ghost(10, 13, new Color(255, 170, 40), 2));   // Turuncu
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
                    default: break;
                }
            }
        });
    }

    // ---------- Meriç: oyun döngüsü ----------
    @Override
    public void actionPerformed(ActionEvent e) {
        frameCount++;

        switch (state) {
            case READY:
                readyCounter--;
                if (readyCounter <= 0) {
                    state = GameState.PLAYING;
                }
                break;

            case PLAYING:
                updatePlaying();
                break;

            default:
                break; // WON / GAME_OVER: oyun donuk, R bekleniyor
        }

        repaint();
        Toolkit.getDefaultToolkit().sync(); // Linux'ta takılmayı önler
    }

    private void updatePlaying() {
        pacman.update(maze);                               // Meriç + Egemen
        scoreManager.checkFood(pacman);                    // Kaan

        // Kaan: kazanma kontrolü
        if (scoreManager.allFoodsEaten()) {
            state = GameState.WON;
            return;
        }

        // Arda: çarpışma (hareketten önce ve sonra, iç içe geçmesinler)
        if (checkHit()) {
            return;
        }
        for (Ghost ghost : ghosts) {                       // Egemen
            ghost.update(maze);
        }
        checkHit();
    }

    private boolean checkHit() {
        if (!collisionManager.checkCollision(pacman, ghosts)) {
            return false;
        }
        if (collisionManager.isGameOver()) {
            state = GameState.GAME_OVER;
        } else {
            startReady(); // can gitti, herkes başa döndü, kısa bekleme
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
            case WON:
                drawOverlay(g, "TEBRİKLER!", new Color(255, 220, 60),
                        "Tüm yemleri topladın  •  Skor: " + scoreManager.getScore(),
                        "Yeniden oynamak için R");
                break;
            case GAME_OVER:
                drawOverlay(g, "GAME OVER", new Color(255, 60, 60),
                        "Skor: " + scoreManager.getScore(),
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
        g.setColor(new Color(70, 110, 255, 120));
        g.fillRect(0, top, w, 2);

        int textY = top + HUD_HEIGHT / 2 + 7;

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.setColor(new Color(170, 180, 220));
        g.drawString("SKOR", 14, textY);
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(scoreManager.getScore()), 76, textY);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        g.setColor(new Color(170, 180, 220));
        String left = "Kalan yem: " + scoreManager.getRemaining();
        FontMetrics fm = g.getFontMetrics();
        g.drawString(left, (w - fm.stringWidth(left)) / 2, textY - 1);

        // Canlar: küçük Pac-Man simgeleri
        g.setColor(new Color(255, 215, 0));
        for (int i = 0; i < collisionManager.getLives(); i++) {
            int iconX = w - 34 - i * 30;
            g.fillArc(iconX, top + HUD_HEIGHT / 2 - 11, 22, 22, 35, 290);
        }
    }

    private void drawReady(Graphics2D g) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        String text = "HAZIR!";
        int x = (maze.getWidth() - fm.stringWidth(text)) / 2;
        int y = 14 * TILE + TILE / 2 + fm.getAscent() / 2 - 2; // labirentin ortasındaki boş satır
        if ((frameCount / 15) % 2 == 0) { // yanıp sönsün
            g.setColor(new Color(255, 230, 60));
            g.drawString(text, x, y);
        }
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
