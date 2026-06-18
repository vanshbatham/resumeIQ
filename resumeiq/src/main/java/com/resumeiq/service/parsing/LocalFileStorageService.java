package com.resumeiq.service.parsing;

import com.resumeiq.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path baseUploadDir;

    public LocalFileStorageService(@Value("${file.storage.upload-dir}") String uploadDir) {
        this.baseUploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseUploadDir);
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize upload directory", e);
        }
    }

    @Override
    public String store(MultipartFile file, String subdirectory) {
        try {
            Path targetDir = baseUploadDir.resolve(subdirectory).normalize();
            if (!targetDir.startsWith(baseUploadDir)) {
                throw new FileStorageException("Path traversal attempt detected", null);
            }
            Files.createDirectories(targetDir);

            String extension = extractExtension(file.getOriginalFilename());
            String storedName = UUID.randomUUID() + "." + extension;
            Path targetPath = targetDir.resolve(storedName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return subdirectory + "/" + storedName;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file", e);
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path filePath = baseUploadDir.resolve(storagePath).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file: " + storagePath, e);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}