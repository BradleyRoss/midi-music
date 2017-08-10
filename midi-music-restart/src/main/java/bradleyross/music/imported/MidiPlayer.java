package bradleyross.music.imported;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
// import javax.sound.midi.MidiDevice;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Sequence;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
// import javax.sound.midi.*;
// import java.io.*;




/* http://blog.taragana.com/index.php/archive/how-to-play-a-midi-file-from-a-java-application/ */


/**
 * Plays a midi file provided on command line.
 * 
 * <p>The first and only parameter is the name of the file to be played.</p>
 * 
 * <p>This is based on the GitHub Gist item <a href="http://gist.github.com/indy/360540"
 *    target-"_blank">indy/play_midi_filejava</a>.  It was mentioned in
 *    <a href="http://blog.taragana.com/index.php/archive/how-to-play-a-midi-file-from-a-java-application/"
 *    target="_blank">
 *    http://blog.taragana.com/index.php/archive/how-to-play-a-midi-file-from-a-java-application/</a></p>
 * <p>Running <code> java bradleyross.music.external.MidiPlayer ~/Downloads/AGNICRT.MID</code>
 *    played the music.  I just have to find out how this differs from my own
 *    program that won't play.</p>
 * @author Bradley Ross
 *
 */

public class MidiPlayer {
	/**
	 * Main driver program.
	 * 
	 * <p>The code for verifying that it is a MIDI file needs to be improved.</p>
	 * 
	 * @param args The first and only argument is the name of the file to be played
	 */
	protected String args[];
	public MidiPlayer(String value[]) {
		args = value;
	}
	public static void main(String args[]) {
		MidiPlayer instance = new MidiPlayer(args);
		instance.run();
	}
	protected void run () {
		String file;
		Sequence sequence = null;
		// Argument check
		if(args.length == 0) {
			/*
			 * Instead of exiting if the name of a MIDI file is not provided,
			 * the code was modified to play a MIDI file contained in the jar
			 * file instead.
			 */
			try {
				InputStream in = MidiPlayer.class.getClassLoader().getResourceAsStream("AGNICRT.MID");
				sequence = MidiSystem.getSequence(in);
			} catch (InvalidMidiDataException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				file = args[0];

				if(!file.toLowerCase().endsWith(".mid") && !file.toLowerCase().endsWith(".midi")) {
					helpAndExit();
				}

				File midiFile = new File(file);
				if(!midiFile.exists() || midiFile.isDirectory() || !midiFile.canRead()) {
					helpAndExit();
				}
				sequence = MidiSystem.getSequence(new File(file));
			}catch (InvalidMidiDataException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Play once
		try {
			Sequencer sequencer = MidiSystem.getSequencer();
			sequencer.open();
			sequencer.setSequence(sequence);
			// sequencer.open();
			sequencer.start();
			System.out.println("Starting Sequencer");
			while(true) {
				if(sequencer.isRunning()) {
					try {
						Thread.sleep(1000); // Check every second
					} catch(InterruptedException ignore) {
						break;
					}
				} else {
					break;
				}
			}
			// Close the MidiDevice & free resources
			sequencer.stop();
			sequencer.close();
		} catch(MidiUnavailableException mue) {
			System.out.println("Midi device unavailable!");
			mue.printStackTrace();
		} catch(InvalidMidiDataException imde) {
			System.out.println("Invalid Midi data!");
			imde.printStackTrace();
		} 
	}  
	/** Provides help message and exits the program */
	private static void helpAndExit() {
		System.out.println("Usage: java MidiPlayer midifile.mid");
		System.exit(1);
	}
}



