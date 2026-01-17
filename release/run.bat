@echo off
set APP_JAR=cgvsu.jar
set FX=javafx-sdk-17.0.17\lib
java --module-path "%FX%" --add-modules javafx.controls,javafx.fxml -jar "%APP_JAR%"
pause