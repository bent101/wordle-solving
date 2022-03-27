import java.util.*;

public class Opening implements Comparable<Opening> {
    SequentialHint firstHint;
    Guess secondGuess;
    List<Answer> possibleAnswers;

    public Opening(SequentialHint firstHint, Guess secondGuess, List<Answer> possibleAnswers) {
        this.firstHint = firstHint;
        this.secondGuess = secondGuess;
        this.possibleAnswers = possibleAnswers;
    }

    @Override
    public String toString() {
        String suffix = "";
        int numAnswers = possibleAnswers.size();
        if(numAnswers == 2) {
            suffix = possibleAnswers.get(0).word +
                    " or " +
                     possibleAnswers.get(1).word;
        } else {
            int percent = 100 * numAnswers / WordleCalculator.NUM_ANSWERS;
            suffix += percent > 0 ? percent + "%" : numAnswers;
        }
        suffix = " (" + suffix + ")";
        return firstHint + " -> " + secondGuess.getWord() + suffix;
    }

    @Override
    public int compareTo(Opening o) {
        int thisSize = this.possibleAnswers.size();
        int otherSize = o.possibleAnswers.size();
        if(thisSize < otherSize) return 1;
        if(thisSize > otherSize) return -1;
        return 0;
    }
}
