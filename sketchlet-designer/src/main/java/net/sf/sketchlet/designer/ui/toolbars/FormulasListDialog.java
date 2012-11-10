/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.toolbars;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author zobrenovic
 */
public class FormulasListDialog extends JDialog {

    JTable formulasTable;
    AbstractTableModel dataModelFormulas;
    JButton ok = new JButton(Language.translate("OK"), Workspace.createImageIcon("resources/ok.png"));
    JTextArea formulaText = new JTextArea();
    JTextField value;

    public FormulasListDialog(JFrame frame, final JTextField value) {
        super(frame, true);
        this.value = value;
        setTitle(Language.translate("Formulas"));
        dataModelFormulas = new AbstractTableModel() {

            String columnNames[] = {Language.translate("Formula"), ""};

            public int getColumnCount() {
                return columnNames.length;
            }

            public String getColumnName(int col) {
                return columnNames[col];
            }

            public int getRowCount() {
                return formulas.length;
            }

            public Object getValueAt(int row, int col) {
                switch (col) {
                    case 0:
                        return formulas[row][1];
                    case 1:
                        return formulas[row][0];
                }

                return "";
            }
        };

        this.formulasTable = new JTable(dataModelFormulas);
        formulasTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        formulasTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                int row = formulasTable.getSelectedRow();
                if (row >= 0) {
                    formulaText.setText(formulas[row][0]);
                }
            }
        });

        this.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
            }
        });

        JPanel panelControls = new JPanel();

        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String strFormula = formulaText.getText();
                if (!strFormula.isEmpty()) {
                    if (value.getText().isEmpty()) {
                        value.setText("=" + strFormula);
                    } else {
                        value.replaceSelection(strFormula);
                    }
                }
                setVisible(false);
            }
        });
        panelControls.add(ok);
        JButton cancel = new JButton(Language.translate("Cancel"), Workspace.createImageIcon("resources/cancel.png"));
        cancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                formulaText.setText("");
                setVisible(false);
            }
        });
        panelControls.add(cancel);
        this.formulasTable.setTableHeader(null);

        JScrollPane formulasPane = new JScrollPane(this.formulasTable);
        formulasPane.setPreferredSize(new Dimension(100, 250));
        JScrollPane areaPane = new JScrollPane(this.formulaText);
        areaPane.setPreferredSize(new Dimension(100, 50));

        JPanel panelNorth = new JPanel(new BorderLayout());
        panelNorth.add(formulasPane, BorderLayout.CENTER);
        panelNorth.add(areaPane, BorderLayout.SOUTH);
        this.add(panelNorth, BorderLayout.NORTH);

        this.add(panelControls, BorderLayout.SOUTH);
        setSize(350, 390);
        this.setLocationRelativeTo(frame);
        setVisible(true);
    }

    public static void main(String args[]) {
        new FormulasListDialog(null, null);
    }

    public static String[][] formulas = {
            {"sin(x)", "Sine", ""},
            {"cos(x)", "Cosine", ""},
            {"tan(x)", "Tangent", ""},
            {"asin(x)", "Arc Sine", ""},
            {"acos(x)", "Arc Cosine", ""},
            {"atan(x)", "Arc Tangent", ""},
            {"atan2(y,x)", "Arc Tangent (with 2 parameters)", ""},
            {"sinh(x)", "Hyperbolic Sine", ""},
            {"cosh(x)", "Hyperbolic Cosine", ""},
            {"tanh(x)", "Hyperbolic Tangent", ""},
            {"asinh(x)", "Inverse Hyperbolic Sine", ""},
            {"acosh(x)", "Inverse Hyperbolic Cosine", ""},
            {"atanh(x)", "Inverse Hyperbolic Tangent", ""},
            {"log(x)", "Logarithm base 10", ""},
            {"ln(x)", "Natural Logarithm", ""},
            {"exp(x)", "Exponential (e^x)", ""},
            {"pow(x)", "", ""},
            {"sqrt(x)", "Square Root", ""},
            {"abs(x)", "Absolute Value / Magnitude", ""},
            {"mod(x)", "Modulus", ""},
            {"sum(x)", "Sum", ""},
            {"max(x,y)", "Max", ""},
            {"min(x,y)", "Min", ""},
            {"rand()", "Random number (between 0 and 1)", ""},
            {"if(cond,trueval,falseval)", "If", ""},
            {"str(x)", "Number to String", ""},
            {"substring(string,start,len)", "Substring", ""},
            {"mid(string,start,len)", "Substring", ""},
            {"left(string,len)", "Left Substring", ""},
            {"right(string,len)", "Right Substring", ""},
            {"binom(n,i)", "Binomial coefficients", ""},
            {"round(x)", "Round", ""},
            {"floor(x)", "Floor", ""},
            {"ceil(x)", "Ceil", ""},};
}
