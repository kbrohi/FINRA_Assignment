package com.finra.assignment.fileuploader.repository.redisdictionary;

import java.util.List;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

/**
 * Defines interface for dictionaries.
 */
public interface RedisDictionary {

    String DICT = "dict:";

    String DEFAULT_DICTIONARY = "dflt";
    int MAX_COUNT = 50;
    int MAX_TRANS_UNIT = 50;

    /**
     * Adds a word to the dictionary.
     *
     * @param dictionaryName name of the dictionary of index
     * @param wordToSave word to save
     */
    void addWord(String dictionaryName, String wordToSave);

    /**
     * Find words which relate to the provided keyword.
     *
     * @param dictionaryName name of the dictionary or index
     * @param searchKeyword the search keyword
     * @return list of keys representing the result of the search
     */
    List<String> findWords(String dictionaryName, String searchKeyword);

    /**
     * Find max number of words which relate to the provided keyword.
     *
     * @param dictionaryName name of the dictionary or index
     * @param searchKeyword the search keyword
     * @param max the max number results
     * @return List of keys representing the result of the search
     */
    List<String> findWords(String dictionaryName, String searchKeyword, int max);
}
