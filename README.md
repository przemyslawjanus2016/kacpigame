# Kacper & Kapi – Edukacyjna Przygoda 0.5.0

Gra edukacyjno-podróżnicza na Androida (Kotlin + Jetpack Compose), przygotowywana na telefon, tablet i publikację w Google Play.

## 0.5.0 – główne zmiany
- **7 światów × 10 etapów = 70 misji kampanii**,
- zakres trudności **4–8 lat**,
- etap 1 zaczyna się od najprostszych zadań przedszkolnych, a kolejne etapy stopniowo podnoszą poziom,
- kliknięcie punktu na mapie otwiera najpierw kartę **Poznaj to miejsce** z prawdziwym zdjęciem, opisem i ciekawostką, dopiero potem można rozpocząć misję,
- kolejny etap odblokowuje wynik co najmniej **5/7 poprawnych odpowiedzi**,
- po słabszym wyniku aplikacja jasno informuje, dlaczego następny etap pozostaje zablokowany,
- dotknięcie zablokowanego punktu pokazuje wymaganie odblokowania,
- przebudowany hybrydowy generator pytań z bankami offline, generowaniem proceduralnym, historią pytań i lekką adaptacją trudności,
- wersja główna jest przygotowywana pod zasady Google Play: brak reklam, brak konta dziecka i brak uprawnienia `REQUEST_INSTALL_PACKAGES`.

## Kampania i trudność

Każdy świat ma 7 aktywnych etapów. Docelowy poziom wieku rośnie następująco:

| Etap | Orientacyjny poziom |
|---|---|
| 1 | 4 lata |
| 2 | 4 lata |
| 3 | 5 lat |
| 4 | 6 lat |
| 5 | 7 lat |
| 6 | 8 lat |
| 7 | 8 lat |

Poziom jest dodatkowo lekko adaptowany na podstawie wcześniejszych wyników dziecka, maksymalnie o jeden krok trudności w górę lub w dół.

### Przykładowe zadania
- 4 lata: liczenie obrazków, dodawanie/odejmowanie do 5, pierwsza litera, proste sylaby, kolory i zwierzęta po angielsku, rytmy obrazkowe, podstawowe pytania o przyrodę i codzienne sytuacje,
- 5–6 lat: działania do 10/20, brakująca liczba, proste zadania tekstowe, dni tygodnia, rzeczownik/czasownik, sekwencje i prosta wiedza o świecie,
- 7–8 lat: działania do 100, pieniądze, mnożenie, później dzielenie, ortografia, części mowy, zegar, analogie, pamięć i trudniejsze pytania o miejsca/przyrodę.

## Odblokowywanie
- start: **Wieliczka, etap 1**,
- misja kampanii ma 7 pytań,
- minimum do zaliczenia: **5/7**,
- zaliczenie odblokowuje następny etap,
- zaliczenie etapu 7 odblokowuje następny świat,
- ćwiczenia tematyczne nie odblokowują kampanii,
- `Wyzeruj wszystkie dane` przywraca start od Wieliczki 1.

## Karty atrakcji
Przed każdą aktywną misją wyświetlany jest ekran **Poznaj to miejsce**:
- prawdziwe zdjęcie atrakcji z Wikimedia Commons,
- opis i ciekawostka,
- autor/licencja źródła,
- możliwość odsłuchania treści przez TTS,
- przycisk `Rozpocznij misję`.

Dane atrakcji dla etapów 8–10 pozostają w projekcie jako materiał do przyszłego rozszerzenia, ale w 0.5.0 nie są częścią aktywnej kampanii.

## Pytania
Silnik pytań łączy:
- duże banki treści offline,
- generowane działania matematyczne i zadania logiczne,
- różne typy interakcji (wybór, obrazek, sekwencja, układanie, pamięć),
- historię ostatnio pokazanych pytań, aby ograniczać powtórki,
- dobór tematów wymagających dodatkowego ćwiczenia.

## Języki
Aktualnie pełna zawartość działa w **PL / EN**. Następne planowane pełne lokalizacje to **DE i ES**, a później **IT i SK**. Nowe języki powinny obejmować nie tylko menu, ale także pytania, opisy atrakcji, ciekawostki i TTS.

## Google Play
Docelowa wersja Play będzie publikowana jako **Android App Bundle (AAB)** i aktualizowana przez Google Play. Główna wersja aplikacji nie korzysta z `REQUEST_INSTALL_PACKAGES` ani z samodzielnej instalacji aktualizacji APK.

Założenia publikacyjne:
- aplikacja płatna jednorazowo, bez reklam i bez zakupów w aplikacji na start,
- docelowa grupa: dzieci 4–8 lat,
- polityka prywatności + poprawnie wypełnione sekcje `Docelowi odbiorcy i treści`, `Bezpieczeństwo danych` oraz IARC,
- lokalny zapis postępu, brak konta dziecka, brak lokalizacji/kontaktów.

## Funkcje
- 7 światów: Wieliczka, Kraków, Tatry, Rzym, Londyn, Mediolan, Malta,
- 70 aktywnych etapów,
- matematyka, polski, angielski, logika, przyroda, wiedza o świecie, życie codzienne,
- paszport podróżnika,
- nagrody i gwiazdki,
- misja dnia i streak,
- lektor TTS i dźwięki,
- Panel Rodzica,
- reset wszystkich danych,
- obsługa telefonu i tabletu,
- prawidłowe cofanie systemowym przyciskiem/gestem Wstecz i zachowanie ekranu przy obrocie.

## Wersja
- `versionName 0.5.0`
- `versionCode 18`
- `compileSdk 36`
- `targetSdk 36`

## GitHub
Kod: `przemyslawjanus2016/kacpigame`  
Wydania testowe APK: `przemyslawjanus2016/kacpigame-release`
