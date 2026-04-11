package cn.edu.bupt.tarecruitment.store;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class XmlDataStore {

    private final Path dataFile;
    private final Path uploadsDirectory;

    public XmlDataStore(Path dataFile, Path uploadsDirectory) {
        this.dataFile = dataFile;
        this.uploadsDirectory = uploadsDirectory;
    }

    public synchronized void initializeIfMissing() throws IOException {
        Files.createDirectories(dataFile.getParent());
        Files.createDirectories(uploadsDirectory);

        if (Files.notExists(dataFile)) {
            write(DemoDataFactory.create());
        }
    }

    public synchronized SystemData read() {
        try {
            if (Files.notExists(dataFile)) {
                initializeIfMissing();
            }

            try (InputStream inputStream = Files.newInputStream(dataFile);
                    XMLDecoder decoder =
                            new XMLDecoder(new BufferedInputStream(inputStream))) {
                Object decoded = decoder.readObject();
                if (!(decoded instanceof SystemData data)) {
                    throw new IllegalStateException("Unexpected data file structure.");
                }
                data.ensureCollections();
                return data;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read XML data store.", e);
        }
    }

    public synchronized void write(SystemData data) throws IOException {
        // 非空校验
        if (data == null) {
            throw new IllegalArgumentException("SystemData cannot be null");
        }

        data.ensureCollections();
        Files.createDirectories(dataFile.getParent());
        Path tempFile = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");

        try (OutputStream outputStream = Files.newOutputStream(tempFile);
             XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(outputStream))) {
            encoder.writeObject(data);
        }

        try {
            Files.move(
                    tempFile,
                    dataFile,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ignored) {
            try {
                Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Files.deleteIfExists(tempFile);
                throw e;
            }
        }
    }

    public Path getUploadsDirectory() {
        return uploadsDirectory;
    }
}
