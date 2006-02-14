package res;

import java.io.*;
import java.util.*;

import frost.components.translate.*;

public class CleanUpLangRes {
    
    // LangRes.java is most current version.
    // this class checks all other LangRes_XXX files for String that are
    // not longer defined in LangRes.java

    static ListResourceBundle origLangResBundle = new LangRes();
    static HashSet origLangResKeys = new HashSet();
    
    // TODO: read .java using LanguageFile, remove unneeded keys and save using LanguageFile!
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        
        readLangResKeys();
        
        readJava("C:\\Projects\\fr-wot\\source\\res\\LangRes_bg.java");
        System.out.println("-----------------------");
        showDeletedStrings("LangRes_bg", new LangRes_bg());

        System.out.println("*********************************************");

        readJava("C:\\Projects\\fr-wot\\source\\res\\LangRes_de.java");
        System.out.println("-----------------------");
        showDeletedStrings("LangRes_de", new LangRes_de());

        System.out.println("*********************************************");

        readJava("C:\\Projects\\fr-wot\\source\\res\\LangRes_es.java");
        System.out.println("-----------------------");
        showDeletedStrings("LangRes_es", new LangRes_es());

        System.out.println("*********************************************");

        readJava("C:\\Projects\\fr-wot\\source\\res\\LangRes_fr.java");
        System.out.println("-----------------------");
        showDeletedStrings("LangRes_fr", new LangRes_fr());

        System.out.println("*********************************************");

        readJava("C:\\Projects\\fr-wot\\source\\res\\LangRes_it.java");
        System.out.println("-----------------------");
        showDeletedStrings("LangRes_it", new LangRes_it());

        System.out.println("*********************************************");

        readJava("C:\\Projects\\fr-wot\\source\\res\\LangRes_ja.java");
        System.out.println("-----------------------");
        showDeletedStrings("LangRes_ja", new LangRes_ja());

        System.out.println("*********************************************");

        readJava("C:\\Projects\\fr-wot\\source\\res\\LangRes_nl.java");
        System.out.println("-----------------------");
        showDeletedStrings("LangRes_nl", new LangRes_nl());
    }
    
    public static void readJava(String fileName) {
        TranslateTableModel ttm = new TranslateTableModel();
        File file = new File(fileName);
        LanguageFile.readLanguageFile(ttm, file);
        int count = 0;
        
        for(int x=0; x < ttm.getRowCount(); x++) {
            String key = (String)ttm.getValueAt(x, 0);
            String val = (String)ttm.getValueAt(x, 1);
            if( origLangResKeys.contains(key) == false ) {
                System.out.println("Key not longer in LangRes, but in "+file.getName()+": '"+key+"'");
                count++;
            }
        }
        System.out.println(">>> Count = "+count);
    }
    
    public static void showDeletedStrings(String className, ListResourceBundle bundle) {
        int count = 0;
        for(Enumeration e = bundle.getKeys(); e.hasMoreElements(); ) {
            String bundleKey = (String)e.nextElement();
            try {
                origLangResBundle.getString(bundleKey);
            } catch(Throwable t) {
                System.out.println("Key not longer in LangRes, but in "+className+": '"+bundleKey+"'");
                count++;
            }
        }
        System.out.println(">>> Count = "+count);
    }
    
    public static void readLangResKeys() {
        TranslateTableModel ttm = new TranslateTableModel();
        File file = new File("C:\\Projects\\fr-wot\\source\\res\\LangRes.java");
        LanguageFile.readLanguageFile(ttm, file);
        
        for(int x=0; x < ttm.getRowCount(); x++) {
            String key = (String)ttm.getValueAt(x, 0);
            origLangResKeys.add(key);
        }
    }

}
