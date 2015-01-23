package leanderk.izou.tts.outputplugin;

import com.gtranslate.Audio;
import com.gtranslate.context.TranslateEnvironment;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;
import intellimate.izou.properties.PropertiesContainer;
import intellimate.izou.system.Context;
import leanderk.izou.tts.outputextension.*;
import leanderk.izou.tts.outputextension.TTSOutputExtension;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Output TTS Plugin
 * TTSOutputExtensions generate the Sentences and this plugin is is responsible for speech synthesization.
 */
public class TTSOutputPlugin extends OutputPlugin<TTSData> {
    @SuppressWarnings("WeakerAccess")
    public static final String ID = TTSOutputPlugin.class.getCanonicalName();
    private final TTSElementCollection collection;
    private final int Buffer = 10;
    private AtomicInteger currentBuffer = new AtomicInteger(0);
    private final Audio audio;
    private String locale;
    private Context context;

    //public TTSOutputPlugin() {
    //    this(null);
    //}

    @SuppressWarnings("WeakerAccess")
    public TTSOutputPlugin(@SuppressWarnings("SameParameterValue")  Context context) {
        super(ID, context);
        collection = new TTSElementCollection(context.threadPool.getThreadPool(), context);
        this.context = context;
        PropertiesContainer properties = context.properties.getPropertiesContainer();

        //required values
        String enableProxy = "false";
        String proxy = "my.proxy.com";
        String port= "8080";
        String googleTranslateText = "http://translate.google.com.{locale}/translate_a/t?";
        String googleTranslateAudio = "http://translate.google.com/translate_tts?";
        String googleTranslateDetect = "http://www.google.com/uds/GlangDetect?";
        if(properties != null && properties.getProperties().getProperty("locale") != null) {
            locale = properties.getProperties().getProperty("locale");
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
        context.logger.getLogger().debug("rendering output");
        List<TTSData> dataList = pollTDoneList();
        context.logger.getLogger().debug("got " + dataList.size() + "TTSData Elements");
        collection.clear();
        dataList.forEach(collection::addTTSElement);
        LinkedList<TTSElement> elements = collection.getFullCollectionAsList();
        context.logger.getLogger().debug("created "+ elements.size() + " TTSElements");
        bufferAndSpeak(elements);
        collection.clear();
    }

    /**
     * event is called when an output-extension is added to this output-plugin
     *
     * @param outputExtension the outputExtension that was added to the outputPlugin
     */
    @Override
    public void outputExtensionWasAdded(OutputExtension<TTSData> outputExtension) {
        try {
            TTSOutputExtension extension = (TTSOutputExtension)outputExtension;
            extension.setLocale(locale);
            context.logger.getLogger().debug("Setting locale for:" + extension.getID());
        } catch (Exception e) {
            context.logger.getLogger().error("Error while trying set locale", e);
        }
    }

    /**
     * buffers the TTS-audio and plays it.
     * because generating the audio happens in the internet, it first has to be buffered.
     * @param elements a list containing all the elements
     */
    private void bufferAndSpeak(LinkedList<TTSElement> elements) {
        while(elements.size() > 0) {
            if(currentBuffer.get() < Buffer) {
                elements.stream().filter(element -> !element.bufferingStarted())
                            .peek(element -> context.logger.getLogger().debug("able to buffer " +
                                    (Buffer - currentBuffer.get()) + " elements"))
                        .limit(Buffer - currentBuffer.get())
                        .forEach(element -> element.buffer(() -> {
                            currentBuffer.decrementAndGet();
                            context.logger.getLogger().debug("buffering " + element.getID() + " finished");
                        }));
            }
            if(elements.get(0).bufferingFinished()) {
                TTSElement element = elements.pop();
                LinkedList<InputStream> inputStreams = element.getInputStreams();
                context.logger.getLogger().debug("speaking: " + element.getID()+ ": <" + element.getWords() + "> " +
                        "with " + inputStreams.size() + " InputStreams");
                inputStreams.forEach(this::speak);
            }
        }
        currentBuffer.set(0);
    }

    /**
     * plays the audio
     * @param sound the InputStream containing the TTS
     */
    private void speak(InputStream sound) {
        try {
            audio.play(sound);
        } catch (Exception e) {
            context.logger.getLogger().error(e);
        }
    }
}
