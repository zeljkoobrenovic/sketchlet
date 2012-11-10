/*
 * Variable.java
 *
 * Created on April 23, 2008, 2:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.communicator.server;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.Locale;

/**
 * @author cuypers
 */
public class Variable implements Comparator {

    public String name = "";
    public String value = "";
    public String group = "";
    public String description = "";
    public String format = "";
    public String min = "";
    public String max = "";
    public int count = 0;
    public int countFilter = 1;
    public int timeFilterMs = 0;
    public long timestamp = System.currentTimeMillis();

    /**
     * Creates a new instance of Variable
     */
    public Variable() {
    }

    public void save() {
    }

    public int compare(Object o1, Object o2) {
        if (o1 instanceof Variable && o2 instanceof Variable) {
            return ((Variable) o1).name.compareTo(((Variable) o2).name);
        } else {
            return 0;
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            return ((Variable) obj).name.equalsIgnoreCase(this.name);
        } else {
            return false;
        }
    }

    public void boundValue() {
        if (!this.format.isEmpty() || !this.min.isEmpty() || !this.max.isEmpty()) {
            try {
                DecimalFormat df = new DecimalFormat(this.format, new DecimalFormatSymbols(Locale.US));
                double dv = Double.parseDouble(this.value);
                if (!this.min.isEmpty()) {
                    try {
                        double m = Double.parseDouble(this.min);
                        if (dv < m) {
                            dv = m;
                        }
                    } catch (Exception e2) {
                    }
                }
                if (!this.max.isEmpty()) {
                    try {
                        double m = Double.parseDouble(this.max);
                        if (dv > m) {
                            dv = m;
                        }
                    } catch (Exception e2) {
                    }
                }

                if (!this.format.isEmpty()) {
                    this.value = df.format(dv);
                }
            } catch (Exception e) {
            }
        }

    }
}
