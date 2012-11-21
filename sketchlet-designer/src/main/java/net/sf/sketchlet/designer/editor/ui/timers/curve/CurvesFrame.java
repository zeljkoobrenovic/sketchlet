/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.timers.curve;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.model.programming.timers.curves.Curve;
import net.sf.sketchlet.model.programming.timers.curves.Curves;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author zobrenovic
 */
public class CurvesFrame extends JFrame {

    public static CurvesFrame frame;
    public JTabbedPane tabs = new JTabbedPane();
    JButton newCurve = new JButton(Language.translate("New Curve"), Workspace.createImageIcon("resources/curve.png", ""));
    JButton save = new JButton(Language.translate("Save"), Workspace.createImageIcon("resources/ok.png", ""));
    JButton deleteCurveBtn = new JButton(Language.translate("Delete"), Workspace.createImageIcon("resources/user-trash.png"));

    public CurvesFrame() {
        this.setIconImage(Workspace.createImageIcon("resources/curve.png").getImage());
        this.setTitle(Language.translate("Time Curves"));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        // this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel btnPanel = new JPanel();
        JPanel btnPanelDown = new JPanel(new BorderLayout());
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(newCurve);
        btnPanelDown.add(save, BorderLayout.WEST);
        btnPanel.add(deleteCurveBtn);

        load();

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
                HelpUtils.openHelpFile(Language.translate("Timers Curves"), "timer_curves");
            }
        });
        help.setToolTipText(Language.translate("What are timer curves?"));
        toolbarPin.add(help);
        btnPanelDown.add(toolbarPin, BorderLayout.EAST);
        getContentPane().add(btnPanel, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(btnPanelDown, BorderLayout.SOUTH);
        newCurve.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                newCurve();
            }
        });
        save.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                save();
                hideFrame();
            }
        });
        this.deleteCurveBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                deleteCurve(tabs.getSelectedIndex());
            }
        });
        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                frame = null;
            }
        });

        setSize(500, 700);
        this.setLocationRelativeTo(SketchletEditor.editorFrame);
    }

    public Curve newCurve() {
        Curve c = Curves.getGlobalCurves().addNewCurve();
        tabs.add(c.getName(), new CurveFrame(c));
        tabs.setSelectedIndex(tabs.getTabCount() - 1);

        return c;
    }

    public void save() {
        Curves.getGlobalCurves().save();
        int i = 0;
        for (Curve c : Curves.getGlobalCurves().getCurves()) {
            tabs.setTitleAt(i++, c.getName());
        }
    }

    public void deleteCurve(int index) {
        if (index >= 0 && index < Curves.getGlobalCurves().getCurves().size()) {
            Curves.getGlobalCurves().getCurves().remove(index);
            load();
        }
    }

    public void load() {
        tabs.removeAll();
        int i = 0;
        for (Curve c : Curves.getGlobalCurves().getCurves()) {
            CurveFrame p = new CurveFrame(c);
            tabs.add(c.getName(), p);
            i++;
        }
    }

    public static void showFrame() {
        if (frame == null) {
            frame = new CurvesFrame();
        }

        frame.setVisible(true);
    }

    public static void hideFrame() {
        if (frame != null) {
            frame.setVisible(false);
            frame = null;
        }
    }

    public static void main(String args[]) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            CurvesFrame.showFrame();
        } catch (Exception e) {
        }
    }
}
