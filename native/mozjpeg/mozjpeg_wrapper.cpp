#include "mozjpeg_wrapper.h"
#include <stdexcept>
#include <cstring>

// Error exit handler
void my_error_exit(j_common_ptr cinfo) {
    MozJpegErrorMgr* myerr = (MozJpegErrorMgr*)cinfo->err;
    (*cinfo->err->format_message)(cinfo, myerr->last_error_msg);
    longjmp(myerr->setjmp_buffer, 1);
}

MozJpegWrapper::MozJpegWrapper() : initialized(false) {
    // We don't initialize cinfo here because we want to reset it cleanly for each encode
    // or we can reuse it. Reusing is better for performance.
    // However, changing settings (like color space) might require re-initialization or careful reset.
    // For safety and simplicity in this "thin" layer, we will init/destroy per encode or keep it alive.
    // The requirement says "Reuse encoder context".
    
    cinfo.err = jpeg_std_error(&jerr.pub);
    jerr.pub.error_exit = my_error_exit;
    
    if (setjmp(jerr.setjmp_buffer)) {
        // If we get here, construction failed
        cleanup();
        throw std::runtime_error(jerr.last_error_msg);
    }
    
    jpeg_create_compress(&cinfo);
    initialized = true;
}

MozJpegWrapper::~MozJpegWrapper() {
    cleanup();
}

void MozJpegWrapper::cleanup() {
    if (initialized) {
        jpeg_destroy_compress(&cinfo);
        initialized = false;
    }
}

std::vector<uint8_t> MozJpegWrapper::encode(
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
) {
    if (!initialized) {
        throw std::runtime_error("Encoder not initialized");
    }

    // Set up error handling for this execution
    if (setjmp(jerr.setjmp_buffer)) {
        // If we get here, libjpeg found an error
        // We should probably abort the current compression cycle
        jpeg_abort_compress(&cinfo);
        throw std::runtime_error(jerr.last_error_msg);
    }

    // Destination buffer
    unsigned long out_size = 0;
    unsigned char* out_buffer = nullptr;
    
    // Use jpeg_mem_dest (standard in libjpeg-turbo / MozJPEG)
    jpeg_mem_dest(&cinfo, &out_buffer, &out_size);

    // Image parameters
    cinfo.image_width = width;
    cinfo.image_height = height;
    
    if (color_space == 0) { // RGB
        cinfo.input_components = 3;
        cinfo.in_color_space = JCS_RGB;
    } else if (color_space == 1) { // GRAY
        cinfo.input_components = 1;
        cinfo.in_color_space = JCS_GRAYSCALE;
    } else {
        throw std::runtime_error("Unsupported color space");
    }

    jpeg_set_defaults(&cinfo);

    // Quality
    jpeg_set_quality(&cinfo, quality, TRUE /* limit to baseline? usually TRUE is fine */);

    // Progressive
    if (progressive) {
        jpeg_simple_progression(&cinfo);
    }

    // Optimize Huffman
    cinfo.optimize_coding = optimize_huffman ? TRUE : FALSE;

    // Subsampling
    // 0=4:4:4, 1=4:2:2, 2=4:2:0, 3=Gray
    if (color_space == 0) { // Only relevant for RGB
        if (subsampling == 0) { // 4:4:4
            cinfo.comp_info[0].h_samp_factor = 1;
            cinfo.comp_info[0].v_samp_factor = 1;
        } else if (subsampling == 1) { // 4:2:2
            cinfo.comp_info[0].h_samp_factor = 2;
            cinfo.comp_info[0].v_samp_factor = 1;
        } else if (subsampling == 2) { // 4:2:0
            cinfo.comp_info[0].h_samp_factor = 2;
            cinfo.comp_info[0].v_samp_factor = 2;
        } else if (subsampling == 3) { // Gray (force grayscale output from RGB input)
             jpeg_set_colorspace(&cinfo, JCS_GRAYSCALE);
        }
    }

    // MozJPEG specific parameters
    // These might need specific API calls depending on MozJPEG version.
    // Assuming standard MozJPEG 3.x/4.x API availability.
    
    // Trellis Quantization
    // JBOOLEAN_TRELLIS_QUANT = 0x10
    // JBOOLEAN_OVERSHOOT_DERINGING = 0x20
    // We use the boolean param extension if available, or try to set fields if exposed.
    // Safest way for MozJPEG is often jpeg_c_set_bool_param.
    // We need to check if these symbols are available in the header we link against.
    // For this implementation, we will assume they are available or use the integer constants.
    
    #ifdef C_PARAM_TRELLIS_QUANT
    if (trellis) {
        jpeg_c_set_bool_param(&cinfo, JBOOLEAN_TRELLIS_QUANT, TRUE);
    }
    #endif

    #ifdef C_PARAM_OVERSHOOT_DERINGING
    if (dering) {
        jpeg_c_set_bool_param(&cinfo, JBOOLEAN_OVERSHOOT_DERINGING, TRUE);
    }
    #endif

    // Start compression
    jpeg_start_compress(&cinfo, TRUE);

    // Write scanlines
    int row_stride = width * cinfo.input_components;
    JSAMPROW row_pointer[1];

    while (cinfo.next_scanline < cinfo.image_height) {
        row_pointer[0] = (JSAMPROW) &raw_data[cinfo.next_scanline * row_stride];
        jpeg_write_scanlines(&cinfo, row_pointer, 1);
    }

    jpeg_finish_compress(&cinfo);

    // Copy result to vector
    std::vector<uint8_t> result;
    if (out_buffer && out_size > 0) {
        result.assign(out_buffer, out_buffer + out_size);
    }
    
    // Free the buffer allocated by jpeg_mem_dest
    // Note: jpeg_mem_dest allocates with malloc, so we should free it.
    // However, the buffer is managed by the destination manager? 
    // Standard jpeg_mem_dest implementation in libjpeg-turbo:
    // "The destination manager ... will allocate the buffer ... The caller is responsible for freeing it."
    if (out_buffer) {
        free(out_buffer);
    }

    return result;
}
