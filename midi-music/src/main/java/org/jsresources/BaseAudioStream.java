package org.jsresources;
/*
 *	BaseAudioStream.java
 *
 *	This file is part of jsresources.org
 */

/*
 * Copyright (c) 1999, 2000 by Matthias Pfisterer
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


import java.io.File;
import java.io.IOException;

import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.FloatControl;




public class BaseAudioStream
	implements	Runnable
{
	/**	Flag for debugging messages.
	 *	If true, some messages are dumped to the console
	 *	during operation.	
	 */
	private static boolean	DEBUG = true;

	/**
	 *	means that the stream has reached EOF or was not started.
	 *	This value is returned in property change callbacks that
	 *	report the current media position.
	 */
	public static final long	MEDIA_POSITION_EOF = -1L;
	public static final String	MEDIA_POSITION_PROPERTY = "BaseAudioStream_media_position";

	// TODO: better size
	private static final int	EXTERNAL_BUFFER_SIZE = 4000 * 4;

	private Thread			m_thread = null;
	private Object			m_dataSource;
	private AudioInputStream	m_audioInputStream;
	private SourceDataLine		m_line;
	private FloatControl		m_gainControl;
	private FloatControl		m_panControl;


	/**
	 *	This variable is used to distinguish stopped state from
	 *	paused state. In case of paused state, m_bRunning is still
	 *	true. In case of stopped state, it is set to false. Doing so
	 *	will terminate the thread.
	 */
	private boolean			m_bRunning;


	protected BaseAudioStream()
	{
		m_dataSource = null;
		m_audioInputStream = null;
		m_line = null;
		m_gainControl = null;
		m_panControl = null;
	}



	protected void setDataSource(File file)
		throws	UnsupportedAudioFileException, LineUnavailableException, IOException
	{
		m_dataSource = file;
		initAudioInputStream();
	}



	protected void setDataSource(URL url)
		throws	UnsupportedAudioFileException, LineUnavailableException, IOException
	{
		m_dataSource = url;
		initAudioInputStream();
	}		



	private void initAudioInputStream()
		throws	UnsupportedAudioFileException, LineUnavailableException, IOException
	{
		if (m_dataSource instanceof URL)
		{
			initAudioInputStream((URL) m_dataSource);
		}
		else if (m_dataSource instanceof File)
		{
			initAudioInputStream((File) m_dataSource);
		}
	}



	private void initAudioInputStream(File file)
		throws	UnsupportedAudioFileException, IOException
	{
/*
		try
		{
*/
			m_audioInputStream = AudioSystem.getAudioInputStream(file);
/*
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("cannot create AudioInputStream for " + file);
		}
		if (m_audioInputStream == null)
		{
			throw new IllegalArgumentException("cannot create AudioInputStream for " + file);
		}
*/
	}



	private void initAudioInputStream(URL url)
		throws	UnsupportedAudioFileException, IOException
	{
/*
		try
		{
*/
			m_audioInputStream = AudioSystem.getAudioInputStream(url);
/*
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("cannot create AudioInputStream for " + url);
		}
		if (m_audioInputStream == null)
		{
			throw new IllegalArgumentException("cannot create AudioInputStream for " + url);
		}
*/
	}



	// from AudioPlayer.java
		/*
		 *	Compressed audio data cannot be fed directely to
		 *	Java Sound. It has to be converted explicitely.
		 *	To do this, we create a new AudioFormat that
		 *	says to which format we want to convert to. Then,
		 *	we try to get a converted AudioInputStream.
		 *	Furthermore, we use the new format and the converted
		 *	stream.
		 *
		 *	Note that the technique shown here is partly non-
		 *	portable. It is used here to keep the example
		 *	simple. A more advanced, more portable technique
		 *	will (hopefully) show up in BaseAudioStream.java soon.
		 *
		 *	Thanks to Christoph Hecker for finding out that this
		 *	was missing.
		 */
