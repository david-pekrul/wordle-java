package org.pekrul.wordle;

import lombok.Getter;

import java.util.*;

public class WordleGame {

    static final int MAX_TURNS = 6;

    @Getter
    private final String startingWord;
    private final String answer;
    @Getter
    List<Turn> turns;

    @Getter
    private boolean solved;

    private Set<String> possibleSolutionSet = new HashSet<>(50);

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

        /* Find the set of words that match the pattern from all previous guesses */
        if (turns.size() == 1) {
            possibleSolutionSet.addAll(Wordle.trieGuessLookup.get(previousTurn.getTurnGuess()).get(previousTurn.getResultString()).keySet());
        } else {
            possibleSolutionSet.retainAll(Wordle.trieGuessLookup.get(previousTurn.getTurnGuess()).get(previousTurn.getResultString()).keySet());
        }

        String nextGuess = possibleSolutionSet.stream().unordered()
                .filter(word -> {
                    if (turns.stream().anyMatch(t -> t.getTurnGuess().equals(word))) {
                        return false;
                    }
                    for (Character definiteGrey : previousTurn.definiteGreyLetters) {
                        if (word.contains("" + definiteGrey)) {
                            if (word.equals(answer)) {
                                throw new RuntimeException("Tried to filter out the answer");
                            }
                            return false;
                        }
                    }
                    return true;
                }).findAny().get();
        return nextGuess;
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
                ", turns=" + turns.size() + " " + builder +
                '}';
    }
}
