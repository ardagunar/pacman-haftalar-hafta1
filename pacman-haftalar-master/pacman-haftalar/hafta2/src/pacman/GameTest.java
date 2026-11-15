package pacman;

/**
 * Basit testler (JUnit gerektirmez, main ile çalışır).
 *
 * Sorumlular:
 *   Arda Güner -> duvar çarpışma testleri
 *   Kaan Avşar -> yem toplama / skor testleri
 */
public class GameTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testPacmanStopsAtWall();
        testPacmanMovesOnPath();
        testFoodIsEatenAndScoreIncreases();
        testSameFoodNotCountedTwice();

        System.out.println("\nBasarili: " + passed + "  Basarisiz: " + failed);
    }

    // ---------- Arda ----------
    static void testPacmanStopsAtWall() {
        Maze maze = new Maze();
        Pacman p = new Pacman(1, 1);
        p.setDirection(-1, 0); // yukarısı duvar
        p.move(maze);
        check("Pac-Man duvardan gecmiyor", p.getRow() == 1 && p.getCol() == 1);
    }

    static void testPacmanMovesOnPath() {
        Maze maze = new Maze();
        Pacman p = new Pacman(1, 1);
        p.setDirection(0, 1); // sağı yol
        p.move(maze);
        check("Pac-Man yolda ilerliyor", p.getCol() == 2);
    }

    // ---------- Kaan ----------
    static void testFoodIsEatenAndScoreIncreases() {
        Maze maze = new Maze();
        ScoreManager sm = new ScoreManager();
        sm.placeFoods(maze);
        sm.checkFood(new Pacman(1, 1));
        check("Yem yeniyor ve skor artiyor", sm.getScore() == Food.POINTS);
    }

    static void testSameFoodNotCountedTwice() {
        Maze maze = new Maze();
        ScoreManager sm = new ScoreManager();
        sm.placeFoods(maze);
        Pacman p = new Pacman(1, 1);
        sm.checkFood(p);
        sm.checkFood(p);
        check("Ayni yem iki kez sayilmiyor", sm.getScore() == Food.POINTS);
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[HATA] ") + name);
        if (ok) passed++; else failed++;
    }
}
