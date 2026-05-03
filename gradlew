#!/bin/sh

APP_HOME=$(cd "${0%/*}" || exit 1; pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if ! command -v java >/dev/null 2>&1; then
  echo "ERROR: Java is not installed or not on PATH."
  exit 1
fi

exec java -Xmx64m -Xms64m $JAVA_OPTS $GRADLE_OPTS -Dorg.gradle.appname=gradlew -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
