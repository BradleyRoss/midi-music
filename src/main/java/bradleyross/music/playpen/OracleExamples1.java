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
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;


/**
 * This contains some examples based on the 
 * <a href="https://docs.oracle.com/javase/tutorial/sound/overview-MIDI.html" target="_blank">
 * Oracle Midi tutorials</a>.
 * 
 * <p>The documents are at 
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
			// Start playing the note Middle C (60), 
			// moderately loud (velocity = 93).
			myMsg.setMessage(ShortMessage.NOTE_ON, 0, 60, 93);
			long timeStamp = -1;

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
		try {
			synth = MidiSystem.getSynthesizer();
			synth.getDefaultSoundbank();
			Instrument[] inst = synth.getLoadedInstruments();
			int counter = 0;
			for (Instrument i : inst) {
				counter++;
				System.out.println(Integer.toString(counter) + " " + i.getName());
			}
			counter = 0;
			inst = synth.getAvailableInstruments();
			for (Instrument i : inst) {
				counter++;
				System.out.println(Integer.toString(counter) + " " + i.getName());
				if (i.getName().equalsIgnoreCase("acoustic grand piano")) {
					synth.loadInstrument(i);
				}
			}
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
		// TODO Auto-generated method stub
		OracleExamples1 instance = new OracleExamples1();
		instance.sample2();
		instance.sample1();
		System.out.println("Program terminated");
	}

}
