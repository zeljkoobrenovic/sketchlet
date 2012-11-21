package net.sf.sketchlet.designer.editor.dnd;

import java.awt.*;

/**
 * The structured representation of the string dropped internally from another part of Sketchlet
 * (variables, timers, macros...).
 * <p/>
 * User: zeljko
 * Date: 17-11-12
 * Time: 21:38
 */
public class InternalDroppedString {
    private String pastedText;
    private String action;
    private String param1;
    private String param2;
    private String tutorialInforPrefix;
    private Point pointOnScreen;

    public InternalDroppedString(String pastedText, String action, String param1, String param2, String tutorialInforPrefix, Point pointOnScreen) {
        this.pastedText = pastedText;
        this.action = action;
        this.param1 = param1;
        this.param2 = param2;
        this.tutorialInforPrefix = tutorialInforPrefix;
        this.pointOnScreen = pointOnScreen;
    }

    public String getPastedText() {
        return pastedText;
    }

    public void setPastedText(String pastedText) {
        this.pastedText = pastedText;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public String getTutorialInforPrefix() {
        return tutorialInforPrefix;
    }

    public void setTutorialInforPrefix(String tutorialInforPrefix) {
        this.tutorialInforPrefix = tutorialInforPrefix;
    }

    public Point getPointOnScreen() {
        return pointOnScreen;
    }

    public void setPointOnScreen(Point pointOnScreen) {
        this.pointOnScreen = pointOnScreen;
    }
}
