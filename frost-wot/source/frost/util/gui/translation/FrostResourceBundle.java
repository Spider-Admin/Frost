/*
 FrostResourceBundle.java / Frost
 Copyright (C) 2006  Frost Project <jtcfrost.sourceforge.net>

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
import java.util.*;

/**
 * Because the Java PropertyResourceBundle does still not support
 * UTF-8 properties files, we use our own bundle. 
 */
public class FrostResourceBundle {

//    private static Logger logger = Logger.getLogger(FrostResourceBundle.class.getName());

    private static final String BUNDLE_NAME = "/i18n/langres"; // base name in jar file
    
    private Map bundle;
    private FrostResourceBundle parentBundle = null;

    /**
     * Load the root bundle.
     */
    public FrostResourceBundle() {
        String resource = BUNDLE_NAME+".properties";
        bundle = FrostResourceBundleReader.loadBundle(resource);
    }
    
    /**
     * Load bundle for localeName (de,en,...), and use parent bundle as fallback.
     */
    public FrostResourceBundle(String localeName, FrostResourceBundle parent) {
        parentBundle = parent;
        if( localeName.length() == 0 ) {
            // use parent only
            bundle = new HashMap();
        } else {
            String resource = BUNDLE_NAME + "_" + localeName + ".properties";
            bundle = FrostResourceBundleReader.loadBundle(resource);
        }
    }

    /**
     * Load bundle for File, without fallback. For tests of new properties files.
     */
    public FrostResourceBundle(File bundleFile) {
        bundle = FrostResourceBundleReader.loadBundle(bundleFile);
    }
    
    /**
     * Load bundle for File, with fallback. For uses user properties files.
     */
    public FrostResourceBundle(File bundleFile, FrostResourceBundle parent) {
    	parentBundle = parent;
        bundle = FrostResourceBundleReader.loadBundle(bundleFile);
    }

    public String getString(String key) throws MissingResourceException {
        String value;
        value = (String)bundle.get(key);
        if( value == null ) {
            if( parentBundle != null ) {
                value = parentBundle.getString(key);
            } else {
                throw new MissingResourceException("Key is missing.", "FrostResourceBundle", key);
            }
        }
        return value;
    }
    
    public Collection getKeys() {
        return bundle.keySet();
    }
}
