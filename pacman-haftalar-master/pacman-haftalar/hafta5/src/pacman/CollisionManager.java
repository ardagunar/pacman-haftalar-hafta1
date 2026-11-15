package pacman;

import java.util.List;

/**
 * Pac-Man ile hayaletler arasındaki çarpışma ve can sistemi.
 *
 * 5. haftada çarpışmanın sonucu hayaletin durumuna bağlı:
 *   NORMAL      -> Pac-Man can kaybeder
 *   FRIGHTENED  -> Pac-Man hayaleti yer, puan alır
 *   DEAD        -> hayalet ekranda yok, çarpışma olmaz
 *
 * Sorumlular:
 *   Arda Günar -> çarpışma kontrolü, başlangıca dönüş; 5. hafta: hayalet yeme
 *   Kaan Avşar -> can sistemi, Game Over; 5. hafta: hayalet yeme puanı
 */
public class CollisionManager {

    public static final int START_LIVES = 3;

    /** Bir hayalet yemenin puanı. */
    public static final int GHOST_POINTS = 200;

    // Merkezler arası mesafe bunun altındaysa çarpışma sayılır.
    // Karenin tamamı değil, biraz altı: köşeden sıyırınca haksız can gitmesin.
    private static final double HIT_DISTANCE = GameObject.TILE * 0.7;

    private int lives = START_LIVES;
    private boolean gameOver = false;

    // ---------- Arda ----------
    /**
     * @return Pac-Man can kaybettiyse true. Hayalet yemek can kaybı değildir, false döner.
     */
    public boolean checkCollision(Pacman pacman, List<Ghost> ghosts, ScoreManager scoreManager) {
        if (gameOver) {
            return false;
        }
        for (Ghost ghost : ghosts) {
            if (ghost.getState() == GhostState.DEAD) {
                continue;
            }
            if (pacman.distanceTo(ghost) >= HIT_DISTANCE) {
                continue;
            }
            if (ghost.getState() == GhostState.FRIGHTENED) {
                ghost.eaten();                              // Arda
                scoreManager.addPoints(GHOST_POINTS);       // Kaan
            } else {
                onHit(pacman, ghosts);
                return true;
            }
        }
        return false;
    }

    private void resetPositions(Pacman pacman, List<Ghost> ghosts) {
        pacman.resetToStart();
        for (Ghost ghost : ghosts) {
            ghost.resetToStart(); // ölü ya da korkmuş olanlar da canlı ve normal başlar
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
