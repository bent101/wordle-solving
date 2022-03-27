import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SequentialHint {

    public enum LetterHint {
        GRAY, YELLOW, GREEN;

        public static LetterHint getFromNum(int num) {
            switch(num) {
                case 1: return GRAY;
                case 2: return YELLOW;
                case 3: return GREEN;
                default: return null;
            }
        }

        @Override
        public String toString() {
            switch(this) {
                case GRAY: return "⬛";
                case YELLOW: return "🟨";
                case GREEN: return "🟩";
                default: return "_";
                
            }
        }
    }

    private static final String INVALID_SEQUENCE =
            "The hint sequence can only contain 1, 2, or 3 for gray, yellow, or green";
    private static final String INVALID_LENGTH =
            "The hint sequence must be 5 characters";

    private Guess guess;
    private LetterHint[] hints;
    public SequentialHint(Guess guess, Answer answer) {
        this.guess = guess;

        // initialize word hint as 5 grays
        hints = new LetterHint[5];
        for(int i = 0; i < 5; i++) {
            hints[i] = LetterHint.GRAY;
        }

        // indeces un-hinted letters in the guess
        List<Integer> unhintedLetterIndeces = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4));

        // un-hinted-at letters in the answer
        List<Character> unhintedAtLetters = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            unhintedAtLetters.add(answer.charAt(i));
        }

        // green hints
        for(int i = 4; i >= 0; i--) {
            char c = guess.charAt(i);
            if(c == answer.charAt(i)) {
                hints[i] = LetterHint.GREEN;
                unhintedLetterIndeces.remove(i);
                unhintedAtLetters.remove(unhintedAtLetters.indexOf(c));
            }
        }

        // yellow hints
        for(int i : unhintedLetterIndeces) {
            char c = guess.charAt(i);
            if(answer.containsChar(c) && unhintedAtLetters.contains(c)) {
                hints[i] = LetterHint.YELLOW;
                unhintedAtLetters.remove(unhintedAtLetters.indexOf(c));
            }
        }
    }

    public SequentialHint(Guess guess, String hintSequence) {
        this.guess = guess;
        if(hintSequence.length() != 5) {
            throw new IllegalArgumentException(INVALID_LENGTH);
        }
        for(int i = 0; i < 5; i++) {
            int num = hintSequence.charAt(i) - '0'; // either 1 2 or 3
            if(num < 1 || num > 3) {
                throw new IllegalArgumentException(INVALID_SEQUENCE);
            }
            hints[i] = LetterHint.getFromNum(num);
        }
    }

    public Guess getGuess() {
        return guess;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(LetterHint hint : hints) {
            if(hint == null) {
                throw new NullPointerException();
            }
            sb.append(hint);
        }
        return sb.toString();
    }
}

