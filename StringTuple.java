import java.util.*;

public class StringTuple {
    List<String> strings;
    int n;

    public StringTuple(String... strings) {
        this.strings = Arrays.asList(strings);
        n = strings.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < n - 2; i++) {
            sb.append(strings.get(i) + ", ");
        }
        sb.append(strings.get(n - 1));
        return sb.toString();
    }
}
