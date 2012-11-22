/*
 * Template.java
 *
 * Created on September 25, 2006, 1:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.net.udp;

import net.sf.sketchlet.common.net.UDPUtils;
import net.sf.sketchlet.net.Template;
import net.sf.sketchlet.blackboard.VariablesBlackboard;

public class UDPTemplate extends Template {
    private String host;
    private int port;

    private static boolean encodingEnabled = true;

    public UDPTemplate(String host, int udpPort) {
        this.host = host;
        this.port = udpPort;
    }

    public UDPTemplate(String host, int udpPort, String template, String variable, String test) {
        super(template, variable, test);
        this.host = host;
        this.port = udpPort;
    }

    public static boolean isEncodingEnabled() {
        return encodingEnabled;
    }

    public static void setEncodingEnabled(boolean encodingEnabled) {
        UDPTemplate.encodingEnabled = encodingEnabled;
    }

    // this is used when we delete UDP template,
    // removing UDP template has to be made explicitely as there is no connection
    public boolean equals(String test) {
        String signature = host + " " + port + " " + getVariable() + " " + getTemplate();
        return signature.startsWith(test);
    }

    public void send() {
        String populatedTemplate = VariablesBlackboard.populateTemplate(this.getTemplate(), isEncodingEnabled());
        UDPUtils.sendPacket(this.host, this.port, populatedTemplate);
    }
}