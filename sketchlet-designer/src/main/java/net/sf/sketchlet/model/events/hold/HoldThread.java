/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.model.events.hold;

import java.util.Vector;

/**
 * @author zobrenovic
 */
public class HoldThread implements Runnable {

    private Thread t = new Thread(this);
    private int durationMs = 0;
    private boolean stopped = false;
    private String id;
    private HoldData data;
    private HoldProcessor processor;
    private HoldThreads threads;
    private boolean bRepeat;

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
            Vector<HoldThread> v = threads.getHoldThreads().get(id);
            if (v == null) {
                v = new Vector<HoldThread>();
                threads.getHoldThreads().put(id, v);
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
