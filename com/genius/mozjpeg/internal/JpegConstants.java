package com.genius.mozjpeg.internal;

/**
 * Constants used across the Java and Native layers.
 */
public final class JpegConstants {

    private JpegConstants() {
        // Prevent instantiation
    }

    // Color Spaces
    public static final int COLOR_SPACE_RGB = 0;
    public static final int COLOR_SPACE_GRAY = 1;

    // Subsampling
    public static final int SUBSAMPLING_444 = 0; // 1x1
    public static final int SUBSAMPLING_422 = 1; // 2x1
    public static final int SUBSAMPLING_420 = 2; // 2x2
    public static final int SUBSAMPLING_GRAY = 3; // Grayscale

    // Defaults
    public static final int DEFAULT_QUALITY = 75;
    public static final boolean DEFAULT_PROGRESSIVE = true;
    public static final boolean DEFAULT_OPTIMIZE_HUFFMAN = true;
}
