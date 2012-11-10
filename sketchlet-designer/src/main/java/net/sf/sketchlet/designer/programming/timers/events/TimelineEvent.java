/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.timers.events;

import net.sf.sketchlet.designer.programming.macros.Macro;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @author zobrenovic
 */
public class TimelineEvent {

    double relTime = 0.0;
    public Macro macro = new Macro();
    public String label = "";
    boolean bExecuted = false;

    public TimelineEvent() {
    }

    public TimelineEvent(double time) {
        setTime(time);
    }

    public TimelineEvent(double time, String label) {
        this(time);
        this.label = label;
    }

    public String getTimeString() {
        DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
        String strNumber = df.format(relTime);
        return strNumber;
    }

    public String getTimeString(double duration) {
        DecimalFormat df = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
        String strNumber = df.format(relTime * duration);
        return strNumber;
    }

    public String getTitle() {
        if (this.label.isEmpty()) {
            return this.getTimeString();
        } else {
            return label;
        }
    }

    public void setTime(double time) {
        if (time < 0) {
            relTime = 0.0;
        } else if (time > 1) {
            relTime = 1;
        } else {
            relTime = time;
        }
    }

    public double getTime() {
        return relTime;
    }
}
