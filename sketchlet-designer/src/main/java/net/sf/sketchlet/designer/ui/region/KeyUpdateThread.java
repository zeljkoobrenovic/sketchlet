/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.region;

/**
 * @author zobrenovic
 */
public class KeyUpdateThread implements Runnable {

    Thread t;
    Runnable action;
    private long timeout = 800;

    public KeyUpdateThread(Runnable action) {
        this.action = action;
        this.t = new Thread(this);
        this.t.start();
    }

    public KeyUpdateThread(Runnable action, long timeout) {
        this.action = action;
        this.timeout = timeout;
        this.t = new Thread(this);
        this.t.start();
    }

    public void run() {
        try {
            Thread.sleep(timeout);
            if (this.action != null) {
                this.action.run();
                this.action = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}