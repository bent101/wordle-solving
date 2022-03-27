import java.util.*;

public class AnswerPool {
    // list of answers in the answer pool
    private List<Answer> answers;
    // priority queue of next best guesses
    private PriorityQueue<Guess> guessQueue;
    // char frequencies across all answers
    private int[] charFreqs;
    
    public AnswerPool(List<Answer> answers) {
        this.answers = new LinkedList<>(answers);
        charFreqs = new int[26];
        for(Answer answer : answers) {
            for(Map.Entry<Character, Integer> e : answer.getCharFreqs().entrySet()) {
                charFreqs[e.getKey() - 'a'] += e.getValue();
            }
        }
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public int size() {
        return answers.size();
    }

    /**
     * Computes how many different hints this guess would yield across all
     * the answers in the answer pool, i.e. how much it divides the pool
     * @param guess the guess
     * @return the number of hints the guess would yield
     */
    public int getGuessDivisiveness(Guess guess) {
        Set<DetailedHint> hints = new HashSet<>();
        for(Answer answer : answers) {
            hints.add(new DetailedHint(guess, answer));
        }
        return hints.size();
    }

    /**
     * Estimates how much the specified guess would narrow
     * down this answer pool by summing character frequencies
     * @param guess the guess to be evaluated
     * @param countDuplicates whether or not duplicate characters in
     * the guess should be counted mutliple times in the answers
     * @return the score, with zero indicating a useless guess
     */
    public int getGuessCharFreqs(Guess guess) {
        int score = 0;
        for(Map.Entry<Character, Integer> e : guess.getCharFreqs().entrySet()) {
            score += charFreqs[e.getKey() - 'a'] * e.getValue();
        }
        return score;
    }

    // same as above method but doesnt count duplicate letters in the guess,
    // so words like areae dont win
    public int getGuessCharSetFreqs(Guess guess) {
        int score = 0;
        for(Character c : guess.getCharFreqs().keySet()) {
            score += charFreqs[c - 'a'];
        }
        return score;
    }

    public boolean guessIsUseful(Guess guess) {
        return getGuessCharFreqs(guess) == 0;
    }

    // assigns a guess 2 points for each green and 1 point for each yellow 
    // and returns the sum across all answers in the answer pool
    public int getGuessScore(Guess guess) {
        int score = 0;
        for(Answer answer : answers) { 
            boolean[] answerVector = answer.getVector();
            for(int i = 0; i < 5; i++) {
                char guessChar = guess.getWord().charAt(i);
                if(guessChar == answer.getWord().charAt(i)) {
                    score += 1;
                }
                if(answerVector[guessChar - 'a']) score++;
            }
        }
        return score;
    }

    public int getGuessesTotalElims(Guess... guesses) {
        int count = 0;
        Map<DetailedHint, Integer> seenHints = new HashMap<>();
        for(Answer answer : answers) {
            DetailedHint hint = new DetailedHint();
            for(Guess guess : guesses) hint.add(new DetailedHint(guess, answer));
            if(!seenHints.containsKey(hint)) {
                seenHints.put(hint, hint.getNumElims(this));
            }
            count += seenHints.get(hint);
        }
        return count;
    }

    
    public List<AnswerPool> getChildren(Guess guess) {
        // maps hints to all of the answers that yield that hint
        Map<DetailedHint, List<Answer>> map = new HashMap<>();
        for(Answer answer : answers) {
            DetailedHint hint = new DetailedHint(guess, answer);
            if(!map.containsKey(hint)) {
                map.put(hint, new ArrayList<>());
            }
            map.get(hint).add(answer);
        }

        // Put the groups of answers into a list of AnswerPools and
        // forget about the hints that categorized them
        List<AnswerPool> children = new ArrayList<>(map.size());
        for(List<Answer> answersList : map.values()) {
            children.add(new AnswerPool(answersList));
        }

        return children;
    }

    /**
     * We can compare memory addresses of answers since they will all come
     * from the same answers array in WordleCalculator
     */
    @Override
    public boolean equals(Object obj) {
        if(obj == null || obj.getClass() != this.getClass())
            return false;
        AnswerPool other = (AnswerPool) obj;
        if(this.answers.size() != other.answers.size())
            return false;
        for(int i = 0; i < this.answers.size(); i++) {
            if(this.answers.get(i) != other.answers.get(i))
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(answers);
    }
}
