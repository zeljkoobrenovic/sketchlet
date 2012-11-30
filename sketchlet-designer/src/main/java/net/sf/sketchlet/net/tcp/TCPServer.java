/*
 * TCPClientConnections.java
 *
 * Created on 26 February 2006, 18:42
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.net.tcp;

import net.sf.sketchlet.common.AcceptConnectionsThread;
import net.sf.sketchlet.common.net.ClientConnectionThread;
import net.sf.sketchlet.common.net.ClientLineProcessingThread;
import net.sf.sketchlet.net.DataReceiver;
import net.sf.sketchlet.net.SketchletNetworkProtocol;
import net.sf.sketchlet.net.Template;
import net.sf.sketchlet.net.TemplateHandler;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;

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

    private TCPServer dataReceiver;

    public AcceptTCPClientConnections(TCPServer dataReceiver, int port) {
        super(port);
        this.dataReceiver = dataReceiver;
    }

    public synchronized void processTemplates(String triggerVariable) {
        synchronized (clients) {
            Iterator iterator = this.clients.iterator();

            while (iterator.hasNext()) {
                TCPClientConnection client = (TCPClientConnection) iterator.next();
                client.getTemplateHandler().processTemplates(triggerVariable);
            }
        }
    }

    public synchronized void processDiffTemplates(String triggerVariable) {
        synchronized (this.clients) {
            Iterator iterator = this.clients.iterator();

            while (iterator.hasNext()) {
                TCPClientConnection client = (TCPClientConnection) iterator.next();
                client.getTemplateHandler().processDiffTemplates(triggerVariable);
            }
        }
    }

    public synchronized void processDiffTemplatesFirstTime(String triggerVariable) {
        synchronized (this.clients) {
            Iterator iterator = this.clients.iterator();

            while (iterator.hasNext()) {
                TCPClientConnection client = (TCPClientConnection) iterator.next();
                client.getTemplateHandler().processDiffTemplatesFirstTime(triggerVariable);
            }
        }
    }

    public synchronized ClientConnectionThread getClientConnectionThreadInstance(Socket socket, Vector clients) {
        return new TCPClientConnection(this.dataReceiver, socket, clients);
    }
}

class TCPClientConnection extends ClientLineProcessingThread {

    private TemplateHandler templateHandler = new TCPTemplateHandler();
    private TCPServer dataReceiver;

    public TCPClientConnection(TCPServer dataReceiver, Socket socket, Vector clients) {
        super(socket, clients, false);
        this.dataReceiver = dataReceiver;
    }

    public void processLine(String line, BufferedReader in, PrintWriter out) throws IOException {
        if (line == null || this.getTemplateHandler() == null || VariablesBlackboard.getInstance() == null) {
            return;
        }

        if (endString.length() > 0) {
            while (line.startsWith(endString)) {
                line = line.substring(endString.length());
            }
            line.replace(endString, "");
        }

        if (line.startsWith("ADD TEMPLATE ")) {
            this.getTemplateHandler().processTemplateCommand(line);
        } else if (line.startsWith("REGISTER ")) {
            this.setEncode(false);
            this.getTemplateHandler().processTemplateCommand(line);
        } else if (line.startsWith("SET ENCODING OFF")) {
            this.setEncode(false);
        } else if (line.startsWith("SET ENCODING ON")) {
            this.setEncode(true);
        } else if (line.startsWith("POPULATE TEMPLATE ")) {
            String template = line.substring(18).trim();
            template = VariablesBlackboard.populateTemplate(template, this.isEncode());
            out.println(template);
            out.flush();
        } else if (line.startsWith("GET ")) {
            String variableList = line.substring(4);
            String values = VariablesBlackboard.getInstance().getVariableValues(variableList);
            out.println(values);
            out.flush();
        } else if (line.startsWith("GETXML ")) {
            String variableList = line.substring(7);
            String values = VariablesBlackboard.getInstance().getVariableValues(variableList, true);
            out.println(values);
            out.flush();
        } else if (line.startsWith("GETALLXML")) {
            String values = VariablesBlackboard.getInstance().getAllVariableValues();
            out.println(values);
            out.flush();
        } else if (line.startsWith("GETXMLFULL")) {
            String values = VariablesBlackboard.getInstance().getAllVariableValuesXml();
            out.println(values);
            out.flush();
        } else if (line.startsWith("SET ENDING 0")) {
            this.endString = "\0";
        } else if (line.startsWith("UPDATE") || line.startsWith("DELETE ")) {
            dataReceiver.updateVariable(line, this.isEncode());
        } else {
            SketchletNetworkProtocol.processCommand(line);
        }
    }

    public TemplateHandler getTemplateHandler() {
        return templateHandler;
    }

    public void setTemplateHandler(TemplateHandler templateHandler) {
        this.templateHandler = templateHandler;
    }

    class TCPTemplateHandler extends TemplateHandler {

        public void sendTemplate(Template template) {
            String populatedTemplate = VariablesBlackboard.populateTemplate(template.getTemplate(), isEncode());
            sendResponse(populatedTemplate);
        }

        public Template createTemplate() {
            return super.createTemplate();
        }
    }
}
