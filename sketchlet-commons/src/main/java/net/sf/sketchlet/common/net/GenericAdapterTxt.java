/*
 * GenericAdapter.java
 *
 * Created on May 1, 2006, 1:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import net.sf.sketchlet.common.config.ConfigItem;
import net.sf.sketchlet.common.config.ConfigModule;
import net.sf.sketchlet.common.config.ConfigItemHelper;
import net.sf.sketchlet.common.Utils;

/**
 *
 * @author obrenovi
 */
public class GenericAdapterTxt extends GenericAdapter {

    /** Creates a new instance of GenericAdapter */
    public GenericAdapterTxt() {
    }

    public Vector<ConfigItem> loadData(String configURLs[], String structureNames[]) {
        Vector<ConfigItem> data = new Vector<ConfigItem>();

        for (int i = 0; i < configURLs.length; i++) {
            loadData(configURLs[i], data, structureNames);
        }

        return data;
    }

    public Vector<ConfigItem> loadData(String configURL, String structureNames[]) {
        Vector<ConfigItem> data = new Vector<ConfigItem>();

        loadData(configURL, data, structureNames);

        return data;
    }

    public Vector<ConfigItem> loadData(String configURL, String structureName) {
        Vector<ConfigItem> data = new Vector<ConfigItem>();

        loadData(configURL, data, new String[]{structureName});

        return data;
    }

    public GenericAdapterClientLineProcessingThread getRemoteControl(GenericAdapter adapter, Socket socket, String communicatorHost, int communicatorPort) {
        return null;
    }

    public void loadData(String configURL, Vector<ConfigItem> data, String structureNames[]) {
        try {
            try {
                new URL(configURL);
            } catch (MalformedURLException mue) {
                configURL = "file:" + configURL;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(configURL).openStream()));

            String line;

            ConfigItem currentItem = null;
            ConfigModule currentModule = null;

            String strXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

            mainLoop:
            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (line.equals("")) {
                    currentItem = null;
                    currentModule = null;
                    continue;
                }

                if (line.startsWith("#")) {
                    continue;   // it is a commentar
                }
                String l1 = "";
                String l2 = "";

                int n = line.indexOf(" ");
                int n2 = line.indexOf("=");

                if (n > 0 && n2 > 0) {
                    n = Math.min(n, n2);
                }

                if (n > 0) {
                    l1 = line.substring(0, n).trim();
                    l2 = line.substring(n + 1).trim();
                } else {
                    l1 = line.trim();
                    l2 = " ";
                }

                for (int i = 0; i < structureNames.length; i++) {
                    if (l1.toLowerCase().equals(structureNames[i].toLowerCase())) {
                        currentModule = new ConfigModule();
                        currentModule.name = structureNames[i];
                        currentModule.value = l2;
                        data.add(currentModule);
                        continue mainLoop;
                    }
                }

                currentItem = new ConfigItem();

                if (line.toLowerCase().startsWith("import ") || line.toLowerCase().startsWith("importfile ") || line.toLowerCase().startsWith("importmodule ")) {
                    l2 = Utils.replaceSystemVariables(l2);
                    loadData(l2, data, structureNames);
                } else {
                    currentItem.name = l1;
                    currentItem.value = l2;
                    if (currentModule != null) {
                        currentModule.moduleItems.add(currentItem);
                    } else {
                        data.add(currentItem);
                    }
                }
            }

            Enumeration elements = data.elements();

            while (elements.hasMoreElements()) {

                Object item = elements.nextElement();

                if (!(item instanceof ConfigModule)) {
                    ConfigItem ci = (ConfigItem) item;

                    if (ci.name.toLowerCase().equals("statevariable")) {
                        this.stateVariable = ci.value;
                    } else if (ci.name.toLowerCase().equals("amicohost")) {
                        this.communicatorHost = ci.value;
                    } else if (ci.name.toLowerCase().equals("amicoport")) {
                        this.communicatorPort = Integer.parseInt(ci.value);
                    } else if (ci.name.toLowerCase().equals("amicocommand")) {
                        int delayMs = 0;
                        String value = ci.value;

                        if (isNumeric("" + ci.value.charAt(0))) {
                            int n = ci.value.indexOf(" ");

                            if (n > 0) {
                                delayMs = Integer.parseInt(ci.value.substring(0, n));
                                value = ci.value.substring(n + 1);
                            }
                        }

                        this.commands.add(new Command(value, delayMs));
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.out);
        }
    }

