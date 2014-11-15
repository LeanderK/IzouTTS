package leanderk.izou.tts.outputplugin;

import com.gtranslate.Audio;
import com.gtranslate.context.TranslateEnvironment;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;
import leanderk.izou.tts.outputextension.TTSData;
import leanderk.izou.tts.outputextension.TTSOutputExtension;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Output TTS Plugin
 * TTSOutputExtensions generate the Sentences and this plugin is is responsible for speech synthesization.
 */
public class TTSOutputPlugin extends OutputPlugin<TTSData>{
    @SuppressWarnings("WeakerAccess")
    public static final String ID = TTSOutputPlugin.class.getCanonicalName();
    private final TTSElementCollection collection;
    private final int Buffer = 10;
    private int currentBuffer = 0;
    private final Audio audio;
    @SuppressWarnings("UnusedDeclaration")
    private ExecutorService executor = Executors.newFixedThreadPool(Buffer/2);
    private String locale;

    public TTSOutputPlugin() {
        this(null);
    }

    @SuppressWarnings("WeakerAccess")
    public TTSOutputPlugin(@SuppressWarnings("SameParameterValue") Properties properties) {
        super(ID);
        collection = new TTSElementCollection(executor);

        //required values
        String enableProxy = "false";
        String proxy = "my.proxy.com";
        String port= "8080";
        String googleTranslateText = "http://translate.google.com.{locale}/translate_a/t?";
        String googleTranslateAudio = "http://translate.google.com/translate_tts?";
        String googleTranslateDetect = "http://www.google.com/uds/GlangDetect?";
        if(properties != null && properties.getProperty("locale") != null) {
            locale = properties.getProperty("locale");
        } else {
            locale = Locale.getDefault().getLanguage();
        }

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
        bufferAndSpeak(elements);
    }

    /**
     * event is called when an output-extension is added to this output-plugin
     *
     * @param outputExtension the outputExtension that was added to the outputPlugin
     */
    @Override
    public void outputExtensionWasAdded(OutputExtension<TTSData> outputExtension) {
        if(TTSOutputExtension.class.isInstance(outputExtension)) {
            TTSOutputExtension extension = (TTSOutputExtension)outputExtension;
            extension.setLocale(locale);
        }
    }

    private void bufferAndSpeak(LinkedList<TTSElement> elements) {
        while(elements.size() > 0) {
            if(currentBuffer < Buffer) {
                elements.stream().filter(element -> !element.bufferingStarted()).forEach(
                        element -> element.buffer(() -> currentBuffer--));
            }
            if(elements.get(0).bufferingFinished()) {
                TTSElement element = elements.pop();
                LinkedList<InputStream> inputStreams = element.getInputStreams();
                inputStreams.forEach(this::speak);
            }
        }
        currentBuffer = 0;
    }

    private void speak(InputStream sound) {
        try {
            audio.play(sound);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
