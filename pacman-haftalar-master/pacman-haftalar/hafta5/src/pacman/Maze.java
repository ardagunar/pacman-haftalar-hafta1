package pacman;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Labirent. 5. haftada ikinci bir harita (Bölüm 2) eklendi.
 *
 * Haritalar metin olarak çizildi, okunması ve değiştirmesi kolay:
 *   '#' = duvar
 *   '.' = yem olan yol
 *   'C' = kiraz            (5. hafta)
 *   'G' = hayalet başlangıcı (5. hafta - artık konumlar haritadan okunuyor)
 *   'P' = Pac-Man başlangıcı
 *   ' ' = boş yol
 * Program başlarken 2 boyutlu int dizisine çevriliyor.
 *
 * Sorumlu: Arda Günar (labirent tasarımı, 2D dizi, duvar çizimi, Bölüm 2 haritası)
 */
public class Maze {

    public static final int WALL = 1;
    public static final int PATH = 0;
    public static final int EMPTY = 2;

    // ---------- Bölüm 1 ----------
    private static final String[] LEVEL_1 = {
        "###########################",
        "#C....##.....#.....##....C#",
        "#.###.##.###.#.###.##.###.#",
        "#.###.##.###.#.###.##.###.#",
        "#.........................#",
        "#.###.#.###########.#.###.#",
        "#.....#......#......#.....#",
        "#####.####.#.#.#.####.#####",
        "#####.#......#......#.#####",
        "#####.#.###.###.###.#.#####",
        "#.........#..G..#.........#",
        "#.###.#.#.#.###.#.#.#.###.#",
        "#.###.#.#..G.G.G..#.#.###.#",
        "#.....#.####.#.####.#.....#",
        "###.#........#........#.###",
        "###.#.######.#.######.#.###",
        "#.....#.............#.....#",
        "#.###.#.####.#.####.#.###.#",
        "#...#........P........#...#",
        "###.#.#.###########.#.#.###",
        "#.....#.....###.....#.....#",
        "#.#########.###.#########.#",
        "#C.......................C#",
        "###########################",
    };

    // ---------- Bölüm 2 ----------
    private static final String[] LEVEL_2 = {
        "###########################",
        "#C.....#...........#.....C#",
        "#.####.#.#########.#.####.#",
        "#.#.....................#.#",
        "#.#.####.###.#.###.####.#.#",
        "#...#....#.......#....#...#",
        "###.#.####.#####.####.#.###",
        "#...#.................#...#",
        "#.###.##.###.#.###.##.###.#",
        "#.....#..#G..G..G#..#.....#",
        "#.###.#.##.#####.##.#.###.#",
        "#.#...#......G......#...#.#",
        "#.#.###.####.#.####.###.#.#",
        "#...#...#.........#...#...#",
        "###.#.###.###.###.###.#.###",
        "#.....#.............#.....#",
        "#.###.#.#####.#####.#.###.#",
        "#.#...#......P......#...#.#",
        "#.#.#####.#######.#####.#.#",
        "#...#.................#...#",
        "#.###.#######.#######.###.#",
        "#.#...#.............#...#.#",
        "#C..#...###########...#..C#",
        "###########################",
    };

    private static final String[][] LEVELS = { LEVEL_1, LEVEL_2 };

    /** Toplam bölüm sayısı. Yeni harita eklemek için LEVELS dizisine eklemek yeterli. */
    public static final int LEVEL_COUNT = LEVELS.length;

    // Her bölümün duvar rengi farklı: {dolgu, parlama, kenar çizgisi}
    private static final Color[][] LEVEL_COLORS = {
        { new Color(12, 14, 60), new Color(40, 70, 255, 70),  new Color(70, 110, 255) },  // Bölüm 1: mavi
        { new Color(6, 38, 30),  new Color(40, 220, 150, 70), new Color(60, 220, 150) },  // Bölüm 2: yeşil
    };

    private final int level;
    private final int[][] map;
    private int pacmanRow;
    private int pacmanCol;
    private final List<int[]> ghostStarts = new ArrayList<>();
    private final List<int[]> cherryCells = new ArrayList<>();

    // Duvarlar bir kez resme çizilip saklanıyor; her frame'de tekrar
    // hesaplanmadığı için oyun daha akıcı çalışıyor.
    private BufferedImage wallImage;

    /** Bölüm 1'in haritası. */
    public Maze() {
        this(1);
    }

    /** level: 1'den LEVEL_COUNT'a kadar. */
    public Maze(int level) {
        if (level < 1 || level > LEVEL_COUNT) {
            throw new IllegalArgumentException("Böyle bir bölüm yok: " + level);
        }
        this.level = level;
        String[] layout = LEVELS[level - 1];

        map = new int[layout.length][layout[0].length()];
        for (int r = 0; r < layout.length; r++) {
            for (int c = 0; c < layout[r].length(); c++) {
                char ch = layout[r].charAt(c);
                switch (ch) {
                    case '#':
                        map[r][c] = WALL;
                        break;
                    case '.':
                        map[r][c] = PATH;
                        break;
                    case 'P':
                        map[r][c] = EMPTY;
                        pacmanRow = r;
                        pacmanCol = c;
                        break;
                    case 'G':
                        map[r][c] = EMPTY;
                        ghostStarts.add(new int[] {r, c});
                        break;
                    case 'C':
                        map[r][c] = EMPTY;
                        cherryCells.add(new int[] {r, c});
                        break;
                    default:
                        map[r][c] = EMPTY;
                        break;
                }
            }
        }
    }

    public int getLevel() { return level; }
    public int getRows() { return map.length; }
    public int getCols() { return map[0].length; }
    public int getCell(int row, int col) { return map[row][col]; }
    public int getPacmanStartRow() { return pacmanRow; }
    public int getPacmanStartCol() { return pacmanCol; }

    /** Hayaletlerin başlangıç kareleri, her biri {satır, sütun}. */
    public List<int[]> getGhostStarts() { return ghostStarts; }

    /** Kirazların kareleri, her biri {satır, sütun}. */
    public List<int[]> getCherryCells() { return cherryCells; }

    public int getWidth()  { return getCols() * GameObject.TILE; }
    public int getHeight() { return getRows() * GameObject.TILE; }

    /** Bu bölümün duvar kenar rengi (skor çubuğu da aynı rengi kullanıyor). */
    public Color getEdgeColor() { return LEVEL_COLORS[level - 1][2]; }

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
     *   1) koyu dolgu
     *   2) yarı saydam kalın çizgi (parlama)
     *   3) ince parlak kenar çizgisi
     * Böylece duvarlar kare kare değil, bütün ve yuvarlak köşeli görünür.
     */
    private BufferedImage createWallImage() {
        int t = GameObject.TILE;
        Color[] colors = LEVEL_COLORS[level - 1];

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

        g.setColor(colors[0]);
        g.fill(walls);

        g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(colors[1]);
        g.draw(walls);

        g.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(colors[2]);
        g.draw(walls);

        g.dispose();
        return img;
    }
}
