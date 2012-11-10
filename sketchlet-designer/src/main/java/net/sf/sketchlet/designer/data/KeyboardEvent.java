/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.designer.data;

/**
 * @author zobrenovic
 */
public class KeyboardEvent {
    String name = "";
    int keyCode = -1;

    public KeyboardEvent(String name, int keyCode) {
        this.name = name;
        this.keyCode = keyCode;
    }
}
