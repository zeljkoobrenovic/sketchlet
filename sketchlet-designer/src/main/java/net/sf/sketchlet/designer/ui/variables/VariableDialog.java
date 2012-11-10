/*
 * AdvancedSettingsDialog.java
 *
 * Created on October 19, 2007, 11:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.variables;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/* 1.4 example used by DialogDemo.java. */
public class VariableDialog extends JDialog {

    public Frame frame;
    public JTextField name;
    public JTextField value;
    public String strName;
    public String strValue;
    public JButton okButton;
    public JButton cancelButton;
    public boolean accepted = false;
    VariableDialog thisDialog = this;

    /**
     * Creates the reusable dialog.
     */
    public VariableDialog(Frame _frame, String strTitle, String _strName, String _strValue) {
        super(_frame, strTitle, true);
        this.frame = _frame;

        this.setLayout(new FlowLayout());

        name = new JTextField(_strName, 10);
        name.selectAll();

        value = new JTextField(_strValue, 10);

        okButton = new JButton(Language.translate("OK"));
        cancelButton = new JButton(Language.translate("Cancel"));

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                if (VariablesTablePanel.variablesTableInterface != null) {
                    VariablesTablePanel.variablesTableInterface.variableDialogAdded(name.getText(), value.getText(), thisDialog);
                }
                accepted = true;
                strName = name.getText();
                strValue = value.getText();
                if (DataServer.variablesServer.isAdditionalVariable(strName)) {
                    JOptionPane.showMessageDialog(frame, Language.translate("You cannot use '") + strName + Language.translate("' as a variable name.") + "\n" + Language.translate("This name is being used to reference a spreadhseet cell."),
                            Language.translate("Reserved Variable Name"), JOptionPane.ERROR_MESSAGE);
                } else {
                    setVisible(false);
                }
            }
        });


        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                accepted = false;
                setVisible(false);
            }
        });

        add(this.name);
        add(new JLabel(" = "));
        add(this.value);

        add(okButton);
        add(cancelButton);

        this.getRootPane().setDefaultButton(okButton);
    }

    public String getName() {
        return strName;
    }

    public void setName(String strName) {
        this.strName = strName;
    }

    public String getValue() {
        return strValue;
    }

    public void setValue(String strValue) {
        this.strValue = strValue;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}
