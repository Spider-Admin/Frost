/*
 * Created on Nov 9, 2003
 *
 */
package frost.util.gui.translation;

import java.util.*;

import javax.swing.event.EventListenerList;

/**
 * @pattern Singleton
 * 
 * @author $Author$
 * @version $Revision$
 */
public class Language {

	private ResourceBundle resourceBundle;
	
	private static boolean initialized = false;
	
	/**
	 * The unique instance of this class.
	 */
	private static Language instance = null;

	/** 
	 * A list of event listeners for this component. 
	 */
	protected EventListenerList listenerList = new EventListenerList();

	/**
	 * Prevent instances of this class from being created.
	 */
	private Language(ResourceBundle resourceBundle) {
		super();
		this.resourceBundle = resourceBundle;
	}
	
	/**
	 * Return the unique instance of this class.
	 *
	 * @return the unique instance of this class
	 */
	public static Language getInstance() {
		return instance;
	}
	
	/**
	 * This method initializes the Language with the given ResourceBundle.
	 * If it has already been initialized, this method does nothing.
	 * @param bundle
	 */
	public static void initialize(ResourceBundle bundle) {
		if (!initialized) {
			initialized = true;
			instance = new Language(bundle);
		}
	}

	/**
	 * If it has already been initialized, this method does nothing.
	 * @param bundleBaseName
	 */
	public static void initialize(String bundleBaseName) {
		initialize(bundleBaseName, Locale.getDefault());
	}

	/**
	 * If it has already been initialized, this method does nothing.
	 * @param bundleBaseName
	 * @param locale
	 */
	public static void initialize(String bundleBaseName, Locale locale) {
		initialize(ResourceBundle.getBundle(bundleBaseName, locale));
	}

	/**
	 * @return
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/**
	 * Adds an <code>LanguageListener</code> to the Language.
	 * @param listener the <code>LanguageListener</code> to be added
	 */
	public void addLanguageListener(LanguageListener listener) {
		listenerList.add(LanguageListener.class, listener);
	}

	/**
	 * Returns an array of all the <code>LanguageListener</code>s added
	 * to this Language with addLanguageListener().
	 *
	 * @return all of the <code>LanguageListener</code>s added or an empty
	 *         array if no listeners have been added
	 */
	public LanguageListener[] getActionListeners() {
		return (LanguageListener[]) (listenerList.getListeners(LanguageListener.class));
	}

	/**
	 * Removes an <code>LanguageListener</code> from the Language.
	 * @param listener the <code>LanguageListener</code> to be removed
	 */
	public void removeLanguageListener(LanguageListener listener) {
		listenerList.remove(LanguageListener.class, listener);
	}

	/**
	 * Notifies all listeners that have registered interest for
	 * notification on this event type.  The event instance 
	 * is lazily created using the <code>event</code> 
	 * parameter.
	 *
	 * @param event  the <code>LanguageEvent</code> object
	 * @see EventListenerList
	 */
	protected void fireLanguageChanged(LanguageEvent event) {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		LanguageEvent e = null;
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == LanguageListener.class) {
				// Lazily create the event:
				if (e == null) {
					e = new LanguageEvent(Language.this);
				}
				((LanguageListener) listeners[i + 1]).languageChanged(e);
			}
		}
	}

	/**
	 * @param resourceBundle
	 */
	public void setLanguageResource(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
		fireLanguageChanged(new LanguageEvent(this));
	}

	/**
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		return resourceBundle.getString(key);
	}

}
