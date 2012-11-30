package net.sf.sketchlet.framework.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 *
 * @author zeljko
 */
public class PageVariable {
    private String name = "";
    private String value = "";
    private String description = "";
    private String format = "";

    public PageVariable() {
    }

    public PageVariable(String name, String value, String format) {
        this.name = name == null ? "" : name;
        this.value = value == null ? "" : value;
        this.format = format == null ? "" : format;
    }

    public PageVariable(String name, String value) {
        this.name = name;
        this.value = value;
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
        this.boundValue();
    }

    private void boundValue() {
        if (!this.format.isEmpty() /*|| !this.min.isEmpty() || !this.max.isEmpty()*/) {
            try {
                DecimalFormat df = new DecimalFormat(this.format, new DecimalFormatSymbols(Locale.US));
                double dv = Double.parseDouble(this.value);
                if (!this.format.isEmpty()) {
                    this.value = df.format(dv);
                }
            } catch (Exception e) {
            }
        }
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

}
