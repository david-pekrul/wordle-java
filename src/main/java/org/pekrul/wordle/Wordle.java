package org.pekrul.wordle;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Wordle {

    static List<String> allWords;
    static List<String> allAnswers;

    static Map<String, Map<String, Set<String>>> guessToResultsToWords;


    public static void main(String[] args) throws IOException {

        initAllWords();
        initAnswerWords();
        initWordLookups();

        String answer = "CHURN";
        allWords.stream().forEach(startingWord -> {
            WordleGame wordleGame = new WordleGame(answer,startingWord);
            wordleGame.playGame();
            System.out.println(wordleGame);
        });

        System.out.println("Done");


    }

    private static void initAllWords() throws IOException {
        allWords = new LinkedList<>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (InputStream resourceAsStream = classloader.getResourceAsStream("allowed_words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            ;
            reader.lines().forEach(line ->
            {
                allWords.add(line.toUpperCase(Locale.ROOT));
            });
        }
        allWords = Collections.unmodifiableList(allWords);
    }

    private static void initAnswerWords() throws IOException {
        allAnswers = new LinkedList<>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (InputStream resourceAsStream = classloader.getResourceAsStream("possible_words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            ;
            reader.lines().forEach(line ->
            {
                allAnswers.add(line.toUpperCase(Locale.ROOT));
            });
        }

        allAnswers = Collections.unmodifiableList(allAnswers);
    }

    private static void initWordLookups() {
        AtomicInteger answersDone = new AtomicInteger();
        guessToResultsToWords = new ConcurrentHashMap<>(allWords.size());
        allAnswers.stream().parallel().forEach(answer -> {
            allWords.stream().forEach(guess -> {
                Turn turn = new Turn(answer, guess);
                guessToResultsToWords.compute(guess, (k1, v1) -> {
                    if (v1 == null) {
                        v1 = new HashMap<>();
                    }
                    v1.compute(turn.getResultString(), (k2, v2) -> {
                        if (v2 == null) {
                            v2 = new HashSet<>();
                        }
                        v2.add(turn.getAnswer());
                        return v2;
                    });
                    return v1;
                });
            });
            answersDone.getAndIncrement();
            if (answersDone.get() % 100 == 0) {
                System.out.println(answersDone.get());
            }
        });
        guessToResultsToWords = Collections.unmodifiableMap(guessToResultsToWords);
    }
}
