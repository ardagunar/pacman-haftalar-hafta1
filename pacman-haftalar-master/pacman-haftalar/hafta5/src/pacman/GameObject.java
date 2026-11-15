package pacman;

import java.awt.Graphics2D;

/**
 * Oyundaki tüm nesnelerin (Pacman, Ghost, Food) ortak atası.
 *
 * Sorumlular:
 *   Meriç Kavuncuoğlu -> 1. hafta soyut sınıf taslağı,
 *                        4. hafta piksel konumu (x, y) - akıcı hareket için
 *   Arda Günar        -> 3. hafta resetToStart()
 */
public abstract class GameObject {

    /** Bir karenin piksel boyutu. Hareket hızları bunu tam bölmeli. */
    public static final int TILE = 24;

    // Labirentteki kare konumu
    protected int row;
    protected int col;

    // Başlangıç konumu (çarpışma sonrası geri dönmek için)
    protected final int startRow;
    protected final int startCol;

    // 4. hafta (Meriç): ekrandaki piksel konumu. Nesneler artık kareden kareye
    // zıplamıyor, piksel piksel kayarak ilerliyor.
    protected int x;
    protected int y;

    public GameObject(int row, int col) {
        this.row = row;
        this.col = col;
        this.startRow = row;
        this.startCol = col;
        this.x = col * TILE;
        this.y = row * TILE;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public int getX()   { return x; }
    public int getY()   { return y; }

    public void resetToStart() {
        row = startRow;
        col = startCol;
        x = col * TILE;
        y = row * TILE;
    }

    /** Aynı karede mi? (yem toplama için) */
    public boolean isSamePlace(GameObject other) {
        return this.row == other.row && this.col == other.col;
    }

    /** İki nesnenin merkezleri arasındaki piksel mesafesi (hayalet çarpışması için). */
    public double distanceTo(GameObject other) {
        return Math.hypot(this.x - other.x, this.y - other.y);
    }

    /** Her alt sınıf kendini farklı çizer (polimorfizm). */
    public abstract void draw(Graphics2D g);
}
