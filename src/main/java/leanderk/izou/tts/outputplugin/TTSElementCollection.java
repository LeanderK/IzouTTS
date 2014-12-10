package leanderk.izou.tts.outputplugin;

import intellimate.izou.system.Context;
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
    private Context context;
    private ExecutorService threadPool;

    TTSElementCollection(ExecutorService threadPool, Context context) {
        this.threadPool = threadPool;
        this.context = context;
    }

    /**
     * adds an TTSElement
     *
     * @param data the element to add
     */
    public void addTTSElement(TTSData data) {
        if (data == null) return;
        TTSElement element;
        //check if it is already referenced
        if (hashMap.containsKey(data.getSourceID())) {
            //retrieve the empty reference
            element = hashMap.get(data.getSourceID());
            element.setPriority(data.getPriority());
            element.setWords(data.getWords());
            element.setLocale(data.getLocale());
        } else {
            //create new Element
            element = new TTSElement(data.getWords(), context, data.getLocale(), data.getSourceID(), data.getPriority(), threadPool);
            hashMap.put(data.getSourceID(), element);
        }
        //it is referencing to other element
        if (data.getAfterID() != null || data.getBeforeID() != null) {
            if(queue.contains(element)) queue.remove(element);
            String parentID;
            if (data.getAfterID() != null) parentID = data.getAfterID();
            else parentID = data.getBeforeID();

            TTSElement parent;
            //check if the element referencing to is already existing
            if (hashMap.containsKey(parentID)) {
                parent = hashMap.get(parentID);
            } else {
                parent = new TTSElement(parentID, context, threadPool);
                parent.setPriority(element.getPriority());
                parent.setDimension(queue);
                hashMap.put(parent.getID(), parent);
                queue.add(parent);
            }


            if (data.getAfterID() != null) parent.addAfter(element);
            else parent.addBefore(element);

        } else {
            element.setDimension(queue);
            if(!queue.contains(element)) queue.add(element);
        }

    }

    /**
     * clears the Collection
     */
    public void clear() {
        queue.clear();
        hashMap.clear();
    }

    /**
     * returns all the Elements as a List
     *
     * @return a list containing all the elements
     */
    public LinkedList<TTSElement> getFullCollectionAsList() {
        LinkedList<TTSElement> temp = new LinkedList<>();
        for (TTSElement element : queue) {
            temp.addAll(getFullCollectionRecursive(element));
        }
        return temp;
    }

    /**
     * this is the actual method for generating the List.
     * it calls itself recursively
     *
     * @param element the element to return the List
     * @return a LinkedList containing all the elements
     */
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
