package pacman;

/**
 * Hareket eden nesnelerin (Pacman ve Ghost) ortak sınıfı.
 * 4. haftada iki sınıftaki ortak hareket kodu buraya taşındı (kalıtım).
 *
 *   GameObject (soyut)
 *     ├── Food
 *     └── MovingObject (soyut)
 *           ├── Pacman
 *           └── Ghost
 *
 * Sorumlu: Meriç Kavuncuoğlu (4. hafta - akıcı, piksel tabanlı hareket)
 */
public abstract class MovingObject extends GameObject {

    // Hareket yönü: (-1,0) yukarı, (1,0) aşağı, (0,-1) sol, (0,1) sağ, (0,0) duruyor
    protected int dRow = 0;
    protected int dCol = 0;

    /** Her karede (frame) kaç piksel ilerlediği. TILE'ı tam bölmeli ki karelerin ortasına tam otursun. */
    protected final int speed;

    public MovingObject(int row, int col, int speed) {
        super(row, col);
        if (TILE % speed != 0) {
            throw new IllegalArgumentException("Hız " + TILE + " sayısını tam bölmeli: " + speed);
        }
        this.speed = speed;
    }

    /** Nesne tam bir karenin üstünde mi? Yön değişikliği sadece burada yapılır. */
    protected boolean isAtTileCenter() {
        return x % TILE == 0 && y % TILE == 0;
    }

    /** Bulunduğu kareden verilen yöne gidilebilir mi? (duvar kontrolü) */
    protected boolean canGo(Maze maze, int dr, int dc) {
        return !maze.isWall(row + dr, col + dc);
    }

    /** Bir frame ilerler ve kare konumunu (row, col) günceller. */
    protected void step() {
        x += dCol * speed;
        y += dRow * speed;
        row = (y + TILE / 2) / TILE;
        col = (x + TILE / 2) / TILE;
    }

    public boolean isMoving() {
        return dRow != 0 || dCol != 0;
    }

    @Override
    public void resetToStart() {
        super.resetToStart();
        dRow = 0;
        dCol = 0;
    }

    /** Her frame'de bir kez çağrılır. */
    public abstract void update(Maze maze);
}
