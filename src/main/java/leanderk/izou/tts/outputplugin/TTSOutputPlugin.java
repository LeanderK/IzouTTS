package leanderk.izou.tts.outputplugin;

import com.gtranslate.Audio;
import com.gtranslate.context.TranslateEnvironment;
import intellimate.izou.output.OutputPlugin;
import javazoom.jl.decoder.JavaLayerException;
import leanderk.izou.tts.outputextension.TTSData;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by LeanderK on 02/11/14.
 */
public class TTSOutputPlugin extends OutputPlugin<TTSData>{
    private TTSElementCollection collection;

    public static final String ID = TTSOutputPlugin.class.getCanonicalName();
    private final int Buffer = 10;
    private Audio audio;

    public TTSOutputPlugin() {
        super(ID);
        collection = new TTSElementCollection();

        //required values
        String enableProxy = "false";
        String proxy = "my.proxy.com";
        String port= "8080";
        String googleTranslateText = "http://translate.google.com.{locale}/translate_a/t?";
        String googleTranslateAudio = "http://translate.google.com/translate_tts?";
        String googleTranslateDetect = "http://www.google.com/uds/GlangDetect?";
        String locale = "en";

        //initializing
        TranslateEnvironment.init(enableProxy,
                proxy,
                port,
                googleTranslateText,
                googleTranslateAudio,
                googleTranslateDetect,
                locale);
        audio = Audio.getInstance();
    }

    /**
     * method that uses tDoneList to generate a final output that will then be rendered.
     * The processed content-data objects are found in tDoneProcessed
     */
    @Override
    public void renderFinalOutput() {
        List<TTSData> dataList = getTDoneList();
        collection.clear();
        dataList.forEach(collection::addTTSElement);
        LinkedList<TTSElement> elements = collection.getFullCollectionAsList();
    }

    private void bufferAndSpeak(LinkedList<TTSElement> elements) {
    }

    private void speak(InputStream sound) {
        try {
            audio.play(sound);
        } catch (JavaLayerException e) {
            //TODO: implement exception logging
            e.printStackTrace();
        }
    }
}
