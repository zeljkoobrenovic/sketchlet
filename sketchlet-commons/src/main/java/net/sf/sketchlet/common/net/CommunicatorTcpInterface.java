/*
 * CommunicatorInterface.java
 *
 * Created on April 25, 2006, 5:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.net;

import net.sf.sketchlet.common.DefaultSettings;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Vector;


/**
 *
 * @author obrenovi
 */
public class CommunicatorTcpInterface implements CommunicatorInterface {
    private static final Logger log = Logger.getLogger(CommunicatorTcpInterface.class);
    private Socket communicator;
    private BufferedReader in;
    private PrintWriter out;
    Vector templates = new Vector();
    private boolean connected = false;
    String host = "localhost";
    int port = 3320;

    public CommunicatorTcpInterface() {
        this(DefaultSettings.getHost(), DefaultSettings.getCommunicatorTCPPort());
    }

    public CommunicatorTcpInterface(boolean retryConnect) {
        this(DefaultSettings.getHost(), DefaultSettings.getCommunicatorTCPPort(), retryConnect);
    }

    public CommunicatorTcpInterface(String communicatorHost, int communicatorPort, boolean retryConnect) {
        if (retryConnect) { // if true, will retry to connect if failed, and will do it until connection is established
            this.connectLoop(communicatorPort, communicatorHost);
        } else {    // try to connect just once
            this.connect(communicatorHost, communicatorPort);
        }
    }

    public CommunicatorTcpInterface(String communicatorHost, int communicatorPort) {
        this.connectLoop(communicatorPort, communicatorHost);
    }

