package bradleyross.music.playpen;
import java.util.List;
import java.util.ArrayList;
import javax.sound.midi.MidiDeviceReceiver;
import javax.sound.midi.MidiDeviceTransmitter;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Sequence;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.InvalidMidiDataException;
/**
 * I am trying to write code that will convert key presses on keyboard to music.
 * 
 * <p>The Javadocs for the MIDI java classes can be found at 
 * <a href="https://docs.oracle.com/javase/7/docs/api/overview-summary.html" target="_blank">
 * https://docs.oracle.com/javase/7/docs/api/overview-summary.html</a><p>
 * <ul>
 * <li><a href="https://www.ibm.com/developerworks/library/it/it-0801art38/" target="_blank">
 *     https://www.ibm.com/developerworks/library/it/it-0801art38/</a> IBM article on 
 *     writing programs for MIDI</li>
 * </ul>
 * @author Bradley Ross
 *
 */
public class MidiExample implements Runnable {
	protected Synthesizer synthesizer = null;
	protected Sequencer sequencer = null;
	public void run() {
		MidiDevice.Info[] list = MidiSystem.getMidiDeviceInfo();
		MidiDevice keyboard = null;
		MidiDevice device = null;
		try {
			synthesizer = MidiSystem.getSynthesizer();
			sequencer = MidiSystem.getSequencer(true);
		} catch (MidiUnavailableException e) {
			System.out.println("Failure setting up sequencer/synthesizer");
			e.printStackTrace();
		}
		System.out.println("I, Vendor, Name, Description, Version");
		for (int i = 0; i < list.length; i++)  {
			System.out.println("*****  *****");
			Info item = list[i];
			try {
				device = MidiSystem.getMidiDevice(item);
				System.out.println(i + " : " + item.getVendor() + " : " + item.getName() + " : " +
						list[i].getDescription() + " : " + list[i].getVersion());
				System.out.println(device.getClass().getName());
				// device.open();
				if (device instanceof Sequencer) {
					System.out.println("subclass of Sequencer"); 
					// sequencer = (Sequencer)device;
					int rcv = device.getMaxReceivers();
					int tx = device.getMaxTransmitters();
					System.out.println("Maximum of " + rcv + " receivers");
					System.out.println("Maximum of " + tx + " transmitters");				 
				}  else if (device instanceof Synthesizer) {
					System.out.println("subclass of Synthesizer"); 
					synthesizer = (Synthesizer)device;
					int rcv = device.getMaxReceivers();
					int tx = device.getMaxTransmitters();
					System.out.println("Maximum of " + rcv + " receivers");
					System.out.println("Maximum of " + tx + " transmitters");
				} else {
					System.out.println("Device is a MIDI port");
					int rcv = device.getMaxReceivers();
					int tx = device.getMaxTransmitters();
					System.out.println("Maximum of " + rcv + " receivers");
					System.out.println("Maximum of " + tx + " transmitters");
				}

				if (device instanceof MidiDeviceReceiver) {
					System.out.println("subclass of MidiDeviceReceiver");
				} 
				if (device instanceof MidiDeviceTransmitter) {
					System.out.println("subclass of MidiDeviceTransmitter");
				}
				if (device instanceof Receiver) {
					System.out.println("subclass of Receiver");
				} 
				if (device instanceof Transmitter) {
					System.out.println("subclass of Transmitter"); 
				} 
				if (device instanceof MidiDevice) {
					System.out.println("subclass of MidiDevice");
				} 
				if (item.getName().contains("KEYBOARD")) {
					keyboard = device;
				}
				List<Receiver> rcvList = device.getReceivers();
				List<Transmitter> txList = device.getTransmitters();
				if (rcvList != null) {
					System.out.println("There are " + rcvList.size() + " receivers");
				}
				if (txList != null) {
					System.out.println("There are " + txList.size() + " transmitters");
				}
			}
			catch (MidiUnavailableException e) {
				System.out.println("MidiUnavailableException");
				e.printStackTrace();
			}
		}
		if (keyboard == null) { 
			System.out.println("No keyboard");
			return;
		}
		Transmitter inPortTrans1, inPortTrans2;
		Receiver synthRcvr;
		Receiver seqRcvr;
		try {
			System.out.println(keyboard.getDeviceInfo().getName());
			synthesizer.open();
			sequencer.open();
			inPortTrans1 = keyboard.getTransmitter();
			keyboard.open();
			synthRcvr = synthesizer.getReceiver();
			inPortTrans1.setReceiver(synthRcvr);
			inPortTrans2 = keyboard.getTransmitter();
			seqRcvr = sequencer.getReceiver();
			inPortTrans2.setReceiver(seqRcvr);
			transceiverData(inPortTrans1, "inPortTrans1");
			transceiverData(inPortTrans2, "inPortTrans2");
			transceiverData(synthRcvr, "synthRcvr");
			transceiverData(seqRcvr, "seqRcvr");

			/* Keyboard information */
			List<Receiver> rcv = keyboard.getReceivers();
			List<Transmitter> tx = keyboard.getTransmitters();
			System.out.println("Keyboard: " +
					Integer.toString(rcv.size()) + " receivers, " +
					Integer.toString(tx.size()) + " transmitters");
			/* Synthesizer information */
			rcv = synthesizer.getReceivers();
			tx = synthesizer.getTransmitters();
			System.out.println("Synthesizer: " + 
					Integer.toString(rcv.size()) + " receivers, " +
					Integer.toString(tx.size()) + " transmitters");				
			rcv = sequencer.getReceivers();
			/* Sequencer information */
			tx = sequencer.getTransmitters();
			System.out.println("Sequencer: " + 
					Integer.toString(rcv.size()) + " receivers, " +
					Integer.toString(tx.size()) + " transmitters");				
			/*
			 * I am very confused about this part,  I want to ensure that the
			 * application doesn't close before I can do some work. 
			 */
			for (int i = 0; i < 5; i++) {
				System.out.println("Sleeping");
				Thread.sleep(50000);
			}
			System.out.println("Sleep complete");
			inPortTrans1.close();
			inPortTrans2.close();
			synthRcvr.close();
			seqRcvr.close();
			/*
			synthesizer.close();
			sequencer.close();
			keyboard.close();
			*/
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
	}
	/**
	 * This is a convenience class that prints some information on 
	 * Transmitter and Receiver objects.
	 * @param obj object for which information is displayed
	 * @param name label for object
	 */
	protected void transceiverData(Object obj, String name) {
		System.out.println(name + " class is " + obj.getClass().getName());
		if (obj instanceof MidiDeviceReceiver) {
			System.out.println("     Subclass of MidiDeviceReceiver");
		}
		if (obj instanceof MidiDeviceTransmitter) {
			System.out.println("     Subclass of MidiDeviceTransmitter");
		}
		if (obj instanceof Receiver) {
			System.out.println("     Subclass of Receiver");
		}
		if (obj instanceof Transmitter) {
			System.out.println("     Subclass of Transmitter");
		}
	}
	/**
	 * Test driver.
	 * @param args not used
	 */
	public static void main(String[] args) {
		MidiExample instance = new MidiExample();
		instance.run();
	}
}

