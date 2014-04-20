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
*/
package frost.util.gui.translation;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;

import javax.swing.event.*;

/**
 * @pattern Singleton
 */
public class Language {

    private static final Logger logger = Logger.getLogger(Language.class.getName());

    private FrostResourceBundle RESOURCE_BUNDLE = null;
    private FrostResourceBundle ROOT_RESOURCE_BUNDLE = null;
//    private BreakIterator LINE_BREAKER = null;

    private static boolean initialized = false;

    private final MessageFormat formatter = new MessageFormat("");

    private final Object[] objectLen1 = new Object[1];
    private final Object[] objectLen2 = new Object[2];
    private final Object[] objectLen3 = new Object[3];

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
    private Language(final String localeName, final boolean isExternal) {
        super();
        ROOT_RESOURCE_BUNDLE = new FrostResourceBundle();
        RESOURCE_BUNDLE = new FrostResourceBundle(localeName.toLowerCase(), ROOT_RESOURCE_BUNDLE, isExternal);
//        LINE_BREAKER = BreakIterator.getLineInstance(RESOURCE_BUNDLE.getLocale());
    }

    private Language(final File bundleFile) {
        ROOT_RESOURCE_BUNDLE = new FrostResourceBundle(bundleFile);
        RESOURCE_BUNDLE = ROOT_RESOURCE_BUNDLE;
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
     * One time init.
     * Takes an initial locale name and if it is a build-in or extern bundle.
     */
    public static void initializeWithName(String localeName, final boolean isExternal) {
        if( !initialized ) {
            initialized = true;

            if( localeName == null ) {
                localeName = Locale.getDefault().getCountry();
            }
            instance = new Language(localeName.toLowerCase(), isExternal);
        }
    }

    /**
     * One time init.
     * Takes an initial locale name and uses either extern (preferred) or intern bundle for this locale.
     */
    public static void initializeWithName(String localeName) {
        if( !initialized ) {
            initialized = true;

            Locale locale;

            if( localeName == null ) {
                locale = Locale.getDefault();
                localeName = Locale.getDefault().getCountry();
            } else {
                locale = new Locale(localeName);
            }

            boolean isExternal;
            if( getExternalLocales().contains(locale) ) {
                isExternal = true;
            } else {
                isExternal = false;
            }
            instance = new Language(localeName.toLowerCase(), isExternal);
        }
    }

    public static void initializeWithFile(final File bundleFile) {
        if( !initialized ) {
            initialized = true;
            instance = new Language(bundleFile);
        }
    }

    /**
     * Adds an <code>LanguageListener</code> to the Language.
     * @param listener the <code>LanguageListener</code> to be added
     */
    public void addLanguageListener(final LanguageListener listener) {
        listenerList.add(LanguageListener.class, listener);
    }

    /**
     * Returns an array of all the <code>LanguageListener</code>s added
     * to this Language with addLanguageListener().
     *
     * @return all of the <code>LanguageListener</code>s added or an empty
     *         array if no listeners have been added
     */
    public LanguageListener[] getLanguageListeners() {
        return (listenerList.getListeners(LanguageListener.class));
    }

    /**
     * Removes an <code>LanguageListener</code> from the Language.
     * @param listener the <code>LanguageListener</code> to be removed
     */
    public void removeLanguageListener(final LanguageListener listener) {
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
    protected void fireLanguageChanged(final LanguageEvent event) {
        // Guaranteed to return a non-null array
        final Object[] listeners = listenerList.getListenerList();
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
     * Scans for existing properties files in localdata/i18n each time.
     */
    public static List<Locale> getExternalLocales() {
        final ArrayList<Locale> lst = new ArrayList<Locale>();
        final File i18nDir = new File("localdata/i18n");
        if( i18nDir.isDirectory() == false ) {
            return lst;
        }
        final File[] files = new File("localdata/i18n").listFiles();
        if( files == null ) {
            return lst;
        }
        for( final File f : files ) {
            final String fname = f.getName();
            if( fname.startsWith("langres_") && fname.endsWith(".properties") ) {
                final String ln = fname.substring("langres_".length(), fname.length() - ".properties".length());
                if( ln.length() == 2 ) {
                    lst.add(new Locale(ln));
                }
            }
        }
        return lst;
    }

    /**
     * @param resourceBundle
     */
    public synchronized void changeLanguage(String localeName, final boolean isExternal) {
        if( localeName == null ) {
            localeName = Locale.getDefault().getLanguage();
        }
        RESOURCE_BUNDLE = new FrostResourceBundle(localeName.toLowerCase(), ROOT_RESOURCE_BUNDLE, isExternal);
//        LINE_BREAKER = BreakIterator.getLineInstance(RESOURCE_BUNDLE.getLocale());

        fireLanguageChanged(new LanguageEvent(this));
    }

    /**
     * @param key
     * @return
     */
    public String getString(final String origKey) {
        String s;
        try {
            s = RESOURCE_BUNDLE.getString(origKey);
        } catch(final MissingResourceException t) {
            s = null;
        } catch(final Throwable t) {
            s = null;
            logger.log(Level.SEVERE, "Exception catched", t);
        }
        if( s == null ) {
            logger.severe("No translation found for key '"+origKey+"', using key.");
            return origKey;
        } else {
            return s;
        }
    }

/////////////////////////////////////////////////////////////////////////////////////7
//    /**
//     * Builds a String containing the source String broken into lines
//     * of maxLength length, using \n as line breaker.
//     */
//    public synchronized String breakLinesText(String source, int maxLength) {
//
//        LINE_BREAKER.setText(source);
//        int start = LINE_BREAKER.first();
//        int end = LINE_BREAKER.next();
//        int lineLength = 0;
//
//        StringBuilder result = new StringBuilder();
//
//        while (end != BreakIterator.DONE) {
//            String word = source.substring(start,end);
//            if( word.indexOf('\n') > -1 ) {
//                // wenn dieses word NICHT mehr passt, dann auf neue zeile, und gleich noch ne neue Zeile wg. \n
//                lineLength = 0; // TODO: fix
//            } else {
//                lineLength = lineLength + word.length();
//                if (lineLength > maxLength) {
//                    result.append("\n");
//                    lineLength = word.length();
//                }
//            }
//            result.append(word);
//            start = end;
//            end = LINE_BREAKER.next();
//        }
//        return result.toString();
//    }
//
//    /**
//     * Builds a HTML String containing the source String broken into lines
//     * of maxLength length, using 'br' as line breaker.
//     */
//    public synchronized String breakLinesHtml(String source, int maxLength) {
//
//        LINE_BREAKER.setText(source);
//        int start = LINE_BREAKER.first();
//        int end = LINE_BREAKER.next();
//        int lineLength = 0;
//
//        StringBuilder result = new StringBuilder();
//        result.append("<html>");
//
//        while (end != BreakIterator.DONE) {
//            String word = source.substring(start,end);
//            lineLength = lineLength + word.length();
//            if (lineLength > maxLength) {
//                result.append("<br>");
//                lineLength = word.length();
//            }
//            result.append(word);
//            start = end;
//            end = LINE_BREAKER.next();
//        }
//        result.append("</html>");
//        return result.toString();
//    }

    public synchronized String formatMessage(final String msg, final Object[] objs) {
        try {
            final String pattern = getString(msg);
            formatter.applyPattern(pattern);
            final String output = formatter.format(objs);
            return output;
        } catch(final IllegalArgumentException ex) {
            return '!' + msg + '!';
        }
    }

    public synchronized String formatMessage(final String msg, final Object obj1) {
        objectLen1[0] = obj1;
        return formatMessage(msg, objectLen1);
    }
    public synchronized String formatMessage(final String msg, final Object obj1, final Object obj2) {
        objectLen2[0] = obj1;
        objectLen2[1] = obj2;
        return formatMessage(msg, objectLen2);
    }
    public synchronized String formatMessage(final String msg, final Object obj1, final Object obj2, final Object obj3) {
        objectLen3[0] = obj1;
        objectLen3[1] = obj2;
        objectLen3[2] = obj3;
        return formatMessage(msg, objectLen3);
    }
}
