package bradleyross.music.MidiRouter;
// import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
// import java.awt.GridBagLayoutInfo;
import java.awt.GridBagConstraints;
import java.awt.HeadlessException;
import java.awt.Dimension;
import java.awt.Color;
// import java.awt.Component;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
// import java.awt.event.ComponentListener;
// import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
// import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
// import java.util.GregorianCalendar;
import javax.sound.midi.InvalidMidiDataException;
// import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;
// import javax.sound.midi.MidiDeviceReceiver;
// import javax.sound.midi.MidiDeviceTransmitter;


import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
// import javax.swing.JButton;
import javax.swing.BoxLayout;
// import javax.swing.border.Border;
import javax.swing.BorderFactory;
// import javax.swing.JTextField;
import javax.swing.JLabel;
/**
 * Sample Java MIDI router.
 * 
 * @author Bradley Ross
 * 
 * <p>Does MidiSystem.getMidiDevice always return the same object given the
 *    same MidiDevice.Info - The answer is no when the MIDI device is
 *    a software synthesizer or sequencer.</p>
 * 
 *
 * @see GridLayout
 * @see GridBagLayout
 * @see BoxLayout
 *
 */
public class MidiRouter implements Runnable{
	/**
	 * A value of zero means no diagnostic output, while 
	 * higher values create more output.
	 */
	protected static int debugFlag = 1;
	public static void setDebugFlag(int value) {
		debugFlag = value;
	}
	public static int getDebugFlag() {
		return debugFlag;
	}
	/**
	 * if true, use CoreMidi4J instead of Core Java.
	 */
	protected boolean useCoreMidi4J = false;
	/**
	 * Use one {@link Connection} object for both the data
	 * flow and logging.
	 */
	protected boolean useCombinedConnection = true;
	/**
	 * Outermost panel for the main window.
	 */
	protected JPanel outer;
	/**
	 * Pane at the top of the main window that
	 * contains general information.
	 */
	protected JPanel top;
	/**
	 * Pane containing a list of potential MIDI
	 * sources.
	 */
	protected JPanel left;
	/**
	 * Pane containing a list of potential MIDI
	 * destinations.
	 */
	protected JPanel right;
	/**
	 * Swing {@link JFrame} attached to the main window.
	 */
	protected JFrame frame;
	/**
	 * Menu bar for the main window.
	 */
	protected JMenuBar menuBar;


	protected String title;
	/**
	 * MIDI file to be used as source of MIDI information
	 * (e.g.: {@link Sequencer}).
	 */
	protected File sourceFile;
	/**
	 * MIDI file to be used as destination of MIDI
	 * information (e.g.: {@link Sequencer} and
	 * {@link Synthesizer})
	 */
	protected File destFile;
	protected MidiDevice.Info[] allDevices;
	protected ArrayList<MidiDevice> sourceMidiDevices;
	protected ArrayList<MidiDevice> destMidiDevices;
	protected ArrayList<MidiDevice.Info> sourceDevices;
	protected ArrayList<MidiDevice.Info> destDevices; 
	protected ArrayList<Listing> sourceItems;
	protected ArrayList<Listing>  destItems;

