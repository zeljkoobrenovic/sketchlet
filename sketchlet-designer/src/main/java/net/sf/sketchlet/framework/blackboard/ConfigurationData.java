/*
 * ConfigurationData.java
 *
 * Created on 24 February 2006, 17:33
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.framework.blackboard;

import net.sf.sketchlet.common.DefaultSettings;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.net.NetUtils;
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
import java.util.List;
import java.util.Vector;

/**
 * @author Omnibook
 */
public class ConfigurationData {
    private static final Logger log = Logger.getLogger(ConfigurationData.class);

    private static int tcpPort = 3320;
    private static int udpPort = 3321;
    private static List initialVariablesURLs = new Vector();
    private static List scriptFiles = new Vector();
    private static String configURL = "file:conf/communicator/config.xml";

    private ConfigurationData() {
    }

    public static void addInitialVariablesURL(String strURL) {
        getInitialVariablesURLs().add(strURL);
    }

    public static void addScriptFile(String strFile) {
        getScriptFiles().add(strFile);
    }

    public static void saveConfiguration() {
        try {
            saveConfiguration(new URL(getConfigURL()).getPath());
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static void saveInitVariables() {
        String strFileURL;
        if (getInitialVariablesURLs().size() == 0) {
            strFileURL = "file:conf/communicator/init-variables.xml";
        } else {
            strFileURL = (String) getInitialVariablesURLs().get(0);
            getInitialVariablesURLs().clear();
        }

        getInitialVariablesURLs().add(strFileURL);

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

            for (String variableName : VariablesBlackboard.getInstance().getVariablesList()) {
                Variable v = VariablesBlackboard.getInstance().getVariable(variableName);

                out.println("    <variable" + " name='" + prepareForXML(v.getName()) + "' format='" + prepareForXML(v.getFormat()) + "' min='" + prepareForXML(v.getMin()) + "' max='" + prepareForXML(v.getMax()) + "'" + " group='" + prepareForXML(v.getGroup()) + "'" + " count-filter='" + v.getCountFilter() + "'" + " time-filter='" + v.getTimeFilterMs() + "'" + " description='" + prepareForXML(v.getDescription()) + "'" + "><![CDATA[" + v.getValue() + "]]></variable>");
            }

            out.println("</variables>");
            out.flush();
            out.close();

        } catch (Exception e) {
            log.error(e);
        }
    }

    public static String prepareForXML(String strText) {
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

            if (getTcpPort() != DefaultSettings.getCommunicatorTCPPort()) {
                out.print(" tcp-port='" + getTcpPort() + "'");
            }

            if (getUdpPort() != DefaultSettings.getCommunicatorUDPPort()) {
                out.print(" udp-port='" + getUdpPort() + "'");
            }

            out.println(">");
            out.println("<init>");

            Iterator iterator = getInitialVariablesURLs().iterator();

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

            if (getTcpPort() != DefaultSettings.getCommunicatorTCPPort()) {
                out.print(" tcp-port='" + getTcpPort() + "'");
            }

            if (getUdpPort() != DefaultSettings.getCommunicatorUDPPort()) {
                out.print(" udp-port='" + getUdpPort() + "'");
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

            ConfigurationData.getInitialVariablesURLs().clear();
            ConfigurationData.getScriptFiles().clear();

            VariablesBlackboard.setScripts(new Vector<ScriptPluginProxy>());

            ConfigurationData.setConfigURL(configURL);
            Document docConfig;

            if (configURL != null) {
                try {
                    docConfig = NetUtils.getBuilder().parse(new URL(configURL).openStream());
                } catch (Exception fnfe) {
                    log.info("Configuration file '" + configURL + "' could not be found. Creating an empty document.");
                    docConfig = NetUtils.getBuilder().newDocument();
                }
            } else {
                docConfig = NetUtils.getBuilder().newDocument();
            }

            XPath xpath = XPathFactory.newInstance().newXPath();

            String expression;
            double number;

            expression = "/config/@tcp-port";
            number = ((Double) xpath.evaluate(expression, docConfig, XPathConstants.NUMBER)).doubleValue();

            if (number > 0.0) {
                ConfigurationData.setTcpPort((int) number);
            } else {
                ConfigurationData.setTcpPort(DefaultSettings.getCommunicatorTCPPort());
            }

            expression = "/config/@udp-port";
            number = ((Double) xpath.evaluate(expression, docConfig, XPathConstants.NUMBER)).doubleValue();

            if (number > 0.0) {
                ConfigurationData.setUdpPort((int) number);
            } else {
                ConfigurationData.setUdpPort(DefaultSettings.getCommunicatorUDPPort());
            }

            try {
                String strURL = new File(SketchletContextUtils.sketchletDataDir() + "/init-variables.xml").getAbsoluteFile().toURI().toString();
                ConfigurationData.addInitialVariablesURL(strURL);
            } catch (Exception e) {
                log.error(e);
            }

            try {
                File files[] = new File(NetUtils.getWorkingDirectory() + SketchletContextUtils.sketchletDataDir() + "/scripts").listFiles();

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
            log.info("TCP Port = " + ConfigurationData.getTcpPort());
            log.info("UDP Port = " + ConfigurationData.getUdpPort());
        }
    }

    public static int getTcpPort() {
        return tcpPort;
    }

    public static void setTcpPort(int tcpPort) {
        ConfigurationData.tcpPort = tcpPort;
    }

    public static int getUdpPort() {
        return udpPort;
    }

    public static void setUdpPort(int udpPort) {
        ConfigurationData.udpPort = udpPort;
    }

    public static List getInitialVariablesURLs() {
        return initialVariablesURLs;
    }

    public static void setInitialVariablesURLs(List initialVariablesURLs) {
        ConfigurationData.initialVariablesURLs = initialVariablesURLs;
    }

    public static List getScriptFiles() {
        return scriptFiles;
    }

    public static void setScriptFiles(List scriptFiles) {
        ConfigurationData.scriptFiles = scriptFiles;
    }

    public static String getConfigURL() {
        return configURL;
    }

    public static void setConfigURL(String configURL) {
        ConfigurationData.configURL = configURL;
    }
}
