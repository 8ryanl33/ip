@ECHO OFF

REM create bin directory if it doesn't exist
if not exist ..\bin mkdir ..\bin

REM delete output from previous run
if exist ACTUAL.TXT del ACTUAL.TXT

REM delete the save file left behind by a previous run, so the first
REM session below always starts with an empty list
if exist data rmdir /s /q data

REM compile the code into the bin folder
javac  -cp ..\src\main\java -Xlint:none -d ..\bin ..\src\main\java\*.java
IF ERRORLEVEL 1 (
    echo ********** BUILD FAILURE **********
    exit /b 1
)
REM no error here, errorlevel == 0

REM FIRST RUN: feed commands from input.txt and redirect the output to ACTUAL.TXT
java -classpath ..\bin Goat < input.txt > ACTUAL.TXT

REM show what was written to the hard disk, so the saved format is tested too
echo ===== SAVED FILE =====>> ACTUAL.TXT
type data\goat.txt >> ACTUAL.TXT

REM SECOND RUN: start again without adding anything, to prove the tasks reload
echo ===== RESTART =====>> ACTUAL.TXT
java -classpath ..\bin Goat < input-restart.txt >> ACTUAL.TXT

REM THIRD RUN: damage the save file, then start the program again, to check
REM that a hand-edited file with a mistake in it is reported, not crashed on
echo ===== DAMAGED FILE =====>> ACTUAL.TXT
(
echo T ^| 1 ^| read book
echo X ^| 0 ^| not a real task type
) > data\goat.txt
type data\goat.txt >> ACTUAL.TXT
echo ===== RECOVERY =====>> ACTUAL.TXT
java -classpath ..\bin Goat < input-corrupt.txt >> ACTUAL.TXT

REM FOURTH RUN: delete only the save file, keeping the data folder, which is
REM the other first-run situation the requirement names
echo ===== FILE DELETED, FOLDER KEPT =====>> ACTUAL.TXT
del data\goat.txt
java -classpath ..\bin Goat < input-nofile.txt >> ACTUAL.TXT
echo ===== FILE REMADE =====>> ACTUAL.TXT
type data\goat.txt >> ACTUAL.TXT

REM compare the output to the expected output
FC ACTUAL.TXT EXPECTED.TXT
