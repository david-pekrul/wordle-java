package org.pekrul.wordle;

import lombok.Getter;

public class ResultPattern {
    @Getter
    private final String answer;
    @Getter
    private final String guess;


    public ResultPattern(String answer, String guess) {
        this.answer = answer;
        this.guess = guess;
    }
}
