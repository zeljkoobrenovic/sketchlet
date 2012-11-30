package net.sf.sketchlet.designer.editor.ui.properties;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.programming.timers.curves.Curves;
import net.sf.sketchlet.util.ui.DataRowFrame;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class PropertiesVariableMappingPanel extends JPanel {

    int rowVariables = -1;
    PropertiesSetPanel setPanel = null;
    PropertiesVariableMappingHandler mapping;
    JComboBox comboBox = new JComboBox();
    JComboBox comboBox2 = new JComboBox();
    JComboBox comboBoxFormat = new JComboBox();
    JTable table;
    JScrollPane tableScroll;
    AbstractTableModel model;

    public PropertiesVariableMappingPanel(final PropertiesVariableMappingHandler mapping) {
        this.mapping = mapping;
//        final JButton edit = new JButton("Edit");
        model = new AbstractTableModel() {

            public String getColumnName(int col) {
                return mapping.columnNames[col].toString();
            }

            public int getRowCount() {
                if (SketchletEditor.getInstance() == null || SketchletEditor.getInstance().getCurrentPage() == null) {
                    return 0;
                } else {
                    return mapping.data.length;
                }
            }

            public int getColumnCount() {
                return mapping.columnNames.length;
            }

            public Object getValueAt(int row, int col) {
                if (SketchletEditor.getInstance() == null || SketchletEditor.getInstance().getCurrentPage() == null) {
                    return "";
                } else {
                    return mapping.data[row][col];
                }
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }

            public void setValueAt(Object value, int row, int col) {
                if (SketchletEditor.getInstance() == null || SketchletEditor.getInstance().getCurrentPage() == null) {
                    return;
                }
                mapping.data[row][col] = value;
                if (col == 0) {
                    String defStart = mapping.properties.getMinValue(value.toString());
                    String defEnd = mapping.properties.getMaxValue(value.toString());

                    if (mapping.data[row][1].toString().isEmpty() && defStart != null) {
                        mapping.data[row][1] = defStart;
                    }
                    if (mapping.data[row][2].toString().isEmpty() && defEnd != null) {
                        mapping.data[row][2] = defEnd;
                    }

                    this.fireTableRowsUpdated(row, row);
                }
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };

        table = new JTable(model);

        JComboBox comboBoxProperties = mapping.properties.getPropertiesCombo();

        table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBoxProperties));


        comboBox.removeAllItems();
        comboBox.setEditable(true);
        comboBox.addItem("");
        comboBox2.removeAllItems();
        comboBox2.setEditable(true);
        comboBox2.addItem("");
        for (String strVar : VariablesBlackboard.getInstance().getVariablesList()) {
            comboBox.addItem(strVar);
            comboBox2.addItem("=" + strVar);
        }

        comboBoxFormat.removeAllItems();
        comboBoxFormat.setEditable(true);
        comboBoxFormat.addItem("");
        comboBoxFormat.addItem("0");
        comboBoxFormat.addItem("00");
        comboBoxFormat.addItem("000");
        comboBoxFormat.addItem("0.00");

        table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBox2));
        table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBox2));
        table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboBox));
        table.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(comboBox2));
        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(comboBox2));
        table.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(comboBoxFormat));

        table.getColumnModel().getColumn(7).setCellEditor(new DefaultCellEditor(Curves.getGlobalCurves().getComboBox()));

        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        tableScroll = new JScrollPane(table);

        setLayout(new BorderLayout());

        this.add(tableScroll, BorderLayout.CENTER);

        final JButton delete = new JButton("Delete");
        final JButton moveUp = new JButton("Move Up");
        final JButton moveDown = new JButton("Move Down");
        final JButton duplicate = new JButton("Duplicate");
        final JButton edit = new JButton("Edit");
        delete.setMargin(new Insets(2, 2, 2, 2));
        duplicate.setMargin(new Insets(2, 2, 2, 2));
        moveUp.setMargin(new Insets(2, 2, 2, 2));
        moveDown.setMargin(new Insets(2, 2, 2, 2));
        edit.setMargin(new Insets(2, 2, 2, 2));
//        edit.setMargin(new Insets(2, 2, 2, 2));
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 1));
        panel.add(delete);
        panel.add(duplicate);
        panel.add(moveUp);
        panel.add(moveDown);
        panel.add(edit);

        delete.setEnabled(rowVariables >= 0);
        duplicate.setEnabled(rowVariables >= 0);
        moveDown.setEnabled(rowVariables >= 0);
        moveUp.setEnabled(rowVariables >= 0);
        edit.setEnabled(rowVariables >= 0);

