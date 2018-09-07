@ECHO OFF

rem Proteus Shell Script
rem Uses Intellij IDEA's Artifact
rem Useful for dev environment when wanting to run from a console.

rem Make sure you [re-]build an exploded artifact first.
rem You should clean out your artifacts directory of old versions.

for %%F in (%1) do CD /D %%~dpF
set ROOT="%cd%"
set PROJECT_NAME=starter-app
set CONFIG_CLASS=com.example.app.config.ProjectConfig

for /F "tokens=* USEBACKQ" %%F IN (`dir %userprofile%\.gradle\caches\*jsp-api-2.2.jar /b/s`) DO (
	set JSP_API_JAR=%%F
)
for /F "tokens=* USEBACKQ" %%F IN (`dir %userprofile%\.gradle\caches\*javax.servlet-api-3.0.1.jar /b/s`) DO (
	set SERVLET_API_JAR=%%F
)
for /F "tokens=* USEBACKQ" %%F IN (`dir %cd%\build\libs\exploded\*.war /b/s`) DO (
    set WEB_INF=%%F\WEB-INF
)
set PROPERTIES="file:///%ROOT%/src/main/webapp/META-INF/resources/contexts/development.properties"
set WEAVER=-javaagent:runtime-aspects/aspectjweaver.jar -javaagent:runtime-aspects/spring-instrument.jar

"%JAVA_HOME%/bin/java" -Dspring.config=%CONFIG_CLASS% ^
  -Djava.io.tmpdir="%TEMP%\%PROJECT_NAME%\shell" ^
  -Dspring.properties=%PROPERTIES% ^
  -Xverify:none ^
  -XX:MaxPermSize=1024M ^
  -Xmx2048M ^
  -DRebuildSessionFactoryDelay=-1 ^
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 ^
  %JVM_OPTS% ^
  %WEAVER% ^
  -Dspring.profiles.active=data-conversion ^
  -Djline.terminal=win ^
  -cp "%JSP_API_JAR%;%SERVLET_API_JAR%;%WEB_INF%\classes;%WEB_INF%\lib\*" ^
  net.proteusframework.shell.ProteusShell %*
