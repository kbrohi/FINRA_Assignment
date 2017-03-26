package com.finra.assignment.fileuploader.repository;

import org.springframework.data.repository.CrudRepository;

import com.finra.assignment.fileuploader.beans.File;

import java.util.List;
/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

/**
 * 
 */
public interface FileRepository extends CrudRepository<File, String> {
    /**
     * Get file meta by their names
     * @param name name of the file
     * @return The list of file metas corresponding to the name
     */
    List<File> findByName(String name);

    /**
     *
     * @param isNewlyAdded Whether the file is newly added or not. Helps when creating the list of newly added files for
     *                     email notification
     * @return The list of file's meta for files that are newly added
     */
    List<File> findByIsNewlyAdded(boolean isNewlyAdded);
}
