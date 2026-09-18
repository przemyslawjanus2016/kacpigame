# Kacper & Kapi: Mój Pierwszy Biznes

Druga edukacyjna gra z uniwersum **Kacper & Kapi**.

## Wersja
- Android: 0.3.0
- applicationId: `pl.janusdigital.kacperkapi.biznes`
- minSdk: 26
- targetSdk / compileSdk: 35

## Co działa
1. Sklepik — liczenie reszty.
2. Uzupełnianie towaru — planowanie stanów półek.
3. Większe zakupy — kilka produktów w jednym rachunku.
4. Promocja dnia — liczenie ceny zestawów promocyjnych.
5. Podsumowanie dnia — sprzedaż, koszty, zysk, oszczędności.
6. Wybór ulepszenia sklepu.
7. Lokalny zapis postępu.

Gra działa offline. Interfejs jest osadzony w Android WebView.

## Budowanie
Projekt wymaga JDK 17, Gradle 8.9 i Android SDK 35.

```bash
gradle assembleDebug
```

APK:
`app/build/outputs/apk/debug/app-debug.apk`

Branch roboczy: `kacper-kapi-biznes-v0.3`.
