package server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FileHandler {

    private static final String FILE_NAME = "episodes.txt";

    public FileHandler() {
        try {
            Files.createFile(Paths.get(FILE_NAME));
        } catch (IOException E){
            System.err.println("Error creating the file: " + E.getMessage());
        }
    }
    
    public String[][] readEpisodes() throws IOException {
        if (!Files.exists(Paths.get(FILE_NAME))) {
            Files.createFile(Paths.get(FILE_NAME));
        }

        List<String> lines = Files.readAllLines(Paths.get(FILE_NAME));
        String[][] episodes = new String[lines.size()][2];

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(", "); // Split by comma and space
            episodes[i][0] = parts[0].split(": ")[1]; // Show name
            episodes[i][1] = parts[1].split(": ")[1]; // Episode number
        }

        return episodes;
    }

    public void appendEpisode(String showName, String episodeNumber) throws IOException {
        String episodeContent = "Show: " + showName + ", Episode: " + episodeNumber;
        Files.write(Paths.get(FILE_NAME), (episodeContent + "\n").getBytes(), java.nio.file.StandardOpenOption.APPEND);
    }
}
