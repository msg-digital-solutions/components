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

IF NOT DEFINED OPS4J_PAX_OPTS SET OPS4J_PAX_OPTS="-Dorg.ops4j.pax.url.mvn.useFallbackRepositories=false -Dorg.ops4j.pax.url.mvn.repositories=\"https://repo.maven.apache.org/maven2@id=maven,https://artifacts-oss.talend.com/nexus/content/repositories/TalendOpenSourceRelease/@id=talend\""

java %JAVA_OPTS% %OPS4J_PAX_OPTS% -Xmx2048m -Dfile.encoding=UTF-8 -Dorg.ops4j.pax.url.mvn.localRepository="%THISDIR%\.m2" -Dorg.ops4j.pax.url.mvn.settings="%THISDIR%config\settings.xml" -Dcomponent.default.config.folder="%THISDIR%\config\default" -cp %CLASSPATH% %APP_CLASS%
