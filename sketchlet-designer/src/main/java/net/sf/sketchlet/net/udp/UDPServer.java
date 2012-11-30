package net.sf.sketchlet.net.udp;

import net.sf.sketchlet.common.net.ProcessPacket;
import net.sf.sketchlet.common.net.UDPDataReceiverThread;
import net.sf.sketchlet.net.DataReceiver;
import net.sf.sketchlet.net.SketchletNetworkProtocol;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.net.Template;
import net.sf.sketchlet.net.TemplateHandler;
import org.apache.log4j.Logger;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author Omnibook
 */
public class UDPServer extends DataReceiver {

    private UDPCommandDataReceiverThread receiveUpdatePacketThread;

    public UDPServer(int port) {
        receiveUpdatePacketThread = new UDPCommandDataReceiverThread(this, port);
    }

    public void processUDPTemplates(String updatedVariables[], String diffVariables[]) {
        this.receiveUpdatePacketThread.getTemplateHandler().processTemplates(updatedVariables);
        this.receiveUpdatePacketThread.getTemplateHandler().processDiffTemplates(diffVariables);
        this.receiveUpdatePacketThread.getTemplateHandler().processDiffTemplatesFirstTime(updatedVariables);
    }
}

class UDPCommandDataReceiverThread extends UDPDataReceiverThread {
    private static final Logger log = Logger.getLogger(UDPCommandDataReceiverThread.class);

    private UDPServer dataReceiver;
    private UDPTemplateHandler templateHandler = new UDPTemplateHandler();
    private boolean encode = true;

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
                        getTemplateHandler().removeUDPTemplates(command.substring(13));
                        getTemplateHandler().processTemplateCommand(command);
                    } catch (Exception e) {
                        log.error(e);
                    }
                } else if (command.startsWith("REGISTER ")) {
                    try {
                        UDPTemplate.setEncodingEnabled(false);
                        getTemplateHandler().removeUDPTemplates(command.substring(9));
                        getTemplateHandler().processTemplateCommand(command);
                    } catch (Exception e) {
                        log.error(e);
                    }
                } else if (command.startsWith("SET ENCODING OFF")) {
                    UDPTemplate.setEncodingEnabled(false);
                } else if (command.startsWith("SET ENCODING ON")) {
                    UDPTemplate.setEncodingEnabled(true);
                } else if (command.startsWith("REMOVE TEMPLATE ")) {
                    getTemplateHandler().removeUDPTemplates(command.substring(16));
                } else if (command.startsWith("UNREGISTER ")) {
                    getTemplateHandler().removeUDPTemplates(command.substring(11));
                } else if (command.startsWith("UPDATE") || command.startsWith("DELETE ")) {
                    dataReceiver.updateVariable(command, encode);
                } else {
                    if (VariablesBlackboard.isPaused()) {
                        return;
                    }
                    SketchletNetworkProtocol.processCommand(command);
                }
            }
        };
    }

    public UDPTemplateHandler getTemplateHandler() {
        return templateHandler;
    }

    public void setTemplateHandler(UDPTemplateHandler templateHandler) {
        this.templateHandler = templateHandler;
    }
}

class UDPTemplateHandler extends TemplateHandler {
    private static final Logger log = Logger.getLogger(UDPTemplateHandler.class);

    private String currentHost;
    private int currentPort;

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
        this.removeUDPTemplates(this.getCommandTemplates(), signature);
        this.removeUDPTemplates(this.getCommandDiffTemplates(), signature);
        this.removeUDPTemplates(this.getCommandDiffTemplatesFirstTime(), signature);
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
