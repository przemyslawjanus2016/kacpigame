# GitHub + automatyczne aktualizacje APK

Repozytoria:
- kod aplikacji: `przemyslawjanus2016/kacpigame`
- wydania APK: `przemyslawjanus2016/kacpigame-release`

Aplikacja sprawdza najnowszy publiczny GitHub Release w `kacpigame-release`.

## Stały klucz podpisu APK
Każda aktualizacja Androida musi być podpisana dokładnie tym samym kluczem.

```powershell
keytool -genkeypair -v -keystore kacperkapi-release.jks -alias kacperkapi -keyalg RSA -keysize 4096 -validity 10000
```

Nie dodawaj pliku JKS ani haseł do repozytorium.

## Sekrety GitHub Actions w repo `kacpigame`
Dodaj w `Settings -> Secrets and variables -> Actions`:
- `ANDROID_KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `RELEASE_REPO_TOKEN`

Base64 klucza:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("kacperkapi-release.jks")) | Set-Content -NoNewline keystore-base64.txt
```

`RELEASE_REPO_TOKEN` powinien być Fine-grained PAT z dostępem tylko do `kacpigame-release` i `Contents: Read and write`.

## Publikacja wersji
1. Zwiększ `versionCode` i `versionName` w `app/build.gradle.kts`.
2. Commit + push do `main`.
3. Dodaj tag, np.:
```powershell
git tag -a v0.4.1 -m "Kacper i Kapi v0.4.1"
git push origin v0.4.1
```
4. Workflow zbuduje podpisany APK i opublikuje go w `kacpigame-release`.
5. Aplikacja wykryje nowy release przez `Ustawienia -> Aktualizacje -> Sprawdź aktualizację`.

## Blokada poziomów
- `UNLOCK_ALL_CONTENT = false`
- start: Wieliczka, etap 1
- kolejny etap po wyniku min. 4/5
- następny świat po zaliczeniu etapu 10
- reset danych wraca do Wieliczki, etap 1
