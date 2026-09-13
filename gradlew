#!/usr/bin/env sh
set -eu
DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
URL="https://raw.githubusercontent.com/gradle/gradle/v8.13.0/gradle/wrapper/gradle-wrapper.jar"
EXPECTED="81a82aaea5abcc8ff68b3dfcb58b3c3c429378efd98e7433460610fecd7ae45f"

if [ ! -f "$JAR" ]; then
  echo "[Kacper i Kapi] Pobieram oficjalny Gradle Wrapper 8.13..."
  if command -v curl >/dev/null 2>&1; then
    curl -L --fail "$URL" -o "$JAR"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$JAR" "$URL"
  else
    echo "Brak curl/wget. Pobierz gradle-wrapper.jar 8.13 ręcznie."
    exit 1
  fi
  ACTUAL=$(sha256sum "$JAR" | awk '{print $1}')
  if [ "$ACTUAL" != "$EXPECTED" ]; then
    echo "BŁĄD: suma SHA256 Gradle Wrapper nie zgadza się."
    rm -f "$JAR"
    exit 1
  fi
fi

exec java -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
