package leanderk.izou.tts.outputplugin;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
* Created by LeanderK on 06/11/14.
*/
public class TTSElement implements Comparable<TTSElement>{
    private String ID;
    private String words;
    private int priority = Integer.MAX_VALUE;
    private PriorityQueue<TTSElement> before = null;
    private PriorityQueue<TTSElement> after = null;
    private PriorityQueue<TTSElement> dimension = null;
    LinkedList<InputStream> sounds = null;

    public TTSElement(String ID) {
        this.ID = ID;
    }

    public TTSElement(String words, String ID, PriorityQueue<TTSElement> dimension) {
        this.words = words;
        this.ID = ID;
        this.dimension = dimension;
    }

    public TTSElement(String words, String ID, int priority) {
        this.words = words;
        this.ID = ID;
        this.priority = Math.abs(priority);
    }

    public boolean isFullSentence() {
        String temp = String.valueOf(words.trim().charAt(words.length() - 1));
        return temp.equals(".");
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public PriorityQueue<TTSElement> getBeforeQueue() {
        if(before == null) {
            before = new PriorityQueue<>();
        }
        return before;
    }

    public void addBefore(TTSElement element) {
        if(before == null) {
            before = new PriorityQueue<>();
        }
        element.setDimension(getDimension());
        before.add(element);
    }

    public LinkedList<InputStream> getSounds() {
        return sounds;
    }

    public void addSound(InputStream stream) {
        sounds.add(stream);
    }

    public PriorityQueue<TTSElement> getAfter() {
        if(after == null) {
            after = new PriorityQueue<>();
        }
        return after;
    }

    public void addAfter(TTSElement element) {
        if(after == null) {
            after = new PriorityQueue<>();
        }
        element.setDimension(getDimension());
        after.add(element);
    }

    public PriorityQueue<TTSElement> getDimension() {
        return dimension;
    }

    public void setDimension(PriorityQueue<TTSElement> dimension) {
        this.dimension = dimension;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = Math.abs(priority);
    }

    @Override
    public int compareTo(TTSElement o) {
        if (this.getPriority() < o.getPriority())
        {
            return 1;
        }
        if (this.getPriority() > o.getPriority())
        {
            return -1;
        }
        return 0;
    }
}
