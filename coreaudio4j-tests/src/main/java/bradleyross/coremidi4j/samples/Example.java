package bradleyross.coremidi4j.samples;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiNotification;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiException;
import javax.sound.midi.MidiDevice;
/**
 * Class used by {@link Available}.
 * @author Derek Cook
 *
 */
public class Example {
    public static boolean isCoreMidiLoaded() throws CoreMidiException {
        return CoreMidiDeviceProvider.isLibraryLoaded();
    }

    public static void watchForMidiChanges() throws CoreMidiException {
        CoreMidiDeviceProvider.addNotificationListener(new CoreMidiNotification() {
                public void midiSystemUpdated() {
                    System.out.println("The MIDI environment has changed.");
                }
            });
    }

    public static MidiDevice.Info[] getWorkingDeviceInfo() {
        return CoreMidiDeviceProvider.getMidiDeviceInfo();
    }
}
