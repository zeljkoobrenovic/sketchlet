/*
 * GenericAdapterClientLineProcessingThread.java
 *
 * Created on May 1, 2006, 1:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common.net;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author obrenovi
 */
public abstract class GenericAdapterClientLineProcessingThread extends ClientLineProcessingThread {
    protected GenericAdapter adapter;
    protected Thread thread = new Thread( this );
    protected Vector commandTemplates = new Vector();
    
    public GenericAdapterClientLineProcessingThread( GenericAdapter adapter, Socket socket, String host, int port ) {
        super( socket );
        this.host = host;
        this.port = port;
        this.adapter = adapter;
    }
    
    public void addCommandTemplate( String commandTemplate ) {
        this.addCommandTemplate( commandTemplate, 0 );
    }
       
    public void addCommandTemplate( String commandTemplate, int delayMs ) {
        this.commandTemplates.add( new Command( commandTemplate, delayMs ) );
    }

    public void updateVariable( String variableName, String value ) {
        try {
            this.sendResponse( "SET " + variableName + " " + value );
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    public void init() {
        this.onLoad();
        this.registerTemplates();
    }

    public void registerTemplates() {
        this.runCommands( this.commandTemplates );
    }

    public void onLoad() {
        this.runCommands( this.adapter.commands );
    }
    
    protected void runCommands( Vector commands ) {
        if (this.socket == null) return;
        
        try {
            PrintWriter out = new PrintWriter( new OutputStreamWriter( this.socket.getOutputStream() ) );
            
            Iterator iterator = commands.iterator();
            
            while (iterator.hasNext()) {
                Command command = ((Command) iterator.next());
                
                if (command.delayMs > 0) Thread.sleep( command.delayMs );
                
                out.println( command.command.trim() );
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    protected void reconnect() {
        super.reconnect();
        this.init();
    }
}


