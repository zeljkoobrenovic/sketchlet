/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.util.ui;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.util.SpringUtilities;
import net.sf.sketchlet.util.UtilContext;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author zobrenovic
 */
public class DataRowFrame extends JDialog {

    int row;
    String labels[];
    Object editors[];
    Object newEditors[];
    JTable table;
    AbstractTableModel tableModel;
    boolean disable[];
    JPanel extraControls[];
    int startCol = 0;
    public static boolean emptyOnCancel = false;
    public static DataRowActionListener dataRowActionListener;
    DataRowFrame dataRowFrame = this;

    public DataRowFrame(JFrame frame, String strCaption, int row, String labels[], Object editors[], boolean disable[], JPanel extraControls[], JTable table, AbstractTableModel tableModel) {
        this(frame, strCaption, row, 0, labels, editors, disable, extraControls, table, tableModel);
    }

    public DataRowFrame(JFrame frame, String strCaption, int row, int startCol, String labels[], Object editors[], boolean disable[], JPanel extraControls[], JTable table, AbstractTableModel tableModel) {
        super(frame, true);
        this.setTitle(strCaption);
        this.startCol = startCol;
        if (frame instanceof JFrame) {
            if (frame != null && ((JFrame) frame).getIconImage() != null) {
                setIconImage(((JFrame) frame).getIconImage());
            }
        }
        this.row = row;
        this.labels = labels;
        this.editors = editors;
        this.table = table;
        this.tableModel = tableModel;
        this.disable = disable;
        this.extraControls = extraControls;

        createInterface();
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    public void createInterface() {
        setLayout(new BorderLayout());
        JPanel centerPane = new JPanel();
        centerPane.setLayout(new SpringLayout());

        newEditors = new Object[editors.length];

        for (int i = 0; i < labels.length; i++) {
            String strLabel = labels[i].trim();
            if (!strLabel.endsWith(":")) {
                strLabel += ":";
            }
            if (tableModel.getValueAt(row, startCol + i) instanceof Boolean) {
                JCheckBox checkBox = new JCheckBox(labels[i], (Boolean) tableModel.getValueAt(row, startCol + i));
                newEditors[i] = checkBox;
                centerPane.add(new JLabel(""));
                centerPane.add(checkBox);
                centerPane.add(new JLabel(""));
            } else {
                strLabel += " ";
                centerPane.add(new JLabel(strLabel, JLabel.RIGHT));

                if (editors[i] == null || editors[i] instanceof JTextField) {
                    JTextField textField = new JTextField((String) tableModel.getValueAt(row, startCol + i));
                    newEditors[i] = textField;
                    centerPane.add(textField);
                    textField.setEnabled(disable == null || !disable[i]);
                } else if (editors[i] instanceof JTextArea) {
                    JTextArea textArea = new JTextArea((String) tableModel.getValueAt(row, startCol + i));
                    textArea.setRows(textArea.getRows());
                    newEditors[i] = textArea;
                    centerPane.add(new JScrollPane(textArea));
                    textArea.setEnabled(disable == null || !disable[i]);
                } else if (editors[i] instanceof JComboBox) {
                    JComboBox comboBox = (JComboBox) editors[i];
                    comboBox.setSelectedItem((String) tableModel.getValueAt(row, startCol + i));
                    newEditors[i] = comboBox;
                    centerPane.add(comboBox);
                    comboBox.setEnabled(disable == null || !disable[i]);
                } else if (editors[i] instanceof JCheckBox) {
                    JCheckBox checkBox = (JCheckBox) editors[i];
                    checkBox.setSelected((Boolean) tableModel.getValueAt(row, startCol + i));
                    newEditors[i] = checkBox;
                    centerPane.add(checkBox);
                    checkBox.setEnabled(disable == null || !disable[i]);
                }

                if (extraControls != null && extraControls[i] != null) {
                    centerPane.add(extraControls[i]);
                } else {
                    centerPane.add(new JLabel(""));
                }
            }
        }
        SpringUtilities.makeCompactGrid(centerPane,
                labels.length, 3, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad

        JPanel buttons = new JPanel();
        JButton btnOk = new JButton(Language.translate("OK"), UtilContext.getInstance().getImageIconFromResources("resources/ok.png"));
        this.getRootPane().setDefaultButton(btnOk);

        JButton btnCancel = new JButton(Language.translate("Cancel"), UtilContext.getInstance().getImageIconFromResources("resources/cancel.png"));
        btnOk.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                try {
                    UtilContext.getInstance().skipUndo(true);
                    saveData();
                    DataRowFrame.emptyOnCancel = false;

                    if (dataRowActionListener != null) {
                        dataRowActionListener.onOK(dataRowFrame);
                    }

                    setVisible(false);
                } finally {
                    UtilContext.getInstance().skipUndo(false);
                }
            }
        });
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (DataRowFrame.emptyOnCancel) {
                    UtilContext.getInstance().skipUndo(true);
                    saveEmptyData();
                    UtilContext.getInstance().skipUndo(false);
                }
                DataRowFrame.emptyOnCancel = false;
                setVisible(false);
            }
        });
        buttons.add(btnOk);
        buttons.add(btnCancel);

        add(centerPane, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    public static JComboBox cloneComboBox(JComboBox editorCombo) {
        JComboBox comboBox = new JComboBox();

        comboBox.setEditable(editorCombo.isEditable());

        for (int ci = 0; ci < editorCombo.getItemCount(); ci++) {
            comboBox.addItem(editorCombo.getItemAt(ci));
        }

        return comboBox;
    }

    public void saveData() {
        for (int i = 0; i < labels.length; i++) {
            if (newEditors[i] instanceof JTextField) {
                tableModel.setValueAt(((JTextField) newEditors[i]).getText(), row, startCol + i);
            } else if (newEditors[i] instanceof JTextArea) {
                tableModel.setValueAt(((JTextArea) newEditors[i]).getText(), row, startCol + i);
            } else if (newEditors[i] instanceof JComboBox) {
                tableModel.setValueAt((String) ((JComboBox) newEditors[i]).getSelectedItem(), row, startCol + i);
            } else if (newEditors[i] instanceof JCheckBox) {
                tableModel.setValueAt(new Boolean(((JCheckBox) newEditors[i]).isSelected()), row, startCol + i);
            }
        }
        tableModel.fireTableDataChanged();
        table.getSelectionModel().setSelectionInterval(row, row);
    }

    public void saveEmptyData() {
        for (int i = 0; i < labels.length; i++) {
            if (newEditors[i] instanceof JTextField) {
                tableModel.setValueAt("", row, startCol + i);
            } else if (newEditors[i] instanceof JComboBox) {
                tableModel.setValueAt("", row, startCol + i);
            } else if (newEditors[i] instanceof JCheckBox) {
            }
        }
        tableModel.fireTableRowsUpdated(row, row);
    }
}
