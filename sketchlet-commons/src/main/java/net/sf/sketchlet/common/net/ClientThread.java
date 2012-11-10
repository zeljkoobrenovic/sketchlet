/*
 * ClientThread.java
 *
 * Created on 16 February 2006, 19:16
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
abstract class ClientThread implements Runnable {
    protected Thread thread = new Thread( this );
    protected int sleepTimeMs;
    
    public ClientThread( int sleepTimeMs ) {
        this.sleepTimeMs = sleepTimeMs;
    }
    
    protected abstract String getAction();
    
    public void run() {
        while (true) {
            this.action();
            this.sleep();
        }
    }
    
    public abstract void action();
    
    public void sleep() {
        try {
            Thread.sleep( sleepTimeMs );
        } catch (Exception e) {
        }
    }
}