	protected MidiMenuBar midiMenuBar = null;
	protected File inputFile = null;
	protected File outputFile = null;
	protected File logFile = null;
	protected Listing selectedSource = null;
	protected Listing selectedDest = null;
	protected Connection connection = new Connection();
	/**
	 * This acts as a {@link Receiver} of MIDI
	 * information and displays the data in a window.
	 */
	protected LogReceiver logReceiver;
	// protected GregorianCalendar calendar;
	/**
	 * Time MIDI connection was opened.
	 */
	protected long musicStart;
	/**
	 * This Connection object will send information from the
	 * source object to the log window.
	 */
	protected Connection logConnection = new Connection();
	/**
	 * If true, no changes are allowed to the selections in the
	 * source and destination columns.
	 */
	boolean selectionLocked = false;
	/**
	 * If true, MIDI messages from the source will be echoed to a
	 * Java Swing window.
	 */
	boolean useLogWindow = true;
	/**
	 * Specifies the way in which an endpoint is specified.
	 * 
	 * @author Bradley Ross
	 *
	 */
	protected enum specType { 
		/**
		 * Specifying a {@link Receiver} object.
		 */
		RECEIVER, 
		/**
		 * Specifying a {@link Transmitter} object.
		 */
		TRANSMITTER, 
		/**
		 * Specifying a {@link MidiDevice} object.
		 */
		DEVICE, 
		/**
		 * Specifying a {@link Info MidiDevice.Info} object.
		 */
		INFO, 
		/**
		 * Value has not been set
		 * 
		 */
		NONE
	}
	public MidiRouter() {
		logReceiver = new LogReceiver();
		// calendar = new GregorianCalendar();
	}
	/**
	 * Constructs the menu bar.
	 * 
	 * @author Bradley Ross
	 *
	 */
	protected class MidiMenuBar implements ActionListener {
		JMenuItem toggleLog = null;
		/**
		 * This is the menu bar for the application.
		 */
		JMenuBar menuBar = null;
		/**
		 * Constructor populates Java application menu bar.
		 * 
		 * @param value JMenuBar structure belonging to {@link #frame}.
		 */
		public MidiMenuBar(JMenuBar value) {
			menuBar = value;
			/*
			 * Main menu
			 */
			JMenu mainMenu = new JMenu("Midi Router");
			menuBar.add(mainMenu);
			JMenuItem aboutRouter = new JMenuItem("About MIDI Router");
			mainMenu.add(aboutRouter);
			aboutRouter.addActionListener(this);
			aboutRouter.setActionCommand("about");
			mainMenu.addSeparator();
			JMenuItem preferences = new JMenuItem("Preferences");
			preferences.setActionCommand("preferences");
			mainMenu.add(preferences);
			preferences.addActionListener(this);
			mainMenu.addSeparator();
			JMenuItem quit = new JMenuItem("Quit");
			quit.setActionCommand("quit");
			mainMenu.add(quit);
			quit.addActionListener(this);
			/*
			 * File menu
			 */
			JMenu fileMenu = new JMenu("File");
			JMenuItem setInput = new JMenuItem("Choose Input File");
			setInput.setActionCommand("setInput");
			setInput.addActionListener(this);
			JMenuItem setOutput = new JMenuItem("Choose Output File");
			setOutput.setActionCommand("setOutput");
			setOutput.addActionListener(this);
			JMenuItem setLog = new JMenuItem("Choose Log File");
			setLog.setActionCommand("setLog");
			setLog.addActionListener(this);
			toggleLog = new JMenuItem();
			if (useLogWindow) {
				toggleLog.setText("Turn Log Window off");
			} else {
				toggleLog.setText("Turn Log Window on");
			}
			toggleLog.setActionCommand("toggleLog");
			toggleLog.addActionListener(this);
			menuBar.add(fileMenu);
			fileMenu.add(setInput);
			fileMenu.add(setOutput);
			fileMenu.add(setLog);
			fileMenu.add(toggleLog);
			/*  Connections menu */
			JMenu connections = new JMenu("Connections");
			JMenuItem openConnection = new JMenuItem("Open");
			openConnection.setActionCommand("openConnection");
			openConnection.addActionListener(this);
			connections.add(openConnection);
			JMenuItem closeConnection = new JMenuItem("Close/Save");
			closeConnection.setActionCommand("closeConnection");
			closeConnection.addActionListener(this);
			connections.add(closeConnection);
			menuBar.add(connections);
		}
		/**
		 * Listener for menu bar.
		 * 
		 * @param e action transmitted from clicking menu item
		 */
		public void actionPerformed(ActionEvent e) {
			try {
				String command = e.getActionCommand();
				if (debugFlag > 5) {
					System.out.println(command);
				}
				if (command.equalsIgnoreCase("about")) {
					System.out.println("About menu item clicked");

				} else if (command.equalsIgnoreCase("preferences")) {
					System.out.println("Preferences menu item clicked");

				} else if (command.equalsIgnoreCase("quit")) {
					frame.setVisible(false);
					frame.dispose();
				} else if (command.equalsIgnoreCase("setInput")) {
					if (debugFlag > 0) {
						System.out.println("Choose Input File menu item clicked");
					}
					JFileChooser chooser = new JFileChooser();
					int returnVal = chooser.showOpenDialog(frame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						inputFile = chooser.getSelectedFile();
					}
					if (inputFile != null) {
						String text = "Input file is " + inputFile.getName();
						System.out.println(text);
					} else {
						showPopup("Problem", "No input file selected");
					}
				} else if (command.equalsIgnoreCase("setOutput")) {
					if (debugFlag > 0) {
						System.out.println("Choose Output File menu item clicked");
					}
					JFileChooser chooser = new JFileChooser();
					int returnVal = chooser.showSaveDialog(frame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						outputFile = chooser.getSelectedFile();
					}
					if (outputFile != null) {
						String text = "Output File is " + outputFile.getName();
						System.out.println(text);
					} else {
						showPopup("Problem", "No output file selected");
					}

				} else if (command.equalsIgnoreCase("setLog")) {
					if (debugFlag > 0) {
						System.out.println("Choose Log File menu item clicked");
					}
					JFileChooser chooser = new JFileChooser();
					int returnVal = chooser.showSaveDialog(frame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						logFile = chooser.getSelectedFile();
					}
					if (logFile != null) {
						String text = "Log File is " + logFile.getName();
						System.out.println(text);
					} else {
						showPopup("Problem", "No log file selected");
					}
				} else if (command.equalsIgnoreCase("openConnection")) {
					if (selectionLocked) { return; }
					selectionLocked = true;
					if (debugFlag > 0) {
						System.out.println("Open Connection menu item clicked");
						if (selectedSource != null && selectedSource.isSequencer) {
							System.out.println("Source is sequencer");
						}
						if (selectedDest != null && selectedDest.isSequencer) {
							System.out.println("Destination is sequencer");
						}
						if (selectedDest != null && selectedDest.isSynthesizer) {
							System.out.println("Destination is synthesizer");
						}
					}
					if (selectedSource == null) {
						String text = "Source MIDI not selected - Select a source";
						showPopup("Problem", text);
						return;
					}

					if (selectedDest == null) {
						String text = "Destination MIDI not selected - Select a destination";
						showPopup("Problem", text);
						return;
					}
					if (selectedSource.isSequencer && 
							(inputFile == null || !inputFile.exists())) {
						String text = "Must select input file for sequencer as input" +
								System.lineSeparator() +
								"Use File -> Choose Input File menu item to select file";
						showPopup("Problem", text);
						return;
					}

					if (selectedDest.isSequencer && 	outputFile == null) {
						String text = "Must select output file for sequencer as output" +
								System.lineSeparator() +
								"Use File -> Choose Output File menu item to select file";
						showPopup("Problem", text);
						return;
					}
					if (useLogWindow && !useCombinedConnection) {
						StringBuffer text = 
								new StringBuffer("Starting setup of connection to log window");
						System.out.println(text.toString());
						logConnection.setSourceDevice(selectedSource.getMidiDevice());
						logConnection.setDestReceiver(logReceiver);
						logConnection.openConnection();
						if (debugFlag > 0) {
							System.out.println("Log connection opened");
						}
					}
					MidiDevice sourceMidiDevice = selectedSource.getMidiDevice();
					connection.setSourceDevice(sourceMidiDevice);
					if (!sourceMidiDevice.isOpen()) {
						sourceMidiDevice.open();
					}
					MidiDevice destMidiDevice = selectedDest.getMidiDevice();
					connection.setDestDevice(destMidiDevice);
					if (!destMidiDevice.isOpen()) {
						destMidiDevice.open();
					}
					// connection.setSourceInfo(selectedSource.getMidiDeviceInfo());
					// connection.setDestInfo(selectedDest.getMidiDeviceInfo());
					if (sourceFile != null) {
						connection.setSourceFile(sourceFile);
					}
					if (destFile != null) {
						connection.setDestFile(destFile);
					}
					connection.openConnection();	
					if (debugFlag > 0) {
						System.out.println("Data connection opened");
						MidiDevice source = selectedSource.getMidiDevice();
						MidiDevice dest = selectedDest.getMidiDevice();
						StringBuffer text2 = new StringBuffer("     * There are " +
								Integer.toString(source.getTransmitters().size()) +
								" transmitters for source");
						text2.append(System.lineSeparator());
						text2.append("     * There are " +
								Integer.toString(dest.getReceivers().size()) +
								" receivers for destination");
						System.out.println(text2.toString());
					}
					if (selectedSource.isSequencer) {
						if (debugFlag > 0) {
							System.out.println("About to start playing MIDI file");
						}
						Sequence sequence2 = MidiSystem.getSequence(new FileInputStream(inputFile));
						Sequencer sequencer = (Sequencer) selectedSource.getMidiDevice();
						sequencer.setSequence(sequence2);
						sequencer.start();
					} else if (selectedDest.isSequencer) {
						Sequencer recorder = (Sequencer) selectedDest.getMidiDevice();
						Sequence sequence = recorder.getSequence();
						Track track = sequence.createTrack();
						recorder.recordEnable(track, -1);
						recorder.startRecording();
					}

				} else if (command.equalsIgnoreCase("closeConnection")) {
					if (selectedDest.isSequencer) {
						Sequencer recorder = (Sequencer) selectedDest.getMidiDevice();
						recorder.stopRecording();
						Sequence sequence = recorder.getSequence();
						int[] types = MidiSystem.getMidiFileTypes(sequence);
						for (int i = 0; i < types.length; i++) {
							System.out.println(Integer.toString(types[i]));
						}
						MidiSystem.write(sequence, types[0], outputFile);
					}
					if (!selectionLocked) { return; }
					selectionLocked = false;
					connection.closeConnection();
					System.out.println("Data connection closed");
					if (useLogWindow && !useCombinedConnection) {
						logConnection.closeConnection();
						System.out.println("Log connection closed");
					}
					// selectedSource.getMidiDevice().close();
					// selectedDest.getMidiDevice().close();
				} else if (command.equalsIgnoreCase("toggleLog")) {
					if (selectionLocked) { return; }
					if (useLogWindow) {
						useLogWindow = false;
						toggleLog.setText("Turn Log Window on");
					} else {
						useLogWindow = true;
						toggleLog.setText("Turn Log Window off");
					}
				}
			} catch (HeadlessException e2) {
				e2.printStackTrace();
				String text = "Display not found";
				showPopup("Error", text);
			} catch (MidiUnavailableException ex) {
				ex.printStackTrace();
				String text = "MidiUnavailableException Error";
				showPopup("Error", text);
			} catch (InvalidMidiDataException ex) {
				ex.printStackTrace();
				showPopup("Error", "InvalidMidiDataException");
			} catch (IOException ex) {
				ex.printStackTrace();
				showPopup("Error", "IOException error");
			}
		}
	}
	/** 
	 * Represents connection between MIDI devices.
	 * 
	 * @author Bradley Ross
	 *
	 */
	protected class Connection {
		/**
		 * sends incoming messages to multiple destinations.
		 */
		protected Splitter splitter = new Splitter();
		/**
		 * Type of specification for the source of the connection.
		 */
		protected specType sourceType = specType.NONE;
		/**
		 * {@link Info} object for the source
		 * of the connection.
		 */
		protected MidiDevice.Info sourceInfo = null;
		/**
		 * Device that is source of connection.
		 */
		protected MidiDevice sourceDevice = null;
		// protected Transmitter sourceTransmitter = null;
		protected boolean sourceIsSequencer = false;
		protected boolean sourceIsSynthesizer = false;
		protected File sourceFile = null;
		protected Receiver receiver = null;
		protected Transmitter transmitter = null;
		/**
		 * Provide information on the source device.
		 * @param value MidiDevice used for input to connection
		 */
		public void setSourceDevice(MidiDevice value) throws MidiUnavailableException {
			if (debugFlag > 0) {
				System.out.println("Starting Connection.setSourceDevice");
			}
			sourceType = specType.DEVICE;
			sourceDevice = value;
			sourceInfo = value.getDeviceInfo();
			if (Sequencer.class.isAssignableFrom(sourceDevice.getClass())) {
				sourceIsSequencer = true;
			}
			if (Synthesizer.class.isAssignableFrom(sourceDevice.getClass())) {
				sourceIsSynthesizer = true;
			}
		}
		public MidiDevice getSourceDevice() {
			return sourceDevice;
		}
		/**
		 * Specify device providing input to connection.
		 * <p>This method is deprecated because there is a problem
		 *    with the Java code when dealing with software synthesizers
		 *    and sequencers.  {@link MidiDevice#getDeviceInfo()} and
		 *    {@link MidiSystem#getMidiDevice(MidiDevice.Info)} are not
		 *    inverse functions in this situation.</p>
		 *    
		 * @param value input device
		 */
		@Deprecated
		public void setSourceInfo(MidiDevice.Info value) {
			sourceInfo = value;
			sourceType = specType.INFO;
			try {
				sourceDevice = MidiSystem.getMidiDevice(sourceInfo);
				if (Sequencer.class.isAssignableFrom(sourceDevice.getClass())) {
					sourceIsSequencer = true;
				}
				if (Synthesizer.class.isAssignableFrom(sourceDevice.getClass())) {
					sourceIsSynthesizer = true;
				}
			} catch (MidiUnavailableException ex) {
				ex.printStackTrace();
			}
		}
		/**
		 * Specifies the {@link Transmitter} object that is the
		 * source of the MIDI data.
		 * @param value
		 */
		public void setSourceTransmitter(Transmitter value) {
			if (debugFlag > 0) {
				System.out.println("Starting Connection.setSourceTransmitter");
			}
			if (value == null) {
				throw new NullPointerException("Input value is null");
			}
			sourceType = specType.TRANSMITTER;
			transmitter = value;
		}
		public MidiDevice.Info getSourceInfo() {
			return sourceInfo;
		}
		public void setSourceFile(File value) {
			sourceFile = value;
		}
		public File getSourceFile() {
			return sourceFile;
		}
		/**
		 * Type of specification for the destination of the connection.
		 */
		protected specType destType = specType.NONE;
		protected MidiDevice.Info destInfo = null;
		protected MidiDevice destDevice = null;
		// protected Receiver destReceiver = null;
		protected boolean destIsSequencer = false;
		protected boolean destIsSynthesizer  = false;
		protected File destFile = null;
		protected boolean active = false;
		/**
		 * Specify the MIDI device to be used for the destination.
		 * @param value MidiDevice object representing destination
		 * @throws MidiUnavailableException
		 */
		public void setDestDevice(MidiDevice value) throws MidiUnavailableException {
			if (value == null) {
				throw new NullPointerException("Input value is null");
			}
			if (debugFlag > 0) {
				System.out.println("Starting Connection.setDestDevice");
			}
			destDevice = value;
			destType = specType.DEVICE;
			destInfo = value.getDeviceInfo();
			if (Sequencer.class.isAssignableFrom(destDevice.getClass())) {
				destIsSequencer = true;
			}
			if (Synthesizer.class.isAssignableFrom(destDevice.getClass())) {
				destIsSynthesizer = true;
				((Synthesizer) destDevice).getDefaultSoundbank();
			}


		}
		/**
		 * Specify MIDI device to receive information from connection.
		 * 
		 * <p>This method is deprecated because there is a problem
		 *    with the Java code when dealing with software synthesizers
		 *    and sequencers.  {@link MidiDevice#getDeviceInfo()} and
		 *    {@link MidiSystem#getMidiDevice(MidiDevice.Info)} are not
		 *    inverse functions in this situation.</p>
		 *    
		 * @param value device receiving information from connection
		 */
		@Deprecated
		public void setDestInfo(MidiDevice.Info value) {
			destType = specType.INFO;
			destInfo = value;
			try {
				MidiDevice destDevice = MidiSystem.getMidiDevice(destInfo);
				if (Sequencer.class.isAssignableFrom(destDevice.getClass())) {
					destIsSequencer = true;
				}
				if (Synthesizer.class.isAssignableFrom(destDevice.getClass())) {
					destIsSynthesizer = true;
				}
			} catch (MidiUnavailableException ex) {
				ex.printStackTrace();
			}
		}
		public void setDestReceiver(Receiver value) {
			if (debugFlag > 0) {
				StringBuffer text3 = new StringBuffer();
				text3.append("Starting Connection.setDestReceiver");
				if (value == null) {
					text3.append(System.lineSeparator() + "Input parameter is null");
				}
				System.out.println(text3.toString());
			}
			if (value == null) {
				throw new NullPointerException("Input value is null");
			}
			destType = specType.RECEIVER;
			receiver = value;

		}
		public Receiver getDestReceiver() {
			return receiver;
		}
		public void setDestFile(File value) {
			destFile = value;

		}
		public File getDestFile() {
			return destFile;
		}
		public void openConnection() 
				throws MidiUnavailableException, IOException, InvalidMidiDataException {
			System.out.println("Running Connection.openConnection");

			musicStart = (new Date()).getTime();
			if (destType == specType.DEVICE || destType == specType.INFO) {
				if (!destDevice.isOpen()) {
					System.out.println("     Open destination device");
					destDevice.open();
				} else {
					System.out.println("     Destination device was open");
				}
				receiver = 	destDevice.getReceiver();


			} else if (destType == specType.RECEIVER) {
				;

			} else {
				System.out.println("Receiver not set");
			}
			if (sourceType == specType.DEVICE || sourceType == specType.INFO) {

				if (!sourceDevice.isOpen()) {
					System.out.println("     Opening source device");
					sourceDevice.open();
				} else {
					System.out.println("     Source device was open");
				}
				transmitter = sourceDevice.getTransmitter();
			} else if (sourceType == specType.TRANSMITTER) {
				;
			} else {
				System.out.println("Transmitter not set");
			}
			if (debugFlag > 0) {
				if (destType == specType.DEVICE) {
					int rcvrCount = destDevice.getReceivers().size();
					System.out.println("     Dest. has " + Integer.toString(rcvrCount) +
							" receivers");
				}
				if (sourceType == specType.DEVICE) {
					int transCount = sourceDevice.getTransmitters().size();
					System.out.println("    Source has " + Integer.toString(transCount) +
							" transmitters");
				}
			}
			if (destType == specType.DEVICE &&
					Synthesizer.class.isAssignableFrom(destDevice.getClass())) {
				ShortMessage temp = 
						new ShortMessage(ShortMessage.PROGRAM_CHANGE, 20, 100);
				receiver.send(temp, -1l);
			}
			splitter.clear();
			splitter.addReceiver(receiver);
			if (useCombinedConnection && useLogWindow) {
				splitter.addReceiver(logReceiver);
			}
			if (debugFlag > 0) {
				int rcvrCount = splitter.getReceivers().size();
				System.out.println("Splitter has " + Integer.toString(rcvrCount) +
						" receivers");
			}
			transmitter.setReceiver(splitter);
			active = true;
		}
		public void closeConnection() throws MidiUnavailableException {
			System.out.println("Calling Connection.closeConnection");

			if (sourceType == specType.DEVICE || sourceType == specType.INFO) {
				int xmtrCount;
				if (!sourceDevice.isOpen()) {
					System.out.println("    Transmitting device is closed");
				} else {
					if (debugFlag > 0) {
						xmtrCount = sourceDevice.getTransmitters().size();
						System.out.println("     " +
								Integer.toString(xmtrCount) + " transmitters for source");
					}
					transmitter.close();
					if (debugFlag > 0) {
						System.out.println("     Closing transmitter");
						xmtrCount = sourceDevice.getTransmitters().size();
						System.out.println("     " +
								Integer.toString(xmtrCount) + " transmitters for source");
					}
				}
			}
			if (destType == specType.DEVICE || destType == specType.INFO) {
				int rcvrCount;
				if (!destDevice.isOpen()) {
					System.out.println("     Receiving device is closed");
				} else {
					if (debugFlag > 0) {
						rcvrCount = destDevice.getReceivers().size();
						System.out.println("     " + 
								Integer.toString(rcvrCount) + " receivers for dest.");
						System.out.println("     Closing receiver");
					}
					receiver.close();
					if (debugFlag > 0) {
						rcvrCount = destDevice.getReceivers().size();
						System.out.println("     " + 
								Integer.toString(rcvrCount) + " receivers for dest.");
					}
				}
			}
			destType = specType.NONE;
			sourceType = specType.NONE;
			active = false;
		}
	}
	/**
	 * Opens the main window for the application.
	 * 
	 * @author Bradley Ross
	 *
	 */
	protected class Outer implements Runnable {
		public void run() {
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			// frame.addComponentListener(sizeListener);
			menuBar = new JMenuBar();
			frame.setJMenuBar(menuBar);
			midiMenuBar = new MidiMenuBar(menuBar);
			title = "Test for MIDI Application";
			frame.setTitle(title);
			// menuBar.add(fileMenu);
			GridBagLayout layout = new GridBagLayout();
			frame.setLayout(layout);
			GridBagConstraints upper = new GridBagConstraints();
			GridBagConstraints lower = new GridBagConstraints();
			upper.gridwidth = GridBagConstraints.REMAINDER;
			upper.gridheight = 1;
			upper.fill = GridBagConstraints.HORIZONTAL;
			upper.weightx = 1.0d;
			upper.weighty = 0.0d;
			lower.gridwidth = 1;
			lower.gridheight = 1;
			lower.fill = GridBagConstraints.BOTH;
			lower.weightx = 1.0d;
			lower.weighty = 1.0d;
			top = new Top();
			layout.setConstraints(top, upper);
			top.doLayout();
			frame.add(top);
			left = new Left();
			layout.setConstraints(left, lower);
			left.doLayout();
			frame.add(left);
			right = new Right();
			layout.setConstraints(right, lower);
			right.doLayout();
			frame.add(right);
			frame.setMinimumSize(new Dimension(800,750));
			//frame.pack();
			frame.setVisible(true);
		}
	}
	/**
	 * This represents a cell in the source or destinations column.
	 * 
	 * <p>The MIDI devices represented by these cells must be
	 *    defined using a {@link MidiDevice} object.
	 * 
	 * @author Bradley Ross
	 *
	 */
	@SuppressWarnings("serial")
	protected abstract class Listing extends JPanel implements MouseListener {	
		protected boolean selected = false;
		protected JTextArea contents;
		protected MidiDevice device = null;
		protected MidiDevice.Info info = null;
		// protected Listing selectedValue;
		protected List<Listing> parentList;
		protected boolean isSynthesizer = false;
		protected boolean isSequencer = false;
		/**
		 * Specify the MIDI device using a
		 * {@link MidiDevice object}.
		 * 
		 * @param device identifies MIDI device
		 */
		public Listing(MidiDevice device) {
			setMidiDevice(device);
			setMidiDeviceInfo(device.getDeviceInfo());
			createComponent();
		}
		/**
		 * Specify the MIDI device using a {@link Info} object.
		 * 
		 * <p>This is deprecated since 
		 *    {@link MidiSystem#getMidiDevice(javax.sound.midi.MidiDevice.Info)} can't 
		 *    be relied upon to identify a single device, especially when
		 *    dealing with synthesizers and sequencers.  The problem
		 *    may be related to software MIDI devices.</p>
		 *    
		 * @param info identifies MIDI device
		 */
		@Deprecated
		public Listing(MidiDevice.Info info) {
			setMidiDeviceInfo(info);
			try {
				device = MidiSystem.getMidiDevice(info);
			} catch (MidiUnavailableException ex) {
				ex.printStackTrace();
			}
			createComponent();
		}
		/**
		 * Creates the {@link JTextArea} object that contains
		 * the content of the cell.
		 */
		protected void createComponent() {
			StringBuffer text = new StringBuffer();
			text.append(info.getName() + " : " + info.getVendor() +
					" : " + info.getVersion() + System.lineSeparator());
			text.append(info.getDescription() + System.lineSeparator());
			if (Synthesizer.class.isAssignableFrom(device.getClass())) {
				text.append("Synthesizer" + System.lineSeparator());
				isSynthesizer = true;
			}
			if (Sequencer.class.isAssignableFrom(device.getClass())) {
				text.append("Sequencer" + System.lineSeparator());
				isSequencer = true;
			}
			text.append("Class: " + device.getClass() + System.lineSeparator());
			BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
			this.setLayout(layout);
			contents = new JTextArea(text.toString());
			contents.setEditable(false);
			contents.addMouseListener(this);
			contents.setBackground(Color.WHITE);
			this.add(contents);	
			setBorder(BorderFactory.createLineBorder(Color.CYAN, 3));
		}
		public MidiDevice getMidiDevice() {
			return device;
		}
		protected void setMidiDevice(MidiDevice value) {
			device = value;
		}
		public MidiDevice.Info getMidiDeviceInfo() {
			return info;
		}
		protected void setMidiDeviceInfo(MidiDevice.Info value) {
			info = value;
		}
		/**
		 * Handles mouse clicks within the cell.
		 */
		public void mouseClicked(MouseEvent event) {
			if (selectionLocked) { return; }
			if (selected) {
				selected = false;
				setSelectedValue(null);
				contents.setBackground(Color.WHITE);
			} else {
				for (Listing item : parentList) {
					item.clear();
				}
				selected = true;
				setSelectedValue(this);
				contents.setBackground(Color.LIGHT_GRAY);
			}
			if (debugFlag > 0) {
				System.out.println("Cell in list of devices clicked");
				StringBuffer diag1 = new StringBuffer();
				diag1.append("Selected source is ");
				if (selectedSource == null) {
					diag1.append("null");
				} else {
					diag1.append("not null");
				}
				diag1.append(System.lineSeparator());
				diag1.append("Selected destination is ");
				if (selectedDest == null) {
					diag1.append("null");
				} else {
					diag1.append("not null");
				}
				System.out.println(diag1.toString());
			}
		}
		public void mousePressed(MouseEvent e) { ; }
		public void mouseReleased(MouseEvent e) { ; }
		public void mouseEntered(MouseEvent e) { ; }
		public void mouseExited(MouseEvent e) { ; }
		protected void clear() {
			selected = false;
			contents.setBackground(Color.WHITE);
		}
		/**
		 * Depending on the subclass, this method will set either
		 * {@link #selectedSource} or {@link #selectedDest}.
		 * @param value logical value to be used
		 */
		protected abstract void setSelectedValue(Listing value); 
	}
	/**
	 * Creates a panel that contains information on a potential destination.
	 * 
	 * @author Bradley Ross
	 *
	 */
	@SuppressWarnings("serial")
	protected class DestListing extends Listing {
		public DestListing(MidiDevice.Info info) {
			super( info);
			parentList = destItems;
		}
		public DestListing(MidiDevice device) {
			super(device);
			parentList = destItems;
		}
		protected void setSelectedValue(Listing value) {
			selectedDest = value;
		}
	}	
	/**
	 * Creates a panel that contains information on a potential
	 * source.
	 * @author Bradley Ross
	 *
	 */
	@SuppressWarnings("serial")
	protected class SourceListing extends Listing  {