    private void connectLoop(final int communicatorPort, final String communicatorHost) {
        while (true) {
            if (this.connect(communicatorHost, communicatorPort)) {
                break;
            } else {
                log.info("CommunicatorTcpInterface: I could not connect to the communicator (" + communicatorHost + ":" + communicatorPort + "). I'll try again in one second...");

                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                }
            }
        }
    }

    public boolean connect(String communicatorHost, int communicatorPort) {
        if (!communicatorHost.equals("")) {
            this.host = communicatorHost;

        } else {
            this.host = DefaultSettings.getHost();


        }
        if (communicatorPort > 0) {
            this.port = communicatorPort;

        } else {
            this.port = DefaultSettings.getCommunicatorTCPPort();


        }
        return this.connect();
    }

    public boolean connect() {
        if (this.isConnected()) {
            return true;


        }
        try {
            this.setCommunicator(new Socket(this.host, this.port));
            this.getCommunicator().setTcpNoDelay(true);
            this.setIn(new BufferedReader(new InputStreamReader(this.getCommunicator().getInputStream())));
            this.setOut(new PrintWriter(new OutputStreamWriter(this.getCommunicator().getOutputStream())));
            log.info("CommunicatorTcpInterface: Connected to the communicator at " + this.host + ":" + this.port);

            Iterator iterator = this.templates.iterator();
            while (iterator.hasNext()) {
                String template = (String) iterator.next();
                this.getOut().println(template);
                this.getOut().flush();
            }
        } catch (Exception e) {
            this.setConnected(false);
            return false;
        }

        this.setConnected(true);
        return true;
    }

    public void send(String strCommand) {
        if (!this.connect()) {
            return;
        }
        try {
            getOut().println(strCommand);
            getOut().flush();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }
    }

    public String sendAndReceive(String strCommand) {
        if (!this.connect()) {
            return "";
        }
        String result = "";
        try {
            getOut().println(strCommand);
            getOut().flush();
            result = getIn().readLine();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return result;
    }

    public void addTemplate(String triggerVariable) {
        String template = "<%=" + triggerVariable + "%>";

        addTemplate(triggerVariable, template);
    }

    public void addTemplate(String triggerVariable, String template) {
        String addTemplateCommand = "ADD TEMPLATE " + triggerVariable + " " + template;
        this.templates.add(addTemplateCommand);

        if (this.isConnected()) {
            this.send(addTemplateCommand);
        } else {    // try to connect, and templates will be send after connection is established;
            this.connect();
        }
    }

    public void addTemplate(String triggerVariables[], String template) {
        if (!this.connect()) {
            return;


        }
        String trigger = "";
        for (int i = 0; i < triggerVariables.length; i++) {
            trigger += triggerVariables[i].trim();

            if (i < triggerVariables.length - 1) {
                trigger += ",";

            }
        }

        String addTemplateCommand = "ADD TEMPLATE " + trigger + " " + template;
        this.templates.add(addTemplateCommand);

        if (this.isConnected()) {
            this.send(addTemplateCommand);
        } else {    // try to connect, and templates will be send after connection is established;
            this.connect();
        }
    }

    public String updateVariable(String variableName, String variableValue) {
        if (!this.connect() || variableName.trim().equals("")) {
            return "";


        }
        try {
            String strCommand = "UPDATE " + variableName + " " + URLEncoder.encode(variableValue, "UTF-8");
            getOut().println(strCommand);
            getOut().flush();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return "";
    }

    public String set(String variableName, String variableValue) {
        if (!this.connect() || variableName.trim().equals("")) {
            return "";


        }
        try {
            String strCommand = "SET " + variableName + " " + variableValue;
            getOut().println(strCommand);
            getOut().flush();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return "";
    }

    public String updateVariable(String variableName, String variableValue, String strGroup, String strDescription) {
        if (!this.connect() || variableName.trim().equals("")) {
            return "";
        }
        try {
            String strCommand = "UPDATE " + variableName + " " + URLEncoder.encode(variableValue, "UTF-8");
            strCommand += " " + URLEncoder.encode(strGroup, "UTF-8");
            strCommand += " " + URLEncoder.encode(strDescription, "UTF-8");

            getOut().println(strCommand);
            getOut().flush();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return "";
    }

    public String updateVariableIfEmpty(String variableName, String variableValue) {
        String value = this.get(variableName);
        if (!value.equals("")) {
            return value;

        } else {
            return updateVariable(variableName, variableValue);

        }
    }

    public String updateVariableIfEmpty(String variableName, String variableValue, String strGroup, String strDescription) {
        String value = this.get(variableName);
        if (!value.equals("")) {
            return value;

        } else {
            return updateVariable(variableName, variableValue, strGroup, strDescription);

        }
    }

    public String deleteVariable(String variableName) {
        if (!this.connect()) {
            return "";


        }
        try {
            String strCommand = "DELETE " + variableName;
            getOut().println(strCommand);
            getOut().flush();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return "";
    }

    public String loadTransformation(String transformationURL) {
        if (!this.connect()) {
            return "";


        }
        try {
            String strCommand = "LOAD TRANSFORMATION " + transformationURL;
            getOut().println(strCommand);
            getOut().flush();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return "";
    }

    public String removeTransformation(String transformationURL) {
        if (!this.connect()) {
            return "";


        }
        try {
            String strCommand = "REMOVE TRANSFORMATION " + transformationURL;
            getOut().println(strCommand);
            getOut().flush();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return "";
    }

    public String populateTemplate(String template) {
        if (!this.connect()) {
            return "";


        }
        String result = "";
        try {
            String strCommand = "POPULATE TEMPLATE " + template;
            getOut().println(strCommand);
            getOut().flush();
            result = getIn().readLine();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return result;
    }

    public String get(String listOfVariables) {
        if (!this.connect()) {
            return "";
        }
        String result = "";
        try {
            String strCommand = "GET " + listOfVariables;
            getOut().println(strCommand);
            getOut().flush();
            result = getIn().readLine();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return result;
    }

    public String getXml(String listOfVariables) {
        if (!this.connect()) {
            return "";


        }
        String result = "";
        try {
            String strCommand = "GETXML " + listOfVariables;
            getOut().println(strCommand);
            getOut().flush();
            result = getIn().readLine();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return result;
    }

    public String getAllXml(String listOfVariables) {
        if (!this.connect()) {
            return "";


        }
        String result = "";
        try {
            String strCommand = "GETALLXML " + listOfVariables;
            getOut().println(strCommand);
            getOut().flush();
            result = getIn().readLine();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return result;
    }

    public String readLine() {
        if (!this.connect()) {
            return null;
        }
        String line = null;

        try {
            line = this.getIn().readLine();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        return line;
    }

    public void register(String triggerVariable) {
        String template = "<%=" + triggerVariable + "%>";

        register(triggerVariable, template);
    }

    public void register(String triggerVariable, String template) {
        String addTemplateCommand = "REGISTER " + triggerVariable + " " + template;
        this.templates.add(addTemplateCommand);

        if (this.isConnected()) {
            this.send(addTemplateCommand);
        } else {    // try to connect, and templates will be send after connection is established;
            this.connect();
        }
    }

    public void register(String[] triggerVariables, String template) {
        if (!this.connect()) {
            return;


        }
        String trigger = "";
        for (int i = 0; i < triggerVariables.length; i++) {
            trigger += triggerVariables[i].trim();

            if (i < triggerVariables.length - 1) {
                trigger += ",";

            }
        }

        String addTemplateCommand = "REGISTER " + trigger + " " + template;
        this.templates.add(addTemplateCommand);

        if (this.isConnected()) {
            this.send(addTemplateCommand);
        } else {    // try to connect, and templates will be send after connection is established;
            this.connect();
        }
    }

    public String addVariable(String variableName, String strGroup, String strDescription) {
        if (!this.connect() || variableName.trim().equals("")) {
            return "";
        }
        try {
            String strCommand = "ADDVAR " + variableName + "|" + strGroup + "|" + strDescription;
            getOut().println(strCommand);
            getOut().flush();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return "";
    }

    public String addVariable(String variableName, String strGroup, String strDescription, String initValue) {
        if (!this.connect() || variableName.trim().equals("")) {
            return "";
        }
        try {
            String strCommand = "ADDVAR " + variableName + "|" + strGroup + "|" + strDescription;
            getOut().println(strCommand);
            getOut().flush();
            Thread.sleep(50);
            strCommand = "SET " + variableName + " " + initValue;
            getOut().println(strCommand);
            getOut().flush();
        } catch (Exception e) {
            this.setConnected(false);
            e.printStackTrace(System.out);
        }

        return "";
    }

    public Socket getCommunicator() {
        return communicator;
    }

    public void setCommunicator(Socket communicator) {
        this.communicator = communicator;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
