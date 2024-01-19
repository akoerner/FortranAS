import java.io.*;
import java.nio.file.*;

public class TemporaryFile implements AutoCloseable {
    private final Path filePath;

    public TemporaryFile(String file) throws IOException {
        this.filePath = createTemporaryFile(file);
    }

    private Path createTemporaryFile(String file) throws IOException {
        Path sourceFilePath = Paths.get(file);

        Path tempFilePath = Files.createTempFile("temp_", ".tmp");

        Files.copy(sourceFilePath, tempFilePath, StandardCopyOption.REPLACE_EXISTING);

        return tempFilePath;
    }

    public File getFile() {
        return filePath.toFile();
    }

    public Path getFilePath() {
        return filePath;
    }

    @Override
    public void close() {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

