/*
 * CommunicatorInterface.java
 *
 * Created on April 25, 2006, 5:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.net;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Vector;
import net.sf.sketchlet.common.DefaultSettings;


/**
 *
 * @author obrenovi
 */
public class CommunicatorUdpInterface implements CommunicatorInterface {
    
    public Socket communicator;
    public BufferedReader in;
    public PrintWriter out;
    Vector templates = new Vector();
    public boolean connected = false;
    String host = "localhost";
    int port = 3320;
    int receivePort = 3339;
    DatagramSocket socket;
    String localHostName;
    
    public CommunicatorUdpInterface() {
        this(DefaultSettings.getHost(), DefaultSettings.getCommunicatorUDPPort());
    }
    
    public CommunicatorUdpInterface(int receivePort) {
        this(DefaultSettings.getHost(), DefaultSettings.getCommunicatorUDPPort(), receivePort);
    }
    
    public CommunicatorUdpInterface(String communicatorHost, int communicatorPort) {
        if (!communicatorHost.equals("")) {
            this.host = communicatorHost;
        } else {
            this.host = DefaultSettings.getHost();
        }
        
        if (communicatorPort > 0) {
            this.port = communicatorPort;
        } else {
            this.port = DefaultSettings.getCommunicatorUDPPort();
        }
    }
    
    public CommunicatorUdpInterface(String communicatorHost, int communicatorPort, int receivePort) {
        this(communicatorHost, communicatorPort);
        this.receivePort = receivePort;
        
        try {
            this.socket = new DatagramSocket(receivePort);
            this.localHostName = this.socket.getLocalAddress().getHostName();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
    
    public void send(String strCommand) {
        UDPUtils.sendPacket(this.host, this.port, strCommand);
    }
    
    public String sendAndReceive(String strCommand) {
        String result = "";
        this.send(strCommand);
        result = UDPUtils.receivePacket(this.socket);
        
        return result;
    }
    
    public void register(String triggerVariable) {
        this.addTemplate(triggerVariable);
    }

    public void register(String triggerVariable, String template) {
        this.addTemplate(triggerVariable, template);
    }

    public void addTemplate(String triggerVariable) {
        String template = "<%=" + triggerVariable + "%>";
        
        addTemplate(triggerVariable, template);
    }
    
    public void addTemplate(String triggerVariable, String template) {
        String addTemplateCommand = "ADD TEMPLATE " + this.localHostName + " " + this.receivePort;
        addTemplateCommand += " " + triggerVariable + " " + template;
        this.send(addTemplateCommand);
    }
    
    public void addTemplate(String triggerVariables[], String template) {
        String trigger = "";
        for (int i = 0; i < triggerVariables.length; i++) {
            trigger += triggerVariables[i].trim();
            
            if (i < triggerVariables.length - 1) {
                trigger += ",";
            }
        }
        
        String addTemplateCommand = "ADD TEMPLATE " + this.localHostName + " " + this.receivePort;
        addTemplateCommand += " " + trigger + " " + template;
        this.send(addTemplateCommand);
    }
    
    public String updateVariable(String variableName, String variableValue) {
        try {
            String strCommand = "UPDATE " + variableName + " " + URLEncoder.encode(variableValue, "UTF-8");
            this.send(strCommand);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        
        return "";
    }
    
    public String updateVariable(String variableName, String variableValue, String strGroup, String strDescription) {
        try {
            String strCommand = "UPDATE " + variableName + " " + URLEncoder.encode(variableValue, "UTF-8");
            strCommand += " " + URLEncoder.encode(strGroup, "UTF-8");
            strCommand += " " + URLEncoder.encode(strDescription, "UTF-8");
            this.send(strCommand);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        
        return "";
    }
    
    public String deleteVariable(String variableName) {
        String strCommand = "DELETE " + variableName;
        this.send(strCommand);
        
        return "";
    }
    
    public String loadTransformation(String transformationURL) {
        String strCommand = "LOAD TRANSFORMATION " + transformationURL;
        this.send(strCommand);
        
        return "";
    }
    
    public String removeTransformation(String transformationURL) {
        String strCommand = "REMOVE TRANSFORMATION " + transformationURL;
        this.send(strCommand);
        
        return "";
    }
    
    public String readLine() {
        String line = null;
        
        line = UDPUtils.receivePacket(this.socket);
        
        return line;
    }
    
   
    public void register(String[] triggerVariables, String template) {
        this.addTemplate(triggerVariables, template);
    }
    
    public String addVariable(String variableName, String strGroup, String strDescription) {
        String strCommand = "ADDVAR " + variableName + "|" + strGroup + "|" + strDescription;
        send(strCommand);
        
        return "";
    }
}
