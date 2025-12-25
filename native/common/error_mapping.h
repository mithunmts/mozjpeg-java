#ifndef ERROR_MAPPING_H
#define ERROR_MAPPING_H

#include <jni.h>
#include <string>

// Exception class name
#define MOZJPEG_EXCEPTION_CLASS "com/genius/mozjpeg/MozJpegException"

// Helper to throw exception from C++
inline void throwJavaException(JNIEnv *env, const char *msg) {
    jclass exClass = env->FindClass(MOZJPEG_EXCEPTION_CLASS);
    if (exClass != NULL) {
        env->ThrowNew(exClass, msg);
    }
}

#endif // ERROR_MAPPING_H
