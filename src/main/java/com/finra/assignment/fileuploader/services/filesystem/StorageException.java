package com.finra.assignment.fileuploader.services.filesystem;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

public class StorageException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	StorageException(String message) {
        super(message);
    }

    StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
