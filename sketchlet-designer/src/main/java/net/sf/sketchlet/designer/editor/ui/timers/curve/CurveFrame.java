/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui.timers.curve;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.model.programming.timers.curves.Curve;
import net.sf.sketchlet.model.programming.timers.curves.CurveSegment;
import net.sf.sketchlet.model.programming.timers.curves.StiffnessSegment;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class CurveFrame extends JPanel {

    public AbstractTableModel model;
    public AbstractTableModel modelStiffness;
    public JTable table;
    public JTable tableStiffness;
    public CurvePanel curvePanel;
    public StiffnessPanel stiffnessPanel;
    public CurvePreviewPanel curvePreviewPanel;
    public JCheckBox checkSpeed = new JCheckBox(Language.translate("Show Speed"), false);
    public JTextField stepTime = new JTextField("0.05", 5);
    public JTextField stepValue = new JTextField("0.05", 5);
    public JButton btnSimple = new JButton(Language.translate("Simplify"));
    public JButton delete = new JButton(Language.translate("Delete"));
    public JButton newStiffnessSegment = new JButton(Language.translate("New Segment"));
    public JButton deleteStiffness = new JButton(Language.translate("Delete"));
    public Curve curve;
    public JTextField nameField = new JTextField(12);
    Vector<CurveSegment> original_segments;

    public CurveFrame(final Curve curve) {
        original_segments = curve.getSegments();
        this.curve = curve;
        setLayout(new BorderLayout());
        nameField.setText(curve.getName());
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel(Language.translate("Name ")));
        namePanel.add(nameField);
        // final JTabbedPane centerPanel = new JTabbedPane();
        final JTabbedPane tabs = new JTabbedPane();
        model = curve.getTableModel();
        modelStiffness = curve.getStiffnessCurve().getTableModel();
        table = new JTable(model);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tableStiffness = new JTable(modelStiffness);
        tableStiffness.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JScrollPane scrollTable = new JScrollPane(table);
        JScrollPane scrollTableStiffness = new JScrollPane(tableStiffness);
        JPanel panelSegmentButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSegmentButtons.add(delete);
        JPanel panelSegmentButtonsStiffness = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSegmentButtonsStiffness.add(newStiffnessSegment);
        panelSegmentButtonsStiffness.add(deleteStiffness);

        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.add(scrollTable);
        panel1.add(panelSegmentButtons, BorderLayout.EAST);
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(scrollTableStiffness);
        panel2.add(panelSegmentButtonsStiffness, BorderLayout.EAST);


        // centerPanel.addTab("Curve Segments", panel1);
        // centerPanel.addTab("Stiffness Segments", panel2);
        curvePanel = new CurvePanel(this, curve);
        stiffnessPanel = new StiffnessPanel(this, curve.getStiffnessCurve());
        delete.setEnabled(false);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                int row = table.getSelectedRow();
                if (row >= 0 && row < curve.getSegments().size() - 1) {
                    curvePanel.selectedSegment = curve.getSegments().elementAt(row);
                    delete.setEnabled(true);
                    curvePanel.repaint();
                } else {
                    delete.setEnabled(false);
                }
            }
        });
        deleteStiffness.setEnabled(false);
        tableStiffness.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                int row = tableStiffness.getSelectedRow();
                if (row >= 0 && row < curve.getStiffnessCurve().getSegments().size() - 1) {
                    deleteStiffness.setEnabled(true);
                    stiffnessPanel.repaint();
                } else {
                    deleteStiffness.setEnabled(false);
                }
            }
        });

        btnSimple.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                simplify();
                repaint();
            }
        });
        checkSpeed.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                curvePanel.repaint();
            }
        });

        JPanel panelNorth = new JPanel(new BorderLayout());

        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkPanel.add(checkSpeed);
        checkPanel.add(new JLabel("     "));
        checkPanel.add(this.btnSimple);
        checkPanel.add(new JLabel(Language.translate("  min dt=")));
        checkPanel.add(this.stepTime);
        checkPanel.add(new JLabel(Language.translate(" min dv=")));
        checkPanel.add(this.stepValue);
        // panelNorth.add(topPanel, BorderLayout.SOUTH);

        JPanel stiffnessPanelEx = new JPanel(new BorderLayout());
        stiffnessPanelEx.add(stiffnessPanel, BorderLayout.CENTER);
        JPanel curvePanelEx = new JPanel(new BorderLayout());
        curvePanelEx.add(curvePanel, BorderLayout.CENTER);
        curvePanelEx.add(checkPanel, BorderLayout.SOUTH);
        JPanel panel3 = new JPanel(new BorderLayout());
        panel3.add(curvePanelEx);
        panel3.add(panel1, BorderLayout.SOUTH);
        tabs.add(Language.translate("Curve"), panel3);
        curvePreviewPanel = new CurvePreviewPanel(this, curve, 10.0);
        JPanel curvePreviewPanelEx = new JPanel(new BorderLayout());
        curvePreviewPanelEx.add(curvePreviewPanel, BorderLayout.NORTH);
        //curvePreviewPanelEx.add(new JPanel(), BorderLayout.SOUTH);
        JPanel durationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JTextField durationField = new JTextField("10.0", 6);
        durationPanel.add(new JLabel(Language.translate("Duration: ")));
        durationPanel.add(durationField);
        durationPanel.add(new JLabel(Language.translate(" seconds  ")));
        JButton refresh = new JButton(Language.translate("Refresh"));
        refresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                try {
                    double duration = Double.parseDouble(durationField.getText());
                    curvePreviewPanel.duration = duration;
                    curvePreviewPanel.repaint();
                } catch (Exception e) {
                }
            }
        });
        durationPanel.add(refresh);
        curvePreviewPanelEx.add(durationPanel, BorderLayout.CENTER);
        JPanel panel4 = new JPanel(new BorderLayout());
        panel4.add(stiffnessPanelEx);
        panel4.add(panel2, BorderLayout.SOUTH);
        tabs.add(Language.translate("Constraints"), panel4);
        tabs.add(Language.translate("Real Time Preview"), curvePreviewPanelEx);

        panelNorth.add(namePanel, BorderLayout.NORTH);
        panelNorth.add(tabs, BorderLayout.CENTER);

        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (curvePanel.selectedSegment != null) {
                    curve.getSegments().remove(curvePanel.selectedSegment);
                    curvePanel.selectedSegment = null;
                    repaint();
                }
            }
        });

        deleteStiffness.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableStiffness.getSelectedRow();
                if (row >= 0) {
                    curve.getStiffnessCurve().getSegments().remove(row);
                    curve.getStiffnessCurve().getPanel().repaint();
                    modelStiffness.fireTableRowsDeleted(row, row);
                }
            }
        });

        newStiffnessSegment.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                double p = 0.5;

                if (curve.getStiffnessCurve().getSegments().size() > 0) {
                    StiffnessSegment ls = curve.getStiffnessCurve().getSegments().firstElement();
                    p = ls.getEndTime() / 2;
                }

                StiffnessSegment ss = new StiffnessSegment();
                ss.setEndTime(p);
                if (curve.getStiffnessCurve().getSegments().size() == 0) {
                    curve.getStiffnessCurve().getSegments().add(ss);
                } else {
                    curve.getStiffnessCurve().getSegments().insertElementAt(ss, 0);
                }
                modelStiffness.fireTableDataChanged();
                curve.getStiffnessCurve().getPanel().repaint();
            }
        });

        add(panelNorth, BorderLayout.NORTH);

        setSize(500, 550);
    }

    public void simplify() {
        double st = 0.0;
        double sv = 0.0;
        try {
            st = Double.parseDouble(this.stepTime.getText());
        } catch (Exception e) {
        }
        try {
            sv = Double.parseDouble(this.stepValue.getText());
        } catch (Exception e) {
        }

        if (st > 0 || sv > 0) {
            Vector<CurveSegment> segments = new Vector<CurveSegment>();
            double pt = 0.0;
            double pv = 0.0;
            for (CurveSegment cs : original_segments) {
                if (original_segments.lastElement() == cs) {
                    segments.add(cs);
                } else if ((st > 0 && cs.getEndTime() - pt >= st) || (sv > 0 && cs.getRelativeValue() - pv >= sv)) {
                    segments.add(cs);
                    pt = cs.getEndTime();
                    pv = cs.getRelativeValue();
                }
            }

            curve.setSegments(segments);
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
