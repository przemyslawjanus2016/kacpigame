# GitHub + automatyczne aktualizacje APK

Repozytoria:
- kod aplikacji: `przemyslawjanus2016/kacpigame`
- wydania APK: `przemyslawjanus2016/kacpigame-release`

Aplikacja sprawdza najnowszy publiczny GitHub Release w `kacpigame-release`.

## 1. Kod aplikacji

Projekt jest przeznaczony do repo `kacpigame`.

```powershell
git init
git add .
git commit -m "Kacper i Kapi 0.4.0"
git branch -M main
git remote add origin https://github.com/przemyslawjanus2016/kacpigame.git
git push -u origin main
```

## 2. Stały klucz podpisu APK

Każda aktualizacja Androida musi być podpisana dokładnie tym samym kluczem.

```powershell
keytool -genkeypair -v -keystore kacperkapi-release.jks -alias kacperkapi -keyalg RSA -keysize 4096 -validity 10000
```

Nie dodawaj pliku JKS ani haseł do repozytorium. Zachowaj kopię klucza poza komputerem.

## 3. Sekrety w repo `kacpigame`

Najpierw przygotuj Base64 klucza:

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("kacperkapi-release.jks")) | Set-Content -NoNewline keystore-base64.txt
```

W `kacpigame -> Settings -> Secrets and variables -> Actions` dodaj:

- `ANDROID_KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `RELEASE_REPO_TOKEN`

`RELEASE_REPO_TOKEN` to Fine-grained Personal Access Token mający dostęp do repo `kacpigame-release` i uprawnienie **Contents: Read and write**. Workflow używa go tylko do utworzenia Release i wysłania APK do repozytorium wydań.

## 4. Pierwsze wydanie

Po skonfigurowaniu sekretów:

```powershell
git tag -a v0.4.0 -m "Kacper i Kapi v0.4.0"
git push origin v0.4.0
```

Workflow w `kacpigame` zbuduje podpisane APK i opublikuje je w:
`https://github.com/przemyslawjanus2016/kacpigame-release/releases`

## 5. Każda następna aktualizacja

1. Zwiększ `versionCode` i `versionName` w `app/build.gradle.kts`.
2. Commit i push do `main`.
3. Utwórz tag zgodny z wersją, np. `v0.4.1`.
4. GitHub Actions zbuduje APK i utworzy Release w `kacpigame-release`.
5. W aplikacji `Ustawienia -> Aktualizacje -> Sprawdź aktualizację` nowa wersja zostanie wykryta.

## Blokada poziomów

`DevOptions.UNLOCK_ALL_CONTENT = false`.

- start: Wieliczka, etap 1,
- następny etap po wyniku co najmniej 4/5,
- po zaliczeniu etapu 10 odblokowuje się następny świat,
- reset danych wraca do Wieliczki, etap 1.
