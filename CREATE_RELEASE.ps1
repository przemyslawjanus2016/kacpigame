param(
    [string]$Version = "0.4.0"
)

$ErrorActionPreference = "Stop"
$Repo = "przemyslawjanus2016/kacpigame"
$Tag = "v$Version"
$Temp = Join-Path $env:TEMP "kacpigame-release-tag"

function Assert-LastExitCode([string]$Step) {
    if ($LASTEXITCODE -ne 0) {
        throw "$Step failed (exit code $LASTEXITCODE)."
    }
}

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    throw "Git is not installed."
}
if (-not (Get-Command gh -ErrorAction SilentlyContinue)) {
    throw "GitHub CLI (gh) is not installed."
}

$login = gh api user --jq ".login"
Assert-LastExitCode "GitHub login check"
if ($login.Trim() -ne "przemyslawjanus2016") {
    throw "Wrong GitHub account: $login"
}

if (Test-Path $Temp) {
    Remove-Item $Temp -Recurse -Force
}

git clone "https://github.com/$Repo.git" $Temp
Assert-LastExitCode "git clone"

Push-Location $Temp
try {
    git pull --ff-only origin main
    Assert-LastExitCode "git pull"

    git ls-remote --exit-code --tags origin "refs/tags/$Tag" *> $null
    if ($LASTEXITCODE -eq 0) {
        throw "Tag $Tag already exists on GitHub."
    }

    git tag -a $Tag -m "Kacper i Kapi $Tag"
    Assert-LastExitCode "git tag"

    git push origin $Tag
    Assert-LastExitCode "git push tag"

    Write-Host "DONE: tag $Tag pushed to GitHub." -ForegroundColor Green
    Write-Host "Actions: https://github.com/$Repo/actions" -ForegroundColor Cyan
    Write-Host "Releases: https://github.com/przemyslawjanus2016/kacpigame-release/releases" -ForegroundColor Cyan
}
finally {
    Pop-Location
}
