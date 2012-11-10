package net.sf.sketchlet.designer.ui.localvars;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.VariablesBlackboardContext;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.ui.desktop.Notepad;
import net.sf.sketchlet.designer.ui.variables.VariableDialog;
import net.sf.sketchlet.designer.ui.variables.VariablesPanel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 9-9-12
 * Time: 9:56
 * To change this template use File | Settings | File Templates.
 */
public class PageVariablesPanel extends JPanel {
    private JToolBar toolbar = new JToolBar(JToolBar.VERTICAL);
    private JTable table;
    private AbstractTableModel model;

    private static final String COLUMNS[] = {Language.translate("Page Variable"), Language.translate("Value"), Language.translate("Format")/*, Language.translate("Description")*/};

    public PageVariablesPanel() {
        this.setLayout(new BorderLayout());
        initGUI();
    }

    public void refreshComponents() {
        this.model.fireTableDataChanged();
    }

    public void initGUI() {
        toolbar.setFloatable(false);
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEADING, 0, 0);
        toolbar.setLayout(flowLayout);

        final JButton addVar = new JButton(Workspace.createImageIcon("resources/add.gif"));
        addVar.setToolTipText(Language.translate("Add a new page variable"));
        addVar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addVariable();
            }
        });
        final JButton deleteVar = new JButton(Workspace.createImageIcon("resources/remove.gif"));
        deleteVar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteVariable();
            }
        });
        deleteVar.setToolTipText(Language.translate("Delete selected page variables"));
        deleteVar.setEnabled(false);

        toolbar.add(addVar);
        toolbar.add(deleteVar);
        model = new AbstractTableModel() {

            public String getColumnName(int col) {
                return COLUMNS[col].toString();
            }

            public int getRowCount() {
                return page().getPageVariableCount() + 1;
            }

            public int getColumnCount() {
                return COLUMNS.length;
            }

            public Object getValueAt(int row, int col) {
                if (row < page().getPageVariableCount()) {
                    String variableName = page().getPageVariableNames().get(row);

                    switch (col) {
                        case 0:
                            return variableName;
                        case 1:
                            return page().getPageVariableValue(variableName);
                        case 2:
                            return page().getPageVariableFormat(variableName);
                    }
                }
                return "";
            }

            public boolean isCellEditable(int row, int col) {
                return (row < page().getPageVariableCount()) || (row == page().getPageVariableCount() && col == 0);
            }

            public void setValueAt(Object value, int row, int col) {
                String variableName = null;
                if (row < page().getPageVariableCount()) {
                    variableName = page().getPageVariableNames().get(row);
                } else if (row == page().getPageVariableCount() && col == 0) {
                    addVariable(value.toString(), "");
                    DataServer.variablesServer.notifyChange(value.toString(), "", "");
                    DataServer.variablesServer.notifyChange(value.toString(), "", "");
                    fireTableDataChanged();
                    return;
                }

                if (variableName != null) {
                    switch (col) {
                        case 1:
                            SketchletContext.getInstance().getCurrentPageContext().setPageVariableValue(variableName, value.toString());
                            DataServer.variablesServer.notifyChange(variableName, value.toString(), value.toString());
                            break;
                        case 2:
                            SketchletContext.getInstance().getCurrentPageContext().setPageVariableFormat(variableName, value.toString());
                            DataServer.variablesServer.notifyChange(variableName, value.toString(), value.toString());
                            break;
                    }
                    fireTableCellUpdated(row, col);
                    SketchletContext.getInstance().repaint();
                }
            }

            public Class getColumnClass(int c) {
                return getValueAt(0, c).getClass();
            }
        };
        table = new JTable(model);
        TableColumn formatColumn = table.getColumnModel().getColumn(2);

        JComboBox comboBoxFormat = new JComboBox();
        comboBoxFormat.setEditable(true);
        comboBoxFormat.removeAllItems();
        comboBoxFormat.addItem("");

        comboBoxFormat.addItem("0");
        comboBoxFormat.addItem("00");
        comboBoxFormat.addItem("000");
        comboBoxFormat.addItem("0.0");
        comboBoxFormat.addItem("0.00");
        comboBoxFormat.addItem("0.000");

        formatColumn.setCellEditor(new DefaultCellEditor(comboBoxFormat));

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        TableColumn column = null;
        for (int i = 0; i < table.getModel().getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(100);
        }
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setDragEnabled(true);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }
                deleteVar.setEnabled(table.getSelectedRowCount() > 0 && table.getSelectedRow() < page().getPageVariableCount());
            }
        });

        table.getColumnModel().getColumn(1).setCellEditor(Notepad.getTableCellEditor());

        table.setTransferHandler(new LocalVariableTransferHandler());

        new FileDrop(System.out, this, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
            }

            public void dragOver(int x, int y) {
            }

            public void stringDropped(Point p, String strText) {
                if (strText.startsWith("=") && strText.length() > 1) {
                    String variableName = strText.substring(1).trim();
                    Variable variable = DataServer.variablesServer.getVariable(variableName);
                    String format = variable.format;
                    String value = variable.value;

                    DataServer.variablesServer.removeVariable(variableName);

                    addVariable(variableName, value, format);
                }
            }
        });

        this.add(toolbar, BorderLayout.NORTH);
        this.add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(120, 80);
    }

    private void deleteVariable() {
        int rows[] = table.getSelectedRows();

        java.util.List<String> variables = new ArrayList<String>();

        for (int row : rows) {
            variables.add(model.getValueAt(row, 0).toString());
        }

        for (String variableName : variables) {
            page().deletePageVariable(variableName);
        }

        model.fireTableDataChanged();
    }

    private PageContext page() {
        return SketchletContext.getInstance().getCurrentPageContext();
    }

    private boolean globalVariableExists(String name) {
        for (String globalVariableName : VariablesBlackboardContext.getInstance().getVariableNames()) {
            if (globalVariableName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean pageVariableExists(String name) {
        for (String variableName : page().getPageVariableNames()) {
            if (variableName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private void addVariable() {
        VariableDialog dialog = new VariableDialog(VariablesPanel.referenceFrame, Language.translate("Add New Variable"), "new-variable", "");
        dialog.pack();
        dialog.setLocationRelativeTo(VariablesPanel.referenceFrame);
        dialog.setVisible(true);

        if (dialog.isAccepted()) {
            addVariable(dialog.getName(), dialog.getValue());
        }
    }


    private boolean addVariable(String name, String value) {
        if (globalVariableExists(name.toString())) {
            JOptionPane.showMessageDialog(this, "The variable '" + name.toString() + "' already exists as a global variable.");
            return false;
        } else if (pageVariableExists(name.toString())) {
            JOptionPane.showMessageDialog(this, "The page variable '" + name.toString() + "' already exists.");
            return false;
        } else {
            page().addPageVariable(name, value);
            model.fireTableDataChanged();
            return true;
        }
    }


    private boolean addVariable(String name, String value, String format) {
        if (addVariable(name, value)) {
            page().setPageVariableFormat(name, format);
            return true;
        }

        return false;
    }


    public static void main(String args[]) {
        JFrame f = new JFrame();

        f.getContentPane().add(new PageVariablesPanel());

        f.pack();

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