		public SourceListing(MidiDevice.Info info) {
			super( info);
			parentList = sourceItems;
		}
		public SourceListing(MidiDevice device) {
			super(device);
			parentList = sourceItems;
		}
		protected void setSelectedValue(Listing value) {
			selectedSource = value;
		}
	}
	/**
	 * Copies message from transmitter to multiple receivers.
	 * @author Bradley Ross
	 *
	 */
	protected class Splitter implements Receiver {
		protected List<Receiver> receivers = new ArrayList<Receiver>();
		public Splitter() { ; }
		public Splitter(Receiver value) {
			receivers.add(value);
		}
		public void clear() {
			receivers.clear();
		}
		public List<Receiver> getReceivers() {
			return new ArrayList<Receiver>(receivers);
		}
		public void addReceiver(Receiver value) {
			receivers.add(value);
		}
		@Override
		public void send(MidiMessage message, long timeStamp) {
			long newTime = timeStamp;
			MidiMessage newMessage = (MidiMessage) message.clone();
			for (Receiver item : receivers) {
				item.send(newMessage, newTime);
			}	
		}
		@Override
		public void close() { ; }
		
	}
	/**
	 * Create a widow displaying text and optionally save the text to a file.
	 * 
	 * @author Bradley Ross
	 *
	 */
	protected class DisplayFrame {
		JFrame displayFrame = null;
		boolean isOpen = false;
		JTextArea textArea = null;
		JMenuBar menuBar = null;
		MenuHandler menuHandler = null;
		protected class MenuHandler implements ActionListener {
			/**
			 * The constructor creates the items for the menu bar.
			 * 
			 * @param menuBar JMenuBar object used for the frame
			 */
			public MenuHandler(JMenuBar menuBar) {
				JMenu fileMenu = new JMenu("File");
				JMenu viewMenu = new JMenu("View");
				JMenuItem saveContents = new JMenuItem("Save Display to File");
				saveContents.setActionCommand("save");
				saveContents.addActionListener(this);
				JMenuItem clearDisplay = new JMenuItem("Clear Display");
				clearDisplay.setActionCommand("clear");
				clearDisplay.addActionListener(this);
				menuBar.add(fileMenu);
				menuBar.add(viewMenu);
				fileMenu.add(saveContents);
				viewMenu.add(clearDisplay);
			}

