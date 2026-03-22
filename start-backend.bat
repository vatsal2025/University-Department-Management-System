@echo off
echo Starting UDIIMS Backend...
cd /d E:\UDIIMS\backend
set JAVA_HOME=C:\Program Files\Java\jdk-22
set MAVEN_OPTS=-Xmx512m
call mvnw.cmd -Dmaven.repo.local=E:\tools\maven-repo spring-boot:run
pause
