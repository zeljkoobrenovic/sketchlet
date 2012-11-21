/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui;

import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.model.Page;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.Hashtable;

/**
 * @author zobrenovic
 */
public class UIUtils {

    public static void populateVariablesCombo(JComboBox comboBox, boolean addEquals) {
        populateVariablesCombo(comboBox, addEquals, null);
    }

    public static void populateVariablesCombo(JComboBox comboBox, boolean addEquals, String firstValues[]) {
        Object selectedItem = comboBox.getSelectedItem();

        comboBox.removeAllItems();
        comboBox.setEditable(true);
        comboBox.addItem("");

        if (firstValues != null && firstValues.length > 0) {
            for (int i = 0; i < firstValues.length; i++) {
                comboBox.addItem(firstValues[i]);
            }
        }

        for (String strVar : DataServer.getInstance().variablesVector) {
            comboBox.addItem((addEquals ? "=" : "") + strVar);
        }

        if (selectedItem != null) {
            comboBox.setSelectedItem(selectedItem);
        } else {
            comboBox.setSelectedIndex(0);
        }
    }

    public static void populateSketchesCombo(JComboBox comboBox, boolean addVariables) {
        Object selectedItem = comboBox.getSelectedItem();

        comboBox.removeAllItems();
        comboBox.setEditable(true);
        comboBox.addItem("");

        if (SketchletEditor.getInstance() != null) {
            for (Page page : SketchletEditor.getInstance().getPages().getPages()) {
                comboBox.addItem(page.getTitle());
            }
        }
        if (addVariables) {
            for (String strVar : DataServer.getInstance().variablesVector) {
                comboBox.addItem("=" + strVar);
            }
        }

        if (selectedItem != null) {
            comboBox.setSelectedItem(selectedItem);
        } else {
            comboBox.setSelectedIndex(0);
        }
    }

    public static void refreshTable(JTable table) {
        TableCellEditor tce = table.getCellEditor();
        if (tce != null) {
            tce.cancelCellEditing();
        }
        TableModel model = table.getModel();
        if (model != null && model instanceof AbstractTableModel) {
            ((AbstractTableModel) model).fireTableDataChanged();
        } else {
            table.repaint();
        }
    }

    public static void refreshComboBox(JComboBox combo, String strValue) {
        ActionListener listeners[] = combo.getActionListeners();
        for (ActionListener a : listeners) {
            combo.removeActionListener(a);
        }
        combo.setSelectedItem(strValue);
        for (ActionListener a : listeners) {
            combo.addActionListener(a);
        }
    }

    public static void refreshCheckBox(JCheckBox checkbox, boolean value) {
        ItemListener listeners[] = checkbox.getItemListeners();
        for (ItemListener il : listeners) {
            checkbox.removeItemListener(il);
        }
        checkbox.setSelected(value);
        for (ItemListener il : listeners) {
            checkbox.addItemListener(il);
        }
    }

    public static void deleteTableRows(JTable table, AbstractTableModel model, Object data[][]) {
        SketchletEditor.getInstance().saveRegionUndo();
        int rows[] = table.getSelectedRows();
        int r = -1;
        for (int ir = rows.length - 1; ir >= 0; ir--) {
            int row = rows[ir];
            if (row >= 0) {
                r = row;

                for (int i = row; i < data.length - 1; i++) {
                    data[i] = data[i + 1];
                }
                Object[] emptyRow = new Object[data[ir].length];
                for (int i = 0; i < data[ir].length; i++) {
                    emptyRow[i] = "";
                }

                data[data.length - 1] = emptyRow;
            }
        }
        if (r != -1) {
            model.fireTableDataChanged();
            table.getSelectionModel().setSelectionInterval(r, r);
        }

    }

    public static void deleteTableRows(JTable table, AbstractTableModel model, java.util.List list) {
        SketchletEditor.getInstance().saveRegionUndo();
        int rows[] = table.getSelectedRows();
        int lastRow = -1;
        for (int ir = rows.length - 1; ir >= 0; ir--) {
            int row = rows[ir];
            if (row >= 0) {
                lastRow = row;
                list.remove(row);
            }
        }
        if (lastRow >= list.size()) {
            lastRow = list.size() - 1;
        }
        model.fireTableDataChanged();
        table.getSelectionModel().setSelectionInterval(lastRow, lastRow);
    }

    public static void makeSmall(JComponent components[]) {
        for (int i = 0; i < components.length; i++) {
            components[i].putClientProperty("JComponent.sizeVariant", "small");
            SwingUtilities.updateComponentTreeUI(components[i]);
        }
    }

    public static void removeActionListeners(JComboBox combo) {
        ActionListener al[] = combo.getActionListeners();
        for (int i = 0; i < al.length; i++) {
            combo.removeActionListener(al[i]);
        }
    }

    public static void removeActionListeners(JTextField textField) {
        ActionListener al[] = textField.getActionListeners();
        for (int i = 0; i < al.length; i++) {
            textField.removeActionListener(al[i]);
        }
    }

    public static JPopupMenu loadMenu(final JTextField value, String items[], Hashtable<String, String[]> subMenus) {
        final JPopupMenu popup = new JPopupMenu();

        for (int i = 0; i < items.length; i++) {
            final String strValue = items[i];
            JMenuItem menuItem = new JMenuItem(strValue);
            menuItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    value.setText(strValue);
                }
            });
            popup.add(menuItem);
        }
        if (subMenus != null) {
            for (String strSubMenu : subMenus.keySet()) {
                JMenu menu = new JMenu(strSubMenu);
                String subItems[] = subMenus.get(strSubMenu);
                for (int i = 0; i < subItems.length; i++) {
                    final String strValue = subItems[i];
                    JMenuItem menuItem = new JMenuItem(strValue);
                    menuItem.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            value.setText(strValue);
                        }
                    });
                    menu.add(menuItem);
                }
                popup.add(menu);
            }
        }

        return popup;
    }
}
