package com.finra.assignment.fileuploader.services.keywordextractor;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

/**
 * Methods to removeDecoratingCharacters phrases for instance, remove dots, brackets, etc
 *
 */
@Component
public class PhraseSplitter {

    private static final String decoratingCharacters = "{}\\(\\)\\[\\]\"\\\\";
    private String decoratingCharRegEx;
    private static final String joinCharacters = "\\.@\\',";
    private String joinCharRegEx;

    /**
     * Create regex used in cleaning a phrase for characters like . , {} or []
     */
    @PostConstruct
    public void afterPropertiesSet() {

        StringBuilder sb = new StringBuilder("[");
        sb.append(decoratingCharacters).append("]");
        decoratingCharRegEx = sb.toString();

        sb = new StringBuilder("[");
        sb.append(joinCharacters).append("]");
        joinCharRegEx = sb.toString();
    }

    /**
     * Clean the phrase, removing unwanted characters an splitting it into words
     * @param phrase The phrase to clean
     * @param preserveAll whether the preserve all words even those have less than three characters
     * @return return a set of words extracted for the phrase
     */
    public Set<String> cleanseAndSplitPhrase(final String phrase, boolean preserveAll) {
        if (phrase == null) {
            return null;
        }
        String cleansedPhrase = phrase.replaceAll(joinCharRegEx, " ");
        cleansedPhrase = cleansedPhrase.replaceAll(decoratingCharRegEx, "");
        cleansedPhrase = cleansedPhrase.trim();

        String[] words = cleansedPhrase.split("\\s");
        Set<String> cleansedWords = new TreeSet<>();

        if (!preserveAll) {
            if(words.length != 0) {
                for(String word : words) {
                    if (word.length() > 2) {
                        cleansedWords.add(word);
                    }
                }
            }
        } else {
            cleansedWords.addAll(Arrays.asList(words));
        }

        return cleansedWords;
    }

//    public void setDecoratingCharacters(String decoratingCharacters) {
//        this.decoratingCharacters = decoratingCharacters;
//    }
//
//    public void setJoinCharacters(String joinCharacters) {
//        this.joinCharacters = joinCharacters;
//    }
}
