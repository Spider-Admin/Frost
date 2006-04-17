package res;

import java.util.*;

public class CleanUpLangRes {
    
    // LangRes.java is most current version.
    // this class checks all other LangRes_XXX files for String that are
    // not longer defined in LangRes.java

    static ListResourceBundle origLangResBundle = new LangRes();
    static HashSet origLangResKeys = new HashSet();
    
    static LangRes_bg bg = new LangRes_bg();
    static LangRes_de de = new LangRes_de();
    static LangRes_es es = new LangRes_es();
    static LangRes_fr fr = new LangRes_fr();
    static LangRes_it it = new LangRes_it();
    static LangRes_ja ja = new LangRes_ja();
    static LangRes_nl nl = new LangRes_nl();
    
    public static void main(String[] args) {
        
        System.out.println("-----------------------");
        showDeletedStrings(bg.getClass().getName(), bg);
        showMissingStrings(bg.getClass().getName(), bg);

        System.out.println("*********************************************");

        System.out.println("-----------------------");
        showDeletedStrings(de.getClass().getName(), de);
        showMissingStrings(de.getClass().getName(), de);

        System.out.println("*********************************************");

        System.out.println("-----------------------");
        showDeletedStrings(es.getClass().getName(), es);
        showMissingStrings(es.getClass().getName(), es);

        System.out.println("*********************************************");

        System.out.println("-----------------------");
        showDeletedStrings(fr.getClass().getName(), fr);
        showMissingStrings(fr.getClass().getName(), fr);

        System.out.println("*********************************************");

        System.out.println("-----------------------");
        showDeletedStrings(it.getClass().getName(), it);
        showMissingStrings(it.getClass().getName(), it);

        System.out.println("*********************************************");

        System.out.println("-----------------------");
        showDeletedStrings(ja.getClass().getName(), ja);
        showMissingStrings(ja.getClass().getName(), ja);

        System.out.println("*********************************************");

        System.out.println("-----------------------");
        showDeletedStrings(nl.getClass().getName(), nl);
        showMissingStrings(nl.getClass().getName(), nl);
    }
    
    public static void showDeletedStrings(String className, ListResourceBundle bundle) {
        int count = 0;
        for(Enumeration e = bundle.getKeys(); e.hasMoreElements(); ) {
            String bundleKey = (String)e.nextElement();
            try {
                origLangResBundle.getString(bundleKey);
            } catch(Throwable t) {
                System.out.println("Key could be deleted in "+className+": '"+bundleKey+"'");
                count++;
            }
        }
        System.out.println(">>> Count = "+count);
    }
    
    public static void showMissingStrings(String className, ListResourceBundle bundle) {
        int count = 0;
        for(Enumeration e = origLangResBundle.getKeys(); e.hasMoreElements(); ) {
            String bundleKey = (String)e.nextElement();
            try {
                bundle.getString(bundleKey);
            } catch(Throwable t) {
                System.out.println("Key missing in "+className+": '"+bundleKey+"'");
                count++;
            }
        }
        System.out.println(">>> Count = "+count);
    }
}
