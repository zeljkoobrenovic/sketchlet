package net.sf.sketchlet.model;

import net.sf.sketchlet.model.evaluator.Evaluator;
import net.sf.sketchlet.model.events.KeyboardEventMacro;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 10-11-12
 * Time: 15:16
 * To change this template use File | Settings | File Templates.
 */
public class KeyboardProcessor {
    private Hashtable<Integer, Vector<HoldThread>> holdThread = new Hashtable<Integer, Vector<HoldThread>>();
    private java.util.List<KeyboardEventMacro> keyboardEventMacros = new ArrayList<KeyboardEventMacro>();

    public boolean processKey(final KeyEvent e, String event) {
        if (event.equalsIgnoreCase("released")) {
            if (this.holdThread != null) {
                Vector<HoldThread> v = holdThread.get(new Integer(e.getKeyCode()));
                if (v != null) {
                    for (HoldThread t : v) {
                        t.stopped = true;
                    }
                    v.removeAllElements();
                    holdThread.remove(new Integer(e.getKeyCode()));
                }
            }
        }

        int modifiers = e.getModifiers();
        boolean shift = (modifiers & KeyEvent.SHIFT_MASK) != 0;
        boolean ctrl = (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
        boolean alt = (modifiers & KeyEvent.ALT_MASK) != 0;

        String keyName = KeyEvents.getCodeName(e.getKeyCode());

        boolean keyProcessed = false;

        for (KeyboardEventMacro keyboardEventMacro : getKeyboardEventMacros()) {
            if (keyName.equalsIgnoreCase(keyboardEventMacro.getKey())) {
                String eventName = keyboardEventMacro.getEventName();
                String strModifiers = keyboardEventMacro.getModifiers();

                if (eventName.toLowerCase().startsWith("hold ") && event.equalsIgnoreCase("pressed")) {
                    try {
                        boolean bRepeat = eventName.endsWith("*");
                        if (bRepeat) {
                            eventName = eventName.substring(0, eventName.length() - 1).trim();
                        }
                        double timeSec = Double.parseDouble(Evaluator.processText(eventName.substring(5), "", ""));
                        Vector<HoldThread> v = holdThread.get(new Integer(e.getKeyCode()));
                        if (v == null) {
                            v = new Vector<HoldThread>();
                            holdThread.put(new Integer(e.getKeyCode()), v);
                        }
                        v.add(new HoldThread((int) (timeSec * 1000), e, keyboardEventMacro, bRepeat));
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                } else if (event.equalsIgnoreCase(eventName)) {
                    boolean _shift = strModifiers.toUpperCase().contains("SHIFT");
                    boolean _ctrl = strModifiers.toUpperCase().contains("CTRL");
                    boolean _alt = strModifiers.toUpperCase().contains("ALT");

                    if (shift != _shift) {
                        continue;
                    }
                    if (ctrl != _ctrl) {
                        continue;
                    }
                    if (alt != _alt) {
                        continue;
                    }

                    keyboardEventMacro.getMacro().startThread(this, "", "", "", null);
                    keyProcessed = true;
                }
            }
        }

        return keyProcessed;
    }

    public void dispose() {
        for (EventMacro eventMacro : getKeyboardEventMacros()) {
            eventMacro.dispose();
        }
        setKeyboardEventMacros(null);
    }

    public java.util.List<KeyboardEventMacro> getKeyboardEventMacros() {
        return keyboardEventMacros;
    }

    public void setKeyboardEventMacros(List<KeyboardEventMacro> keyboardEventMacros) {
        this.keyboardEventMacros = keyboardEventMacros;
    }

    class HoldThread implements Runnable {

        Thread t = new Thread(this);
        int durationMs = 0;
        boolean stopped = false;
        KeyEvent e;
        KeyboardEventMacro keyboardEventMacro;
        boolean bRepeat;

        public HoldThread(int durationMs, KeyEvent e, KeyboardEventMacro keyboardEventMacro, boolean bRepeat) {
            this.durationMs = durationMs;
            this.e = e;
            this.keyboardEventMacro = keyboardEventMacro;
            this.bRepeat = bRepeat;
            t.start();
        }

        public void run() {
            try {
                while (true) {
                    Thread.sleep(this.durationMs);
                    if (!stopped) {
                        processHoldKey(e, keyboardEventMacro);
                    } else {
                        break;
                    }
                    if (!bRepeat) {
                        break;
                    }
                }

                Vector<HoldThread> v = holdThread.get(new Integer(e.getKeyCode()));
                if (v != null) {
                    v.removeElement(this);
                }
            } catch (Exception e) {
            }
        }
    }

    public boolean processHoldKey(final KeyEvent e, KeyboardEventMacro keyboardEventMacro) {
        int modifiers = e.getModifiers();
        boolean shift = (modifiers & KeyEvent.SHIFT_MASK) != 0;
        boolean ctrl = (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
        boolean alt = (modifiers & KeyEvent.ALT_MASK) != 0;

        String name = KeyEvents.getCodeName(e.getKeyCode());

        boolean keyProcessed = false;

        String strKey = keyboardEventMacro.getKey();
        String strEvent = keyboardEventMacro.getEventName();
        if (strEvent.startsWith("hold ") && name.equalsIgnoreCase(strKey)) {
            String strModifiers = keyboardEventMacro.getModifiers();
            boolean _shift = strModifiers.toUpperCase().contains("SHIFT");
            boolean _ctrl = strModifiers.toUpperCase().contains("CTRL");
            boolean _alt = strModifiers.toUpperCase().contains("ALT");

            if (shift != _shift) {
                return false;
            }
            if (ctrl != _ctrl) {
                return false;
            }
            if (alt != _alt) {
                return false;
            }

            keyboardEventMacro.getMacro().startThread(this, "", "", "", null);
            keyProcessed = true;
        }

        return keyProcessed;
    }
}
