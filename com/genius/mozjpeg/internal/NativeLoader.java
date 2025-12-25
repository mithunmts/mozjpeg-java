package com.genius.mozjpeg.internal;

import com.genius.mozjpeg.MozJpegException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Handles loading of the native MozJPEG JNI library.
 * Detects OS and architecture to load the correct binary.
 */
public final class NativeLoader {

    private static boolean loaded = false;
    private static final String LIB_NAME_BASE = "mozjpeg_jni";

    private NativeLoader() {
        // Prevent instantiation
    }

    /**
     * Loads the native library. Safe to call multiple times.
     *
     * @throws MozJpegException if loading fails
     */
    public static synchronized void load() {
        if (loaded) {
            return;
        }

        try {
            // 1. Try loading from system path first (for dev environments)
            try {
                System.loadLibrary(LIB_NAME_BASE);
                loaded = true;
                return;
            } catch (UnsatisfiedLinkError e) {
                // Continue to load from resources
            }

            // 2. Determine OS and Arch
            String osName = System.getProperty("os.name").toLowerCase();
            String osArch = System.getProperty("os.arch").toLowerCase();

            String resourcePath = getResourcePath(osName, osArch);
            
            // 3. Extract and load
            File tempFile = extractLibrary(resourcePath);
            System.load(tempFile.getAbsolutePath());
            
            loaded = true;
        } catch (Exception e) {
            throw new MozJpegException("Failed to load native library: " + e.getMessage(), e);
        }
    }

    private static String getResourcePath(String osName, String osArch) {
        String platform;
        String extension;

        if (osName.contains("win")) {
            platform = "windows";
            extension = ".dll";
        } else if (osName.contains("nux") || osName.contains("nix")) {
            platform = "linux";
            extension = ".so";
        } else if (osName.contains("mac")) {
            platform = "macos";
            extension = ".dylib";
        } else {
            throw new MozJpegException("Unsupported OS: " + osName);
        }

        // Normalize arch
        if (osArch.contains("64")) {
            osArch = "x64";
        } else if (osArch.contains("86")) {
            osArch = "x86";
        } else if (osArch.contains("arm") || osArch.contains("aarch")) {
             // Simple check, might need more robust logic for armv7 vs v8
             if (osArch.contains("64")) {
                 osArch = "arm64";
             } else {
                 osArch = "arm32";
             }
        }

        return "/native/" + platform + "/" + osArch + "/" + LIB_NAME_BASE + extension;
    }

    private static File extractLibrary(String resourcePath) throws IOException {
        InputStream in = NativeLoader.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new MozJpegException("Native library not found in resources: " + resourcePath);
        }

        // Create a temp file
        String filename = new File(resourcePath).getName();
        File tempFile = File.createTempFile("mozjpeg_jni_", "_" + filename);
        tempFile.deleteOnExit();

        Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        in.close();

        return tempFile;
    }
}
