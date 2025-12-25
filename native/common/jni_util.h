#ifndef JNI_UTIL_H
#define JNI_UTIL_H

#include <jni.h>

// Helper to cast jlong to pointer
template <typename T>
T *getHandle(jlong handle) {
    return reinterpret_cast<T *>(handle);
}

// Helper to cast pointer to jlong
template <typename T>
jlong createHandle(T *ptr) {
    return reinterpret_cast<jlong>(ptr);
}

#endif // JNI_UTIL_H
