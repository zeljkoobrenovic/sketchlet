package net.sf.sketchlet.framework.blackboard;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.Locale;

/**
 * @author cuypers
 */
public class Variable implements Comparator {

    private String name = "";
    private String value = "";
    private String group = "";
    private String description = "";
    private String format = "";
    private String min = "";
    private String max = "";
    private int count = 0;
    private int countFilter = 1;
    private int timeFilterMs = 0;
    private long timestamp = System.currentTimeMillis();

    public Variable() {
    }

    public void save() {
    }

    public int compare(Object o1, Object o2) {
        if (o1 instanceof Variable && o2 instanceof Variable) {
            return ((Variable) o1).getName().compareTo(((Variable) o2).getName());
        } else {
            return 0;
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof Variable) {
            return ((Variable) obj).getName().equalsIgnoreCase(this.getName());
        } else {
            return false;
        }
    }

    public void boundValue() {
        if (!this.getFormat().isEmpty() || !this.getMin().isEmpty() || !this.getMax().isEmpty()) {
            try {
                DecimalFormat df = new DecimalFormat(this.getFormat(), new DecimalFormatSymbols(Locale.US));
                double dv = Double.parseDouble(this.getValue());
                if (!this.getMin().isEmpty()) {
                    try {
                        double m = Double.parseDouble(this.getMin());
                        if (dv < m) {
                            dv = m;
                        }
                    } catch (Exception e2) {
                    }
                }
                if (!this.getMax().isEmpty()) {
                    try {
                        double m = Double.parseDouble(this.getMax());
                        if (dv > m) {
                            dv = m;
                        }
                    } catch (Exception e2) {
                    }
                }

                if (!this.getFormat().isEmpty()) {
                    this.setValue(df.format(dv));
                }
            } catch (Exception e) {
            }
        }

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCountFilter() {
        return countFilter;
    }

    public void setCountFilter(int countFilter) {
        this.countFilter = countFilter;
    }

    public int getTimeFilterMs() {
        return timeFilterMs;
    }

    public void setTimeFilterMs(int timeFilterMs) {
        this.timeFilterMs = timeFilterMs;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
