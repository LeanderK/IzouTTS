package leanderk.izou.tts.outputextension;

import intellimate.izou.addon.PropertiesContainer;
import intellimate.izou.events.Event;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.system.Context;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The OutputExtension used for generating the TTS-Sentences.
 * <br>
 * <br>
 * Usage is fairly simple, just extend it and fill the method generateSentence() and canGenerateForLanguage() with Logic.
 * If an Event was fired and the Extension gets activated, first it will ask whether it can actually generate for this
 * language (not automated jet, may be in future versions).<br>
 * there are already methods, which help you with generating the sentences. If your code comes to the point, where you
 * want to generate the String, use the getWords(...) method. It will choose a String from the properties List.<br>
 * IT IS VERY IMPORTANT THAT THE GENERATED SENTENCES HAVE ALL CORRECT ENDINGS(DOTS).<br>
 * <p>
 * The details are more complicated:<br>
 * First, you have to pass an ID.
 * Then the method will get all the keys from the properties-file, search for the following pattern:<br><br>
 * locale_category_id(_index)<br>
 * <br>
 * <table summary="">
 *   <tr>
 *      <td>locale</td><td>= the current locale</td>
 *   </tr>
 *   <tr>
 *      <td>category</td><td>= s for sentence of p it part of a sentence</td>
 *   </tr>
 *   <tr>
 *      <td>id</td><td>= the id of the key</td>
 *   </tr>
 *   <tr>
 *      <td>index</td><td>= ta unique number if there are multiple entries</td>
 *   </tr>
 * </table>
 * <br>
 * example: <code>en_s_greeting_13</code><br><br>
 * If there are multiple keys, it will choose a random one.<br>
 * It will then retrieve the value. The value can be the sentence to return, or it can contain these elements:<br><br>
 * <code>$key = a variable</code>
 * they get replaced by the values in the HashMap.<br>
 * <code>§id = </code> another properties-key, will be recursively called.<br>
 * <br>
 * Full example: <code>en_s_greeting_13 = Hello $name, §joke.</code>
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class TTSOutputExtension extends OutputExtension<TTSData> {

    private PropertiesContainer propertiesContainer;
    private String locale;

    /**
     * creates a new outputExtension with a new id
     *
     * @param id                  the id to be set to the id of outputExtension
     * @param propertiesContainer the PropertiesContainer used for generating Sentences
     *                            (you can retrieve from the AddOn Class)
     */
    public TTSOutputExtension(String id, PropertiesContainer propertiesContainer, Context context) {
        super(id, context);
        this.propertiesContainer = propertiesContainer;
    }

    /**
     * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
     * to the outputPlugin
     *
     * @param event the Event to generate for
     */
    @Override
    public TTSData generate(Event event) {
        if (canGenerateForLanguage(locale)) return generateSentence(event);
        return null;
    }

    /**
     * return the locale
     *
     * @return a String containing ISO 639-Code for language
     */
    public String getLocale() {
        return locale;
    }

    /**
     * sets the current locale.
     * This method will be used by the TTSOutputPlugin
     *
     * @param locale ISO 639-Code for language
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * override this class to generate the TTSData.
     * it will be called, when canGenerate returns true for the locale
     *
     * @param event the Event which triggered the generation
     * @return an instance of TTSData, which will then be consumed by the TTSOutputPlugin
     */
    public abstract TTSData generateSentence(Event event);

    /**
     * checks if the TTSOutputExtension can generate TTSData fot the locale
     *
     * @param locale the locale of the request
     * @return true if able to generate, false if not
     */
    public abstract boolean canGenerateForLanguage(String locale);

    /**
     * this method should be used to generate the words for the TTSData.
     * <p>
     * it will get all the keys from the properties-file. Details of this method are explained in the javadoc of the
     * class.
     *
     * @param ID     the ID of the word
     * @param values the values to bind
     * @return a String containing the value of one key
     */
    public String getWords(String ID, HashMap<String, String> values) {
        return getWords(ID, values, 0);
    }

    /**
     * {@link #getWords(String, java.util.HashMap)} is just a wrapper for this class with a start value for recursion.
     *
     * @param ID        the ID of the word
     * @param values    the values to bind
     * @param recursion the number of the recursions, the limit is 100
     * @return a String containing the value of one key
     */
    private String getWords(String ID, HashMap<String, String> values, int recursion) {
        String words = getRandomWords(ID);
        words = replaceVariables(words, values);
        words = replaceNames(words, values, recursion);
        return words;
    }

    /**
     * returns one value from multiple possible keys containing the ID.
     * <p>
     * IT IS VERY IMPORTANT THAT THE GENERATED SENTENCES HAVE ALL CORRECT ENDINGS(DOTS).<br>
     * Details of this method are explained in the javadoc of the class.
     *
     * @param ID a String ID
     * @return a String containing the value of one key
     */
    private String getRandomWords(String ID) {
        LinkedList<String> words = getPossibleWords(ID);
        if (words.isEmpty()) {
            return "";
        } else if (words.size() == 1) {
            return words.pop();
        } else {
            Random random = new Random();
            return words.get(random.nextInt(words.size()));
        }
    }

    /**
     * returns all possible values for the ID.
     * Details of this method are explained in the javadoc of the class.
     *
     * @param ID the ID to search the keys for
     * @return a list containing all the possible values
     */
    private LinkedList<String> getPossibleWords(String ID) {
        LinkedList<String> foundList = new LinkedList<>();
        Enumeration<Object> keys = propertiesContainer.getProperties().keys();
        Pattern pattern = Pattern.compile(locale + "_([sp])_(" + ID + ")_?[1-9]?");
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches()) {
                String words = propertiesContainer.getProperties().getProperty(key);
                foundList.add(words);
            }
        }
        return foundList;
    }

    /**
     * replaces all the variables (<code>$name</code>).
     * Details of this method are explained in the javadoc of the class.
     *
     * @param string a string which may contain variables.
     * @param values a HashMap where the values are stored
     * @return a String where all the variables are replaced.
     */
    private String replaceVariables(String string, HashMap<String, String> values) {
        Pattern pattern = Pattern.compile("(\\$(\\w)+)");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String originalKey = matcher.group();
            String key = originalKey.replace("$", "");
            string = string.replace(originalKey, values.get(key));
        }
        return string;
    }

    /**
     * replaces all the references to other records with the records.
     * (Recursive calls to {@link #getWords(String, java.util.HashMap)} if recursion doesn't exceed 100)
     *
     * @param string    the words to replace in
     * @param values    the values
     * @param recursion the number of the recursion
     * @return a string where all the references got replaced
     */
    private String replaceNames(String string, HashMap<String, String> values, int recursion) {
        Pattern pattern = Pattern.compile("(§(\\w)+)");
        Matcher matcher = pattern.matcher(string);
        while (matcher.find()) {
            String originalKey = matcher.group();
            String key = originalKey.replace("$", "");
            String newWords;
            if (recursion >= 100) {
                newWords = "";
            } else {
                newWords = getWords(key, values);
            }
            string = string.replace(originalKey, newWords);
        }
        return string;
    }
}
