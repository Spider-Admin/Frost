/*
  Language.java / Frost
  Copyright (C) 2001  Frost Project <jtcfrost.sourceforge.net>

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
--------------------------------------------------------------------------
  DESCRIPTION:
  This file contains the whole 'Options' dialog. It first reads the
  actual config from properties file, and on 'OK' it saves all
  settings to the properties file and informs the caller to reload
  this file.
*/
package frost.util.gui.translation;

import java.util.*;
import java.util.logging.*;

import javax.swing.event.EventListenerList;

/**
 * @pattern Singleton
 */
public class Language {

    private static Logger logger = Logger.getLogger(Language.class.getName());

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
        String s;
        try {
            s = resourceBundle.getString(key);
        } catch(Throwable t) {
            s = null;
            logger.log(Level.SEVERE,"Exception catched", t);
        }
        if( s == null ) {
            logger.severe("No translation found for key '"+key+"', using key.");
            return key;
        } else {
            return s;
        }
    }

}
