package experiment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JLabel;

import enums.FileExt;
import enums.FileName;
import enums.FilePath;
import enums.Key;
import utilities.MyUtilities;

public class WordManager {
    private static final String DEFAULT_WORD = "test"; // ced, frazzled, test, calumnies, atherosclerosis --- check that A doesn't trigger when hit enter.....
    private static ArrayList<WordObserver> OBSERVERS = new ArrayList<WordObserver>();
    private final int DICTIONARY_SIZE = 118619;
    private boolean isDefault = false;
    private Queue<String> wordList = new LinkedList<String>();
    private int currentLetter = 0;
    private String answer = "";
    
    public WordManager() {
        setDefault();
    }
    
    public void setDefault() {
        wordList.clear();
        wordList.add(DEFAULT_WORD);
        currentLetter = 0;
        isDefault = true;
        notifyListenersWordSet();
    }
    
    public boolean isDefault() {
        return isDefault;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public char currentLetter() {
        if(currentLetter < answer.length()) {
            return Key.VK_BACK_SPACE.getValue();
        } else if(currentLetter == currentWord().length()) {
            return Key.VK_ENTER.getValue();
        } else {
            return currentWord().charAt(currentLetter);
        }
    }
    
    public Key currentLetterAsKey() {
        if(currentLetter < answer.length()) {
            return Key.VK_BACK_SPACE;
        } else if(currentLetter == currentWord().length()) {
            return Key.VK_ENTER;
        } else {
            return Key.getByValue(currentWord().charAt(currentLetter));
        }
    }
    
    public String currentWord() {
        if(isValid()) {
            return wordList.peek();
        } else {
            isDefault = true;
            return DEFAULT_WORD;
        }
    }
    
    public boolean isValid() {
        return !wordList.isEmpty();
    }
    
    public void nextWord() {
        if(!isDefault && isValid()) {
            wordList.remove();
            if(isValid()) {
                notifyListenersWordSet();
            }
        }
    }
    
    public boolean isMatch() {
        if(!wordList.isEmpty() && currentWord().equals(answer)) {
            return true;
        }
        return false;
    }
    
    public void loadTutorialWords() {
        wordList.clear();
        try {
            isDefault = false;
            wordList.addAll(MyUtilities.FILE_IO_UTILITIES.readListFromFile(FilePath.DOCS.getPath(), FileName.TUTORIAL.getName() + FileExt.DICTIONARY.getExt()));
            currentLetter = 0;
            if(isValid()) {
                notifyListenersWordSet();
            }
        } catch (IOException e) {
            setDefault();
            System.out.println("An error occured while trying to load the words.");
            e.printStackTrace();
        }
    }

    public void loadWords(int reservoirSize) {
        wordList.clear();
        try {
            isDefault = false;
            wordList.addAll(MyUtilities.FILE_IO_UTILITIES.reservoirSampling(reservoirSize, DICTIONARY_SIZE,
                    FilePath.DOCS.getPath(), FileName.DICTIONARY.getName() + FileExt.DICTIONARY.getExt()));
            currentLetter = 0;
            if(isValid()) {
                notifyListenersWordSet();
            }
        } catch (IOException e) {
            setDefault();
            System.out.println("An error occured while trying to load the words.");
            e.printStackTrace();
        }
    }
    
    public void paintLetters(JLabel wordLabel, JLabel answerLabel) {
        String word = currentWord();
        int matchIndex = -1;
        for(int i = 0; i < word.length() && i < answer.length(); i++) {
            if(word.charAt(i) == answer.charAt(i)) {
                matchIndex = i;
            } else {
                break;
            }
        }
        if(matchIndex < word.length()) {
            currentLetter = matchIndex + 1;
            notifyListenersLetterIndexChanged();
        }
        if(matchIndex > -1) {
            wordLabel.setText("<html><nobr><font color=green>" + word.substring(0, matchIndex + 1) + "</font>" + word.substring(matchIndex + 1) + "</nobr></html>");
        } else {
            wordLabel.setText("<html><nobr>" + word + "</nobr></html>");
        }
        answerLabel.setText("<html><nobr><font color=green>" + answer.substring(0, matchIndex + 1) + "</font><font color=red>" + answer.substring(matchIndex + 1) + "</font></nobr></html>");
    }
    
    public static void registerObserver(WordObserver observer) {
        if(OBSERVERS.contains(observer)) {
            return;
        }
        OBSERVERS.add(observer);
    }
    
    public static void removeObserver(WordObserver observer) {
        OBSERVERS.remove(observer);
    }

    protected void notifyListenersWordSet() {
        for(WordObserver observer : OBSERVERS) {
            observer.wordSetEventObserved(currentWord());
        }
    }
    
    protected void notifyListenersLetterIndexChanged() {
        for(WordObserver observer : OBSERVERS) {
            observer.currentLetterIndexChangedEventObservered(currentLetter, currentLetterAsKey());
        }
    }
}
