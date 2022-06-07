package org.pekrul.wordle;

import lombok.Getter;
import org.pekrul.wordle.data.WordleData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordleGame {

    static final int MAX_TURNS = 6;

    @Getter
    private final String startingWord;
    private final String answer;
    @Getter
    private List<Turn> turns;

    private final WordleData data;

    @Getter
    private boolean solved;

    private Set<Integer> possibleSolutionSet = new HashSet<>(50);

//    public WordleGame(WordleData data, String answer, String startingWord) {
//        this.startingWord = startingWord;
//        this.answer = answer;
//        this.data = data;
//        turns = new ArrayList<>(MAX_TURNS);
//    }

    public WordleGame(WordleData data, Integer answer, Integer startingWord) {
        this.startingWord = data.getWord(startingWord);
        this.answer = data.getWord(answer);
        this.data = data;
        turns = new ArrayList<>(MAX_TURNS);
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
            possibleSolutionSet.addAll(data.getPossibleAnswerIds(previousTurn));
        } else {
            possibleSolutionSet.retainAll(data.getPossibleAnswerIds(previousTurn));
        }


        Integer nextGuessId = possibleSolutionSet.stream().unordered()
                .filter(wordId -> {
                    if (turns.stream().anyMatch(t -> t.getTurnGuess().equals(wordId))) {
                        return false;
                    }
                    for (Character definiteGrey : previousTurn.definiteGreyLetters) {

                        if (data.getWord(wordId).contains("" + definiteGrey)) {
                            if (wordId.equals(answer)) {
                                throw new RuntimeException("Tried to filter out the answer");
                            }
                            return false;
                        }
                    }
                    return true;
                })
                .findAny().get(); //Note: if I want to run through EVERY game, this would change to a Collector.Set
        return data.getWord(nextGuessId);
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
