#!/usr/bin/env sh

set -e

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
GRADLE_VERSION="9.4.1"
GRADLE_HOME="$APP_HOME/.gradle-local/gradle-$GRADLE_VERSION"

if [ -f "$WRAPPER_JAR" ]; then
  exec java -jar "$WRAPPER_JAR" "$@"
fi

if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi

if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$APP_HOME/.gradle-local"
  ZIP_PATH="$APP_HOME/.gradle-local/gradle-$GRADLE_VERSION-bin.zip"
  URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  if command -v curl >/dev/null 2>&1; then
    curl -L --fail "$URL" -o "$ZIP_PATH"
  elif command -v wget >/dev/null 2>&1; then
    wget "$URL" -O "$ZIP_PATH"
  else
    echo "Gradle is not installed and neither curl nor wget is available." >&2
    exit 1
  fi
  unzip -q "$ZIP_PATH" -d "$APP_HOME/.gradle-local"
fi

exec "$GRADLE_HOME/bin/gradle" "$@"
