package pacman;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Hayalet. GameObject soyut sınıfından kalıtım alır.
 *
 * Sorumlular (3. hafta):
 *   Meriç Kavuncuoğlu -> sınıfın oluşturulması, konum/hız/yön alanları, draw()
 *   Egemen Çavuş      -> rastgele hareket, duvar kontrolü, duvara çarpınca yeni yön seçme
 */
public class Ghost extends GameObject {

    // ---------- Meriç: konum, hız, yön ----------
    // Olası yönler: yukarı, aşağı, sol, sağ
    private static final int[][] DIRECTIONS = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

    private final Color color;
    private int dRow;
    private int dCol;

    // Hız: hayalet her "speed" oyun adımında bir kare ilerler (1 = en hızlı)
    private final int speed;
    private int tickCounter = 0;

    private final Random random = new Random();

    public Ghost(int row, int col, Color color, int speed) {
        super(row, col);
        this.color = color;
        this.speed = speed;
        chooseRandomDirection(null);
    }

    public Color getColor() {
        return color;
    }

    // ---------- Egemen: hareket ----------
    /**
     * İlk denemedeki "hayalet duvarın içinden geçiyor" hatasının çözümü:
     * Pac-Man'deki gibi önce gidilecek kare kontrol edilir.
     * Duvarsa hayalet yeni bir yön seçer.
     */
    public void move(Maze maze) {
        tickCounter++;
        if (tickCounter < speed) {
            return; // henüz sırası gelmedi
        }
        tickCounter = 0;

        int nextRow = row + dRow;
        int nextCol = col + dCol;

        if (maze.isWall(nextRow, nextCol)) {
            chooseRandomDirection(maze);
            nextRow = row + dRow;
            nextCol = col + dCol;
        }

        if (!maze.isWall(nextRow, nextCol)) {
            row = nextRow;
            col = nextCol;
        }
    }

    /** Duvar olmayan yönler arasından rastgele birini seçer. maze null ise tamamen rastgele. */
    private void chooseRandomDirection(Maze maze) {
        List<int[]> options = new ArrayList<>();
        for (int[] d : DIRECTIONS) {
            if (maze == null || !maze.isWall(row + d[0], col + d[1])) {
                options.add(d);
            }
        }
        if (options.isEmpty()) {
            dRow = 0;
            dCol = 0;
            return;
        }
        int[] chosen = options.get(random.nextInt(options.size()));
        dRow = chosen[0];
        dCol = chosen[1];
    }

    // ---------- Meriç: çizim ----------
    @Override
    public void draw(Graphics g, int tileSize) {
        int x = col * tileSize + 2;
        int y = row * tileSize + 2;
        int size = tileSize - 4;

        // Gövde: üstü yarım daire, altı dikdörtgen
        g.setColor(color);
        g.fillArc(x, y, size, size, 0, 180);
        g.fillRect(x, y + size / 2, size, size / 2);

        // Gözler
        g.setColor(Color.WHITE);
        int eye = size / 4;
        g.fillOval(x + size / 5, y + size / 4, eye, eye);
        g.fillOval(x + size - size / 5 - eye, y + size / 4, eye, eye);
        g.setColor(Color.BLUE);
        int pupil = eye / 2;
        g.fillOval(x + size / 5 + pupil / 2, y + size / 4 + pupil / 2, pupil, pupil);
        g.fillOval(x + size - size / 5 - eye + pupil / 2, y + size / 4 + pupil / 2, pupil, pupil);
    }
}
