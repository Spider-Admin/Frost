/*
 * Created on Dec 15, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.components;

import java.awt.*;
import java.util.Collection;

import javax.swing.*;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MiscToolkit {
	
	private static MiscToolkit instance = null;
	
	/**
	 * Prevent instances of this class from being created.
	 */
	private MiscToolkit() {
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
				setContainerEnabledInner((Container)component, enabled);
			} else {
				component.setEnabled(enabled);
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
					setContainerEnabledInner((Container)component, enabled);
				} else {
					component.setEnabled(enabled);
				}
			} 
			container.setEnabled(enabled);
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
	 * @param exceptions
	 */
	private void setContainerEnabledInner(Container container, boolean enabled, Collection exceptions) {
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
		java.awt.Dimension dimension = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dimension.width / 2, dimension.height / 2);
		frame.setUndecorated(true);
		frame.show(); 
		frame.toFront();
		optionPane.createDialog(frame, title).show();
		frame.dispose();	
	}
	/**
	 * Return the unique instance of this class.
	 *
	 * @return the unique instance of this class
	 */
	public synchronized static MiscToolkit getInstance() {
		if (instance == null) {
			instance = new MiscToolkit();	
		}
		return instance;
	}

}
