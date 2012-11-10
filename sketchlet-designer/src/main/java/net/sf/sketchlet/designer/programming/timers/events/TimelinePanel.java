/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.timers.events;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class TimelinePanel extends JPanel {

    Timeline timeline;
    TimelineEvent selectedEvent = null;
    int marginH = 20;
    int marginV = 0;
    TimerEventsPanel eventsPanel;

    public TimelinePanel(Timeline timeline, TimerEventsPanel eventsPanel) {
        this.timeline = timeline;
        timeline.panel = this;
        this.eventsPanel = eventsPanel;
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
        this.selectedEvent = timeline.events.firstElement();
    }

    MouseAdapter mouseAdapter = new MouseAdapter() {

        boolean bShouldReload = false;

        public void mousePressed(MouseEvent me) {
            int x = me.getX();
            int y = me.getY();
            if (x >= 0) {
                SketchletEditor.editorPanel.saveTimerUndo(eventsPanel.timelinePanel.timeline.timer);
                selectedEvent = timeline.selectEvent(x, y, TimelinePanel.this);

                if (selectedEvent == null) {
                    int w = getWidth() - marginH * 2;
                    x -= marginH;
                    selectedEvent = timeline.addEvent((double) x / w);
                    bShouldReload = true;
                }

                if (selectedEvent != null) {
                    timeline.updateVariables(selectedEvent.relTime);
                }
                repaint();
            }
        }

        public void mouseReleased(MouseEvent me) {
            if (bShouldReload) {
                bShouldReload = false;
                eventsPanel.reload();
            } else {
                eventsPanel.reselect();
            }

            if (selectedEvent != null && selectedEvent.relTime > 0 && selectedEvent.relTime < 1 && (me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                JPopupMenu popupMenu = new JPopupMenu();
                TutorialPanel.prepare(popupMenu, true);

                JMenuItem delete = new JMenuItem("Delete");
                delete.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        if (selectedEvent != null) {
                            timeline.deleteEvent(selectedEvent);
                            eventsPanel.reload();
                            repaint();
                        }
                    }
                });

                popupMenu.add(delete);

                popupMenu.show(TimelinePanel.this, me.getX(), me.getY());
            }
            repaint();
        }

        public void mouseDragged(MouseEvent me) {
            if (selectedEvent != null && selectedEvent.relTime > 0 && selectedEvent.relTime < 1) {
                bShouldReload = true;
                int x = me.getX();
                int y = me.getY();
                if (x >= 0) {
                    int w = getWidth() - marginH * 2;
                    x -= marginH;
                    double relTime = (double) x / w;
                    if (relTime > 0 && relTime < 1) {
                        selectedEvent.relTime = relTime;
                        timeline.updateVariables(relTime);
                    }
                    repaint();
                }
            }
        }
    };

    public Dimension getPreferredSize() {
        return new Dimension(500, 40);
    }

    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g);

        int w = getWidth() - marginH * 2;
        int h = getHeight() - marginV * 2;

        g2d.setColor(Color.BLACK);

        g2d.drawRect(0, 0, getWidth() - 2, getHeight() - 2);

        g2d.setColor(new Color(66, 66, 66));

        g2d.drawLine(marginH + 0, h / 2 + marginV, w + marginH, h / 2 + marginV);

        for (int i = 1; i < 20; i++) {
            int x = marginH + w * i / 20;
            int y1 = marginV + h / 2 - 3;
            int y2 = marginV + h / 2 + 3;
            g2d.drawLine(x, y1, x, y2);
        }

        timeline.draw(g2d, this);
    }
}
