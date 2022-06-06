package org.pekrul.wordle;

import lombok.Getter;
import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WordleData {

    private String ALL_WORDS_FILE = "allowed_words.txt";
    private String ANSWER_WORDS_FILE = "possible_words.txt";

    @Getter
    Set<String> allWords;
    @Getter
    Set<String> allAnswers;


    Trie<String, Trie<String, Trie<String, Boolean>>> trieGuessLookup;

    Map<String, Map<String, Set<String>>> mapGuessLookup;

    private static WordleData singleton;
    private static Object mutex = new Object();

    public static WordleData getInstance() throws IOException {
        synchronized (mutex) {
            if (singleton != null) {
                return singleton;
            }
            singleton = new WordleData();
            return singleton;
        }
    }

    private WordleData() throws IOException {
        allWords = initWordsFromFile(ALL_WORDS_FILE);
        allAnswers = initWordsFromFile(ANSWER_WORDS_FILE);

        /* Sanity Check*/
        for (String answer : allAnswers) {
            if (!allWords.contains(answer)) {
                throw new RuntimeException(answer + " is not in the set of words for guessing");
            }
        }

        initTrieLookup();
//        initMapLookups();
        System.out.println("WorldeData init done");
    }

    public Set<String> getPossibleAnswers(String guess, String resultPattern) {
        return trieGuessLookup.get(guess).get(resultPattern).keySet();
    }

    public Set<String> getPossibleAnswers(Turn previousTurn) {
        return getPossibleAnswers(previousTurn.getTurnGuess(), previousTurn.getResultString());
    }


    private Set<String> initWordsFromFile(String fileName) throws IOException {
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

    private void initMapLookups() {
        AtomicInteger answersDone = new AtomicInteger();
        Object mutex = new Object();
        mapGuessLookup = new HashMap<>(allWords.size());
        allWords.stream().parallel().forEach(guess -> {
            Map<String, Set<String>> resultToAnswersPerGuess = new HashMap<>();
            allAnswers.stream().forEach(answer -> {
                Turn turn = new Turn(answer);
                Turn generatedTurn = turn.applyGuess(guess);
                resultToAnswersPerGuess.compute(generatedTurn.getResultString(), (k, v) -> {
                    if (v == null) {
                        v = new HashSet<>();
                    }
                    v.add(answer);
                    return v;
                });
            });

            //put the guess into the larger data set
            //guess -> resultToWordsPerAnswer
            synchronized (mutex) {
                mapGuessLookup.put(guess, Collections.unmodifiableMap(resultToAnswersPerGuess));
            }

            answersDone.getAndIncrement();
            if (answersDone.get() % 100 == 0) {
                System.out.println(answersDone.get());
            }
        });
        mapGuessLookup = Collections.unmodifiableMap(mapGuessLookup);
    }

    private void initTrieLookup() {
        trieGuessLookup = new PatriciaTrie<>();

        AtomicInteger answersDone = new AtomicInteger();
        Object mutex = new Object();
        allWords.stream().parallel().forEach(guess -> {

            Trie<String, Trie<String, Boolean>> resultToAnswersPerGuess = new PatriciaTrie<>();
            allAnswers.stream().forEach(answer -> {
                Turn turn = new Turn(answer);
                Turn generatedTurn = turn.applyGuess(guess);
                resultToAnswersPerGuess.compute(generatedTurn.getResultString(), (k, v) -> {
                    if (v == null) {
                        v = new PatriciaTrie<>();
                    }
                    v.put(answer, true);
                    return v;
                });
            });

            //put the guess into the larger data set
            //guess -> resultToWordsPerAnswer
            synchronized (mutex) {
                trieGuessLookup.put(guess, resultToAnswersPerGuess);
            }

            answersDone.getAndIncrement();
            if (answersDone.get() % 100 == 0) {
                System.out.println(answersDone.get());
            }
        });

        System.out.println("Done Trie init");
    }
}
