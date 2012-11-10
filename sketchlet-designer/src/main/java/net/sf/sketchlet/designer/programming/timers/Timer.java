/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.programming.timers;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.timers.events.Timeline;
import net.sf.sketchlet.designer.ui.ProgressMonitor;
import net.sf.sketchlet.designer.ui.timers.TimerPanel;
import net.sf.sketchlet.programming.TimerProgrammingUnit;
import net.sf.sketchlet.util.XMLUtils;

import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Timer extends TimerProgrammingUnit {

    public TimerPanel panel;
    public String columns[] = {Language.translate("Variable"), Language.translate("Start value"), Language.translate("End value"), Language.translate("Format"), Language.translate("Curve")}; // , "Test Value"};
    public Timeline timeline = new Timeline(this, null);
    public int tabIndex = 0;

    public Timer() {
    }

    public Timer getUndoCopy() {
        Timer timer = new Timer();

        timer.name = this.name;
        timer.strDurationInSec = this.strDurationInSec;
        timer.strPauseBefore = this.strPauseBefore;
        timer.strPauseAfter = this.strPauseAfter;
        timer.pulsar = this.pulsar;
        timer.bResetAtEnd = this.bResetAtEnd;
        timer.defaultCurve = this.defaultCurve;
        timer.loop = this.loop;

        Page.copyArray(this.variables, timer.variables);
        timer.timeline = this.timeline.getUndoCopy();

        return timer;
    }

    public void restore(Timer t) {
        this.name = t.name;
        this.strDurationInSec = t.strDurationInSec;
        this.strPauseBefore = t.strPauseBefore;
        this.strPauseAfter = t.strPauseAfter;
        this.pulsar = t.pulsar;
        this.bResetAtEnd = t.bResetAtEnd;
        this.defaultCurve = t.defaultCurve;
        this.loop = t.loop;
        this.timeline.restore(t.timeline);

        Page.copyArray(t.variables, this.variables);
    }

    public TimerThread startThread(String strParams, Vector<TimerThread> activeTimers) {
        return new TimerThread(this, strParams, activeTimers, true);
    }

    public TimerThread startThread(String strParams, Vector<TimerThread> activeTimers, ProgressMonitor progressMonitor) {
        return new TimerThread(this, strParams, activeTimers, progressMonitor, true);
    }

    public void save(PrintWriter out) {
        if (panel != null) {
            try {
                String oldName = this.name;
                this.name = this.panel.fieldName.getText();
                this.defaultCurve = (String) panel.comboCurve.getSelectedItem();
                if (this.defaultCurve == null) {
                    this.defaultCurve = "";
                }
                if (!this.name.equals(oldName)) {
                    SketchletEditor.editorPanel.pages.replaceReferences("Start Timer", oldName, this.name);
                }
                this.strDurationInSec = this.panel.fieldDuration.getText();
                this.strPauseBefore = this.panel.fieldWait.getText();
                this.strPauseAfter = this.panel.fieldWaitAfter.getText();
                // this.resolutionPerSec = Double.parseDouble(this.panel.fieldResolution.getText());
                this.loop = panel.checkBoxLoop.isSelected();
                this.pulsar = panel.checkBoxPulsar.isSelected();
                this.bResetAtEnd = panel.checkBoxReset.isSelected();
            } catch (Exception e) {
            }
        }

        out.println("<timer>");
        out.println("<name>" + this.name + "</name>");
        out.println("<duration>" + XMLUtils.prepareForXML(this.strDurationInSec) + "</duration>");
        out.println("<pause-before>" + XMLUtils.prepareForXML(this.strPauseBefore) + "</pause-before>");
        out.println("<pause-after>" + XMLUtils.prepareForXML(this.strPauseAfter) + "</pause-after>");
        out.println("<loop>" + this.loop + "</loop>");
        out.println("<pulsar>" + this.pulsar + "</pulsar>");
        out.println("<default-curve>" + this.defaultCurve + "</default-curve>");
        out.println("<reset-at-end>" + this.bResetAtEnd + "</reset-at-end>");
        out.println("<tab-index>" + this.tabIndex + "</tab-index>");
        for (int i = 0; i < variables.length; i++) {
            boolean bShouldSave = false;
            for (int j = 0; j < variables[i].length; j++) {
                if (!((String) variables[i][j]).isEmpty()) {
                    bShouldSave = true;
                    break;
                }
            }

            if (!bShouldSave) {
                continue;
            }

            out.println("    <variable>");
            out.println("      <name>" + variables[i][0] + "</name>");
            out.println("      <start>" + variables[i][1] + "</start>");
            out.println("      <end>" + variables[i][2] + "</end>");
            //out.println("      <prefix>" + variables[i][3] + "</prefix>");
            //out.println("      <postfix>" + variables[i][4] + "</postfix>");
            out.println("      <format>" + variables[i][3] + "</format>");
            out.println("      <curve>" + variables[i][4] + "</curve>");
            out.println("    </variable>");
        }

        timeline.save(out);

        out.println("</timer>");
    }
}
