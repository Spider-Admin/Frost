/*
  MessageUploadFailedDialog.java / Frost
  Copyright (C) 2001  Jan-Thomas Czornack <jantho@users.sourceforge.net>
  This file is contributed by Stefan Majewski <feuerblume@users.sourceforge.net>

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
import java.util.*;
import java.util.Timer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import frost.util.gui.translation.Language;

/**
 * @author $Author$
 * @version $Revision$
 */
public class MessageUploadFailedDialog extends JDialog {
	
	/**
	 * 
	 */
	private class ButtonListener implements ActionListener {
		
		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == retryButton) {
				userAnswer = RETRY_VALUE;
			} else if (e.getSource() == tryOnNextStartupButton) {
				userAnswer = RETRY_NEXT_STARTUP_VALUE;
			} else if (e.getSource() == discardButton) {
				userAnswer = DISCARD_VALUE;
			}
			dispose();
		}
		
	}

	/**
	 * 
	 */
	private class RetryButtonTimer extends Timer {

		private int secs;

		/**
		 * @param secs
		 */
		public RetryButtonTimer(int secs) {
			this.secs = secs;
		}

		/**
		 * 
		 */
		public void start() {
			scheduleAtFixedRate(new TimerTask() {
				public void run() {
					timerTriggered();
				}
			}, 1000, 1000);
		}

		/**
		 * 
		 */
		private void timerTriggered() {
			secs--;
			retryButton.setText(retryButtonText + secs);
			if (secs == 0) {
				userAnswer = RETRY_VALUE;
				dispose();
			}
		}

	}
	
	private static final int SECONDS_TO_WAIT = 30;
	
	public static final int NO_VALUE = 0;
	public static final int RETRY_VALUE = 1;
	public static final int RETRY_NEXT_STARTUP_VALUE = 2;
	public static final int DISCARD_VALUE = 3;	
	
	private JPanel messagePanel;
	private JButton retryButton, discardButton, tryOnNextStartupButton;
	private String retryButtonText;
	private RetryButtonTimer timer;
	private int userAnswer = NO_VALUE;
	
	/**
	 * @param owner
	 */

	public MessageUploadFailedDialog(Frame owner) {
		super(owner, true);
		
		Language language = Language.getInstance();
		
		retryButtonText = language.getString("Retry") + " - ";

		setTitle(language.getString("Upload of message failed"));

		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(mainPanel);

		BorderLayout layout = new BorderLayout(0, 15);
		mainPanel.setLayout(layout);

		timer = new RetryButtonTimer(SECONDS_TO_WAIT);

		Icon warningIcon = UIManager.getIcon("OptionPane.warningIcon");
		String warningText = "  " + language.getString("Frost was not able to upload your message.");
		mainPanel.add(new JLabel(warningText, warningIcon, SwingConstants.LEFT), BorderLayout.NORTH);

		mainPanel.add(getButtonPanel(language), BorderLayout.SOUTH);

		ButtonListener bl = new ButtonListener();
		retryButton.addActionListener(bl);
		tryOnNextStartupButton.addActionListener(bl);
		discardButton.addActionListener(bl);

		setResizable(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		pack();
		setLocationRelativeTo(owner);
	}
	
	/**
	 * @return
	 */
	public int startDialog() {
		retryButton.requestFocus();
		timer.start();
		setModal(true); // paranoia
		setVisible(true);
		return userAnswer;
	}
	
	/**
	 * @return
	 */
	private JPanel getButtonPanel(Language language) {
		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
				
		retryButton = new JButton(retryButtonText + SECONDS_TO_WAIT);
		discardButton = new JButton(language.getString("Discard message"));
		tryOnNextStartupButton = new JButton(language.getString("Retry on next startup"));
		
		buttonsPanel.add(retryButton);
		buttonsPanel.add(tryOnNextStartupButton);
		buttonsPanel.add(discardButton);
		
		return buttonsPanel;
	}

	/* (non-Javadoc)
	 * @see java.awt.Window#dispose()
	 */
	public void dispose() {
		timer.cancel();
		super.dispose();
	}

}
