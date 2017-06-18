package org.jsresources;
/*
 *	SingleChannelStereoConverter.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 2004 by Matthias Pfisterer
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

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;



/**	<titleabbrev>SingleChannelStereoConverter</titleabbrev>
	<title>Converting mono to one channel of stereo</title>

	<formalpara><title>Purpose</title>

	<para>Converts mono audio or stereo file to a stereo audio file
	with the mono stream appearing either on the left or the right
	channel of the stereo stream, and the other channel muted. If the
	input is stereo, it is mixed down to a mono stream, which is then
	used as above. Besides the number of channels, the output file will have the same audio format as the input file.</para>
	</formalpara>

	<formalpara><title>Usage</title>
	<para>
	<cmdsynopsis>
	<command>java SingleChannelStereoConverter</command>
	<group>
	<arg><option>-l</option></arg>
	<arg><option>-r</option></arg>
	</group>
	<arg choice="plain"><replaceable class="parameter">input_file</replaceable></arg>
	<arg choice="plain"><replaceable class="parameter">output_file</replaceable></arg>
	</cmdsynopsis>
	</para></formalpara>

	<formalpara><title>Parameters</title>
	<variablelist>
	<varlistentry>
	<term><option>-l</option></term>
	<listitem><para>assigns the mono stream to the left channel</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><option>-r</option></term>
	<listitem><para>assigns the mono stream to the right channel</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">input_file</replaceable></term>
	<listitem><para>the file name of the audio file that audio data should be read from. This can be a mono or a stereo file.</para></listitem>
	</varlistentry>
	<varlistentry>
	<term><replaceable class="parameter">output_file</replaceable></term>
	<listitem><para>the file name of the audio file that audio data should be read from. This can be a mono or a stereo file.</para></listitem>
	</varlistentry>
	</variablelist>
	</formalpara>

	<formalpara><title>Bugs, limitations</title>

	<para>The converter processes only audio data in common PCM
	formats (8 bit unsigned, 16 bit signed). To process stereo input
	files, the PCM2PCM converter of <ulink
	url="http://www.tritonus.org/">Tritonus</ulink> is required. You
	can download it from <ulink
	url="http://www.tritonus.org/plugins.html">Tritonus
	Plug-ins</ulink>.</para>

	</formalpara>

	<formalpara><title>Source code</title>
	<para>
	<ulink url="SingleChannelStereoConverter.java.html">SingleChannelStereoConverter.java</ulink>,
	<ulink url="SingleChannelStereoAudioInputStream.java.html">SingleChannelStereoAudioInputStream.java</ulink>,
	<ulink url="AudioCommon.java.html">AudioCommon.java</ulink>
	</para>
	</formalpara>

*/
public class SingleChannelStereoConverter
{
	/**	Flag for debugging messages.
	 *	If true, some messages are dumped to the console
	 *	during operation.	
	 */
	private static final boolean DEBUG = true;



	public static void main(String[] args)
		throws Exception
	{
		if (args.length != 3)
		{
			printUsageAndExit();
		}
		// convert to left or right channel?
		boolean bLeft = true;
		if (args[0].equals("-r"))
			bLeft = false;

		String	strInputFilename = args[1];
		String	strOutputFilename = args[2];
		File	inputFile = new File(strInputFilename);
		File	outputFile = new File(strOutputFilename);

		/* We use the format of the input file for the output file. Therefore,
		   we first have to find out the file type of the input file.
		*/
		AudioFileFormat aff = AudioSystem.getAudioFileFormat(inputFile);
		AudioFileFormat.Type fileType = aff.getType();

		/* Reading the input file... */
		AudioInputStream ais = AudioSystem.getAudioInputStream(inputFile);

		/* ...converting to single channel stereo format... */
		ais = new SingleChannelStereoAudioInputStream(ais, bLeft);

		/* ...and writing to output file. */
		int	nWrittenBytes = 0;
		nWrittenBytes = AudioSystem.write(ais, fileType, outputFile);
		if (DEBUG) { out("Written bytes: " + nWrittenBytes); }
	}



	private static void printUsageAndExit()
	{
		out("SingleChannelStereoConverter: usage:");
		out("\tjava SingleChannelStereoConverter [-l|-r] <inputfile> <outputfile>");
		System.exit(0);
	}


	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** SingleChannelStereoConverter.java ***/
