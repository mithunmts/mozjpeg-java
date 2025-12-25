@echo off
setlocal

echo ==========================================
echo MozJPEG Java Build Script (Java 8 Target)
echo ==========================================

REM Check for javac
where javac >nul 2>nul
if %errorlevel% neq 0 (
    echo Error: javac not found in PATH.
    echo Please install a JDK [JDK 9+ recommended for --release 8 support] and add it to PATH.
    exit /b 1
)

REM Clean build directory
if exist build\classes rmdir /s /q build\classes
if exist build\jar rmdir /s /q build\jar
mkdir build\classes
mkdir build\jar

echo Compiling Java sources (Target: Java 8)...

REM Collect all java files
dir /s /b com\genius\mozjpeg\*.java > sources.txt

REM Compile with --release 8 to ensure binary compatibility with Java 8 runtime
javac --release 8 -d build\classes @sources.txt
if %errorlevel% neq 0 (
    echo Compilation failed!
    del sources.txt
    exit /b 1
)
del sources.txt

echo Copying resources...
REM Copy native libraries from src/main/resources to the build output
if exist src\main\resources (
    xcopy /s /y /i src\main\resources\* build\classes\
)

echo Creating JAR file...
jar cf build\jar\mozjpeg-java-8.jar -C build\classes .
if %errorlevel% neq 0 (
    echo JAR creation failed!
    exit /b 1
)

echo ==========================================
echo Build Successful!
echo JAR Location: build\jar\mozjpeg-java-8.jar
echo ==========================================
