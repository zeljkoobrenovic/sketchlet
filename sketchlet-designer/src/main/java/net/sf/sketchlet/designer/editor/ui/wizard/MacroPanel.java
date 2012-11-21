/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui.wizard;

/**
 *
 * @author zobrenovic
 */

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.ui.macros.MacrosFrame;
import net.sf.sketchlet.model.programming.macros.Macro;
import net.sf.sketchlet.model.programming.macros.Macros;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class MacroPanel extends JPanel {

    JComboBox macroCombo;
    ActionParamPage paramPage;
    JButton editMacro = new JButton(Language.translate("edit..."));

    public MacroPanel(ActionParamPage paramPage) {
        this.paramPage = paramPage;
        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        macroCombo = new JComboBox();
        macroCombo.setEditable(true);
        macroCombo.addItem("");
        for (Macro m : Macros.globalMacros.macros) {
            macroCombo.addItem(m.getName());
        }

        macroCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                enableControls();
            }
        });

        add(new JLabel("Select the action list"), BorderLayout.NORTH);
        panel.add(new JLabel("Action List: "));
        panel.add(macroCombo);
        JButton addMacro = new JButton("new action list...");
        addMacro.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Macro m = Macros.globalMacros.addNewMacro();
                macroCombo.addItem(m.getName());
                macroCombo.setSelectedItem(m.getName());
                enableControls();
            }
        });
        editMacro.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String strMacro = (String) macroCombo.getSelectedItem();
                if (strMacro != null) {
                    int index = macroCombo.getSelectedIndex() - 1;
                    MacrosFrame.showMacros(strMacro, true);
                    if (index >= 0) {
                        macroCombo.removeAllItems();
                        macroCombo.addItem("");
                        for (Macro m : Macros.globalMacros.macros) {
                            macroCombo.addItem(m.getName());
                        }
                        macroCombo.setSelectedItem(Macros.globalMacros.macros.elementAt(index).getName());
                    }
                }
            }
        });
        panel.add(addMacro);
        panel.add(editMacro);
        add(panel, BorderLayout.CENTER);
        enableControls();
    }

    public void enableControls() {
        editMacro.setEnabled(macroCombo.getSelectedIndex() > 0);
    }

    protected String validateContents(Component comp, Object o) {
        if (macroCombo.getSelectedItem().toString().length() == 0) {
            return "Select macro";
        } else {
            paramPage.param1 = macroCombo.getSelectedItem().toString();
            paramPage.param2 = "";
            return null;
        }
    }
}
