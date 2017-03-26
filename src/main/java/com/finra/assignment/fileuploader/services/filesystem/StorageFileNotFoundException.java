package com.finra.assignment.fileuploader.services.filesystem;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

public class StorageFileNotFoundException extends StorageException {

    StorageFileNotFoundException(String message) {
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}