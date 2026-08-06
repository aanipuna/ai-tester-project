package com.dialog.dtg.core.store;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class FileLockManager {

    public LockedHandle lock(Path targetFile, Duration timeout) throws IOException {
        Path parent = targetFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path lockFile = targetFile.resolveSibling(targetFile.getFileName().toString() + ".lck");
        RandomAccessFile raf = new RandomAccessFile(lockFile.toFile(), "rw");
        FileChannel channel = raf.getChannel();

        Instant deadline = Instant.now().plus(timeout);
        while (Instant.now().isBefore(deadline)) {
            FileLock lock = channel.tryLock();
            if (lock != null) {
                return new LockedHandle(raf, channel, lock);
            }
            sleep(50);
        }

        closeQuietly(raf, channel);
        throw new IOException("Could not acquire lock for file: " + targetFile);
    }

    private void sleep(long millis) throws IOException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for file lock", ex);
        }
    }

    private void closeQuietly(RandomAccessFile raf, FileChannel channel) {
        try {
            channel.close();
        } catch (IOException ignored) {
        }
        try {
            raf.close();
        } catch (IOException ignored) {
        }
    }

    public static final class LockedHandle implements AutoCloseable {
        private final RandomAccessFile raf;
        private final FileChannel channel;
        private final FileLock lock;

        public LockedHandle(RandomAccessFile raf, FileChannel channel, FileLock lock) {
            this.raf = raf;
            this.channel = channel;
            this.lock = lock;
        }

        @Override
        public void close() throws IOException {
            try {
                if (lock != null && lock.isValid()) {
                    lock.release();
                }
            } finally {
                channel.close();
                raf.close();
            }
        }
    }
}
