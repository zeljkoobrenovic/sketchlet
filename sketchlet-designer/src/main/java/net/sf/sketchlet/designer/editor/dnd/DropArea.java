package net.sf.sketchlet.designer.editor.dnd;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import org.apache.commons.lang.StringUtils;

import java.awt.*;

/**
 * @author zeljko
 */
public class DropArea {
    private Image icon;
    private String text;
    private int width = 24;
    private int height = 24;
    private boolean acceptVariablesEnabled = true;
    private boolean acceptMacrosEnabled = true;
    private boolean acceptTimersEnabled = true;
    private boolean acceptPagesEnabled = true;
    private String profiles = "";

    private InternallyDroppedRunnable runnable;

    public DropArea() {
    }

    public DropArea(Image icon, String text, int width, int height, String profiles, InternallyDroppedRunnable runnable) {
        this.icon = icon;
        this.text = text;
        this.width = width;
        this.height = height;
        this.profiles = profiles;
        this.runnable = runnable;
    }

    public static InternalDroppedString processInternallyDroppedString(Point pointOnScreen, String strText) {
        String action = "", param1 = "", param2 = "", tutorialTextPrefix = "";
        if (strText.startsWith("=")) {
            action = "Variable update";
            param1 = strText.substring(1);
            param2 = "";
            tutorialTextPrefix = "Drag the variable, and drop it on ";
        } else if (strText.startsWith("@timer ")) {
            action = "Start timer";
            param1 = strText.substring(7);
            param2 = "";
            tutorialTextPrefix = "Drag the timer, and drop it on ";
        } else if (strText.startsWith("@sketch ")) {
            action = "Go to page";
            param1 = strText.substring(8);
            param2 = "";
            tutorialTextPrefix = "Drag the page, and drop it on ";
        } else if (strText.startsWith("@macro ")) {
            action = "Start Action";
            param1 = strText.substring(7);
            param2 = "";
            tutorialTextPrefix = "Drag the action, and drop it on ";
        }

        return new InternalDroppedString(strText, action, param1, param2, tutorialTextPrefix, pointOnScreen);
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isAcceptVariablesEnabled() {
        return acceptVariablesEnabled;
    }

    public void setAcceptVariablesEnabled(boolean acceptVariablesEnabled) {
        this.acceptVariablesEnabled = acceptVariablesEnabled;
    }

    public boolean isAcceptMacrosEnabled() {
        return acceptMacrosEnabled;
    }

    public void setAcceptMacrosEnabled(boolean acceptMacrosEnabled) {
        this.acceptMacrosEnabled = acceptMacrosEnabled;
    }

    public boolean isAcceptTimersEnabled() {
        return acceptTimersEnabled;
    }

    public void setAcceptTimersEnabled(boolean acceptTimersEnabled) {
        this.acceptTimersEnabled = acceptTimersEnabled;
    }

    public boolean isAcceptPagesEnabled() {
        return acceptPagesEnabled;
    }

    public void setAcceptPagesEnabled(boolean acceptPagesEnabled) {
        this.acceptPagesEnabled = acceptPagesEnabled;
    }

    public String getProfiles() {
        return profiles;
    }

    public void setProfiles(String profiles) {
        this.profiles = profiles;
    }

    public boolean isActive() {
        return acceptDroppedString() && (StringUtils.isBlank(profiles) || Profiles.isActive(profiles));
    }

    private boolean acceptDroppedString() {
        String strText = FileDrop.getCurrentString();
        if (strText.startsWith("=")) {
            return isAcceptVariablesEnabled();
        } else if (strText.startsWith("@timer ")) {
            return isAcceptTimersEnabled();
        } else if (strText.startsWith("@sketch ")) {
            return isAcceptPagesEnabled();
        } else if (strText.startsWith("@macro ")) {
            return isAcceptMacrosEnabled();
        } else {
            return true;
        }
    }

    public InternallyDroppedRunnable getRunnable() {
        return runnable;
    }

    public void setRunnable(InternallyDroppedRunnable runnable) {
        this.runnable = runnable;
    }
}
