/*
 * Created on Dec 15, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.components;

import java.awt.*;
import java.util.*;
import java.util.logging.Logger;

import javax.swing.*;

/**
 * @author Administrator
 *
 * This a gui related utilities class.
 */
public class MiscToolkit {

	private static MiscToolkit instance = new MiscToolkit();

	private static Logger logger = Logger.getLogger(MiscToolkit.class.getName());
	/**
	 * Return the unique instance of this class.
	 *
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
	 * Configures a button, setting its tooltip text, rollover icon and some other
	 * default properties.
	 * @param button the button to configure
	 * @param toolTipKey language resource key to extract its tooltip text with
	 * @param rolloverIcon displayed icon when mouse arrow is over button
	 * @param languageResource language resource key to extract its tooltip text from
	 */
	public void configureButton(
		JButton button,
		String toolTipKey,
		String rolloverIcon,
		ResourceBundle languageResource) {

		String text = null;
		try {
			text = languageResource.getString(toolTipKey);
		} catch (MissingResourceException ex) {
			logger.severe("Missing resource in configureButton method: " + toolTipKey);
			text = toolTipKey; // better than nothing ;)
		}
		button.setToolTipText(text);

		button.setRolloverIcon(new ImageIcon(getClass().getResource(rolloverIcon)));
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setBorderPainted(false);
		button.setFocusPainted(false);
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
	 * This method shows a message in a JDialog
	 * @param message the message to show
	 * @param type the type of JDialog
	 * @param title the title of the JDialog
	 */
	public void showMessage(String message, int type, String title) {
		JOptionPane optionPane = new JOptionPane(message, type);
		JFrame frame = new JFrame();
		frame.setTitle("Frost");
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dimension.width / 2, dimension.height / 2);
		frame.setUndecorated(true);
		frame.show();
		frame.toFront();
		optionPane.createDialog(frame, title).show();
		frame.dispose();
	}

}
