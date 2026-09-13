@echo off
cd /d "%~dp0"
echo === Kacper i Kapi - przygotowanie projektu ===
call gradlew.bat --version
if errorlevel 1 (
  echo.
  echo Nie udalo sie przygotowac Gradle Wrappera.
  pause
  exit /b 1
)
echo.
echo Gotowe. Otworz ten folder w Android Studio i wykonaj Sync Project with Gradle Files.
pause
