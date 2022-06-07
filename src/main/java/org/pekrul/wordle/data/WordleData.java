package org.pekrul.wordle.data;

import lombok.Getter;
import org.pekrul.wordle.Turn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public abstract class WordleData {

    private String ALL_WORDS_FILE = "allowed_words.txt";
    private String ANSWER_WORDS_FILE = "possible_words.txt";

    //    protected Set<String> allWords;
    @Getter
    protected Set<Integer> allAnswers;
    @Getter
    protected Map<String, Integer> allWordsToIds;
    @Getter
    protected Map<Integer,String> allIdsToWords;
    //look into a BiMap? https://github.com/google/guava/wiki/NewCollectionTypesExplained#bimap


    void init() throws IOException {
        allWordsToIds = initAllWords(ALL_WORDS_FILE);
        allIdsToWords = initAllWordsReverse(allWordsToIds);
        allAnswers = convertWordsToIds(initWordsFromFile(ANSWER_WORDS_FILE));

        /* Sanity Check*/
        /*for (Integer answer : allAnswers) {
            if (!allWordsToIds.containsKey(answer)) {
                throw new RuntimeException(answer + " is not in the set of words for guessing");
            }
        }*/
    }

    public Integer getAllWordsSize(){
        return allIdsToWords.size();
    }

    public int getAllAnswerSize(){
        return allAnswers.size();
    }

    public abstract Set<Integer> getPossibleAnswerIds(String guess, String resultPattern);

    public abstract Set<Integer> getPossibleAnswerIds(Turn previousTurn);

    public String getWord(int wordId){
        return allIdsToWords.get(wordId);
    }


    Set<String> initWordsFromFile(String fileName) throws IOException {
        Set<String> words = new HashSet<>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (InputStream resourceAsStream = classloader.getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            reader.lines().forEach(line ->
            {
                words.add(line.toUpperCase(Locale.ROOT));
            });
        }

        return Collections.unmodifiableSet(words);
    }

    Set<Integer> convertWordsToIds(Set<String> input){
        Set<Integer> result = new HashSet<>(input.size());
        input.forEach(v -> result.add(allWordsToIds.get(v)));
        return result;
    }

    Map<String, Integer> initAllWords(String fileName) throws IOException {
        Map<String, Integer> words = new HashMap<>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        int wordId = 0;
        try (InputStream resourceAsStream = classloader.getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                words.put(line.toUpperCase(Locale.ROOT), wordId);
                wordId = wordId + 1;
            }
        }
        return Collections.unmodifiableMap(words);
    }

    Map<Integer,String> initAllWordsReverse(Map<String,Integer> input){
        Map<Integer,String> reversed = new HashMap<>(input.size());
        input.forEach((k,v) -> reversed.put(v,k));
        return reversed;
    }


}
