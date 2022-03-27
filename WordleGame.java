
/**
 * Implements a game of Wordle with a sequences of guesses and one answer
 */
public class WordleGame {
    public static final int MAX_GUESSES = 6;

    protected Guess[] guesses;
    protected Answer answer;
    // stores info given by each guess
    protected DetailedHint[] hints;
    protected SequentialHint[] sequentialHints;
    protected int numGuesses;
    public enum GameState {IN_PROGRESS, WON, LOST}

    public WordleGame(Answer answer) {
        guesses = new Guess[MAX_GUESSES];
        hints = new DetailedHint[MAX_GUESSES];
        sequentialHints = new SequentialHint[MAX_GUESSES];
        this.answer = answer;
        numGuesses = 0;
    }

    public WordleGame(Guess firstGuess, Answer answer) {
        this(answer);
        guess(firstGuess);
    }

    public void guess(Guess guess) {
        DetailedHint newHint = new DetailedHint(guess, answer);
        hints[numGuesses] = newHint;
        sequentialHints[numGuesses] = new SequentialHint(guess, answer);
        guesses[numGuesses] = guess;
        numGuesses++;
    }

    public int numGuesses() {
        return numGuesses;
    }

    public GameState getGameState() {
        if(numGuesses == 0) return GameState.IN_PROGRESS;
        if(guesses[numGuesses - 1].isCorrect(answer))
            return GameState.WON;
        if(numGuesses == MAX_GUESSES)
            return GameState.LOST;
        return GameState.IN_PROGRESS;
    }

    /**
     * Returns whether or not a word is compliant with the last hint
     * @param word The word to test
     * @return True if it complies with the last hint, false otherwise
     * @throws IllegalStateException if no guesses have been made
     */
    public boolean wordIsCompliant(WordleWord word) {
        if(numGuesses == 0) {
            throw new IllegalStateException(
                "Can't determine compliance because no guesses have been made");
        }
        // assume the word is compliant with all previous hints
        return hints[numGuesses - 1].wordIsCompliant(word);
    }

    public void undo() {
        if(numGuesses == 0) {
            throw new IllegalStateException("There is nothing to undo");
        }
        numGuesses--;
    }

    @Override
    public String toString() {
        String ret = "Answer: " + answer + "\n";
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 5; i++) {
            sb.append(guesses[i].getWord() + " -> " + sequentialHints[i] + "\n");
        }
        ret += sb.toString();

        return ret;
    }

}
