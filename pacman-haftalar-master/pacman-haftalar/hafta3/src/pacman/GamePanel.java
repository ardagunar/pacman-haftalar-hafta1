package pacman;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Oyun alanı: çizim, oyun döngüsü ve klavye.
 *
 * Sorumlular:
 *   Meriç Kavuncuoğlu -> panel kurulumu, oyun döngüsü (Timer), paintComponent,
 *                        (3. hafta) hayalet listesinin oluşturulması
 *   Egemen Çavuş      -> klavye dinleyicisi, panele odak verme,
 *                        (3. hafta) hayaletlerin başlangıç konumları
 *   Kaan Avşar        -> skor ve can bilgisinin ekrana yazılması, Game Over mesajı
 */
public class GamePanel extends JPanel implements ActionListener {

    public static final int TILE_SIZE = 32;
    private static final int SCORE_BAR = 40;

    private final Maze maze;
    private final Pacman pacman;
    private final ScoreManager scoreManager;
    private final CollisionManager collisionManager;
    private final List<Ghost> ghosts = new ArrayList<>();
    private final Timer timer;

    // ---------- Meriç: kurulum ----------
    public GamePanel() {
        maze = new Maze();
        pacman = new Pacman(7, 9);
        scoreManager = new ScoreManager();
        scoreManager.placeFoods(maze);
        collisionManager = new CollisionManager();

        createGhosts();

        setPreferredSize(new Dimension(maze.getCols() * TILE_SIZE,
                                       maze.getRows() * TILE_SIZE + SCORE_BAR));
        setBackground(Color.BLACK);

        setupKeyboard();

        timer = new Timer(150, this);
        timer.start();
    }

    // ---------- Meriç + Egemen: hayaletler ----------
    /** Birden fazla hayalet bir listede tutulur, her birinin farklı başlangıç noktası var. */
    private void createGhosts() {
        // Egemen: başlangıç konumları (labirentin köşeleri, Pac-Man'den uzakta)
        ghosts.add(new Ghost(1, 17, Color.RED, 1));                 // Kırmızı - en hızlı
        ghosts.add(new Ghost(13, 1, Color.PINK, 2));                // Pembe
        ghosts.add(new Ghost(13, 17, Color.CYAN, 2));               // Turkuaz
        ghosts.add(new Ghost(1, 1, new Color(255, 165, 0), 3));     // Turuncu - en yavaş
    }

    // ---------- Egemen: klavye ----------
    private void setupKeyboard() {
        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:    pacman.setDirection(-1, 0); break;
                    case KeyEvent.VK_DOWN:  pacman.setDirection(1, 0);  break;
                    case KeyEvent.VK_LEFT:  pacman.setDirection(0, -1); break;
                    case KeyEvent.VK_RIGHT: pacman.setDirection(0, 1);  break;
                    default: break;
                }
            }
        });
    }

    // ---------- Meriç: oyun döngüsü ----------
    @Override
    public void actionPerformed(ActionEvent e) {
        pacman.move(maze);                                   // Meriç + Egemen
        scoreManager.checkFood(pacman);                      // Kaan

        // Pac-Man hareketinden sonra ve hayaletler hareket ettikten sonra
        // iki kez kontrol: karşılıklı yer değiştirirken içinden geçmesinler.
        boolean hit = collisionManager.checkCollision(pacman, ghosts);   // Arda
        if (!hit) {
            for (Ghost ghost : ghosts) {                     // Egemen
                ghost.move(maze);
            }
            collisionManager.checkCollision(pacman, ghosts); // Arda
        }

        if (collisionManager.isGameOver()) {
            timer.stop();
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        maze.draw(g, TILE_SIZE);                       // Arda
        for (Food food : scoreManager.getFoods()) {    // Kaan
            food.draw(g, TILE_SIZE);
        }
        pacman.draw(g, TILE_SIZE);                     // Meriç
        for (Ghost ghost : ghosts) {                   // Meriç
            ghost.draw(g, TILE_SIZE);
        }
        drawStatusBar(g);                              // Kaan
        if (collisionManager.isGameOver()) {
            drawGameOver(g);                           // Kaan
        }
    }

    // ---------- Kaan: skor, can, Game Over ----------
    private void drawStatusBar(Graphics g) {
        int y = maze.getRows() * TILE_SIZE + 27;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Skor: " + scoreManager.getScore(), 10, y);

        // Kalan canlar küçük Pac-Man simgeleriyle
        g.drawString("Can:", 170, y);
        g.setColor(Color.YELLOW);
        for (int i = 0; i < collisionManager.getLives(); i++) {
            g.fillArc(220 + i * 26, y - 17, 20, 20, 30, 300);
        }
    }

    private void drawGameOver(Graphics g) {
        String text = "GAME OVER";
        g.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g.getFontMetrics();
        int x = (maze.getCols() * TILE_SIZE - fm.stringWidth(text)) / 2;
        int y = maze.getRows() * TILE_SIZE / 2 + fm.getAscent() / 2;

        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, y - fm.getAscent() - 10, maze.getCols() * TILE_SIZE, fm.getHeight() + 20);
        g.setColor(Color.RED);
        g.drawString(text, x, y);
    }
}
