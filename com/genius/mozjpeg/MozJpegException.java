package com.genius.mozjpeg;

/**
 * Exception thrown when a MozJPEG encoding error occurs.
 * This wraps native libjpeg errors and JNI-layer issues.
 */
public class MozJpegException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MozJpegException(String message) {
        super(message);
    }

    public MozJpegException(String message, Throwable cause) {
        super(message, cause);
    }
}
