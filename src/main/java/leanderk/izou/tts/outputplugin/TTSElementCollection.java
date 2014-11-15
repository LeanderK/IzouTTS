package leanderk.izou.tts.outputplugin;

import leanderk.izou.tts.outputextension.TTSData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;

/**
 * This class holds all the instances of TTSElement.
 * It is also responsible for ordering them.
 */
class TTSElementCollection {
    private final HashMap<String, TTSElement> hashMap = new HashMap<>();
    private final PriorityQueue<TTSElement> queue = new PriorityQueue<>();
    private ExecutorService threadPool;

    TTSElementCollection(ExecutorService threadPool) {
        this.threadPool = threadPool;
    }

    public void addTTSElement(TTSData data) {
        if(data == null) return;
        TTSElement element;
        if(hashMap.containsKey(data.getSourceID())) {
            element = hashMap.get(data.getSourceID());
            element.setPriority(data.getPriority());
            element.setWords(data.getWords());
        }
        else {
            element = new TTSElement(data.getWords(), data.getLocale(), data.getSourceID(), data.getPriority(), threadPool);
            hashMap.put(data.getSourceID(), element);
        }
        if(data.getAfterID() != null || data.getBeforeID() != null) {
            String parentID;
            if(data.getAfterID() != null) parentID = data.getAfterID();
            else parentID = data.getBeforeID();

            TTSElement parent;
            if(hashMap.containsKey(parentID)) {
                parent = hashMap.get(parentID);
            } else {
                parent = new TTSElement(parentID);
                parent.setDimension(queue);
                hashMap.put(parent.getID(), parent);
                queue.add(parent);
            }


            if(data.getAfterID() != null) parent.addAfter(element);
            else parent.addBefore(element);

        } else {
            element.setDimension(queue);
            queue.add(element);
        }

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
