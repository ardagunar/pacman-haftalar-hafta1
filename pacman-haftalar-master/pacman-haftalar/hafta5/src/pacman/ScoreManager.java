package pacman;

import java.util.ArrayList;
import java.util.List;

/**
 * Yemler ve skor.
 *
 * Sorumlular:
 *   Arda Günar -> placeFoods(): yemlerin labirente yerleşimi
 *   Kaan Avşar -> checkFood(), skor; 4. hafta: allFoodsEaten() (kazanma durumu);
 *                 5. hafta: addPoints() (kiraz ve hayalet puanı)
 *
 * 5. hafta notu: placeFoods() skoru sıfırlamaz, sadece yemleri yeniden dizer.
 * Böylece Bölüm 2'ye geçince skor kaldığı yerden devam eder.
 */
public class ScoreManager {

    private final List<Food> foods = new ArrayList<>();
    private int score = 0;
    private int remaining = 0;

    // ---------- Arda ----------
    public void placeFoods(Maze maze) {
        foods.clear();
        for (int r = 0; r < maze.getRows(); r++) {
            for (int c = 0; c < maze.getCols(); c++) {
                if (maze.getCell(r, c) == Maze.PATH) {
                    foods.add(new Food(r, c));
                }
            }
        }
        remaining = foods.size();
    }

    // ---------- Kaan ----------
    public void checkFood(Pacman pacman) {
        for (Food food : foods) {
            if (!food.isEaten() && pacman.isSamePlace(food)) {
                food.setEaten(true);
                score += Food.POINTS;
                remaining--;
            }
        }
    }

    /** 5. hafta: kiraz yeme ve hayalet yeme puanları buradan eklenir. */
    public void addPoints(int points) {
        score += points;
    }

    /** 4. hafta: tüm yemler toplandıysa bölüm biter. */
    public boolean allFoodsEaten() {
        return remaining == 0;
    }

    public int getRemaining() {
        return remaining;
    }

    public List<Food> getFoods() {
        return foods;
    }

    public int getScore() {
        return score;
    }
}
