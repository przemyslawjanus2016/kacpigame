@echo off
setlocal
cd /d "%~dp0"
echo ==============================================
echo Kacper ^& Kapi v0.2.1 CLEAN
echo Czyszczenie starego buildu i cache projektu...
echo ==============================================
if exist app\build rmdir /s /q app\build
if exist build rmdir /s /q build
if exist .gradle rmdir /s /q .gradle
if exist .kotlin rmdir /s /q .kotlin
echo.
echo Gotowe. Teraz otworz ten folder w Android Studio.
echo File ^> Sync Project with Gradle Files, a potem Build ^> Rebuild Project.
pause
