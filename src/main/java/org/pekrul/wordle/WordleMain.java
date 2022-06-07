package org.pekrul.wordle;

import org.javatuples.Pair;
import org.pekrul.wordle.data.WordleData;
import org.pekrul.wordle.data.WordleDataMap;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class WordleMain {

    private Map<Integer, Map<Integer, Integer>> startingWordToAnswerToSolveCount;

    public WordleMain() {
    }

    public void run() throws IOException {
        //WordleData wordleData = WordleDataTrie.getInstance();
        WordleData wordleData = WordleDataMap.getInstance(); //Map seems faster now.

        startingWordToAnswerToSolveCount = new ConcurrentHashMap<>(wordleData.getAllWordsSize());

        /* FULL */
        long start = System.currentTimeMillis();
        LocalDateTime gamesStart = LocalDateTime.now();
        long totalGames = 1l * wordleData.getAllAnswerSize() * wordleData.getAllWordsSize();
        int percent = (int) Math.round(totalGames * 0.01);
        final DecimalFormat df = new DecimalFormat("0.00");

        AtomicLong gamesPlayed = new AtomicLong();
        wordleData.getAllAnswers().stream().forEach(answerId -> {
            wordleData.getAllIdsToWords().keySet().stream().parallel().forEach(startingWordId -> {
                WordleGame wordleGame = new WordleGame(wordleData, answerId, startingWordId);
                wordleGame.playGame();
                gamesPlayed.getAndIncrement();
                if (gamesPlayed.get() % percent == 0) {
                    double progress = 100 * (gamesPlayed.get() / (1.0 * totalGames));
//                    System.out.println(wordleGame);
                    System.out.println("Games Played: " + gamesPlayed.get() + "/" + totalGames + "\t\t" + df.format(progress));
                }
                startingWordToAnswerToSolveCount.compute(startingWordId, (k1, v1) -> {
                    if (v1 == null) {
                        v1 = new HashMap<>();
                    }
                    v1.compute(answerId, (k2, v2) -> {
                        if (wordleGame.isSolved()) {
                            return wordleGame.getTurns().size();
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

        runStats(wordleData);
    }

    private void runStats(WordleData wordleData) {
        startingWordToAnswerToSolveCount.entrySet().stream()
                .map(x -> {
                    return new Pair<>(x.getKey(), getAverage(x.getValue()));
                })
                .sorted((a, b) -> {
                    return Double.compare(a.getValue1(), b.getValue1());
                })
                .limit(10)
                .forEach(x -> {
                    System.out.println(wordleData.getWord(x.getValue0()) + ":\t" + x.getValue1());
                });
    }

    private double getAverage(Map<?, Integer> input) {
        return input.entrySet().stream().map(x -> {
            return (long) x.getValue();
        }).reduce(0l, Long::sum) / (1.0 * input.size());
    }
}
