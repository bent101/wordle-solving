import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class WordleCalculator {

    public static final int NUM_GUESSES = 12972;
    public static final int NUM_ANSWERS = 2315;
    private Guess[] guesses;
    private Answer[] answers;
    private AnswerPool rootAnswerPool;
    public int[] charFreqs; // frequencies of characters across all wordle answers

    public WordleCalculator() throws AssertionError, IOException {
        try (
            Scanner guessesScanner = new Scanner(new File("io/guesses.txt"));
            Scanner answersScanner = new Scanner(new File("io/answers.txt"));
        ) {
            guesses = new Guess[NUM_GUESSES];
            answers = new Answer[NUM_ANSWERS];

            int numGuesses = 0;
            int numAnswers = 0;

            // read in guesses, making sure there are NUM_GUESSES of them
            while(guessesScanner.hasNext()) {
                if(numGuesses == NUM_GUESSES) {
                    throw new AssertionError("There are more than " + NUM_GUESSES + " guesses");
                }
                guesses[numGuesses++] = new Guess(guessesScanner.nextLine());
            }
            if(numGuesses < NUM_GUESSES) {
                throw new AssertionError("There are less than " + NUM_GUESSES + " guesses");
            }

            // read in answers, making sure there are NUM_ANSWERS of them
            while(answersScanner.hasNext()) {
                if(numAnswers == NUM_ANSWERS) {
                    throw new AssertionError("There are more than " + NUM_ANSWERS + " answers");
                }
                answers[numAnswers++] = new Answer(answersScanner.nextLine());
            }
            if(numAnswers < NUM_ANSWERS) {
                throw new AssertionError("There are less than " + NUM_ANSWERS + " answers");
            }

            rootAnswerPool = new AnswerPool(Arrays.asList(answers));

            charFreqs = new int[26];
            for(Answer answer : answers) {
                for(char c : answer.getWord().toCharArray()) {
                    charFreqs[c - 'a']++;
                }
            }
        }
    }

    public Guess getGuess(int i) {
        return guesses[i];
    }

    public Answer getAnswer(int i) {
        return answers[i];
    }

    public void addElimToGuess(int guessIdx, int turn) {
        guesses[guessIdx].addElim(turn);
    }

    public int indexOfGuess(String guessWord) {
        for(int i = 0; i < NUM_GUESSES; i++) {
            if(guesses[i].getWord().equals(guessWord)) {
                return i;
            }
        }
        return -1;
    }

    public int indexOfAnswer(String answerWord) {
        for(int i = 0; i < NUM_ANSWERS; i++) {
            if(answers[i].getWord().equals(answerWord)) {
                return i;
            }
        }
        return -1;
    }

    public void findDuplicateLetterWords() {
        int max = 3;
            List<String> maxWords = new ArrayList<>();
            for(Guess guess : guesses) {
                Map<Character, Integer> freqs = new HashMap<>();
                for(char c : guess.getWord().toCharArray()) {
                    if(!freqs.containsKey(c)) {
                        freqs.put(c, 1);
                    } else {
                        freqs.put(c, freqs.get(c) + 1);
                    }
                }
                int maxFreq = 0;
                for(int n : freqs.values()) {
                    maxFreq = Math.max(maxFreq, n);
                }
                if(maxFreq > max) {
                    max = maxFreq;
                    maxWords.clear();
                    maxWords.add(guess.getWord());
                }
                else if(maxFreq == max) {
                    maxWords.add(guess.getWord());
                }
            }
            
            System.out.println(maxWords);
    }

    private void printGuess(Guess guess, Answer answer) {
        DetailedHint hint = new DetailedHint(guess, answer);
        SequentialHint hintSequence = new SequentialHint(guess, answer);
        System.out.printf("GUESS: %s | ANSWER: %s | SEQUENCE: %s%n%s", guess, answer, hintSequence, hint);
    }

    private void printProgress(String message, int numerator, int denominator) {
        int percentage = numerator * 100 / denominator;
        int lastPercentage = (numerator - 1) * 100 / denominator;
        if(percentage > lastPercentage) {
            System.out.print("\r" + message + " (" + percentage + "%)");
        }
    }

    public void writeOpenings(String firstWord) throws IOException {
        Guess firstGuess = new Guess(firstWord);
        List<Opening> openings = new ArrayList<>();
        Set<Answer> seenAnswers = new HashSet<>();
        System.out.println("getting openings...");
        for(int i = 0; i < NUM_ANSWERS; i++) {

            if(seenAnswers.contains(answers[i])) continue;
            
            // get a new possible first hint
            DetailedHint firstHint = new DetailedHint(firstGuess, answers[i]);
            SequentialHint sequentialFirstHint = new SequentialHint(firstGuess, answers[i]);

            // get the list of possible answers given the first hint
            List<Answer> possibleAnswers = new ArrayList<>();
            for(Answer answer : answers) {
                if(firstHint.wordIsCompliant(answer)) {
                    possibleAnswers.add(answer);
                    seenAnswers.add(answer);
                }
            }

            // find the best second guess for the list of possible answers
            int mostElims = 0;
            Guess bestSecondGuess = null;
            if(possibleAnswers.size() == 1) {
                bestSecondGuess = new Guess(possibleAnswers.get(0).getWord());
            } else {
                for(Guess secondGuess : guesses) {
                    int numElims = 0;
                    for(Answer realAnswer : possibleAnswers) {
                        DetailedHint secondHint = new DetailedHint(secondGuess, realAnswer);
                        for(Answer fakeAnswer : answers) {
                            if(!secondHint.wordIsCompliant(fakeAnswer)) {
                                numElims++;
                            }
                        }
                    }
                    if(numElims > mostElims) {
                        mostElims = numElims;
                        bestSecondGuess = secondGuess;
                    }
                }
            }

            openings.add(new Opening(
                    sequentialFirstHint, bestSecondGuess, possibleAnswers));
        }
        System.out.println("done");
        Collections.sort(openings);
        
        try(FileWriter fw = new FileWriter("io/" + firstWord + "-openings.rtf")) {
            for(Opening opening : openings) {
                fw.write(opening + "\n");
            }
        }
    }

    // predicts the best next opening by summing guesses' character frequencies in answers
    public List<Guess> predictNextOpeners(int n, Guess... prevGuesses) {
        boolean[] usedChars = new boolean[26];
        for(Guess guess : prevGuesses) {
            for(char c : guess.word.toCharArray()) {
                usedChars[c - 'a'] = true;
            }
        }
        return Stream.of(guesses)
            .filter(g -> !g.containsDuplicates() && !g.overlapsWith(usedChars))
            .sorted(Comparator.comparing(rootAnswerPool::getGuessCharSetFreqs).reversed())
            .limit(n).toList();
    }

    public String[] getWorstWords(int n) {
        List<Map<String, List<String>>> groups = new ArrayList<>(5);
        for(int i = 0; i < 5; i++) {
            Map<String, List<String>> map = new HashMap<>();
            for(Guess guess : guesses) {
                String group = guess.getWord().substring(0, i) + guess.getWord().substring(i+1);
                if(!map.containsKey(group)) map.put(group, new ArrayList<>());
                map.get(group).add(guess.getWord());
            }
            groups.add(map);
        }

        List<List<String>> worstGroups = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            for(List<String> group : groups.get(i).values()) {
                worstGroups.add(group);
            }
        }

        worstGroups = worstGroups.stream()
                .sorted(Comparator.comparing(List<String>::size).reversed())
                .limit(n)
                .toList();
        String[] ret = new String[n];
        for(int i = 0; i < n; i++) {
            ret[i] = worstGroups.get(i).stream().min(Comparator.reverseOrder()).orElse("none");
        }
        return ret;
    }

    public List<Guess> predictBestOpeningWords(int numCandidates) {
        return Stream.of(guesses)
            .filter(guess -> !guess.containsDuplicates())
            .sorted(Comparator.comparing(rootAnswerPool::getGuessScore).reversed())
            .limit(numCandidates).toList();
    }

    public void predictBestOpeningPair(int numCandidates) {
        List<Guess> candidates = predictBestOpeningWords(numCandidates);
        int max = 0;
        Map<DetailedHint, Integer> seenHints = new HashMap<>();
        for(int i = 0; i < numCandidates - 1; i++) {
            Guess guess1 = candidates.get(i);
            boolean[] usedChars = guess1.getVector();
            for(int j = i + 1; j < numCandidates; j++) {
                Guess guess2 = candidates.get(j);
                if(guess2.overlapsWith(usedChars)) continue;
                int elims = 0;
                for(Answer answer : answers) {
                    DetailedHint hint = new DetailedHint(guess1, answer);
                    DetailedHint test = new DetailedHint();
                    test.add(hint);
                    if(!hint.equals(test)) {
                        System.out.println(hint);
                        System.out.println(test);
                    }
                    hint.add(new DetailedHint(guess2, answer));
                    if(!seenHints.containsKey(hint)) {
                        seenHints.put(hint, hint.getNumElims(rootAnswerPool));
                    }
                    elims += seenHints.get(hint);
                }
                double avg = NUM_ANSWERS - ((double) elims / NUM_ANSWERS);
                System.out.print(String.format(
                    "\rThe opening (%s, %s) leaves an average of %,.2f words (%d eliminations)",
                    guess1, guess2, avg, elims));
                
                if(elims > max) {
                    System.out.println();
                    max = elims;
                }
            }
        }
    }

    public void predictBestOpeningTriple(int numCandidates) {
        List<Guess> candidates = predictBestOpeningWords(numCandidates);
        int max = 0;
        Map<DetailedHint, Integer> seenHints = new HashMap<>();
        for(int i = 0; i < numCandidates - 2; i++) {
            Guess guess1 = candidates.get(i);
            boolean[] usedChars = guess1.getVector();
            for(int j = i + 1; j < numCandidates - 1; j++) {
                Guess guess2 = candidates.get(j);
                if(guess2.overlapsWith(usedChars)) continue;
                for(char c : guess2.getWord().toCharArray()) {
                    usedChars[c - 'a'] = true;
                }
                for(int k = j + 1; k < numCandidates; k++) {
                    Guess guess3 = candidates.get(k);
                    if(guess3.overlapsWith(usedChars)) continue;
                    int elims = 0;
                    for(Answer answer : answers) {
                        DetailedHint hint = new DetailedHint(guess1, answer);
                        hint.add(new DetailedHint(guess2, answer));
                        hint.add(new DetailedHint(guess3, answer));
                        if(!seenHints.containsKey(hint)) {
                            seenHints.put(hint, hint.getNumElims(rootAnswerPool));
                        }
                        elims += seenHints.get(hint);
                    }
                    double avg = NUM_ANSWERS - ((double) elims / NUM_ANSWERS);
                    System.out.print(String.format(
                        "\rThe opening (%s, %s, %s) leaves an average of %,.2f words (%d eliminations)",
                        guess1, guess2, guess3, avg, elims));
                    
                    if(elims > max) {
                        System.out.println();
                        max = elims;
                    }
                }
                // reset usedChars vector to only characters from guess1 so it
                // can be reused to find the next non-overlapping second guess
                usedChars = guess1.getVector();
            }
        }
    }

    public void printOpenerInfo(String... openingWords) {
        Guess[] openingGuesses = new Guess[openingWords.length];
        for(int i = 0; i < openingWords.length; i++) {
            openingGuesses[i] = new Guess(openingWords[i]);
        }
        int elims = rootAnswerPool.getGuessesTotalElims(openingGuesses);
        double avg = NUM_ANSWERS - ((double) elims / NUM_ANSWERS);
        System.out.print(String.format(
            "%nThe opening %s leaves an average of %,.2f words (%d eliminations)",
            Arrays.toString(openingWords), avg, elims));
    }

    public static void main(String[] args) throws IOException {
        WordleCalculator calc = new WordleCalculator();

        calc.predictBestOpeningTriple(8000);
        
    }
}
