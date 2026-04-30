@echo off
cd "D:\Programs\Java\j8085-Microprocessor-Emulator\Project\src"
javac -encoding UTF-8 -d out *.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed — see errors above.
    pause
    exit /b 1
)

echo.
echo Compiled successfully. Running...
echo.
java -cp out Main
pause