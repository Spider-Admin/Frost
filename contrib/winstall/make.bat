@echo off

rem *** Define the makensis.exe path here ***
set makensis=c:\nsis\makensis-bz2.exe

echo Cleaning directories...
del *.exe
del log.txt
del data\*.exe

if %1 == clean goto end

echo Compiling language dependent scripts...
for %%a in (lang\*.nsi) do %makensis% %%a >>log.txt
move *.exe data >nul

echo Compiling main script...
%makensis% main.nsi >>log.txt
echo.
echo Done, see log.txt for details

:end
set makensis=