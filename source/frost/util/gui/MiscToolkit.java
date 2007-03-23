/*
 MiscToolkit.java / Frost
 Copyright (C) 2003  Frost Project <jtcfrost.sourceforge.net>

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
package frost.util.gui;

import java.awt.*;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;

import frost.util.gui.translation.Language;

/**
 * This a gui related utilities class.
 * @author $Author$
 * @version $Revision$
 */
public class MiscToolkit {

	private static MiscToolkit instance = new MiscToolkit();

	private static Logger logger = Logger.getLogger(MiscToolkit.class.getName());
	
	/**
	 * Return the unique instance of this class.
	 * @return the unique instance of this class
	 */
	public static MiscToolkit getInstance() {
		return instance;
	}

	/**
	 * Prevent instances of this class from being created.
	 */
	private MiscToolkit() {
	}

	/**
	 * Configures a button to be a default icon button, setting its rollover icon 
	 * and some other default properties.
	 * @param button the button to configure
	 * @param rolloverIcon displayed icon when mouse arrow is over button
	 */
	public void configureButton(JButton button, String rolloverIcon) {
		button.setRolloverIcon(new ImageIcon(getClass().getResource(rolloverIcon)));
		button.setMargin(new Insets(0, 0, 0, 0));
        button.setPreferredSize(new Dimension(30,25));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
        button.setOpaque(false);
	}

    /**
     * Configures a button, setting its tooltip text, rollover icon and some other
     * default properties.
     * @param button the button to configure
     * @param toolTipKey language resource key to extract its tooltip text with
     * @param rolloverIcon displayed icon when mouse arrow is over button
     * @param language language to extract the tooltip text from
     */
	public void configureButton(
		JButton button,
		String toolTipKey,
		String rolloverIcon,
		Language language) {

		String text = null;
		try {
			text = language.getString(toolTipKey);
		} catch (MissingResourceException ex) {
            ex.printStackTrace();
			logger.severe("Missing resource in configureButton method: " + toolTipKey);
			text = toolTipKey; // better than nothing ;)
		}
		button.setToolTipText(text);

		configureButton(button, rolloverIcon);
	}
	
	/**
	 * This method loads an image from the given resource path and scales it to
	 * the dimensions passed as parameters.
	 * @param imgPath resource path to load de image from.
	 * @param width width to scale the image to.
	 * @param height height to scale the image to.
	 * @return an ImageIcon containing the image.
	 */
	public ImageIcon getScaledImage(String imgPath, int width, int height) {
		ImageIcon icon = new ImageIcon(getClass().getResource(imgPath));
		icon = new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
		return icon;
	}

	/**
	 * This method enables/disables the subcomponents of a container.
	 * If the container contains other containers, the subcomponents of those
	 * are enabled/disabled recursively.
	 * @param container
	 * @param enabled 
	 */
	public void setContainerEnabled(Container container, boolean enabled) {
		int componentCount = container.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			Component component = container.getComponent(x);
			if (component instanceof Container) {
				setContainerEnabledInner((Container) component, enabled);
			} else {
				component.setEnabled(enabled);
			}
		}
	}

	/**
	 * This method enables/disables the subcomponents of a container.
	 * If the container contains other containers, the components of those
	 * are enabled/disabled recursively.
	 * All of the components in the exceptions collection are ignored in this process.
	 * @param container
	 * @param enabled 
	 * @param exceptions the components to ignore
	 */
	public void setContainerEnabled(Container container, boolean enabled, Collection exceptions) {
		int componentCount = container.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			Component component = container.getComponent(x);
			if (!exceptions.contains(component)) {
				if (component instanceof Container) {
					setContainerEnabledInner((Container) component, enabled, exceptions);
				} else {
					component.setEnabled(enabled);
				}
			}
		}
	}

	/**
	 * @param container
	 * @param enabled
	 */
	private void setContainerEnabledInner(Container container, boolean enabled) {
		int componentCount = container.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			Component component = container.getComponent(x);
			if (component instanceof Container) {
				setContainerEnabledInner((Container) component, enabled);
			} else {
				component.setEnabled(enabled);
			}
		}
		container.setEnabled(enabled);
	}

	/**
	 * @param container
	 * @param enabled
	 * @param exceptions
	 */
	private void setContainerEnabledInner(
		Container container,
		boolean enabled,
		Collection exceptions) {
			
		int componentCount = container.getComponentCount();
		for (int x = 0; x < componentCount; x++) {
			Component component = container.getComponent(x);
			if (!exceptions.contains(component)) {
				if (component instanceof Container) {
					setContainerEnabledInner((Container) component, enabled, exceptions);
				} else {
					component.setEnabled(enabled);
				}
			}
		}
		container.setEnabled(enabled);
	}

	/**
	 * This method shows a message in a JDialog. It creates a dummy frame 
	 * so that when the dialog pops up, something appears on the task bar too.
	 * @param message the message to show
	 * @param type the type of JDialog
	 * @param title the title of the JDialog
	 */
	public void showMessage(String message, int type, String title) {
		JFrame frame = new JFrame();
		frame.setTitle("Frost");
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dimension.width / 2, dimension.height / 2);
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.toFront();
		JOptionPane.showMessageDialog(frame, message, title, type);
		frame.dispose();
	}
	
	/**
	 * This method shows a dialog requesting input from the user. It creates
	 * a dummy frame so that when the dialog pops up, something appears on
	 * the task bar too.
	 * @param message the message to show
	 * @return the text the user has input
	 */
	public String showInputDialog(Object message) {
		JFrame frame = new JFrame();
		frame.setTitle("Frost");
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dimension.width / 2, dimension.height / 2);
		frame.setUndecorated(true);
		frame.setVisible(true);
		frame.toFront();
		String returnValue = JOptionPane.showInputDialog(frame, message);
		frame.dispose();
		return returnValue;
	}
}
