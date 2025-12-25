#ifndef MOZJPEG_WRAPPER_H
#define MOZJPEG_WRAPPER_H

#include <vector>
#include <cstdint>
#include <cstdio>
#include <jpeglib.h>
#include <setjmp.h>

// Custom error manager to handle libjpeg errors via setjmp/longjmp
struct MozJpegErrorMgr {
    struct jpeg_error_mgr pub;
    jmp_buf setjmp_buffer;
    char last_error_msg[JMSG_LENGTH_MAX];
};

class MozJpegWrapper {
public:
    MozJpegWrapper();
    ~MozJpegWrapper();

    // Encodes the image and returns the JPEG bytes
    // Throws std::runtime_error on failure (which JNI layer catches)
    std::vector<uint8_t> encode(
        const uint8_t* raw_data,
        int width,
        int height,
        int color_space,
        int quality,
        bool progressive,
        bool optimize_huffman,
        bool trellis,
        bool dering,
        int subsampling
    );

private:
    struct jpeg_compress_struct cinfo;
    struct MozJpegErrorMgr jerr;
    bool initialized;

    void init();
    void cleanup();
};

#endif // MOZJPEG_WRAPPER_H
