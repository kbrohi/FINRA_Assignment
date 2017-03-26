package com.finra.assignment.fileuploader.repository.redisdictionary;

import java.util.List;

import com.finra.assignment.fileuploader.services.keywordextractor.KeywordAndIdExtractor;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

/**
 * Defines a repository which indexes data to enable search capabilities.
 */
public interface SearchableKeywordDataRepository {


    /**
     * Index the data in the dictionary.
     *
     * @param dictionaryName used to store correlated data together.
     * @param keywordAndIdExtractor allows to extract id and keywords from data provided.
     * @param data data to be indexed.
     * @param <T> type of the data
     */
    <T> void indexDataByKeywords(String dictionaryName, KeywordAndIdExtractor<T> keywordAndIdExtractor, T data);

    /**
     * Find ids of data which relate to the phrase provided in the dictionary.
     *
     * @param dictionaryName name of the dictionary or the index
     * @param phrase the search phrase
     * @return a list of ids representing the search result
     */
    List<String> findDataByPhrase(String dictionaryName, String phrase);

}