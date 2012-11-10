/*
 * WebServerStarter.java
 *
 * Created on March 17, 2006, 3:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common.net;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.net.*;

//file: WebServerStarter.java
//declare a class wich inherit JFrame
public class WebServerStarter
        extends JFrame {
    //declare some panel, scrollpanel, textarea for gui
    JPanel jPanel1 = new JPanel();
    JScrollPane jScrollPane1 = new JScrollPane();
    JTextArea jTextArea2 = new JTextArea();
    static Integer listen_port = null;
    static String rootDirectory = null;

    public WebServerStarter( int port, String rootDirectory ) {
        new WebServer(listen_port.intValue(), this, rootDirectory);
    }
    
    //basic class constructor
    public WebServerStarter() {
        try {
            jbInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //the JavaAPI entry point
    //where it starts this class if run
    public static void main(String[] args) {
        //start server on port x, default 80
        //use argument to main for what port to start on
        try {
            listen_port = new Integer(args[0]);
            rootDirectory = args[1];
            //catch parse error
        } catch (Exception e) {
            rootDirectory = "/ufs/obrenovi";
            listen_port = new Integer(8012);
        }
        //create an instance of this class
        WebServerStarter webserver = new WebServerStarter();
    }
    
    //set up the user interface
    private void jbInit() throws Exception {
        //oh the pretty colors
        jTextArea2.setBackground(new Color(16, 12, 66));
        jTextArea2.setForeground(new Color(151, 138, 255));
        jTextArea2.setBorder(BorderFactory.createLoweredBevelBorder());
        jTextArea2.setToolTipText("");
        jTextArea2.setEditable(false);
        jTextArea2.setColumns(30);
        jTextArea2.setRows(15);
        //change this to impress your friends
        this.setTitle("Jon\'s fancy prancy webserver");
        //ugly inline listener, it's for handling closing of the window
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                this_windowClosed(e);
            }
        });
        //add the various to the proper containers
        jScrollPane1.getViewport().add(jTextArea2);
        jPanel1.add(jScrollPane1);
        this.getContentPane().add(jPanel1, BorderLayout.EAST);
        
        //tveak the apearance
        this.setVisible(true);
        this.setSize(420, 350);
        this.setResizable(false);
        //make sure it is drawn
        this.validate();
        //create the actual serverstuff,
        //all that is implemented in another class
        new WebServer(listen_port.intValue(), this, rootDirectory);
    }
    
    //exit program when "X" is pressed.
    void this_windowClosed(WindowEvent e) {
        System.exit(1);
    }
    
    //this is a method to get messages from the actual
    //server to the window
    public void send_message_to_window(String s) {
        if (jTextArea2 != null)
            jTextArea2.append(s);
    }
} //class end