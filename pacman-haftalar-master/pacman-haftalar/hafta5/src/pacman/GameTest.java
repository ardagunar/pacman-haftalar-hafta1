package pacman;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/**
 * Testler (JUnit gerektirmez, main ile çalışır).
 *
 * Sorumlular:
 *   Arda Günar -> duvar, harita ve çarpışma testleri
 *   Kaan Avşar -> yem, skor, can, kiraz ve süre testleri
 */
public class GameTest {

    private static final int T = GameObject.TILE;
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        // 1.-2. hafta
        testPacmanStopsAtWall();
        testPacmanMovesOnPath();
        testFoodIsEatenAndScoreIncreases();
        testSameFoodNotCountedTwice();
        // 3. hafta
        testCollisionReducesLifeAndResets();
        testGameOverWhenLivesRunOut();
        // 4. hafta
        testPacmanTurnsAtJunction();
        testWinWhenAllFoodsEaten();
        // 5. hafta
        testEveryLevelIsPlayable();
        testLevelsAreDifferent();
        testGhostNeverEntersWallInAnyState();
        testCherryFrightensGhosts();
        testPowerLastsSevenSeconds();
        testSecondCherryRestartsTimer();
        testFrightenedGhostIsEaten();
        testGhostRespawnsAfterFiveSeconds();
        testRespawnedGhostIsDangerousAgain();
        testScoreKeptWhenNextLevelLoads();

