package org.pekrul.wordle;

import org.apache.commons.collections4.Trie;
import org.apache.commons.collections4.trie.PatriciaTrie;
import org.javatuples.Pair;

import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class Wordle {

    static Set<String> allWords;
    static Set<String> allAnswers;

    static Map<String, Map<String, Set<String>>> guessToResultsToWords;

    static Map<String, Map<String, Integer>> startingWordToAnswerToSolveCount;

    static Trie<String, Trie<String, Trie<String, Boolean>>> trieGuessLookup;


    public static void main(String[] args) throws IOException {

        initAllWords();
        initAnswerWords();

        /* SANITY CHECK */
        for (String answer : allAnswers) {
            if (!allWords.contains(answer)) {
                throw new RuntimeException(answer + " is not in the set of words for guessing");
            }
        }

//        initMapLookups();
        initTrieLookup();
        startingWordToAnswerToSolveCount = new ConcurrentHashMap<>(allWords.size());


        /* FULL */
        long start = System.currentTimeMillis();
        LocalDateTime gamesStart = LocalDateTime.now();
        long totalGames = 1l * allAnswers.size() * allWords.size();
        int percent = (int)Math.round(totalGames * 0.01);
        final DecimalFormat df = new DecimalFormat("0.00");

        AtomicLong gamesPlayed = new AtomicLong();
        allAnswers.stream().forEach(answer -> {
            allWords.stream().parallel().forEach(startingWord -> {
                WordleGame wordleGame = new WordleGame(answer, startingWord);
                wordleGame.playGame();
                gamesPlayed.getAndIncrement();
                if (gamesPlayed.get() % percent == 0) {
                    double progress = 100 * (gamesPlayed.get() / (1.0 * totalGames));
//                    System.out.println(wordleGame);
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
                            return Integer.MAX_VALUE;
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

        runStats();
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

    private static void initMapLookups() {
        AtomicInteger answersDone = new AtomicInteger();
        Object mutex = new Object();
        guessToResultsToWords = new HashMap<>(allWords.size());
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
                guessToResultsToWords.put(guess, Collections.unmodifiableMap(resultToAnswersPerGuess));
            }

            answersDone.getAndIncrement();
            if (answersDone.get() % 100 == 0) {
                System.out.println(answersDone.get());
            }
        });
        guessToResultsToWords = Collections.unmodifiableMap(guessToResultsToWords);
    }


    private static void runStats() {
        startingWordToAnswerToSolveCount.entrySet().stream()
                .map(x -> {
                    return new Pair<>(x.getKey(), getAverage(x.getValue()));
                })
                .sorted((a, b) -> {
                    return Double.compare(a.getValue1(),b.getValue1());
                })
                .limit(10)
                .forEach(x -> {
                    System.out.println(x.getValue0() + ":\t" + x.getValue1());
                });
    }

    private static double getAverage(Map<String, Integer> input) {
        return input.entrySet().stream().map(x -> {
            return (long) x.getValue();
        }).reduce(0l, Long::sum) / (1.0 * input.size());
    }

    private static void initTrieLookup() {
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
