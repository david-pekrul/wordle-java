package org.pekrul.wordle.data;

import org.pekrul.wordle.Turn;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class WordleDataMap extends WordleData {

    Map<Integer, Map<String, Set<Integer>>> mapGuessLookup;

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

    public Set<Integer> getPossibleAnswerIds(Turn previousTurn) {
        return getPossibleAnswerIds(previousTurn.getTurnGuess(), previousTurn.getResultString());
    }

    public Set<Integer> getPossibleAnswerIds(String guess, String resultPattern) {

        return mapGuessLookup.get(allWordsToIds.get(guess)).get(resultPattern);
    }

    private WordleDataMap() throws IOException {
        super.init();
        initMapLookups();
        System.out.println("WorldeData init done");
    }

    private void initMapLookups() {
        AtomicInteger answersDone = new AtomicInteger();
        Object mutex = new Object();
        mapGuessLookup = new HashMap<>(allWordsToIds.size());
        allWordsToIds.entrySet().stream().parallel().forEach(guessEntry -> {
            String guess = guessEntry.getKey();
            Map<String, Set<Integer>> resultToAnswersPerGuess = new HashMap<>();
            allAnswers.stream().forEach(answerId -> {
                String answer = allIdsToWords.get(answerId);
                Turn turn = new Turn(answer);
                Turn generatedTurn = turn.applyGuess(guess);
                resultToAnswersPerGuess.compute(generatedTurn.getResultString(), (k, v) -> {
                    if (v == null) {
                        v = new HashSet<>();
                    }
                    v.add(answerId);
                    return v;
                });
            });

            //put the guess into the larger data set
            //guess -> resultToWordsPerAnswer
            synchronized (mutex) {
                mapGuessLookup.put(guessEntry.getValue(), Collections.unmodifiableMap(resultToAnswersPerGuess));
            }

            answersDone.getAndIncrement();
            if (answersDone.get() % 100 == 0) {
                System.out.println(answersDone.get());
            }
        });
        mapGuessLookup = Collections.unmodifiableMap(mapGuessLookup);
    }
}
