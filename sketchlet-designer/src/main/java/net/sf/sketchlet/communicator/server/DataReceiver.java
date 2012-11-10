/*
 * DataReceiver.java
 *
 * Created on February 21, 2006, 11:03 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.communicator.server;

import net.sf.sketchlet.common.net.ClientLineProcessingThread;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author Omnibook
 */
public class DataReceiver {
    private static final Logger log = Logger.getLogger(DataReceiver.class);

    Vector peerConnections = new Vector();

    public DataReceiver() {
    }

    public void updateVariable(String updateCommand, boolean bEncode) {
        if (DataServer.paused) {
            return;
        }

        int n;
        while ((n = updateCommand.indexOf('\0')) >= 0) {
            updateCommand = updateCommand.substring(n + 1);
        }

        if (bEncode) {
            StringTokenizer tokenizer = new StringTokenizer(updateCommand, " \t\n");
            if (tokenizer.countTokens() >= 2) {
                String prefix = tokenizer.nextToken();
                String variableName = tokenizer.nextToken();
                String value = "";
                String group = "";
                String description = "";

                try {
                    value = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
                    group = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
                    description = URLDecoder.decode(tokenizer.nextToken(), "UTF-8");
                } catch (Exception e) {
                    // log.error(e);
                }
                if (prefix.equalsIgnoreCase("UPDATE-DIRECT")) {
                    DataServer.variablesServer.updateVariable(variableName, value, group, description);
                } else if (prefix.equalsIgnoreCase("UPDATE")) {
                    DataServer.variablesServer.updateVariable(variableName, value, group, description);
                } else if (prefix.equalsIgnoreCase("DELETE")) {
                    if (variableName.contains("*")) {
                        DataServer.variablesServer.removeVariables(variableName);
                    } else {
                        DataServer.variablesServer.removeVariable(variableName);
                    }
                } else {
                    log.info("Command '" + updateCommand + "' not recognized.");
                }
            } else {
                log.info("Wrong number of arguments in '" + updateCommand + "'");
            }
        } else {
            int nu = updateCommand.indexOf(" ");
            if (nu > 0) {
                String prefix = updateCommand.substring(0, nu);
                updateCommand = updateCommand.substring(nu + 1);
                nu = updateCommand.indexOf(" ");

                if (nu > 0) {
                    String variableName = updateCommand.substring(0, nu);
                    String value = updateCommand.substring(nu + 1);
                    String group = "";
                    String description = "";

                    if (prefix.equalsIgnoreCase("UPDATE-DIRECT")) {
                        DataServer.variablesServer.updateVariable(variableName, value);
                    } else if (prefix.equalsIgnoreCase("UPDATE")) {
                        DataServer.variablesServer.updateVariable(variableName, value);
                    } else if (prefix.equalsIgnoreCase("DELETE")) {
                        if (variableName.contains("*")) {
                            DataServer.variablesServer.removeVariables(variableName);
                        } else {
                            DataServer.variablesServer.removeVariable(variableName);
                        }
                    } else {
                        log.info("Command '" + updateCommand + "' not recognized.");
                    }
                }
            }
        }
    }

    // inner class
    class PeerDataReceiver extends ClientLineProcessingThread {

        protected Thread thread = new Thread(this);
        protected Vector commandTemplates = new Vector();

        public PeerDataReceiver(Socket socket, String host, int port, Vector templates) {
            super(socket);
            this.host = host;
            this.port = port;
            this.commandTemplates = templates;
        }

        public void registerTemplates() {
            if (this.socket == null) {
                return;
            }
            try {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()));

                Iterator iterator = this.commandTemplates.iterator();

                while (iterator.hasNext()) {
                    String commandTemplate = (String) iterator.next();
                    out.println(commandTemplate);
                    out.flush();
                    //log.info( "SENDING: " + commandTemplate );
                }
            } catch (Exception e) {
                log.error(e);
            }
        }

        public void processLine(String line, BufferedReader in, PrintWriter out) throws IOException {
            updateVariable(line.trim(), encode);
        }

        protected void reconnect() {
            log.info("Reconnecting...");
            super.reconnect();
            this.registerTemplates();
        }
    }
}


