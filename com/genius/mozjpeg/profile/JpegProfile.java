package com.genius.mozjpeg.profile;

import com.genius.mozjpeg.internal.JpegConstants;

/**
 * Pre-defined compression profiles for common use cases.
 */
public enum JpegProfile {

    /**
     * Aggressive compression for scanned documents.
     * High readability, small file size.
     * Quality: 60, Subsampling: 4:2:0, Progressive: True
     */
    SCAN_DOCUMENT(60, true, true, true, true, JpegConstants.SUBSAMPLING_420),

    /**
     * Optimized for grayscale text documents.
     * Quality: 70, Subsampling: Gray, Progressive: True
     */
    GRAYSCALE_TEXT(70, true, true, true, true, JpegConstants.SUBSAMPLING_GRAY),

    /**
     * High quality for color photographs.
     * Quality: 85, Subsampling: 4:4:4, Progressive: True
     */
    COLOR_PHOTO(85, true, true, true, true, JpegConstants.SUBSAMPLING_444),

    /**
     * Near-lossless quality for archiving.
     * Quality: 95, Subsampling: 4:4:4, Progressive: False
     */
    LOSSLESS_NEAR(95, false, true, false, false, JpegConstants.SUBSAMPLING_444);

    private final int quality;
    private final boolean progressive;
    private final boolean optimizeHuffman;
    private final boolean trellis;
    private final boolean dering;
    private final int subsampling;

    JpegProfile(int quality, boolean progressive, boolean optimizeHuffman, boolean trellis, boolean dering, int subsampling) {
        this.quality = quality;
        this.progressive = progressive;
        this.optimizeHuffman = optimizeHuffman;
        this.trellis = trellis;
        this.dering = dering;
        this.subsampling = subsampling;
    }

    public int getQuality() {
        return quality;
    }

    public boolean isProgressive() {
        return progressive;
    }

    public boolean isOptimizeHuffman() {
        return optimizeHuffman;
    }

    public boolean isTrellis() {
        return trellis;
    }

    public boolean isDering() {
        return dering;
    }

    public int getSubsampling() {
        return subsampling;
    }
}
