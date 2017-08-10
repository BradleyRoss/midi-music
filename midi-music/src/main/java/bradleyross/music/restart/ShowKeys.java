package bradleyross.music.restart;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


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
	public ShowKeys() {
		
	}
	protected class  BuildMenu extends JMenuBar {
		public BuildMenu() {
			
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
		c.weightx = 1.0d;
		c.weighty = 1.0d;
		layout.setConstraints(splitPane, c);
		mainFrame.add(splitPane);
		
		mainFrame.setVisible(true);
		
		
		
	}
	public static void main(String[] args) {
		ShowKeys instance = new ShowKeys();
		SwingUtilities.invokeLater(instance);
	}

}
