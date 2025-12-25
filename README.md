# MozJPEG JNI Wrapper for Java

A high-performance, production-grade JNI wrapper for MozJPEG v4.1.5. This library provides a Java interface to the MozJPEG encoder, offering superior compression ratios compared to standard JPEG encoders.

## Features

- **Production Grade**: Statically linked binaries for **Windows (x64/x86)** and **Linux (x64/x86)**. No external dependencies required on the target system.
- **High Compression**: Access to MozJPEG's advanced features like trellis quantization, scan optimization, and overshoot deringing.
- **Safe**: RAII-based C++ wrapper with robust error handling mapped to Java exceptions.
- **Fast**: Minimal buffer copying and reused encoder contexts for high-throughput applications.
- **Java 8 Compatible**: Built for Java 1.8+ compatibility.

## Supported Platforms

The generated JAR includes native binaries for:
- **Windows**: x64 (64-bit) and x86 (32-bit)
- **Linux**: x64 (64-bit) and x86 (32-bit)

## Prerequisites for Building

To build the project from source, you need the following tools installed:

### General
- **Java JDK 8+**:
    - For x64 builds: A 64-bit JDK.
    - For x86 builds: A 32-bit JDK is required to link JNI correctly.
- **CMake 3.10+**: Build system generator.
- **NASM**: Required for MozJPEG SIMD optimizations.
- **Git**: For cloning the MozJPEG repository.

### Windows
- **Visual Studio 2019/2022**: With C++ desktop development workload.

### Linux
- **GCC/G++**: C/C++ compiler.
- **Multilib support** (for x86 builds):
  ```bash
  sudo apt-get install gcc-multilib g++-multilib
  ```

## Build Instructions

### Windows (Full Build)

The easiest way to build on Windows is to use the master script, which builds native libraries (x64 & x86) and then packages the Java JAR.

```bat
build_all.bat
```

**Artifacts produced:**
- `build/jar/mozjpeg-java-8.jar`: The final library containing all native binaries.
- `src/main/resources/native/windows/`: Generated DLLs.

### Linux (Native Build)

To build the Linux shared objects (`.so`), use the provided shell script. This is useful if you are developing on Linux or using WSL (Windows Subsystem for Linux) to generate the Linux artifacts for the Windows build.

```bash
./build_native.sh
```

This script will:
1.  Download and build MozJPEG statically (with `-fPIC`).
2.  Build the JNI wrapper linked against the static MozJPEG library.
3.  Output artifacts to `src/main/resources/native/linux/`.

### Java Only

If native libraries are already present in `src/main/resources`, you can build just the Java JAR:

```bat
build_java.bat
```

## Maven Integration

This project includes a `pom.xml` for Maven integration.

### Install Locally
To install the library into your local Maven repository:

```powershell
mvn install
```

### Dependency Usage
Once installed, add it to your project:

```xml
<dependency>
    <groupId>com.genius</groupId>
    <artifactId>mozjpeg-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage Example

### Basic Encoding

```java
import com.genius.mozjpeg.MozJpegEncoder;
import com.genius.mozjpeg.profile.JpegProfile;

public class Example {
    public void compressImage(byte[] rawRgb, int width, int height) {
        // Initialize encoder (try-with-resources ensures cleanup)
        try (MozJpegEncoder encoder = new MozJpegEncoder()) {
            
            // Encode RGB data using the 'Document' profile
            byte[] jpegBytes = encoder.encode(
                rawRgb, 
                width, 
                height, 
                JpegProfile.SCAN_DOCUMENT
            );
            
            // Use jpegBytes...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Custom Configuration

```java
try (MozJpegEncoder encoder = new MozJpegEncoder()) {
    // Manual configuration
    encoder.setQuality(85);
    encoder.setChromaSubsampling(MozJpegEncoder.SAMPLING_420);
    encoder.setTrellisQuant(true);
    encoder.setOvershootDeringing(true);
    encoder.setOptimizeScans(true);

    byte[] result = encoder.encode(rawRgbData, width, height);
}
```

## Project Structure

- `com/genius/mozjpeg/`: Java source code.
- `native/mozjpeg/`: C++ JNI wrapper source code.
- `mozjpeg-build/`: Working directory for downloading and building MozJPEG.
- `src/main/resources/native/`: Location of compiled native libraries (DLL/SO).

1. **Reuse Encoders**: Creating a `MozJpegEncoder` allocates native resources. Reuse the instance for batch processing.
2. **Thread Safety**: `MozJpegEncoder` is **not** thread-safe. Use one instance per thread or synchronize access.
3. **Input Data**: Ensure input byte arrays are exactly `width * height * 3` (RGB) or `width * height * 1` (Gray).

## License

Free for anything.
```
