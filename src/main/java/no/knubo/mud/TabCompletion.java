package no.knubo.mud;

import java.util.Arrays;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class TabCompletion {

    private ListIterator<String> iterator;
    private String word;
    private String inputText;
    private int start;
    private int end;
    private int caretPositon;
    private String lastPickedWord;

    public TabCompletion(String text, String word, String inputText, int caretPositon, int start, int end) {
        this.word = word;
        this.inputText = inputText;
        this.start = start;
        this.end = end;
        this.caretPositon = caretPositon;
        String lowerWord = word.toLowerCase();
        iterator = Arrays.asList(text.split("\\W"))
                .stream().filter(t -> t.length() > 0).filter(t -> t.toLowerCase().startsWith(lowerWord))
                .distinct().collect(Collectors.toList()).listIterator();
    }

    public int getCaretPositon() {
        return start+lastPickedWord.length();
    }

    public String getNextSuggestion() {
        lastPickedWord = getNextWord();
        return inputText.substring(0, start) + lastPickedWord + inputText.substring(end + 1);
    }

    public String getPreviousSuggestion() {
        lastPickedWord = getPreviousWord();
        return inputText.substring(0, start) + lastPickedWord + inputText.substring(end + 1);
    }

    public String getNextWord() {
        if (iterator.hasNext()) {
            return iterator.next();
        }

        while (iterator.hasPrevious()) {
            iterator.previous();
        }

        return word;
    }

    public String getPreviousWord() {
        if (iterator.hasPrevious()) {
            return iterator.previous();
        }

        while (iterator.hasNext()) {
            iterator.next();
        }

        return word;

    }
}
