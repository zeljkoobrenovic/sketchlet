/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.properties;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.desktop.Notepad;
import net.sf.sketchlet.designer.editor.ui.region.PropertiesTableRenderer;
import net.sf.sketchlet.model.PropertiesInterface;
import net.sf.sketchlet.util.RefreshTime;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class PropertiesSetPanel extends JPanel {

    AbstractTableModel model;
    public JTable table;
    JScrollPane tableScroll;
    boolean bUpdating = false;
    Runnable saveUndoAction;

    public PropertiesSetPanel(final PropertiesInterface properties) {
        this(properties, true);
    }

    public void setSaveUndoAction(Runnable undoAction) {
        this.saveUndoAction = undoAction;
    }

    public PropertiesSetPanel(final PropertiesInterface properties, boolean bSlider) {
        final String columnNames[] = new String[]{Language.translate("Property"), Language.translate("Value"), Language.translate("Description")};
        model = new AbstractTableModel() {

            public String getColumnName(int col) {
                return columnNames[col].toString();
            }

            public int getRowCount() {
                if (SketchletEditor.getInstance() == null || SketchletEditor.getInstance().getCurrentPage() == null) {
                    return 0;
                }
                return properties.getPropertiesCount();
            }

            public int getColumnCount() {
                return columnNames.length;
            }

            public Object getValueAt(int row, int col) {
                String property = properties.getData()[row][0];
                if (col == 0) {
                    return properties.getData()[row][col];
                } else if (col == 1 && properties.getData()[row][col] != null) {
                    return properties.getProperty(property);
                } else if (col == 2) {
                    return properties.getPropertyDescription(property);
                }

                return "";
            }

            public boolean isCellEditable(int row, int col) {
                return properties.getData()[row][1] != null && col == 1;
            }

            public void setValueAt(Object value, int row, int col) {
                if (bUpdating) {
                    return;
                }
                bUpdating = true;
                if (saveUndoAction != null && !this.getValueAt(row, col).toString().equals(value.toString())) {
                    saveUndoAction.run();
                }
                if (col == 1) {
                    properties.setProperty(properties.getData()[row][0], value.toString());
                    fireTableCellUpdated(row, col);
                    RefreshTime.update();
                    properties.repaintProperties();
                    properties.logPropertyUpdate(properties.getData()[row][0], value.toString(), tableScroll);
                }
                bUpdating = false;
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };
        table = new JTable(model);
        table.setDefaultRenderer(String.class, new PropertiesTableRenderer(properties.getData()));
        table.setTransferHandler(new PropertiesTransferHandler(properties));
        table.setDragEnabled(true);

        JComboBox comboBox = new JComboBox();

        comboBox.removeAllItems();
        comboBox.setEditable(true);
        comboBox.addItem("");
        for (String strVar : DataServer.getInstance().variablesVector) {
            comboBox.addItem("=" + strVar);
        }

        // table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBox));
        table.getColumnModel().getColumn(1).setCellEditor(Notepad.getTableCellEditor());

        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tableScroll = new JScrollPane(table);

        setLayout(new BorderLayout());

        this.add(tableScroll, BorderLayout.CENTER);
        JPanel panelButtons = new JPanel(new BorderLayout());
        final JButton btnClear = new JButton("Clear");
        final JButton btnRefresh = new JButton("Refresh");
        final JTextField startField = new JTextField(5);
        final JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
        final JTextField endField = new JTextField(5);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.add(endField, BorderLayout.NORTH);
        sliderPanel.add(slider, BorderLayout.CENTER);
        sliderPanel.add(startField, BorderLayout.SOUTH);
        sliderPanel.setBorder(BorderFactory.createTitledBorder("Explore"));

        btnClear.setEnabled(false);
        startField.setEnabled(false);
        slider.setEnabled(false);
        endField.setEnabled(false);
        btnClear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = table.getSelectedRow();
                if (row >= 0 && properties.getData()[row][1] != null) {
                    model.setValueAt("", row, 1);
                    startField.setEnabled(true);
                    slider.setEnabled(true);
                    endField.setEnabled(true);
                }
            }
        });
        btnRefresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                table.repaint();
            }
        });
        JPanel panelSouth = new JPanel(new GridLayout(2, 1));
        panelSouth.add(btnClear);
        panelSouth.add(btnRefresh);
        panelButtons.add(panelSouth, BorderLayout.SOUTH);

        if (bSlider) {
            panelButtons.add(sliderPanel, BorderLayout.CENTER);
        }

        startField.setHorizontalAlignment(JTextField.CENTER);
        endField.setHorizontalAlignment(JTextField.CENTER);

        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!bCanUpdate) {
                    return;
                }

                bCanUpdate = false;
                try {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        String start = startField.getText();
                        String end = endField.getText();
                        if (start != null && end != null) {
                            _start = Double.parseDouble(start);
                            _end = Double.parseDouble(end);
                            int fps = (int) slider.getValue();

                            model.setValueAt("" + (Math.min(_start, _end) + Math.abs(_start - _end) * fps / (slider.getMaximum() - slider.getMinimum())), row, 1);
                            RefreshTime.update();
                            properties.repaintProperties();
                        }
                    }
                } catch (Exception e2) {
                }

                bCanUpdate = true;
            }
        });

        this.add(panelButtons, BorderLayout.EAST);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                int row = table.getSelectedRow();
                boolean bEnable = row >= 0 && properties.getData()[row][1] != null;
                btnClear.setEnabled(bEnable);
                String start = null;
                String end = null;
                String init = "";
                if (row >= 0) {
                    String strProperty = properties.getData()[row][0];
                    start = properties.getMinValue(strProperty);
                    end = properties.getMaxValue(strProperty);
                    init = properties.getProperty(strProperty);
                    if (init == null || init.isEmpty()) {
                        String defaultValue = properties.getDefaultValue(strProperty);
                        if (defaultValue != null && !defaultValue.isEmpty()) {
                            init = defaultValue;
                        }
                    }

                    if (start == null || end == null) {
                        bEnable = false;
                    }

                    try {
                        _start = Double.parseDouble(start);
                        _end = Double.parseDouble(end);

                        if (init.isEmpty()) {
                            _init = _start;
                        } else {
                            _init = Double.parseDouble(init);
                        }

                        bCanUpdate = false;
                        slider.setValue(slider.getMinimum() + (int) ((slider.getMaximum() - slider.getMinimum()) * (_init - Math.min(_start, _end)) / Math.abs(_end - _start)));
                        bCanUpdate = true;
                    } catch (Exception e) {
                        bEnable = false;
                    }
                }
                startField.setEnabled(bEnable);
                slider.setEnabled(bEnable);
                endField.setEnabled(bEnable);

                if (bEnable) {
                    startField.setText(start);
                    endField.setText(end);
                }
            }
        });

    }

    double _start = 0.0;
    double _end = 0.0;
    double _init = 0.0;
    boolean bCanUpdate = true;
}
