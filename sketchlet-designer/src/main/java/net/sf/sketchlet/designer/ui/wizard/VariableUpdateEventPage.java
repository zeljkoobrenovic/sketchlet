/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.wizard;

import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.util.SpringUtilities;
import org.netbeans.spi.wizard.WizardPage;

import javax.swing.*;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class VariableUpdateEventPage extends WizardPage {

    JComboBox variableCombo;
    JComboBox operatorCombo;
    JTextField value;

    public VariableUpdateEventPage() {
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new SpringLayout());
        variableCombo = new JComboBox();
        value = new JTextField(20);
        variableCombo.setEditable(true);
        variableCombo.addItem("");
        operatorCombo = new JComboBox();
        operatorCombo.addItem("");
        operatorCombo.addItem("=");
        operatorCombo.addItem(">");
        operatorCombo.addItem(">=");
        operatorCombo.addItem("<");
        operatorCombo.addItem("<=");
        operatorCombo.addItem("<>");
        operatorCombo.addItem("in");
        operatorCombo.addItem("not in");
        operatorCombo.addItem("updated");

        for (String strVar : DataServer.variablesServer.variablesVector) {
            variableCombo.addItem(strVar);
        }

        panel.add(new JLabel("Variable: "));
        panel.add(variableCombo);
        panel.add(new JLabel("Operator: "));
        panel.add(operatorCombo);
        panel.add(new JLabel("Value: "));
        panel.add(value);

        SpringUtilities.makeCompactGrid(panel,
                3, 2, //rows, cols
                0, 15, //initialX, initialY
                5, 5);//xPad, yPad

        JPanel panelVars = new JPanel();
        panelVars.add(panel);

        add(new JLabel("Select the variable condition"), BorderLayout.NORTH);
        add(panelVars, BorderLayout.CENTER);
    }

    public static final String getDescription() {
        return "Define the condition";
    }

    protected String validateContents(Component comp, Object o) {
        if (variableCombo.getSelectedItem().toString().length() == 0) {
            return "Select the variable";
        } else if (operatorCombo.getSelectedItem().toString().length() == 0) {
            return "Select the operator";
        } else {
            return null;
        }
    }
}

