package com.finra.assignment.fileuploader.services.filesystem;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

/**
 * Service to store files to the file system
 */
@Service
public class FileSystemStorageService {

    @Value("${file-system.directory}")
    private String uploadDirectory;

    private Path uploadDirectoryPath;

    public FileSystemStorageService() {
        this.uploadDirectoryPath = Paths.get("files-uploaded");

        if(!uploadDirectoryPath.toFile().exists()){
            try {
                Files.createDirectory(uploadDirectoryPath);
            } catch (IOException e) {
                throw new StorageException("Could not initialize storage", e);
            }
        }

    }

    /**
     * Save a multipart file to the file system
     * @param file The multipart file to be saved
     */
    public void saveFile(MultipartFile file, String name){
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + file.getOriginalFilename());
            }
            Files.copy(file.getInputStream(), this.uploadDirectoryPath.resolve(name));

        } catch (IOException e) {
            throw new StorageException("Failed to store file " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Get the path of a file stored in the file system withing the app context
     * @param filename The file's name
     * @return The a Path object representing the path to the file
     */
    public Path load(String filename) {
        return uploadDirectoryPath.resolve(filename);
    }

    /**
     * Get a file stored within the context of the app as a resource. used to sending requested files to client apps
     * @param filename The filename
     * @return The file as a resource
     */
    public Resource getFileAsResource(String filename){
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if(resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }
}
