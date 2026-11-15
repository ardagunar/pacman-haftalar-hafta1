package pacman;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Labirent. 4. haftada harita büyütüldü (19x15 -> 27x24) ve duvar grafikleri yenilendi.
 *
 * Harita metin olarak çizildi, okunması ve değiştirmesi kolay:
 *   '#' = duvar, '.' = yem olan yol, ' ' veya 'P' = yemsiz yol (P: Pac-Man başlangıcı)
 * Program başlarken 2 boyutlu int dizisine çevriliyor.
 *
 * Sorumlu: Arda Güner (labirent tasarımı, 2D dizi, duvar çizimi)
 */
public class Maze {

    public static final int WALL = 1;
    public static final int PATH = 0;
    public static final int EMPTY = 2;

    private static final String[] LAYOUT = {
        "###########################",
        "#.....##.....#.....##.....#",
        "#.###.##.###.#.###.##.###.#",
        "#.###.##.###.#.###.##.###.#",
        "#.........................#",
        "#.###.#.###########.#.###.#",
        "#.....#......#......#.....#",
        "#####.####.#.#.#.####.#####",
        "#####.#......#......#.#####",
        "#####.#.###.###.###.#.#####",
        "#.........#.....#.........#",
        "#.###.#.#.#.###.#.#.#.###.#",
        "#.###.#.#.........#.#.###.#",
        "#.....#.####.#.####.#.....#",
        "###.#........#........#.###",
        "###.#.######.#.######.#.###",
        "#.....#.............#.....#",
        "#.###.#.####.#.####.#.###.#",
        "#...#........P........#...#",
        "###.#.#.###########.#.#.###",
        "#.....#.....###.....#.....#",
        "#.#########.###.#########.#",
        "#.........................#",
        "###########################",
    };

    private final int[][] map;
    private int pacmanRow;
    private int pacmanCol;

    // Duvarlar bir kez resme çizilip saklanıyor; her frame'de tekrar
    // hesaplanmadığı için oyun daha akıcı çalışıyor.
    private BufferedImage wallImage;

    public Maze() {
        map = new int[LAYOUT.length][LAYOUT[0].length()];
        for (int r = 0; r < LAYOUT.length; r++) {
            for (int c = 0; c < LAYOUT[r].length(); c++) {
                char ch = LAYOUT[r].charAt(c);
                if (ch == '#') {
                    map[r][c] = WALL;
                } else if (ch == '.') {
                    map[r][c] = PATH;
                } else {
                    map[r][c] = EMPTY;
                    if (ch == 'P') {
                        pacmanRow = r;
                        pacmanCol = c;
                    }
                }
            }
        }
    }

    public int getRows() { return map.length; }
    public int getCols() { return map[0].length; }
    public int getCell(int row, int col) { return map[row][col]; }
    public int getPacmanStartRow() { return pacmanRow; }
    public int getPacmanStartCol() { return pacmanCol; }

    public int getWidth()  { return getCols() * GameObject.TILE; }
    public int getHeight() { return getRows() * GameObject.TILE; }

    /** Harita dışı da duvar sayılır. */
    public boolean isWall(int row, int col) {
        if (row < 0 || row >= getRows() || col < 0 || col >= getCols()) {
            return true;
        }
        return map[row][col] == WALL;
    }

    public void draw(Graphics2D g) {
        if (wallImage == null) {
            wallImage = createWallImage();
        }
        g.drawImage(wallImage, 0, 0, null);
    }

    /**
     * Tüm duvar kareleri tek bir şekilde (Area) birleştirilir, sonra:
     *   1) koyu lacivert dolgu
     *   2) yarı saydam kalın mavi çizgi (parlama)
     *   3) ince parlak mavi kenar çizgisi
     * Böylece duvarlar kare kare değil, bütün ve yuvarlak köşeli görünür.
     */
    private BufferedImage createWallImage() {
        int t = GameObject.TILE;
        BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        Area walls = new Area();
        int inset = 3; // duvarı biraz içeri çekip yollar arasında boşluk bırak
        for (int r = 0; r < getRows(); r++) {
            for (int c = 0; c < getCols(); c++) {
                if (map[r][c] != WALL) {
                    continue;
                }
                // Komşusu da duvarsa o yöne doğru boşluk bırakma -> duvarlar birleşir
                double left   = isWall(r, c - 1) ? 0 : inset;
                double right  = isWall(r, c + 1) ? 0 : inset;
                double top    = isWall(r - 1, c) ? 0 : inset;
                double bottom = isWall(r + 1, c) ? 0 : inset;
                walls.add(new Area(new Rectangle2D.Double(
                        c * t + left, r * t + top,
                        t - left - right, t - top - bottom)));
            }
        }

        g.setColor(new Color(12, 14, 60));
        g.fill(walls);

        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(40, 70, 255, 70));
        g.draw(walls);

        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(new Color(70, 110, 255));
        g.draw(walls);

        g.dispose();
        return img;
    }
}
