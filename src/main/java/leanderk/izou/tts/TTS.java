package leanderk.izou.tts;

import leanderk.izou.tts.outputplugin.TTSOutputPlugin;
import org.intellimate.izou.output.OutputPluginModel;
import org.intellimate.izou.sdk.activator.Activator;
import org.intellimate.izou.sdk.addon.AddOn;
import org.intellimate.izou.sdk.contentgenerator.ContentGenerator;
import org.intellimate.izou.sdk.events.EventsController;
import org.intellimate.izou.sdk.output.OutputExtension;
import ro.fortsoft.pf4j.Extension;

/**
 * Created by LeanderK on 01/11/14.
 */
@Extension
public class TTS extends AddOn {

    @SuppressWarnings("WeakerAccess")
    public static final String ID = TTS.class.getCanonicalName();

    public TTS() {
        super(ID);
    }

    @Override
    public void prepare() {
        /*
        //required values
        String enableProxy = "false";
        String proxy = "my.proxy.com";
        String port= "8080";
        String googleTranslateText = "http://translate.google.com.{locale}/translate_a/t?";
        String googleTranslateAudio = "http://translate.google.com/translate_tts?";
        String googleTranslateDetect = "http://www.google.com/uds/GlangDetect?";
        String locale = "en";

        //initializing
        TranslateEnvironment.init(enableProxy, proxy, port, googleTranslateText, googleTranslateAudio, googleTranslateDetect, locale);


        // this  code converts the string to a sound and plays
        Audio audio = Audio.getInstance();
        InputStream sound = null;
        InputStream sound1 = null;
        try {
            sound = audio.getAudio("Hello Leander. Its a good morning today.","en");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            sound1 = audio.getAudio("Today will be mostly sunny. Fuck you.","en");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            audio.play(sound);
            audio.play(sound1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    @Override
    public Activator[] registerActivator() {
        return null;
    }

    @Override
    public ContentGenerator[] registerContentGenerator() {
        return null;
    }

    @Override
    public EventsController[] registerEventController() {
        return null;
    }

    @Override
    public OutputPluginModel[] registerOutputPlugin() {
        OutputPluginModel[] outputPlugins = new OutputPluginModel[1];
        outputPlugins[0] = new TTSOutputPlugin(getContext());
        return outputPlugins;
    }

    @Override
    public OutputExtension[] registerOutputExtension() {
        return null;
    }
}
