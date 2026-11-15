# Pac-Man (BLP 203) – Haftalık Kodlar ve İş Bölümü

Her hafta ayrı bir klasör ve **kendi başına çalışır**. Klasörlerde o haftanın sonundaki kod durumu var.
Her dosyada `// ---------- İsim: ... ----------` yorumlarıyla kimin kısmı olduğu işaretli.

## Çalıştırma (her klasör için aynı)
```
cd hafta5
javac -encoding UTF-8 -d out src/pacman/*.java
java -cp out pacman.Main        # oyun
java -cp out pacman.GameTest    # testler (1. hafta hariç)
```
Kontroller: Yön tuşları (4. haftadan itibaren ayrıca W A S D), oyun bitince **R** ile yeniden başlama. 5. haftada bölüm geçiş ekranı **Enter** ya da **Boşluk** ile atlanabilir.

---

## 1. Hafta – Kurulum, pencere, labirent
İş bölümü: **Meriç & Egemen** sınıfların kodlanması, **Arda & Kaan** labirent tasarımı ve testler.

| Kişi | Ne yaptı |
|---|---|
| Meriç | `GameObject` soyut sınıf taslağı, `Pacman` iskeleti, `GamePanel` ve `Main` (Swing penceresi) |
| Egemen | Klavye dinleyicisi, panele odak verme, Pac-Man'in ilk hareket denemesi |
| Arda | `Maze`: labirentin 2 boyutlu dizi olarak tanımlanması ve duvarların çizimi |
| Kaan | Testler: Pac-Man'in duvarlardan geçtiği hata bulundu, hücre boyutunun sabitlenmesi |

> Bilerek bırakılan hata: Pac-Man duvarların içinden geçiyor (raporla uyumlu, 2. haftada düzeltiliyor).

## 2. Hafta – Hareket, duvar çarpışması, yemler, skor
İş bölümü: **Meriç & Egemen** hareket ve duvar çarpışması, **Arda & Kaan** yemler ve skor sistemi.

| Kişi | Ne yaptı |
|---|---|
| Meriç | Pac-Man yön yapısı, oyun döngüsü |
| Egemen | `canMove()` – duvar çarpışma kontrolü (duvardan geçme hatası çözüldü) |
| Arda | `placeFoods()` – yemlerin yerleşimi, duvar testleri |
| Kaan | `Food` sınıfı, `checkFood()` (yem yenmeme hatası çözüldü), skor, yem/skor testleri |

## 3. Hafta – Hayaletler, çarpışma, can sistemi
İş bölümü: **Meriç & Egemen** Ghost sınıfı ve hayalet hareketi, **Arda & Kaan** çarpışma, can sistemi ve testler.

| Kişi | Ne yaptı |
|---|---|
| Meriç | `Ghost` sınıfı (konum, hız, yön), hayalet çizimi, hayalet listesi |
| Egemen | Rastgele hareket, duvar kontrolü, duvara çarpınca yeni yön, başlangıç konumları |
| Arda | `CollisionManager.checkCollision()`, `resetToStart()`, çarpışma testleri |
| Kaan | Can sistemi, Game Over, skor ve canın ekranda gösterimi, can testi |

## 4. Hafta – Akıcı oyun, büyük harita, yeni grafikler, kazanma ekranı
Öneri iş bölümü: **Meriç & Egemen** akıcı hareket, **Arda & Kaan** harita, grafikler ve kazanma durumu.

| Kişi | Ne yaptı |
|---|---|
| Meriç | `MovingObject` (Pacman ve Ghost'un ortak sınıfı), piksel tabanlı akıcı hareket, 60 FPS oyun döngüsü, oyun durumları (HAZIR / OYUN / KAZANDI / GAME OVER), Pac-Man ağız animasyonu |
| Egemen | Hayaletlerin akıcı hareketi ve kavşaklarda yön seçmesi, yön tamponu (erken basılan yön kavşakta uygulanır), WASD ve R tuşu |
| Arda | Büyük harita (19x15 → 27x24), haritanın metinden okunması, yeni duvar grafikleri, piksel mesafeli çarpışma, harita erişim testi |
| Kaan | **TEBRİKLER ekranı** (tüm yemler bitince), parlayan yemler, yeni skor/can/kalan yem çubuğu, Game Over penceresi, kazanma testi |

**3. haftaya göre değişenler:**
- Oyun 150 ms yerine 16 ms'de bir güncelleniyor (~7 FPS → ~60 FPS). Karakterler kareden kareye zıplamıyor, kayarak ilerliyor.
- Duvarlar her frame'de yeniden çizilmiyor; bir kez resme çizilip saklanıyor, bu da akıcılığa yardım ediyor.
- Çarpışmada can gidince kısa bir "HAZIR!" beklemesi var.

Testler: 11/11 geçiyor.

## 5. Hafta – Kiraz, hayalet yeme, canlanma, Bölüm 2
Öneri iş bölümü: **Meriç & Egemen** hayalet durumları ve süreler, **Arda & Kaan** kiraz, ikinci harita, puanlar ve testler.

| Kişi | Ne yaptı |
|---|---|
| Meriç | `GhostState` (NORMAL / FRIGHTENED / DEAD), hayaletin durum geçişleri (`frighten`, `calm`, `eaten`), korkmuş (mavi) ve canlanmak üzere olan hayaletin çizimi, bölüm geçişi (`loadLevel`, LEVEL_CLEAR durumu) |
| Egemen | Korkan hayaletin yavaşlaması ve geri dönmesi, **5 saniyelik canlanma süresi** (`tick`), `setSpeed`, sürelerin gerçek zamanla ölçülmesi (`deltaMs`), bölüme göre hayalet hızları |
| Arda | **Bölüm 2 haritası**, `Maze(level)` ile bölüm seçimi, haritada `C` (kiraz) ve `G` (hayalet) işaretleri, bölüme göre duvar rengi, korkmuş hayaletin yenmesi (`CollisionManager`), harita testleri |
| Kaan | `Cherry` sınıfı, `PowerManager` (**7 saniyelik hayalet yeme süresi**), kiraz (+50) ve hayalet (+200) puanı, süre çubuğu, bölüm geçiş ve oyun sonu ekranları, kiraz/süre testleri |

**Oyun kuralları:**
- Her bölümde köşelerde 4 kiraz var. Kiraz yenince hayaletler 7 saniye boyunca mavi ve yavaş olur; bu sürede Pac-Man onları yiyebilir. Son 2 saniyede yanıp sönerek uyarırlar.
- Süre bitmeden ikinci kiraz yenirse süre yeniden 7 saniyeden başlar.
- Yenen hayalet 5 saniye sonra kendi başlangıç noktasında canlanır. Canlanan hayalet, süre bitmemiş olsa bile yine tehlikelidir. Canlanmadan önceki son 1,5 saniyede o noktada soluk soluk görünür.
- Bölüm 1'in tüm yemleri bitince TEBRİKLER ekranı çıkar ve 3 saniye sonra Bölüm 2 başlar. Skor ve canlar korunur. Bölüm 2'nin haritası farklı (yeşil duvarlar) ve hayaletleri daha hızlıdır.
- Bölüm 2 de bitince oyun kazanılır.

**Yeni bölüm eklemek:** `Maze.java` içinde yeni bir harita dizisi yazıp `LEVELS` dizisine eklemek yeterli; oyun kendiliğinden Bölüm 3'e geçer.

Testler: 23/23 geçiyor.
