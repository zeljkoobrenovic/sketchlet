/*
 * Global.java
 *
 * Created on April 26, 2006, 5:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.communicator;

import net.sf.sketchlet.communicator.server.tcp.TCPServer;
import net.sf.sketchlet.communicator.server.udp.UDPServer;
import org.apache.log4j.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;


/**
 * @author obrenovi
 */
public class Global {
    private static final Logger log = Logger.getLogger(Global.class);
    private static UDPServer serverUDP;
    private static TCPServer serverTCP;
    private static String workingDirectory;

    private static DocumentBuilderFactory factory;
    private static DocumentBuilder builder;
    private static TransformerFactory tFactory;

    static {
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = getFactory().newDocumentBuilder();
            tFactory = TransformerFactory.newInstance();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static UDPServer getServerUDP() {
        return serverUDP;
    }

    public static TCPServer getServerTCP() {
        return serverTCP;
    }

    public static String getWorkingDirectory() {
        return workingDirectory;
    }

    public static DocumentBuilderFactory getFactory() {
        return factory;
    }

    public static DocumentBuilder getBuilder() {
        return builder;
    }

    public static TransformerFactory gettFactory() {
        return tFactory;
    }

    public static void setServerUDP(UDPServer serverUDP) {
        Global.serverUDP = serverUDP;
    }

    public static void setServerTCP(TCPServer serverTCP) {
        Global.serverTCP = serverTCP;
    }

    public static void setWorkingDirectory(String workingDirectory) {
        Global.workingDirectory = workingDirectory;
    }
}
