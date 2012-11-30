package net.sf.sketchlet.framework.model.events.hold;

import java.util.List;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class HoldThread implements Runnable {

    private Thread thread = new Thread(this);
    private int durationMs = 0;
    private boolean stopped = false;
    private String id;
    private HoldData data;
    private HoldProcessor processor;
    private HoldThreads threads;
    private boolean repeatEnabled;

    public HoldThread(int durationMs, HoldThreads threads, HoldData data, HoldProcessor processor, String id, boolean repeatEnabled) {
        this.durationMs = durationMs;
        this.data = data;
        this.processor = processor;
        this.threads = threads;
        this.id = id;
        this.repeatEnabled = repeatEnabled;
        thread.start();
    }

    public void run() {
        try {
            List<HoldThread> v = threads.getHoldThreads().get(id);
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

                if (!repeatEnabled) {
                    break;
                }
            }
            v.remove(this);

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
