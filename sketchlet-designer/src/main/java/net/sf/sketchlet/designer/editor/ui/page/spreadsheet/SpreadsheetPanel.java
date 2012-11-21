/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.page.spreadsheet;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.UIUtils;
import net.sf.sketchlet.designer.editor.ui.desktop.Notepad;
import net.sf.sketchlet.model.evaluator.Evaluator;
import net.sf.sketchlet.util.RefreshTime;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EventObject;

/**
 * @author zobrenovic
 */
public class SpreadsheetPanel extends JPanel {

    RSyntaxTextArea formula = Notepad.getTextField();
    JCheckBox checkBox = new JCheckBox(Language.translate("show formulas"));

    public SpreadsheetPanel() {
        setLayout(new BorderLayout());
        table = new JTable(model);
        table.setTransferHandler(new SpreadsheetTransferHandler());
        table.setDragEnabled(true);
        new FileDrop(System.out, table, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
                if (files.length > 0) {
                }
            }

            public void dragOver(int x, int y) {
                Point p = new Point(x, y);
                int row = table.rowAtPoint(p);
                int col = table.columnAtPoint(p);
            }

            public void stringDropped(Point p, String strText) {
                int row = table.rowAtPoint(p);
                int col = table.columnAtPoint(p);
                Workspace.getPage().updateSpreadsheetCell(row, col, strText);
                model.fireTableCellUpdated(row, col);
            }
        });
        String strColumnWidths[] = Workspace.getPage().getStrSpreadsheetColumnWidths().trim().split(",");

        TableColumn tc = table.getColumnModel().getColumn(0);
        tc.setWidth(30);
        tc.setMaxWidth(30);

        for (int ic = 1; ic < table.getColumnCount(); ic++) {
            int w = 100;
            if (ic < strColumnWidths.length) {
                try {
                    w = (int) Double.parseDouble(strColumnWidths[ic]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            table.getColumnModel().getColumn(ic).setWidth(w);
            table.getColumnModel().getColumn(ic).setPreferredWidth(w);
        }

        table.setDefaultRenderer(String.class, new SpreadsheetCellRenderer(this));
        final TableCellEditor defaultEditor = Notepad.getTableCellEditor();
        table.setDefaultEditor(String.class, new TableCellEditor() {

            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                return defaultEditor.getTableCellEditorComponent(table, Workspace.getPage().getSpreadsheetCellValue(row, column), isSelected, row, column);
            }

            public Object getCellEditorValue() {
                return defaultEditor.getCellEditorValue();
            }

            public boolean isCellEditable(EventObject anEvent) {
                return defaultEditor.isCellEditable(anEvent);
            }

            public boolean shouldSelectCell(EventObject anEvent) {
                return defaultEditor.shouldSelectCell(anEvent);
            }

            public boolean stopCellEditing() {
                return defaultEditor.stopCellEditing();
            }

            public void cancelCellEditing() {
                defaultEditor.cancelCellEditing();
            }

            public void addCellEditorListener(CellEditorListener l) {
                defaultEditor.addCellEditorListener(l);
            }

            public void removeCellEditorListener(CellEditorListener l) {
                defaultEditor.removeCellEditorListener(l);
            }
        });
        table.setCellSelectionEnabled(true);
        ListSelectionListener listener = new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                int c = table.getSelectedColumn();
                int r = table.getSelectedRow();
                if (c > 0 && r >= 0) {
                    formula.setText(Workspace.getPage().getSpreadsheetCellValue(r, c));
                }
            }
        };
        table.getSelectionModel().addListSelectionListener(listener);
        table.getColumnModel().getSelectionModel().addListSelectionListener(listener);


        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.getColumnModel().addColumnModelListener(new TableColumnModelListener() {

            public void columnAdded(TableColumnModelEvent e) {
                SketchletEditor.getInstance().saveSpreadsheetColumWidths();
            }

            public void columnRemoved(TableColumnModelEvent e) {
                SketchletEditor.getInstance().saveSpreadsheetColumWidths();
            }

            public void columnMoved(TableColumnModelEvent e) {
                SketchletEditor.getInstance().saveSpreadsheetColumWidths();
            }

            public void columnMarginChanged(ChangeEvent e) {
                SketchletEditor.getInstance().saveSpreadsheetColumWidths();
            }

            public void columnSelectionChanged(ListSelectionEvent e) {
                SketchletEditor.getInstance().saveSpreadsheetColumWidths();
            }
        });

        add(new JScrollPane(table));

        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        // panelTop.add(new JLabel(Language.translate("Formula: ")));
        // panelTop.add(formula);
        // TutorialPanel.prepare(formula);
        formula.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = formula.getText();
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    formula.setText(text.replace("\n", ""));
                    int c = table.getSelectedColumn();
                    int r = table.getSelectedRow();
                    if (c >= 0 && r >= 0) {
                        model.setValueAt(formula.getText(), r, c);
                    }
                }
            }
        });
        panelTop.add(checkBox);
        checkBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                model.fireTableDataChanged();
            }
        });
        add(panelTop, BorderLayout.EAST);
    }

    public JTable table;
    public AbstractTableModel model = new AbstractTableModel() {

        public int getColumnCount() {
            return Workspace.getPage().getSpreadsheetData()[0].length;
        }

        public String getColumnName(int col) {
            return col == 0 ? "" : "" + (char) ('A' + (col - 1));
        }

        public int getRowCount() {
            return Workspace.getPage().getSpreadsheetData().length;
        }

        public Object getValueAt(int row, int col) {
            if (col == 0) {
                return "" + (row + 1);
            }
            String expression = Workspace.getPage().getSpreadsheetCellValue(row, col);
            if (checkBox != null && expression != null && (!checkBox.isSelected() && (expression.startsWith("=") || expression.startsWith("${")))) {
                String prevValue = Workspace.getPage().getPrevSpreadsheetData()[row][col];
                // Object result = JEParser.getValue(expression.substring(1));
                Object result = Evaluator.processText(expression, "", "");
                if (result == null) {
                    return "ERROR";
                }

                String strValue = result.toString();
                if (!strValue.equals(prevValue)) {
                    Workspace.getPage().getPrevSpreadsheetData()[row][col] = strValue;
                    DataServer.getInstance().notifyChange(this.getColumnName(col) + "" + (row + 1), strValue, prevValue);
                }
                return strValue;
            } else {
                return expression;
            }
        }

        public void setValueAt(Object value, int row, int col) {
            SketchletEditor.getInstance().saveSketchUndo();
            Workspace.getPage().updateSpreadsheetCell(row, col, value.toString());
            model.fireTableDataChanged();
            table.getSelectionModel().setSelectionInterval(row, row);
            formula.setText(value.toString());
            DataServer.getInstance().notifyChange(getColumnName(col) + (row + 1), value.toString(), value.toString());
            RefreshTime.update();
            SketchletEditor.getInstance().repaint();
            RefreshTime.update();
        }

        public boolean isCellEditable(int row, int col) {
            return col > 0;
        }

        public Class getColumnClass(int c) {
            return String.class;
        }
    };

    public void refreshComponenets() {
        UIUtils.refreshTable(this.table);
    }
}
