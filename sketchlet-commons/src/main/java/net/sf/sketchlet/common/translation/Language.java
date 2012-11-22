/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.translation;

import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Properties;

/**
 *
 * @author zobrenovic
 */
public class Language {

    private static Properties translation;
    private static Hashtable<String, String> hash = new Hashtable<String, String>();

    public static void loadTranslation(String strFile) {
        if (strFile == null) {
            translation = null;
            return;
        }

        translation = new Properties();
        try {
            translation.loadFromXML(new FileInputStream(strFile));
            hash.clear();
            for (String strKey : translation.stringPropertyNames()) {
                hash.put(strKey.toLowerCase(), translation.getProperty(strKey));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static Properties missingTerms = new Properties();

    public static void saveMissingTerms(String strFile) {
        /*try {
            PrintWriter pw = new PrintWriter(new FileWriter(strFile));
            for (String strTerm : missingTerms.stringPropertyNames()) {
                pw.println(strTerm);
            }
            pw.flush();
            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    public static String translate(String strText) {
        if (translation == null || strText == null || strText.isEmpty()) {
            return strText;
        }

        String result = translation.getProperty(strText);
        if (result == null) {
            result = hash.get(strText.toLowerCase());
        }
        if (result == null) {
            missingTerms.put(strText, strText);
        }
        return result != null ? result : strText;
    }

    public static void main(String args[]) throws Exception {
        System.out.println(translate("File"));
    }
}
