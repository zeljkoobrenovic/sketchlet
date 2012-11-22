/*
 * GenericAdapter.java
 *
 * Created on May 1, 2006, 1:15 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.net;

import net.sf.sketchlet.common.DefaultSettings;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Map;
import java.util.Vector;

/**
 * @author obrenovi
 */
public abstract class GenericAdapter {
    private static Logger log = Logger.getLogger(GenericAdapter.class);
    protected String adapterURLs[];
    protected CommunicatorTcpInterface communicatorTcpInterface;
    protected String communicatorHost = DefaultSettings.getHost();
    protected int communicatorPort = DefaultSettings.getCommunicatorTCPPort();
    protected String stateVariable = "";
    protected GenericAdapterClientLineProcessingThread remoteControl;
    protected Vector commands = new Vector();
    protected String rootName = "amico-adapter";

    /**
     * Creates a new instance of GenericAdapter
     */
    public GenericAdapter() {
    }

    public void init(String configFileName, String rootName) {
        this.init(configFileName, rootName, null);
    }

    public String[] init(String configFileName, String rootName, String appSpecificData[]) {
        this.rootName = rootName;
        String appSpecificDataResults[] = this.load(configFileName, appSpecificData);
        this.communicatorTcpInterface = new CommunicatorTcpInterface(this.communicatorHost, this.communicatorPort);
        Socket socket = null;

        while (true) {
            try {
                socket = new Socket(this.communicatorHost, this.communicatorPort);
                log.info("GenericAdapter: Connected to the communicator at " + this.communicatorHost + ":" + this.communicatorPort);
                break;
            } catch (Exception e) {
                // e.printStackTrace(System.out);
                log.info("GenericAdapter: I could not connect to the communicator. I'll try again in one second...");
                try {
                    Thread.sleep(1000);
                } catch (Exception e2) {
                }
            }
        }

        this.remoteControl = this.getRemoteControl(this, socket, this.communicatorHost, this.communicatorPort);
        this.loadAdapters();
        this.remoteControl.init();

        return appSpecificDataResults;
    }

    public abstract GenericAdapterClientLineProcessingThread getRemoteControl(GenericAdapter adapter, Socket socket, String communicatorHost, int communicatorPort);

    public String[] load(String configURL, String appSpecificData[]) {
        String appSpecificDataResults[] = null;
        try {
            DocumentBuilderFactory factory;
            DocumentBuilder builder;
            TransformerFactory tFactory;
            Transformer transformers[];

            try {
                factory = DocumentBuilderFactory.newInstance();
                builder = factory.newDocumentBuilder();
                tFactory = TransformerFactory.newInstance();
            } catch (Exception e) {
                e.printStackTrace(System.out);
                return null;
            }

            Document docConfig;
            try {
                docConfig = builder.parse(new URL(configURL).openStream());
            } catch (FileNotFoundException fnfe) {
                log.error("Configuration file '" + configURL + "' could not be found. Creating an empty document.");
                docConfig = builder.newDocument();
            }

            XPath xpath = XPathFactory.newInstance().newXPath();

            String expression;
            String str;
            double number;

            if (appSpecificData != null) {
                appSpecificDataResults = new String[appSpecificData.length];

                for (int i = 0; i < appSpecificData.length; i++) {
                    expression = appSpecificData[i];
                    str = (String) xpath.evaluate(expression, docConfig, XPathConstants.STRING);
                    appSpecificDataResults[i] = str;
                }
            }

            this.stateVariable = (String) xpath.evaluate("/" + this.rootName + "/@state-variable", docConfig, XPathConstants.STRING);

            expression = "/" + this.rootName + "/communicator/@host";
            str = (String) xpath.evaluate(expression, docConfig, XPathConstants.STRING);

            if (!str.equals("")) {
                this.communicatorHost = str;
            } else {
                this.communicatorHost = DefaultSettings.getHost();
            }

            expression = "/" + this.rootName + "/communicator/@port";
            number = ((Double) xpath.evaluate(expression, docConfig, XPathConstants.NUMBER)).doubleValue();
            this.communicatorPort = (int) number;

            if (number > 0) {
                this.communicatorPort = (int) number;
            } else {
                this.communicatorPort = DefaultSettings.getCommunicatorTCPPort();
            }


            expression = "/" + this.rootName + "/communicator/command";
            NodeList commands = (NodeList) xpath.evaluate(expression, docConfig, XPathConstants.NODESET);

            for (int i = 0; i < commands.getLength(); i++) {
                Node command = commands.item(i);
                int delayMs = (int) ((Double) xpath.evaluate("@delayMs", command, XPathConstants.NUMBER)).doubleValue();
                this.commands.add(new Command(command.getTextContent(), delayMs));
            }

            expression = "/" + this.rootName + "/" + this.rootName + "-adapter";
            NodeList adapters = (NodeList) xpath.evaluate(expression, docConfig, XPathConstants.NODESET);

            this.adapterURLs = new String[adapters.getLength()];

            for (int i = 0; i < adapters.getLength(); i++) {
                Node adapter = adapters.item(i);
                adapterURLs[i] = (String) xpath.evaluate("@url", adapter, XPathConstants.STRING);
            }
        } catch (XPathExpressionException xpee) {
            xpee.printStackTrace(System.out);
        } catch (SAXException sxe) {
            sxe.printStackTrace(System.out);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.out);
        }

        return appSpecificDataResults;
    }

    public void updateStateVariable(String state) {
        if (!this.stateVariable.equals("")) {
            this.communicatorTcpInterface.set(this.stateVariable, state);
        }
    }

    public String replaceSystemVariables(String commandLine) {
        java.util.Map<String, String> env = System.getenv();

        for (Map.Entry variable : env.entrySet()) {
            String name = (String) variable.getKey();
            String value = (String) variable.getValue();

            commandLine = commandLine.replace("%" + name + "%", value);
            commandLine = commandLine.replace("$" + name, value);
        }

        return commandLine;
    }

    public void loadAdapters() {
        for (int i = 0; i < this.adapterURLs.length; i++) {
            String adapterUrl = this.replaceSystemVariables(adapterURLs[i]);

            this.loadAdapter(adapterUrl);
        }
    }

    public abstract void loadAdapter(String adapterURL);
}

class Command {

    public Command(String command, int delayMs) {
        this.setCommand(command);
        this.setDelayMs(delayMs);
    }

    private String command;
    private int delayMs = 0;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public void setDelayMs(int delayMs) {
        this.delayMs = delayMs;
    }
}