package net.sf.sketchlet.designer.editor.ui.variables;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.ui.SyntaxEditorWrapper;
import net.sf.sketchlet.framework.blackboard.VariableOperationsListener;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.framework.blackboard.Variable;
import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.ui.TextTransfer;
import net.sf.sketchlet.designer.editor.ui.connectors.PluginsFrame;
import net.sf.sketchlet.designer.editor.ui.desktop.Notepad;
import net.sf.sketchlet.designer.editor.dnd.TableTransferHandler;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.util.SpringUtilities;
import net.sf.sketchlet.util.UtilContext;
import org.apache.commons.lang.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Omnibook
 */
public class VariablesTablePanel extends JPanel {

    public int selectedRow = -1;
    public int selectedRow2 = -1;
    public boolean editable = true;
    public boolean bTableDirty = false;
    public boolean bTableUpdated = false;
    public TextTransfer clipboard = new TextTransfer();
    public VariablesTableModel variablesTableModel;
    JButton stopButton;
    JToolBar toolbar;
    VariablesPanel mainFrame;
    public JTable table;
    public JScrollPane scrollPane;
    VariablesTablePanel parent = this;
    public static VariablesTableInterface variablesTableInterface = null;

    public VariablesTablePanel(JToolBar toolbar, JToolBar toolbarDown, VariablesPanel mainFrame) {
        super(new BorderLayout());
        this.editable = true;
        this.toolbar = toolbar;
        this.mainFrame = mainFrame;

        variablesTableModel = new VariablesTableModel(mainFrame);

        table = new JTable(variablesTableModel);
        TableColumn formatColumn = table.getColumnModel().getColumn(4);

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
        //table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(1).setCellEditor(Notepad.getTableCellEditor());

        table.putClientProperty("JComponent.sizeVariant", "normal");
        table.setDefaultRenderer(String.class, new VariablesTableRenderer());
        SwingUtilities.updateComponentTreeUI(table);
        if (toolbar != null) {
            toolbar.setBorder(BorderFactory.createEmptyBorder());
        }
        // table.setRowHeight(30);
        table.getTableHeader().setReorderingAllowed(false);
        table.setDragEnabled(true);
        table.setTransferHandler(new TableTransferHandler());
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        TableColumn column = null;
        for (int i = 0; i < table.getModel().getColumnCount(); i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(variablesTableModel.columnWidths[i]);
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        table.addMouseListener(new PopupListener());

        JPanel commandsPanel = new JPanel();
        commandsPanel.setLayout(new FlowLayout());
        JButton addButton = new JButton(Language.translate("Add..."));
        addButton.setEnabled(editable);
        final JButton removeButton = new JButton(Language.translate("Remove"));
        removeButton.setEnabled(false);

        if (VariablesBlackboard.isPaused()) {
            stopButton = new JButton(Language.translate("Continue updates"));
            stopButton.setToolTipText(Language.translate("Continues to receive and process updates of variables send by modules"));
        } else {
            stopButton = new JButton(Language.translate("Stop updates"));
            stopButton.setToolTipText(Language.translate("Pause processing and ignores updates of variables send by modules"));
        }

        final JButton copyNameButton = new JButton(Language.translate("Copy variable name"));
        copyNameButton.setEnabled(false);

        commandsPanel.add(addButton);
        commandsPanel.add(removeButton);
        commandsPanel.add(copyNameButton);

        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                addVariable();
            }
        });
        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                removeVariable();
            }
        });

        copyNameButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                copyVariableName();
            }
        });


        stopButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                pauseUpdates();
            }
        });

        commandsPanel.add(stopButton);

        //Ask to be notified of selection changes.
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                    selectedRow = -1;
                    selectedRow2 = -1;
                } else {
                    selectedRow = lsm.getMinSelectionIndex();
                    selectedRow2 = lsm.getMaxSelectionIndex();
                    table.setToolTipText((String) table.getModel().getValueAt(selectedRow, 2));
                }
                enableControls();
            }
        });

        scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        add(scrollPane, BorderLayout.CENTER);

        if (toolbar != null) {
            final JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png", ""));
            help.setToolTipText(Language.translate("What are Variables?"));
            help.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    HelpUtils.openHelpFile("Variables", "variables");
                }
            });

            JToolBar toolbarHelp = new JToolBar();
            toolbarHelp.setFloatable(false);
            toolbarHelp.add(help);
            JPanel panelNorth = new JPanel(new BorderLayout());
            panelNorth.add(toolbar);
            panelNorth.add(toolbarHelp, BorderLayout.EAST);

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(panelNorth, BorderLayout.SOUTH);
            panel.add(toolbarDown, BorderLayout.NORTH);
            add(toolbarDown, BorderLayout.SOUTH);
            add(panelNorth, BorderLayout.NORTH);
        } else {
            add(commandsPanel, BorderLayout.SOUTH);
        }
        selectedRow = -1;
        selectedRow2 = -1;
        enableControls();
        new FileDrop(System.out, table, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
            }

            public void dragOver(int x, int y) {
            }

            public void stringDropped(Point p, String strText) {
                if (strText.startsWith("=") && strText.length() > 1) {
                    PageContext page = SketchletContext.getInstance().getCurrentPageContext();
                    String variableName = strText.substring(1).trim();
                    String value = page.getPageVariableValue(variableName);
                    String format = page.getPageVariableFormat(variableName);

                    page.deletePageVariable(variableName);

                    VariablesBlackboard.getInstance().updateVariable(variableName, value);
                    Variable variable = VariablesBlackboard.getInstance().getVariable(variableName);
                    variable.setFormat(format);
                }
            }
        });
    }

    public void addVariable() {
        VariableDialog dialog = new VariableDialog(VariablesPanel.referenceFrame, Language.translate("Add New Variable"), "new-variable", "");
        dialog.pack();
        dialog.setLocationRelativeTo(VariablesPanel.referenceFrame);
        dialog.setVisible(true);


        if (dialog.accepted) {
            getDataServer().updateVariable(dialog.strName, dialog.strValue, "", "");
        }
    }

    public void removeVariable() {
        if (selectedRow == -1 && selectedRow2 == -1) {
            return;
        }

        boolean paused = getDataServer().isPaused();
        getDataServer().setPaused(true);

        int start, end;

        if (selectedRow2 == -1) {
            start = end = selectedRow;
        } else {
            start = Math.min(selectedRow, selectedRow2);
            end = Math.max(selectedRow, selectedRow2);
        }

        for (int i = end; i >= start; i--) {
            if (i < VariablesTableModel.model.getRowCount()) {
                Variable v = VariablesTableModel.variableRows.elementAt(i);
                getDataServer().removeVariable(v.getName());
            }
        }

        getDataServer().setPaused(paused);
    }

    public void setFormat(String strFormat) {
        if (selectedRow == -1 && selectedRow2 == -1) {
            return;
        }

        boolean paused = getDataServer().isPaused();
        getDataServer().setPaused(true);

        int start, end;

        if (selectedRow2 == -1) {
            start = end = selectedRow;
        } else {
            start = Math.min(selectedRow, selectedRow2);
            end = Math.max(selectedRow, selectedRow2);
        }

        for (int i = end; i >= start; i--) {
            Variable v = VariablesTableModel.variableRows.elementAt(i);
            v.setFormat(strFormat);
        }

        getDataServer().setPaused(paused);
    }

    public void copyVariableName() {
        if (selectedRow < 0) {
            return;
        }
        Variable v = VariablesTableModel.variableRows.elementAt(selectedRow);
        clipboard.setClipboardContents(v.getName());
    }

    public void editVariable() {
        if (selectedRow >= 0) {
            Variable v = VariablesTableModel.variableRows.elementAt(selectedRow);
            this.editVariable(v);
        }
    }

    public void editVariable(final Variable variable) {
        final JFrame frame = new JFrame();
        frame.setLayout(new BorderLayout());
        final JPanel pane = new JPanel();
        pane.setLayout(new SpringLayout());

        pane.add(new JLabel(Language.translate("Variable Name")));
        final JLabel name = new JLabel(variable.getName());
        pane.add(name);
        pane.add(new JLabel(Language.translate(Language.translate("Value"))));
        final RSyntaxTextArea editor = Notepad.getInstance();
        editor.setRows(5);
        editor.setColumns(30);
        editor.setText(variable.getValue());
        pane.add(Notepad.getEditorPanel(new SyntaxEditorWrapper(editor), false, false));
        pane.add(new JLabel(Language.translate(Language.translate("Description"))));
        final JTextField description = new JTextField(variable.getDescription());
        pane.add(description);
        pane.add(new JLabel(Language.translate(Language.translate("Module"))));
        final JTextField module = new JTextField(variable.getGroup());
        pane.add(module);
        pane.add(new JLabel(Language.translate(Language.translate("Format"))));
        final JTextField format = new JTextField(variable.getFormat());
        pane.add(format);
        pane.add(new JLabel(Language.translate(Language.translate("Min"))));
        final JTextField min = new JTextField(variable.getMin());
        pane.add(min);
        pane.add(new JLabel(Language.translate(Language.translate("Max"))));
        final JTextField max = new JTextField(variable.getMax());
        pane.add(max);
        pane.add(new JLabel(Language.translate(Language.translate("Count"))));
        final JTextField count = new JTextField(variable.getCount());
        pane.add(count);
        pane.add(new JLabel(Language.translate(Language.translate("Count filter"))));
        final JTextField countFilter = new JTextField(variable.getCountFilter());
        pane.add(countFilter);
        pane.add(new JLabel(Language.translate(Language.translate("Time filter (ms)"))));
        final JTextField timeFilter = new JTextField(variable.getTimeFilterMs());
        pane.add(timeFilter);
        pane.add(new JLabel(Language.translate(Language.translate("Timestamp"))));
        final JLabel timestamp = new JLabel(variable.getTimestamp() + "");
        pane.add(timestamp);

        SpringUtilities.makeCompactGrid(pane,
                11, 2, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad

        final JPanel buttons = new JPanel();
        final JButton btnOk = new JButton(Language.translate("OK"), UtilContext.getInstance().getImageIconFromResources("resources/ok.png"));
        frame.getRootPane().setDefaultButton(btnOk);

        final JButton btnCancel = new JButton(Language.translate("Cancel"), UtilContext.getInstance().getImageIconFromResources("resources/cancel.png"));
        btnOk.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                variable.setValue(editor.getText());
                variable.setDescription(description.getText());
                variable.setFormat(format.getText());
                variable.setMin(min.getText());
                variable.setMax(max.getText());
                try {
                    if (StringUtils.isNotBlank(count.getText())) {
                        variable.setCount((int) Double.parseDouble(count.getText()));
                    }
                    if (StringUtils.isNotBlank(countFilter.getText())) {
                        variable.setCountFilter((int) Double.parseDouble(countFilter.getText()));
                    }
                    if (StringUtils.isNotBlank(timeFilter.getText())) {
                        variable.setTimeFilterMs((int) Double.parseDouble(timeFilter.getText()));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                variablesTableModel.fireTableDataChanged();
                frame.setVisible(false);
            }
        });
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                frame.setVisible(false);
            }
        });
        buttons.add(btnOk);
        buttons.add(btnCancel);

        frame.add(pane, BorderLayout.CENTER);
        frame.add(buttons, BorderLayout.SOUTH);

        frame.pack();

        frame.setLocationRelativeTo(mainFrame);
        frame.setVisible(true);
    }

    public void pauseUpdates() {
        VariablesBlackboard.setPaused(!VariablesBlackboard.isPaused());

        JMenuItem mi = (JMenuItem) mainFrame.menuItems.get("pausecomm");
        JButton b = (JButton) mainFrame.toolbarButtons.get("pausecomm");

        if (VariablesBlackboard.isPaused()) {
            stopButton.setText(Language.translate("Continue updates"));
            stopButton.setToolTipText(Language.translate("Continues to receive and process updates of variables send by modules"));

            if (mi != null) {
                mi.setText(Language.translate("Enable variable updates"));
                mi.setIcon(Workspace.createImageIcon("resources/start.gif", null));
            }
            if (b != null) {
                b.setIcon(Workspace.createImageIcon("resources/start.gif", null));
            }
        } else {
            stopButton.setText("Stop updates");
            stopButton.setToolTipText(Language.translate("Pause processing and ignores updates of variables send by modules"));
            if (mi != null) {
                mi.setText(Language.translate("Disable variable updates"));
                mi.setIcon(Workspace.createImageIcon("resources/stop.gif", null));
            }
            if (b != null) {
                b.setIcon(Workspace.createImageIcon("resources/stop.gif", null));
            }
        }
    }

    public void register() {
        getDataServer().getInstance().addVariableOperationsListener(new VariableOperationsListener() {

            public void variableAdded(String triggerVariable, String value) {
                bTableDirty = true;
            }

            public void variableUpdated(String triggerVariable, String value) {
                bTableUpdated = true;
            }

            public void variableDeleted(String triggerVariable) {
                bTableDirty = true;
            }
        });
    }

    public void networkConnectors() {
        PluginsFrame.showFrame();
    }

    public void calculateAggregateVariables() {
    }

    public void serializeVariable() {
    }

    public void setCountFilter() {
        try {
            int rows[] = table.getSelectedRows();
            int init = VariablesTableModel.variableRows.elementAt(rows[0]).getCountFilter();
            for (int i = 1; i < rows.length; i++) {
                Variable v = VariablesTableModel.variableRows.elementAt(rows[i]);
                if (v.getCountFilter() != init) {
                    init = 1;
                    break;
                }
            }

            String strValue = (String) JOptionPane.showInputDialog(Language.translate("Set count filter: "), "" + init);

            if (strValue != null) {
                int countFilter = Integer.parseInt(strValue);
                int n = 0;

                for (int i = 0; i < rows.length; i++) {
                    Variable v = VariablesTableModel.variableRows.elementAt(rows[i]);
                    v.setCountFilter(countFilter);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setTimeFilter() {
        try {
            int rows[] = table.getSelectedRows();
            int init = VariablesTableModel.variableRows.elementAt(rows[0]).getTimeFilterMs();
            for (int i = 1; i < rows.length; i++) {
                Variable v = VariablesTableModel.variableRows.elementAt(rows[i]);
                if (v.getTimeFilterMs() != init) {
                    init = 0;
                    break;
                }
            }

            String strValue = (String) JOptionPane.showInputDialog(Language.translate("Set time filter (ms): "), "" + init);

            if (strValue != null) {
                int timeFilterMs = Integer.parseInt(strValue);

                for (int i = 0; i < rows.length; i++) {
                    Variable v = VariablesTableModel.variableRows.elementAt(rows[i]);
                    v.setTimeFilterMs(timeFilterMs);
                    VariablesTableModel.model.fireTableRowsUpdated(rows[i], rows[i]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGroup() {
        int rows[] = table.getSelectedRows();
        String strGroup = VariablesTableModel.variableRows.elementAt(rows[0]).getGroup();
        for (int i = 1; i < rows.length; i++) {
            Variable v = VariablesTableModel.variableRows.elementAt(rows[i]);
            if (!v.getGroup().equalsIgnoreCase(strGroup)) {
                strGroup = "";
                break;
            }
        }

        strGroup = (String) JOptionPane.showInputDialog(Language.translate("Set group: "), strGroup);

        if (strGroup != null) {
            for (int i = 0; i < rows.length; i++) {
                Variable v = VariablesTableModel.variableRows.elementAt(rows[i]);
                v.setGroup(strGroup);
            }
        }

        variablesTableModel.fireTableDataChanged();
    }

    public VariablesBlackboard getDataServer() {
        return VariablesBlackboard.getInstance();
    }

    public void enableControls() {
        mainFrame.enableMenuAndToolbar(new String[]{"removevar", "copyvarname", "aggregatevalue", "serialize", "countfilter"}, selectedRow >= 0);
    }

    JPopupMenu popupMenu = new JPopupMenu();
    public static boolean inCtrlMode;

    class PopupListener extends MouseAdapter {

        JMenuItem menuItemCopyVariableName;
        JMenu menuItemCopySpreadsheet;
        JMenu menuItemCopySketches;
        JMenu menuItemCopyScript;
        JMenuItem menuItemRemove;
        JMenuItem menuItemAggregate;
        JMenuItem menuSerialize;
        JMenuItem menuSetCountFilter;
        JMenuItem menuSetTimeFilter;
        JMenuItem menuSetGroup;
        JMenuItem menuEdit;
        JMenu menuFormat;

        public PopupListener() {
            menuEdit = new JMenuItem(Language.translate("Edit..."));
            menuEdit.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    editVariable();
                }
            });
            menuItemCopyVariableName = new JMenuItem(Language.translate("Copy Variable Name"));
            menuItemCopyVariableName.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    copyVariableName();
                }
            });

            menuItemCopySpreadsheet = new JMenu(Language.translate("Copy Spreadsheet Formulas"));
            this.menuItemCopySketches = new JMenu(Language.translate("Copy Variable Names"));
            menuItemCopyScript = new JMenu(Language.translate("Copy Script Expressions"));
            CopyExpression.load(parent);
            CopyExpression.populateSpreadsheetsMenu(menuItemCopySpreadsheet);
            CopyExpression.populateSketchesMenu(menuItemCopySketches);
            CopyExpression.populateScriptsMenu(menuItemCopyScript);

            menuFormat = new JMenu(Language.translate("Format"));
            final String format[] = new String[]{"", "0", "0.0", "0.00", "0.000", "0.0000"};

            for (int fi = 0; fi < format.length; fi++) {
                final String strFormat = format[fi];
                JMenuItem mi = new JMenuItem(strFormat.isEmpty() ? Language.translate("No format") : strFormat);
                mi.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent event) {
                        setFormat(strFormat);
                    }
                });

                menuFormat.add(mi);
            }

            menuItemRemove = new JMenuItem(Language.translate("Remove"));
            menuItemRemove.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    removeVariable();
                }
            });
            menuItemAggregate = new JMenuItem(Language.translate("Calculate Aggregate Variables..."));
            menuItemAggregate.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    calculateAggregateVariables();
                }
            });
            menuSerialize = new JMenuItem(Language.translate("Serialize..."));
            menuSerialize.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    serializeVariable();
                }
            });

            menuSetCountFilter = new JMenuItem(Language.translate("Set count filter..."));
            menuSetCountFilter.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    setCountFilter();
                }
            });

            menuSetTimeFilter = new JMenuItem(Language.translate("Set time filter..."));
            menuSetTimeFilter.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    setTimeFilter();
                }
            });

            menuSetGroup = new JMenuItem(Language.translate("Set group..."));
            menuSetGroup.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    setGroup();
                }
            });


            popupMenu.add(menuEdit);
            popupMenu.addSeparator();
            popupMenu.add(menuFormat);
            popupMenu.addSeparator();
            popupMenu.add(menuItemCopySketches);
            popupMenu.add(menuItemCopySpreadsheet);

            popupMenu.add(menuItemCopyScript);
            popupMenu.addSeparator();

            popupMenu.add(menuItemRemove);

            popupMenu.addSeparator();
            popupMenu.add(menuSetTimeFilter);
            popupMenu.add(menuSetCountFilter);
            popupMenu.add(menuSetGroup);
        }

        public void mousePressed(MouseEvent e) {
            inCtrlMode = e.isControlDown();
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                if (table.getSelectedRow() == -1 || table.getSelectedRow() >= VariablesTableModel.variableRows.size()) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row == -1 || row >= VariablesTableModel.variableRows.size()) {
                        return;
                    }
                    table.getSelectionModel().setSelectionInterval(row, row);
                }

                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
