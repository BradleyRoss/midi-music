package bradleyross.music.framework;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
// import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceInfo;
import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDeviceProvider;
// import uk.co.xfactorylibrarians.coremidi4j.CoreMidiSource;
// import uk.co.xfactorylibrarians.coremidi4j.CoreMidiDestination;
public class SelectAndTest implements Runnable {
	protected JFrame frame;
	protected JTextArea outputText = null;
	protected MidiDevice.Info[] infoList = null;
	protected ArrayList<DeviceCell> cellList = new ArrayList<DeviceCell>();
	protected ArrayList<CollectedDeviceInformation> deviceList =
			new ArrayList<CollectedDeviceInformation>();
	public void run() {
		frame = new JFrame();
		frame.setJMenuBar(new MenuBar());
		infoList = CoreMidiDeviceProvider.getMidiDeviceInfo();

	}
	public void clearAll() {
		for (DeviceCell item : cellList) {
			item.setSelected(false);
		}
	}
	@SuppressWarnings("serial")
	protected class MenuBar extends JMenuBar implements ActionListener {
		public MenuBar() {
			JMenu actions = new JMenu("Actions");
			add(actions);
			JMenuItem transceiverCounting = 
					new JMenuItem("Counting of Receivers/Transmitters");
			transceiverCounting.setActionCommand("xcvrCounting");
			transceiverCounting.addActionListener(this);
			actions.add(transceiverCounting);

		}
		public void actionPerformed(ActionEvent ev) {
			String command = ev.getActionCommand();
			if (command.equalsIgnoreCase("xcvrCounting")) {

			}

		}
	}
	@SuppressWarnings("serial")
	protected class DeviceCell extends JTextArea implements MouseListener {
		protected boolean selected = false;
		Color colorOn = Color.LIGHT_GRAY;
		Color colorOff = Color.WHITE;
		CollectedDeviceInformation information = null;
		public CollectedDeviceInformation getCollectedDeviceInformation() {
			return information;
		}
		public DeviceCell(MidiDevice.Info value) throws MidiUnavailableException {
			information = new CollectedDeviceInformation(value);
			setText(information.toString());
			addMouseListener(this);
		}
		public void clear() {

		}
		public void setSelected(boolean value) {
			
		}
		public boolean getSelected() {
			return selected;
		}
		@Override
		public void mouseClicked(MouseEvent e) {


		}

		@Override
		public void mousePressed(MouseEvent e) {


		}

		@Override
		public void mouseReleased(MouseEvent e) {


		}

		@Override
		public void mouseEntered(MouseEvent e) {


		}

		@Override
		public void mouseExited(MouseEvent e) {


		}

	}
	protected void layoutFrame() {
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c1 = new GridBagConstraints();
		GridBagConstraints c2 = new GridBagConstraints();

	}
	@SuppressWarnings("serial")
	protected class DeviceListPanel extends JPanel {

		public DeviceListPanel() {
			GridBagConstraints c = new GridBagConstraints();
			GridBagLayout layout = new GridBagLayout();
			setLayout(layout);
			c.gridwidth = GridBagConstraints.REMAINDER;
			DeviceCell textArea = null;
			for (MidiDevice.Info item : infoList) {
				try {
					textArea = new DeviceCell(item);
					cellList.add(textArea);
				} catch (MidiUnavailableException ex) {
					ex.printStackTrace();
				}
			}
		}






	}
	public void main(String[] args) {
		SelectAndTest instance = new SelectAndTest();
		instance.run();
	}

}
