#include <jni.h>
#include "mozjpeg_wrapper.h"
#include "../common/jni_util.h"
#include "../common/error_mapping.h"

extern "C" {

/*
 * Class:     com_genius_mozjpeg_MozJpeg
 * Method:    initEncoder
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_genius_mozjpeg_MozJpeg_initEncoder
  (JNIEnv *env, jclass clazz) {
    try {
        MozJpegWrapper* wrapper = new MozJpegWrapper();
        return createHandle(wrapper);
    } catch (const std::exception& e) {
        throwJavaException(env, e.what());
        return 0;
    }
}

/*
 * Class:     com_genius_mozjpeg_MozJpeg
 * Method:    destroyEncoder
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_genius_mozjpeg_MozJpeg_destroyEncoder
  (JNIEnv *env, jclass clazz, jlong ctx) {
    MozJpegWrapper* wrapper = getHandle<MozJpegWrapper>(ctx);
    if (wrapper) {
        delete wrapper;
    }
}

/*
 * Class:     com_genius_mozjpeg_MozJpeg
 * Method:    encode
 * Signature: (J[BIIIIZZZZI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_genius_mozjpeg_MozJpeg_encode
  (JNIEnv *env, jclass clazz, jlong ctx, jbyteArray rawImage, jint width, jint height, 
   jint colorSpace, jint quality, jboolean progressive, jboolean optimizeHuffman, 
   jboolean trellis, jboolean dering, jint subsampling) {
    
    MozJpegWrapper* wrapper = getHandle<MozJpegWrapper>(ctx);
    if (!wrapper) {
        throwJavaException(env, "Encoder context is null");
        return NULL;
    }

    // Get raw bytes
    jbyte* dataPtr = env->GetByteArrayElements(rawImage, NULL);
    if (!dataPtr) {
        throwJavaException(env, "Failed to get byte array elements");
        return NULL;
    }
    
    jsize dataLen = env->GetArrayLength(rawImage);
    
    // Basic validation
    int components = (colorSpace == 0) ? 3 : 1;
    if (dataLen < width * height * components) {
        env->ReleaseByteArrayElements(rawImage, dataPtr, JNI_ABORT);
        throwJavaException(env, "Input buffer too small for image dimensions");
        return NULL;
    }

    try {
        std::vector<uint8_t> result = wrapper->encode(
            reinterpret_cast<const uint8_t*>(dataPtr),
            width,
            height,
            colorSpace,
            quality,
            progressive,
            optimizeHuffman,
            trellis,
            dering,
            subsampling
        );

        env->ReleaseByteArrayElements(rawImage, dataPtr, JNI_ABORT);

        // Create output array
        jbyteArray output = env->NewByteArray(result.size());
        if (output) {
            env->SetByteArrayRegion(output, 0, result.size(), reinterpret_cast<const jbyte*>(result.data()));
        }
        return output;

    } catch (const std::exception& e) {
        env->ReleaseByteArrayElements(rawImage, dataPtr, JNI_ABORT);
        throwJavaException(env, e.what());
        return NULL;
    }
}

} // extern "C"
