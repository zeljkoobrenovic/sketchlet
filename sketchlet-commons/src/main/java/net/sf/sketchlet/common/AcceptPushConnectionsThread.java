/*
 * AcceptPushConnectionsThread.java
 *
 * Created on 17 February 2006, 16:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.common;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

/**
 *
 * @author Omnibook
 */
public class AcceptPushConnectionsThread implements Runnable {

    private Vector clients = new Vector();
    private Thread thread = new Thread(this);
    private int port;

    /** Creates a new instance of ConnectionServerThread */
    public AcceptPushConnectionsThread(int port) {
        this.port = port;
        this.thread.start();
    }

    public void run() {
        ServerSocket serverSocket = null;
        boolean listening = true;

        try {
            serverSocket = new ServerSocket(this.port);

            while (listening) {
                this.clients.addElement(serverSocket.accept());
            }

            serverSocket.close();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "The port " + this.port + " is already in use.\n"
                    + "Another AMICO instance may be running.\n"
                    + "Only one instance of AMICO can run at a time.\n");
            //System.err.println( "Could not listen on port: " + this.port );
            //e.printStackTrace();
            System.exit(-1);
        }
    }

    public void push(String command) {
        Iterator iterator = this.clients.iterator();
        while (iterator.hasNext()) {
            try {
                Socket client = (Socket) iterator.next();
                PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                out.println(command);
                out.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

