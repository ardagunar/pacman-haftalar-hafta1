package pacman;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Oyun alanı (temel Swing kodu).
 *
 * Sorumlular:
 *   Meriç Kavuncuoğlu -> panel kurulumu, çizim, Timer
 *   Egemen Çavuş      -> klavye dinleyicisi, panele odak verme
 *   Arda Güner / Kaan Avşar -> hücre boyutunun sabitlenmesi (labirent pencereye otursun)
 */
public class GamePanel extends JPanel implements ActionListener {

    // Sorun: labirent pencereye tam oturmuyordu.
    // Çözüm: hücre boyutu sabitlendi, pencere boyutu buna göre hesaplanıyor.
    public static final int TILE_SIZE = 32;

    private final Maze maze;
    private final Pacman pacman;

    // ---------- Meriç ----------
    public GamePanel() {
        maze = new Maze();
        pacman = new Pacman(7, 9);

        setPreferredSize(new Dimension(maze.getCols() * TILE_SIZE, maze.getRows() * TILE_SIZE));
        setBackground(Color.BLACK);

        setupKeyboard();

        Timer timer = new Timer(150, this);
        timer.start();
    }

    // ---------- Egemen ----------
    private void setupKeyboard() {
        // Sorun: klavye tuşları ilk başta çalışmıyordu.
        // Çözüm: panele odak verildi.
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

    @Override
    public void actionPerformed(ActionEvent e) {
        pacman.move(maze);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        maze.draw(g, TILE_SIZE);    // Arda
        pacman.draw(g, TILE_SIZE);  // Meriç
    }
}
