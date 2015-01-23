import intellimate.izou.addon.AddOn;
import intellimate.izou.main.Main;
import leanderk.izou.tts.TTS;

import java.util.LinkedList;

/**
 * Use this class to debug
 */
@SuppressWarnings("UnusedAssignment")
public class Debug {
    public static void main(String[] args) {
        /*
        String enableProxy = "false";
        String proxy = "my.proxy.com";
        String port= "8080";
        String googleTranslateText = "http://translate.google.com.{locale}/translate_a/t?";
        String googleTranslateAudio = "http://translate.google.com/translate_tts?";
        String googleTranslateDetect = "http://www.google.com/uds/GlangDetect?";
        String locale = "pe";

        //initializing
        TranslateEnvironment.init(enableProxy, proxy, port, googleTranslateText, googleTranslateAudio, googleTranslateDetect, locale);

        Audio audio = Audio.getInstance();
        InputStream sound = null;
        try {
            sound = audio.getAudio("Hey, wake up there! It's 4 56 pm.","en");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            audio.play(sound);
        } catch (JavaLayerException e) {
            e.printStackTrace();
        }
        */
        LinkedList<AddOn> addOns = new LinkedList<>();
        addOns.add(new TTS());
        Main main = new Main(addOns);
    }
}
