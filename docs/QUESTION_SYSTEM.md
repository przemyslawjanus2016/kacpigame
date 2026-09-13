# System pytań v0.2.0

## Założenie
Gra nie korzysta z krótkiej, zamkniętej listy quizów. Pytania są tworzone hybrydowo:

1. banki treści (słówka, ortografia, fakty),
2. proceduralne generatory liczb, czasu, pieniędzy, logiki i wariantów językowych,
3. historia pytań zapisująca do 5000 ostatnio pokazanych identyfikatorów.

Dzięki temu pytania nie powinny powtarzać się w normalnej rozgrywce przez bardzo długi czas.

## Rozmiar baz startowych

- 96 par słówek PL/EN,
- 30 zestawów ortograficznych,
- 24 słowa z policzoną liczbą sylab,
- 37 pytań/faktów przyrodniczych i geograficznych,
- 70 etapów świata (7 światów × 10 etapów), które są też źródłem pytań kontekstowych.

## Generatory

### Matematyka
- dodawanie,
- odejmowanie,
- mnożenie,
- dzielenie,
- brakująca liczba,
- porównania,
- zakupy,
- zadania tekstowe.

### Język polski
- ortografia,
- sylaby,
- rzeczownik,
- czasownik,
- przymiotnik,
- interpunkcja,
- kombinowane zestawy słów generowane losowo.

### Angielski
- PL → EN,
- EN → PL,
- obrazek/emoji → słowo,
- kategorie znaczeniowe,
- zwroty podróżne,
- poprawna pisownia,
- pierwsza litera,
- najdłuższe słowo,
- słowo niepasujące do grupy.

### Logika
- ciągi arytmetyczne,
- wzory powtarzalne,
- niepasujący element,
- porządkowanie,
- analogie.

### Przyroda
- fakty,
- klasyfikowanie zwierząt,
- pory roku,
- siedliska.

### Wiedza o świecie
- fakty o miastach,
- rozpoznawanie miejsc danego świata,
- kraj, w którym znajduje się miejsce,
- kolejność podróży.

### Życie codzienne
- zegar,
- czas trwania,
- reszta z zakupów,
- sumy zakupów,
- dni tygodnia,
- miesiące,
- jednostki miary,
- temperatury,
- kierunki świata.

## Test generatora
W lokalnym teście developerskim wygenerowano po 1000 pytań dla każdej z 7 dziedzin. Wszystkie miały poprawny indeks odpowiedzi i zgodną liczbę opcji PL/EN; praktycznie wszystkie miały unikalne identyfikatory w tej próbie.
