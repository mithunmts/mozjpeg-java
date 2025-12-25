cmake_minimum_required(VERSION 3.10)
project(mozjpeg_jni)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Use static runtime to match MozJPEG static lib
if(MSVC)
    set(CompilerFlags
        CMAKE_CXX_FLAGS
        CMAKE_CXX_FLAGS_DEBUG
        CMAKE_CXX_FLAGS_RELEASE
        CMAKE_C_FLAGS
        CMAKE_C_FLAGS_DEBUG
        CMAKE_C_FLAGS_RELEASE
    )
    foreach(CompilerFlag ${CompilerFlags})
        string(REPLACE "/MD" "/MT" ${CompilerFlag} "${${CompilerFlag}}")
    endforeach()
endif()

# Find JNI
find_package(JNI REQUIRED)

# Find MozJPEG
# Expect MOZJPEG_DIR to be passed
if (NOT DEFINED MOZJPEG_DIR)
    message(FATAL_ERROR "MOZJPEG_DIR must be defined")
endif()

get_filename_component(MOZJPEG_DIR_ABS "${MOZJPEG_DIR}" ABSOLUTE)
message(STATUS "Searching for MozJPEG in: ${MOZJPEG_DIR_ABS}")

include_directories(
    ${JNI_INCLUDE_DIRS}
    ${MOZJPEG_DIR_ABS}/include
    ../common
)

# Find library - look for static lib first
# Force full path if we know it exists
if (WIN32)
    if (EXISTS "${MOZJPEG_DIR_ABS}/lib/jpeg-static.lib")
        set(MOZJPEG_LIBRARY_STATIC "${MOZJPEG_DIR_ABS}/lib/jpeg-static.lib")
        message(STATUS "Found library manually (Windows): ${MOZJPEG_LIBRARY_STATIC}")
    endif()
else()
    if (EXISTS "${MOZJPEG_DIR_ABS}/lib/libjpeg.a")
        set(MOZJPEG_LIBRARY_STATIC "${MOZJPEG_DIR_ABS}/lib/libjpeg.a")
        message(STATUS "Found library manually (Unix): ${MOZJPEG_LIBRARY_STATIC}")
    endif()
endif()

if (NOT MOZJPEG_LIBRARY_STATIC)
    find_library(MOZJPEG_LIBRARY_STATIC NAMES jpeg-static jpeg
        HINTS ${MOZJPEG_DIR_ABS}/lib
        NO_DEFAULT_PATH
    )
endif()

if (NOT MOZJPEG_LIBRARY_STATIC)
    message(STATUS "Could not find library in ${MOZJPEG_DIR_ABS}/lib")
    # Try listing the directory for debugging
    file(GLOB LIB_FILES "${MOZJPEG_DIR_ABS}/lib/*")
    message(STATUS "Files in lib: ${LIB_FILES}")
    message(FATAL_ERROR "MozJPEG static library not found in ${MOZJPEG_DIR_ABS}/lib")
endif()

add_library(mozjpeg_jni SHARED
    mozjpeg_jni.cpp
    mozjpeg_wrapper.cpp
    ../common/jni_util.cpp
)

target_link_libraries(mozjpeg_jni
    ${MOZJPEG_LIBRARY_STATIC}
)

# Strip the 'lib' prefix on Windows for the DLL
if(WIN32)
    set_target_properties(mozjpeg_jni PROPERTIES PREFIX "")
endif()
