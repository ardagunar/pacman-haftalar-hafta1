package pacman;

import java.util.List;

/**
 * Pac-Man ile hayaletler arasındaki çarpışma ve can sistemi.
 *
 * Sorumlular (3. hafta):
 *   Arda Güner -> çarpışma kontrolü, çarpışma sonrası başlangıç noktasına dönüş
 *   Kaan Avşar -> can sistemi, Game Over durumu
 */
public class CollisionManager {

    public static final int START_LIVES = 3;

    private int lives = START_LIVES;
    private boolean gameOver = false;

    // ---------- Arda: çarpışma kontrolü ----------
    /**
     * Yem toplama kontrolüne benzer şekilde konumlar karşılaştırılır.
     * @return çarpışma olduysa true
     */
    public boolean checkCollision(Pacman pacman, List<Ghost> ghosts) {
        if (gameOver) {
            return false;
        }
        for (Ghost ghost : ghosts) {
            if (pacman.isSamePlace(ghost)) {
                onHit(pacman, ghosts);
                return true;
            }
        }
        return false;
    }

    /** Herkesi başlangıç noktasına döndürür. */
    private void resetPositions(Pacman pacman, List<Ghost> ghosts) {
        pacman.resetToStart();
        pacman.setDirection(0, 0);
        for (Ghost ghost : ghosts) {
            ghost.resetToStart();
        }
    }

    // ---------- Kaan: can sistemi ----------
    /**
     * İlk denemede ilk temasta oyun bitiyordu.
     * Çözüm: can sistemi. Her çarpışmada bir can gider, canlar bitince Game Over.
     */
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
