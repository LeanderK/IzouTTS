package leanderk.izou.tts.outputextension;

/**
 * Holds the Data for TTS.
 * To get an Instance, use the factory method {@link #createTTSData(String, String, int, String)} or
 * {@link #createTTSData(String, String, int, String, String, String)}.<br><br>
 * It contains:
 * <ul>
 * <li>String words, which holds the message to be spoken
 * <li>int priority, which is used to place this particular message in a list of messages.<br>
 *      0 is max priority, maxInt least priority<br>
 *      AVOID 0. It is preserved for really important parts!
 * <li>String sourceID, which holds the ID where the message was generated
 * <li>String beforeID, you can use this to place your message before another message
 * <li>String afterID, you can use this to place your message after another message
 * </ul>
 */
@SuppressWarnings("UnusedDeclaration")
public class TTSData {
    private String words;
    private String locale;
    //0 most priority, maxInt least priority
    private int priority;
    private String sourceID;
    private String beforeID;
    private String afterID;

    private TTSData(String words, String locale, int priority, String sourceID, String beforeID, String afterID) {
        this.words = words;
        this.locale = locale;
        this.priority = Math.abs(priority);
        this.sourceID = sourceID;
        this.beforeID = beforeID;
        this.afterID = afterID;
    }


    private TTSData(String words, String locale, int priority, String sourceID) {
        this.words = words;
        this.locale = locale;
        this.priority = priority;
        this.sourceID = sourceID;
    }

    /**
     * factory method for TTSData.
     * for further information about the parameters, see documentation of the class
     * @param words the message, may be null or empty
     * @param locale the locale code, must not be null or empty
     * @param priority the priority
     * @param sourceID the sourceID, must not be null or empty
     * @param beforeID the beforeID, may be null or empty
     * @param afterID the afterID, may be null or empty
     * @return TTSData an instance of null
     */
    public static TTSData createTTSData(String words, String locale, int priority, String sourceID, String beforeID, String afterID) {
        if(sourceID == null || sourceID.trim().isEmpty() || locale == null) return null;
        return new TTSData(words, locale, priority, sourceID, beforeID, afterID);
    }

    /**
     * factory method for TTSData.
     * for further information about the parameters, see documentation of the class
     * @param words the message, may be null or empty
     * @param locale the locale code, must not be null or empty
     * @param priority the priority
     * @param sourceID the sourceID, must not be null or empty
     * @return TTSData an instance of null
     */
    public static TTSData createTTSData(String words, String locale, int priority, String sourceID) {
        if(sourceID == null || sourceID.trim().isEmpty() || locale == null) return null;
        return new TTSData(words, locale, priority, sourceID);
    }

    public String getLocale() {
        return locale;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = Math.abs(priority);
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String sourceID) {
        if(sourceID == null || sourceID.trim().isEmpty()) return;
        this.sourceID = sourceID;
    }

    public String getBeforeID() {
        return beforeID;
    }

    public void setBeforeID(String beforeID) {
        this.beforeID = beforeID;
    }

    public String getAfterID() {
        return afterID;
    }

    public void setAfterID(String afterID) {
        this.afterID = afterID;
    }
}
