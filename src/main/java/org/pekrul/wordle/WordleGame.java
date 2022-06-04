package org.pekrul.wordle;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordleGame {

    static final int MAX_TURNS = 8;

    static final Set<Character> ALL_LETTERS = Arrays.stream("ABCDEFGHIJKLMNOPQRSTUVWXYZ".split("(?!^)")).map(x -> x.charAt(0)).collect(Collectors.toSet());
    @Getter
    private final String startingWord;
    private final String answer;
    @Getter
    List<Turn> turns;



    @Getter
    private boolean solved;

    private String previousWord;
    private String previousResult;

    public WordleGame(String answer, String startingWord) {


        this.startingWord = startingWord;
        previousWord = startingWord;
        this.answer = answer;
        turns = new ArrayList<>(6);
    }

    public void playGame() {
        solved = false;
        while(turns.size() <= MAX_TURNS) { //allow for 2 extra guesses
            Turn nextTurn = new Turn(answer, nextGuess());
            previousWord = nextTurn.getGuess();
            previousResult = nextTurn.getResultString();
            turns.add(nextTurn);
            if (nextTurn.getGreenCount() == answer.length()) {
                solved = true;
                break;
            }
        }
    }

    private String nextGuess() {
        if (turns.isEmpty()) {
            return startingWord;
        }
        Map<String, Set<String>> nextResultOptions = Wordle.guessToResultsToWords.get(previousWord);
        Set<String> nextSet = nextResultOptions.get(previousResult);

        nextSet.removeAll(turns.stream().map(t -> t.getGuess()).collect(Collectors.toSet()));
        String next = nextSet.stream().findAny().get();
        return next;
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
                ", turns=" + builder +
                '}';
    }
}
