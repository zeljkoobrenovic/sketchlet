package net.sf.sketchlet.framework.model.programming.timers;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.ProgressMonitor;
import net.sf.sketchlet.designer.editor.ui.timers.TimerPanel;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.programming.timers.events.Timeline;
import net.sf.sketchlet.programming.TimerProgrammingUnit;
import net.sf.sketchlet.util.XMLUtils;

import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Timer extends TimerProgrammingUnit {

    private TimerPanel panel;
    private String[] columns = {Language.translate("Variable"), Language.translate("Start value"), Language.translate("End value"), Language.translate("Format"), Language.translate("Curve")}; // , "Test Value"};
    private Timeline timeline = new Timeline(this, null);
    private int tabIndex = 0;

    public Timer() {
    }

    public Timer getUndoCopy() {
        Timer timer = new Timer();

        timer.setName(this.getName());
        timer.setStrDurationInSec(this.getStrDurationInSec());
        timer.setStrPauseBefore(this.getStrPauseBefore());
        timer.setStrPauseAfter(this.getStrPauseAfter());
        timer.setPulsar(this.isPulsar());
        timer.setbResetAtEnd(this.isbResetAtEnd());
        timer.setDefaultCurve(this.getDefaultCurve());
        timer.setLoop(this.isLoop());

        Page.copyArray(this.getVariables(), timer.getVariables());
        timer.setTimeline(this.getTimeline().getUndoCopy());

        return timer;
    }

    public void restore(Timer t) {
        this.setName(t.getName());
        this.setStrDurationInSec(t.getStrDurationInSec());
        this.setStrPauseBefore(t.getStrPauseBefore());
        this.setStrPauseAfter(t.getStrPauseAfter());
        this.setPulsar(t.isPulsar());
        this.setbResetAtEnd(t.isbResetAtEnd());
        this.setDefaultCurve(t.getDefaultCurve());
        this.setLoop(t.isLoop());
        this.getTimeline().restore(t.getTimeline());

        Page.copyArray(t.getVariables(), this.getVariables());
    }

    public TimerThread startThread(String strParams, Vector<TimerThread> activeTimers) {
        return new TimerThread(this, strParams, activeTimers, true);
    }

    public TimerThread startThread(String strParams, Vector<TimerThread> activeTimers, ProgressMonitor progressMonitor) {
        return new TimerThread(this, strParams, activeTimers, progressMonitor, true);
    }

    public void save(PrintWriter out) {
        if (getPanel() != null) {
            try {
                String oldName = this.getName();
                this.setName(this.getPanel().fieldName.getText());
                this.setDefaultCurve((String) getPanel().comboCurve.getSelectedItem());
                if (this.getDefaultCurve() == null) {
                    this.setDefaultCurve("");
                }
                if (!this.getName().equals(oldName)) {
                    SketchletEditor.getInstance().getPages().replaceReferences("Start Timer", oldName, this.getName());
                }
                this.setStrDurationInSec(this.getPanel().fieldDuration.getText());
                this.setStrPauseBefore(this.getPanel().fieldWait.getText());
                this.setStrPauseAfter(this.getPanel().fieldWaitAfter.getText());
                // this.resolutionPerSec = Double.parseDouble(this.panel.fieldResolution.getText());
                this.setLoop(getPanel().checkBoxLoop.isSelected());
                this.setPulsar(getPanel().checkBoxPulsar.isSelected());
                this.setbResetAtEnd(getPanel().checkBoxReset.isSelected());
            } catch (Exception e) {
            }
        }

        out.println("<timer>");
        out.println("<name>" + this.getName() + "</name>");
        out.println("<duration>" + XMLUtils.prepareForXML(this.getStrDurationInSec()) + "</duration>");
        out.println("<pause-before>" + XMLUtils.prepareForXML(this.getStrPauseBefore()) + "</pause-before>");
        out.println("<pause-after>" + XMLUtils.prepareForXML(this.getStrPauseAfter()) + "</pause-after>");
        out.println("<loop>" + this.isLoop() + "</loop>");
        out.println("<pulsar>" + this.isPulsar() + "</pulsar>");
        out.println("<default-curve>" + this.getDefaultCurve() + "</default-curve>");
        out.println("<reset-at-end>" + this.isbResetAtEnd() + "</reset-at-end>");
        out.println("<tab-index>" + this.getTabIndex() + "</tab-index>");
        for (int i = 0; i < getVariables().length; i++) {
            boolean bShouldSave = false;
            for (int j = 0; j < getVariables()[i].length; j++) {
                if (!((String) getVariables()[i][j]).isEmpty()) {
                    bShouldSave = true;
                    break;
                }
            }

            if (!bShouldSave) {
                continue;
            }

            out.println("    <variable>");
            out.println("      <name>" + getVariables()[i][0] + "</name>");
            out.println("      <start>" + getVariables()[i][1] + "</start>");
            out.println("      <end>" + getVariables()[i][2] + "</end>");
            out.println("      <format>" + getVariables()[i][3] + "</format>");
            out.println("      <curve>" + getVariables()[i][4] + "</curve>");
            out.println("    </variable>");
        }

        getTimeline().save(out);

        out.println("</timer>");
    }

    public TimerPanel getPanel() {
        return panel;
    }

    public void setPanel(TimerPanel panel) {
        this.panel = panel;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }

    public int getTabIndex() {
        return tabIndex;
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }
}
