package org.pekrul.wordle.data;

import org.pekrul.wordle.Turn;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WordleDataMap extends WordleData {

    Map<String, Map<String, Set<String>>> mapGuessLookup;

    private static WordleData singleton;
    private static Object mutex = new Object();

    public static WordleData getInstance() throws IOException {
        synchronized (mutex) {
            if (singleton != null) {
                return singleton;
            }
            singleton = new WordleDataMap();
            return singleton;
        }
    }

    public Set<String> getPossibleAnswers(Turn previousTurn) {
        return getPossibleAnswers(previousTurn.getTurnGuess(), previousTurn.getResultString());
    }

    public Set<String> getPossibleAnswers(String guess, String resultPattern) {
        return mapGuessLookup.get(guess).get(resultPattern);
    }

    private WordleDataMap() throws IOException {
        super.init();
        initMapLookups();
        System.out.println("WorldeData init done");
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
}
