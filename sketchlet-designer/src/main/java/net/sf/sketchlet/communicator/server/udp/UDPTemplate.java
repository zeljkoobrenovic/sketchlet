/*
 * Template.java
 *
 * Created on September 25, 2006, 1:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.communicator.server.udp;

import net.sf.sketchlet.common.net.UDPUtils;
import net.sf.sketchlet.communicator.server.DataServer;

public class UDPTemplate extends net.sf.sketchlet.communicator.server.Template {
    public String host;
    public int port;

    public static boolean encode = true;

    public UDPTemplate(String host, int udpPort) {
        this.host = host;
        this.port = udpPort;
    }

    public UDPTemplate(String host, int udpPort, String template, String variable, String test) {
        super(template, variable, test);
        this.host = host;
        this.port = udpPort;
    }

    // this is used when we delete UDP template,
    // removing UDP template has to be made explicitely as there is no connection
    public boolean equals(String test) {
        String signature = host + " " + port + " " + variable + " " + template;
        return signature.startsWith(test);
    }

    public void send() {
        String populatedTemplate = DataServer.populateTemplate(this.template, encode);
        UDPUtils.sendPacket(this.host, this.port, populatedTemplate);
    }
}