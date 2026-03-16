#!/bin/sh
# Maven Wrapper Script — Unix/Linux/macOS
# Invokes the embedded Maven wrapper JAR in .mvn/wrapper/

set -e

MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(pwd)}"
while [ ! -f "${MAVEN_PROJECTBASEDIR}/.mvn/wrapper/maven-wrapper.properties" ]; do
  MAVEN_PROJECTBASEDIR=$(dirname "${MAVEN_PROJECTBASEDIR}")
  if [ "/" = "${MAVEN_PROJECTBASEDIR}" ]; then
    echo "Error: Cannot find .mvn/wrapper/maven-wrapper.properties" >&2
    exit 1
  fi
done

WRAPPER_JAR="${MAVEN_PROJECTBASEDIR}/.mvn/wrapper/maven-wrapper.jar"
JAVA_CMD="${JAVA_HOME:+${JAVA_HOME}/bin/}java"

exec "$JAVA_CMD" \
  -classpath "${WRAPPER_JAR}" \
  org.apache.maven.wrapper.MavenWrapperMain \
  "$@"