			/**
			 * Actions to be taken when menu item is clicked.
			 * 
			 * @param ev Event created by clicking on menu item
			 */
			public void actionPerformed(ActionEvent ev) {
				String command = ev.getActionCommand();
				if (command.equalsIgnoreCase("save")) {
					try {
						File saveFile = null;
						JFileChooser chooser = new JFileChooser();
						int returnVal = chooser.showSaveDialog(frame);
						if (returnVal == JFileChooser.APPROVE_OPTION) {
							saveFile = chooser.getSelectedFile();
							saveFile.createNewFile();
						}
						if (saveFile != null) {
							String text = "Saving display to " + saveFile.getName();
							System.out.println(text);
							PrintWriter writer = new PrintWriter(saveFile, "UTF-8"); 
							textArea.write(writer);
							writer.close();
						} else {
							showPopup("Problem", "No save file selected");
						}
					} catch (FileNotFoundException ex) {
						String text = "FileNotFoundException encountered " +
								"while saving contents of window to file";
						showPopup("Error", text);
						ex.printStackTrace();
					} catch (UnsupportedEncodingException ex) {
						String text = "UnsupportedEncodingException encountered " +
								"while saving contents of window to file";
						showPopup("Error", text);
						ex.printStackTrace();
					} catch (IOException ex) {
						String text = "IOException encountered " +
								"while saving contents of window to file";
						showPopup("Error", text);
						ex.printStackTrace();
					}
				} else if (command.equalsIgnoreCase("clear")) {
					clear();
				}
			}
		}
		/**
		 * Open the window that displays information.
		 */
		protected void open() {
			if (isOpen) { return; }
			isOpen = true;
			displayFrame = new JFrame();
			displayFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			menuBar = new JMenuBar();
			menuHandler = new MenuHandler(menuBar);
			displayFrame.setJMenuBar(menuBar);
			displayFrame.setSize(new Dimension(600, 600));
			GridBagLayout layout1 = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			displayFrame.setLayout(layout1);
			c.fill = GridBagConstraints.BOTH;
			c.weightx = 1.0d;
			c.weighty = 1.0d;
			textArea = new JTextArea();
			textArea.setColumns(80);
			textArea.setRows(100);
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setBackground(new Color(200, 255, 255));
			JScrollPane scrollPane = new JScrollPane(textArea);
			layout1.setConstraints(scrollPane, c);		
			displayFrame.add(scrollPane);
			displayFrame.setVisible(true);
		}
		/**
		 * Close the window for the display.
		 */
		protected void close() {
			if (!isOpen) { return; }
			isOpen = false;
			displayFrame.setVisible(false);
			displayFrame.dispose();
		}
		public JFrame getJFrame() {
			return frame;
		}
		/**
		 * Send a message to the display.
		 * @param text message to be displayed
		 */
		protected void write(String text) {
			if (!isOpen) { return; }
			textArea.append(text + System.lineSeparator());
		}
		protected void clear() {
			textArea.setText(System.lineSeparator());
		}
	}
	/**
	 * Displays MIDI information in a Java Swing window.
	 * @author Bradley Ross
	 *
	 */
	protected class LogReceiver implements Receiver {
		DisplayFrame display = null;
		boolean isOpen = false;
		public LogReceiver() {
			display = new DisplayFrame();
			display.open();
			isOpen = true;
		}

