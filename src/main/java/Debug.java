import com.gtranslate.Audio;
import com.gtranslate.context.TranslateEnvironment;
import javazoom.jl.player.Player;
import leanderk.izou.tts.outputplugin.MRLSoundAudioDevice;

import java.io.InputStream;

/**
 * Use this class to debug
 */
@SuppressWarnings("UnusedAssignment")
public class Debug {
    public static void main(String[] args) {
        String enableProxy = "false";
        String proxy = "my.proxy.com";
        String port= "8080";
        String googleTranslateText = "http://translate.google.com.{locale}/translate_a/t?";
        String googleTranslateAudio = "http://translate.google.com/translate_tts?";
        String googleTranslateDetect = "http://www.google.com/uds/GlangDetect?";
        String locale = "de";

        //initializing
        TranslateEnvironment.init(enableProxy, proxy, port, googleTranslateText, googleTranslateAudio, googleTranslateDetect, locale);

        Audio audio = Audio.getInstance();
        InputStream sound = null;
        try {
            sound = audio.getAudio("Gerade hat es 16 Grad und ist großteils bewölkt. ","de");
            MRLSoundAudioDevice javaSoundAudioDevice = new MRLSoundAudioDevice();
            javaSoundAudioDevice.setGain(0.1f);
            Player player = new Player(sound, javaSoundAudioDevice);
            player.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        LinkedList<AddOnModel> addOns = new LinkedList<>();
        addOns.add(new TTS());
        Main main = new Main(addOns);*/
    }
}
