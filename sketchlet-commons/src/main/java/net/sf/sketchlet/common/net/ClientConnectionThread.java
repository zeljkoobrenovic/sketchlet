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

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author  Omnibook
 */
public abstract class ClientConnectionThread implements Runnable {
    protected Socket socket;
    protected String host;
    protected int port;
    protected Vector connections;
    protected Thread thread = new Thread( this );
    
    protected static int counter = 0;
    protected int id;
    /** Creates a new instance of ClientConnectionThread */
    public ClientConnectionThread( Socket socket, Vector connections ) {
        this.id = ++ClientConnectionThread.counter;
        this.socket = socket;
        if (socket != null) {
            this.host = socket.getInetAddress().getHostAddress();
            this.port = socket.getPort();
        }
        this.connections = connections;
        this.thread.start();
    }
    
    public void removeFromConnectionsPool() {
        if (this.connections != null) {
            this.onDisconnect();
            this.connections.remove( this );
        }
    }
    
    public void onDisconnect() {
    }
}
