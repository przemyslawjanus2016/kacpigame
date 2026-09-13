# Kacper i Kapi – Edukacyjna Przygoda 0.4.0 GITHUB RELEASE

Projekt Android Studio (Kotlin + Jetpack Compose) na telefon i tablet.

## Najważniejsze w 0.4.0
- wersja produkcyjna z **blokadą poziomów i światów**,
- komplet **70 kart atrakcji**: Wieliczka, Kraków, Tatry, Rzym, Londyn, Mediolan i Malta,
- prawdziwe zdjęcia atrakcji z Wikimedia Commons + opisy i ciekawostki PL/EN,
- pozostałe 40 kart korzysta z Wikimedia Commons API i pobiera wraz ze zdjęciem autora oraz licencję,
- mechanizm aktualizacji z **GitHub Releases** w `Ustawienia > Aktualizacje`,
- workflow GitHub Actions do testów oraz automatycznego tworzenia podpisanego APK po wysłaniu taga `vX.Y.Z`,
- docelowe repo aktualizacji: `przemyslawjanus2016/kacpigame`.

## Blokada poziomów
`data/DevOptions.kt` ma:

```kotlin
const val UNLOCK_ALL_CONTENT: Boolean = false
```

Reguły gry:
- start: **Wieliczka, etap 1**,
- misję trzeba zaliczyć wynikiem co najmniej **4/5**,
- wtedy odblokowuje się następny etap,
- ukończenie etapu 10 odblokowuje następny świat i jego etap 1,
- ćwiczenia tematyczne nie odblokowują kampanii,
- `Wyzeruj wszystkie dane` wraca do Wieliczki 1.

## Karty prawdziwych atrakcji
Każda z 70 misji ma ekran **Poznaj to miejsce** przed zadaniem:
- prawdziwe zdjęcie,
- opis PL/EN,
- ciekawostka PL/EN,
- `Posłuchaj / Listen` przez Android TTS,
- autor/licencja Wikimedia Commons,
- cache zdjęcia na urządzeniu do późniejszego użycia offline.

Pierwsze 30 kart ma ręcznie wskazane pliki Commons. Rzym, Londyn, Mediolan i Malta używają precyzyjnych zapytań do Wikimedia Commons API, dzięki czemu aplikacja pobiera rzeczywiste zdjęcie i metadane autora/licencji.

## Aktualizacje przez GitHub Releases
W ustawieniach znajduje się sekcja **Aktualizacje**:
1. aplikacja sprawdza `releases/latest`,
2. porównuje `versionName` z tagiem release, np. `v0.4.1`,
3. pobiera załączony plik `.apk`,
4. otwiera systemowy instalator Androida.

Pierwsza aktualizacja może wymagać zgody Androida na `Instalowanie nieznanych aplikacji` dla tej aplikacji.

> To jest wariant dystrybucji GitHub. Uprawnienie `REQUEST_INSTALL_PACKAGES` należy usunąć w przyszłym wariancie przeznaczonym do Google Play.

## GitHub Actions
- `.github/workflows/build.yml` — test + debug APK przy pushu do `main`,
- `.github/workflows/release.yml` — po tagu `v*.*.*` buduje podpisany release APK i tworzy GitHub Release.

Podpis wydania używa sekretów repozytorium i **jednego stałego klucza JKS**. Klucz ani hasła nie są w repo.

Pełna instrukcja pierwszego uruchomienia GitHub: `GITHUB_RELEASE_SETUP.md`.

## Szybki start Android Studio
1. Rozpakuj projekt do nowego folderu.
2. Otwórz cały folder w Android Studio.
3. `File > Sync Project with Gradle Files`.
4. Uruchom `debug` na telefonie/tablecie.

## Zawartość
- 7 światów,
- 70 etapów,
- 70 kart atrakcji,
- matematyka, polski, angielski, logika, przyroda, wiedza o świecie i życie codzienne,
- fabuła,
- misja dnia i streak,
- paszport podróżnika,
- nagrody,
- TTS i dźwięki,
- adaptacyjne pytania,
- Panel Rodzica,
- reset wszystkich danych,
- PL/EN.

## Prywatność
Postęp pozostaje lokalny. Internet jest używany do zdjęć Wikimedia Commons oraz ręcznego sprawdzania publicznych GitHub Releases. Brak konta dziecka, reklam i dostępu do lokalizacji/kontaktów.

## Wersja
- `versionName 0.4.0`
- `versionCode 16`
- `compileSdk 36`
- `targetSdk 36`



## GitHub

Kod: `przemyslawjanus2016/kacpigame`  
Aktualizacje APK: `przemyslawjanus2016/kacpigame-release`
