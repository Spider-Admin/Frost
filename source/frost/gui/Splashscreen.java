/*
  Splashscreen.java
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
import java.util.logging.Logger;

import javax.swing.*;

public class Splashscreen extends JDialog {

	private static Logger logger = Logger.getLogger(Splashscreen.class.getName());

	//Splashscreen size depends on this image. 
	private ImageIcon frostLogo = new ImageIcon(Splashscreen.class.getResource("/data/logo.png"));

	//GUI Objects
	JPanel mainPanel = new JPanel(new BorderLayout());
	JLabel pictureLabel = new JLabel();
	JProgressBar progressBar = new JProgressBar(0, 100);

	/**Constructor*/
	public Splashscreen() {
		init();
	}

	/**Close the splashscreen*/
	public void closeMe() {
		hide();
		dispose();
		logger.info("Splashscreen: I'm gone now :-(");
	}

	/**Component initialization*/
	private void init() {

		setUndecorated(true);
		setResizable(false);

		pictureLabel.setIcon(frostLogo);

		progressBar.setStringPainted(true);
		progressBar.setString("Starting...");

		getContentPane().add(mainPanel);
		mainPanel.add(pictureLabel, BorderLayout.CENTER);
		mainPanel.add(progressBar, BorderLayout.SOUTH);

		pack();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension splashscreenSize = getSize();
		if (splashscreenSize.height > screenSize.height) {
			splashscreenSize.height = screenSize.height;
		}
		if (splashscreenSize.width > screenSize.width) {
			splashscreenSize.width = screenSize.width;
		}
		setLocation(
			(screenSize.width - splashscreenSize.width) / 2,
			(screenSize.height - splashscreenSize.height) / 2);
	}

	/**
	 * Set progress for the progressBar.
	 * Default range is from 0 to 100.
	 * */
	public void setProgress(int progress) {
		progressBar.setValue(progress);
	}

	/**Set the text for the progressBar*/
	public void setText(String text) {
		progressBar.setString(text);
	}

}
