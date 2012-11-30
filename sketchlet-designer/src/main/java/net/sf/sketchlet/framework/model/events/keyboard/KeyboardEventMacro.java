package net.sf.sketchlet.framework.model.events.keyboard;

import net.sf.sketchlet.framework.model.events.EventMacro;

/**
 *
 * @author zeljko
 */
public class KeyboardEventMacro extends EventMacro {
    public static final String PARAMETER_KEY = "key";
    public static final String PARAMETER_MODIFIERS = "modifiers";

    public KeyboardEventMacro(String eventName) {
        super(eventName);
        parameters().put(PARAMETER_KEY, "");
        parameters().put(PARAMETER_MODIFIERS, "");
    }

    public KeyboardEventMacro(EventMacro eventMacro) {
        super(eventMacro);
    }

    public void setKey(String key) {
        getMacro().getParameters().put(PARAMETER_KEY, key);
    }

    public void setModifiers(String modifiers) {
        getMacro().getParameters().put(PARAMETER_MODIFIERS, modifiers);
    }

    public String getKey() {
        return getMacro().getParameters().get(PARAMETER_KEY);
    }

    public String getModifiers() {
        return getMacro().getParameters().get(PARAMETER_MODIFIERS);
    }
}
