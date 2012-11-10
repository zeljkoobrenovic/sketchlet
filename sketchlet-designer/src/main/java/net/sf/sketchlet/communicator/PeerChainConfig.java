/*
 * PeerChainConfig.java
 *
 * Created on April 21, 2008, 2:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.communicator;

import java.util.Vector;

/**
 * @author cuypers
 */
public class PeerChainConfig {
    public String host;
    public int port;
    public Vector templates = new Vector();

    public PeerChainConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void addTemplate(String template) {
        this.templates.add(template);
    }
}
