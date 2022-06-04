package org.pekrul.wordle;

import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Turn {
    @Getter
    private final String answer;
    @Getter
    private final String guess;

    @Getter
    private int greenCount;

    @Getter
    private final String resultString;

    public enum LetterStatus {
        GREEN("G"), YELLOW("Y"), GREY("_");

        private final String display;

        LetterStatus(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }

    public Turn(String answer, String guess) {

        if (answer.length() != guess.length()) {
            throw new RuntimeException("Lengths don't match.");
        }
        this.answer = answer.toUpperCase(Locale.ROOT);
        this.guess = guess.toUpperCase(Locale.ROOT);
        greenCount = 0;
        resultString = generateResult().intern();
    }

    private String generateResult() {
        LetterStatus[] result = new LetterStatus[guess.length()];

        Stream<Character> characterStream = answer.chars().mapToObj(c -> (char) c);

        for (int i = 0; i < guess.length(); i++) {
            char letter = guess.charAt(i);
            if (answer.charAt(i) == letter) {
                result[i] = LetterStatus.GREEN;
                greenCount++;
            }
        }
        if (greenCount != guess.length()) {

            Map<Character, Long> answerLetterFreq = answer.chars().mapToObj(c -> (char) c).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            for (int i = 0; i < guess.length(); i++) {
                if (result[i] == LetterStatus.GREEN) {
                    continue;
                }
                Character letter = guess.charAt(i);
                if (!answer.contains("" + letter)) {
                    result[i] = LetterStatus.GREY;
                    continue;
                }
                /*
                    Determine if this should be a yellow
                 */
                Long letterFreq = answerLetterFreq.get(letter);
                if (letterFreq != null && letterFreq != 0) {
                    result[i] = LetterStatus.YELLOW;
                    answerLetterFreq.compute(letter, (K, V) -> V - 1);
                } else {
                    result[i] = LetterStatus.GREY;
                }
            }
        }

        StringBuilder builder = new StringBuilder();
        for (LetterStatus s : result) {
            builder.append(s);
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "{" +
                "answer='" + answer + '\'' +
                ", guess='" + guess + '\'' +
                ", greenCount=" + greenCount +
                ", result=" + resultString +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Turn turn = (Turn) o;

        if (!answer.equals(turn.answer)) return false;
        if (!guess.equals(turn.guess)) return false;
        return resultString.equals(turn.resultString);
    }

    @Override
    public int hashCode() {
        int result = answer.hashCode();
        result = 31 * result + guess.hashCode();
        result = 31 * result + resultString.hashCode();
        return result;
    }
}
