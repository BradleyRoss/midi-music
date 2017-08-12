package bradleyross.music.imported;
import java.util.List;
import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
// import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
// import javax.sound.midi.Transmitter;

/**
 * Plays musical notes on first synthesizer it finds.
 * 
 * <p>This program is based loosely on SynthNote which can be found 
 *    at<a href="http://www.jsresources.org/examples/SynthNote.java.html"
 *    target="_blank">
 *    http://www.jsresources.org/examples/SynthNote.java.html</a>.
 *    However, it has been heavily modified to reduce the need on
 *    third-party libraries and to make it more understandable.</p>
 * @author bradleyross
 *
 */
public class SynthNote implements Runnable {
	List<MidiDevice> devices = new ArrayList<MidiDevice>();
	MidiDevice.Info[] infos = null; 
	Synthesizer synthesizer = null;
	Receiver receiver = null;
	int PROGRAM_CHANGE = 192;
	int NOTE_ON = 144;
	int NOTE_OFF = 128;
	public void run() {
		try {
			infos = MidiSystem.getMidiDeviceInfo();
			for (MidiDevice.Info item : infos) {
				MidiDevice device = MidiSystem.getMidiDevice(item);
				devices.add(device);
				if (synthesizer == null) { 
					if (!Synthesizer.class.isAssignableFrom(device.getClass())) {
						continue;
					}
					if (device.getMaxReceivers() == 0) { continue; }
					synthesizer = (Synthesizer) device;
					synthesizer.open();
					receiver = synthesizer.getReceiver();
				}
			}
			if (synthesizer == null) {
				System.out.println("Unable to find synthesizer - aborting");
				return;
			}
			System.out.println("Setting instrument to church organ");
			ShortMessage message1 = new ShortMessage(PROGRAM_CHANGE, 20, 100);
			System.out.println("Sending first note -  note 96");
			receiver.send(message1, -1l);
			ShortMessage message2 = new ShortMessage(NOTE_ON, 96, 100);
			receiver.send(message2, -1l);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			ShortMessage message3 = new ShortMessage(NOTE_OFF, 96, 0);
			receiver.send(message3, -1);
			System.out.println("Second note");
			sendNote(32, 2000);
			System.out.println("Third note");
			sendNote(44, 2000);
			System.out.println("Fourth note");
			sendNote(56, 2000);
			for (int i = 0; i < 7; i++) {
				int note = 56 + 12*i;
				if (note > 127) {
					break;
				}
				System.out.println("New note: " + Integer.toString(note));
				sendNote(56 + 12*i, 1000);
			}
			sendChord(56, 2000);
			synthesizer.close();

		} catch (MidiUnavailableException ex) {
			ex.printStackTrace();
		} catch (InvalidMidiDataException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Play a note
	 * @param note note to be played
	 * @param length length of note in milliseconds
	 * @throws MidiUnavailableException
	 * @throws InvalidMidiDataException
	 */
	protected void sendNote (int note, int length ) 
			throws MidiUnavailableException, InvalidMidiDataException {
		ShortMessage message1 = new ShortMessage(NOTE_ON,note, 100);
		receiver.send(message1, -1);
		try {
			Thread.sleep(length);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		ShortMessage message2 = new ShortMessage(NOTE_OFF, note, 100);
		receiver.send(message2, -1);
		
	}
	/**
	 * Send a chord.
	 * @param note  base note
	 * @param length length of chord in milliseconds
	 * @throws MidiUnavailableException
	 * @throws InvalidMidiDataException
	 */
	
	protected void sendChord(int note, int length) 
	throws MidiUnavailableException, InvalidMidiDataException {
		receiver.send(new ShortMessage(NOTE_ON, note, 110), -1);
		receiver.send(new ShortMessage(NOTE_ON, note + 12, 110), -1);
		receiver.send(new ShortMessage(NOTE_ON, note + 24, 110), -1);
		try {
			Thread.sleep(length);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		receiver.send(new ShortMessage(NOTE_OFF, note, 110), -1);
		receiver.send(new ShortMessage(NOTE_OFF, note + 12, 110), -1);
		receiver.send(new ShortMessage(NOTE_OFF, note + 24, 110), -1);
	}
	/**
	 * Test driver.
	 * @param args not used
	 */
	public static void main(String[] args) {
		SynthNote instance = new SynthNote();
		instance.run();
	}

}
