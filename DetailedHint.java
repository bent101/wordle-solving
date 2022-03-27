import java.util.*;

public class DetailedHint {

    static class DuplicateCharacter {
        char c;
        Set<Integer> greenIndeces;
        Set<Integer> yellowIndeces;
        boolean hasGray;

        /**
         * Constuctor for a hint centered around a repeating character
         * @param c the character
         * @param greenIndeces the indeces at which the character must be
         * @param yellowIndeces the indeces at which the character can not be
         * @param hasGray true if at least one of the characters was given a gray hint, false otherwise
         */
        private DuplicateCharacter(char c, Set<Integer> greenIndeces, Set<Integer> yellowIndeces, boolean hasGray) {
            this.c = c;
            this.greenIndeces = greenIndeces;
            this.yellowIndeces = yellowIndeces;

            // If any of the duplicate letters are gray, there must be exactly G + Y occurences; otherwise, there
            // are greater than or equal to G + Y occurences
            this.hasGray = hasGray;
        }

        private boolean wordIsCompliant(String word) {
            // indeces at which the character could still be (starts as everything except yellows)
            Set<Integer> availableIndeces = new HashSet<>(5 - yellowIndeces.size());
            for(int i = 0; i < 5; i++) {
                if(!yellowIndeces.contains(i)) {
                    availableIndeces.add(i);
                }
            }

            for(int i : greenIndeces) {
                if(word.charAt(i) == c) availableIndeces.remove(i);
                else return false;
            }

            int count = 0; // counts non-green occurences of c
            if(hasGray) {
                // return true only if count equals the number of yellows
                for(int i : availableIndeces) {
                    if(word.charAt(i) == c) {
                        count++;
                        if(count > yellowIndeces.size()) return false;
                    }
                }
                return count == yellowIndeces.size();
            } else {
                // return true once the count is high enough, false otherwise
                for(int i : availableIndeces) {
                    if(word.charAt(i) == c) {
                        count++;
                        if(count == yellowIndeces.size()) return true;
                    } 
                }
                return false;
            }
        }
    
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(c + " occurs ");
            if(!hasGray) sb.append("at least ");
            sb.append(greenIndeces.size() + yellowIndeces.size() + " times\n");
            sb.append("    It must be at these indeces: ");
            for(int i : greenIndeces) sb.append(i + " ");
            sb.append("\n");
            sb.append("    It can't be at these indeces: ");
            for(int i : yellowIndeces) sb.append(i + " ");
            sb.append("\n");

