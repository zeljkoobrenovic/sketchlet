package net.sf.sketchlet.net.tcp;

import net.sf.sketchlet.common.AcceptConnectionsThread;
import net.sf.sketchlet.common.net.ClientConnectionThread;
import net.sf.sketchlet.common.net.ClientLineProcessingThread;
import net.sf.sketchlet.net.DataReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

/**
 * @author Omnibook
 */

public class TCPDataReceiver extends DataReceiver {
    private AcceptTCPDataReceiverConnections acceptUpdateConnections;

    public TCPDataReceiver(int port) {
        this.acceptUpdateConnections = new AcceptTCPDataReceiverConnections(this, port);
    }
}

class AcceptTCPDataReceiverConnections extends AcceptConnectionsThread {
    private TCPDataReceiver dataReceiver;

    public AcceptTCPDataReceiverConnections(TCPDataReceiver dataReceiver, int port) {
        super(port);
        this.dataReceiver = dataReceiver;
    }

    public ClientConnectionThread getClientConnectionThreadInstance(Socket socket, Vector clients) {
        return new ClientLineProcessingThread(socket, clients, false) {
            public void processLine(String line, BufferedReader in, PrintWriter out) throws IOException {
                dataReceiver.updateVariable(line, this.isEncode());
            }
        };
    }
}