package leanderk.izou.tts.outputplugin;

import com.gtranslate.Audio;
import com.gtranslate.context.TranslateEnvironment;
import org.intellimate.izou.sdk.Context;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.BreakIterator;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Represents a TTS-Block.
 * This class holds the Data from the TTSOutputPlugins (passed to the OutputPlugin via TTSData) and all the Logic,
 * Buffer etc.
 * Instances are stored and ordered in TTSElementCollection
 */
@SuppressWarnings("UnusedDeclaration")
class TTSElement implements Comparable<TTSElement> {
    private final List<Future<InputStream>> futures = new ArrayList<>();
    private String ID;
    private String words = "";
    private String locale;
    private int priority = Integer.MAX_VALUE;
    private PriorityQueue<TTSElement> before = null;
    private PriorityQueue<TTSElement> after = null;
    private PriorityQueue<TTSElement> dimension = null;
    private ExecutorService threadPool;
    private Context context;

    public TTSElement(String ID, Context context, ExecutorService threadPool) {
        this.ID = ID;
        this.context = context;
        this.threadPool = threadPool;
    }

    public TTSElement(String words, Context context, String locale, String ID, int priority, ExecutorService threadPool) {
        this.words = words;
        this.locale = locale;
        this.ID = ID;
        this.context = context;
        this.priority = Math.abs(priority);
        this.threadPool = threadPool;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public String getWords() {
        return words;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * returns the all InputStreams and CLEARS them.
     *
     * @return a List containing InputStreams
     */
    public LinkedList<InputStream> getInputStreams() {
        LinkedList<InputStream> inputStreams = new LinkedList<>();
        for (Future<InputStream> future : futures) {
            try {
                inputStreams.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                context.getLogger().error("Error while trying to create the TTS-InputStream", e);
            }
        }
        futures.clear();
        return inputStreams;
    }

    public boolean bufferingStarted() {
        if(words != null && !words.isEmpty()) {
            return !futures.isEmpty();
        }
        return true;
    }

    public boolean bufferingFinished() {
        if(words == null || words.isEmpty()) {
            return true;
        }

        boolean finished = true;
        if (futures.isEmpty()) return false;
        for (Future future : futures) {
            if (!future.isDone()) finished = false;
        }
        return finished;
    }

    public PriorityQueue<TTSElement> getBeforeQueue() {
        if (before == null) {
            before = new PriorityQueue<>();
        }
        return before;
    }

    public void addBefore(TTSElement element) {
        if (before == null) {
            before = new PriorityQueue<>();
        }
        element.setDimension(getDimension());
        before.add(element);
    }

    public PriorityQueue<TTSElement> getAfter() {
        if (after == null) {
            after = new PriorityQueue<>();
        }
        return after;
    }

    public void addAfter(TTSElement element) {
        if (after == null) {
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = Math.abs(priority);
    }

    /**
     * buffers the speech generation.
     * The speech will be generated online and send back as audio. This method buffers the audio.
     *
     * @param callback will be called after finishing
     */
    public void buffer(Callback callback) {
        context.getLogger().debug("buffering: " + getID());
        if (words.length() > 100) {
            handleLongText(words, callback);
        } else {
            futures.add(threadPool.submit(new BufferWorker(words, locale, callback)));
        }
    }

    /**
     * handles long texts (over 100 characters).
     * The method breaks them into the sentences
     *
     * @param text     the text to buffer
     * @param callback will be called after finishing
     */
    private void handleLongText(String text, Callback callback) {
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.getDefault());
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String substring = text.substring(start, end);
            if (substring.length() > 100) {
                bufferLongSentences(substring, callback);
            } else {
                futures.add(threadPool.submit(new BufferWorker(substring, locale, callback)));
            }
        }
    }

    /**
     * If the sentences are again over 100 characters, this method will be called.
     * It splits the sentence in parts <= 100 characters.
     *
     * @param sentence the text to buffer
     * @param callback will be called after finishing
     */
    private void bufferLongSentences(String sentence, Callback callback) {
        BreakIterator iterator = BreakIterator.getWordInstance(Locale.getDefault());
        iterator.setText(sentence);
        StringBuilder wordsStringBuilder = new StringBuilder();
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String substring = sentence.substring(start, end);
            if (wordsStringBuilder.length() + substring.length() > 100) {
                futures.add(threadPool.submit(new BufferWorker(wordsStringBuilder.toString(), locale, callback)));
                wordsStringBuilder.setLength(0);
                wordsStringBuilder.append(substring);
            } else {
                wordsStringBuilder.append(substring);
            }
        }
        if (wordsStringBuilder.length() != 0)
            futures.add(threadPool.submit(new BufferWorker(wordsStringBuilder.toString(), locale, callback)));
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     *
     * @param o the object to compare to
     * @return negative if less, zero if equal or positive if greater than o
     */
    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(TTSElement o) {
        if (o == null) return 1;
        if (this.getPriority() < o.getPriority()) {
            return -1;
        }
        if (this.getPriority() > o.getPriority()) {
            return 1;
        }
        return 0;
    }

    public interface Callback {
        abstract void callback();
    }

    /**
     * A little helper class, which downloads the TTS concurrently.
     */
    private class BufferWorker implements Callable<InputStream> {
        private final String text;
        private final String languageLocale;
        private final Callback callback;

        public BufferWorker(String text, String languageLocale, Callback callback) {
            this.text = text;
            this.languageLocale = languageLocale;
            this.callback = callback;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         *
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public InputStream call() throws Exception {
            Audio audio = Audio.getInstance();
            InputStream audioIs = null;
            try {
                //gor testing
                //audioIs = audio.getAudio(text, languageLocale);
                URL url = new URL(TranslateEnvironment.getSystemProperty("google.translate.audio") + "q=" + URLEncoder.encode(text, "UTF-8") + "&tl=" + languageLocale);
                URLConnection urlConn = url.openConnection();
                urlConn.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
                InputStream audioSrc = urlConn.getInputStream();
                audioIs =  new BufferedInputStream(audioSrc);
            } catch (Exception e) {
                context.getLogger().error("an error occurred while trying to create InputStream for " + getID() +
                        " which text:<" + text + "> and locale:<" + locale + ">", e);
                throw e;
            }
            if (callback != null) {
                int running = 0;
                for (Future future : futures) {
                    if (!future.isDone()) running++;
                }
                //last remaining
                if (running == 1) callback.callback();
            }
            return audioIs;
        }
    }
}