            return sb.toString();
        }
    }

    // set of all invalid characters
    private Set<Character> grayChars;
    
    // map yellow characters to all of their invalid indeces
    private Map<Character, ArrayList<Boolean>> yellowChars;

    // set of all character/index pairs
    private Map<Integer, Character> greenChars;

    // set of all duplicate characters
    private Set<DuplicateCharacter> duplicateChars;


    public DetailedHint() {
        greenChars = new HashMap<>();
        yellowChars = new HashMap<>();
        grayChars = new HashSet<>();
        duplicateChars = new HashSet<>();
    }

    public DetailedHint(Guess guess, Answer answer) {
        this();

        // letterIsHinted[i] = the i-th letter in the guess has been assigned a green or yellow hint
        boolean[] letterIsHinted = new boolean[5];

        // letterIsUsed[i] = the i-th letter in the answer has been used for a green or yellow hint
        boolean[] letterIsUsed = new boolean[5];

        // green chars
        for(int i = 0; i < 5; i++) {
            if(guess.charAt(i) == answer.charAt(i)) {
                greenChars.put(i, guess.charAt(i));
                letterIsHinted[i] = true;
                letterIsUsed[i] = true;
            }
        }

        // yellow chars
        for(int i = 0; i < 5; i++) {
            if(letterIsHinted[i]) continue;
            for(int j = 0; j < 5; j++) {
                if(letterIsUsed[j]) continue;
                char c = guess.charAt(i);
                if(c == answer.charAt(j)) {
                    // if the character is already yellow, add the new index
                    // otherwise, make a new entry
                    if(yellowChars.containsKey(c)) {
                        yellowChars.get(c).set(i, Boolean.TRUE);
                    } else {
                        yellowChars.put(c, new ArrayList<>(5));
                        for(int k = 0; k < 5; k++) {
                            yellowChars.get(c).add(Boolean.FALSE);
                        }
                        yellowChars.get(c).set(i, Boolean.TRUE);
                    }
                    
                    letterIsHinted[i] = true;
                    letterIsUsed[j] = true;
                }
            }
        }

        // gray chars
        for(int i = 0; i < 5; i++) {
            if(!letterIsHinted[i]) {
                grayChars.add(guess.charAt(i));
            }
        }

        // deal with duplicate letters
        Set<Character> seen = new HashSet<>();
        Set<Character> duplicates = new HashSet<>();
        for(int i = 0; i < 5; i++) {
            char c = guess.charAt(i);
            if(seen.contains(c)) duplicates.add(c);
            else seen.add(c);
        }

        // don't give special treatment to the duplicate character if
        // it is not in the answer
        duplicates.removeIf(
            c -> !greenChars.values().contains(c) && !yellowChars.containsKey(c)
        );

        for(char c : duplicates) {
            Set<Integer> greenIndeces = new HashSet<>();
            Set<Integer> yellowIndeces = new HashSet<>();

            // green chars
            Iterator<Map.Entry<Integer, Character>> iterator = greenChars.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<Integer, Character> entry = iterator.next();
                if(entry.getValue().equals(c)) {
                    greenIndeces.add(entry.getKey());
                    iterator.remove();
                }
            }

            // yellow chars
            if(yellowChars.containsKey(c)) {
                ArrayList<Boolean> indexIsInvalid = yellowChars.get(c);
                for(int i = 0; i < 5; i++) {
                    if(Boolean.TRUE.equals(indexIsInvalid.get(i))) {
                        yellowIndeces.add(i);
                    }
                }
                yellowChars.remove(c);
            }

            // gray chars
            boolean hasGray = grayChars.contains(c);
            grayChars.remove(c);
            duplicateChars.add(new DuplicateCharacter(c, greenIndeces, yellowIndeces, hasGray));
        }
    }

    public DetailedHint(SequentialHint sequentialHint) {
        // TODO: finish implementation (for user inputted hints)
    }

    // copy constructor to keep a prefix sum of hints for hard mode
    public DetailedHint(DetailedHint other) {
        this();
        this.grayChars.addAll(other.grayChars);
        this.yellowChars.putAll(other.yellowChars);
        this.greenChars.putAll(other.greenChars);
    }

    public boolean wordIsCompliant(WordleWord wordleWord) {
        String word = wordleWord.getWord();
        // green chars first
        for(Map.Entry<Integer, Character> entry : greenChars.entrySet()) {
            if(word.charAt(entry.getKey()) != entry.getValue()) {
                return false;
            }
        }

        // yellow chars
        for(Map.Entry<Character, ArrayList<Boolean>> entry : yellowChars.entrySet()) {
            int index = word.indexOf(entry.getKey());
            if(index == -1 || Boolean.TRUE.equals(entry.getValue().get(index))) {
                return false;
            }
        }

        // gray chars
        for(Character c : grayChars) {
            for(int i = 0; i < 5; i++) {
                if(word.charAt(i) == c) return false;
            }
        }

        // duplicate chars
        for(DuplicateCharacter c : duplicateChars) {
            if(!c.wordIsCompliant(word)) return false;
        }

        return true;
    }

    /**
     * helper method to promote yellow hints with 4 invalid indeces
     * to green hints
     */
    private void promoteYellows() {
        Iterator<Character> iterator = yellowChars.keySet().iterator();
        while(iterator.hasNext()) {
            Character key = iterator.next();
            ArrayList<Boolean> thisList = this.yellowChars.get(key);
            // promote the yellow hint to a green hint if the combined boolean array
            // has only one false
            int lastValidIndex = -1; // becomes the only valid index if there is only one 
            boolean promote = true;
            for(int i = 0; i < 5 && promote; i++) {
                if(Boolean.FALSE.equals(thisList.get(i))) {
                    if(lastValidIndex != -1) promote = false;
                    else lastValidIndex = i;
                }
            }
            if(promote) {
                greenChars.put(lastValidIndex, key);
                iterator.remove();
            }
        }
    }

    public void add(DetailedHint other) {
        // green chars
        this.greenChars.putAll(other.greenChars);

        // yellow chars
        for(Map.Entry<Character, ArrayList<Boolean>> entry : other.yellowChars.entrySet()) {
            Character c = entry.getKey();
            ArrayList<Boolean> otherList = entry.getValue();
            if(this.yellowChars.containsKey(c)) {
                ArrayList<Boolean> thisList = this.yellowChars.get(c);
                for(int i = 0; i < 5; i++) {
                    if(Boolean.FALSE.equals(thisList.get(i)) && Boolean.TRUE.equals(otherList.get(i))) {
                        thisList.set(i, true);
                    }
                }
            } else {
                this.yellowChars.put(c, otherList);
            }

        }

        promoteYellows();

        // gray chars
        this.grayChars.addAll(other.grayChars);

        //TODO: add duplicate characters and promote them to greens if needed

    }

    public int getNumElims(AnswerPool answerPool) {
        int count = 0;
        for(Answer answer : answerPool.getAnswers()) {
            if(!wordIsCompliant(answer)) count++;
        }
        return count;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(obj.getClass() != this.getClass()) return false;
        DetailedHint other = (DetailedHint) obj;
        
        return this.greenChars.equals(other.greenChars) &&
        this.yellowChars.equals(other.yellowChars) &&
        this.grayChars.equals(other.grayChars) &&
        this.duplicateChars.equals(other.duplicateChars);
    }

    @Override
    public int hashCode() {
        return Objects.hash(greenChars, yellowChars, grayChars, duplicateChars);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(!grayChars.isEmpty()) {
            sb.append("Gray hints:\n");
            for(Character c : grayChars) {
                sb.append(c + " ");
            }
            sb.append("\n");
        }
        
        if(!yellowChars.isEmpty()) {
            sb.append("Yellow hints:\n");
            for(Map.Entry<Character, ArrayList<Boolean>> entry : yellowChars.entrySet()) {
                ArrayList<Integer> invalidIndeces = new ArrayList<>();
                for(int i = 0; i < entry.getValue().size(); i++) {
                    if(Boolean.TRUE.equals(entry.getValue().get(i))) {
                        invalidIndeces.add(i);
                    }
                }
    
                switch(invalidIndeces.size()) {
                    case 0: sb.append("Error: \"" + entry.getKey() + "\" should have at least one invalid index");
                    break;
                    case 1: sb.append("\"" + entry.getKey() + "\" can't be at index " + invalidIndeces.get(0));
                    break;
                    default:
                    sb.append("\"" + entry.getKey() + "\" can't be at indeces ");
                    for(int i = 0; i < invalidIndeces.size() - 1; i++) {
                        sb.append(invalidIndeces.get(i) + ", ");
                    }
                    sb.append(invalidIndeces.get(invalidIndeces.size() - 1));
                }
                sb.append("\n");
            }
        }

        if(!greenChars.isEmpty()) {
            sb.append("Green hints:\n");
            for(Map.Entry<Integer, Character> entry : greenChars.entrySet()) {
                sb.append(entry.getValue() + " is at index " + entry.getKey() + "\n");
            }
        }
        
        if(!duplicateChars.isEmpty()) {
            for(DuplicateCharacter c : duplicateChars) {
                sb.append(c);
            }
        }
        
        return sb.toString();
    }
}
