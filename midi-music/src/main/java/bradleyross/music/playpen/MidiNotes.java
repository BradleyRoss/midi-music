package bradleyross.music.playpen;
// import javax.sound.midi.Instrument;
// import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
// import javax.sound.midi.Sequencer;
// import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
/**
 * Attempt to display keyboard actions.
 * <p>The program doesn't exit when the timer ends.</p>
 * <p>Perhaps there has to be a class representng a
 *    virtual MIDI device.  This would implement 
 *    {@link MidiDevice} with one receiver and no
 *    transmitters allowed.  The receiver would run
 *    in its own thread and implement 
 *    {@link Receiver}.</p>
 * @author Bradley Ross
 *
 */
public class MidiNotes {

	protected class Listener implements Receiver {
		protected int counter = 0;
        protected int shiftValue(byte b) {
        	int temp = b;
        	if (temp < 0) { temp = temp +256; }
        	return temp;
        }
        protected String showByte(byte b) {
        	return Integer.toString(shiftValue(b));
        }
		public void send(MidiMessage message, long timeStamp) {
			counter++;
			System.out.print(Integer.toString(counter) + "  " + Long.toString(timeStamp/1000000l) + "    ");
			byte[] data = message.getMessage();
			/*
			if (message.getStatus() == ShortMessage.NOTE_ON) {
				System.out.println("NOTE_ON  " + showByte(data[1]) + "  " + showByte(data[2]));
			} else if (message.getStatus() == ShortMessage.NOTE_OFF) {
				System.out.println("NOTE_OFF " + showByte(data[1]) + "  " + showByte(data[2]));
			} else {
				System.out.println(Integer.toString(message.getStatus()) + showByte(data[1]) + showByte(data[2]) );
			}
			*/
			for (int i = 0; i < data.length; i++) {
				int value = data[i] & 0xFF;
				if (i == 0) {
					int status = message.getStatus();
					if (status == ShortMessage.NOTE_ON) {
						System.out.print("NOTE_ON    ");
					} else if (status == ShortMessage.NOTE_OFF) {
						System.out.print("NOTE_OFF   ");
					} else if (status == ShortMessage.PITCH_BEND) {
						System.out.print("PITCH_BEND ");
					} else if (status == ShortMessage.CONTROL_CHANGE) {
						System.out.print("CONTROL_CH ");
					} else {
						System.out.print(Integer.toString(status, 16) + "      ");
					}
				} else if (i > 10) {
					break;
				} else {
					System.out.print(Integer.toString(value, 16) + " ");
				}
				
			}
			System.out.println();
			
		}

		public void close() {
			System.out.println("Close message received");
		}
		public void finalize() {
			System.out.println("No more references to listener");
		}
		
	}
	public void run() {
		try {
			// Synthesizer synthesizer = null;
			// Sequencer sequencer = null;
			Transmitter transmitter = null;
			Receiver receiver = null;
			MidiDevice keyboard = null;
			MidiDevice.Info devices[] = null;
			// Instrument instruments[] = null;
			devices = MidiSystem.getMidiDeviceInfo();
			for (MidiDevice.Info item : devices) {
				MidiDevice dev = MidiSystem.getMidiDevice(item);
				System.out.println(item.getName() + " : " + item.getDescription() +
						" : " + item.getVersion() + " : " + item.getVendor());
				if (item.getName().equalsIgnoreCase("KEYBOARD")) {
					keyboard = dev;
				} else {
					dev.close();
				}
				int maxRcvr = dev.getMaxReceivers();
				int maxXmtr = dev.getMaxTransmitters();
				System.out.println("     Max  Rcvr:" + Integer.toString(maxRcvr) +
						"  Xmtr:" + Integer.toString(maxXmtr));
				
				dev = null;
			}
			devices = null;
			keyboard.open();
			System.out.println("Keyboard is open: " + Boolean.toString(keyboard.isOpen()));
			devices = null;
			// sequencer = MidiSystem.getSequencer();
			// synthesizer = MidiSystem.getSynthesizer();
			/*
		sequencer = MidiSystem.getSequencer();
		sequencer.open();
		synthesizer = MidiSystem.getSynthesizer();
		synthesizer.open();
		System.out.println("Synthesizer is open: " + Boolean.toString(synthesizer.isOpen()));
		*/
		transmitter = keyboard.getTransmitter();
		receiver = new Listener();
		transmitter.setReceiver(receiver);
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// synthesizer.close();
		// sequencer.close();
		receiver.close();
		receiver = null;
		transmitter.setReceiver(null);
		transmitter.close();
		transmitter = null;
		keyboard.close();
		keyboard = null;
		// sequencer.close();
		// synthesizer.close();
		/*
		transmitter = keyboard.getTransmitter();
		receiver = synthesizer.getReceiver();
		transmitter.setReceiver(receiver);

		instruments = synthesizer.getAvailableInstruments();
		synthesizer.loadInstrument(instruments[1]);
		System.out.println("Send NOTE_ON");
		receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 20, 100), 1000000L);
		try {
		Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Send NOTE_OFF");
		receiver.send(new ShortMessage(ShortMessage.NOTE_OFF, 20, 100), 3000000L);
		transmitter.close();
		*/
		
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		
		
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		
		System.out.println("Application finished");

		return;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MidiNotes instance = new MidiNotes();
		instance.run();
		instance = null;
		System.out.println("Run method complete");

	}

}
