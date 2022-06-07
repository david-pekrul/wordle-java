//package org.pekrul.wordle.data;
//
//import org.apache.commons.collections4.Trie;
//import org.apache.commons.collections4.trie.PatriciaTrie;
//import org.pekrul.wordle.Turn;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public class WordleDataTrie extends WordleData {
//
//
//    Trie<String, Trie<String, Trie<String, Boolean>>> trieGuessLookup;
//
//    private static WordleData singleton;
//    private static Object mutex = new Object();
//
//    public static WordleData getInstance() throws IOException {
//        synchronized (mutex) {
//            if (singleton != null) {
//                return singleton;
//            }
//            singleton = new WordleDataTrie();
//            return singleton;
//        }
//    }
//
//    private WordleDataTrie() throws IOException {
//        super.init();
//        initTrieLookup();
//        System.out.println("WordleDataTrie init done");
//    }
//
//    public Set<String> getPossibleAnswers(Turn previousTurn) {
//        return getPossibleAnswers(previousTurn.getTurnGuess(), previousTurn.getResultString());
//    }
//
//    public Set<String> getPossibleAnswers(String guess, String resultPattern) {
//        return trieGuessLookup.get(guess).get(resultPattern).keySet();
//    }
//
//    private void initTrieLookup() {
//        trieGuessLookup = new PatriciaTrie<>();
//
//        AtomicInteger answersDone = new AtomicInteger();
//        Object mutex = new Object();
//        allWordsToIds.keySet().stream().parallel().forEach(guess -> {
//
//            Trie<String, Trie<String, Boolean>> resultToAnswersPerGuess = new PatriciaTrie<>();
//            allAnswers.stream().forEach(answer -> {
//                Turn turn = new Turn(answer);
//                Turn generatedTurn = turn.applyGuess(guess);
//                resultToAnswersPerGuess.compute(generatedTurn.getResultString(), (k, v) -> {
//                    if (v == null) {
//                        v = new PatriciaTrie<>();
//                    }
//                    v.put(answer, true);
//                    return v;
//                });
//            });
//
//            //put the guess into the larger data set
//            //guess -> resultToWordsPerAnswer
//            synchronized (mutex) {
//                trieGuessLookup.put(guess, resultToAnswersPerGuess);
//            }
//
//            answersDone.getAndIncrement();
//            if (answersDone.get() % 100 == 0) {
//                System.out.println(answersDone.get());
//            }
//        });
//    }
//}
