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
    protected String WORD_FREQ_FILE = "wordle_words_freqs_full.txt";
    protected Double FREQ_CUTOFF = 0.01;

    @Getter
    Set<String> allWords;
    @Getter
    Set<String> allAnswers;
    @Getter
    Map<String,Double> wordFreq;

    void init() throws IOException {
        allWords = initWordsFromFile(ALL_WORDS_FILE);
        allAnswers = initWordsFromFile(ANSWER_WORDS_FILE);

        /* Sanity Check*/
        for (String answer : allAnswers) {
            if (!allWords.contains(answer)) {
                throw new RuntimeException(answer + " is not in the set of words for guessing");
            }
            if(!wordFreq.containsKey(answer)){
                throw new RuntimeException(answer + "is not in the word freq information");
            }
        }
    }

    public abstract Set<String> getPossibleAnswers(String guess, String resultPattern);

    public abstract Set<String> getPossibleAnswers(Turn previousTurn);


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

    Map<String,Double> wordFrequencyInEnglish(String fileName) throws IOException {
        //https://github.com/3b1b/videos/blob/master/_2022/wordle/simulations.py#L40-L58
        Map<String,Double> wordFreq = new HashMap<>(allWords.size());
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (InputStream resourceAsStream = classloader.getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            reader.lines().forEach(line ->
            {
                List<String> split = Arrays.asList(line.split(" "));
                double mean = split.subList(split.size() - 5, split.size() - 1).stream().map(Double::parseDouble).reduce(0.0, Double::sum) / 5.0;
                wordFreq.put(split.get(0),mean);
            });
        }

        return Collections.unmodifiableMap(wordFreq);
    }

    protected void removeInfrequentWords() {
        allWords.removeIf(word -> {
            Double freq = wordFreq.get(word);
            return (freq == null || freq < FREQ_CUTOFF);
        });
    }
}
