/*
 * GenericAdapter.java
 *
 * Created on May 1, 2006, 1:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import net.sf.sketchlet.common.config.ConfigItem;
import net.sf.sketchlet.common.config.ConfigModule;
import org.w3c.dom.NodeList;

/**
 *
 * @author obrenovi
 */
public class SimpleProperties {

    /** Creates a new instance of GenericAdapter */
    public SimpleProperties() {
    }
    XPathEvaluator xpath;

    public String loadData(String configURLs[], String structureNames[]) {
        String strXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        strXML += "<properties>";

        for (int i = 0; i < configURLs.length; i++) {
            strXML += loadDataToXML(configURLs[i], structureNames);
        }

        strXML += "</properties>";

        this.xpath = new XPathEvaluator();
        this.xpath.createDocumentFromString(strXML);

        return strXML;
    }

    public String loadData(String configURL, String structureNames[]) {
        String strXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        strXML += "<properties>";

        strXML += loadDataToXML(configURL, structureNames);

        strXML += "</properties>";

        this.xpath = new XPathEvaluator();
        this.xpath.createDocumentFromString(strXML);

        return strXML;
    }

    public String loadData(String configURL) {
        String strXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        strXML += "<properties>";

        strXML += loadDataToXML(configURL, null);

        strXML += "</properties>";

        this.xpath = new XPathEvaluator();
        this.xpath.createDocumentFromString(strXML);

        return strXML;
    }

    public String loadData(String configURL, String structureName) {
        String strXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        strXML += "<properties>";

        strXML += loadDataToXML(configURL, new String[]{structureName});

        strXML += "</properties>";

        this.xpath = new XPathEvaluator();
        this.xpath.createDocumentFromString(strXML);

        return strXML;
    }

    public String loadDataToXML(String configURL, String structureNames[]) {
        String strXML = "";
        BufferedReader in = null;
        try {
            try {
                new URL(configURL);
            } catch (MalformedURLException mue) {
                configURL = "file:" + configURL;
            }

            in = new BufferedReader(new InputStreamReader(new URL(configURL).openStream()));

            String line;

            ConfigItem currentItem = null;
            ConfigModule currentModule = null;

            mainLoop:
            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (line.equals("")) {
                    if (currentModule != null) {
                        strXML += currentModule.toXML();
                    }
                    currentItem = null;
                    currentModule = null;
                    continue;
                }

                if (line.startsWith("#")) {
                    continue;   // it is a comment
                }
                String l1 = "";
                String l2 = "";

                int n = line.indexOf(" ");
                int n2 = line.indexOf("=");

                if ((n2 > 0 && n > n2) || (n2 > 0 && n < 0)) {
                    n = n2;
                }

                if (n > 0) {
                    l1 = line.substring(0, n).trim();
                    l2 = line.substring(n + 1).trim();
                } else {
                    l1 = line.trim();
                    l2 = " ";
                }

                if (structureNames != null) {
                    for (int i = 0; i < structureNames.length; i++) {
                        if (l1.toLowerCase().equals(structureNames[i].toLowerCase())) {
                            if (currentModule != null) {
                                strXML += currentModule.toXML();
                            }

                            currentModule = new ConfigModule();
                            currentModule.name = structureNames[i];
                            currentModule.value = l2;

                            continue mainLoop;
                        }
                    }
                }

                currentItem = new ConfigItem();

                if (line.toLowerCase().startsWith("import ") || line.toLowerCase().startsWith("importfile ") || line.toLowerCase().startsWith("importmodule ")) {
                    l2 = Utils.replaceSystemVariables(l2);
                    strXML += loadDataToXML(l2, structureNames);
                } else {
                    currentItem.name = l1;
                    currentItem.value = l2;
                    if (currentModule != null) {
                        currentModule.moduleItems.add(currentItem);
                    } else {
                        strXML += currentItem.toXML();
                    }
                }
            }

            if (currentModule != null) {
                strXML += currentModule.toXML();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.out);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }

        return strXML;
    }

    private static final boolean isNumeric(final String s) {
        final char[] numbers = s.toCharArray();
        for (int x = 0; x < numbers.length; x++) {
            final char c = numbers[x];
            if ((c >= '0') && (c <= '9')) {
                continue;
            }
            return false; // invalid
        }
        return true; // valid
    }

    public String getString(String strXPath) {
        if (this.xpath == null) {
            return "";
        }

        if (!strXPath.startsWith("/")) {
            strXPath = "/properties/" + strXPath;
        }

        return xpath.getString(strXPath.toLowerCase());
    }

    public String getString(String strXPath, int position) {
        return this.getString(strXPath + "[position()=" + (position + 1) + "]/@id");
    }

    public String getString(String parent, int position, String child) {
        if (this.xpath == null) {
            return "";
        }

        String strXPath = parent + "[position()=" + (position + 1) + "]/" + child;

        if (!strXPath.startsWith("/")) {
            strXPath = "/properties/" + strXPath;
        }

        return xpath.getString(strXPath.toLowerCase());
    }

    public int getInteger(String strXPath) {
        return (int) this.getDouble(strXPath);
    }

    public int getInteger(String parent, int position, String child) {
        return (int) this.getDouble(parent, position, child);
    }

    public double getDouble(String strXPath) {
        if (this.xpath == null) {
            return 0;
        }

        if (!strXPath.startsWith("/")) {
            strXPath = "/properties/" + strXPath;
        }

        return xpath.getDouble(strXPath.toLowerCase());
    }

    public double getDouble(String parent, int position, String child) {
        if (this.xpath == null) {
            return 0;
        }

        String strXPath = parent + "[position()=" + (position + 1) + "]/" + child;

        if (!strXPath.startsWith("/")) {
            strXPath = "/properties/" + strXPath;
        }

        return xpath.getDouble(strXPath.toLowerCase());
    }

    public boolean getBoolean(String strXPath) {
        if (this.xpath == null) {
            return false;
        }

        if (!strXPath.startsWith("/")) {
            strXPath = "/properties/" + strXPath.toLowerCase();
        }

        return xpath.getString(strXPath).equalsIgnoreCase("true") || xpath.getString(strXPath).equalsIgnoreCase("yes");
    }

    public boolean getBoolean(String parent, int position, String child) {
        if (this.xpath == null) {
            return false;
        }

        String strXPath = parent + "[position()=" + (position + 1) + "]/" + child;

        if (!strXPath.startsWith("/")) {
            strXPath = "/properties/" + strXPath.toLowerCase();
        }

        return xpath.getString(strXPath).equalsIgnoreCase("true") || xpath.getString(strXPath).equalsIgnoreCase("yes");
    }

    public int getCount(String strXPath) {
        if (this.xpath == null) {
            return 0;
        }

        if (!strXPath.startsWith("/")) {
            strXPath = "/properties/" + strXPath;
        }

        NodeList nodes = xpath.getNodes(strXPath.toLowerCase());

        return nodes.getLength();
    }

    public static void main(String args[]) {
        SimpleProperties gat = new SimpleProperties();

        String strXML = gat.loadData("C:\\obren\\projects\\amico\\myprojects\\papers\\mashing up oil and water\\workspace.txt", "addprocess");
        System.out.println(strXML);

        System.out.println(gat.getString("/properties/title"));
        System.out.println(gat.getString("title"));

        System.out.println(gat.getCount("addprocess"));

        for (int i = 0; i < gat.getCount("addprocess"); i++) {
            String strPrefix = "addprocess[position()=" + (i + 1) + "]/";

            System.out.println(gat.getString(strPrefix + "commandline"));
            System.out.println(gat.getString("addprocess", i, "commandline"));
        }
    }
}
