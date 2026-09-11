@echo off
if "%JAVA_HOME%"=="" if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot" set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
pushd "%~dp0food-ordering-app-main"
call mvnw.cmd %*
popd