//        panel.add(edit);
        JPanel panelWrapper = new JPanel(new BorderLayout());
        panelWrapper.add(panel);
        this.add(panelWrapper, BorderLayout.EAST);
        new FileDrop(System.out, table, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
            }

            public void dragOver(int x, int y) {
            }

            public void stringDropped(Point p, String strText) {
                DataRowFrame.emptyOnCancel = true;
                addNewRow(strText);
                DataRowFrame.emptyOnCancel = false;
            }
        });
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                rowVariables = table.getSelectedRow();
                delete.setEnabled(rowVariables >= 0);
                duplicate.setEnabled(rowVariables >= 0);
                moveDown.setEnabled(rowVariables >= 0 && rowVariables < mapping.data.length - 1);
                moveUp.setEnabled(rowVariables > 0);
                edit.setEnabled(rowVariables >= 0);
            }
        });
        final Object data[][] = mapping.data;
        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (rowVariables >= 0) {
                    int r = rowVariables;

                    for (int i = rowVariables; i < data.length - 1; i++) {
                        data[i] = data[i + 1];
                    }

                    data[data.length - 1] = new Object[]{"", "", "", "", "", "", "", ""};

                    model.fireTableDataChanged();

                    table.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });

        moveUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (rowVariables > 0) {
                    Object[] rowData1 = data[rowVariables];
                    Object[] rowData2 = data[rowVariables - 1];

                    data[rowVariables] = rowData2;
                    data[rowVariables - 1] = rowData1;

                    int r = rowVariables - 1;

                    model.fireTableDataChanged();

                    table.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveDown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (rowVariables < data.length - 1) {
                    Object[] rowData1 = data[rowVariables];
                    Object[] rowData2 = data[rowVariables + 1];

                    data[rowVariables] = rowData2;
                    data[rowVariables + 1] = rowData1;

                    int r = rowVariables + 1;

                    model.fireTableDataChanged();

                    table.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        duplicate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (rowVariables < data.length - 1) {
                    Object[] rowData1 = data[rowVariables];

                    for (int i = data.length - 2; i >= rowVariables + 1; i--) {
                        data[i + 1] = data[i];
                    }

                    data[rowVariables + 1] = new Object[]{
                            "" + data[rowVariables][0],
                            "" + data[rowVariables][1],
                            "" + data[rowVariables][2],
                            "" + data[rowVariables][3],
                            "" + data[rowVariables][4],
                            "" + data[rowVariables][5],
                            "" + data[rowVariables][6],
                            "" + data[rowVariables][7]};

                    int r = rowVariables + 1;

                    model.fireTableDataChanged();
                    table.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });

        edit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                editVariableMapping(rowVariables);
            }
        });

    }

    public void addNewRow(String strVar) {
        if (strVar.startsWith("=") && strVar.length() > 1) {
            for (int i = 0; i < mapping.data.length; i++) {
                if (mapping.data[i][3].toString().isEmpty()) {
                    mapping.data[i][3] = strVar.substring(1);
                    editVariableMapping(i);
                    break;
                }
            }
        }
    }

    public void editVariableMapping(int row) {
        if (row >= 0) {
            Object editors[] = new Object[mapping.columnNames.length];
            final JComboBox comboProp = DataRowFrame.cloneComboBox(mapping.properties.getPropertiesCombo());
            final JComboBox comboStart = DataRowFrame.cloneComboBox(comboBox2);
            final JComboBox comboEnd = DataRowFrame.cloneComboBox(comboBox2);

            comboProp.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    Object prop = comboProp.getSelectedItem();
                    if (prop != null) {
                        String strProp = prop.toString();
                        comboStart.setSelectedItem(mapping.properties.getMinValue(strProp));
                        comboEnd.setSelectedItem(mapping.properties.getMaxValue(strProp));
                    }
                }
            });

            editors[0] = comboProp;
            editors[1] = comboStart;
            editors[2] = comboEnd;
            editors[3] = DataRowFrame.cloneComboBox(comboBox);
            editors[4] = DataRowFrame.cloneComboBox(comboBox2);
            editors[5] = DataRowFrame.cloneComboBox(comboBox2);
            editors[6] = DataRowFrame.cloneComboBox(comboBoxFormat);
            editors[7] = DataRowFrame.cloneComboBox(Curves.getGlobalCurves().getComboBox());

            new DataRowFrame(SketchletEditor.editorFrame,
                    "Mapping to Numeric Variable",
                    row,
                    mapping.columnNames,
                    editors, null, null,
                    table,
                    model);
        }
    }

    public void setPropertiesSetPanel(PropertiesSetPanel panel) {
        this.setPanel = panel;
        this.mapping.setPanel = panel;

    }
}