    public void loadData2(String configURL, Vector<ConfigItem> data, String structureNames[]) {
        try {
            try {
                new URL(configURL);
            } catch (MalformedURLException mue) {
                configURL = "file:" + configURL;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(configURL).openStream()));

            String line;

            ConfigItem currentItem = null;
            ConfigModule currentModule = null;

            mainLoop:
            while ((line = in.readLine()) != null) {
                line = line.trim();

                if (line.equals("")) {
                    currentItem = null;
                    currentModule = null;
                    continue;
                }

                if (line.startsWith("#")) {
                    continue;   // it is a commentar
                }
                String l1 = "";
                String l2 = "";

                int n = line.indexOf(" ");

                if (n > 0) {
                    l1 = line.substring(0, n).trim();
                    l2 = line.substring(n + 1).trim();
                } else {
                    l1 = line.trim();
                    l2 = "";
                }

                for (int i = 0; i < structureNames.length; i++) {
                    if (l1.toLowerCase().equals(structureNames[i].toLowerCase())) {
                        currentModule = new ConfigModule();
                        currentModule.name = structureNames[i];
                        currentModule.value = l2;
                        data.add(currentModule);
                        continue mainLoop;
                    }
                }

                currentItem = new ConfigItem();

                if (line.toLowerCase().startsWith("import ") || line.toLowerCase().startsWith("importfile ") || line.toLowerCase().startsWith("importmodule ")) {
                    l2 = Utils.replaceSystemVariables(l2);
                    loadData(l2, data, structureNames);
                } else {
                    currentItem.name = l1;
                    currentItem.value = l2;
                    if (currentModule != null) {
                        currentModule.moduleItems.add(currentItem);
                    } else {
                        data.add(currentItem);
                    }
                }
            }

            Enumeration elements = data.elements();

            while (elements.hasMoreElements()) {

                Object item = elements.nextElement();

                if (!(item instanceof ConfigModule)) {
                    ConfigItem ci = (ConfigItem) item;

                    if (ci.name.toLowerCase().equals("statevariable")) {
                        this.stateVariable = ci.value;
                    } else if (ci.name.toLowerCase().equals("amicohost")) {
                        this.communicatorHost = ci.value;
                    } else if (ci.name.toLowerCase().equals("amicoport")) {
                        this.communicatorPort = Integer.parseInt(ci.value);
                    } else if (ci.name.toLowerCase().equals("amicocommand")) {
                        int delayMs = 0;
                        String value = ci.value;

                        if (isNumeric("" + ci.value.charAt(0))) {
                            int n = ci.value.indexOf(" ");

                            if (n > 0) {
                                delayMs = Integer.parseInt(ci.value.substring(0, n));
                                value = ci.value.substring(n + 1);
                            }
                        }

                        this.commands.add(new Command(value, delayMs));
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace(System.out);
        }
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

    public void loadAdapters() {
    }

    public void loadAdapter(String adapterURL) {
    }

    public static void main(String args[]) {
        GenericAdapterTxt gat = new GenericAdapterTxt();

        Vector<ConfigItem> data = gat.loadData("C:\\obren\\projects\\amico-core\\src\\amico-process-runner\\conf\\processrunner\\config.txt", "addprocess");
        ConfigItemHelper helper = new ConfigItemHelper(data);

        System.out.println(data);

        System.out.println(helper.getString("title"));
        System.out.println(helper.getString("AddProcess", 0, "AutoStart"));
        System.out.println(helper.getString("AddProcess", 0));
    }
}
