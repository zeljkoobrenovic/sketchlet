package net.sf.sketchlet.framework.model.programming.timers.events;

import net.sf.sketchlet.designer.editor.SketchletEditor;

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

    private Timeline timeline;
    private TimelineEvent selectedEvent = null;
    private int horizontalMargin = 20;
    private int verticalMargin = 0;
    private TimerEventsPanel eventsPanel;

    public TimelinePanel(Timeline timeline, TimerEventsPanel eventsPanel) {
        this.setTimeline(timeline);
        timeline.setPanel(this);
        this.setEventsPanel(eventsPanel);
        this.addMouseListener(mouseAdapter);
        this.addMouseMotionListener(mouseAdapter);
        this.setSelectedEvent(timeline.getEvents().firstElement());
    }

    MouseAdapter mouseAdapter = new MouseAdapter() {

        boolean bShouldReload = false;

        public void mousePressed(MouseEvent me) {
            int x = me.getX();
            int y = me.getY();
            if (x >= 0) {
                SketchletEditor.getInstance().saveTimerUndo(getEventsPanel().getTimelinePanel().getTimeline().getTimer());
                setSelectedEvent(getTimeline().selectEvent(x, y, TimelinePanel.this));

                if (getSelectedEvent() == null) {
                    int w = getWidth() - getHorizontalMargin() * 2;
                    x -= getHorizontalMargin();
                    setSelectedEvent(getTimeline().addEvent((double) x / w));
                    bShouldReload = true;
                }

                if (getSelectedEvent() != null) {
                    getTimeline().updateVariables(getSelectedEvent().relativeTime);
                }
                repaint();
            }
        }

        public void mouseReleased(MouseEvent me) {
            if (bShouldReload) {
                bShouldReload = false;
                getEventsPanel().reload();
            } else {
                getEventsPanel().reselect();
            }

            if (getSelectedEvent() != null && getSelectedEvent().relativeTime > 0 && getSelectedEvent().relativeTime < 1 && (me.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                JPopupMenu popupMenu = new JPopupMenu();

                JMenuItem delete = new JMenuItem("Delete");
                delete.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        if (getSelectedEvent() != null) {
                            getTimeline().deleteEvent(getSelectedEvent());
                            getEventsPanel().reload();
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
            if (getSelectedEvent() != null && getSelectedEvent().relativeTime > 0 && getSelectedEvent().relativeTime < 1) {
                bShouldReload = true;
                int x = me.getX();
                int y = me.getY();
                if (x >= 0) {
                    int w = getWidth() - getHorizontalMargin() * 2;
                    x -= getHorizontalMargin();
                    double relTime = (double) x / w;
                    if (relTime > 0 && relTime < 1) {
                        getSelectedEvent().relativeTime = relTime;
                        getTimeline().updateVariables(relTime);
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

        int w = getWidth() - getHorizontalMargin() * 2;
        int h = getHeight() - getVerticalMargin() * 2;

        g2d.setColor(Color.BLACK);

        g2d.drawRect(0, 0, getWidth() - 2, getHeight() - 2);

        g2d.setColor(new Color(66, 66, 66));

        g2d.drawLine(getHorizontalMargin() + 0, h / 2 + getVerticalMargin(), w + getHorizontalMargin(), h / 2 + getVerticalMargin());

        for (int i = 1; i < 20; i++) {
            int x = getHorizontalMargin() + w * i / 20;
            int y1 = getVerticalMargin() + h / 2 - 3;
            int y2 = getVerticalMargin() + h / 2 + 3;
            g2d.drawLine(x, y1, x, y2);
        }

        getTimeline().draw(g2d, this);
    }

    public int getHorizontalMargin() {
        return horizontalMargin;
    }

    public void setHorizontalMargin(int horizontalMargin) {
        this.horizontalMargin = horizontalMargin;
    }

    public int getVerticalMargin() {
        return verticalMargin;
    }

    public void setVerticalMargin(int verticalMargin) {
        this.verticalMargin = verticalMargin;
    }

    public TimelineEvent getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(TimelineEvent selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public TimerEventsPanel getEventsPanel() {
        return eventsPanel;
    }

    public void setEventsPanel(TimerEventsPanel eventsPanel) {
        this.eventsPanel = eventsPanel;
    }

    public Timeline getTimeline() {
        return timeline;
    }

    public void setTimeline(Timeline timeline) {
        this.timeline = timeline;
    }
}
