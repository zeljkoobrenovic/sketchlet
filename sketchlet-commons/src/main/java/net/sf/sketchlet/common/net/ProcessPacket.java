/*
 * ProcessPacket.java
 *
 * Created on 24 February 2006, 11:51
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package net.sf.sketchlet.common.net;

/**
 *
 * @author Omnibook
 */
public interface ProcessPacket {
    public void processPacket( byte[] data );
}
