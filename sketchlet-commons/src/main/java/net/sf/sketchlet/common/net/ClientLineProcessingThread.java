/*
 * ClientConnectionThread.java
 *
 * Created on 15 February 2006, 13:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.common.net;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author Omnibook
 */
public abstract class ClientLineProcessingThread extends ClientConnectionThread {
    private static final Logger log = Logger.getLogger(ClientLineProcessingThread.class);
    protected boolean bReconnect = true;
    protected BufferedReader in;
    protected PrintWriter out;
    protected String endString = "";
    Vector onDisconnectCommands = new Vector();
    public boolean encode = true;

    public ClientLineProcessingThread(Socket socket) {
        this(socket, null);
    }

    public ClientLineProcessingThread(Socket socket, boolean bReconnect) {
        this(socket, null, bReconnect);
    }

    public ClientLineProcessingThread(Socket socket, Vector connections) {
        this(socket, connections, true);
    }

    public ClientLineProcessingThread(Socket socket, Vector connections, boolean bReconnect) {
        super(socket, connections);
        this.bReconnect = bReconnect;
    }

    public void sendResponse(String response) {
        while (this.out == null) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }

        this.out.println(response + endString);
        this.out.flush();
    }

    public void run() {
        while (true) {
            if (this.socket != null) {
                try {
                    this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                    this.out = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));

                    String line;
                    while ((line = in.readLine()) != null) {
                        this.processLine(line, in, out);
                        // Thread.sleep(10);
                    }
                } catch (Exception ioe) {
                }
            }

            if (this.bReconnect) {
                this.reconnect();
            } else {
                // remove the thread from the thread pool, and end this thread
                if (this.connections != null) {
                    this.onDisconnect();
                    this.connections.remove(this);
                }
                break;
            }
        }
    }

    protected void reconnect() {
        while (true) {
            try {
                this.socket = new Socket(this.host, this.port);
                log.info("ClientLineProcessingThread: Connected to the communicator at " + this.host + ":" + this.port);
                break;
            } catch (Exception e) {
                log.info("ClientLineProcessingThread: I could not connect to the communicator. I'll try again in one second...");
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
            }
        }
    }

    public abstract void processLine(String line, BufferedReader in, PrintWriter out) throws IOException;

    public void addDisconnectCommand(String command) {
        this.onDisconnectCommands.add(command);
    }

    public void onDisconnect() {
        Iterator iterator = this.onDisconnectCommands.iterator();

        while (iterator.hasNext()) {
            String command = (String) iterator.next();
            try {
                this.processLine(command, null, null);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }
}
