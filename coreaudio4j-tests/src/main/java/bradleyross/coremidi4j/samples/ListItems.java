package bradleyross.coremidi4j.samples;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
// import javax.sound.midi.Receiver;
// import javax.sound.midi.Transmitter;
import java.util.ArrayList;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;
// import uk.co.xfactorylibrarians.coremidi4j.CoreMidiNotification;
/**
 * Generates a list of MIDI devices.
 * 
 * @author Bradley Ross
 *
 */
public class ListItems {
	/**
	 *    List of devices returned by CoreMidi4J.
	 */
	protected MidiDevice.Info[] deviceList;
	/**
	 * List of MidiDevices that can send information.
	 */
	protected ArrayList<MidiDevice.Info> sources = new ArrayList<MidiDevice.Info>();
	/**
	 * List of MidiDevices that can receive information.
	 */
	protected ArrayList<MidiDevice.Info> dests = new ArrayList<MidiDevice.Info>(); 
	/**
	 *  Lists devices.
	 */
	public void run() {
		deviceList = CoreMidiDeviceProvider.getMidiDeviceInfo(); 
		for (MidiDevice.Info item : deviceList) {
			MidiDevice device;
			System.out.println("****");
			System.out.println(item.getName() + " : " + item.getVendor());
			System.out.println(item.getDescription());
			System.out.println("Version: " + item.getVersion());
			System.out.println("Hash code: " + Integer.toString(item.hashCode()));
			try {
				device = MidiSystem.getMidiDevice(item);
				int maxRcvr = device.getMaxReceivers();
				if (maxRcvr != 0) {
					dests.add(item);
					if (maxRcvr < 0) {
						System.out.println("Unlimited Receivers");
					} else {
						System.out.println("Max. Receivers: " + maxRcvr);
					}
				}
				int maxXmtr = device.getMaxTransmitters();
				if (maxXmtr != 0 ) {
					sources.add(item);
					if (maxXmtr < 0) {
						System.out.println("Unlimited Transmitters");
					} else {
						System.out.println("Max. Transmitters: " + maxXmtr);
					}
				}
			} catch (MidiUnavailableException e) {
				System.out.println("Error obtaining list of MIDI devices");
				e.printStackTrace();
			}
			System.out.println("****");
			System.out.println("Sources:");
			for (MidiDevice.Info source : sources) {
				System.out.println("    " + source.getName() + " : " + source.getVendor() + " : " +
						Integer.toString(source.hashCode()));
			}
			System.out.println("****");
			System.out.println("Sinks:");
			for (MidiDevice.Info sink : dests) {
				System.out.println("    " + sink.getName() + " : " + sink.getVendor() + " : " +
						Integer.toString(sink.hashCode()));
			}
		}
	}
	/**
	 * Test driver.
	 * 
	 * @param args Not used in this example.
	 */
	public static void main(String[] args) {
		try {
			Class.forName
			("uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Unable to open driver - aborting");
			return;
		}
		ListItems instance = new ListItems();
		instance.run();
	}
}
