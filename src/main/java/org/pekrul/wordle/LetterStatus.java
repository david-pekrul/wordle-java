package org.pekrul.wordle;

public enum LetterStatus {
    GREEN('G'), YELLOW('Y'), GREY('_');

    private final char display;

    LetterStatus(char display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return "" + display;
    }
}
