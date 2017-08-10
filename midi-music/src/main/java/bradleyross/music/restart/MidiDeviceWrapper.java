
package bradleyross.music.restart;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
// import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
/**
 * 
 * @author Bradley Ross
 *
 */
public class MidiDeviceWrapper {
	protected MidiDevice midiDevice = null;
	protected MidiDevice.Info midiDeviceInfo = null;
	protected boolean isSynthesizerImpl = false;
	protected boolean isSequencerImpl = false;
	public MidiDeviceWrapper(MidiDevice value) {
		midiDevice = value;
		midiDeviceInfo = midiDevice.getDeviceInfo();
	}
	public MidiDeviceWrapper(MidiDevice.Info value) 
			throws MidiUnavailableException {
		midiDeviceInfo = value;
		midiDevice = MidiSystem.getMidiDevice(midiDeviceInfo);
	}
	public MidiDevice getMidiDevice() {
		return midiDevice;
	}
	public MidiDevice.Info getMidiDeviceInfo() {
		return midiDeviceInfo;
	}
	protected void collectData() {
		if (Synthesizer.class.isAssignableFrom(midiDevice.getClass())) {
			isSynthesizerImpl = true;
		}
		if (Sequencer.class.isAssignableFrom(midiDevice.getClass())) {
			isSequencerImpl= true;
		}
	}
	public boolean isSequencer() { return isSequencerImpl; }
	public boolean isSynthesizer() { return isSynthesizerImpl; }
	public boolean isSource() {
		if (midiDevice.getMaxTransmitters() != 0) {
			return true; 
		} else {
			return false;
		}
	}
	public boolean isDestination() {
		if (midiDevice.getMaxReceivers() != 0) {
			return true;
		} else {
			return false;
		}
	}

}


