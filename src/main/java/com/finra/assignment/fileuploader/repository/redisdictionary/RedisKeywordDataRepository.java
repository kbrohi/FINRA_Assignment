package com.finra.assignment.fileuploader.repository.redisdictionary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.data.redis.support.collections.DefaultRedisSet;
import org.springframework.stereotype.Component;

import com.finra.assignment.fileuploader.services.keywordextractor.KeywordAndIdExtractor;
import com.finra.assignment.fileuploader.services.keywordextractor.PhraseSplitter;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * @author Kaleemullah Brohi
 * @Email kaleem.brohi27@gmail.com
 * @Contact 571-320-7965
 */

/**
 * Allows operations to retrieve data based on keywords
 */
@Component
public class RedisKeywordDataRepository implements SearchableKeywordDataRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisKeywordDataRepository.class);

    private static final String TEMP_SET_KEY = "global:tmpId";
    private static final String TEMP_PREFIX = "tmp:";

    private final StringRedisTemplate stringRedisTemplate;

    private final PhraseSplitter phraseSplitter;

    private RedisDictionary redisDictionary;

    private RedisAtomicLong tempSetKeyCounter;

    @Autowired
    public RedisKeywordDataRepository(List<RedisDictionary> redisDictionaries, PhraseSplitter phraseSplitter,
                                      StringRedisTemplate stringRedisTemplate) {

        redisDictionary = new AggregatingRedisDictionary(redisDictionaries);
        this.phraseSplitter = phraseSplitter;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostConstruct
    public void init() {
        tempSetKeyCounter = new RedisAtomicLong(TEMP_SET_KEY, stringRedisTemplate.getConnectionFactory());
    }

    private String getKeyForKeyword(String type, String keyword) {
        return "index:" + type + ":" + keyword;
    }

    private Set<String> getSetByKeyword(String type, String keyword) {
        return new DefaultRedisSet<>(getKeyForKeyword(type, keyword), stringRedisTemplate);
    }

    @Override
    public <T> void indexDataByKeywords(String dictionaryName, KeywordAndIdExtractor<T> keywordAndIdExtractor, T data) {
        String keywordPhrase = keywordAndIdExtractor.extractKeywords(data);
        Set<String> keywords = phraseSplitter.cleanseAndSplitPhrase(keywordPhrase, false);

        if (keywords != null) {
            for(String keyword : keywords) {
                getSetByKeyword(dictionaryName, keyword).add(keywordAndIdExtractor.extractId(data));

                redisDictionary.addWord(dictionaryName, keyword);
            }
        }

    }

    @Override
    public List<String> findDataByPhrase(String dictionaryName, String phrase) {
        return findDataByKeywordPrefixes(dictionaryName, phraseSplitter.cleanseAndSplitPhrase(phrase, true));
    }

    private List<String> findDataByKeywordPrefixes(String dictionaryName, Set<String> keywords) {
        List<String> resultDataIds = new ArrayList<>();

        if(keywords != null) {
            //temp redis key prefix to be used to store results for aggregation
            StringBuilder tmpKeyPrefix = new StringBuilder(TEMP_PREFIX)
                    .append(tempSetKeyCounter.incrementAndGet())
                    .append(":");

            int tmpResultSetCount = 0;
            List<String> tmpResultSetAllKeys = new ArrayList<>();
            for (String keyword : keywords) {
                String tmpResultSetKey = tmpKeyPrefix.append(tmpResultSetCount++).toString();

                Long resultCount = findDataByKeyword(dictionaryName, tmpResultSetKey, keyword);
                if (resultCount != null && resultCount > 0) {
                    tmpResultSetAllKeys.add(tmpResultSetKey);
                }
            }

            if(tmpResultSetAllKeys.size() == 1) {
                resultDataIds.addAll(stringRedisTemplate.opsForSet().members(tmpResultSetAllKeys.get(0)));
            } else if(tmpResultSetAllKeys.size() > 1) {
                String firstKey = tmpResultSetAllKeys.remove(0);
                resultDataIds.addAll(stringRedisTemplate.opsForSet().intersect(firstKey, tmpResultSetAllKeys));

                //Add the first key back so its data set can be deleted later
                tmpResultSetAllKeys.add(firstKey);
            } else {
                LOGGER.debug("No results found");
            }

            LOGGER.debug("Deleting tmp keys. Ids of the result were [{}]", resultDataIds);
            //clean up tmp sets
            tmpResultSetAllKeys.stream().filter(key -> key.startsWith(tmpKeyPrefix.toString()))
                    .forEach(stringRedisTemplate::delete);

        }

        return resultDataIds;
    }

    private Long findDataByKeyword(String dictionaryName, String tmpResultSetKey, String keyword) {
        LOGGER.debug("Finding data for keyword prefix [{}]", keyword);

        List<String> resultWords = redisDictionary.findWords(dictionaryName, keyword, RedisDictionary.MAX_COUNT);
        List<String> resultWordsSetKeys = buildSetKeys(dictionaryName, resultWords);

        if(resultWords.isEmpty()) {
            LOGGER.info("No results found for [{}]", keyword);
            return null;
        } else{
            //Join result from all words found into tmp set

            LOGGER.debug("Words found for prefix [{}]; [{}], tmpResultSetKey [{}]", keyword, resultWords, tmpResultSetKey);
            String firstKey = resultWordsSetKeys.remove(0);
            return stringRedisTemplate.opsForSet().unionAndStore(firstKey, resultWordsSetKeys, tmpResultSetKey);
        }
    }

    private List<String> buildSetKeys(String dictionaryName, List<String> resultWords) {
        List<String> resultWordsSetKeys = new ArrayList<>();

        if(!resultWords.isEmpty()) {
            for(String resultWord : resultWords) {
                resultWordsSetKeys.add(getKeyForKeyword(dictionaryName, resultWord));
            }
        }
        return resultWordsSetKeys;
    }

    private static class AggregatingRedisDictionary implements RedisDictionary {

        private List<RedisDictionary> redisDictionaries;

        AggregatingRedisDictionary(List<RedisDictionary> redisDictionaries) {
            assert redisDictionaries != null;
            this.redisDictionaries = redisDictionaries;
        }

        @Override
        public void addWord(String dictionaryName, String wordToSave) {
            for(RedisDictionary redisDictionary : redisDictionaries) {
                redisDictionary.addWord(dictionaryName, wordToSave);
            }
        }

        @Override
        public List<String> findWords(String dictionaryName, String searchKeyword) {
            List<String> results = new ArrayList<>();

            for(RedisDictionary redisDictionary : redisDictionaries) {
                results.addAll(redisDictionary.findWords(dictionaryName, searchKeyword));
            }

            return results;
        }

        @Override
        public List<String> findWords(String dictionaryName, String searchKeyword, int max) {
            List<String> results = new ArrayList<>();

            for(RedisDictionary redisDictionary : redisDictionaries) {
                results.addAll(redisDictionary.findWords(dictionaryName, searchKeyword, max));
            }

            return results;
        }
    }

//    public void setStringRedisTemplate(RedisTemplate stringRedisTemplate) {
//        this.stringRedisTemplate = stringRedisTemplate;
//    }
//
//    public void setPhraseSplitter(PhraseSplitter phraseSplitter) {
//        this.phraseSplitter = phraseSplitter;
//    }
}
