package bradleyross.coremidi4j.samples;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiException;
/**
 * Test automatic installation of CoreMidi4J.
 * 
 * <p>The file META-INF/services/javax.sound.midi.spi.MidiDeviceProvider contains the 
 *    line uk.co.xfactorylibrarians.coremidi4j.coremidi4j.CoreMidiDeviceProvider.
 *    This means that the CoreMidiDeviceProvider class is automatically loaded
 *    when the jar file is in the CLASSPATH.</p>
 * @author Bradley Ross
 *
 */
public class InstallationTest {
	public static void main(String[] args) {
		try {
		System.out.println("isRunning: " +
		Boolean.toString(CoreMidiDeviceProvider.isLibraryLoaded()));
		} catch (CoreMidiException ex) {
			ex.printStackTrace();
		}
	}

}
