package net.sf.sketchlet.net;

import net.sf.sketchlet.net.tcp.TCPServer;
import net.sf.sketchlet.net.udp.UDPServer;
import org.apache.log4j.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;


/**
 * @author obrenovi
 */
public class NetUtils {
    private static final Logger log = Logger.getLogger(NetUtils.class);
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
        NetUtils.serverUDP = serverUDP;
    }

    public static void setServerTCP(TCPServer serverTCP) {
        NetUtils.serverTCP = serverTCP;
    }

    public static void setWorkingDirectory(String workingDirectory) {
        NetUtils.workingDirectory = workingDirectory;
    }
}
