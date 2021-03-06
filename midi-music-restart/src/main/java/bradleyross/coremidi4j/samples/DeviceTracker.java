package bradleyross.coremidi4j.samples;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
// import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
// import javax.sound.midi.Transmitter;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceInfo;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiSource;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDestination;
/**
 * Example of getting unique ID using CoreMidi4J by using explicit casting of MidiDevice
 * and MidiDevice.Info to carry out narrowing conversion to make more information
 * visible.
 * 
 * @author Bradley Ross
 *
 */
public class DeviceTracker {
	protected class DeviceInfo {
		/**
		 * {@link MidiDevice} (or subclass) object that provides
		 * information about a MIDI device.
		 */
		protected MidiDevice device = null;
		/**
		 * {@link MidiDevice} (or subclass) object that provides
		 * information to identify a MIDI device.
		 */
		protected MidiDevice.Info info = null;
		protected boolean isSequencer = false;
		protected boolean isSynthesizer = false;
		protected boolean isCoreMidiSource = false;
		protected boolean isCoreMidiDestination = false;
		protected boolean usesCoreMidi4J = false;
		protected int maxReceivers = -1;
		protected int maxTransmitters = -1;
		protected int endPointReference = -999;
		protected String  informationString = new String();
		protected int uniqueID = -999;
		public DeviceInfo(MidiDevice value) {
			device = value;
			info = device.getDeviceInfo();
			updateInformation();
		}
		public DeviceInfo(MidiDevice.Info value) throws MidiUnavailableException {
			info = value;
			device = MidiSystem.getMidiDevice(value);
			updateInformation();
		}
		protected void updateInformation() {
			if (Synthesizer.class.isAssignableFrom(device.getClass())) {
				isSynthesizer = true;
			} else {
				isSynthesizer = false;
			}
			if (Sequencer.class.isAssignableFrom(device.getClass())) {
				isSequencer = true;
			} else {
				isSequencer = false;
			}
			if (CoreMidiSource.class.isAssignableFrom(device.getClass())) {
				isCoreMidiSource = true;
			} else {
				isCoreMidiSource = false;
			}
			if (CoreMidiDestination.class.isAssignableFrom(device.getClass())) {
				isCoreMidiDestination = true;
			} else {
				isCoreMidiDestination = false;
			}
			uniqueID = 0xFFFFFFFF;
			endPointReference = -9999;
			if (CoreMidiDeviceInfo.class.isAssignableFrom(info.getClass())) {
				// System.out.println("Getting extended information for device");
				CoreMidiDeviceInfo coreInfo = (CoreMidiDeviceInfo) info;
				uniqueID = coreInfo.getdeviceUniqueID();
				informationString = coreInfo.getInformationString();
				endPointReference = coreInfo.getEndPointReference();
			}
			maxTransmitters = device.getMaxTransmitters();
			maxReceivers = device.getMaxReceivers();

		}
		public String toString() {
			String sp5 = "     ";
			StringWriter buffer = new StringWriter();
			PrintWriter out = new PrintWriter(buffer);
			out.println("Name: " + info.getName());
			out.println(sp5 + "Description: " + info.getDescription());
			out.println(sp5 + "Vendor: " + info.getVendor());
			out.println(sp5 + "Version: " + info.getVersion());
			out.println(sp5 + "Device Class: " + device.getClass());
			out.println(sp5 + "Info Class: " + info.getClass());			
			if (CoreMidiDeviceInfo.class.isAssignableFrom(info.getClass())) {
				out.println(sp5 + "Unique ID: " + String.format("%08X", uniqueID));
				out.println(sp5 + "End Point Reference: " + Integer.toString(endPointReference));
				out.println(sp5 + "Information String: " + informationString);
			}
			out.println(sp5 + "Is Sequencer: " + Boolean.toString(isSequencer));
			out.println(sp5 + "Is Synthesizer: " + Boolean.toString(isSynthesizer));
			out.println(sp5 + "CoreMidiDestination: " + Boolean.toString(isCoreMidiDestination));
			out.println(sp5 + "CoreMidiSource: " + Boolean.toString(isCoreMidiSource));
			if (maxTransmitters < 0) {
				out.println(sp5 + "Unlimited number of transmitters");
			} else if (maxTransmitters == 0) {
				out.println(sp5 + "No transmitters allowed");
			} else {
				out.println(sp5 + "Maximum of " + Integer.toString(maxTransmitters));
			}
			if (maxReceivers < 0) {
				out.println(sp5 + "Unlimited number of receivers");
			} else if (maxReceivers == 0) {
				out.println(sp5 + "No receivers allowed");
			} else {
				out.println(sp5 + "Maximum of " + Integer.toString(maxReceivers));
			}
			return buffer.toString();
		}
	}
	protected List<DeviceInfo> deviceList = null;
	public void run() {
		deviceList = new ArrayList<DeviceInfo>();
		MidiDevice.Info[] infoList = CoreMidiDeviceProvider.getMidiDeviceInfo();
		for (MidiDevice.Info item : infoList) {
			try {
				DeviceInfo tracker = new DeviceInfo(item);
				System.out.println("Using CoreMidiDeviceProvider#getMidiDeviceInfo()");
				System.out.println(tracker.toString());
				System.out.println(" *****   *****   *****   *****");
			} catch (MidiUnavailableException ex) {
				ex.printStackTrace();
			}
		}
		MidiDevice.Info[] infoList2 = MidiSystem.getMidiDeviceInfo();
		for (MidiDevice.Info item : infoList2) {
			try {
				DeviceInfo tracker = new DeviceInfo(item);
				System.out.println("Using MidiSystem#getMidiDeviceInfo()");
				System.out.println(tracker.toString());
				System.out.println(" *****   *****   *****   *****");
			} catch (MidiUnavailableException ex) {
				ex.printStackTrace();
			}
		}
	}
	/**
	 * Test driver.
	 * @param args  not used
	 */
	public static void main(String[] args) {

		// Class.forName("uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider");
		DeviceTracker instance = new DeviceTracker();
		instance.run();
	}
}
