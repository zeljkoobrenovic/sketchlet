/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.programming;

/**
 *
 * @author zobrenovic
 */
public class TimerProgrammingUnit extends ProgrammingUnit {

    private String name = "";
    private String defaultCurve = "";
    private String strDurationInSec = "2.0";
    private String strPauseBefore = "";
    private String strPauseAfter = "";
    private boolean loop = false;
    private boolean pulsar = false;
    private boolean bResetAtEnd = false;
    private Object[][] variables = {
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},
        {"", "", "", "", ""},};

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDefaultCurve() {
        return defaultCurve;
    }

    public void setDefaultCurve(String defaultCurve) {
        this.defaultCurve = defaultCurve;
    }

    public String getStrDurationInSec() {
        return strDurationInSec;
    }

    public void setStrDurationInSec(String strDurationInSec) {
        this.strDurationInSec = strDurationInSec;
    }

    public String getStrPauseBefore() {
        return strPauseBefore;
    }

    public void setStrPauseBefore(String strPauseBefore) {
        this.strPauseBefore = strPauseBefore;
    }

    public String getStrPauseAfter() {
        return strPauseAfter;
    }

    public void setStrPauseAfter(String strPauseAfter) {
        this.strPauseAfter = strPauseAfter;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public boolean isPulsar() {
        return pulsar;
    }

    public void setPulsar(boolean pulsar) {
        this.pulsar = pulsar;
    }

    public boolean isbResetAtEnd() {
        return bResetAtEnd;
    }

    public void setbResetAtEnd(boolean bResetAtEnd) {
        this.bResetAtEnd = bResetAtEnd;
    }

    public Object[][] getVariables() {
        return variables;
    }

    public void setVariables(Object[][] variables) {
        this.variables = variables;
    }
}
