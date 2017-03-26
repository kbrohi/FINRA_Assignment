package com.finra.assignment.fileuploader.services.keywordextractor;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

/**
 * Extracts keyword from the data provided.
 * @param <T> type of the data
 */
public interface KeywordAndIdExtractor<T> {

    /**
     * Extracts all the keyword in a sentence.
     *
     * @param data
     * @return
     */
    String extractKeywords(T data);

    /**
     * Extracts id from the data that will be used for indexing.
     *
     * @param data
     * @return
     */
    String extractId(T data);
}
