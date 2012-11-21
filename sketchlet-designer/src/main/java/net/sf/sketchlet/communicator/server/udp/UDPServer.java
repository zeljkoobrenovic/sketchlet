/*
 * UDPDataReceiver.java
 *
 * Created on 24 February 2006, 11:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.communicator.server.udp;

import net.sf.sketchlet.common.net.ProcessPacket;
import net.sf.sketchlet.common.net.UDPDataReceiverThread;
import net.sf.sketchlet.communicator.server.DataReceiver;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.StandardNetInterfaces;
import net.sf.sketchlet.communicator.server.Template;
import net.sf.sketchlet.communicator.server.TemplateHandler;
import org.apache.log4j.Logger;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Omnibook
 */
public class UDPServer extends DataReceiver {

    UDPCommandDataReceiverThread receiveUpdatePacketThread;

    public UDPServer(int port) {
        receiveUpdatePacketThread = new UDPCommandDataReceiverThread(this, port);
    }

    public void processUDPTemplates(String updatedVariables[], String diffVariables[]) {
        this.receiveUpdatePacketThread.templateHandler.processTemplates(updatedVariables);
        this.receiveUpdatePacketThread.templateHandler.processDiffTemplates(diffVariables);
        this.receiveUpdatePacketThread.templateHandler.processDiffTemplatesFirstTime(updatedVariables);
    }
}

class UDPCommandDataReceiverThread extends UDPDataReceiverThread {
    private static final Logger log = Logger.getLogger(UDPCommandDataReceiverThread.class);

    UDPServer dataReceiver;
    UDPTemplateHandler templateHandler = new UDPTemplateHandler();
    public boolean encode = true;

    public UDPCommandDataReceiverThread(UDPServer dataReceiver, int port) {
        super(port);
        this.dataReceiver = dataReceiver;
    }

    public ProcessPacket getPacketProcessor() {
        return new ProcessPacket() {

            public void processPacket(byte[] data) {
                String command = new String(data).trim();
                if (command.startsWith("ADD TEMPLATE")) {
                    try {
                        templateHandler.removeUDPTemplates(command.substring(13));
                        templateHandler.processTemplateCommand(command);
                    } catch (Exception e) {
                        log.error(e);
                    }
                } else if (command.startsWith("REGISTER ")) {
                    try {
                        UDPTemplate.encode = false;
                        templateHandler.removeUDPTemplates(command.substring(9));
                        templateHandler.processTemplateCommand(command);
                    } catch (Exception e) {
                        log.error(e);
                    }
                } else if (command.startsWith("SET ENCODING OFF")) {
                    UDPTemplate.encode = false;
                } else if (command.startsWith("SET ENCODING ON")) {
                    UDPTemplate.encode = true;
                } else if (command.startsWith("REMOVE TEMPLATE ")) {
                    templateHandler.removeUDPTemplates(command.substring(16));
                } else if (command.startsWith("UNREGISTER ")) {
                    templateHandler.removeUDPTemplates(command.substring(11));
                } else if (command.startsWith("UPDATE") || command.startsWith("DELETE ")) {
                    dataReceiver.updateVariable(command, encode);
                } else {
                    if (DataServer.isPaused()) {
                        return;
                    }
                    StandardNetInterfaces.processCommand(command);
                }
            }
        };
    }
}

class UDPTemplateHandler extends TemplateHandler {
    private static final Logger log = Logger.getLogger(UDPTemplateHandler.class);

    String currentHost;
    int currentPort;

    public void sendTemplate(Template template) {
        ((UDPTemplate) template).send();
    }

    // must be called before createTemplate()
    public String processAdditionalParameters(String commandTemplate) {
        try {
            commandTemplate = commandTemplate.trim();
            int n1 = commandTemplate.indexOf(" ");
            if (n1 > 0) {
                int n2 = commandTemplate.indexOf(" ", n1 + 1);
                if (n2 > n1) {
                    this.currentHost = commandTemplate.substring(0, n1).trim();
                    this.currentPort = Integer.parseInt(commandTemplate.substring(n1 + 1, n2).trim());
                }

                String rest = commandTemplate.substring(n2 + 1).trim();
                return rest;
            }
        } catch (Exception e) {
            log.error(e);
        }

        return commandTemplate;
    }

    public Template createTemplate() {
        return new UDPTemplate(this.currentHost, this.currentPort);
    }

    public synchronized void removeUDPTemplates(String signature) {
        this.removeUDPTemplates(this.commandTemplates, signature);
        this.removeUDPTemplates(this.commandDiffTemplates, signature);
        this.removeUDPTemplates(this.commandDiffTemplatesFirstTime, signature);
    }

    public void removeUDPTemplates(Hashtable<String, Vector<Template>> templatesVector, String signature) {
        Enumeration<Vector<Template>> templatesEnumeration = templatesVector.elements();

        while (templatesEnumeration.hasMoreElements()) {
            Vector removeVector = new Vector();
            Vector<Template> templates = templatesEnumeration.nextElement();

            for (Template template : templates) {
                if (((UDPTemplate) template).equals(signature)) {
                    removeVector.add(template);
                }
            }

            // in separate loop to avoid concurent update exception
            for (Object template : removeVector) {
                templates.remove(template);
            }
        }
    }
}
