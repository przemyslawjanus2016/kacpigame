# Google Play – pełna wersja Kacper & Kapi

## Model sprzedażowy

- Aplikacja do pobrania bezpłatnie.
- Wieliczka (świat 1) pozostaje darmowa.
- Światy 2–7 wymagają jednorazowego zakupu pełnej wersji.
- Brak reklam i brak abonamentu.
- Produkt Google Play: `full_game_unlock`.

## Konfiguracja w Play Console

1. Najpierw prześlij AAB aplikacji `pl.janusdigital.kacperikapi` do testu wewnętrznego.
2. Otwórz: **Monetize with Play → Products → One-time products**.
3. Utwórz produkt o identyfikatorze dokładnie:
   - `full_game_unlock`
4. Typ: jednorazowy produkt kupowany raz (niekonsumpcyjny / trwałe odblokowanie).
5. Dodaj opcję zakupu **Buy** i aktywuj ją.
6. Ustaw cenę startową, np. 39,99 PLN, a następnie sprawdź ceny regionalne.
7. Dodaj lokalizowane nazwy i opisy produktu dla PL, EN, DE, ES, IT i SK.
8. Zapisz i aktywuj produkt.

## Testowanie bez prawdziwych pieniędzy

1. W Play Console dodaj konto testowe do **License testing**.
2. Dodaj to samo konto jako testera ścieżki **Internal testing**.
3. Instaluj aplikację z linku Google Play dla testera, nie z ręcznie wgranego APK.
4. Sprawdź:
   - udany zakup,
   - anulowanie zakupu,
   - zakup oczekujący (pending),
   - ponowne uruchomienie aplikacji,
   - reinstalację i „Przywróć zakup”,
   - zwrot/refund i ponowne odebranie dostępu.

## Zasady aplikacji

- Zakup uruchamia się dopiero po przejściu bramki rodzicielskiej.
- Dostęp jest przyznawany dopiero dla stanu `PURCHASED`.
- Zakup `PENDING` nie odblokowuje treści.
- Zakup jest potwierdzany (`acknowledgePurchase`).
- Przy starcie aplikacja pyta Google Play o posiadane produkty i przywraca odblokowanie.
- Lokalny zapis jest tylko cache ostatnio potwierdzonego stanu. Docelowo przed większą skalą sprzedaży warto dodać weryfikację tokenu zakupu po stronie serwera.

## Ważne dla wersji testowych

Ręcznie instalowany APK spoza Google Play nie jest właściwym środowiskiem do testowania rzeczywistego zakupu Google Play. Do testów Billing używaj Internal testing w Play Console.
