package net.sf.sketchlet.framework.model.events;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.framework.model.programming.macros.Macro;

import java.util.Map;

/**
 *
 * @author zeljko
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
        this.getMacro().setRepeat(eventMacro.getMacro().getRepeat());
        for (int i = 0; i < eventMacro.getMacro().getActions().length; i++) {
            this.getMacro().getActions()[i] = eventMacro.getMacro().getActions()[i];
        }
        for (String paramName : eventMacro.parameters().keySet()) {
            this.parameters().put(paramName, eventMacro.parameters().get(paramName));
        }
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
        this.macro.setName(eventName);
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
