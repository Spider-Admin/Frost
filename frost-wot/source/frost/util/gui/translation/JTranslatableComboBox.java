/*
 * Created on Nov 19, 2003
 */
package frost.util.gui.translation;

import javax.swing.JComboBox;

/**
 * @author $Author$
 * @version $Revision$
 */
public class JTranslatableComboBox extends JComboBox implements LanguageListener {

	/**
	 * 
	 */
	private class CheckBoxItem {

		private String key = null;
		private String value = null;

		/**
		 * @param key
		 * @param value
		 */
		public CheckBoxItem(String key, String value) {
			super();
			this.key = key;
			this.value = value;
		}
		
		/**
		 * @return
		 */
		public String getKey() {
			return key;
		}
		
		/**
		 * @param value
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return value;
		}

	}
	private Language language = null;
	private String[] keys;

	/**
	 * @param language
	 * @param keys
	 */
	public JTranslatableComboBox(Language language, String[] keys) {
		super();
		this.language = language;
		this.keys = keys;
		language.addLanguageListener(this);
		for (int i = 0; i < keys.length; i++) {
			String value = language.getString(keys[i]);
			CheckBoxItem item = new CheckBoxItem(keys[i], value);
			addItem(item);
		}
	}

	/* (non-Javadoc)
	 * @see frost.gui.translation.LanguageListener#languageChanged(frost.gui.translation.LanguageEvent)
	 */
	public void languageChanged(LanguageEvent event) {
		refreshLanguage();		
	}

	/**
	 * 
	 */
	private void refreshLanguage() {
		for (int i = 0; i < keys.length; i++) {
			CheckBoxItem item = (CheckBoxItem) getItemAt(i);
			String newValue = language.getString(item.getKey());
			item.setValue(newValue);
		}
		//This is done to refresh the horizontal size
		CheckBoxItem dummy = new CheckBoxItem("","");	
		addItem(dummy);
		removeItem(dummy);	
	}
	
	/**
	 * @return
	 */
	public String getSelectedKey() { 
		Object selectedItem = getSelectedItem();
		if ((selectedItem != null) && (selectedItem instanceof CheckBoxItem)) {
			return ((CheckBoxItem) selectedItem).getKey();
		} else {
			return null;
		}
	}
	
	/**
	 * If no item contains that key, the selection remains unchanged.
	 * @param aKey the key of the item to select
	 */
	public void setSelectedKey(String aKey) {
		boolean found = false;
		for (int i = 0;(i < getItemCount()) && !found; i++) {
			Object item = getItemAt(i);
			if (item instanceof CheckBoxItem) {
				if (((CheckBoxItem) item).getKey().equals(aKey)) {
					setSelectedIndex(i);
					found = true;
				}
			}
		}
	}

}
