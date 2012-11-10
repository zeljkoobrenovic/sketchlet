/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.template;

/**
 *
 * @author zobrenovic
 */
public enum TemplateMarkers {

    VELOCITY("${", "}"), JSP("<%=", "%>");
    private String start = "";
    private String end = "";

    private TemplateMarkers(String start, String end) {
        this.start = start;
        this.end = end;
    }

    public String start() {
        return start;
    }

    public String end() {
        return end;
    }

    public static boolean isStart(String start) {
        for (TemplateMarkers m : TemplateMarkers.values()) {
            if (m.start().equals(start)) {
                return true;
            }
        }
        return false;
    }

    public static boolean startsWithMarker(String start) {
        for (TemplateMarkers m : TemplateMarkers.values()) {
            if (start.startsWith(m.start())) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsStartMarker(String value) {
        for (TemplateMarkers m : TemplateMarkers.values()) {
            if (value.contains(m.start())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isEnd(String start) {
        for (TemplateMarkers m : TemplateMarkers.values()) {
            if (m.end().equals(start)) {
                return true;
            }
        }
        return false;
    }
}
