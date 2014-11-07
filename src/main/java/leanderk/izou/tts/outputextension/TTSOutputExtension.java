package leanderk.izou.tts.outputextension;

import intellimate.izou.output.OutputExtension;

/**
 * Created by LeanderK on 06/11/14.
 */
public abstract class TTSOutputExtension extends OutputExtension<TTSData>{

    /**
     * creates a new outputExtension with a new id
     *
     * @param id the id to be set to the id of outputExtension
     */
    public TTSOutputExtension(String id) {
        super(id);
    }

    @Override
    public TTSData call() throws Exception {
        return generateSentence();
    }

    public abstract TTSData generateSentence() throws Exception;
}
