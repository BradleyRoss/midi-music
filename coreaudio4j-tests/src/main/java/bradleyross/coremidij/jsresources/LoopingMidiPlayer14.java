package bradleyross.coremidij.jsresources;
/*
 *	LoopingMidiPlayer14.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999 - 2001 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

import java.io.File;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;



/**	<titleabbrev>LoopingMidiPlayer14</titleabbrev>
	<title>Looping a MIDI file (JDK1.4 and earlier)</title>

	<formalpara><title>Purpose</title>

	<para>Loops a MIDI file using loopstart/loopend meta messages
	(JDK1.4 and earlier).</para></formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java LoopingMidiPlayer14</command>
	<arg choice="plain"><replaceable>midifile</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option><replaceable>midifile</replaceable></option></term>
	<listitem><para>the name of the MIDI file that should be
	played</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>

	<para>This program always uses the default Sequencer and the default
	Synthesizer to play on. For using non-default sequencers,
	synthesizers or to play on an external MIDI port, see
	<olink targetdoc="MidiPlayer"
	targetptr="MidiPlayer">MidiPlayer</olink>.</para>

	<para>This program does not work with the JDK 1.5. For looping
	using the JDK 1.5, see <olink targetdoc="LoopingMidiPlayer15"
	targetptr="LoopingMidiPlayer15">LoopingMidiPlayer15</olink>.</para>

	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="LoopingMidiPlayer14.java.html">LoopingMidiPlayer14.java</ulink>
	</para>
	</formalpara>

*/
public class LoopingMidiPlayer14
{
	public static void main(String[] args)
		throws MidiUnavailableException, InvalidMidiDataException, IOException
	{
		final Sequencer sequencer;

		/* We check if there is no command-line argument at all or the
		 * first one is '-h'.  If so, we display the usage message and
		 * exit.
		 */
		if (args.length == 0 || args[0].equals("-h"))
		{
			printUsageAndExit();
		}

		String	strFilename = args[0];
		File	midiFile = new File(strFilename);

		/* We read in the MIDI file to a Sequence object.  This object
		 * is set at the Sequencer later.
		 */
		Sequence sequence = MidiSystem.getSequence(midiFile);

		/* Here, we set the loop points to loop over the whole
		 * sequence. In order to do so, we insert loopstart and loopend
		 * meta messages into the sequence.
		 */

		Track track = sequence.getTracks()[0];
		final int MARKER = 6;
		long loopStartTick = 0;
		long loopEndTick = sequence.getTickLength();
		addMetaEvent(track, MARKER, "loopstart".getBytes(), loopStartTick);
		addMetaEvent(track, MARKER, "loopend".getBytes(), loopEndTick);

		/* Now, we need a Sequencer to play the sequence.  Here, we
		 * simply request the default sequencer with an implicitly
		 * connected synthesizer
		 */
		sequencer = MidiSystem.getSequencer();

		/* The Sequencer is still a dead object.  We have to open() it
		 * to become live.  This is necessary to allocate some
		 * ressources in the native part.
		 */
		sequencer.open();

		/* Next step is to tell the Sequencer which Sequence it has to
		 * play. In this case, we set it as the Sequence object
		 * created above.
		 */
		sequencer.setSequence(sequence);

		/* To free system resources, it is recommended to close the
		 * synthesizer and sequencer properly.
		 *
		 * To accomplish this, we register a Listener to the
		 * Sequencer. It is called when there are "meta" events. Meta
		 * event 47 is end of track.
		 *
		 * Thanks to Espen Riskedal for finding this trick.
		 */
		sequencer.addMetaEventListener(new MetaEventListener()
			{
				public void meta(MetaMessage event)
				{
					if (event.getType() == 47)
					{
						sequencer.close();
						System.exit(0);
					}
				}
			});

		/* Now, we can start over.
		 */
		sequencer.start();
	}



	private static void addMetaEvent(Track track, int type,
									 byte[] data, long tick)
	{
		MetaMessage message = new MetaMessage();
		try
		{
			message.setMessage(type, data, data.length);
			MidiEvent event = new MidiEvent( message, tick );
			track.add(event);
		}
		catch (InvalidMidiDataException e)
		{
			e.printStackTrace();
		}
	}


	private static void printUsageAndExit()
	{
		out("LoopingMidiPlayer14: usage:");
		out("\tjava LoopingMidiPlayer14 <midifile>");
		System.exit(1);
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** LoopingMidiPlayer14.java ***/
