package com.genius.mozjpeg;

import com.genius.mozjpeg.internal.JpegConstants;
import com.genius.mozjpeg.profile.JpegProfile;

/**
 * High-level wrapper for the MozJPEG encoder.
 * Provides a safe, object-oriented API for encoding images.
 */
public class MozJpegEncoder implements AutoCloseable {

    private long nativeHandle;
    private boolean closed = false;

    // Configuration state
    private int quality = JpegConstants.DEFAULT_QUALITY;
    private boolean progressive = JpegConstants.DEFAULT_PROGRESSIVE;
    private boolean optimizeHuffman = JpegConstants.DEFAULT_OPTIMIZE_HUFFMAN;
    private boolean trellis = false;
    private boolean dering = false;
    private int subsampling = JpegConstants.SUBSAMPLING_420;

    /**
     * Creates a new MozJpegEncoder instance.
     * Initializes the native encoder context.
     */
    public MozJpegEncoder() {
        this.nativeHandle = MozJpeg.initEncoder();
    }

    /**
     * Encodes the given raw RGB bytes to JPEG using the current settings.
     *
     * @param rawRgbBytes The raw pixel data (RGB, 3 bytes per pixel).
     * @param width       Image width.
     * @param height      Image height.
     * @return The encoded JPEG bytes.
     */
    public byte[] encode(byte[] rawRgbBytes, int width, int height) {
        checkClosed();
        return MozJpeg.encode(
            nativeHandle,
            rawRgbBytes,
            width,
            height,
            JpegConstants.COLOR_SPACE_RGB,
            quality,
            progressive,
            optimizeHuffman,
            trellis,
            dering,
            subsampling
        );
    }

    /**
     * Encodes the given raw bytes to JPEG using a specific profile.
     * This overrides the current settings of the encoder for this operation.
     *
     * @param rawBytes The raw pixel data.
     * @param width    Image width.
     * @param height   Image height.
     * @param profile  The compression profile to use.
     * @return The encoded JPEG bytes.
     */
    public byte[] encode(byte[] rawBytes, int width, int height, JpegProfile profile) {
        checkClosed();
        
        // Determine color space based on profile or input assumption
        // For simplicity, we assume RGB unless the profile implies otherwise or user handles it.
        // However, the profile itself dictates subsampling, not input color space.
        // We will assume input is RGB for standard profiles, but if the user wants Grayscale input,
        // they should probably use a specific method or we need to infer it.
        // For this API, let's assume the input matches the profile's intent or is RGB.
        // If profile is GRAYSCALE_TEXT, we might expect 1 byte per pixel if we were strict,
        // but usually we convert RGB to Gray.
        // The native layer takes 'colorSpace' which describes the INPUT.
        // Let's add an overload or assume RGB input for now.
        
        int inputColorSpace = JpegConstants.COLOR_SPACE_RGB;
        if (profile == JpegProfile.GRAYSCALE_TEXT) {
             // If the profile is grayscale, the output will be grayscale.
             // But the input might still be RGB.
             // If the user passes 1-byte-per-pixel data, they need to tell us.
             // For safety, let's stick to RGB input default for this helper.
        }

        return MozJpeg.encode(
            nativeHandle,
            rawBytes,
            width,
            height,
            inputColorSpace,
            profile.getQuality(),
            profile.isProgressive(),
            profile.isOptimizeHuffman(),
            profile.isTrellis(),
            profile.isDering(),
            profile.getSubsampling()
        );
    }
    
    /**
     * Encodes raw grayscale bytes (1 byte per pixel).
     */
    public byte[] encodeGrayscale(byte[] rawGrayBytes, int width, int height) {
        checkClosed();
        return MozJpeg.encode(
            nativeHandle,
            rawGrayBytes,
            width,
            height,
            JpegConstants.COLOR_SPACE_GRAY,
            quality,
            progressive,
            optimizeHuffman,
            trellis,
            dering,
            JpegConstants.SUBSAMPLING_GRAY // Force gray subsampling for gray input
        );
    }

    public void setQuality(int quality) {
        if (quality < 0 || quality > 100) {
            throw new IllegalArgumentException("Quality must be between 0 and 100");
        }
        this.quality = quality;
    }

    public void setProgressive(boolean progressive) {
        this.progressive = progressive;
    }

    public void setOptimizeHuffman(boolean optimizeHuffman) {
        this.optimizeHuffman = optimizeHuffman;
    }

    public void setTrellisQuant(boolean trellis) {
        this.trellis = trellis;
    }

    public void setOvershootDeringing(boolean dering) {
        this.dering = dering;
    }

    /**
     * Sets chroma subsampling.
     * @param subsamplingStr "4:4:4", "4:2:2", "4:2:0", or "Gray"
     */
    public void setChromaSubsampling(String subsamplingStr) {
        switch (subsamplingStr) {
            case "4:4:4": this.subsampling = JpegConstants.SUBSAMPLING_444; break;
            case "4:2:2": this.subsampling = JpegConstants.SUBSAMPLING_422; break;
            case "4:2:0": this.subsampling = JpegConstants.SUBSAMPLING_420; break;
            case "Gray":  this.subsampling = JpegConstants.SUBSAMPLING_GRAY; break;
            default: throw new IllegalArgumentException("Unknown subsampling: " + subsamplingStr);
        }
    }

    private void checkClosed() {
        if (closed) {
            throw new IllegalStateException("Encoder is closed");
        }
    }

    @Override
    public synchronized void close() {
        if (!closed) {
            MozJpeg.destroyEncoder(nativeHandle);
            closed = true;
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
