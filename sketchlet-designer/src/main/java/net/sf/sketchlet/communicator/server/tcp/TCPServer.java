/*
 * TCPClientConnections.java
 *
 * Created on 26 February 2006, 18:42
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.communicator.server.tcp;

import net.sf.sketchlet.common.AcceptConnectionsThread;
import net.sf.sketchlet.common.net.ClientConnectionThread;
import net.sf.sketchlet.common.net.ClientLineProcessingThread;
import net.sf.sketchlet.communicator.server.DataReceiver;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.StandardNetInterfaces;
import net.sf.sketchlet.communicator.server.Template;
import net.sf.sketchlet.communicator.server.TemplateHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author Omnibook
 */
public class TCPServer extends DataReceiver {

    protected AcceptTCPClientConnections acceptTCPClientConnections;

    /**
     * Creates a new instance of DataQueryServer
     */
    public TCPServer(int port) {
        this.acceptTCPClientConnections = new AcceptTCPClientConnections(this, port);
    }

    public void processTemplates(String updatedVariables[], String diffVariables[]) {
        this.processTemplates(updatedVariables);
        this.processDiffTemplates(diffVariables);
        this.processDiffTemplatesFirstTime(updatedVariables);
    }

    private void processTemplates(String triggerVariables[]) {
        if (triggerVariables != null) {
            for (int i = 0; i < triggerVariables.length; i++) {
                this.processTemplates(triggerVariables[i]);
            }
        }
    }

    private void processDiffTemplates(String triggerVariables[]) {
        if (triggerVariables != null) {
            for (int i = 0; i < triggerVariables.length; i++) {
                this.processDiffTemplates(triggerVariables[i]);
            }
        }
    }

    private void processDiffTemplatesFirstTime(String triggerVariables[]) {
        if (triggerVariables != null) {
            for (int i = 0; i < triggerVariables.length; i++) {
                this.processDiffTemplatesFirstTime(triggerVariables[i]);
            }
        }
    }

    private void processTemplates(String triggerVariable) {
        this.acceptTCPClientConnections.processTemplates(triggerVariable);
    }

    private void processDiffTemplates(String triggerVariable) {
        this.acceptTCPClientConnections.processDiffTemplates(triggerVariable);
    }

    private void processDiffTemplatesFirstTime(String triggerVariable) {
        this.acceptTCPClientConnections.processDiffTemplatesFirstTime(triggerVariable);
    }
}

class AcceptTCPClientConnections extends AcceptConnectionsThread {

    TCPServer dataReceiver;

    public AcceptTCPClientConnections(TCPServer dataReceiver, int port) {
        super(port);
        this.dataReceiver = dataReceiver;
    }

    public synchronized void processTemplates(String triggerVariable) {
        synchronized (clients) {
            Iterator iterator = this.clients.iterator();

            while (iterator.hasNext()) {
                TCPClientConnection client = (TCPClientConnection) iterator.next();
                client.templateHandler.processTemplates(triggerVariable);
            }
        }
    }

    public synchronized void processDiffTemplates(String triggerVariable) {
        synchronized (this.clients) {
            Iterator iterator = this.clients.iterator();

            while (iterator.hasNext()) {
                TCPClientConnection client = (TCPClientConnection) iterator.next();
                client.templateHandler.processDiffTemplates(triggerVariable);
            }
        }
    }

    public synchronized void processDiffTemplatesFirstTime(String triggerVariable) {
        synchronized (this.clients) {
            Iterator iterator = this.clients.iterator();

            while (iterator.hasNext()) {
                TCPClientConnection client = (TCPClientConnection) iterator.next();
                client.templateHandler.processDiffTemplatesFirstTime(triggerVariable);
            }
        }
    }

    public synchronized ClientConnectionThread getClientConnectionThreadInstance(Socket socket, Vector clients) {
        return new TCPClientConnection(this.dataReceiver, socket, clients);
    }
}

class TCPClientConnection extends ClientLineProcessingThread {

    public TemplateHandler templateHandler = new TCPTemplateHandler();
    TCPServer dataReceiver;

    public TCPClientConnection(TCPServer dataReceiver, Socket socket, Vector clients) {
        super(socket, clients, false);
        this.dataReceiver = dataReceiver;
    }

    public void processLine(String line, BufferedReader in, PrintWriter out) throws IOException {
        if (line == null || this.templateHandler == null || DataServer.variablesServer == null) {
            return;
        }

        if (endString.length() > 0) {
            while (line.startsWith(endString)) {
                line = line.substring(endString.length());
            }
            line.replace(endString, "");
        }

        if (line.startsWith("ADD TEMPLATE ")) {
            this.templateHandler.processTemplateCommand(line);
        } else if (line.startsWith("REGISTER ")) {
            this.encode = false;
            this.templateHandler.processTemplateCommand(line);
        } else if (line.startsWith("SET ENCODING OFF")) {
            this.encode = false;
        } else if (line.startsWith("SET ENCODING ON")) {
            this.encode = true;
        } else if (line.startsWith("POPULATE TEMPLATE ")) {
            String template = line.substring(18).trim();
            template = DataServer.populateTemplate(template, this.encode);
            out.println(template);
            out.flush();
        } else if (line.startsWith("GET ")) {
            String variableList = line.substring(4);
            String values = DataServer.variablesServer.getVariableValues(variableList);
            out.println(values);
            out.flush();
        } else if (line.startsWith("GETXML ")) {
            String variableList = line.substring(7);
            String values = DataServer.variablesServer.getVariableValues(variableList, true);
            out.println(values);
            out.flush();
        } else if (line.startsWith("GETALLXML")) {
            String values = DataServer.variablesServer.getAllVariableValues();
            out.println(values);
            out.flush();
        } else if (line.startsWith("GETXMLFULL")) {
            String values = DataServer.variablesServer.getAllVariableValuesXml();
            out.println(values);
            out.flush();
        } else if (line.startsWith("SET ENDING 0")) {
            this.endString = "\0";
        } else if (line.startsWith("UPDATE") || line.startsWith("DELETE ")) {
            dataReceiver.updateVariable(line, this.encode);
        } else {
            StandardNetInterfaces.processCommand(line);
        }
    }

    class TCPTemplateHandler extends TemplateHandler {

        public void sendTemplate(Template template) {
            String populatedTemplate = DataServer.populateTemplate(template.template, encode);
            sendResponse(populatedTemplate);
        }

        public Template createTemplate() {
            return super.createTemplate();
        }
    }
}
