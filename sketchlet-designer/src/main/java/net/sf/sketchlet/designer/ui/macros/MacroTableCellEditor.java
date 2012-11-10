/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.macros;

import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.macros.Macro;
import net.sf.sketchlet.designer.programming.macros.Macros;
import net.sf.sketchlet.designer.programming.screenscripts.ScreenScripts;
import net.sf.sketchlet.designer.programming.timers.Timer;
import net.sf.sketchlet.designer.programming.timers.Timers;
import net.sf.sketchlet.script.ScriptPluginProxy;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class MacroTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    int actionColIndex;
    int paramColIndex;
    Object value = "";

    public MacroTableCellEditor(int actionColIndex, int paramColIndex) {
        this.actionColIndex = actionColIndex;
        this.paramColIndex = paramColIndex;
    }

    //Implement the one CellEditor method that AbstractCellEditor doesn't.
    public Object getCellEditorValue() {
        return value;
    }

    //Implement the one method defined by TableCellEditor.
    public Component getTableCellEditorComponent(final JTable table, Object value, boolean isSelected, final int row, final int column) {
        this.value = value;
        if (column == this.actionColIndex) {
            final JComboBox comboBox = new JComboBox(Macro.commands);
            comboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
            comboBox.setSelectedItem(value.toString());
            comboBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    String text = (String) comboBox.getSelectedItem();
                    if (text != null) {
                        table.setValueAt(text, row, column);
                    }
                }
            });
            return comboBox;
        } else if (column == this.paramColIndex) {
            final JComboBox comboBoxParam1 = new JComboBox();
            comboBoxParam1.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
            String strCommand = table.getModel().getValueAt(row, this.actionColIndex).toString();
            comboBoxParam1.setEditable(true);
            comboBoxParam1.addItem("");

            if (strCommand.equalsIgnoreCase("pause") || strCommand.equalsIgnoreCase("pause (seconds)")) {
                for (double i = 1.0; i <= 10.0; i += 1.0) {
                    comboBoxParam1.addItem(i + "");
                }
            } else if (strCommand.startsWith("variable")) {
                for (String strVar : DataServer.variablesServer.variablesVector) {
                    comboBoxParam1.addItem(strVar);
                }
            } else if (strCommand.endsWith("timer") && Timers.globalTimers != null) {
                for (Timer t : Timers.globalTimers.timers) {
                    comboBoxParam1.addItem(t.name);
                }
            } else if (strCommand.equalsIgnoreCase("go to page") && SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.pages != null) {
                for (Page s : SketchletEditor.editorPanel.pages.pages) {
                    comboBoxParam1.addItem(s.title);
                }
            } else if (strCommand.endsWith("macro") && Macros.globalMacros != null) {
                for (Macro m : Macros.globalMacros.macros) {
                    comboBoxParam1.addItem(m.name);
                }
                if (ScreenScripts.publicScriptRunner != null) {
                    ScreenScripts.publicScriptRunner.setCombos(comboBoxParam1);
                }
                for (ScriptPluginProxy script : DataServer.scripts) {
                    comboBoxParam1.addItem("Script:" + script.scriptFile.getName());
                }
            } else if (strCommand.equalsIgnoreCase("repeat")) {
                comboBoxParam1.addItem("Forever");
                for (int i = 2; i <= 10; i++) {
                    comboBoxParam1.addItem(i + "");
                }
            }
            comboBoxParam1.setSelectedItem(value.toString());
            comboBoxParam1.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    String text = (String) comboBoxParam1.getSelectedItem();
                    if (text != null) {
                        table.setValueAt(text, row, column);
                    }
                }
            });
            return comboBoxParam1;
        }

        return new JTextField(value.toString());
    }
}

