import java.util.*;

public class WordleWord {
    private static final String INVALID_LENGTH =
            "The length of a Wordle word must be five";
    private static final String INVALID_LETTERS = 
            "The word must contain only lowercase letters";
            
    protected String word;
    private boolean containsDuplicates;
    private Map<Character, Integer> charFreqs;

    public WordleWord(String word) {
        if(word.length() != 5) {
            throw new IllegalArgumentException(INVALID_LENGTH);
        }
        this.word = word;

        containsDuplicates = false;
        charFreqs = new HashMap<>();
        for(char c : word.toCharArray()) {
            if(c < 'a' || c > 'z') {
                throw new IllegalArgumentException(INVALID_LETTERS);
            }
            if(charFreqs.put(c, charFreqs.getOrDefault(c, 0) + 1) != null) {
                containsDuplicates = true;
            }
        }
    }

    public char charAt(int index) {
        return word.charAt(index);
    }

    public boolean containsDuplicates() {
        return containsDuplicates;
    }

    public boolean containsChar(char c) {
        return charFreqs.containsKey(c);
    }

    /**
     * Returns the occurences of the given character in this word
     * @param c the character
     * @return the number of occurences
     */
    public int charFreq(char c) {
        return charFreqs.getOrDefault(c, 0);
    }

    public Map<Character, Integer> getCharFreqs() {
        return charFreqs;
    }

    public boolean[] getVector() {
        boolean[] ret = new boolean[26];
        for(char c : word.toCharArray()) ret[c - 'a'] = true;
        return ret;
    }

    public String getWord() {
        return word;
    }

    @Override
    public String toString() {
        return word;
    }

    // @Override
    // public boolean equals(Object obj) {
    //     if(obj == null || this.getClass() != obj.getClass())
    //         return false;
    //     return this.word.equals(((WordleWord) obj).getWord());
    // }

    // @Override
    // public int hashCode() {
    //     return Objects.hash(word);
    // }
}