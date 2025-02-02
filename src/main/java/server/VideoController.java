package server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "https://video-processor-iota.vercel.app")
@RequestMapping("/video")
public class VideoController {

    private final ImageSplit imageSplit;

    @Autowired
    public VideoController(ImageSplit imageSplit) {
        this.imageSplit = imageSplit;
    }

    @PostMapping("/process")
    public ResponseEntity<byte[]> handleVideoUpload(@RequestParam("video") MultipartFile videoFile) {
        try {
            byte[] zipFile = imageSplit.splitVideoToZip(videoFile);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=frames.zip");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");

            // response
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(zipFile);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error during video processing.".getBytes());
        }
    }
}
