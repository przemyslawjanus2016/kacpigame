param(
    [string]$Version = "0.4.0"
)

$ErrorActionPreference = "Stop"
$Tag = "v$Version"
$Repo = "przemyslawjanus2016/kacpigame"

if (git status --porcelain) {
    throw "Repo ma niezapisane zmiany. Najpierw commit/push."
}

$existing = git tag -l $Tag
if ($existing) {
    throw "Tag $Tag juz istnieje."
}

git push origin main
git tag -a $Tag -m "Kacper i Kapi $Tag"
git push origin $Tag

Write-Host "Tag $Tag wyslany." -ForegroundColor Green
Write-Host "GitHub Actions zbuduje podpisany APK i opublikuje go tutaj:" -ForegroundColor Cyan
Write-Host "https://github.com/przemyslawjanus2016/kacpigame-release/releases"
Write-Host "Status budowania:" -ForegroundColor Cyan
Write-Host "https://github.com/$Repo/actions"
