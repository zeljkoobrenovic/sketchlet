/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.model;

import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.renderer.regions.ConnectorRenderer;
import net.sf.sketchlet.model.evaluator.Evaluator;
import net.sf.sketchlet.util.XMLUtils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * @author zobrenovic
 */
public class Connector {

    private ActiveRegion region1 = null;
    private ActiveRegion region2 = null;
    private String caption = "";
    private String textColor = "";
    private String fontName = "";
    private String fontStyle = "";
    private String fontSize = "";
    private String lineStyle = "";
    private String lineThickness = "";
    private String fillColor = "";
    private String lineColor = "";
    private String lineStart = "";
    private String lineEnd = "";
    private String[][] variablesMapping = {
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

    private ConnectorRenderer renderer = new ConnectorRenderer(this);

    public Connector() {
    }

    public Connector(ActiveRegion region1) {
        this.setRegion1(region1);
    }

    public Connector(ActiveRegion region1, ActiveRegion region2) {
        this.setRegion1(region1);
        this.setRegion2(region2);
    }

    public void dispose() {
        this.getRenderer().setConnector(null);
        this.setRenderer(null);
        this.setRegion1(null);
        this.setRegion2(null);
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

        str.append(prefix + "<connector region1='" + getRegion1().getDrawImageFileName() + "' region2 ='" + getRegion2().getDrawImageFileName() + "'>\n");
        if (!this.getCaption().isEmpty()) {
            str.append(prefix + "    <connector-caption>" + XMLUtils.prepareForXML(this.getCaption()) + "</connector-caption>\n");
        }
        if (!getTextColor().isEmpty()) {
            str.append(prefix + "    <connector-property name='text color'>" + XMLUtils.prepareForXML(this.getTextColor()) + "</connector-property>\n");
        }
        if (!getFontName().isEmpty()) {
            str.append(prefix + "    <connector-property name='font name'>" + XMLUtils.prepareForXML(this.getFontName()) + "</connector-property>\n");
        }
        if (!getFontStyle().isEmpty()) {
            str.append(prefix + "    <connector-property name='font style'>" + XMLUtils.prepareForXML(this.getFontStyle()) + "</connector-property>\n");
        }
        if (!getFontSize().isEmpty()) {
            str.append(prefix + "    <connector-property name='font size'>" + XMLUtils.prepareForXML(this.getFontSize()) + "</connector-property>\n");
        }
        if (!getFillColor().isEmpty()) {
            str.append(prefix + "    <connector-property name='fill color'>" + XMLUtils.prepareForXML(this.getFillColor()) + "</connector-property>\n");
        }
        if (!getLineStyle().isEmpty()) {
            str.append(prefix + "    <connector-property name='line style'>" + XMLUtils.prepareForXML(this.getLineStyle()) + "</connector-property>\n");
        }
        if (!getLineThickness().isEmpty()) {
            str.append(prefix + "    <connector-property name='line thickness'>" + XMLUtils.prepareForXML(this.getLineThickness()) + "</connector-property>\n");
        }
        if (!getLineColor().isEmpty()) {
            str.append(prefix + "    <connector-property name='line color'>" + XMLUtils.prepareForXML(this.getLineColor()) + "</connector-property>\n");
        }
        if (!getLineStart().isEmpty()) {
            str.append(prefix + "    <connector-property name='line start'>" + XMLUtils.prepareForXML(this.getLineStart()) + "</connector-property>\n");
        }
        if (!getLineEnd().isEmpty()) {
            str.append(prefix + "    <connector-property name='line end'>" + XMLUtils.prepareForXML(this.getLineEnd()) + "</connector-property>\n");
        }
        str.append(prefix + "    <connector-mappings>\n");
        for (String mapping[] : this.getVariablesMapping()) {
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
            this.setTextColor(value);
        } else if (key.equalsIgnoreCase("font name")) {
            this.setFontName(value);
        } else if (key.equalsIgnoreCase("font style")) {
            this.setFontStyle(value);
        } else if (key.equalsIgnoreCase("font size")) {
            this.setFontSize(value);
        } else if (key.equalsIgnoreCase("fill color")) {
            this.setFillColor(value);
        } else if (key.equalsIgnoreCase("line style")) {
            this.setLineStyle(value);
        } else if (key.equalsIgnoreCase("line thickness")) {
            this.setLineThickness(value);
        } else if (key.equalsIgnoreCase("line color")) {
            this.setLineColor(value);
        } else if (key.equalsIgnoreCase("line start")) {
            this.setLineStart(value);
        } else if (key.equalsIgnoreCase("line end")) {
            this.setLineEnd(value);
        }
    }

    public void updateVariables(boolean bPlayback) {
        for (String line[] : this.getVariablesMapping()) {
            String variable = line[0];
            if (!variable.isEmpty()) {
                variable = Evaluator.processText(variable, "", "");
                String strA = Evaluator.processText(line[1], "", "");
                String strB = Evaluator.processText(line[2], "", "");
                String strFormat = Evaluator.processText(line[3], "", "");
                int x1 = this.getRegion1().getCenterX(bPlayback);
                int y1 = this.getRegion1().getCenterY(bPlayback);
                int x2 = this.getRegion2().getCenterX(bPlayback);
                int y2 = this.getRegion2().getCenterY(bPlayback);
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

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(String lineStyle) {
        this.lineStyle = lineStyle;
    }

    public String getLineThickness() {
        return lineThickness;
    }

    public void setLineThickness(String lineThickness) {
        this.lineThickness = lineThickness;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public String getLineColor() {
        return lineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    public String getLineStart() {
        return lineStart;
    }

    public void setLineStart(String lineStart) {
        this.lineStart = lineStart;
    }

    public String getLineEnd() {
        return lineEnd;
    }

    public void setLineEnd(String lineEnd) {
        this.lineEnd = lineEnd;
    }

    public String[][] getVariablesMapping() {
        return variablesMapping;
    }

    public void setVariablesMapping(String[][] variablesMapping) {
        this.variablesMapping = variablesMapping;
    }

    public ConnectorRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(ConnectorRenderer renderer) {
        this.renderer = renderer;
    }
}
