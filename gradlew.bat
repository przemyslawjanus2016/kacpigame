@echo off
setlocal
set "DIR=%~dp0"
set "JAR=%DIR%gradle\wrapper\gradle-wrapper.jar"
set "WRAPPER_URL=https://raw.githubusercontent.com/gradle/gradle/v8.13.0/gradle/wrapper/gradle-wrapper.jar"
set "EXPECTED_SHA=81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f"

if not exist "%JAR%" (
  echo [Kacper i Kapi] Pobieram oficjalny Gradle Wrapper 8.13...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "try { Invoke-WebRequest -UseBasicParsing '%WRAPPER_URL%' -OutFile '%JAR%' } catch { Write-Error $_; exit 1 }"
  if errorlevel 1 exit /b 1

  for /f "tokens=*" %%H in ('powershell -NoProfile -Command "(Get-FileHash -Algorithm SHA256 '%JAR%').Hash.ToLower()"') do set "ACTUAL_SHA=%%H"
  if /I not "%ACTUAL_SHA%" "%EXPECTED_SHA%" (
    echo BLAD: suma SHA256 Gradle Wrapper nie zgadza sie.
    del /q "%JAR%" 2>nul
    exit /b 1
  )
  echo [Kacper i Kapi] Gradle Wrapper zweryfikowany.
)

java -classpath "%JAR%" org.gradle.wrapper.GradleWrapperMain %*
endlocal
