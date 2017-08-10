package bradleyross.music.playpen;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
/**
 * Test whether MidiDevice.Info.getMidiDevice always gives the
 * same object.
 * 
 * <p>Three MidiDevice objects are created from the same
 *    MidiDevice.Info object.  The same object should be returned
 *    in each case.  This is not true for the synthesizer and
 *    sequencer.</p>
 * <p>A MidiDevice.Info object was created from each of the MidiDevice
 *    objects.  The hash code should be the same for each of the three
 *    cases.  This appears to be the case.</p>
 * <p>Question: If two identical MIDI keyboards are attached at the same time,
 *    will they have different hash codes?</p> 
 * @author Bradley Ross
 *
 */
public class RelationshipTest implements Runnable {
	/**
	 * Carries out test for a single MidiDevice.Info object.
	 * @param item object to be tested
	 * @throws MidiUnavailableException
	 */
	protected void single(MidiDevice.Info item) throws MidiUnavailableException {
		System.out.println("Name: " + item.getName());
		System.out.println("Vendor: " + item.getVendor());
		System.out.println("Version: " + item.getVersion());
		System.out.println("Description: " + item.getDescription());
		System.out.println("Hash: " + Integer.toString(item.hashCode()));
		MidiDevice device1 = MidiSystem.getMidiDevice(item);
		MidiDevice device2 = MidiSystem.getMidiDevice(item);
		MidiDevice device3 = MidiSystem.getMidiDevice(item);
		if (device1 == device2) {
			System.out.println("object 1 equals object 2");
		} else {
			System.out.println("object 1 not equals object 2 - this appears to be bug");
		}
		if (device2 == device3) {
			System.out.println("object 2 equals object 3");
		} else {
			System.out.println("object 2 not equals object 3 - this appears to be bug");
		}
		if (device1 == device3) {
			System.out.println("object 1 equals object 3");
		} else {
			System.out.println("object 1 not equals object 3 - this appears to be bug");
		}
		System.out.println("Hash from object 1: " + 
				Integer.toString(device1.getDeviceInfo().hashCode()));
		System.out.println("Hash from object 2: " + 
				Integer.toString(device2.getDeviceInfo().hashCode()));				
		System.out.println("Hash from object 3: " + 
				Integer.toString(device3.getDeviceInfo().hashCode()));
	}
	public void run() {
		try {
			int counter = 0;
			MidiDevice.Info[] infoList = MidiSystem.getMidiDeviceInfo();
			for (MidiDevice.Info item : infoList) {
				System.out.println("*****  *****  *****");
				System.out.println("Device " + Integer.toString(counter));
				single(item);
				counter++;
			}
			System.out.println("*****  *****  *****");
			MidiDevice.Info synthesizer = MidiSystem.getSynthesizer().getDeviceInfo();
			System.out.println("Synthesizer");
			single(synthesizer);
			System.out.println("*****  *****  *****");
			MidiDevice.Info sequencer = MidiSystem.getSequencer().getDeviceInfo();
			System.out.println("Sequencer");
			single(sequencer);			
		} catch (MidiUnavailableException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Test driver.
	 * 
	 * @param args not used
	 */
	public static void main(String[] args) {
		RelationshipTest instance = new RelationshipTest();
		System.out.println("Java version is " + System.clearProperty("java.version"));
		System.out.println("*****  *****  *****  *****");
		System.out.println("First run");
		instance.run();	
		for (int i = 0;  i < 2; i++) {
			System.out.println("*****  *****  *****  *****  *****  *****  *****");
		}
		System.out.println("Second run");
		instance.run();
	}
}