		/**
		 * Used by MIDI transmitter to send message to this receiver.
		 * 
		 * <p><a href="https://www.midi.org/specifications/item/table-1-summary-of-midi-message"
		 *    target="_blank">
		 *    https://www.midi.org/specifications/item/table-1-summary-of-midi-message</a>
		 *    List of MIDI codes.
		 *    </p>
		 * @param message string of bytes containing message
		 * @param timeStamp time generated by transmitting device
		 */		
		public void send(MidiMessage message, long timeStamp) {
			StringBuffer build = new StringBuffer();
			int length = message.getLength();
			int status = message.getStatus();
			byte[] data = message.getMessage();	
			if (timeStamp > 0) {
				String seconds = Long.toString(timeStamp/1000000l);
				String micros = String.format("%06d", timeStamp % 1000000l);
				build.append(" " + seconds + "." + micros + " ");
			} else {
				long time = (new Date()).getTime() - musicStart;
				String seconds = String.format("%6d", time / 1000l);
				String millis = String.format("%03d", time % 1000l);
				build.append(" " + seconds + "." + millis + " ");
			}
			build.append("Status: " + String.format("%02x", status));
			build.append(" Length: " + Integer.toString(length) + " -- ");
			for (int i = 0; i < data.length; i++) {
				build.append(String.format("%02x", data[i]));
				if (i > 10) {
					build.append(" ...");
					break;
				}
				build.append(" ");
			}
			display.write(build.toString());
		}
		public void close() {
			System.out.println("Calling LogReceiver.close()");
		}

	}
	/**
	 * Creates top panel of display.
	 * @author Bradley Ross
	 *
	 */
	@SuppressWarnings("serial")
	protected class Top extends JPanel {
		public Top() {
			BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
			this.setLayout(layout);
			JTextArea textBlock = new JTextArea();
			textBlock.setEditable(false);
			textBlock.append("The purpose of this application is to " +
					"route MIDI information between devices" +
					System.lineSeparator());
			this.add(textBlock);
			setBorder(BorderFactory.createLineBorder(Color.red, 3));
		}
	}
	/**
	 * Creates the panel at the lower left corner of the window which
	 * contains the list of possible sources.
	 * @author Bradley Ross
	 *
	 */
	@SuppressWarnings("serial")
	protected class Left extends JPanel {
		protected BoxLayout layout;	
		protected JLabel titleField = new JLabel("Sources");
		public Left() {
			rebuild();
		}
		protected void rebuild() {
			sourceItems = new ArrayList<Listing>();
			layout = new BoxLayout(this, BoxLayout.Y_AXIS);
			this.setLayout(layout);
			setBorder(BorderFactory.createLineBorder(Color.green, 3));		
			this.add(titleField);
			for (MidiDevice.Info item : sourceDevices) {
				SourceListing block = new SourceListing(item);
				this.add((Listing) block);
				sourceItems.add((Listing) block);
			}
		}
	}
	/**
	 * Creates the panel at the lower right hand corner of the
	 * window which contains information for destination devices.
	 * 
	 * @author Bradley Ross
	 *
	 */
	@SuppressWarnings("serial")
	protected class Right extends JPanel {
		protected BoxLayout layout;
		protected JLabel titleField = new JLabel("Destinations");
		public Right() {
			rebuild();
		}
		protected void rebuild() {
			destItems = new ArrayList<Listing>();
			layout = new BoxLayout(this, BoxLayout.Y_AXIS);
			this.setLayout(layout);
			setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
			this.add(titleField);
			for (MidiDevice.Info item : destDevices) {
				DestListing block = new DestListing(item);
				this.add((Listing) block);
				destItems.add((Listing) block);
			}
		}
	}
	/**
	 * Display a message in a popup window.
	 * 
	 * @param message message to appear in popup window
	 * @throws HeadlessException
	 */
	protected void showPopup(String message) throws HeadlessException {
		showPopup("Problem Detected", message);
	}
	/**
	 * Display a message in a popup window.
	 * 
	 * @param title text to be displayed on title of popup window
	 * @param message message to appear in popup window
	 * @throws HeadlessException
	 */
	protected void showPopup(String title, String message) throws HeadlessException{
		JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
	}
	/**
	 * Constructs the lists of potential source and destination devices.
	 */
	protected void buildLists() {
		allDevices = MidiSystem.getMidiDeviceInfo();
		destDevices = new ArrayList<MidiDevice.Info>();
		sourceDevices = new ArrayList<MidiDevice.Info>();
		for (MidiDevice.Info item : allDevices) {
			MidiDevice device;
			try {
				device = MidiSystem.getMidiDevice(item);

				if (device.getMaxReceivers() != 0) {
					destDevices.add(item);
				}
				if (device.getMaxTransmitters() != 0) {
					sourceDevices.add(item);
				}
				if (device.getMaxReceivers() == 0 &&
						device.getMaxTransmitters() == 0) {
					System.out.println(item.getName());
				}
			} catch (MidiUnavailableException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * Starts the Swing graphics operations.
	 */
	public void run() {
		buildLists();
		/* Start Swing graphics */
		try {
			Outer instance = new Outer();
			SwingUtilities.invokeLater(instance);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * This is a test of the {@link DisplayFrame} class.
	 */
	protected void testDisplayFrame() {
		DisplayFrame display = new DisplayFrame();
		display.open();
		for (int i = 0; i <= 100; i++) {
			display.write(Integer.toString(i) + "  This is a test of the system");
		}
	}
	/**
	 * Convenience method for tests of log software.
	 * @param values items used to build Midi message
	 * @return message
	 */
	protected byte[] buildMessage2(int... values) {
		return buildMessage(values);
	}
	/**
	 * Convenience method for tests of log software.
	 * @param data items used to build Midi message
	 * @return message
	 */
	protected byte[] buildMessage(int[] data) {
		byte[] buffer = new byte[data.length];
		for (int i = 0; i < data.length; i++) {
			if (data[i] > 255) {
				buffer[i] = (byte)( data[i] - 256);
			} else {
				buffer[i] = (byte) data[i];
			}
		}
		return buffer;
	}
	/**
	 * Subclass of MidiMessage used for testing.
	 * @author Bradley Ross
	 * 
	 * <p>The constructor for MidiMessage is protected, which
	 *    makes it difficult to use in test cases.</p>
	 *
	 */
	protected class TestMidiMessage extends MidiMessage {
		protected byte[] inter;
		public TestMidiMessage(byte[] data) {
			super(data);
			inter = data;
		}
		public TestMidiMessage clone() {
			return new TestMidiMessage(inter);
		}
	}
	/**
	 * This is a test of the {@link LogReceiver} class.
	 * 
	 * <p>Must use subclass of MidiMessage.</p>
	 */
	protected void testLogReceiver() {
		LogReceiver receiver = new LogReceiver();
		byte[] message = buildMessage2(0, 255, 128, 17);
		TestMidiMessage testObject = new TestMidiMessage(message);
		receiver.send(testObject, 1000000l);
		for (int i = 0; i < message.length; i++) {
			System.out.print(String.format("%O2x ", message[i]));
		}
		System.out.println();
		receiver.close();
	}
	/**
	 * Test driver.
	 * 
	 * @param args first argument can optionally run test cases
	 */
	public static void main(String[] args) {
		MidiRouter instance = new MidiRouter();
		if (args.length == 0) {
			instance.run();
			return;
		}
		if (args[0].equalsIgnoreCase("display")) {
			instance.testDisplayFrame();
		}
		if (args[0].equalsIgnoreCase("log")) {
			instance.testLogReceiver();
		}
	}
}
