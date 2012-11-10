/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.events.hold;

import java.util.Vector;

/**
 * @author zobrenovic
 */
public class HoldThread implements Runnable {

    Thread t = new Thread(this);
    int durationMs = 0;
    boolean stopped = false;
    String id;
    HoldData data;
    HoldProcessor processor;
    HoldThreads threads;
    boolean bRepeat;

    public HoldThread(int durationMs, HoldThreads threads, HoldData data, HoldProcessor processor, String id, boolean bRepeat) {
        this.durationMs = durationMs;
        this.data = data;
        this.processor = processor;
        this.threads = threads;
        this.id = id;
        this.bRepeat = bRepeat;
        t.start();
    }

    public void run() {
        try {
            Vector<HoldThread> v = threads.holdThreads.get(id);
            if (v == null) {
                v = new Vector<HoldThread>();
                threads.holdThreads.put(id, v);
            }
            v.add(this);
            while (true) {
                Thread.sleep(this.durationMs);
                if (!stopped) {
                    processor.process(data);
                } else {
                    break;
                }

                if (!bRepeat) {
                    break;
                }
            }
            v.removeElement(this);

            data = null;
            processor = null;
            threads = null;
        } catch (Exception e) {
        }
    }

    public void stop() {
        this.stopped = true;
    }
}
