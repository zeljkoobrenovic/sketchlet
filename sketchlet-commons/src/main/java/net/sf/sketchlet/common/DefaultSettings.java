/*
 * DefaultSettings.java
 *
 * Created on November 15, 2006, 3:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.common;

/**
 *
 * @author obrenovi
 */
public class DefaultSettings {

    private DefaultSettings() {
    }
    static String sketchletDesignerRoot = null;
    static String sketchletDesignerHost = "localhost";
    static int sketchletDesignerPortBase = 3300;
    static int numOfTransformationSteps = 4;
    static String httpUserAgent = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7.8) Gecko/20050511";

    /** Creates a new instance of DefaultSettings */
    static {
        /*DefaultSettings.sketchletDesignerRoot = System.getenv("AMICO_HOME");
        if (DefaultSettings.sketchletDesignerRoot == null || DefaultSettings.sketchletDesignerRoot.isEmpty()) {
            DefaultSettings.sketchletDesignerRoot = System.getenv("SKETCHLET_HOME");
            if (DefaultSettings.sketchletDesignerRoot == null || DefaultSettings.sketchletDesignerRoot.isEmpty()) {
            }
        }
        if (DefaultSettings.sketchletDesignerRoot != null && (!DefaultSettings.sketchletDesignerRoot.endsWith("\\") || !DefaultSettings.sketchletDesignerRoot.endsWith("/"))) {
            DefaultSettings.sketchletDesignerRoot += "/";
        }*/

        DefaultSettings.loadDefaultSettings();
    }

    private static void loadDefaultSettings() {
        if (DefaultSettings.sketchletDesignerRoot != null) {
            XPathEvaluator xpath = new XPathEvaluator();
            xpath.createDocumentFromFile("file:" + DefaultSettings.sketchletDesignerRoot + "conf/default_settings.xml");

            if (xpath.document != null) {
                String host = xpath.getString("/amico-default-settings/variable[@name='AMICO_HOST']");
                int portBase = xpath.getInteger("/amico-default-settings/variable[@name='AMICO_PORT_BASE']");
                int numOfTransSteps = xpath.getInteger("/amico-default-settings/variable[@name='NUMBER_OF_TRANSFORMATION_STEPS']");
                String httpUserAgent = xpath.getString("/amico-default-settings/variable[@name='HTTP_USER_AGENT']");

                if (!host.equals("")) {
                    DefaultSettings.sketchletDesignerHost = host;
                }

                if (portBase > 0) {
                    DefaultSettings.sketchletDesignerPortBase = portBase;
                }

                if (numOfTransSteps > 0) {
                    DefaultSettings.numOfTransformationSteps = numOfTransSteps;
                }

                if (!httpUserAgent.equals("")) {
                    DefaultSettings.httpUserAgent = httpUserAgent;
                }
            }
        }
    }

    public static String getHTTPUserAgent() {
        return DefaultSettings.httpUserAgent;
    }

    public static int getPortBase() {
        return DefaultSettings.sketchletDesignerPortBase;
    }

    public static String getHost() {
        return DefaultSettings.sketchletDesignerHost;
    }

    public static int getNumberOfTransformationSteps() {
        return DefaultSettings.numOfTransformationSteps;
    }

    public static int getCommunicatorTCPPort() {
        return DefaultSettings.sketchletDesignerPortBase + 20;
    }

    public static int getCommunicatorUDPPort() {
        return DefaultSettings.sketchletDesignerPortBase + 21;
    }

    /*
    public int getCommunicatorTCPUpdateOnlyPort() {
    return DefaultSettings.sketchletDesignerPortBase + 10;
    }
    public int getCommunicatorUDPUpdateOnlyPort() {
    return DefaultSettings.sketchletDesignerPortBase + 11;
    }
     */
    public static int getOSCServerPort() {
        return DefaultSettings.sketchletDesignerPortBase + 50;
    }

    public static int getTUIOServerPort() {
        return 3333;
    }

    public static int getVNCServerPort() {
        return 5950;
    }

    public static int getHTTPServerPort() {
        return DefaultSettings.sketchletDesignerPortBase + 80;
    }

    public static int getXmlRpcServerPort() {
        return DefaultSettings.sketchletDesignerPortBase + 88;
    }
}
