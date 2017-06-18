
package bradleyross.coremidi4j.samples;

/**
 * Demonstration driver for CoreMidi4J.
 * 
 * <>This is part of the demonstration code contained in the 
 *   documentation for the DerekCook/CoreMidi4J.</p>
 *   
 * @author Derek Cook
 *
 */

public class Available {

	public static void main(String[] args) throws Exception {
		try {
			/*
			 * The following statement previously read Class deviceProviderClass = Class.forName(...
			 * However, since the returned object is never used, this results in a warning message
			 * in Eclipse.  The statement does not require returning a value, and I removed the
			 * storage of the returned value.
			 */
			Class.forName(
					"uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider");
			System.out.println("CoreMIDI4J Java classes are available.");
			System.out.println("Working MIDI Devices:");
			for (javax.sound.midi.MidiDevice.Info device : Example.getWorkingDeviceInfo()) {
				System.out.println("  " + device);
			}
			if (Example.isCoreMidiLoaded()) {
				System.out.println("CoreMIDI4J native library is running.");
				Example.watchForMidiChanges();
				/*
				 * If the sleep method throws InterruptedException, it does not mean
				 * that the CoreMIDI4J classes are not loaded.  For that reason, the
				 * exception is trapped locally to avoid erroneous messages.
				 */
				try {
					System.out.println("Watching for MIDI environment changes for thirty seconds.");
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("CoreMIDI4J native library is not available.");
			}
		} catch (Exception e) {
			System.out.println("CoreMIDI4J Java classes are not available.");
			e.printStackTrace();
		}
	}
}
