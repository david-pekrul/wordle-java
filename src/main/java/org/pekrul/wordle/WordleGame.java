package org.pekrul.wordle;

import lombok.Getter;
import org.pekrul.wordle.data.WordleData;

import java.util.*;

public class WordleGame {

    static final int MAX_TURNS = 6;

    @Getter
    private final String startingWord;
    private final String answer;
    @Getter
    private List<Turn> turns;

    private final WordleData data;
    private int turnsPlayed;

    @Getter
    private boolean solved;

    private Set<String> possibleSolutionSet = new HashSet<>(50);

    public WordleGame(WordleData data, String answer, String startingWord) {
        this.startingWord = startingWord;
        this.answer = answer;
        this.data = data;
        turns = new LinkedList<>();
        turnsPlayed = 0;
    }

    public void playGame() {
        solved = false;
        Turn turn = new Turn(answer);
        while (turns.size() <= MAX_TURNS) { //allow for 2 extra guesses
            Turn nextTurn = turn.applyGuess(nextGuess());

            turns.add(nextTurn);
            turnsPlayed++;

            if (nextTurn.getTurnGuess().equals(nextTurn.getAnswer())) {
                solved = true;
                break;
            }

            turn = nextTurn;
        }
    }


    private String nextGuess() {
        if (turnsPlayed == 0) {
            return startingWord;
        }

        Turn previousTurn = turns.get(turnsPlayed - 1);

        /* Find the set of words that match the pattern from all previous guesses */
        if (turnsPlayed == 1) {
            possibleSolutionSet.addAll(data.getPossibleAnswers(previousTurn));
        } else {
            possibleSolutionSet.retainAll(data.getPossibleAnswers(previousTurn));
        }

        possibleSolutionSet.remove(previousTurn.getTurnGuess());

        possibleSolutionSet.removeIf(word -> {
            for (Character definiteGrey : previousTurn.definiteGreyLetters) {
                if (word.contains("" + definiteGrey)) {
                    if (word.equals(answer)) {
                        throw new RuntimeException("Tried to filter out the answer");
                    }
                    return true;
                }
            }
            return false;
        });

        /*
            Of the remaining words in the possibleSolutionSet, we have a choice on which one to pick, but with tradeoffs.
            Option 1: word -> most selective of the words that remain.
                Which word, if we guessed it, would create the smallest average remaining set?
                Word -> Every other word -> group by result string -> count(size) -> average over # of groups
                This potentially sacrifices the winning word for the most selective word. But how often?
            Option 2: word -> most common word in the english language?
         */


        //Note: if I want to run through EVERY game, this would change to a Collector.Set
        //and then order them by some metric (entropy?)
        return possibleSolutionSet.stream().findAny().get();
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
