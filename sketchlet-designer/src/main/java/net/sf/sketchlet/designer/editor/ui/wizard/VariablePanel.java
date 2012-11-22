/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui.wizard;

import net.sf.sketchlet.blackboard.VariablesBlackboard;
import net.sf.sketchlet.util.SpringUtilities;

import javax.swing.*;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class VariablePanel extends JPanel {

    JComboBox variableCombo;
    JTextField value;
    ActionParamPage paramPage;

    public VariablePanel(ActionParamPage paramPage) {
        this.paramPage = paramPage;
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new SpringLayout());
        variableCombo = new JComboBox();
        value = new JTextField(20);
        variableCombo.setEditable(true);
        variableCombo.addItem("");

        for (String strVar : VariablesBlackboard.getInstance().getVariablesList()) {
            variableCombo.addItem(strVar);
        }

        panel.add(new JLabel("Variable: "));
        panel.add(variableCombo);
        panel.add(new JLabel("Value"));
        panel.add(value);

        SpringUtilities.makeCompactGrid(panel,
                2, 2, //rows, cols
                0, 15, //initialX, initialY
                5, 5);//xPad, yPad

        JPanel panelVars = new JPanel();
        panelVars.add(panel);

        add(new JLabel("Select the variable and value"), BorderLayout.NORTH);
        add(panelVars, BorderLayout.CENTER);
    }

    protected String validateContents(Component comp, Object o) {
        if (variableCombo.getSelectedItem().toString().length() == 0) {
            return "Select variable";
        } else {
            paramPage.param1 = variableCombo.getSelectedItem() != null ? (String) variableCombo.getSelectedItem() : "";
            paramPage.param2 = value.getText();

            return null;
        }
    }
}
