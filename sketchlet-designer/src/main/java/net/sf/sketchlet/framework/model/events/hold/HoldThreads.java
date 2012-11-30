package net.sf.sketchlet.framework.model.events.hold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zobrenovic
 */
public class HoldThreads {

    private Map<String, List<HoldThread>> holdThreads = new HashMap<String, List<HoldThread>>();
    private List<String> occurredEvents = new ArrayList<String>();
    private final static String SAFE_OCCURRED_PREFIX = "kjhf84398tr*&*(&(8R73";

    public void processHold(int durationMs, HoldData data, HoldProcessor processor, String id, boolean bRepeat) {
        new HoldThread(durationMs, this, data, processor, id, bRepeat);
    }

    public void stopHold(String id) {
        List<HoldThread> v = getHoldThreads().get(id);
        if (v != null) {
            for (HoldThread t : v) {
                t.stop();
            }
        }
    }

    public void setOccurred(String id) {
        id = HoldThreads.SAFE_OCCURRED_PREFIX + id;
        if (!this.occurredEvents.contains(id)) {
            this.occurredEvents.add(id);
        }
    }

    public boolean hasOccurred(String id) {
        id = HoldThreads.SAFE_OCCURRED_PREFIX + id;
        return this.occurredEvents.contains(id);
    }

    public void removeOccurred(String id) {
        id = HoldThreads.SAFE_OCCURRED_PREFIX + id;
        for (int i = this.occurredEvents.size() - 1; i >= 0; i--) {
            String strId = this.occurredEvents.get(i);
            if (strId.startsWith(id)) {
                this.occurredEvents.remove(i);
            }
        }
        this.occurredEvents.remove(id);
    }

    public Map<String, List<HoldThread>> getHoldThreads() {
        return holdThreads;
    }
}