        System.out.println("\nBasarili: " + passed + "  Basarisiz: " + failed);
    }

    // ================= Arda =================

    static void testPacmanStopsAtWall() {
        Maze maze = new Maze();
        Pacman p = new Pacman(1, 1);
        p.setDirection(-1, 0); // yukarısı duvar
        for (int i = 0; i < 30; i++) p.update(maze);
        check("Pac-Man duvardan gecmiyor", p.getX() == T && p.getY() == T);
    }

    static void testPacmanMovesOnPath() {
        Maze maze = new Maze();
        Pacman p = new Pacman(1, 1);
        p.setDirection(0, 1);
        for (int i = 0; i < T / Pacman.SPEED; i++) p.update(maze); // tam bir kare
        check("Pac-Man yolda piksel piksel ilerliyor", p.getCol() == 2 && p.getX() == 2 * T);
    }

    static void testCollisionReducesLifeAndResets() {
        Maze maze = new Maze();
        int sr = maze.getPacmanStartRow();
        int sc = maze.getPacmanStartCol();
        Pacman p = new Pacman(sr, sc);
        List<Ghost> ghosts = oneGhost(sr, sc + 2);
        CollisionManager cm = new CollisionManager();
        ScoreManager sm = new ScoreManager();

        p.setDirection(0, 1);
        boolean hit = false;
        for (int i = 0; i < 30 && !hit; i++) {
            p.update(maze);
            hit = cm.checkCollision(p, ghosts, sm);
        }
        check("Normal hayalete carpinca can 1 azaliyor",
              hit && cm.getLives() == CollisionManager.START_LIVES - 1 && !cm.isGameOver());
        check("Carpisma sonrasi Pac-Man baslangica donuyor",
              p.getRow() == sr && p.getCol() == sc && p.getX() == sc * T);
    }

    static void testPacmanTurnsAtJunction() {
        Maze maze = new Maze();
        Pacman p = new Pacman(1, 1); // (1,1)'den sağa gider, (1,5)'te aşağı yol var
        p.setDirection(0, 1);
        p.update(maze);
        p.setDirection(1, 0); // aşağıya erken basıldı
        for (int i = 0; i < 200; i++) p.update(maze);
        check("Erken basilan yon ilk kavsakta uygulaniyor", p.getCol() == 5 && p.getRow() > 1);
    }

    /** Her bölümde: tüm yemlere ve kirazlara ulaşılabiliyor, 4 hayalet ve 4 kiraz var. */
    static void testEveryLevelIsPlayable() {
        for (int level = 1; level <= Maze.LEVEL_COUNT; level++) {
            Maze maze = new Maze(level);
            boolean[][] seen = new boolean[maze.getRows()][maze.getCols()];
            Deque<int[]> queue = new ArrayDeque<>();
            queue.add(new int[] {maze.getPacmanStartRow(), maze.getPacmanStartCol()});
            seen[maze.getPacmanStartRow()][maze.getPacmanStartCol()] = true;
            int[][] dirs = { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
            while (!queue.isEmpty()) {
                int[] cur = queue.poll();
                for (int[] d : dirs) {
                    int r = cur[0] + d[0], c = cur[1] + d[1];
                    if (!maze.isWall(r, c) && !seen[r][c]) {
                        seen[r][c] = true;
                        queue.add(new int[] {r, c});
                    }
                }
            }
            boolean ok = true;
            for (int r = 0; r < maze.getRows(); r++)
                for (int c = 0; c < maze.getCols(); c++)
                    if (maze.getCell(r, c) == Maze.PATH && !seen[r][c]) ok = false;
            for (int[] cell : maze.getCherryCells()) if (!seen[cell[0]][cell[1]]) ok = false;
            for (int[] cell : maze.getGhostStarts()) if (!seen[cell[0]][cell[1]]) ok = false;

            check("Bolum " + level + ": tum yemlere ve kirazlara ulasilabiliyor", ok);
            check("Bolum " + level + ": 4 hayalet ve 4 kiraz var",
                  maze.getGhostStarts().size() == 4 && maze.getCherryCells().size() == 4);
        }
    }

    static void testLevelsAreDifferent() {
        Maze a = new Maze(1);
        Maze b = new Maze(2);
        int different = 0;
        for (int r = 0; r < a.getRows(); r++)
            for (int c = 0; c < a.getCols(); c++)
                if (a.isWall(r, c) != b.isWall(r, c)) different++;
        check("Bolum 2 haritasi Bolum 1'den farkli (" + different + " kare)",
              Maze.LEVEL_COUNT >= 2 && different > 50
              && a.getRows() == b.getRows() && a.getCols() == b.getCols());
    }

    /**
     * Hayalet korkunca yavaşlıyor ve geri dönüyor, ölüp canlanıyor. Bütün bu
     * geçişler rastgele zamanlarda denenirken hayalet hiçbir zaman duvara girmemeli.
     */
    static void testGhostNeverEntersWallInAnyState() {
        Random rnd = new Random(42);
        boolean ok = true;
        for (int level = 1; level <= Maze.LEVEL_COUNT && ok; level++) {
            Maze maze = new Maze(level);
            for (int speed : new int[] {2, 3}) {
                int[] start = maze.getGhostStarts().get(0);
                Ghost ghost = new Ghost(start[0], start[1], Color.RED, speed);
                for (int i = 0; i < 20000 && ok; i++) {
                    int dice = rnd.nextInt(200);
                    if (dice == 0) ghost.frighten();
                    else if (dice == 1) ghost.calm();
                    else if (dice == 2 && ghost.getState() == GhostState.FRIGHTENED) ghost.eaten();
                    ghost.tick(16);
                    ghost.update(maze);
                    int[][] corners = {
                        {ghost.getY(), ghost.getX()}, {ghost.getY(), ghost.getX() + T - 1},
                        {ghost.getY() + T - 1, ghost.getX()}, {ghost.getY() + T - 1, ghost.getX() + T - 1}
                    };
                    for (int[] c : corners) {
                        if (maze.isWall(c[0] / T, c[1] / T)) ok = false;
                    }
                }
            }
        }
        check("Hayalet hicbir durumda (normal/korkmus/canlanan) duvara girmiyor", ok);
    }

    static void testFrightenedGhostIsEaten() {
        Maze maze = new Maze();
        Pacman p = new Pacman(maze.getPacmanStartRow(), maze.getPacmanStartCol());
        Ghost ghost = new Ghost(5, 5, Color.RED, 2);
        List<Ghost> ghosts = new ArrayList<>();
        ghosts.add(ghost);
        CollisionManager cm = new CollisionManager();
        ScoreManager sm = new ScoreManager();

        ghost.frighten();
        Pacman onGhost = new Pacman(5, 5);
        boolean lifeLost = cm.checkCollision(onGhost, ghosts, sm);
        check("Korkmus hayalet yeniyor: can gitmiyor, +" + CollisionManager.GHOST_POINTS + " puan",
              !lifeLost && cm.getLives() == CollisionManager.START_LIVES
              && ghost.getState() == GhostState.DEAD
              && sm.getScore() == CollisionManager.GHOST_POINTS);

        // Ölü hayaletle çarpışma olmaz
        boolean again = cm.checkCollision(new Pacman(5, 5), ghosts, sm);
        check("Olu hayalet carpismiyor ve tekrar puan vermiyor",
              !again && sm.getScore() == CollisionManager.GHOST_POINTS
              && p.getRow() == maze.getPacmanStartRow());
    }

    // ================= Kaan =================

    static void testFoodIsEatenAndScoreIncreases() {
        Maze maze = new Maze();
        ScoreManager sm = new ScoreManager();
        sm.placeFoods(maze);
        Food first = sm.getFoods().get(0);
        sm.checkFood(new Pacman(first.getRow(), first.getCol()));
        check("Yem yeniyor ve skor artiyor", sm.getScore() == Food.POINTS && first.isEaten());
    }

    static void testSameFoodNotCountedTwice() {
        Maze maze = new Maze();
        ScoreManager sm = new ScoreManager();
        sm.placeFoods(maze);
        Food first = sm.getFoods().get(0);
        Pacman p = new Pacman(first.getRow(), first.getCol());
        sm.checkFood(p);
        sm.checkFood(p);
        check("Ayni yem iki kez sayilmiyor", sm.getScore() == Food.POINTS);
    }

    static void testGameOverWhenLivesRunOut() {
        List<Ghost> ghosts = oneGhost(5, 5);
        CollisionManager cm = new CollisionManager();
        ScoreManager sm = new ScoreManager();
        for (int i = 0; i < CollisionManager.START_LIVES; i++) {
            cm.checkCollision(new Pacman(5, 5), ghosts, sm);
        }
        check("Canlar bitince Game Over", cm.isGameOver() && cm.getLives() == 0);
    }

    static void testWinWhenAllFoodsEaten() {
        Maze maze = new Maze();
        ScoreManager sm = new ScoreManager();
        sm.placeFoods(maze);
        boolean notYet = !sm.allFoodsEaten();
        eatAllFoods(sm);
        check("Tum yemler bitince bolum tamamlaniyor",
              notYet && sm.allFoodsEaten() && sm.getScore() == sm.getFoods().size() * Food.POINTS);
    }

    static void testCherryFrightensGhosts() {
        Maze maze = new Maze();
        PowerManager pm = new PowerManager();
        pm.placeCherries(maze);
        ScoreManager sm = new ScoreManager();
        List<Ghost> ghosts = oneGhost(5, 5);
        Cherry cherry = pm.getCherries().get(0);

        boolean before = ghosts.get(0).getState() == GhostState.NORMAL && !pm.isActive();
        boolean ate = pm.checkCherry(new Pacman(cherry.getRow(), cherry.getCol()), ghosts, sm);
        check("Kiraz yeniyor: +" + Cherry.POINTS + " puan, hayaletler yenebilir oluyor",
              before && ate && cherry.isEaten() && sm.getScore() == Cherry.POINTS
              && pm.isActive() && ghosts.get(0).getState() == GhostState.FRIGHTENED);
    }

    static void testPowerLastsSevenSeconds() {
        Maze maze = new Maze();
        PowerManager pm = new PowerManager();
        pm.placeCherries(maze);
        List<Ghost> ghosts = oneGhost(5, 5);
        Cherry cherry = pm.getCherries().get(0);
        pm.checkCherry(new Pacman(cherry.getRow(), cherry.getCol()), ghosts, new ScoreManager());

        pm.update(6999, ghosts);
        boolean stillOn = pm.isActive() && ghosts.get(0).getState() == GhostState.FRIGHTENED;
        pm.update(1, ghosts);
        boolean off = !pm.isActive() && ghosts.get(0).getState() == GhostState.NORMAL;
        check("Hayalet yeme suresi tam 7 saniye (6999 ms acik, 7000 ms kapali)",
              PowerManager.POWER_MS == 7000 && stillOn && off);
    }

    static void testSecondCherryRestartsTimer() {
        Maze maze = new Maze();
        PowerManager pm = new PowerManager();
        pm.placeCherries(maze);
        List<Ghost> ghosts = oneGhost(5, 5);
        ScoreManager sm = new ScoreManager();
        Cherry c1 = pm.getCherries().get(0);
        Cherry c2 = pm.getCherries().get(1);

        pm.checkCherry(new Pacman(c1.getRow(), c1.getCol()), ghosts, sm);
        pm.update(5000, ghosts);
        pm.checkCherry(new Pacman(c2.getRow(), c2.getCol()), ghosts, sm);
        check("Sure bitmeden ikinci kiraz yenirse sure yeniden 7 saniye oluyor",
              pm.getPowerLeftMs() == PowerManager.POWER_MS && sm.getScore() == 2 * Cherry.POINTS);
    }

    static void testGhostRespawnsAfterFiveSeconds() {
        Ghost ghost = new Ghost(5, 5, Color.RED, 2);
        Maze maze = new Maze();
        for (int i = 0; i < 40; i++) ghost.update(maze); // başlangıçtan uzaklaşsın
        ghost.frighten();
        ghost.eaten();

        ghost.tick(4999);
        boolean stillDead = ghost.getState() == GhostState.DEAD;
        ghost.tick(1);
        check("Yenen hayalet tam 5 saniye sonra baslangic noktasinda canlaniyor",
              Ghost.RESPAWN_MS == 5000 && stillDead
              && ghost.getState() == GhostState.NORMAL
              && ghost.getRow() == 5 && ghost.getCol() == 5 && ghost.getX() == 5 * T);
    }

    static void testRespawnedGhostIsDangerousAgain() {
        Maze maze = new Maze();
        PowerManager pm = new PowerManager();
        pm.placeCherries(maze);
        Ghost ghost = new Ghost(5, 5, Color.RED, 2);
        List<Ghost> ghosts = new ArrayList<>();
        ghosts.add(ghost);
        CollisionManager cm = new CollisionManager();
        ScoreManager sm = new ScoreManager();
        Cherry cherry = pm.getCherries().get(0);

        pm.checkCherry(new Pacman(cherry.getRow(), cherry.getCol()), ghosts, sm); // t = 0
        cm.checkCollision(new Pacman(5, 5), ghosts, sm);                          // hemen yendi
        ghost.tick(5000);                                                         // t = 5 sn: canlandı
        pm.update(5000, ghosts);                                                  // güç hâlâ açık (2 sn kaldı)

        boolean lifeLost = cm.checkCollision(new Pacman(5, 5), ghosts, sm);
        check("Canlanan hayalet, sure bitmemis olsa bile yine tehlikeli",
              pm.isActive() && lifeLost && cm.getLives() == CollisionManager.START_LIVES - 1);
    }

    /** Bölüm 2'ye geçerken skor sıfırlanmamalı, yemler yeniden dizilmeli. */
    static void testScoreKeptWhenNextLevelLoads() {
        ScoreManager sm = new ScoreManager();
        sm.placeFoods(new Maze(1));
        eatAllFoods(sm);
        int scoreAfterLevel1 = sm.getScore();

        sm.placeFoods(new Maze(2));
        check("Bolum 2'ye gecince skor korunuyor, yemler yeniden diziliyor",
              sm.getScore() == scoreAfterLevel1 && scoreAfterLevel1 > 0
              && !sm.allFoodsEaten() && sm.getRemaining() == sm.getFoods().size());
    }

    // ================= Yardımcılar =================

    private static List<Ghost> oneGhost(int row, int col) {
        List<Ghost> ghosts = new ArrayList<>();
        ghosts.add(new Ghost(row, col, Color.RED, 2));
        return ghosts;
    }

    private static void eatAllFoods(ScoreManager sm) {
        for (Food f : new ArrayList<>(sm.getFoods())) {
            sm.checkFood(new Pacman(f.getRow(), f.getCol()));
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[HATA] ") + name);
        if (ok) passed++; else failed++;
    }
}
