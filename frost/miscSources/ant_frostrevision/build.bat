@echo off
rem adapt for your OS and environment ;)
set CLASSPATH=.;C:\Tools\ant\lib\ant.jar
c:\tools\jdk15\bin\javac frost/buildsupport/FrostRevision.java
jar cf frostsvn.jar frost/buildsupport/*.class
pause
