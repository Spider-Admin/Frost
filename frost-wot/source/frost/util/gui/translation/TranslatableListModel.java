/*
 * Created on Dec 5, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package frost.util.gui.translation;

import swingwtx.swing.DefaultListModel;

/**
 * A translatable list model contains keys to a languageResource. It shows the localized values on screen.
 * Its getElementAt method returns the localized value, while the mehtod getKeyAt returns the key.
 */
public class TranslatableListModel extends DefaultListModel implements LanguageListener {

	private UpdatingLanguageResource languageResource = null;

	/**
	 * @param newLanguageResource
	 */
	public TranslatableListModel(UpdatingLanguageResource newLanguageResource) {
		super();
		languageResource = newLanguageResource;
		languageResource.addLanguageListener(this);
	}

	/* (non-Javadoc)
	 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
	 */
	public void languageChanged(LanguageEvent event) {
		fireContentsChanged(this, 0, getSize() - 1);
	}

	/** 
	 * This method returns the internationalized value at a given position
	 */
	public Object getElementAt(int index) {
		String key = super.getElementAt(index).toString();
		return languageResource.getString(key);
	}

	/**
	 * This method returns the key at a given position
	 * @param selectedIndex
	 * @return the key 
	 */
	public String getKeyAt(int selectedIndex) {
		return super.getElementAt(selectedIndex).toString();
	}

	/**
	 * This method returns the position of the key in the model
	 * @param key
	 * @return the position
	 */
	public int indexOfKey(Object key) {
		return super.indexOf(key);
	}

	/**
	 * This method returns the position of the first occurrence of the
	 * internationalized element in the model
	 * @param elem internationalized item
	 * @return the position
	 */
	public int indexOf(Object elem) {
		int position = -1;
		for (int i = 0; (i < getSize() - 1) || (position != -1);i++) {
			String localizedValue = languageResource.getString(getKeyAt(i));
			if (elem.equals(localizedValue)) {
				position = i;	
			}
		} 
		return super.indexOf(elem);
	}

}
