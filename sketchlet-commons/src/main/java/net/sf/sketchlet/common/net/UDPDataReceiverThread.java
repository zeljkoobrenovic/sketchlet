/*
 * UDPDataReceiverThread.java
 *
 * Created on 24 February 2006, 11:36
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.common.net;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Omnibook
 */
public abstract class UDPDataReceiverThread implements Runnable {

    protected DatagramSocket socket = null;
    protected boolean receiveData = true;
    Thread thread = new Thread(this);

    public UDPDataReceiverThread(int port) {
        try {
            socket = new DatagramSocket(port);
            this.thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (receiveData) {
            try {
                byte[] buf = new byte[2048];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                getPacketProcessor().processPacket(packet.getData());

                // Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        socket.close();
    }

    public abstract ProcessPacket getPacketProcessor();
}