/*
		if ((audioFormat.getEncoding() == AudioFormat.Encoding.ULAW) ||
		    (audioFormat.getEncoding() == AudioFormat.Encoding.ALAW)) 
		{
			if (DEBUG)
			{
				out("AudioPlayer.main(): converting");
			}
			AudioFormat newFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED, 
				audioFormat.getSampleRate(),
				audioFormat.getSampleSizeInBits() * 2,
				audioFormat.getChannels(),
				audioFormat.getFrameSize() * 2,
				audioFormat.getFrameRate(),
				true);
			AudioInputStream	newStream = AudioSystem.getAudioInputStream(newFormat, audioInputStream);
			audioFormat = newFormat;
			audioInputStream = newStream;
                }
*/



	protected void initLine()
		throws	LineUnavailableException
	{
		if (m_line == null)
		{
			createLine();
			openLine();
		}
		else
		{
			AudioFormat	lineAudioFormat = m_line.getFormat();
			AudioFormat	audioInputStreamFormat = m_audioInputStream == null ? null : m_audioInputStream.getFormat();
			if (!lineAudioFormat.equals(audioInputStreamFormat))
			{
				m_line.close();
				openLine();
			}
		}
	}



	private void createLine()
		throws	LineUnavailableException
	{
		if (m_line != null)
		{
			return;
		}
		/*
		 *	From the AudioInputStream, i.e. from the sound file, we
		 *	fetch information about the format of the audio data. These
		 *	information include the sampling frequency, the number of
		 *	channels and the size of the samples. There information
		 *	are needed to ask Java Sound for a suitable output line
		 *	for this audio file.
		 */
		AudioFormat	audioFormat = m_audioInputStream.getFormat();
		if (DEBUG)
		{
			out("BaseAudioStream.initLine(): audio format: " + audioFormat);
		}

		/*
		 *	Asking for a line is a rather tricky thing.
		 *	...
		 *	Furthermore, we have to give Java Sound a hint about how
		 *	big the internal buffer for the line should be. Here,
		 *	we say AudioSystem.NOT_SPECIFIED, signaling that we don't
		 *	care about the exact size. Java Sound will use some default
		 *	value for the buffer size.
		 */
		DataLine.Info	info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
		m_line = (SourceDataLine) AudioSystem.getLine(info);

		if (m_line.isControlSupported(FloatControl.Type.MASTER_GAIN/*VOLUME*/))
		{
			m_gainControl = (FloatControl) m_line.getControl(FloatControl.Type.MASTER_GAIN);
			if (DEBUG)
			{
				out("max gain: " + m_gainControl.getMaximum());
				out("min gain: " + m_gainControl.getMinimum());
				out("gain precision: " + m_gainControl.getPrecision());
			}
		}
		else
		{
			if (DEBUG)
			{
				out("FloatControl.Type.MASTER_GAIN is not supported");
			}
		}
		if (m_line.isControlSupported(FloatControl.Type.PAN/*BALANCE*/))
		{
			m_panControl = (FloatControl) m_line.getControl(FloatControl.Type.PAN);
			if (DEBUG)
			{
				out("max balance: " + m_panControl.getMaximum());
				out("min balance: " + m_panControl.getMinimum());
				out("balance precision: " + m_panControl.getPrecision());
			}
		}
		else
		{
			if (DEBUG)
			{
				out("FloatControl.Type.PAN is not supported");
			}
		}
	}



	private void openLine()
		throws	LineUnavailableException
	{
		if (m_line == null)
		{
			return;
		}
		AudioFormat	audioFormat = m_audioInputStream.getFormat();
		m_line.open(audioFormat, m_line.getBufferSize());
	}



	// TODO: if class can be instatiated without file or url, m_audioInputStream may
	// be null
	protected AudioFormat getFormat()
	{
		return m_audioInputStream.getFormat();
	}



	public void start()
	{
		if (DEBUG)
		{
			out("start() called");
		}
		if (!(m_thread == null || !m_thread.isAlive()))
		{
			if (DEBUG)
			{
				out("WARNING: old thread still running!!");
			}
		}
		if (DEBUG)
		{
			out("creating new thread");
		}
		m_thread = new Thread(this);
		m_thread.start();
		if (DEBUG)
		{
			out("additional thread started");
		}
		if (DEBUG)
		{
			out("starting line");
		}
		m_line.start();
	}



	protected void stop()
	{
		if (m_bRunning)
		{
			if (m_line != null)
			{
				m_line.stop();
				m_line.flush();
			}
			m_bRunning = false;
			/*
			 *	We re-initialize the AudioInputStream. Since doing
			 *	a stop on the stream implies that there has been
			 *	a successful creation of an AudioInputStream before,
			 *	we can almost safely ignore this exception.
			 *	The LineUnavailableException can be ignored because
			 *	in case of reinitializing the same AudioInputStream,
			 *	no new line is created or opened.
			 */
			try
			{
				initAudioInputStream();
			}
			catch (UnsupportedAudioFileException e)
			{
			}
			catch (LineUnavailableException e)
			{
			}
			catch (IOException e)
			{
			}
		}
	}



	public void pause()
	{
		m_line.stop();
	}



	public void resume()
	{
		m_line.start();
	}



	public void run()
	{
		if (DEBUG)
		{
			out("thread start");
		}
		int	nBytesRead = 0;
		m_bRunning = true;
		byte[]	abData = new byte[EXTERNAL_BUFFER_SIZE];
		// int	nFrameSize = m_line.getFormat().getFrameSize();
		while (nBytesRead != -1 && m_bRunning)
		{
			try
			{
				nBytesRead = m_audioInputStream.read(abData, 0, abData.length);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (nBytesRead >= 0)
			{
				//int	nFramesToWrite = nBytesRead / nFrameSize;
				if (DEBUG)
				{
					out("Trying to write: " + nBytesRead);
				}
				int	nBytesWritten = m_line.write(abData, 0, nBytesRead);
				if (DEBUG)
				{
					out("Written: " + nBytesWritten);
				}
			}
		}

		/*
		 *	Wait until all data are played.
		 *	This is only necessary because of the bug noted below.
		 *	(If we do not wait, we would interrupt the playback by
		 *	prematurely closing the line and exiting the VM.)
		 */
		// TODO: check how this interferes with stop()
		m_line.drain();
		if (DEBUG)
		{
			out("after drain()");
		}

		/*
		 *	Stop the line and reinitialize the AudioInputStream.
		 *	This should be done before reporting end-of-media to be
		 *	prepared if the EOM message triggers a new start().
		 */
		stop();
		if (DEBUG)
		{
			out("after this.stop()");
		}
	}


	public boolean hasGainControl()
	{
		return m_gainControl != null;
	}

/*
	public void setMute(boolean bMute)
	{
		if (hasGainControl())
		{
			m_gainControl.setMute(bMute);
		}
	}



	public boolean getMute()
	{
		if (hasGainControl())
		{
			return m_gainControl.getMute();
		}
		else
		{
			return false;
		}
	}
*/



	public void setGain(float fGain)
	{
		if (hasGainControl())
		{
			m_gainControl.setValue(fGain);
		}
	}



	public float getGain()
	{
		if (hasGainControl())
		{
			return m_gainControl.getValue();
		}
		else
		{
			return 0.0F;
		}
	}


	public float getMaximum()
	{
		if (hasGainControl())
		{
			return m_gainControl.getMaximum();
		}
		else
		{
			return 0.0F;
		}
	}


	public float getMinimum()
	{
		if (hasGainControl())
		{
			return m_gainControl.getMinimum();
		}
		else
		{
			return 0.0F;
		}
	}


	public boolean hasPanControl()
	{
		return m_panControl != null;
	}



	public float getPrecision()
	{
		if (hasPanControl())
		{
			return m_panControl.getPrecision();
		}
		else
		{
			return 0.0F;
		}
	}



	public float getPan()
	{
		if (hasPanControl())
		{
			return m_panControl.getValue();
		}
		else
		{
			return 0.0F;
		}
	}



	public void setPan(float fPan)
	{
		if (hasPanControl())
		{
			m_panControl.setValue(fPan);
		}
	}



	private static void out(String strMessage)
	{
		System.out.println(strMessage);
	}
}



/*** BaseAudioStream.java ***/

