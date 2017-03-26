package com.finra.assignment.fileuploader.services.keywordextractor;

import org.springframework.stereotype.Component;

import com.finra.assignment.fileuploader.beans.File;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

/**
 * Extracts keyword from the file meta data provided.
 */


@Component
public class FileKeyWordAndIdExtractor implements KeywordAndIdExtractor<File>{

    @Override
    public String extractKeywords(File file) {

        return file.getName() + " " + file.getDescription();
    }

    @Override
    public String extractId(File file) {
        return file.getId();
    }
}
