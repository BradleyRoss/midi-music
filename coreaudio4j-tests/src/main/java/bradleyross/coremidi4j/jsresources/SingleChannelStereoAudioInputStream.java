package bradleyross.coremidi4j.jsresources;
/*
 *	SingleChannelStereoAudioInputStream.java
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;



/**	Converting a mono stream to single-sided a stereo stream.


	Note: skip(), available() and mark()/reset() are not tested!

	@author Matthias Pfisterer
 */
public class SingleChannelStereoAudioInputStream
extends AudioInputStream
{
	private static final boolean	DEBUG = false;

	/** Flag for writing silence samples.  If this flag is true, zero
		bytes for silence samples are not written to the array passed
		to read(). This assumes that the array has been filled with
		0's outside of this class, which is often the case. Non-zero
		bytes (e.g. 128 for unsigned 8 bit) are always written.
	*/
	private static boolean sm_bOptimizeSilenceWriting = true;

	/** The AudioInputStream for this one, already converted to mono.
	 */
	private AudioInputStream	m_sourceStream;

	/** Stream should appear left or right?  If true, the signal is
		put on the left channel and silence on the right, otherwise
		vice versa.
	*/
	private boolean m_bSignalOnLeftChannel;

	/* A silence sample (mono) in byte representation of this stream.
	 */
	private byte[] m_abSilenceSample;

	/** Intermediate buffer for read().  This reference to this buffer
		is an instance variable so that there is no need to allocate
		the buffer on each invocation of read(). We initialize it here
		with an array of length 0 because this saves the handling of
		null references.
	*/
	private byte[] m_abSourceBuffer = new byte[0];

	/** Constructor.

	@param sourceStream the stream this one should be based on. The
	stream has to be in a PCM encoding. It may be stereo or mono if
	Tritonus' PCM2PCM converter is available in the system. If not,
	only mono is allowed.

	@param bSignalOnLeftChannel determines on which of the stereo
	channels (left or right) the sourceStream should appear. Passing
	true puts the stream on the left side and silence on the right
	side, passing false does it the other way round.

	@throws IllegalArgumentException if the encoding of sourceStream
	is neither PCM_SIGNED nor PCM_UNSIGNED, or if the encoding is
	PCM_UNSIGNED and the sample size in bits is different from 8.
	*/
	public SingleChannelStereoAudioInputStream(AudioInputStream sourceStream,
											   boolean bSignalOnLeftChannel)
	{
		super(new ByteArrayInputStream(new byte[0]),
			  new AudioFormat(sourceStream.getFormat().getSampleRate(),
							  sourceStream.getFormat().getSampleSizeInBits(),
							  2, // always stereo
							  sourceStream.getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED),
							  sourceStream.getFormat().isBigEndian()),
		      sourceStream.getFrameLength());
		if (DEBUG) { out("SingleChannelStereoAudioInputStream.<init>(): begin"); }
		AudioFormat sourceFormat = sourceStream.getFormat();
		if (! AudioCommon.isPcm(sourceFormat.getEncoding()))
		{
			throw new IllegalArgumentException("source stream has to be PCM");
		}

		/* Convert to mono, if necessary. For this to work cleanly,
		 * the PCM2PCM converter of Tritonus is needed.
		 */
		if (sourceFormat.getChannels() != 1)
		{
			AudioFormat monoFormat = new AudioFormat(
				sourceFormat.getEncoding(),
				sourceFormat.getSampleRate(),
				sourceFormat.getSampleSizeInBits(),
				1,
				(sourceFormat.getSampleSizeInBits() + 7) / 8,
				sourceFormat.getFrameRate(),
				sourceFormat.isBigEndian());
			sourceStream = AudioSystem.getAudioInputStream(monoFormat, sourceStream);
		}
		m_sourceStream = sourceStream;
		m_bSignalOnLeftChannel = bSignalOnLeftChannel;

		/* This value is in bytes. Note that it is the storage size.
		   It may be four bytes for 24 bit samples.
		   The channel number is always 2 (stereo).
		*/
		int	nSampleSizeInBytes = getFormat().getFrameSize() / 2;
		m_abSilenceSample = new byte[nSampleSizeInBytes];
		/* For signed PCM representation, the values of the array
		   m_abSilenceSample are left with the initial value 0 (this
		   represents silence). For unsigned 8 bit, the value 128
		   represents silence. Therefore, the (single) byte is
		   initialized with this value. Unsigned formats with more
		   than 8 bits are not supported.
		*/
		if (getFormat().getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED))
		{
			if (getFormat().getSampleSizeInBits() == 8)
				m_abSilenceSample[0] = (byte) 128;
			else
				throw new IllegalArgumentException("unsigned formats are only supported for 8 bit");
		}
		if (DEBUG) { out("SingleChannelStereoAudioInputStream.<init>(): end"); }
	}



	public int read()
		throws	IOException
	{
		if (DEBUG) { out("SingleChannelStereoAudioInputStream.read(): begin"); }
		/* This stream is stereo, so the frame size is at least 2
		 * bytes.  Therefore, reading a single byte is always an
		 * error.
		 */
		throw new IOException("cannot read fraction of a frame");
	}



	public int read(byte[] abData, int nOffset, int nLength)
		throws	IOException
	{
		if (DEBUG)
		{
			out("SingleChannelStereoAudioInputStream.read(byte[], int, int): begin");
			out("SingleChannelStereoAudioInputStream.read(byte[], int, int): requested length: " + nLength);
		}
		// TODO: check if cacheing in an instance variable makes a difference in performance
		int	nThisFrameSize = getFormat().getFrameSize();
		/* This value is in bytes. Note that it is the storage size.
		   It may be four bytes for 24 bit samples.
		*/
		int nThisSampleSizeInBytes = nThisFrameSize / 2;
		int	nSourceFrameSize = m_sourceStream.getFormat().getFrameSize();
		if ((nLength % nThisFrameSize) != 0)
		{
			throw new IOException("cannot read fraction of a frame");
		}
		AudioFormat.Encoding	encoding = getFormat().getEncoding();
		if (DEBUG)
		{
			out("SingleChannelStereoAudioInputStream.read(byte[], int, int): frame size: " + nThisFrameSize);
			out("SingleChannelStereoAudioInputStream.read(byte[], int, int): encoding: " + encoding);
		}
		int nFrames = nLength / nThisFrameSize;
		int nUsedSourceBufferLength = nFrames * nSourceFrameSize;
		if (m_abSourceBuffer.length < nUsedSourceBufferLength)
		{
			m_abSourceBuffer = new byte[nUsedSourceBufferLength];
		}
		// using a local variable for performance
		byte[] abSourceBuffer = m_abSourceBuffer;
		// out("nUsedSourceBufferLength: " + nUsedSourceBufferLength);
		int	nBytesRead = m_sourceStream.read(abSourceBuffer, 0, nUsedSourceBufferLength);
		if (DEBUG) { out("SingleChannelStereoAudioInputStream.read(byte[], int, int): bytes read: " + nBytesRead); }
		if (nBytesRead == -1)
		{
			/*
			  The end of the source stream has been reached.
			  Nothing more to do here.
			*/
			return -1;
		}
		nFrames = nBytesRead / m_sourceStream.getFormat().getFrameSize();
		int nThisFrameBoundry = nOffset;
		int nSourceFrameBoundry = 0;
		int n;
		if (nThisSampleSizeInBytes == 2)
		{
			for (int i = 0; i < nFrames; i++)
			{
				int nSignalDestIndex;
				int nSilenceDestIndex;
				if (m_bSignalOnLeftChannel)
				{
					nSignalDestIndex = nThisFrameBoundry;
					nSilenceDestIndex = nThisFrameBoundry + nThisSampleSizeInBytes;
				}
				else
				{
					nSilenceDestIndex = nThisFrameBoundry;
					nSignalDestIndex = nThisFrameBoundry + nThisSampleSizeInBytes;
				}
				// signal
				abData[nSignalDestIndex] = abSourceBuffer[nSourceFrameBoundry];
				abData[nSignalDestIndex + 1] = abSourceBuffer[nSourceFrameBoundry + 1];
				// silence
				if (! sm_bOptimizeSilenceWriting)
				{
					abData[nSilenceDestIndex] = 0;
					abData[nSilenceDestIndex + 1] = 0;
				}
				nThisFrameBoundry += nThisFrameSize;
				nSourceFrameBoundry += nSourceFrameSize;
			}
		}
		else
		{
			for (int i = 0; i < nFrames; i++)
			{
				int nSignalDestIndex;
				int nSilenceDestIndex;
				if (m_bSignalOnLeftChannel)
				{
					nSignalDestIndex = nThisFrameBoundry;
					nSilenceDestIndex = nThisFrameBoundry + nThisSampleSizeInBytes;
				}
				else
				{
					nSilenceDestIndex = nThisFrameBoundry;
					nSignalDestIndex = nThisFrameBoundry + nThisSampleSizeInBytes;
				}
				// signal
				n = 0;
				while (n < nThisSampleSizeInBytes)
				{
					abData[nSignalDestIndex + n] = abSourceBuffer[nSourceFrameBoundry + n];
					n++;
				}
				// silence
				if (! sm_bOptimizeSilenceWriting)
				{
					n = 0;
					while (n < nThisSampleSizeInBytes)
					{
						abData[nSilenceDestIndex + n] = 0;
						n++;
					}
				}
				nThisFrameBoundry += nThisFrameSize;
				nSourceFrameBoundry += nSourceFrameSize;
			}
		}
		if (DEBUG) { out("SingleChannelStereoAudioInputStream.read(byte[], int, int): end"); }
		return nFrames * nThisFrameSize;
	}



	/**
	   Calls skip() on the source stream. Since the source stream may
	   have a different frame size, The number of bytes is
	   recalculated, so that the number of skipped frames is the same
	   as requested.
	*/
	public long skip(long lLength)
		throws	IOException
	{
		// frame size of this stream
		int nThisFrameSize = getFormat().getFrameSize();
		// frame size of source stream
		int nSourceFrameSize = m_sourceStream.getFormat().getFrameSize();

		long lBytesInSource = (lLength / nThisFrameSize) * nSourceFrameSize;
		long lBytesSkippedInSource = m_sourceStream.skip(lBytesInSource);
		return (lBytesSkippedInSource / nSourceFrameSize) * nThisFrameSize;
	}



	/**
	   The minimum of available() of all input stream is calculated and returned.
	*/
	public int available()
		throws	IOException
	{
		// frame size of this stream
		int nThisFrameSize = getFormat().getFrameSize();
		// frame size of source stream
		int nSourceFrameSize = m_sourceStream.getFormat().getFrameSize();

		int	nAvailableInSource = m_sourceStream.available();
		return (nAvailableInSource / nSourceFrameSize) * nThisFrameSize;
	}



	public void close()
		throws	IOException
	{
		m_sourceStream.close();
	}



	/**
	   Recalculates nReadLimit to an equivalent number (same number of
	   frames) for the source stream and calls mark() on it.
	*/
	public void mark(int nReadLimit)
	{
		// frame size of this stream
		int nThisFrameSize = getFormat().getFrameSize();
		// frame size of source stream
		int nSourceFrameSize = m_sourceStream.getFormat().getFrameSize();

		int nSourceReadLimit = (nReadLimit / nThisFrameSize) * nSourceFrameSize;
		m_sourceStream.mark(nSourceReadLimit);
	}


	/**
	   Calls reset() on the source stream.
	*/
	public void reset()
		throws	IOException
	{
		m_sourceStream.reset();
	}



	/**
	   returns true if the source stream return true for
	   markSupported().
	*/
	public boolean markSupported()
	{
		return m_sourceStream.markSupported();
	}



	/** Print a message to standard output.
		@param strMessage the string that should be printed
	*/
	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** SingleChannelStereoAudioInputStream.java ***/
