package vn.auction.server.dao;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/**
 * Quản lý việc đọc và ghi SystemSnapshot ra file nhị phân.
 * Phụ trách: TV2.
 */
public class SnapshotDao {
    private final Path filePath;

    public SnapshotDao(Path filePath) {
        this.filePath = filePath;
    }

    public synchronized void save(SystemSnapshot snapshot) throws IOException {
        Path parent = filePath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path tempFile = filePath.resolveSibling(filePath.getFileName() + ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(tempFile))) {
            out.writeObject(snapshot);
            out.flush();
        }

        try {
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public synchronized Optional<SystemSnapshot> load() throws IOException, ClassNotFoundException {
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(filePath))) {
            Object obj = in.readObject();
            if (obj instanceof SystemSnapshot snapshot) {
                return Optional.of(snapshot);
            }
            throw new IOException("Định dạng dữ liệu không khớp với SystemSnapshot");
        }
    }
}

