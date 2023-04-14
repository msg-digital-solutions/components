@ECHO OFF

TITLE COMPONENT WEB SERVICE
SET APP_CLASS="org.talend.components.service.rest.Application"

SET THISDIR=%~dp0
SET CLASSPATH=.\config;.\config\default;${project.artifactId}-${project.version}.jar

REM Set env variables which points to hadoop winutils binaries. It is required for S3 component
REM If PAX_MVN_REPO is set, then it means bare service is used and config is located in default directory
IF DEFINED PAX_MVN_REPO (
	SET "HADOOP_HOME=%THISDIR%config\default\hadoop"
) ELSE (
	SET "HADOOP_HOME=%THISDIR%config\hadoop"
)
SET PATH=%PATH%;%HADOOP_HOME%\bin

IF NOT DEFINED OPS4J_PAX_OPTS SET OPS4J_PAX_OPTS=-Dorg.ops4j.pax.url.mvn.useFallbackRepositories=false

REM Check java major version to add some required options for java >=9
SET JAVA_VERSION=0
FOR /f "tokens=3" %%g IN ('java -version 2^>^&1 ^| findstr /i "version"') DO (
  SET JAVA_VERSION=%%g
)
SET JAVA_VERSION=%JAVA_VERSION:"=%
FOR /f "delims=.-_ tokens=1-2" %%v IN ("%JAVA_VERSION%") DO (
  IF /I "%%v" EQU "1" (
    SET JAVA_VERSION=%%w
  ) ELSE (
    SET JAVA_VERSION=%%v
  )
)

IF %JAVA_VERSION% GEQ 9 (
    SET JAVA_OPTS=%JAVA_OPTS% --add-opens java.base/java.net=ALL-UNNAMED
)


java %JAVA_OPTS% %OPS4J_PAX_OPTS% -Xmx2048m -Dfile.encoding=UTF-8 -Dorg.ops4j.pax.url.mvn.localRepository="%THISDIR%\.m2" -Dorg.ops4j.pax.url.mvn.settings="%THISDIR%config\settings.xml" -Dcomponent.default.config.folder="%THISDIR%\config\default" -cp %CLASSPATH% %APP_CLASS%
