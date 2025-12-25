@echo off
echo ==========================================
echo MozJPEG Full Build (Native + Java)
echo ==========================================

call build_native.bat
if %errorlevel% neq 0 (
    echo Native build failed!
    exit /b %errorlevel%
)

call build_java.bat
if %errorlevel% neq 0 (
    echo Java build failed!
    exit /b %errorlevel%
)

echo.
echo ==========================================
echo Full Build Complete!
echo Native Libs: src\main\resources\native\
echo Java JAR:    build\jar\mozjpeg-java-8.jar
echo ==========================================
pause
