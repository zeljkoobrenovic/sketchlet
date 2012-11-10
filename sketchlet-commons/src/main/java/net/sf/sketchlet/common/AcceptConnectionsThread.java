/*
 * AcceptConnectionsThread.java
 *
 * Created on 15 February 2006, 13:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package net.sf.sketchlet.common;
 
import java.io.*;
import java.net.*;
import java.util.Vector;
import javax.swing.*;
import net.sf.sketchlet.common.net.ClientConnectionThread;

/**
 *
 * @author Omnibook
 */
public abstract class AcceptConnectionsThread implements Runnable {
    protected Vector clients = new Vector();
    protected Thread thread = new Thread( this );
    protected int port;
    
    /** Creates a new instance of ConnectionServerThread */
    public AcceptConnectionsThread( int port ) {
        this.port = port;
        this.thread.start();
    }
    
    public void run() {
        ServerSocket serverSocket = null;
        boolean listening = true;

        try {
            serverSocket = new ServerSocket( this.port );

            while (listening)
                this.clients.addElement( this.getClientConnectionThreadInstance( serverSocket.accept(), clients ) );

            serverSocket.close();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "The port " + this.port + " is already in use.\n"
                    + "Another Sketchlet Designer instance may be running.\n"
                    + "Only one instance of Sketchlet Designer can run at a time.\n");
            //System.err.println( "Could not listen on port: " + this.port );
            //e.printStackTrace();
            System.exit(-1);
        }
    }
    
    public abstract ClientConnectionThread getClientConnectionThreadInstance( Socket socket, Vector clients );
}