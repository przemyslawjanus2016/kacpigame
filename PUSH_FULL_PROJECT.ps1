$ErrorActionPreference = "Stop"

$Repo = "przemyslawjanus2016/kacpigame"
$Source = (Get-Location).Path
$Temp = Join-Path $env:TEMP "kacpigame-upload"

Write-Host "== Kacper i Kapi: full push to GitHub ==" -ForegroundColor Cyan

if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    throw "GitHub CLI (gh) not found. Install it with: winget install --id GitHub.cli"
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    throw "Git not found. Install Git for Windows."
}

$login = (gh api user --jq ".login").Trim()
if ($LASTEXITCODE -ne 0) {
    throw "Could not read GitHub login. Run: gh auth login"
}

if ($login -ne "przemyslawjanus2016") {
    throw "Wrong GitHub account: $login. Expected: przemyslawjanus2016"
}

Write-Host "GitHub account OK: $login" -ForegroundColor Green

gh auth setup-git | Out-Null

if (Test-Path $Temp) {
    Remove-Item $Temp -Recurse -Force
}

Write-Host "Cloning repository..." -ForegroundColor Yellow
git clone "https://github.com/$Repo.git" $Temp
if ($LASTEXITCODE -ne 0) {
    throw "git clone failed"
}

Write-Host "Copying project..." -ForegroundColor Yellow
robocopy "$Source" "$Temp" /MIR /XD ".git" ".gradle" "build" "app\build" /XF "local.properties" "*.jks" "*.keystore" "keystore-base64.txt" | Out-Null
$rc = $LASTEXITCODE
if ($rc -ge 8) {
    throw "Robocopy failed with code: $rc"
}

Push-Location $Temp
try {
    git add -A

    $changes = git status --porcelain
    if (-not $changes) {
        Write-Host "No changes to push." -ForegroundColor Yellow
    }
    else {
        git commit -m "Kacper i Kapi v0.4.0 - full Android project"
        if ($LASTEXITCODE -ne 0) {
            throw "git commit failed"
        }

        git push origin main
        if ($LASTEXITCODE -ne 0) {
            throw "git push failed"
        }

        Write-Host "DONE: full project pushed to https://github.com/$Repo" -ForegroundColor Green
    }
}
finally {
    Pop-Location
}
