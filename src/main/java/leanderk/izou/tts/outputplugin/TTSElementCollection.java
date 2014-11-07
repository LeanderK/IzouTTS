package leanderk.izou.tts.outputplugin;

import leanderk.izou.tts.outputextension.TTSData;
import sun.awt.image.ImageWatched;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * Created by LeanderK on 06/11/14.
 */
public class TTSElementCollection {
    private HashMap<String, TTSElement> hashMap = new HashMap<>();
    private PriorityQueue<TTSElement> queue = new PriorityQueue<>();

    public void addTTSElement(TTSData data) {
        TTSElement element = null;
        if(hashMap.containsKey(data.getSourceID())) {
            element = hashMap.get(data.getSourceID());
            element.setPriority(data.getPriority());
            element.setWords(data.getWords());
        }
        else {
            element = new TTSElement(data.getWords(), data.getSourceID(), data.getPriority());
            hashMap.put(data.getSourceID(), element);
        }
        if(data.getAfterID() != null || !data.getAfterID().isEmpty()) {
            if(hashMap.containsKey(data.getAfterID())) {
                TTSElement parent = hashMap.get(data.getAfterID());
                parent.addAfter(element);
            } else {
                TTSElement parent = new TTSElement(data.getAfterID());
                parent.addAfter(element);
                hashMap.put(parent.getID(), parent);
                queue.add(parent);
            }
        } else if(data.getBeforeID() != null || !data.getBeforeID().isEmpty()) {
            if(hashMap.containsKey(data.getAfterID())) {
                TTSElement parent = hashMap.get(data.getAfterID());
                parent.addBefore(element);
            } else {
                TTSElement parent = new TTSElement(data.getAfterID());
                parent.addBefore(element);
                hashMap.put(parent.getID(), parent);
                queue.add(parent);
            }
        } else {
            element.setDimension(queue);
            queue.add(element);
        }

    }

    public HashMap<String, TTSElement> getHashMap() {
        return hashMap;
    }

    public PriorityQueue<TTSElement> getQueue() {
        return queue;
    }

    public void clear() {
        queue.clear();
        hashMap.clear();
    }

    public LinkedList<TTSElement> getFullCollectionAsList() {
        LinkedList<TTSElement> temp = new LinkedList<>();
        for (TTSElement element : queue) {
            temp.addAll(getFullCollectionRecursive(element));
        }
        return temp;
    }

    private LinkedList<TTSElement> getFullCollectionRecursive(TTSElement element) {
        LinkedList<TTSElement> temp = new LinkedList<>();
        for (TTSElement children : element.getBeforeQueue()) {
            temp.addAll(getFullCollectionRecursive(children));
        }
        temp.add(element);
        for (TTSElement children : element.getAfter()) {
            temp.addAll(getFullCollectionRecursive(children));
        }
        return temp;
    }
}
