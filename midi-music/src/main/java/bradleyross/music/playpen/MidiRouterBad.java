package bradleyross.music.playpen;
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
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Receiver;
// import javax.sound.midi.MidiDeviceReceiver;
// import javax.sound.midi.MidiDeviceTransmitter;
import javax.sound.midi.MidiMessage;
// import javax.sound.midi.ShortMessage;
import javax.sound.midi.MidiChannel;
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
 * @see FlowLayout
 * @see GridLayout
 * @see GridBagLayout
 * @see BoxLayout
 *
 */
public class MidiRouterBad implements Runnable{
	/**
	 * A value of zero means no diagnostic output, while 
	 * higher values create more output.
	 */
	protected int debugFlag = 1;
	protected JPanel outer;
	protected JPanel top;
	protected JPanel left;
	protected JPanel right;
	protected JFrame frame;
	protected JMenuBar menuBar;


	protected String title;
	protected File sourceFile;
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
	 * This Connection object will send information from the
	 * source object to the log window.
	 */
	protected Connection logConnection = new Connection();
	protected Synthesizer synthesizer = null;
	/**
	 * If true, no changes are allowed to the selections in the
	 * source and destination columns.
	 */
	boolean selectionLocked = false;
	/**
	 * If true, Midi messages from the source will be echoed a
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
		 * Specifying a {@link MidiDevice.Info} object.
		 */
		INFO }
	LogReceiver logReceiver = null;	

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
		 * @param value JMenuBar structure belonging to {@link frame}.
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
					showPopup("Information", "About menu item clicked");

				} else if (command.equalsIgnoreCase("preferences")) {
					showPopup("Information", "Preferences menu item clicked");

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
						showPopup("Information", text);
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
						showPopup("Information", text);
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
						showPopup("Information", text);
					} else {
						showPopup("Problem", "No log file selected");
					}
				} else if (command.equalsIgnoreCase("openConnection")) {

					selectionLocked = true;
					if (useLogWindow && logReceiver == null) {
						logReceiver = new LogReceiver();
					}
					if (debugFlag > 0) {
						System.out.println("Open Connection menu item clicked");
						if (selectedSource != null && selectedSource.isSequencer) {
							System.out.print("Source is sequencer with ");
							int xmtrCount = 
									selectedSource.getMidiDevice().getTransmitters().size();
							System.out.println(Integer.toString(xmtrCount) + 
									" transmitters");
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
					if (useLogWindow) {
						if (debugFlag > 0) {
							StringBuffer text = new StringBuffer();
							text.append("Starting setup of connection to log window");
							if (debugFlag > 5) {
								showPopup("Information", text.toString());
							} else {
								System.out.println(text.toString());
							}
						}
						MidiDevice sourceDevice = selectedSource.getMidiDevice();
						if (debugFlag > 0) {
							System.out.println("MidiDevice for source obtained");
						}
						logConnection.setSourceDevice(sourceDevice);
						logConnection.setDestReceiver(logReceiver);
						logConnection.openConnection();
						if (debugFlag > 0) {
							System.out.println("Log connection opened");
							int xmtrCount = sourceDevice.getTransmitters().size();
							System.out.println("Source has " + Integer.toString(xmtrCount) +
									" transmitters");
						}
					}
					if (debugFlag > 0) {
						System.out.println("Starting data connection");
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
						StringBuffer text2 = new StringBuffer("There are " +
								Integer.toString(source.getTransmitters().size()) +
								" transmitters for source");
						System.out.println(text2.toString());
						// showPopup("Information", text2.toString());
					}
					if (selectedSource.isSequencer) {
						if (debugFlag > 0) {
							System.out.println("About to start playing MIDI file");
						}
						Sequence sequence2 = MidiSystem.getSequence(new FileInputStream(inputFile));
						Sequencer sequencer = (Sequencer) selectedSource.getMidiDevice();
						sequencer.setSequence(sequence2);
						sequencer.start();
					}
				} else if (command.equalsIgnoreCase("closeConnection")) {
					selectionLocked = false;
					connection.closeConnection();
					if (useLogWindow) {
						logConnection.closeConnection();
						logReceiver.close();
						if (debugFlag > 0) {
							System.out.println("Log connection closed");
						}
					}
					selectedSource.getMidiDevice().close();
					selectedDest.getMidiDevice().close();
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
	 * Represents connection between Midi devices.
	 * 
	 * @author Bradley Ross
	 *
	 */
	protected class Connection {
		/**
		 * Type of specification for the source of the connection.
		 */
		protected specType sourceType;
		protected MidiDevice.Info sourceInfo = null;
		protected MidiDevice sourceDevice = null;
		protected Transmitter sourceTransmitter = null;
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
			sourceTransmitter = sourceDevice.getTransmitter();
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
		 *    and sequencers.  {@link MidiDevice.getDeviceInfo()} and
		 *    {@link MidiSystem.getMidiDevice(MidiDevice.Info)} are not
		 *    inverse functions in this situation.</p>
		 *    
		 * @param input device
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
		public void setSourceTransmitter(Transmitter value) {
			if (debugFlag > 0) {
				System.out.println("Starting Connection.setSourceTransmitter");	
			}
			if (value == null) {
				throw new NullPointerException("Input value is null");
			}
			sourceType = specType.TRANSMITTER;
			sourceTransmitter = value;
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
		protected specType destType;
		protected MidiDevice.Info destInfo = null;
		protected MidiDevice destDevice = null;
		protected Receiver destReceiver = null;
		protected boolean destIsSequencer = false;
		protected boolean destIsSynthesizer  = false;
		protected File destFile = null;
		protected boolean active = false;
		public void setDestDevice(MidiDevice value) {
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
			}
		}
		/**
		 * Specify MIDI device to receive information from connection.
		 * 
		 * <p>This method is deprecated because there is a problem
		 *    with the Java code when dealing with software synthesizers
		 *    and sequencers.  {@link MidiDevice.getDeviceInfo()} and
		 *    {@link MidiSystem.getMidiDevice(MidiDevice.Info)} are not
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
				text3.append("Starting Connection.setDestReceiver" + System.lineSeparator());
				if (value == null) {
					text3.append("Input parameter is null");
				}
				if (debugFlag > 5) {
					showPopup("Information", text3.toString());
				} else {
					System.out.println(text3.toString());
				}
			}
			if (value == null) {
				throw new NullPointerException("Input value is null");
			}
			destType = specType.RECEIVER;
			destReceiver = value;
		}
		public Receiver getDestReceiver() {
			return destReceiver;
		}
		public void setDestFile(File value) {
			destFile = value;

		}
		public File getDestFile() {
			return destFile;
		}
		public void openConnection() 
				throws MidiUnavailableException, IOException, InvalidMidiDataException {
			if (debugFlag > 0) {
				System.out.println("*****  *****");
				System.out.println("Starting Connection.openConnection");
			}
			if (destType == specType.DEVICE || destType == specType.INFO) {
				if (!destDevice.isOpen()) {
					destDevice.open();
				}
				receiver = 	destReceiver;
				if (debugFlag > 0) {

					System.out.println("Destination is " + 
							destDevice.getDeviceInfo().getName());	
					System.out.println("Destination is open " +
							Boolean.toString(destDevice.isOpen()));
				}
			}
			if (destType == specType.RECEIVER) {
				receiver = destReceiver;

			}
			if (sourceType == specType.DEVICE || sourceType == specType.INFO) {
				if (!sourceDevice.isOpen()) {
					sourceDevice.open();
				}	


				if (debugFlag > 0) {
					System.out.println("Source is " + 
							sourceDevice.getDeviceInfo().getName());	
					System.out.println("Source is open " +
							Boolean.toString(sourceDevice.isOpen()));
				}
			}
			transmitter = sourceTransmitter;
			if (debugFlag > 0) {
				int xmtrCount = sourceDevice.getTransmitters().size();
				System.out.println("Source has " + Integer.toString(xmtrCount) +
						" transmitters");
			}

			transmitter.setReceiver(receiver);
			active = true;
		}
		public void closeConnection() throws MidiUnavailableException {
			transmitter.close();
			receiver.close();
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
		public Listing(MidiDevice device) {
			setMidiDevice(device);
			setMidiDeviceInfo(device.getDeviceInfo());
			createComponent();
		}
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
				StringBuffer diag1 = new StringBuffer();
				diag1.append("Cell in list clicked");
				diag1.append("Selected source is ");
				if (selectedSource == null) {
					diag1.append("null");
				} else {
					diag1.append("not null");
				}
				diag1.append(System.lineSeparator() + "Selected destination is ");
				if (selectedDest == null) {
					diag1.append("null");
				} else {
					diag1.append("not null");
				}
				diag1.append(System.lineSeparator());
				System.out.println(diag1.toString());
				// showPopup("Information", diag1.toString());
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
		 * {@link selectedSource} or {@link selectedDest}.
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
				JMenuItem saveContents = new JMenuItem("Save");
				saveContents.setActionCommand("save");
				saveContents.addActionListener(this);
				menuBar.add(fileMenu);
				fileMenu.add(saveContents);
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
							showPopup("Information", text);
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
	}
	/**
	 * Displays MIDI information in a Java Swing window.
	 * @author Bradley Ross
	 *
	 */
	protected class LogReceiver implements Receiver {
		DisplayFrame display = null;
		boolean isOpen = false;
		int counter = 0;
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
		 *    <p>
		 * @param message string of bytes containing message
		 * @param timeStamp time generated by transmitting device
		 */

		public void send(MidiMessage message, long timeStamp) {

			StringBuffer build = new StringBuffer();
			int length = message.getLength();
			int status = message.getStatus();
			int typeCode = status/16;
			int channel = status % 16;
			byte[] data = message.getMessage();
			if (debugFlag > 0) {
				counter++;
				StringBuffer inter = new StringBuffer();
				inter.append(Integer.toString(counter));
				inter.append(" LogReceiver.send - Status: " +
						Integer.toString(status) + " Length: " +
						Integer.toString(length) + " " +
						Integer.toString(typeCode) + " " +
						Integer.toString(channel));
				for (int i = 0; i < length; i++) {
					inter.append(" " + String.format("%O2x", data[i]));
				}
				System.out.println(inter.toString());
			}

			int data2 = -1;
			int data3 = -1;
			if (length > 1) {
				data2 = data[2];
				if (data2 < 0) {
					data2 = data2 + 256;
				}
			}
			if (length > 2) {
				data3 = data[3];
				if (data3 < 0) {
					data3 = data3 + 256;
				}
			}

			String typeName;
			if (typeCode == 8) {
				typeName = "Note Off";
			} else if (typeCode == 9) {
				typeName = "Note On";
			} else if (typeCode == 10) {
				typeName = "Polyphonic Aftertouch";
			} else if (typeCode == 11) {
				typeName = "Control/Mode Change";
			} else if (typeCode == 12) {
				typeName = "Program Change";
			} else if (typeCode == 13) {
				typeName = "Channel Aftertouch";
			} else if (typeCode == 14) {
				typeName = "Pitch Bend Change";
			} else {
				typeName = "Not Listed";
			}
			if (typeCode >= 8 && typeCode <= 10) {

				build.append(typeName + " - Chan " + Integer.toString(channel) +
						" Note: " + Integer.toString(data2) +
						" Vel/Press: " + Integer.toString(data3) +
						" Timestamp: " +Long.toString(timeStamp) +
						System.lineSeparator());
			} else if (typeCode == 12) {
				build.append(typeName + " - Chan " + Integer.toString(channel) +
						" Data " + Integer.toString(data2) +
						System.lineSeparator());
			} else if (typeCode >= 11 && typeCode <= 14) {
				build.append(typeName + " - Chan " + Integer.toString(channel) +
						" Data 1: " + Integer.toString(data2) +
						" Data 2: " + Integer.toString(data3) +
						" Timestamp: " +Long.toString(timeStamp) +
						System.lineSeparator());
			} else {
				build.append("     " + Long.toString(timeStamp) + ", " +
						Integer.toString(status, 16) + ", " +
						Integer.toString(length));
				build.append(", ");
				for (int i = 0; i < data.length; i++) {
					/*  
					 *  Byte.toUnsignedInt(byte) appears to
					 *  start with Java 8.
					 */
					int temp = Byte.toUnsignedInt(data[i]);
					if (temp < 16) {
						build.append("0" + Integer.toString(temp,16));
					} else {
						build.append(Integer.toString(temp,16));
					}
					if (i > 10) {
						break;
					}
				}
				build.append(" ");
			}
			display.write(build.toString());
		}
		public void close() {
			display.getJFrame().setVisible(false);
			display.getJFrame().dispose();
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
		try {
			synthesizer = MidiSystem.getSynthesizer();
			StringBuffer buffer = new StringBuffer();
			int counter = 0;
			if (debugFlag > 2) {
				buffer.append("Synthesizer is open: " + Boolean.toString(synthesizer.isOpen()) +
						System.lineSeparator());
				for (MidiChannel channel : synthesizer.getChannels()) {
					buffer.append("Channel " + Integer.toString(counter) + 
							"    Mute status: " + Boolean.toString(channel.getMute()) +
							"    Solo status: " + Boolean.toString(channel.getSolo()) +
							System.lineSeparator());
					counter++;
				}
				showPopup("Information", buffer.toString());
			}

		} catch (MidiUnavailableException ex) {
			ex.printStackTrace();
		}
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
			System.out.print(String.format("%02x ", message[i]));
		}
		System.out.println();
		receiver.close();
	}
	/**
	 * Test driver.
	 * 
	 * @param args not used in this application
	 */
	public static void main(String[] args) {
		MidiRouterBad instance = new MidiRouterBad();
		instance.run();
		// instance.testDisplayFrame();
		// instance.testLogReceiver();
	}
}
