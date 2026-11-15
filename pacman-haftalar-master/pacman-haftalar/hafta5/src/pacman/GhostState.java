package pacman;

/**
 * Hayaletin o anki durumu (5. hafta).
 *
 *   NORMAL      -> Pac-Man'e değerse can götürür
 *   FRIGHTENED  -> Pac-Man kiraz yedi: hayalet mavi, yavaş ve yenebilir
 *   DEAD        -> Pac-Man tarafından yendi: ekranda yok, 5 saniye sonra canlanır
 *
 * Sorumlu: Meriç Kavuncuoğlu
 */
public enum GhostState {
    NORMAL,
    FRIGHTENED,
    DEAD
}
