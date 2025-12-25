package com.genius.mozjpeg;

import com.genius.mozjpeg.internal.NativeLoader;

/**
 * Low-level JNI interface to MozJPEG.
 * 
 * <p>This class manages the native encoder lifecycle.
 * It is recommended to use {@link MozJpegEncoder} for a higher-level API.
 * </p>
 */
public final class MozJpeg {

    static {
        NativeLoader.load();
    }

    private MozJpeg() {
        // Prevent instantiation
    }

    /**
     * Initializes a new native encoder context.
     *
     * @return A pointer (handle) to the native encoder structure.
     * @throws MozJpegException if initialization fails.
     */
    public static native long initEncoder();

    /**
     * Destroys the native encoder context and frees memory.
     *
     * @param ctx The native handle returned by {@link #initEncoder()}.
     */
    public static native void destroyEncoder(long ctx);

    /**
     * Encodes raw image data to JPEG.
     *
     * @param ctx             The native encoder handle.
     * @param rawImage        The raw pixel data (RGB or Grayscale).
     * @param width           Image width in pixels.
     * @param height          Image height in pixels.
     * @param colorSpace      0 for RGB, 1 for Grayscale.
     * @param quality         Compression quality (0-100).
     * @param progressive     True for progressive JPEG, false for baseline.
     * @param optimizeHuffman True to enable Huffman table optimization.
     * @param trellis         True to enable trellis quantization.
     * @param dering          True to enable overshoot deringing.
     * @param subsampling     Chroma subsampling: 0=4:4:4, 1=4:2:2, 2=4:2:0, 3=Gray.
     * @return The encoded JPEG bytes.
     * @throws MozJpegException if encoding fails.
     */
    public static native byte[] encode(
        long ctx,
        byte[] rawImage,
        int width,
        int height,
        int colorSpace,
        int quality,
        boolean progressive,
        boolean optimizeHuffman,
        boolean trellis,
        boolean dering,
        int subsampling
    );
}
