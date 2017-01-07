#!/usr/bin/env bash
# Proteus Shell Script
# Uses Intellij IDEA's Artifact
# Useful for dev environment when wanting to run from a console.

# Make sure you [re-]build an exploded artifact first.
# You should clean out your artifacts directory of old versions.

cd $(dirname $0)/..
ROOT=$(pwd)
PROJECT_NAME="starter-app"
CONFIG_CLASS="com.example.app.config.ProjectConfig"

JSP_API_JAR="$(find ${HOME}/.gradle/caches/ -iname 'jsp-api-2.2.jar' | head -n 1)"
SERVLET_API_JAR="$(find ${HOME}/.gradle/caches/ -iname 'javax.servlet-api-3.0.?.jar' | head -n 1)"
WEB_INF="$(ls -d build/gradle/libs/exploded/*.war/WEB-INF || ls -d build/idea/artifacts/${PROJECT_NAME}/exploded/*.war/WEB-INF)"
PROPERTIES="file://${ROOT}/src/main/webapp/META-INF/resources/contexts/development.properties"
WEAVER="-javaagent:runtime-aspects/aspectjweaver.jar -javaagent:runtime-aspects/spring-instrument.jar"
#WEAVER=""

${JAVA_HOME}/bin/java -Dspring.config=${CONFIG_CLASS} \
  -Djava.io.tmpdir=/tmp/${PROJECT_NAME}/shell \
  -Dspring.properties=${PROPERTIES} \
  -Xverify:none \
  -XX:MaxPermSize=1024M \
  -Xmx2000M \
  -DRebuildSessionFactoryDelay=-1 \
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
  ${JVM_OPTS} \
  ${WEAVER} \
  -cp "${JSP_API_JAR}:${SERVLET_API_JAR}:${WEB_INF}/classes:${WEB_INF}/lib/*" \
  net.proteusframework.shell.ProteusShell "$@"
