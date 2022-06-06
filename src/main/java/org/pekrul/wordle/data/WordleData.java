package org.pekrul.wordle.data;

import lombok.Getter;
import org.pekrul.wordle.Turn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public abstract class WordleData {

    private String ALL_WORDS_FILE = "allowed_words.txt";
    private String ANSWER_WORDS_FILE = "possible_words.txt";

    @Getter
    Set<String> allWords;
    @Getter
    Set<String> allAnswers;

    void init() throws IOException {
        allWords = initWordsFromFile(ALL_WORDS_FILE);
        allAnswers = initWordsFromFile(ANSWER_WORDS_FILE);
        /* Sanity Check*/
        for (String answer : allAnswers) {
            if (!allWords.contains(answer)) {
                throw new RuntimeException(answer + " is not in the set of words for guessing");
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
}
