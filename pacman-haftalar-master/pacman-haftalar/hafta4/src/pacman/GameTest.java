package pacman;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Testler (JUnit gerektirmez, main ile çalışır).
 *
 * Sorumlular:
 *   Arda Güner -> duvar, harita ve çarpışma testleri
 *   Kaan Avşar -> yem, skor, can ve kazanma testleri
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
        testGhostNeverEntersWall();
        testCollisionReducesLifeAndResets();
        testGameOverWhenLivesRunOut();
        // 4. hafta
        testAllFoodsReachable();
        testPacmanTurnsAtJunction();
        testWinWhenAllFoodsEaten();

        System.out.println("\nBasarili: " + passed + "  Basarisiz: " + failed);
    }

    // ---------- Arda ----------
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

    static void testGhostNeverEntersWall() {
        Maze maze = new Maze();
        Ghost ghost = new Ghost(12, 13, Color.RED, 3);
        boolean ok = true;
        for (int i = 0; i < 5000 && ok; i++) {
            ghost.update(maze);
            // Hayaletin kapladığı karenin dört köşesi de yolda olmalı
            int[][] corners = {
                {ghost.getY(), ghost.getX()}, {ghost.getY(), ghost.getX() + T - 1},
                {ghost.getY() + T - 1, ghost.getX()}, {ghost.getY() + T - 1, ghost.getX() + T - 1}
            };
            for (int[] c : corners) {
                if (maze.isWall(c[0] / T, c[1] / T)) ok = false;
            }
        }
        check("Hayalet 5000 frame boyunca hic duvara girmiyor", ok);
    }

    static void testCollisionReducesLifeAndResets() {
        Maze maze = new Maze();
        int sr = maze.getPacmanStartRow();
        int sc = maze.getPacmanStartCol();
        Pacman p = new Pacman(sr, sc);
        List<Ghost> ghosts = new ArrayList<>();
        ghosts.add(new Ghost(sr, sc + 2, Color.RED, 2));
        CollisionManager cm = new CollisionManager();

        p.setDirection(0, 1);
        boolean hit = false;
        for (int i = 0; i < 30 && !hit; i++) {
            p.update(maze);
            hit = cm.checkCollision(p, ghosts);
        }
        check("Carpismada can 1 azaliyor (oyun hemen bitmiyor)",
              hit && cm.getLives() == CollisionManager.START_LIVES - 1 && !cm.isGameOver());
        check("Carpisma sonrasi Pac-Man baslangica donuyor",
              p.getRow() == sr && p.getCol() == sc && p.getX() == sc * T);
    }

    /** 4. hafta: büyük haritada ulaşılamayan yem kalmasın, yoksa oyun kazanılamaz. */
    static void testAllFoodsReachable() {
        Maze maze = new Maze();
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
        check("Buyuk haritadaki tum yemlere ulasilabiliyor", ok);
    }

    /** 4. hafta: yön tamponu - erken basılan yön kavşakta uygulanıyor. */
    static void testPacmanTurnsAtJunction() {
        Maze maze = new Maze();
        Pacman p = new Pacman(1, 1); // (1,1)'den sağa gider, (1,5)'te aşağı yol var
        p.setDirection(0, 1);
        p.update(maze);
        p.setDirection(1, 0); // aşağıya erken basıldı, şu an aşağısı duvar
        for (int i = 0; i < 200; i++) p.update(maze);
        check("Erken basilan yon ilk kavsakta uygulaniyor", p.getCol() == 5 && p.getRow() > 1);
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

    static void testGameOverWhenLivesRunOut() {
        List<Ghost> ghosts = new ArrayList<>();
        ghosts.add(new Ghost(5, 5, Color.RED, 2));
        CollisionManager cm = new CollisionManager();
        for (int i = 0; i < CollisionManager.START_LIVES; i++) {
            cm.checkCollision(new Pacman(5, 5), ghosts);
        }
        check("Canlar bitince Game Over", cm.isGameOver() && cm.getLives() == 0);
    }

    static void testWinWhenAllFoodsEaten() {
        Maze maze = new Maze();
        ScoreManager sm = new ScoreManager();
        sm.placeFoods(maze);
        boolean notYet = !sm.allFoodsEaten();
        // Her yemin üstüne bir Pac-Man koyup ye
        for (Food f : new ArrayList<>(sm.getFoods())) {
            sm.checkFood(new Pacman(f.getRow(), f.getCol()));
        }
        check("Tum yemler bitince kazaniliyor (TEBRIKLER)",
              notYet && sm.allFoodsEaten() && sm.getScore() == sm.getFoods().size() * Food.POINTS);
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[HATA] ") + name);
        if (ok) passed++; else failed++;
    }
}
