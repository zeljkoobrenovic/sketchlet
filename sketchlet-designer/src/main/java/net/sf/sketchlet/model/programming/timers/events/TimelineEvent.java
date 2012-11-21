/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.model.programming.timers.events;

import net.sf.sketchlet.model.programming.macros.Macro;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @author zobrenovic
 */
public class TimelineEvent {

    public double relativeTime = 0.0;
    private Macro macro = new Macro();
    private String label = "";
    private boolean executed = false;

    public TimelineEvent() {
    }

    public TimelineEvent(double time) {
        setTime(time);
    }

    public TimelineEvent(double time, String label) {
        this(time);
        this.setLabel(label);
    }

    public String getTimeString() {
        DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        String strNumber = df.format(relativeTime);
        return strNumber;
    }

    public String getTimeString(double duration) {
        DecimalFormat df = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
        String strNumber = df.format(relativeTime * duration);
        return strNumber;
    }

    public String getTitle() {
        if (this.getLabel().isEmpty()) {
            return this.getTimeString();
        } else {
            return getLabel();
        }
    }

    public void setTime(double time) {
        if (time < 0) {
            relativeTime = 0.0;
        } else if (time > 1) {
            relativeTime = 1;
        } else {
            relativeTime = time;
        }
    }

    public double getTime() {
        return relativeTime;
    }

    public Macro getMacro() {
        return macro;
    }

    public void setMacro(Macro macro) {
        this.macro = macro;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }
}
