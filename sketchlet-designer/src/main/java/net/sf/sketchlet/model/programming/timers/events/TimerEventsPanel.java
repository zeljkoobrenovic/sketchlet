/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.model.programming.timers.events;

import net.sf.sketchlet.common.translation.Language;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class TimerEventsPanel extends JPanel {

    private TimelinePanel timelinePanel;
    private JCheckBox updateVariablesCheckbox = new JCheckBox(Language.translate("update variables"), true);
    private JTabbedPane tabs = new JTabbedPane();

    public TimerEventsPanel(final Timeline timeline) {
        this.setLayout(new BorderLayout());
        JPanel panelNorth = new JPanel(new BorderLayout());
        setTimelinePanel(new TimelinePanel(timeline, this));
        panelNorth.add(getTimelinePanel(), BorderLayout.CENTER);
        panelNorth.add(getUpdateVariablesCheckbox(), BorderLayout.EAST);
        add(panelNorth, BorderLayout.NORTH);
        reload();
        add(tabs);
        tabs.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabs);
        getUpdateVariablesCheckbox().putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(getUpdateVariablesCheckbox());

        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!tabs.isShowing() || timeline.isLoading()) {
                    return;
                }

                int index = tabs.getSelectedIndex();
                if (index >= 0) {
                    getTimelinePanel().setSelectedEvent(timeline.getEvents().elementAt(index));
                    getTimelinePanel().repaint();
                }
            }
        });
    }

    public void reload() {
        getTimelinePanel().getTimeline().loadTabs(tabs, getTimelinePanel());
    }

    public void reselect() {
        getTimelinePanel().getTimeline().selectTab(tabs, getTimelinePanel());
    }

    public static void main(String args[]) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Timeline tl = new Timeline(null, null);

        frame.add(new TimerEventsPanel(tl));

        frame.pack();
        frame.setVisible(true);
    }

    public TimelinePanel getTimelinePanel() {
        return timelinePanel;
    }

    public void setTimelinePanel(TimelinePanel timelinePanel) {
        this.timelinePanel = timelinePanel;
    }

    public JCheckBox getUpdateVariablesCheckbox() {
        return updateVariablesCheckbox;
    }
}
