/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.timers.events;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.help.TutorialPanel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class TimerEventsPanel extends JPanel {

    public TimelinePanel timelinePanel;
    JCheckBox updateVariables = new JCheckBox(Language.translate("update variables"), true);
    JTabbedPane tabs = new JTabbedPane();

    public TimerEventsPanel(final Timeline timeline) {
        this.setLayout(new BorderLayout());
        JPanel panelNorth = new JPanel(new BorderLayout());
        timelinePanel = new TimelinePanel(timeline, this);
        panelNorth.add(timelinePanel, BorderLayout.CENTER);
        panelNorth.add(updateVariables, BorderLayout.EAST);
        add(panelNorth, BorderLayout.NORTH);
        reload();
        add(tabs);
        tabs.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabs);
        updateVariables.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(updateVariables);

        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!tabs.isShowing() || timeline.isLoading()) {
                    return;
                }

                int index = tabs.getSelectedIndex();
                if (index >= 0) {
                    timelinePanel.selectedEvent = timeline.events.elementAt(index);
                    timelinePanel.repaint();
                }
            }
        });
        TutorialPanel.prepare(tabs);
    }

    public void reload() {
        timelinePanel.timeline.loadTabs(tabs, timelinePanel);
    }

    public void reselect() {
        timelinePanel.timeline.selectTab(tabs, timelinePanel);
    }

    public static void main(String args[]) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Timeline tl = new Timeline(null, null);

        frame.add(new TimerEventsPanel(tl));

        frame.pack();
        frame.setVisible(true);
    }
}
