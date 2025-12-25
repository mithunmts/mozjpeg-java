import com.genius.mozjpeg.MozJpegEncoder;
import com.genius.mozjpeg.profile.JpegProfile;
import java.io.FileOutputStream;
import java.util.Arrays;

public class ExampleUsage {

    public static void main(String[] args) {
        System.out.println("MozJPEG JNI Example");

        int width = 1024;
        int height = 768;
        byte[] rawRgb = new byte[width * height * 3];
        
        // Generate a dummy gradient image
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int idx = (i * width + j) * 3;
                rawRgb[idx] = (byte) (i % 256);     // R
                rawRgb[idx + 1] = (byte) (j % 256); // G
                rawRgb[idx + 2] = (byte) 128;       // B
            }
        }

        try (MozJpegEncoder encoder = new MozJpegEncoder()) {
            System.out.println("Encoding image...");
            System.out.println("Input size (Raw RGB): " + rawRgb.length + " bytes");
            
            long start = System.currentTimeMillis();
            byte[] jpeg = encoder.encode(rawRgb, width, height, JpegProfile.SCAN_DOCUMENT);
            long end = System.currentTimeMillis();
            
            System.out.println("Output size (JPEG):   " + jpeg.length + " bytes");
            System.out.println("Time taken:           " + (end - start) + "ms");
            
            // Save the output
            try (FileOutputStream fos = new FileOutputStream("test_output.jpg")) {
                 fos.write(jpeg);
                 System.out.println("Saved to test_output.jpg");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
