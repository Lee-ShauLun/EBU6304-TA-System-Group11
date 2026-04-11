package cn.edu.bupt.tarecruitment.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppConfig {

    private static final String DEFAULT_PORT = "8080";
    private static final String DATA_FOLDER = "data";

    private final int port;
    private final Path rootDirectory;
    private final Path dataFile;
    private final Path uploadsDirectory;

    private AppConfig(int port, Path rootDirectory, Path dataFile, Path uploadsDirectory) {
        this.port = port;
        this.rootDirectory = rootDirectory;
        this.dataFile = dataFile;
        this.uploadsDirectory = uploadsDirectory;
    }

    public static AppConfig load() {
        Path root = Paths.get("").toAbsolutePath().normalize();
        String configuredPort = System.getProperty("trs.port", System.getenv().getOrDefault("TRS_PORT", DEFAULT_PORT));

        int port;
        try {
            port = Integer.parseInt(configuredPort.trim());
        } catch (NumberFormatException ignored) {
            port = Integer.parseInt(DEFAULT_PORT);
        }

        Path dataDirectory = root.resolve(DATA_FOLDER);
        return new AppConfig(
                port,
                root,
                dataDirectory.resolve("trs-data.xml"),
                dataDirectory.resolve("uploads"));
    }

    public int getPort() {
        return port;
    }

    public Path getRootDirectory() {
        return rootDirectory;
    }

    public Path getDataFile() {
        return dataFile;
    }

    public Path getUploadsDirectory() {
        return uploadsDirectory;
    }
}
