/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.tools.log;

/**
 * @author zobrenovic
 */
public class LogItem {
    private long time = 0;
    private String action = "";
    private String parameters = "";

    public LogItem(long time, String action, String paramaters) {
        this.time = time;
        this.action = action;
        this.parameters = paramaters;
    }

    public String toString() {
        return time + "\t" + action + "\t" + parameters;
    }
}
