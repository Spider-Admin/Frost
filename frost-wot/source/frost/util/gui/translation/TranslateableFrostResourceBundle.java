/*
 TranslateableFrostResourceBundle.java / Frost
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
import java.util.logging.*;

public class TranslateableFrostResourceBundle extends FrostResourceBundle {

    private static final Logger logger = Logger.getLogger(TranslateableFrostResourceBundle.class.getName());

    /**
     * Changed default constructor:
     * Start with empty bundle.
     */
    public TranslateableFrostResourceBundle() {
        bundle = new HashMap();
    }

    /**
     * Load build-in bundle for localeName (de,en,...), and use parent bundle as fallback.
     */
    public TranslateableFrostResourceBundle(String localeName, FrostResourceBundle parent, boolean isExternal) {
        super(localeName, parent, isExternal);
    }
    
    /**
     * Load bundle for File, without fallback. For tests of new properties files.
     */
    public TranslateableFrostResourceBundle(File bundleFile) {
        super(bundleFile);
    }

    /**
     * Removes the key from bundle, returns old value if there was one.
     */
    public String removeKey(String key) {
        return (String)bundle.remove(key);
    }
    
    /**
     * Sets key to value, returns old value if there was one.
     */
    public String setKey(String key, String value) {
        return (String)bundle.put(key, value);
    }
    
    public boolean containsKey(String key) {
        return bundle.containsKey(key);
    }

    /**
     * Save the bundle to a file.
     * Returns false if save was not successful.
     */
    public boolean saveBundleToFile(String localeName) {
        try {
            File externalBundleDir = new File(EXTERNAL_BUNDLE_DIR);
            if( !externalBundleDir.isDirectory() ) {
                externalBundleDir.mkdirs();
            }
            String filename = EXTERNAL_BUNDLE_DIR + "langres_"+localeName+".properties";
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8")));
            
            TreeMap sorter = new TreeMap(bundle);
            
            for( Iterator i = sorter.keySet().iterator(); i.hasNext(); ) {
                String key = (String)i.next();
                String val = getString(key);
                key = key.trim();
                val = val.trim();
                // change newlines in val into \n
                StringBuilder sbTmp = new StringBuilder();
                for(int x=0; x < val.length(); x++) {
                    char c = val.charAt(x);
                    if( c == '\n' ) {
                        sbTmp.append("\\n");
                    } else {
                        sbTmp.append(c);
                    }
                }
                val = sbTmp.toString();

                out.println(key + "=" + val);
            }
            
            out.close();
            return true;
        } catch(Throwable t) {
            logger.log(Level.SEVERE, "Error saving bundle.", t);
            return false;
        }
    }
}
