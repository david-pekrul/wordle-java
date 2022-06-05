package org.pekrul.wordle;

import java.io.*;
import java.sql.Time;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

public class Wordle {

    static Set<String> allWords;
    static Set<String> allAnswers;

    static Map<String, Map<String, Set<String>>> guessToResultsToWords;

    static Map<String, Map<String, Integer>> startingWordToAnswerToSolveCount;


    public static void main(String[] args) throws IOException {

        initAllWords();
        initAnswerWords();

        /* TEST */
        Turn testTurn = new Turn("AUDIT");
        testTurn = testTurn.applyGuess("AGENT");

        /* SANITY CHECK */
        for (String answer : allAnswers) {
            if (!allWords.contains(answer)) {
                throw new RuntimeException(answer + " is not in the set of words for guessing");
            }
        }

        initWordLookups();
        startingWordToAnswerToSolveCount = new ConcurrentHashMap<>(allWords.size());



        /* TEST */
        WordleGame testGame = new WordleGame("APART", "TASTY");
        testGame.playGame();
        System.out.println(testGame);


        /* FULL */
        long start = System.currentTimeMillis();
        LocalDateTime gamesStart = LocalDateTime.now();
        long totalGames = 1l * allAnswers.size() * allWords.size();
        final DecimalFormat df = new DecimalFormat("0.00");

        AtomicLong gamesPlayed = new AtomicLong();
        allAnswers.stream().forEach(answer -> {
            allWords.stream().parallel().forEach(startingWord -> {
                WordleGame wordleGame = new WordleGame(answer, startingWord);
                wordleGame.playGame();
//                System.out.println(wordleGame);
                gamesPlayed.getAndIncrement();
                if (gamesPlayed.get() % 1000 == 0) {
                    double progress = 100 * (gamesPlayed.get() / (1.0 * totalGames));
                    System.out.println("Games Played: " + gamesPlayed.get() + "/" + totalGames + "\t\t" + df.format(progress));
                }
                startingWordToAnswerToSolveCount.compute(startingWord, (k1, v1) -> {
                    if (v1 == null) {
                        v1 = new HashMap<>();
                    }
                    v1.compute(answer, (k2, v2) -> {
                        if (wordleGame.isSolved()) {
                            return wordleGame.turns.size();
                        } else {
                            return 30;
                        }
                    });
                    return v1;
                });
            });
        });

        long end = System.currentTimeMillis();

        System.out.println("Done");
        System.out.println("Games Played: " + gamesPlayed.get());
        System.out.println("Game time: " + df.format((end - start) / 1000.0));
    }

    private static void initAllWords() throws IOException {
        allWords = new HashSet<>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (InputStream resourceAsStream = classloader.getResourceAsStream("allowed_words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            ;
            reader.lines().forEach(line ->
            {
                allWords.add(line.toUpperCase(Locale.ROOT));
            });
        }
        allWords = Collections.unmodifiableSet(allWords);
    }

    private static void initAnswerWords() throws IOException {
        allAnswers = new HashSet<>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try (InputStream resourceAsStream = classloader.getResourceAsStream("possible_words.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
            ;
            reader.lines().forEach(line ->
            {
                allAnswers.add(line.toUpperCase(Locale.ROOT));
            });
        }

        allAnswers = Collections.unmodifiableSet(allAnswers);
    }

    private static void initWordLookups() {
        AtomicInteger answersDone = new AtomicInteger();
        Object mutex = new Object();
        Object mutex2 = new Object();
        guessToResultsToWords = new HashMap<>(allWords.size());
        allWords.stream().parallel().forEach(guess -> {
            Map<String, Set<String>> resultToWordsPerAnswer = new HashMap<>();
            allAnswers.stream().forEach(answer -> {
                Turn turn = new Turn(answer);
                Turn generatedTurn = turn.applyGuess(guess);
                resultToWordsPerAnswer.compute(generatedTurn.getResultString(), (k, v) -> {
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
                guessToResultsToWords.put(guess, Collections.unmodifiableMap(resultToWordsPerAnswer));
            }

            answersDone.getAndIncrement();
            if (answersDone.get() % 100 == 0) {
                System.out.println(answersDone.get());
            }
        });
        guessToResultsToWords = Collections.unmodifiableMap(guessToResultsToWords);
    }
}
