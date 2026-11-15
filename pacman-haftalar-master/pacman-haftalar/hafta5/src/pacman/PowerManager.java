package pacman;

import java.util.ArrayList;
import java.util.List;

/**
 * Kirazlar ve "hayalet yeme" süresi (5. hafta).
 *
 * Pac-Man bir kiraz yiyince:
 *   - skor artar,
 *   - yaşayan tüm hayaletler FRIGHTENED (yenebilir) olur,
 *   - 7 saniyelik süre başlar. Süre bitince hayaletler normale döner.
 * Süre devam ederken ikinci bir kiraz yenirse süre yeniden 7 saniyeden başlar.
 *
 * Süreler frame sayısıyla değil milisaniyeyle tutuluyor; bilgisayar yavaş
 * olsa bile 7 saniye gerçekten 7 saniye sürüyor.
 *
 * Sorumlular:
 *   Arda Günar -> placeCherries(): kirazların haritadaki 'C' karelerine yerleşimi
 *   Kaan Avşar -> checkCherry(), 7 saniyelik süre
 */
public class PowerManager {

    /** Hayaletlerin yenebilir kaldığı süre: 7 saniye. */
    public static final int POWER_MS = 7000;

    /** Sürenin son 2 saniyesinde hayaletler yanıp sönerek uyarır. */
    public static final int WARNING_MS = 2000;

    private final List<Cherry> cherries = new ArrayList<>();
    private int powerLeftMs = 0;

    // ---------- Arda ----------
    public void placeCherries(Maze maze) {
        cherries.clear();
        for (int[] cell : maze.getCherryCells()) {
            cherries.add(new Cherry(cell[0], cell[1]));
        }
    }

    // ---------- Kaan ----------
    /** Pac-Man bir kirazın üstündeyse yer. Yem toplama kontrolüyle aynı mantık. */
    public boolean checkCherry(Pacman pacman, List<Ghost> ghosts, ScoreManager scoreManager) {
        for (Cherry cherry : cherries) {
            if (!cherry.isEaten() && pacman.isSamePlace(cherry)) {
                cherry.setEaten(true);
                scoreManager.addPoints(Cherry.POINTS);
                start(ghosts);
                return true;
            }
        }
        return false;
    }

    private void start(List<Ghost> ghosts) {
        powerLeftMs = POWER_MS;
        for (Ghost ghost : ghosts) {
            ghost.frighten();
            ghost.setFrightEnding(false);
        }
    }

    /** Her frame çağrılır. deltaMs: bir önceki frame'den bu yana geçen süre. */
    public void update(int deltaMs, List<Ghost> ghosts) {
        if (!isActive()) {
            return;
        }
        powerLeftMs -= deltaMs;
        if (powerLeftMs <= 0) {
            stop(ghosts);
            return;
        }
        boolean ending = powerLeftMs <= WARNING_MS;
        for (Ghost ghost : ghosts) {
            ghost.setFrightEnding(ending);
        }
    }

    /** Süreyi bitirir, korkmuş hayaletleri normale döndürür (ölü olanlar ölü kalır). */
    public void stop(List<Ghost> ghosts) {
        powerLeftMs = 0;
        for (Ghost ghost : ghosts) {
            ghost.calm();
        }
    }

    public boolean isActive() {
        return powerLeftMs > 0;
    }

    public int getPowerLeftMs() {
        return powerLeftMs;
    }

    public List<Cherry> getCherries() {
        return cherries;
    }
}
