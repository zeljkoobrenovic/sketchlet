package net.sf.sketchlet.designer.data;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.programming.macros.Macro;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 15-10-12
 * Time: 20:58
 * To change this template use File | Settings | File Templates.
 */
public class EventMacro {
    private String eventName = "";
    private Macro macro = new Macro();

    public EventMacro(String eventName) {
        setEventName(eventName);
    }

    public EventMacro(EventMacro eventMacro) {
        populateFromInstance(eventMacro);
    }

    public String getEventName() {
        return eventName;
    }

    public void startMacro() {
        getMacro().startThread(Workspace.getPage(), "", "", "", null);
    }

    public void populateFromInstance(EventMacro eventMacro) {
        this.setEventName(eventMacro.getEventName());
        this.getMacro().repeat = eventMacro.getMacro().repeat;
        for (int i = 0; i < eventMacro.getMacro().actions.length; i++) {
            this.getMacro().actions[i] = eventMacro.getMacro().actions[i];
        }
        for (String paramName : eventMacro.parameters().keySet()) {
            this.parameters().put(paramName, eventMacro.parameters().get(paramName));
        }
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
        this.macro.name = eventName;
    }

    public Macro getMacro() {
        return macro;
    }

    public void setMacro(Macro macro) {
        this.macro = macro;
    }

    public Map<String, String> parameters() {
        return macro.getParameters();
    }

    public void dispose() {
        if (macro != null) {
            macro.dispose();
            macro = null;
        }
    }
}
