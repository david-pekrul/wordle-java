package org.pekrul.wordle;

import lombok.Getter;

import java.util.*;
import java.util.stream.Stream;

public class Turn {
    @Getter
    private final String answer;
    @Getter
    private String turnGuess;


    @Getter
    private String resultString;

    @Getter
//    Map<Integer, Set<Character>> greyCharactersByLocation;

    /***
     * The information built up about the non-green characters
     */
            Map<Integer, Map<Character, LetterStatus>> indexToCharacterToStatus;
    @Getter
    Set<Character> definiteGreyLetters;
//    Set<Character> nonDuplicatedCharacters;

    public Turn(String answer) {
        this.answer = answer;
        this.indexToCharacterToStatus = new HashMap<>();
        this.definiteGreyLetters = new HashSet<>();
    }

    private Turn(String answer, String guess, String resultString, Set<Character> prevGreyLetters) {
        this.answer = answer;
        this.turnGuess = guess;
        this.resultString = resultString;
        this.definiteGreyLetters = prevGreyLetters;
    }


    public Turn applyGuess(String guess) {
        LetterStatus[] result = new LetterStatus[guess.length()];

//        Map<Integer, Map<Character, LetterStatus>> nextLetterInfo = new HashMap<>(indexToCharacterToStatus);
        Set<Character> nextGreyLetters = new HashSet<>(this.definiteGreyLetters);

        String nonGreenLetters = "";
        String greenLetters = "";
        String remainingAnswerLetters = "";
        for (int i = 0; i < guess.length(); i++) {
            char letter = guess.charAt(i);
            if (answer.charAt(i) == letter) {
                result[i] = LetterStatus.GREEN;
                greenLetters += letter;
                nonGreenLetters += "_";
                remainingAnswerLetters += "_";
            } else {
                nonGreenLetters += letter;
                remainingAnswerLetters += answer.charAt(i);
                greenLetters += "_";
            }
        }
        /*
            At this point, we have the green letters and non-green letters.
            We don't know if the non-green letter/positions are yellow or grey.
            Grey    -> the letter does not appear in the answer
                ==> Definite Grey
            Grey    -> the letter appears in the answer, but that is already covered by a green.
            Yellow -> ???
         */


        //definiteGrey must be the letters that appear once in the guess and are not in the answer.
        //these match up to the grey letters on the wordle keyboard

        //nonDuplicated are the letters that appear multiple in the guess, but one of them is grey


        String yellowLetters = "";
        for (int i = 0; i < guess.length(); i++) {
            if (result[i] == LetterStatus.GREEN) {
                continue;
            }
            Character letter = guess.charAt(i);

            if (remainingAnswerLetters.contains("" + letter)) {
                //this letter is not a green letter
                //this letter is still in the remaining answer letters, just in a different location
                //=> This is a yellow letter
                result[i] = LetterStatus.YELLOW;
                yellowLetters += letter;
                //remove this letter from the remaining answer letters because we can only have as many yellow letters are there are misplaced correct guesses.
                remainingAnswerLetters = remainingAnswerLetters.replaceFirst("" + letter, "_");
                continue;
            }

            //this letter is not a green letter
            //this letter does not appear in the remaining answer letters
            //=> this letter is a grey letter
            result[i] = LetterStatus.GREY;

            //HOWEVER, this letter might be a duplicate of a green or yellow letter, so we need more before adding it to the definite grey set
            //Somehow determine if the answer contains this letter, but possibly in a different spot that is already green or yellow WITHOUT using answer.contains(letter);
            if (!greenLetters.contains("" + letter) && !yellowLetters.contains(""+letter)) {
                nextGreyLetters.add(letter);
            }

            //figure out how to remember a letter isn't duplicated, or that all instances of the letter are either green or yellow already
        }

        //SANITY CHECK
        for(Character c : answer.toCharArray()){
            if(nextGreyLetters.contains(c)){
                throw new RuntimeException("Filtering out letters in the answer.");
            }
        }
        StringBuilder builder = new StringBuilder();
        for (LetterStatus s : result) {
            builder.append(s);
        }
        String resultString = builder.toString();
        return new Turn(answer, guess, resultString, nextGreyLetters);
    }

    @Override
    public String toString() {
        return "{" +
                "answer='" + answer + '\'' +
                ", guess='" + turnGuess + '\'' +
                ", result=" + resultString +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Turn turn = (Turn) o;

        if (!answer.equals(turn.answer)) return false;
        if (!turnGuess.equals(turn.turnGuess)) return false;
        return resultString.equals(turn.resultString);
    }

    @Override
    public int hashCode() {
        int result = answer.hashCode();
        result = 31 * result + turnGuess.hashCode();
        result = 31 * result + resultString.hashCode();
        return result;
    }
}
