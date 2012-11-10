/*
 * ConfigurationData.java
 *
 * Created on 24 February 2006, 17:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.communicator;

import net.sf.sketchlet.common.DefaultSettings;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.script.ScriptPluginProxy;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author Omnibook
 */
public class ConfigurationData {
    private static final Logger log = Logger.getLogger(ConfigurationData.class);

    /**
     * Creates a new instance of ConfigurationData
     */
    private ConfigurationData() {
    }

    public static int tcpPort = 3320;
    public static int udpPort = 3321;
    public static Vector initialVariablesURLs = new Vector();
    public static Vector scriptFiles = new Vector();
    public static String configURL = "file:conf/communicator/config.xml";

    public static void addInitialVariablesURL(String strURL) {
        initialVariablesURLs.add(strURL);
    }

    public static void addScriptFile(String strFile) {
        scriptFiles.add(strFile);
    }

    public static void saveConfiguration() {
        try {
            saveConfiguration(new URL(configURL).getPath());
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static void saveInitVariables() {
        String strFileURL;
        if (initialVariablesURLs.size() == 0) {
            strFileURL = "file:conf/communicator/init-variables.xml";
        } else {
            strFileURL = (String) initialVariablesURLs.get(0);
            initialVariablesURLs.removeAllElements();
        }

        initialVariablesURLs.add(strFileURL);

        try {
            saveInitVariables(new URL(strFileURL).getPath());
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static void saveInitVariables(String strFileName) {
        int n = strFileName.lastIndexOf("/");
        if (n <= 0) {
            n = strFileName.lastIndexOf("\\");
        }
        if (n > 0) {
            new File(strFileName.substring(0, n)).mkdirs();
        }

        try {
            PrintWriter out = new PrintWriter(new FileWriter(strFileName));
            out.println("<?xml version='1.0' encoding='UTF-8'?>");
            out.println("<variables>");

            for (String variableName : DataServer.variablesServer.variablesVector) {
                Variable v = DataServer.variablesServer.getVariable(variableName);

                out.println("    <variable" + " name='" + prepareForXML(v.name) + "' format='" + prepareForXML(v.format) + "' min='" + prepareForXML(v.min) + "' max='" + prepareForXML(v.max) + "'" + " group='" + prepareForXML(v.group) + "'" + " count-filter='" + v.countFilter + "'" + " time-filter='" + v.timeFilterMs + "'" + " description='" + prepareForXML(v.description) + "'" + "><![CDATA[" + v.value + "]]></variable>");
            }

            out.println("</variables>");
            out.flush();
            out.close();

        } catch (Exception e) {
            log.error(e);
        }
    }

    public static String prepareForXML(
            String strText) {
        if (strText != null) {
            return getEsc(strText, false);
        } else {
            return "";
        }

    }

    private static String getEsc(String str, boolean isAttVal) {
        String result = "";
        for (int i = 0; i
                < str.length(); i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '&':
                    result += "&amp;";
                    break;

                case '\'':
                    result += "&#039;";
                    break;

                case '\\':
                    result += "&#092;";
                    break;

                case '<':
                    result += "&lt;";
                    break;

                case '>':
                    result += "&gt;";
                    break;

                case '\"':
                    if (isAttVal) {
                        result += "&quot;";
                    } else {
                        result += '\"';
                    }

                    break;
                case '\n':
                    result += "\\n";

                    break;
                case '\r':
                    result += "\\r";

                    break;
                default:
                    if (ch > '\u007f') {
                        result += "&#";
                        result +=
                                Integer.toString(ch);
                        result +=
                                ';';
                    } else {
                        result += ch;
                    }

            }
        }

        return result;
    }

    public static void saveConfiguration(String strFileName) {
        try {
            int n = strFileName.lastIndexOf("/");
            if (n <= 0) {
                n = strFileName.lastIndexOf("\\");
            }
            if (n > 0) {
                new File(strFileName.substring(0, n)).mkdirs();
            }

            PrintWriter out = new PrintWriter(new FileWriter(strFileName));

            out.println("<?xml version='1.0' encoding='UTF-8'?>");
            out.print("<config");

            if (tcpPort != DefaultSettings.getCommunicatorTCPPort()) {
                out.print(" tcp-port='" + tcpPort + "'");
            }

            if (udpPort != DefaultSettings.getCommunicatorUDPPort()) {
                out.print(" udp-port='" + udpPort + "'");
            }

            out.println(">");
            out.println("<init>");

            Iterator iterator = initialVariablesURLs.iterator();

            while (iterator.hasNext()) {
                String strURL = (String) iterator.next();
                out.println("    <init-variables url='" + strURL + "'/>");
            }

            out.println("</init>");

            out.println("</config>");
            out.flush();
            out.close();

        } catch (Exception e) {
            log.error(e);
        }

        saveInitVariables();
    }

    public static void saveConfiguration(String strFileName, String initFileName) {
        try {
            int n = strFileName.lastIndexOf("/");
            if (n <= 0) {
                n = strFileName.lastIndexOf("\\");
            }
            if (n > 0) {
                new File(strFileName.substring(0, n)).mkdirs();
            }

            PrintWriter out = new PrintWriter(new FileWriter(strFileName));

            out.println("<?xml version='1.0' encoding='UTF-8'?>");
            out.print("<config");

            if (tcpPort != DefaultSettings.getCommunicatorTCPPort()) {
                out.print(" tcp-port='" + tcpPort + "'");
            }

            if (udpPort != DefaultSettings.getCommunicatorUDPPort()) {
                out.print(" udp-port='" + udpPort + "'");
            }

            out.println(">");
            out.println("</config>");
            out.flush();
            out.close();

        } catch (Exception e) {
            log.error(e);
        }

        saveInitVariables(initFileName);
    }

    public static void loadFromURL(String configURL) {
        try {

            ConfigurationData.initialVariablesURLs.removeAllElements();
            ConfigurationData.scriptFiles.removeAllElements();

            DataServer.scripts = new Vector<ScriptPluginProxy>();

            ConfigurationData.configURL = configURL;
            Document docConfig;

            if (configURL != null) {
                try {
                    docConfig = Global.builder.parse(new URL(configURL).openStream());
                } catch (Exception fnfe) {
                    log.error("Configuration file '" + configURL + "' could not be found. Creating an empty document.", fnfe);
                    docConfig = Global.builder.newDocument();
                }
            } else {
                docConfig = Global.builder.newDocument();
            }

            XPath xpath = XPathFactory.newInstance().newXPath();

            String expression;
            String str;
            double number;

            expression = "/config/@tcp-port";
            number = ((Double) xpath.evaluate(expression, docConfig, XPathConstants.NUMBER)).doubleValue();

            if (number > 0.0) {
                ConfigurationData.tcpPort = (int) number;
            } else {
                ConfigurationData.tcpPort = DefaultSettings.getCommunicatorTCPPort();
            }

            expression = "/config/@udp-port";
            number = ((Double) xpath.evaluate(expression, docConfig, XPathConstants.NUMBER)).doubleValue();

            if (number > 0.0) {
                ConfigurationData.udpPort = (int) number;
            } else {
                ConfigurationData.udpPort = DefaultSettings.getCommunicatorUDPPort();
            }

            try {
                String strURL = new File(SketchletContextUtils.sketchletDataDir() + "/init-variables.xml").getAbsoluteFile().toURI().toString();
                ConfigurationData.addInitialVariablesURL(strURL);
            } catch (Exception e) {
                log.error(e);
            }

            try {
                File files[] = new File(Global.workingDirectory + SketchletContextUtils.sketchletDataDir() + "/scripts").listFiles();

                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        if (!files[i].isDirectory() && !files[i].getPath().endsWith(".triggers")) {
                            ConfigurationData.addScriptFile(files[i].getPath());
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e);
            }
        } catch (XPathExpressionException xpee) {
            log.error(xpee);
        }

        ConfigurationData.printConfData();
    }

    private static boolean firstTime = true;

    public static void printConfData() {
        if (firstTime) {
            firstTime = false;
            log.info("TCP Port = " + ConfigurationData.tcpPort);
            log.info("UDP Port = " + ConfigurationData.udpPort);
        }
    }
}
