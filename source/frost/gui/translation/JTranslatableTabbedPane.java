/*
 * Created on Apr 7, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.gui.translation;

import java.awt.Component;

import javax.swing.JTabbedPane;

/**
 * @author Administrator
 *
 * This subclass of JTabbedPane lets the user add tabs and get their index
 * by the key to an UpdatingLanguageResource, instead of by the localized 
 * title. If the language of that resource changes, the titles 
 * automatically change too.
 */
public class JTranslatableTabbedPane extends JTabbedPane {

	private UpdatingLanguageResource languageResource;

	/**
	 * 
	 */
	public JTranslatableTabbedPane(UpdatingLanguageResource newLanguageResource) {
		super();
		languageResource = newLanguageResource;
	}

	/**
	 * This method returns the localized title
	 * of the tab in the specified position.
	 * 
	 * @see javax.swing.JTabbedPane#getTitleAt(int)
	 */
	public String getTitleAt(int index) {
		String key = super.getTitleAt(index); 
		return languageResource.getString(key);
	}

	/**
	 * This method returns the position of the tab whose title
	 * has the UpdatingLanguageResource key passed as a parameter, 
	 * or -1 if no one was found.
	 * 
	 * @see javax.swing.JTabbedPane#indexOfTab(java.lang.String)
	 */
	public int indexOfTab(String key) {
		for (int i = 0; i < getTabCount(); i++) {
			if (super.getTitleAt(i).equals(key == null ? "" : key)) {
				return i;
			}
		}
		return -1;
	}

}
