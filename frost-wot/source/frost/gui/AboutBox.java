/*
  AboutBox.java / About Box
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License as
  published by the Free Software Foundation; either version 2 of
  the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
package frost.gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import frost.util.gui.translation.Language;

/**
 * @author $Author$
 * @version $Revision$
 */
public class AboutBox extends JDialog {
	
	private class Listener extends WindowAdapter implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == okButton) {
				cancel();
			}	
			if (e.getSource() == moreButton) {
				moreButtonPressed();
			}	
		}
		
		public void windowClosing(WindowEvent e) {
			cancel();
			super.windowClosing(e);
		}
	}
	
	private Listener listener = new Listener();
	
	private Language language = null;

	private final static String product = "Frost";

	// because a growing amount of users use CVS version:
	private String version = null;

	private final static String copyright = "Copyright (c) 2003 Jan-Thomas Czornack";
	private final static String comments2 = "http://jtcfrost.sourceforge.net/";

	JPanel contentPanel = new JPanel();
	JPanel topPanel = new JPanel();
	JPanel buttonsPanel = new JPanel();
	JPanel imagePanel = new JPanel();
	JPanel messagesPanel = new JPanel();
	JPanel morePanel;
	
	JLabel imageLabel = new JLabel();
	JLabel productLabel = new JLabel();
	JLabel versionLabel = new JLabel();
	JLabel copyrightLabel = new JLabel();
	JLabel licenseLabel = new JLabel();
	JLabel websiteLabel = new JLabel();
	
	JButton okButton = new JButton();
	JButton moreButton = new JButton();
	
	private boolean moreExtended = false;
	
	private JScrollPane moreScrollPane;

	private static final ImageIcon frostImage =
		new ImageIcon(AboutBox.class.getResource("/data/jtc.jpg"));

	/**
	 * @param parent
	 */
	public AboutBox(Frame parent) {
		super(parent);
		language = Language.getInstance();
		initialize();
		setLocationRelativeTo(parent);
	}

	/**
	 * Component initialization
	 */
	private void initialize() {
		imageLabel.setIcon(frostImage);
		setTitle(language.getString("About"));
		setResizable(false);
		
		// Image panel
		imagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		imagePanel.add(imageLabel);
		
		// Messages panel
		GridLayout gridLayout = new GridLayout(5, 1);
		messagesPanel.setLayout(gridLayout);
		messagesPanel.setBorder(new EmptyBorder(10, 50, 10, 10));
		productLabel.setText(product);
		versionLabel.setText(getVersion());
		copyrightLabel.setText(copyright);
		licenseLabel.setText(language.getString("Open Source Project (GPL license)"));
		websiteLabel.setText(comments2);
		messagesPanel.add(productLabel);
		messagesPanel.add(versionLabel);
		messagesPanel.add(copyrightLabel);
		messagesPanel.add(licenseLabel);
		messagesPanel.add(websiteLabel);
		
		// Buttons panel
		moreButton.setText(language.getString("More") + " >>");
		okButton.setText(language.getString("OK"));
		buttonsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		buttonsPanel.add(moreButton);
		buttonsPanel.add(okButton);
				
		// Putting everything together
		topPanel.setLayout(new BorderLayout());
		topPanel.add(imagePanel, BorderLayout.WEST);
		topPanel.add(messagesPanel, BorderLayout.CENTER);
		
		contentPanel.setLayout(new BorderLayout());
		contentPanel.add(buttonsPanel, BorderLayout.CENTER);
		contentPanel.add(topPanel, BorderLayout.NORTH);
		getContentPane().add(contentPanel, null);
		
		// Add listeners
		moreButton.addActionListener(listener);
		okButton.addActionListener(listener);
		addWindowListener(listener);
		
		pack();
	}
	
	/**
	 * @return
	 */
	private JPanel getMorePanel() {
		if (morePanel == null) {
			morePanel = new JPanel(new BorderLayout());
			morePanel.setBorder(new EmptyBorder(10,10,10,10));
			
			JTextArea moreTextArea = new JTextArea();
			moreTextArea.setEditable(false);
			moreTextArea.setMargin(new Insets(5,5,5,5));
						
			moreScrollPane = new JScrollPane(moreTextArea);
			moreTextArea.setRows(10);
			morePanel.add(moreScrollPane, BorderLayout.CENTER);
			
			moreTextArea.append(language.getString("Development:") + "\n");
			moreTextArea.append("   Jan-Thomas Czornack\n");
			moreTextArea.append("   Thomas Mueller\n");
			moreTextArea.append("   Jim Hunziker\n");
			moreTextArea.append("   Stefan Majewski\n");
			moreTextArea.append("   José Manuel Arnesto\n\n");
			moreTextArea.append(language.getString("Windows Installer:") + "\n");
			moreTextArea.append("   Benoit Laniel\n\n");
			moreTextArea.append(language.getString("System Tray Executables:") + "\n");
			moreTextArea.append("   Jeeva S\n\n");
			moreTextArea.append(language.getString("Translation Support:") + "\n");
			moreTextArea.append("   Rudolf Krist\n");
			moreTextArea.append("   RapHHfr\n\n");
			moreTextArea.append(language.getString("Splash Screen Logo:") + "\n");
			moreTextArea.append("   Frédéric Scheer\n\n");
			moreTextArea.append(language.getString("Misc code contributions:") + "\n");
			moreTextArea.append("   SuperSlut Yoda");
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					moreScrollPane.getViewport().setViewPosition(new Point(0,0));	
				}
			});
		}
		return morePanel;
	}

	/**
	 * @return
	 */
	private String getVersion() {
		if (version == null) {
			version =
				language.getString("Version")
					+ ": "
					+ getClass().getPackage().getSpecificationVersion();
		}
		return version;
	}
	

	/**
	 * 
	 */
	private void moreButtonPressed() {
		if (moreExtended) {
		
			moreButton.setText(language.getString("More") + " >>");
			contentPanel.remove(getMorePanel());
			pack();
			moreExtended = false;
		} else {
			
			contentPanel.add(getMorePanel(), BorderLayout.SOUTH);
			moreButton.setText(language.getString("Less") + " <<");
			pack();
			moreExtended = true;
		}
		
	}

	/**
	 * Close the dialog
	 */
	private void cancel() {
		setVisible(false);
		dispose();
	}
}
