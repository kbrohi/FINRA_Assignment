package com.finra.assignment.fileuploader.repository.redisdictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.collections.DefaultRedisZSet;
import org.springframework.data.redis.support.collections.RedisZSet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

/**
 * Implements Word prefix dictionary for auto completion.
 */
@Component
public class RedisWordCompletionDictionary implements RedisDictionary {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisWordCompletionDictionary.class);

    private static final String END_TOKEN = "*";

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public RedisWordCompletionDictionary(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    private RedisZSet<String> getDictionary(String dictionaryName) {
        return new DefaultRedisZSet<>(DICT + "compl:" + dictionaryName, stringRedisTemplate);
    }

    @Override
    public void addWord(String dictionaryName, final String wordToSave) {
        assert wordToSave != null;
        String word = wordToSave.toLowerCase();

        String prefix;
        //Add all the variants
        for (int i = 1; i < word.length(); i++) {
            prefix = word.substring(0, i);
            getDictionary(dictionaryName).add(prefix, 0);
            LOGGER.debug("Added prefix [{}]", prefix);
        }
        //Add the full word with End prefix to identify full word
        getDictionary(dictionaryName).add(word + END_TOKEN, 0);
        LOGGER.debug("Added word ** [{}]", word);
    }


    @Override
    public List<String> findWords(String dictionaryName, String searchKeyword) {
        return findWords(dictionaryName, searchKeyword, MAX_COUNT);
    }


    @Override
    public List<String> findWords(String dictionaryName, final String searchKeyword, final int max) {

        assert max <= MAX_COUNT;

        String prefix = searchKeyword.toLowerCase();

        List<String> results = new ArrayList<>();

        Long start = getDictionary(dictionaryName).rank(prefix);

        //if start is null then check if this is a complete word and try to find it
        if (start == null) {
            start = getDictionary(dictionaryName).rank(prefix + END_TOKEN);
        }

        LOGGER.info("Rank of prefix [{}] was [{}]", prefix, start);

        if (start != null) {
            while (true) {
                Set<String> rangeRecs = getDictionary(dictionaryName).range(start, start + MAX_TRANS_UNIT - 1);

                start += MAX_TRANS_UNIT;

                if (!rangeRecs.isEmpty()) {
                    for (String entry : rangeRecs) {

                        LOGGER.debug("Processing entry [{}]", entry);
                        //If the entry is shorter than prefix then a new sequence has started
                        //Also if entry doesn't have the same prefix
                        if (entry.length() < prefix.length()
                                || !entry.substring(0, prefix.length()).equals(prefix)
                                || results.size() >= max) {
                            LOGGER.info("Returning at prefix [{}]; end condition satisfied; ", entry);
                            return results;

                        } else if (entry.endsWith(END_TOKEN)) {
                            //Remove the end token before returning
                            String word = entry.substring(0, entry.length() - END_TOKEN.length());
                            results.add(word);
                            LOGGER.debug("Found word [{}]", word);
                        } else {
                            LOGGER.debug("Prefix found [{}]", entry);
                        }
                    }

                } else {
                    LOGGER.info("Returning as no more to read");
                    return results;
                }
            }
        }
        return results;
    }

//    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
//        this.stringRedisTemplate = stringRedisTemplate;
//    }

}
