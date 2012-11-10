/*
 * UDPUtils.java
 *
 * Created on 24 February 2006, 11:39
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.common.net;

import java.net.*;
import java.io.*;

/**
 *
 * @author Omnibook
 */
public class UDPUtils {

    /** Creates a new instance of UDPUtils */
    private UDPUtils() {
    }

    public static void sendPacket(String host, int port, String strData) {
        sendPacket(host, port, strData.getBytes());
    }

    public static void sendPacket(String host, int port, byte[] data) {
        if (host != null) {
            try {
                DatagramSocket socket = new DatagramSocket();
                InetAddress address = InetAddress.getByName(host);

                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                socket.send(packet);
            } catch (Exception e) {
                System.out.println("Host: " + host);
                e.printStackTrace();
            }
        }
    }

    public static String receivePacket(int port) {
        String strPacket = "";
        try {
            strPacket = receivePacket(new DatagramSocket(port));
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return strPacket;
    }

    public static String receivePacket(DatagramSocket socket) {
        String strPacket = "";
        try {
            byte[] buf = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            //System.out.println( "UDP: " + new String( packet.getData() ).trim() );
            strPacket = new String(packet.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return strPacket;
    }
}
