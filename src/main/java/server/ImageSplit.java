package server;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Frame;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ImageSplit {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + File.separator + "video-uploads";

    public byte[] splitVideoToZip(MultipartFile videoFile) throws IOException, InterruptedException {
        // check dir
        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new IOException("Failed to create temp directory: " + TEMP_DIR);
        }

        File videoTempFile = new File(tempDir, videoFile.getOriginalFilename());
        videoFile.transferTo(videoTempFile);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
             FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoTempFile)) {

            grabber.start();
            Java2DFrameConverter converter = new Java2DFrameConverter();
            Frame frame;
            int frameIndex = 0;

            // get frames and add to the ZIP
            while ((frame = grabber.grabImage()) != null) {
                BufferedImage img = converter.convert(frame);

                String imageFileName = String.format("frame_%04d.jpg", frameIndex++);
                ZipEntry zipEntry = new ZipEntry(imageFileName);
                zipOutputStream.putNextEntry(zipEntry);

                try (ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream()) {
                    ImageIO.write(img, "jpg", imageOutputStream);
                    zipOutputStream.write(imageOutputStream.toByteArray());
                }

                zipOutputStream.closeEntry();
            }

            grabber.stop();
        } finally {
            // remove file when done
            if (!videoTempFile.delete()) {
                System.err.println("Failed to delete temporary file: " + videoTempFile.getAbsolutePath());
            }
        }

        return byteArrayOutputStream.toByteArray();
    }
}
