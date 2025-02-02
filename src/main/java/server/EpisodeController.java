package server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/episodes")
public class EpisodeController {

    private final FileHandler fileHandler;

    @Autowired
    public EpisodeController(FileHandler fileHandler) {
        this.fileHandler = fileHandler;
    }

    @GetMapping("/current")
    public ResponseEntity<String> getCurrentEpisode() {
        try {
            String[][] episodes = fileHandler.readEpisodes();
            if (episodes.length > 0) {
                String currentEpisode = "Show: " + episodes[episodes.length - 1][0] + ", Episode: " + episodes[episodes.length - 1][1];
                return ResponseEntity.ok(currentEpisode);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No episode data found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading episode data.");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addEpisode(@RequestParam("showName") String showName,
            @RequestParam("episodeNumber") String episodeNumber) {
        try {
            fileHandler.appendEpisode(showName, episodeNumber);
            return ResponseEntity.status(HttpStatus.CREATED).body("Episode added successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding episode.");
        }
    }
}
