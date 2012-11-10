/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.regions.connector;

import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.util.XMLUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Properties;

/**
 * @author zobrenovic
 */
public class Connector {

    ActiveRegion region1 = null;
    ActiveRegion region2 = null;
    public String caption = "";
    public String textColor = "";
    public String fontName = "";
    public String fontStyle = "";
    public String fontSize = "";
    public String lineStyle = "";
    public String lineThickness = "";
    public String fillColor = "";
    public String lineColor = "";
    public String lineStart = "";
    public String lineEnd = "";
    public String variablesMapping[][] = {
            {"", "", "", ""},
            {"", "", "", ""},
            {"", "", "", ""},
            {"", "", "", ""},
            {"", "", "", ""},
            {"", "", "", ""},
            {"", "", "", ""},
            {"", "", "", ""},
            {"", "", "", ""},
            {"", "", "", ""},
            {"", "", "", ""},};
    Properties properties = new Properties();
    public ConnectorRenderer renderer = new ConnectorRenderer(this);

    public Connector() {
    }

    public Connector(ActiveRegion region1) {
        this.region1 = region1;
    }

    public Connector(ActiveRegion region1, ActiveRegion region2) {
        this.region1 = region1;
        this.region2 = region2;
    }

    public void dispose() {
        this.renderer.connector = null;
        this.renderer = null;
        this.region1 = null;
        this.region2 = null;
    }

    public void setRegion1(ActiveRegion region1) {
        this.region1 = region1;
    }

    public void setRegion2(ActiveRegion region2) {
        this.region2 = region2;
    }

    public ActiveRegion getRegion1() {
        return this.region1;
    }

    public ActiveRegion getRegion2() {
        return this.region2;
    }

    public String toXML(String prefix) {
        StringBuffer str = new StringBuffer("");

        str.append(prefix + "<connector region1='" + region1.getDrawImageFileName() + "' region2 ='" + region2.getDrawImageFileName() + "'>\n");
        if (!this.caption.isEmpty()) {
            str.append(prefix + "    <connector-caption>" + XMLUtils.prepareForXML(this.caption) + "</connector-caption>\n");
        }
        if (!textColor.isEmpty()) {
            str.append(prefix + "    <connector-property name='text color'>" + XMLUtils.prepareForXML(this.textColor) + "</connector-property>\n");
        }
        if (!fontName.isEmpty()) {
            str.append(prefix + "    <connector-property name='font name'>" + XMLUtils.prepareForXML(this.fontName) + "</connector-property>\n");
        }
        if (!fontStyle.isEmpty()) {
            str.append(prefix + "    <connector-property name='font style'>" + XMLUtils.prepareForXML(this.fontStyle) + "</connector-property>\n");
        }
        if (!fontSize.isEmpty()) {
            str.append(prefix + "    <connector-property name='font size'>" + XMLUtils.prepareForXML(this.fontSize) + "</connector-property>\n");
        }
        if (!fillColor.isEmpty()) {
            str.append(prefix + "    <connector-property name='fill color'>" + XMLUtils.prepareForXML(this.fillColor) + "</connector-property>\n");
        }
        if (!lineStyle.isEmpty()) {
            str.append(prefix + "    <connector-property name='line style'>" + XMLUtils.prepareForXML(this.lineStyle) + "</connector-property>\n");
        }
        if (!lineThickness.isEmpty()) {
            str.append(prefix + "    <connector-property name='line thickness'>" + XMLUtils.prepareForXML(this.lineThickness) + "</connector-property>\n");
        }
        if (!lineColor.isEmpty()) {
            str.append(prefix + "    <connector-property name='line color'>" + XMLUtils.prepareForXML(this.lineColor) + "</connector-property>\n");
        }
        if (!lineStart.isEmpty()) {
            str.append(prefix + "    <connector-property name='line start'>" + XMLUtils.prepareForXML(this.lineStart) + "</connector-property>\n");
        }
        if (!lineEnd.isEmpty()) {
            str.append(prefix + "    <connector-property name='line end'>" + XMLUtils.prepareForXML(this.lineEnd) + "</connector-property>\n");
        }
        str.append(prefix + "    <connector-mappings>\n");
        for (String mapping[] : this.variablesMapping) {
            String variable = mapping[0];
            String min = mapping[1];
            String max = mapping[2];
            String format = mapping[3];

            if (!variable.isEmpty()) {
                str.append(prefix + "        <connector-mapping variable='" + variable + "' min='" + min + "' max='" + max + "' format='" + format + "'/>\n");
            }
        }

        str.append(prefix + "    </connector-mappings>\n");
        str.append(prefix + "</connector>\n");

        return str.toString();
    }

    public void setProperty(String key, String value) {
        if (key.equalsIgnoreCase("text color")) {
            this.textColor = value;
        } else if (key.equalsIgnoreCase("font name")) {
            this.fontName = value;
        } else if (key.equalsIgnoreCase("font style")) {
            this.fontStyle = value;
        } else if (key.equalsIgnoreCase("font size")) {
            this.fontSize = value;
        } else if (key.equalsIgnoreCase("fill color")) {
            this.fillColor = value;
        } else if (key.equalsIgnoreCase("line style")) {
            this.lineStyle = value;
        } else if (key.equalsIgnoreCase("line thickness")) {
            this.lineThickness = value;
        } else if (key.equalsIgnoreCase("line color")) {
            this.lineColor = value;
        } else if (key.equalsIgnoreCase("line start")) {
            this.lineStart = value;
        } else if (key.equalsIgnoreCase("line end")) {
            this.lineEnd = value;
        }
    }

    public void updateVariables(boolean bPlayback) {
        for (String line[] : this.variablesMapping) {
            String variable = line[0];
            if (!variable.isEmpty()) {
                variable = Evaluator.processText(variable, "", "");
                String strA = Evaluator.processText(line[1], "", "");
                String strB = Evaluator.processText(line[2], "", "");
                String strFormat = Evaluator.processText(line[3], "", "");
                int x1 = this.region1.getCenterX(bPlayback);
                int y1 = this.region1.getCenterY(bPlayback);
                int x2 = this.region2.getCenterX(bPlayback);
                int y2 = this.region2.getCenterY(bPlayback);
                double distance = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

                double a = 0;
                if (!strA.isEmpty()) {
                    try {
                        a = Double.parseDouble(strA);
                    } catch (Exception e) {
                    }
                }

                double b = 1;
                if (!strB.isEmpty()) {
                    try {
                        b = Double.parseDouble(strB);
                    } catch (Exception e) {
                    }
                }

                distance = a + b * distance;

                String strValue;
                if (strFormat.isEmpty()) {
                    strFormat = "0.00";
                }
                DecimalFormat df = new DecimalFormat(strFormat, new DecimalFormatSymbols(Locale.US));
                strValue = df.format(distance);

                VariablesBlackboardContext.getInstance().updateVariableIfDifferent(variable, strValue);
            }
        }
    }
}
