@echo off
setlocal EnableDelayedExpansion

echo ==========================================
echo MozJPEG JNI Build Script (Windows)
echo Supports x64 and x86 (Win32)
echo ==========================================

set WORK_DIR=%CD%
set MOZJPEG_VERSION=v4.1.5

REM Clean up previous builds
if exist mozjpeg-build rmdir /s /q mozjpeg-build
if exist native\mozjpeg\build-x64 rmdir /s /q native\mozjpeg\build-x64
if exist native\mozjpeg\build-x86 rmdir /s /q native\mozjpeg\build-x86

REM 1. Clone MozJPEG
mkdir mozjpeg-build
cd mozjpeg-build
echo Cloning MozJPEG...
git clone https://github.com/mozilla/mozjpeg.git
cd mozjpeg
git checkout %MOZJPEG_VERSION%
cd %WORK_DIR%

REM 2. Build for x64
call :BuildArch x64 x64
if %errorlevel% neq 0 (
    echo x64 Build Failed!
    exit /b %errorlevel%
)

REM 3. Build for x86
call :BuildArch Win32 x86
if %errorlevel% neq 0 (
    echo x86 Build Failed!
    echo Note: You need a 32-bit JDK installed for the x86 build to succeed.
    exit /b %errorlevel%
)

echo ==========================================
echo All builds successful!
echo ==========================================
exit /b 0

REM ==========================================
REM Function: BuildArch
REM Arguments: %1 = CMake Arch (x64 or Win32), %2 = Java Arch (x64 or x86)
REM ==========================================
:BuildArch
set CMAKE_ARCH=%1
set JAVA_ARCH=%2
set BUILD_DIR_MOZ=mozjpeg-build\build-%JAVA_ARCH%
set INSTALL_DIR_MOZ=%WORK_DIR%\mozjpeg-build\install-%JAVA_ARCH%
set BUILD_DIR_JNI=native\mozjpeg\build-%JAVA_ARCH%

echo.
echo ------------------------------------------
echo Building %JAVA_ARCH% (CMake Arch: %CMAKE_ARCH%)
echo ------------------------------------------

REM --- Build MozJPEG Static ---
mkdir %BUILD_DIR_MOZ%
cd %BUILD_DIR_MOZ%

echo Configuring MozJPEG (%JAVA_ARCH%)...
cmake -A %CMAKE_ARCH% ^
    -DCMAKE_INSTALL_PREFIX="%INSTALL_DIR_MOZ%" ^
    -DPNG_SUPPORTED=OFF ^
    -DWITH_TURBOJPEG=OFF ^
    -DENABLE_SHARED=OFF ^
    -DENABLE_STATIC=ON ^
    -DCMAKE_POLICY_VERSION_MINIMUM=3.5 ^
    ..\..\mozjpeg-build\mozjpeg

if %errorlevel% neq 0 exit /b %errorlevel%

echo Building MozJPEG Static Lib (%JAVA_ARCH%)...
cmake --build . --config Release --target jpeg-static
if %errorlevel% neq 0 exit /b %errorlevel%

echo Installing MozJPEG Artifacts (%JAVA_ARCH%)...
mkdir "%INSTALL_DIR_MOZ%"
mkdir "%INSTALL_DIR_MOZ%\include"
mkdir "%INSTALL_DIR_MOZ%\lib"

copy ..\..\mozjpeg-build\mozjpeg\jpeglib.h "%INSTALL_DIR_MOZ%\include\" >nul
copy ..\..\mozjpeg-build\mozjpeg\jmorecfg.h "%INSTALL_DIR_MOZ%\include\" >nul
copy ..\..\mozjpeg-build\mozjpeg\jerror.h "%INSTALL_DIR_MOZ%\include\" >nul
copy jconfig.h "%INSTALL_DIR_MOZ%\include\" >nul
copy Release\jpeg-static.lib "%INSTALL_DIR_MOZ%\lib\" >nul

cd %WORK_DIR%

REM --- Build JNI Wrapper ---
mkdir %BUILD_DIR_JNI%
cd %BUILD_DIR_JNI%

echo Configuring JNI Wrapper (%JAVA_ARCH%)...
cmake -A %CMAKE_ARCH% ^
    -DMOZJPEG_DIR="%INSTALL_DIR_MOZ%" ^
    ..

if %errorlevel% neq 0 exit /b %errorlevel%

echo Building JNI Wrapper (%JAVA_ARCH%)...
cmake --build . --config Release
if %errorlevel% neq 0 exit /b %errorlevel%

REM --- Copy Artifacts ---
echo Copying DLL to resources (%JAVA_ARCH%)...
set TARGET_RES=src\main\resources\native\windows\%JAVA_ARCH%
cd %WORK_DIR%
if not exist "%TARGET_RES%" mkdir "%TARGET_RES%"
copy "%BUILD_DIR_JNI%\Release\mozjpeg_jni.dll" "%TARGET_RES%\"

echo Done with %JAVA_ARCH%.
exit /b 0
