#!/bin/bash
set -e

# MozJPEG JNI Build Script for Linux (x64 and x86)
WORK_DIR="$(pwd)"
MOZJPEG_VERSION="v4.1.5"
BUILD_ROOT="mozjpeg-build"

# Function to build for a specific architecture
build_arch() {
    ARCH=$1
    BITNESS=$2
    
    echo "=========================================="
    echo "Building for Linux $ARCH ($BITNESS-bit)"
    echo "=========================================="

    INSTALL_DIR="$WORK_DIR/$BUILD_ROOT/install-$ARCH"
    BUILD_DIR_MOZ="$WORK_DIR/$BUILD_ROOT/build-$ARCH"
    BUILD_DIR_JNI="$WORK_DIR/native/mozjpeg/build-linux-$ARCH"
    
    # Flags
    if [ "$BITNESS" == "32" ]; then
        FLAGS="-m32 -fPIC"
        # Force NASM to use elf32
        ASM_FLAGS="-f elf32"
        # Help CMake identify target
        CMAKE_ARCH_FLAGS="-DCMAKE_SYSTEM_PROCESSOR=i686 -DCMAKE_ASM_NASM_OBJECT_FORMAT=elf32"
    else
        FLAGS="-m64 -fPIC"
        ASM_FLAGS="-f elf64"
        CMAKE_ARCH_FLAGS="-DCMAKE_SYSTEM_PROCESSOR=x86_64 -DCMAKE_ASM_NASM_OBJECT_FORMAT=elf64"
    fi

    # --- 1. Build MozJPEG Static ---
    echo "Building MozJPEG Static ($ARCH)..."
    
    # Clean build directory to avoid cache mismatch issues
    rm -rf "$BUILD_DIR_MOZ"
    mkdir -p "$BUILD_DIR_MOZ"
    cd "$BUILD_DIR_MOZ"

    cmake -G "Unix Makefiles" \
        -DCMAKE_INSTALL_PREFIX="$INSTALL_DIR" \
        -DENABLE_STATIC=1 \
        -DENABLE_SHARED=0 \
        -DWITH_JPEG8=1 \
        -DWITH_TURBOJPEG=0 \
        -DPNG_SUPPORTED=0 \
        -DCMAKE_C_FLAGS="$FLAGS" \
        -DCMAKE_ASM_NASM_FLAGS="$ASM_FLAGS" \
        $CMAKE_ARCH_FLAGS \
        "$WORK_DIR/$BUILD_ROOT/mozjpeg"

    make -j$(nproc)
    make install

    # --- 2. Build JNI Wrapper ---
    echo "Building JNI Wrapper ($ARCH)..."
    
    # Clean build directory to avoid cache mismatch issues
    rm -rf "$BUILD_DIR_JNI"
    mkdir -p "$BUILD_DIR_JNI"
    cd "$BUILD_DIR_JNI"

    cmake -G "Unix Makefiles" \
        -DCMAKE_BUILD_TYPE=Release \
        -DMOZJPEG_DIR="$INSTALL_DIR" \
        -DCMAKE_CXX_FLAGS="$FLAGS" \
        -DCMAKE_C_FLAGS="$FLAGS" \
        "$WORK_DIR/native/mozjpeg"

    make -j$(nproc)

    # --- 3. Copy Artifacts ---
    TARGET_RES="$WORK_DIR/src/main/resources/native/linux/$ARCH"
    mkdir -p "$TARGET_RES"
    cp libmozjpeg_jni.so "$TARGET_RES/mozjpeg_jni.so"
    
    echo "Finished $ARCH build."
}

# 1. Clone MozJPEG if needed
if [ ! -d "$BUILD_ROOT/mozjpeg" ]; then
    echo "Cloning MozJPEG..."
    mkdir -p "$BUILD_ROOT"
    cd "$BUILD_ROOT"
    git clone https://github.com/mozilla/mozjpeg.git
    cd mozjpeg
    git checkout "$MOZJPEG_VERSION"
    cd "$WORK_DIR"
else
    echo "MozJPEG already cloned."
fi

# 2. Build x64
build_arch "x64" "64"

# 3. Build x86 (Check for multilib support)
echo "Checking for 32-bit build support..."
if echo "int main() { return 0; }" | gcc -m32 -x c - -o /dev/null 2>/dev/null; then
    build_arch "x86" "32"
else
    echo "----------------------------------------------------------------"
    echo "SKIPPING x86 BUILD: gcc-multilib not found or -m32 not supported."
    echo "To build 32-bit on Debian/Ubuntu/WSL: sudo apt-get install gcc-multilib g++-multilib"
    echo "----------------------------------------------------------------"
fi

echo "Done."
