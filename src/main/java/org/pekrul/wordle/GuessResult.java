package org.pekrul.wordle;

import lombok.NonNull;

public class GuessResult {

    private final String guess;
    private final String result;

    public GuessResult(@NonNull String guess, @NonNull String result) {
        this.guess = guess;
        this.result = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GuessResult that = (GuessResult) o;

        if (!guess.equals(that.guess)) return false;
        return result.equals(that.result);
    }

    @Override
    public int hashCode() {
        int result1 = guess.hashCode();
        result1 = 31 * result1 + result.hashCode();
        return result1;
    }
}
