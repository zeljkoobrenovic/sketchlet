/*
 * TCPConnectionUtils.java
 *
 * Created on 16 February 2006, 19:03
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package net.sf.sketchlet.common.net;

import java.io.*;
import java.net.*;

/**
 *
 * @author Omnibook
 */
public class TCPConnectionUtils {
    
    /** Creates a new instance of TCPConnectionUtils */
    public TCPConnectionUtils() {
    }
    
    public static void sendData( String host, int port, String command ) {
        try {
            Socket socket = new Socket( host, port );

            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            PrintWriter out = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ) );

            out.println( command );
            out.flush();
            out.close();
            socket.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
    }
    
    public static String getData( String host, int port, String command ) {
        String response = null;
        
        try {
            Socket socket = new Socket( host, port );

            BufferedReader in = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
            PrintWriter out = new PrintWriter( new OutputStreamWriter( socket.getOutputStream() ) );

            out.println( command );
            out.flush();
            response = in.readLine();
            out.close();
            socket.close();
        } catch (Exception ioe) {
            ioe.printStackTrace();
        }
        
        return response;
    }
}
