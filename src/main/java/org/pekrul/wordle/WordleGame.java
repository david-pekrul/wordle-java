package org.pekrul.wordle;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

public class WordleGame {

    static final int MAX_TURNS = 6;

    static final Set<Character> ALL_LETTERS = Arrays.stream("ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("(?!^)")).map(x -> x.charAt(0)).collect(Collectors.toSet());
    @Getter
    private final String startingWord;
    private final String answer;
    @Getter
    List<Turn> turns;

    @Getter
    private boolean solved;


    public WordleGame(String answer, String startingWord) {
        this.startingWord = startingWord;
        this.answer = answer;
        turns = new ArrayList<>(6);
    }

    public void playGame() {
        solved = false;
        Turn turn = new Turn(answer);
        while (turns.size() <= MAX_TURNS) { //allow for 2 extra guesses
            Turn nextTurn = turn.applyGuess(nextGuess());

            turns.add(nextTurn);

            if (nextTurn.getTurnGuess().equals(nextTurn.getAnswer())) {
                solved = true;
                break;
            }

            turn = nextTurn;
        }
    }

    private String nextGuess() {
        if (turns.isEmpty()) {
            return startingWord;
        }

        Turn previousTurn = turns.get(turns.size() - 1);
        String previousWord = previousTurn.getTurnGuess();
        String previousResult = previousTurn.getResultString();

        /* Find the set of words that match the pattern from all previous guesses */
        Set<String> intersection = null;
        for(Turn t : turns) {
            if(intersection == null){
                intersection = new HashSet<>(Wordle.guessToResultsToWords.get(t.getTurnGuess()).get(t.getResultString()));
            } else {
                intersection.retainAll(Wordle.guessToResultsToWords.get(t.getTurnGuess()).get(t.getResultString()));
            }
        }

//        Set<String> nextSet = new HashSet<>(Wordle.guessToResultsToWords.get(previousWord).get(previousResult));
        Set<String> nextSet = intersection;

        if(!nextSet.contains(answer)){
            throw new RuntimeException("Answer filtered out");
        }
        /*
            Steps:
                Remove words already used.
                Remove words that have yellow letters in the same places we've already tried
                Filter to words that have yellow letters in different locations
         */

        nextSet.removeAll(turns.stream().map(t -> t.getTurnGuess()).collect(Collectors.toSet()));

        if(!nextSet.contains(answer)){
            throw new RuntimeException("Answer filtered out");
        }

        Set<String> finalSet = nextSet.stream()
                .filter(word -> {
                    for (Character definiteGrey : previousTurn.definiteGreyLetters) {
                        if (word.contains("" + definiteGrey)) {
                            if(word.equals(answer)){
                                throw new RuntimeException("Tried to filter out the answer");
                            }
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toSet());

        if(!finalSet.contains(answer)){
            throw new RuntimeException("Answer filtered out");
        }

        return finalSet.stream().findAny().get();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        turns.forEach(turn -> {
            builder.append("\r\n\t");
            builder.append(turn);
        });
        builder.append("\r\n");

        return "WordleGame{" +
                " startingWord='" + startingWord + '\'' +
                ", answer='" + answer + '\'' +
                ", solved=" + solved +
                ", turns=" + turns.size() +" " + builder +
                '}';
    }
}
