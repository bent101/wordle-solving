import java.util.Objects;

public class Guess extends WordleWord {
    // keeps track of the number of answers the guess eliminates for each turn 
    private int[] totalElimsPerTurn;

    // turn to use for sorting
    private static int turnToCompare = 2;

    public Guess(String word) {
        super(word);
        totalElimsPerTurn = new int[WordleGame.MAX_GUESSES];
    }

    /**
     * Gets the number of answers eliminated for a given turn over all answers and previous guesses
     * @param turn The turn from 0 to 5
     * @return The number of answers eliminated
     */
    public int getElims(int turn) {
        return totalElimsPerTurn[turn];
    }

    public int getTotalElims() {
        int sum = 0;
        for(int n : totalElimsPerTurn) sum += n;
        return sum;
    }

    public boolean isCorrect(Answer answer) {
        return this.word.equals(answer.getWord());
    }

    public void addElim(int turn) {
        totalElimsPerTurn[turn]++;
    } 

    public boolean overlapsWith(boolean[] usedChars) {
        for(char c: word.toCharArray()) {
            if(usedChars[c - 'a']) return true;
        }
        return false;
    }

    // @Override
    // public String toString() {
    //     String ret = word + " | ";
    //     StringBuilder sb = new StringBuilder();
    //     for(int n : totalElimsPerTurn) {
    //         sb.append(n + " | ");
    //     }
    //     ret += sb.toString();
    //     return ret;
    // }

    @Override
    public int hashCode() {
        return Objects.hash(this.getElims(turnToCompare));
    }

}
