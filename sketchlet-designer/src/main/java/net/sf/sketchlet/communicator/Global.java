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
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.util.Hashtable;


/**
 * @author obrenovi
 */
public class Global {
    private static final Logger log = Logger.getLogger(Global.class);
    // public static TCPDataReceiver dataReceiverTCP;
    public static UDPServer serverUDP;
    public static TCPServer serverTCP;
    public static Hashtable delayedVariables = new Hashtable();
    public static String workingDirectory;

    public static DocumentBuilderFactory factory;
    public static DocumentBuilder builder;
    public static TransformerFactory tFactory;
    Transformer t;

    static {
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            tFactory = TransformerFactory.newInstance();
        } catch (Exception e) {
            log.error(e);
        }
    }

}
