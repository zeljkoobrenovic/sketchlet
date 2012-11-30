package net.sf.sketchlet.framework.model.events;

import java.util.List;

/**
 *
 * @author zeljko
 */
public abstract class EventMacroFactory<T extends EventMacro> {

    public abstract T getNewEventMacroInstance(String... args);

    public abstract String getEventTypeName(boolean plural);

    public abstract List<T> getEventMacroList();

    public String getEventDescription(T eventMacro) {
        return eventMacro.getEventName();
    }
}
