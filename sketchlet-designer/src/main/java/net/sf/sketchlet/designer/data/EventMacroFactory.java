package net.sf.sketchlet.designer.data;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 15-10-12
 * Time: 21:15
 * To change this template use File | Settings | File Templates.
 */
public abstract class EventMacroFactory<T extends EventMacro> {

    public abstract T getNewEventMacroInstance(String... args);

    public abstract String getEventTypeName(boolean plural);

    public abstract List<T> getEventMacroList();

    public String getEventDescription(T eventMacro) {
        return eventMacro.getEventName();
    }
}
