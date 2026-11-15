package pacman;

import java.awt.Color;
import java.awt.Graphics;

/**
 * Oyuncunun kontrol ettiği karakter. GameObject'ten kalıtım alır. (İSKELET)
 *
 * Sorumlular:
 *   Meriç Kavuncuoğlu -> sınıf iskeleti, draw()
 *   Egemen Çavuş      -> ilk hareket denemesi
 */
public class Pacman extends GameObject {

    private int dRow = 0;
    private int dCol = 0;

    public Pacman(int row, int col) {
        super(row, col);
    }

    public void setDirection(int dRow, int dCol) {
        this.dRow = dRow;
        this.dCol = dCol;
    }

    /**
     * BİLİNEN HATA (1. hafta testinde bulundu):
     * Gidilecek karenin duvar olup olmadığına bakılmıyor, bu yüzden
     * Pac-Man duvarların içinden geçiyor. 2. haftada düzeltilecek.
     */
    public void move(Maze maze) {
        int nextRow = row + dRow;
        int nextCol = col + dCol;

        // Sadece haritanın dışına çıkmasını engelliyoruz (yoksa ekrandan kayboluyor)
        if (nextRow >= 0 && nextRow < maze.getRows() && nextCol >= 0 && nextCol < maze.getCols()) {
            row = nextRow;
            col = nextCol;
        }
    }

    @Override
    public void draw(Graphics g, int tileSize) {
        g.setColor(Color.YELLOW);
        g.fillArc(col * tileSize + 2, row * tileSize + 2, tileSize - 4, tileSize - 4, 30, 300);
    }
}
