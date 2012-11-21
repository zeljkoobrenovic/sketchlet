/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui.timers;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.ui.timers.curve.CurvesFrame;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.model.programming.timers.TimerThread;
import net.sf.sketchlet.model.programming.timers.Timers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class TimersFrame extends JDialog {

    public JTabbedPane tabs = new JTabbedPane();
    JButton newTimer = new JButton("New Timer", Workspace.createImageIcon("resources/timer.png", ""));
    JButton save = new JButton("Save", Workspace.createImageIcon("resources/ok.png", ""));
    JButton deleteTimerBtn = new JButton("Delete", Workspace.createImageIcon("resources/user-trash.png"));
    JButton btnCurves = new JButton("Edit Timer Curves", Workspace.createImageIcon("resources/curve.png"));
    Vector<TimerThread> testTimers = new Vector<TimerThread>();

    public TimersFrame() {
        setTitle("Timers");
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        JPanel btnPanel = new JPanel();
        JPanel btnPanelDown = new JPanel(new BorderLayout());
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(newTimer);
        btnPanelDown.add(save, BorderLayout.WEST);
        btnPanel.add(deleteTimerBtn);
        btnPanel.add(btnCurves);
        this.setIconImage(Workspace.createImageIcon("resources/timer.png").getImage());
        //frame = this;
        newTimer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                //newTimer();
            }
        });
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                for (TimerThread tt : testTimers) {
                    tt.stop();
                }
                testTimers.removeAllElements();
                //save();
                //hideTimers();
            }
        });
        this.deleteTimerBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                //deleteTimer(tabs.getSelectedIndex());
            }
        });
        btnCurves.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                CurvesFrame.showFrame();
            }
        });

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                for (TimerThread tt : testTimers) {
                    tt.stop();
                }
                testTimers.removeAllElements();
                Timers.getGlobalTimers().save();
                //frame = null;
            }
        });
        final JButton alwaysOnTopBtn = new JButton(Workspace.createImageIcon("resources/pin_up.png", ""));
        alwaysOnTopBtn.setActionCommand("pin");
        alwaysOnTopBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (alwaysOnTopBtn.getActionCommand().equals("pin")) {
                    alwaysOnTopBtn.setActionCommand("unpin");
                    alwaysOnTopBtn.setIcon(Workspace.createImageIcon("resources/pin_down.png", ""));
                    setAlwaysOnTop(true);
                } else {
                    alwaysOnTopBtn.setActionCommand("pin");
                    alwaysOnTopBtn.setIcon(Workspace.createImageIcon("resources/pin_up.png", ""));
                    setAlwaysOnTop(false);
                }
            }
        });
        JToolBar toolbarPin = new JToolBar();
        toolbarPin.setFloatable(false);
        toolbarPin.add(alwaysOnTopBtn);
        final JButton help = new JButton("", Workspace.createImageIcon("resources/help-browser.png", ""));
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Timers", "timers");
            }
        });
        help.setToolTipText("What are timers?");
        toolbarPin.add(help);
        btnPanelDown.add(toolbarPin, BorderLayout.EAST);
        getContentPane().add(btnPanel, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(btnPanelDown, BorderLayout.SOUTH);
        // load();
        pack(); // setSize(550, 500);
    }

    /*
    public Timer newTimer() {
        Timer t = Timers.globalTimers.addNewTimer();
        tabs.add(t.name, new TimerPanel(t, frame, Timers.globalTimers.timers.size() - 1));
        tabs.setSelectedIndex(tabs.getTabCount() - 1);

        return t;

    }

    public void save() {
        Timers.globalTimers.save();
        int i = 0;
        for (Timer t : Timers.globalTimers.timers) {
            tabs.setTitleAt(i++, t.name);
        }
    }

    public void deleteTimer(int index) {
        if (index >= 0 && index < Timers.globalTimers.timers.size()) {
            Timers.globalTimers.timers.remove(index);
            load();
        }
    }

    public void load() {
        tabs.removeAll();
        int i = 0;
        for (Timer t : Timers.globalTimers.timers) {
            TimerPanel p = new TimerPanel(t, frame, i);
            tabs.add(t.name, p);
            i++;
        }
    }
    public static TimersFrame frame;

    public static void showTimers(boolean bModal) {
        if (frame == null) {
            frame = new TimersFrame();
        }
        frame.setLocationRelativeTo(FreeHand.freeHandFrame);
        frame.setModal(bModal);
        frame.setVisible(true);
    }

    public static void showTimers(boolean bModal, int index) {
        if (frame == null) {
            frame = new TimersFrame();
        }
        frame.setLocationRelativeTo(FreeHand.freeHandFrame);
        frame.setModal(bModal);
        frame.load();
        if (index >= 0 && index < frame.tabs.getTabCount()) {
            frame.tabs.setSelectedIndex(index);
        }
        frame.setVisible(true);
    }

    public static void showTimers(String strName, boolean bModal) {
        if (frame == null) {
            frame = new TimersFrame();
        }
        frame.setLocationRelativeTo(FreeHand.freeHandFrame);

        for (int i = 0; i < Timers.globalTimers.timers.size(); i++) {
            Timer t = Timers.globalTimers.timers.elementAt(i);
            if (t.name.equalsIgnoreCase(strName)) {
                frame.tabs.setSelectedIndex(i);
                break;
            }
        }

        frame.setModal(bModal);
        frame.setVisible(true);
    }

    public static void hideTimers() {
        if (frame != null && frame.isVisible()) {
            frame.save();
            frame.setVisible(false);
        }
        frame = null;
    }

    public static void main(String args[]) {
        showTimers(false);
    }
     */
}
