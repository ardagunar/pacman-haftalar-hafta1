package pacman;

/**
 * Basit testler (JUnit gerektirmez, main ile çalışır).
 * Raporlardaki iki hatanın düzeldiğini kontrol eder.
 *
 * Sorumlular:
 *   Arda Güner -> duvar / labirent testleri
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

        // 3. hafta
        testGhostNeverEntersWall();
        testCollisionReducesLifeAndResets();
        testGameOverWhenLivesRunOut();

        System.out.println("\nBaşarılı: " + passed + "  Başarısız: " + failed);
    }

    // ---------- Arda ----------
    static void testPacmanStopsAtWall() {
        Maze maze = new Maze();
        Pacman p = new Pacman(1, 1);
        p.setDirection(-1, 0); // yukarısı duvar
        p.move(maze);
        check("Pac-Man duvardan geçmiyor", p.getRow() == 1 && p.getCol() == 1);
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
        Pacman p = new Pacman(1, 1);
        sm.checkFood(p);
        check("Yem yeniyor ve skor artıyor", sm.getScore() == Food.POINTS);
    }

    static void testSameFoodNotCountedTwice() {
        Maze maze = new Maze();
        ScoreManager sm = new ScoreManager();
        sm.placeFoods(maze);
        Pacman p = new Pacman(1, 1);
        sm.checkFood(p);
        sm.checkFood(p);
        check("Aynı yem iki kez sayılmıyor", sm.getScore() == Food.POINTS);
    }

    // ---------- 3. hafta - Arda: hayalet duvar + çarpışma ----------
    static void testGhostNeverEntersWall() {
        Maze maze = new Maze();
        Ghost ghost = new Ghost(1, 17, java.awt.Color.RED, 1);
        boolean ok = true;
        for (int i = 0; i < 1000; i++) {
            ghost.move(maze);
            if (maze.isWall(ghost.getRow(), ghost.getCol())) {
                ok = false;
                break;
            }
        }
        check("Hayalet 1000 adımda hiç duvara girmiyor", ok);
    }

    static void testCollisionReducesLifeAndResets() {
        Maze maze = new Maze();
        Pacman p = new Pacman(7, 9);                          // başlangıç (7,9)
        Ghost ghost = new Ghost(7, 10, java.awt.Color.RED, 1); // hemen sağında
        java.util.List<Ghost> ghosts = new java.util.ArrayList<>();
        ghosts.add(ghost);
        CollisionManager cm = new CollisionManager();

        // Pac-Man sağa gidip hayaletin üstüne biner
        p.setDirection(0, 1);
        p.move(maze);
        boolean hit = cm.checkCollision(p, ghosts);

        check("Çarpışmada can 1 azalıyor (oyun hemen bitmiyor)",
              hit && cm.getLives() == CollisionManager.START_LIVES - 1 && !cm.isGameOver());
        check("Çarpışma sonrası Pac-Man başlangıca dönüyor",
              p.getRow() == 7 && p.getCol() == 9);
    }

    // ---------- 3. hafta - Kaan: can sistemi ----------
    static void testGameOverWhenLivesRunOut() {
        Ghost ghost = new Ghost(5, 5, java.awt.Color.RED, 1);
        java.util.List<Ghost> ghosts = new java.util.ArrayList<>();
        ghosts.add(ghost);
        CollisionManager cm = new CollisionManager();
        for (int i = 0; i < CollisionManager.START_LIVES; i++) {
            cm.checkCollision(new Pacman(ghost.getRow(), ghost.getCol()), ghosts);
        }
        check("Canlar bitince Game Over", cm.isGameOver() && cm.getLives() == 0);
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[HATA] ") + name);
        if (ok) passed++; else failed++;
    }
}
