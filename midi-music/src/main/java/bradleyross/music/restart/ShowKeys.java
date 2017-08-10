package bradleyross.music.restart;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;
import bradleyross.music.restart.ParseMessage;

import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import bradleyross.music.restart.MidiDeviceWrapper;


/**
 * The purpose of this application will be to display
 * keystrokes as they occur.
 * @author Bradley Ross
 *
 */
public class ShowKeys implements Runnable {
	protected JFrame mainFrame = null;
	protected JPanel left = null;
	protected JPanel right = null;
	protected ParseMessage parser = new ParseMessage();
	protected ArrayList<MidiDeviceWrapper> allDevices = null;
	public ShowKeys() {
		try {
			allDevices = new ArrayList<MidiDeviceWrapper>();
		for (MidiDevice.Info item : MidiSystem.getMidiDeviceInfo()) {
			MidiDevice device = MidiSystem.getMidiDevice(item);
			allDevices.add(new MidiDeviceWrapper(device));
		}
		}catch (MidiUnavailableException ex) {
			ex.printStackTrace();
		}
	}
	@SuppressWarnings("serial")
	protected class  BuildMenu extends JMenuBar implements ActionListener {
		public BuildMenu() {
			super();
			JMenu file = new JMenu("File");
			this.add(file);
			JMenuItem chooseLog = new JMenuItem("Choose Log File");
			chooseLog.setActionCommand("chooseLog");
			chooseLog.addActionListener(this);
			JMenuItem saveLog = new JMenuItem("Save Log");
			saveLog.setActionCommand("saveLog");
			saveLog.addActionListener(this);
			
		}
		public void actionPerformed(ActionEvent ev) {
			String command = ev.getActionCommand();
			if (command.equalsIgnoreCase("chooseLog")) {
				
			} else if (command.equalsIgnoreCase("saveLog")) {
				
			}
		}
	}
	protected class LeftPanel extends JPanel implements MouseListener {
		public LeftPanel() {
			
		}

		public void mouseClicked(MouseEvent e) {
		
			
		}

		public void mousePressed(MouseEvent e) {
			
		}

		public void mouseReleased(MouseEvent e) {
			
		}

		public void mouseEntered(MouseEvent e) {
			
		}

		public void mouseExited(MouseEvent e) {
			
		}
		
	}
	public void run() {
		mainFrame = new JFrame();
		mainFrame.setSize(600, 600);
		JSplitPane splitPane = null;
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		mainFrame.setLayout(layout);
		mainFrame.setJMenuBar(new BuildMenu());
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(0.5d);
		c.weightx = 1.0d;
		c.weighty = 1.0d;
		c.fill = GridBagConstraints.BOTH;
		layout.setConstraints(splitPane, c);
		mainFrame.add(splitPane);
		
		mainFrame.setVisible(true);
		
		
		
	}
	public static void main(String[] args) {
		ShowKeys instance = new ShowKeys();
		SwingUtilities.invokeLater(instance);
	}

}
