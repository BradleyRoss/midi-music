/**
 * 
 */
package bradleyross.music.playpen;
import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDeviceReceiver;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
// import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;


/**
 * This contains some examples based on the 
 * <a href="https://docs.oracle.com/javase/tutorial/sound/overview-MIDI.html" target="_blank">
 * Oracle Midi tutorials</a>.
 * 
 * <p>The example on the Oracle Site only contained the NOTE_ON command.
 *    Without the sleep method call and the NOTE_OFF, the program
 *    terminated immediately and there was no sound.</p>
 * <p>The default receiver belonged to the Synthesizer in my first
 *    test.  However, when I attached an external keyboard, the default
 *    receiver for the system became the keyboard.  It does not appear that
 *    {@link MidiSystem.getReceiver()} and 
 *    {@link MidiSystem.getTransmitter()} are very
 *    useful since the returned values can vary.</p>
 * <p>It should also be noted that the current Java configuration returns
 *    a different synthesizer for each call to 
 *    {@link MidiSystem.getSynthesizer}.  However, all of the instances will 
 *    yield the same {@link MidiDevice.Info} object.   This means that
 *    each call to {@link MidiDevice.Info#getMidiDevice()} will yield
 *    a different object.</p>
 * 
 * @author Bradley Ross
 *
 */
public class OracleExamples1 {
	protected Synthesizer synth;
	protected void sample1() {
		System.out.println("Starting sample1");
		try {
			MidiDeviceReceiver rcvr = (MidiDeviceReceiver)MidiSystem.getReceiver();
			MidiDevice dev = rcvr.getMidiDevice();
			dev.open();
			System.out.println("isOpen is " + dev.isOpen());
			System.out.println(rcvr.getMidiDevice().getDeviceInfo().getDescription());


			ShortMessage myMsg = new ShortMessage();
			/*  0x45 refers to Oboe */
			// myMsg.setMessage(ShortMessage.PROGRAM_CHANGE, 0x45, 0 );
			// myMsg.setMessage(ShortMessage.PROGRAM_CHANGE, 1, 0);
			long timeStamp = -1;
			rcvr.send(myMsg, timeStamp);
			// Start playing the note Middle C (60), 
			// moderately loud (velocity = 93).
			myMsg.setMessage(ShortMessage.NOTE_ON, 0, 60, 93);
			timeStamp = -1;
			rcvr.send(myMsg, timeStamp);
			try {
				Thread.sleep(2000);

			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
			myMsg.setMessage(ShortMessage.NOTE_OFF, 0, 60, 93);
			timeStamp = -1;
			rcvr.send(myMsg, timeStamp);
		} catch (InvalidMidiDataException e) {
			System.out.println("InvalidMidiDataException encountered");
			e.printStackTrace();
		} catch (MidiUnavailableException e) {
			System.out.println("MidiUnavailableException encountered");
			e.printStackTrace();
		}
	}
	protected void sample2() {
		System.out.println("Starting sample 2");
		try {
			synth = MidiSystem.getSynthesizer();
			Soundbank bank = synth.getDefaultSoundbank();
			System.out.println("Available Instruments");
			int counter = 0;
			boolean successful;
			successful = synth.isSoundbankSupported(bank);
			System.out.println("Max Polyphony " + 
					Integer.toString(synth.getMaxPolyphony()));
			System.out.println("isSoundbankSupported " + 
					Boolean.toString(successful));
			Instrument[] inst = synth.getAvailableInstruments();
			/*
			 * It doesn't appear that any of the instruments are
			 * loading directly.
			 */
			for (Instrument i : inst) {
				counter++;
				System.out.println(Integer.toString(counter) + " " + i.getName());
				if (i.getName().equalsIgnoreCase("acoustic grand piano")) {
					successful = synth.loadInstrument(i);
					System.out.println("     Loading " + i.getName() + " " +
							Boolean.toString(successful));
				}
				if (counter > 10 && counter < 20) {
					successful = synth.loadInstrument(i);
					System.out.println("     Loading " + i.getName() + " " +
							Boolean.toString(successful));
				}
			}
			System.out.println("Loaded Instruments");
			inst = synth.getLoadedInstruments();
			counter = 0;
			for (Instrument i : inst) {
				counter++;
				System.out.println(Integer.toString(counter) + " " + i.getName());
			}
			System.out.println("Channels");
			MidiChannel[] channels = synth.getChannels();
			for (MidiChannel item : channels) {
				System.out.println(item.getProgram());
			}
		}catch (MidiUnavailableException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OracleExamples1 instance = new OracleExamples1();
		instance.sample2();
		instance.sample1();
		System.out.println("Program terminated");
	}

}
