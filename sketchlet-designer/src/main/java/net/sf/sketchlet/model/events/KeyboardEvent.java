/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.model.events;

/**
 * @author zobrenovic
 */
public class KeyboardEvent {
    private String name = "";
    private int keyCode = -1;

    public KeyboardEvent(String name, int keyCode) {
        this.setName(name);
        this.setKeyCode(keyCode);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }
}
