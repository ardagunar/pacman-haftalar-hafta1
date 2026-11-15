package pacman;

import java.util.ArrayList;
import java.util.List;

/**
 * Yemler ve skor.
 *
 * Sorumlular:
 *   Arda Güner -> placeFoods(): yemlerin labirente yerleşimi
 *   Kaan Avşar -> checkFood(), skor; 4. hafta: allFoodsEaten() (kazanma durumu)
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

    /** 4. hafta: tüm yemler toplandıysa oyuncu kazanır. */
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
