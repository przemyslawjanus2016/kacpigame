$ErrorActionPreference = "Stop"
$Repo = "przemyslawjanus2016/kacpigame"

Write-Host "== Kacper i Kapi -> GitHub ==" -ForegroundColor Cyan

gh auth status

if (-not (Test-Path ".git")) {
    git init
}

git branch -M main

$repoExists = $true
try {
    gh repo view $Repo --json nameWithOwner | Out-Null
} catch {
    $repoExists = $false
}

if (-not $repoExists) {
    Write-Host "Tworze publiczne repo $Repo ..." -ForegroundColor Yellow
    gh repo create $Repo --public --source . --remote origin
} else {
    $origin = git remote get-url origin 2>$null
    if (-not $origin) {
        git remote add origin "https://github.com/$Repo.git"
    }
}

git add .
$changes = git status --porcelain
if ($changes) {
    git commit -m "Kacper i Kapi 0.4.0 - GitHub release build"
}

git push -u origin main
Write-Host "Kod jest na https://github.com/$Repo" -ForegroundColor Green
Write-Host "Teraz skonfiguruj sekrety podpisu wg GITHUB_RELEASE_SETUP.md, a nastepnie uruchom CREATE_RELEASE.ps1" -ForegroundColor Yellow
