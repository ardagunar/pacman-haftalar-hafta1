package pacman;

import java.util.List;

/**
 * Pac-Man ile hayaletler arasındaki çarpışma ve can sistemi.
 *
 * Sorumlular:
 *   Arda Güner -> çarpışma kontrolü (4. hafta: piksel mesafesiyle), başlangıca dönüş
 *   Kaan Avşar -> can sistemi, Game Over
 */
public class CollisionManager {

    public static final int START_LIVES = 3;

    // Merkezler arası mesafe bunun altındaysa çarpışma sayılır.
    // Karenin tamamı değil, biraz altı: köşeden sıyırınca haksız can gitmesin.
    private static final double HIT_DISTANCE = GameObject.TILE * 0.7;

    private int lives = START_LIVES;
    private boolean gameOver = false;

    // ---------- Arda ----------
    /**
     * 3. haftada aynı kare kontrolü yeterliydi. 4. haftada nesneler piksel piksel
     * hareket ettiği için iki kare arasında birbirinin içinden geçebiliyorlardı;
     * bu yüzden artık merkezler arası mesafeye bakılıyor.
     */
    public boolean checkCollision(Pacman pacman, List<Ghost> ghosts) {
        if (gameOver) {
            return false;
        }
        for (Ghost ghost : ghosts) {
            if (pacman.distanceTo(ghost) < HIT_DISTANCE) {
                onHit(pacman, ghosts);
                return true;
            }
        }
        return false;
    }

    private void resetPositions(Pacman pacman, List<Ghost> ghosts) {
        pacman.resetToStart();
        for (Ghost ghost : ghosts) {
            ghost.resetToStart();
        }
    }

    // ---------- Kaan ----------
    private void onHit(Pacman pacman, List<Ghost> ghosts) {
        lives--;
        if (lives <= 0) {
            lives = 0;
            gameOver = true;
        } else {
            resetPositions(pacman, ghosts);
        }
    }

    public int getLives() {
        return lives;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}
