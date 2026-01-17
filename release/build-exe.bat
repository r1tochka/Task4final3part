@echo off
set JAR=cgvsu.jar
set FX=javafx-sdk-17.0.17\lib
set MAIN=com.cgvsu.Main
set NAME=Simple3DViewer
set JDK_BIN="C:\Program Files\Java\jdk-21\bin\jpackage.exe"

%JDK_BIN% ^
  --input . ^
  --main-jar "%JAR%" ^
  --main-class "%MAIN%" ^
  --name "%NAME%" ^
  --type exe ^
  --module-path "%FX%" ^
  --add-modules javafx.controls,javafx.fxml ^
  --dest dist ^
  --win-console

pause
