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
        LinkedList<AddOn> addOns = new LinkedList<>();
        addOns.add(new TTS());
        Main main = new Main(addOns);
    }
}
