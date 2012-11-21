/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui.wizard;

import org.netbeans.spi.wizard.WizardPage;

import java.awt.*;

public class ActionParamPage extends WizardPage {

    SketchPanel sketchPanel = new SketchPanel(this);
    VariablePanel variablePanel = new VariablePanel(this);
    ActiveRegionPanel activeRegionPanel = new ActiveRegionPanel(this);
    ActiveRegionAnimatePanel activeRegionAnimatePanel = new ActiveRegionAnimatePanel(this);
    TimerPanel timerPanel = new TimerPanel(this);
    MacroPanel macroPanel = new MacroPanel(this);
    int nPanel = 0;
    String action = "";
    String param1 = "";
    String param2 = "";

    public ActionParamPage() {
        setLayout(new BorderLayout());
    }

    public void setPanel(int nPanel) {
        this.nPanel = nPanel;
        this.removeAll();
        switch (nPanel) {
            case 0:
                this.action = "Go to page";
                add(sketchPanel);
                break;
            case 1:
                this.action = "Variable update";
                add(variablePanel);
                break;
            case 2:
                this.action = "Variable increment";
                add(variablePanel);
                break;
            case 3:
                this.action = "Variable append";
                add(variablePanel);
                break;
            case 4:
                this.action = "Region";
                add(activeRegionPanel);
                break;
            case 5:
                this.action = "Region Animate";
                add(activeRegionAnimatePanel);
                break;
            case 6:
                this.action = "Start Timer";
                add(timerPanel);
                break;
            case 7:
                this.action = "Pause Timer";
                add(timerPanel);
                break;
            case 8:
                this.action = "Stop Timer";
                add(timerPanel);
                break;
            case 9:
                this.action = "Start Action";
                add(macroPanel);
                break;
            case 10:
                this.action = "Stop Action";
                add(macroPanel);
                break;
        }
        revalidate();
    }

    public static final String getDescription() {
        return "Define action parameters";
    }

    protected String validateContents(Component comp, Object o) {
        switch (nPanel) {
            case 0:
                return sketchPanel.validateContents(comp, o);
            case 1:
            case 2:
            case 3:
                return variablePanel.validateContents(comp, o);
            case 4:
                return activeRegionPanel.validateContents(comp, o);
            case 5:
                return activeRegionAnimatePanel.validateContents(comp, o);
            case 6:
            case 7:
                return timerPanel.validateContents(comp, o);
            case 8:
            case 9:
                return macroPanel.validateContents(comp, o);
        }
        return null;
    }
}

