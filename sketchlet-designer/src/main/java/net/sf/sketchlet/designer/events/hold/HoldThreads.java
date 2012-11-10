/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.events.hold;

import java.util.Hashtable;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class HoldThreads {

    public Hashtable<String, Vector<HoldThread>> holdThreads = new Hashtable<String, Vector<HoldThread>>();
    public Vector<String> occuredEvents = new Vector<String>();
    private final static String safeOccuredPrefix = "kjhf84398tr*&*(&(8R73";

    public void processHold(int durationMs, HoldData data, HoldProcessor processor, String id, boolean bRepeat) {
        new HoldThread(durationMs, this, data, processor, id, bRepeat);
    }

    public void stopHold(String id) {
        Vector<HoldThread> v = holdThreads.get(id);
        if (v != null) {
            for (HoldThread t : v) {
                t.stop();
            }
        }
    }

    public void setOccured(String id) {
        id = safeOccuredPrefix + id;
        if (!this.occuredEvents.contains(id)) {
            this.occuredEvents.add(id);
        }
    }

    public boolean hasOccured(String id) {
        id = safeOccuredPrefix + id;
        return this.occuredEvents.contains(id);
    }

    public void removeOccured(String id) {
        id = safeOccuredPrefix + id;
        for (int i = this.occuredEvents.size() - 1; i >= 0; i--) {
            String strId = this.occuredEvents.elementAt(i);
            if (strId.startsWith(id)) {
                this.occuredEvents.remove(i);
            }
        }
        this.occuredEvents.remove(id);
    }
}
