package net.sf.sketchlet.designer.data;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 26-10-12
 * Time: 21:01
 * To change this template use File | Settings | File Templates.
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
