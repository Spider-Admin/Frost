package res;

import java.util.*;

public class CleanUpLangRes {
    
    // LangRes.java is most current version.
    // this class checks all other LangRes_XXX files for String that are
    // not longer defined in LangRes.java

    static ListResourceBundle origLangRes = new LangRes();
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        showDeletedStrings("LangRes_bg", new LangRes_bg());
        showDeletedStrings("LangRes_de", new LangRes_de());
        showDeletedStrings("LangRes_en", new LangRes_en());
        showDeletedStrings("LangRes_es", new LangRes_es());
        showDeletedStrings("LangRes_fr", new LangRes_fr());
        showDeletedStrings("LangRes_it", new LangRes_it());
        showDeletedStrings("LangRes_ja", new LangRes_ja());
        showDeletedStrings("LangRes_nl", new LangRes_nl());
    }
    
    public static void showDeletedStrings(String className, ListResourceBundle bundle) {
        
        for(Enumeration e = bundle.getKeys(); e.hasMoreElements(); ) {
            String bundleKey = (String)e.nextElement();
            try {
                origLangRes.getString(bundleKey);
            } catch(Throwable t) {
                System.out.println("Key not longer in LangRes, but in "+className+": '"+bundleKey+"'");
            }
        }
    }
}
