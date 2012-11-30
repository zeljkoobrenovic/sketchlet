package net.sf.sketchlet.designer.editor.ui.macros;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.ProgressMonitor;
import net.sf.sketchlet.framework.model.programming.macros.Macro;
import net.sf.sketchlet.framework.model.programming.macros.MacroThread;
import net.sf.sketchlet.framework.model.programming.screenscripts.AWTRobotUtil;
import net.sf.sketchlet.util.ui.DataRowFrame;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class MacroPanel extends JPanel implements ProgressMonitor {

    private Macro macro;
    public MacroCodePanel macroCodePanel;
    public JTable tableMacros;
    public AbstractTableModel model;
    int row, column;
    public JTextField macroName = new JTextField(6);
    String repeatTimes[] = {"Forever", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
    public JComboBox repeatField = new JComboBox(repeatTimes);
    JButton test = new JButton(Language.translate("Test"));
    boolean bTestMacroRunning = false;
    JButton refresh = new JButton(Language.translate("Reset"));
    JButton complete = new JButton(Language.translate("Complete Blocks"));
    JCheckBox checkHighlight = new JCheckBox(Language.translate("highlight execution"), false);
    JButton delete = new JButton(Language.translate("Delete"));
    JButton moveUp = new JButton(Language.translate("Move Up"));
    JButton moveDown = new JButton(Language.translate("Move Down"));
    JButton duplicate = new JButton(Language.translate("Duplicate"));
    JButton edit = new JButton(Language.translate("Edit"));
    JButton copy = new JButton(Language.translate("Copy"));
    JButton paste = new JButton(Language.translate("Paste"));
    public MacroThread macroThread;
    int currentEditor = -1;
    JComboBox comboBoxParam1 = new JComboBox();
    JProgressBar progressBar;
    MacroPanel parent = this;
    JScrollPane scrollPanel;
    public int numberOfActions = 0;
    public static ImageIcon iconDuplicate = Workspace.createImageIcon("resources/duplicate.png");
    public static ImageIcon iconInsertBefore = Workspace.createImageIcon("resources/duplicate_up_16.gif");
    public static ImageIcon iconInsertAfter = Workspace.createImageIcon("resources/duplicate_16.gif");
    public static ImageIcon iconMoveUp = Workspace.createImageIcon("resources/move_up_16.gif");
    public static ImageIcon iconMoveDown = Workspace.createImageIcon("resources/move_down_16.gif");
    public static ImageIcon iconEdit = Workspace.createImageIcon("resources/open_16.gif");
    public static ImageIcon iconDelete = Workspace.createImageIcon("resources/delete_16.png");
    Runnable saveUndoAction;
    private boolean bLoading = false;

    public MacroPanel(final Macro macro, boolean showName, boolean showRepeat) {
        this(macro, showName, showRepeat, 0);
    }

    public MacroPanel(final Macro macro, boolean showName, boolean showRepeat, final int index) {
        this(macro, showName, showRepeat, index, false);
    }

    public void setSaveUndoAction(Runnable undoAction) {
        this.saveUndoAction = undoAction;
    }

    int n = 0;

    private void saveUndo() {
        if (this.saveUndoAction != null && !bLoading) {
            this.saveUndoAction.run();
        }
    }

    public void dispose() {
        this.setMacro(null);
    }

    public MacroPanel(final Macro macro, boolean showName, boolean showRepeat, final int index, boolean bSmall) {
        this.setMacro(macro);
        this.numberOfActions = bSmall ? macro.getLastNonEmptyRow() + 7 : macro.getActions().length;
        repeatField.setEditable(true);
        macro.panel = this;
        setLayout(new BorderLayout());
        model = new MyTableModel();
        tableMacros = new JTable(model);
        //tableMacros.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        tableMacros.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        TableColumn col = tableMacros.getColumnModel().getColumn(0);
        int width = 40;
        col.setWidth(width);
        col.setMaxWidth(width);
        col.setPreferredWidth(width);
        col.setCellRenderer(new BlockCellRenderer(false));

        macroCodePanel = new MacroCodePanel();

        tableMacros.setPreferredScrollableViewportSize(new Dimension(100, 200));
        // JScrollPane panel = new JScrollPane(tableMacros);


        progressBar = new JProgressBar();
        // progressBar.setPreferredSize(new Dimension(80,8));

        tableMacros.addMouseListener(new MouseAdapter() {

            public void mouseMoved(MouseEvent me) {
                //int r = tableMacros.rowAtPoint(me.getPoint());
                //if (r >= 0 && r != row) {
                //    setColumnEditor((String) macro.getActions()[r][0]);
                //}
            }
        });
        tableMacros.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                //row = tableMacros.getSelectedRow();
                //if (row >= 0) {
                //    setColumnEditor((String) macro.getActions()[row][0]);
                //}
                //enableControls();
            }
        });

//            scrollPanel = new JScrollPane(tableMacros);
        scrollPanel = new JScrollPane(macroCodePanel);
        scrollPanel.getVerticalScrollBar().setUnitIncrement(16);
        new FileDrop(System.out, this, new FileDrop.Listener() {

            public void filesDropped(Point p, java.io.File[] files) {
            }

            public void dragOver(int x, int y) {
            }

            public void stringDropped(Point p, String strText) {
                DataRowFrame.emptyOnCancel = true;
                if (strText.startsWith("=") && strText.length() > 1) {
                    for (int i = 0; i < numberOfActions; i++) {
                        if (macro.getActions()[i][0].toString().isEmpty()) {
                            macro.getActions()[i][0] = "Variable update";
                            macro.getActions()[i][1] = strText.substring(1);
                            editMacroActions(i);
                            break;
                        }
                    }
                }
                if (strText.startsWith("@timer") && strText.length() > 1) {
                    for (int i = 0; i < numberOfActions; i++) {
                        if (macro.getActions()[i][0].toString().isEmpty()) {
                            macro.getActions()[i][0] = "Start timer";
                            macro.getActions()[i][1] = strText.substring(7);
                            editMacroActions(i);
                            break;
                        }
                    }
                }
                if (strText.startsWith("@sketch") && strText.length() > 1) {
                    for (int i = 0; i < numberOfActions; i++) {
                        if (macro.getActions()[i][0].toString().isEmpty()) {
                            macro.getActions()[i][0] = "Go to page";
                            macro.getActions()[i][1] = strText.substring(8);
                            editMacroActions(i);
                            break;
                        }
                    }
                }
                if (strText.startsWith("@macro") && strText.length() > 1) {
                    for (int i = 0; i < numberOfActions; i++) {
                        if (macro.getActions()[i][0].toString().isEmpty()) {
                            macro.getActions()[i][0] = "Start action";
                            macro.getActions()[i][1] = strText.substring(7);
                            editMacroActions(i);
                            break;
                        }
                    }
                }
                DataRowFrame.emptyOnCancel = false;
            }
        });


        if (showName && showRepeat) {
            FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
            flowLayout.setVgap(0);
            flowLayout.setHgap(0);
            JPanel namePanel = new JPanel(flowLayout);
            repeatField.setSelectedItem(macro.getRepeat() + "");
            JPanel btnPanel = new JPanel();
            btnPanel.setLayout(new GridLayout(5, 1));
            btnPanel.add(delete);
            btnPanel.add(duplicate);
            btnPanel.add(moveUp);
            btnPanel.add(moveDown);
            btnPanel.add(edit);

            JPanel panelRight = new JPanel(new BorderLayout());
            JLabel labelName = new JLabel(Language.translate("Name: "));
            namePanel.add(labelName);
            labelName.putClientProperty("JComponent.sizeVariant", "small");
            SwingUtilities.updateComponentTreeUI(labelName);
            macroName.putClientProperty("JComponent.sizeVariant", "small");
            SwingUtilities.updateComponentTreeUI(macroName);
            macroName.setText(macro.getName());
            namePanel.add(macroName);

            // panelRight.add(btnPanel, BorderLayout.CENTER);
            JLabel labelRepeat = new JLabel(Language.translate(" Repeat: "));
            namePanel.add(labelRepeat);
            labelRepeat.putClientProperty("JComponent.sizeVariant", "small");
            SwingUtilities.updateComponentTreeUI(labelRepeat);
            repeatField.putClientProperty("JComponent.sizeVariant", "small");
            SwingUtilities.updateComponentTreeUI(repeatField);
            namePanel.add(repeatField);
            namePanel.add(new JLabel("               "));
            namePanel.add(complete);
            namePanel.add(refresh);
            namePanel.add(new JLabel("    "));
            namePanel.add(test);
            namePanel.add(progressBar);
            namePanel.add(checkHighlight);
            namePanel.add(copy);
            namePanel.add(paste);
            this.add(panelRight, BorderLayout.EAST);

            this.add(namePanel, BorderLayout.SOUTH);
            this.add(scrollPanel, BorderLayout.CENTER);
            /*JPanel rightPanel = new JPanel(new BorderLayout());
            JPanel namePanel = new JPanel();
            namePanel.add(new JLabel("Name: "));
            macroName.setText(macro.name);
            namePanel.add(macroName);
            namePanel.add(new JLabel("  Repeat: "));
            repeatField.setSelectedItem(macro.repeat + "");
            namePanel.add(repeatField);
            rightPanel.add(namePanel, BorderLayout.NORTH);
            this.add(rightPanel, BorderLayout.WEST);
            JPanel bottomPanel = new JPanel(new BorderLayout());
            JPanel testPanel = new JPanel(new GridLayout(0, 2));
            JPanel btnPanel = new JPanel();
            btnPanel.setLayout(new GridLayout(5, 1));
            test.setMargin(new Insets(2, 2, 2, 2));
            refresh.setMargin(new Insets(2, 2, 2, 2));
            complete.setMargin(new Insets(2, 2, 2, 2));
            delete.setMargin(new Insets(2, 2, 2, 2));
            duplicate.setMargin(new Insets(2, 2, 2, 2));
            moveUp.setMargin(new Insets(2, 2, 2, 2));
            moveDown.setMargin(new Insets(2, 2, 2, 2));
            edit.setMargin(new Insets(2, 2, 2, 2));
            btnPanel.add(delete);
            btnPanel.add(duplicate);
            btnPanel.add(moveUp);
            btnPanel.add(moveDown);
            btnPanel.add(edit);
            // bottomPanel.add(btnPanel, BorderLayout.CENTER);
            testPanel.add(complete);
            testPanel.add(refresh);
            testPanel.add(test);
            testPanel.add(progressBar);
            testPanel.add(checkHighlight);
            
            JPanel testPanel2 = new JPanel(new BorderLayout());
            testPanel2.add(testPanel, BorderLayout.NORTH);
            
            rightPanel.add(testPanel2, BorderLayout.SOUTH);
            this.add(bottomPanel, BorderLayout.SOUTH);
            this.add(scrollPanel, BorderLayout.CENTER);
            // this.add(btnPanel, BorderLayout.EAST);*/
        } else if (!showName && showRepeat) {
            FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
            flowLayout.setVgap(0);
            flowLayout.setHgap(0);
            JPanel namePanel = new JPanel(flowLayout);
            repeatField.setSelectedItem(macro.getRepeat() + "");
            JPanel btnPanel = new JPanel();
            btnPanel.setLayout(new GridLayout(5, 1));
            btnPanel.add(delete);
            btnPanel.add(duplicate);
            btnPanel.add(moveUp);
            btnPanel.add(moveDown);
            btnPanel.add(edit);

            JPanel panelRight = new JPanel(new BorderLayout());

            // panelRight.add(btnPanel, BorderLayout.CENTER);
            JLabel labelRepeat = new JLabel(Language.translate(" Repeat: "));
            labelRepeat.putClientProperty("JComponent.sizeVariant", "small");
            SwingUtilities.updateComponentTreeUI(labelRepeat);
            repeatField.putClientProperty("JComponent.sizeVariant", "small");
            SwingUtilities.updateComponentTreeUI(repeatField);
            namePanel.add(labelRepeat);
            namePanel.add(repeatField);
            namePanel.add(new JLabel("               "));
            namePanel.add(complete);
            namePanel.add(refresh);
            namePanel.add(new JLabel("    "));
            namePanel.add(test);
            namePanel.add(progressBar);
            namePanel.add(checkHighlight);
            namePanel.add(copy);
            namePanel.add(paste);
            this.add(panelRight, BorderLayout.EAST);

            this.add(namePanel, BorderLayout.SOUTH);
            this.add(scrollPanel, BorderLayout.CENTER);
        }

        final MacroPanel thisPanel = this;

        refresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                parent.macroCodePanel.reload();
            }
        });

        complete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                macroCodePanel.save();
                macro.complete();
                macroCodePanel.reload();
            }
        });

        copy.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                macro.copy();
            }
        });

        paste.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                macroCodePanel.save();
                macro.paste();
                macroCodePanel.reload();
            }
        });

        test.putClientProperty("JComponent.sizeVariant", "small");
        complete.putClientProperty("JComponent.sizeVariant", "small");
        refresh.putClientProperty("JComponent.sizeVariant", "small");
        checkHighlight.putClientProperty("JComponent.sizeVariant", "small");
        copy.putClientProperty("JComponent.sizeVariant", "small");
        paste.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(test);
        SwingUtilities.updateComponentTreeUI(refresh);
        SwingUtilities.updateComponentTreeUI(checkHighlight);
        SwingUtilities.updateComponentTreeUI(complete);
        SwingUtilities.updateComponentTreeUI(copy);
        SwingUtilities.updateComponentTreeUI(paste);

        test.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                macroCodePanel.save();

                if (!bTestMacroRunning) {
                    if (macroThread != null) {
                        macroThread.stop();
                        macroThread = null;
                    }
                    macroThread = macro.startThread("", "", "", thisPanel);
                    test.setText(Language.translate("Stop Test"));
                } else {
                    if (macroThread != null) {
                        macroThread.stop();
                        macroThread = null;
                    }
                    test.setText(Language.translate("Test"));
                    if (macroThread != null) {
                        macroThread.stop();
                    }
                    if (prevCombo != null) {
                        prevCombo.setBackground(Color.CYAN);
                        prevCombo = null;
                    }
                }

                bTestMacroRunning = !bTestMacroRunning;
            }
        });
        moveUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (row > 0) {
                    saveUndo();
                    Object[] rowData1 = macro.getActions()[row];
                    Object[] rowData2 = macro.getActions()[row - 1];

                    macro.getActions()[row] = rowData2;
                    macro.getActions()[row - 1] = rowData1;

                    int r = row - 1;

                    model.fireTableDataChanged();

                    tableMacros.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveDown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (row < numberOfActions - 1) {
                    saveUndo();
                    Object[] rowData1 = macro.getActions()[row];
                    Object[] rowData2 = macro.getActions()[row + 1];

                    macro.getActions()[row] = rowData2;
                    macro.getActions()[row + 1] = rowData1;

                    int r = row + 1;

                    model.fireTableDataChanged();

                    tableMacros.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        duplicate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (row < numberOfActions - 1) {
                    saveUndo();
                    for (int i = numberOfActions - 2; i >= row + 1; i--) {
                        macro.getActions()[i + 1] = macro.getActions()[i];
                    }

                    macro.getActions()[row + 1] = new Object[]{
                            "" + macro.getActions()[row][0],
                            "" + macro.getActions()[row][1],
                            "" + macro.getActions()[row][2]};

                    int r = row + 1;

                    model.fireTableDataChanged();
                    tableMacros.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (row >= 0) {
                    saveUndo();
                    int r = row;

                    for (int i = row; i < numberOfActions - 1; i++) {
                        macro.getActions()[i] = macro.getActions()[i + 1];
                    }

                    macro.getActions()[numberOfActions - 1] = new Object[]{"", "", ""};

                    model.fireTableDataChanged();

                    tableMacros.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        edit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableMacros.getSelectedRow();
                editMacroActions(row);
            }
        });

        Macro.setCombos(tableMacros, comboBoxParam1, macro.getActions(), 1, 2);
        enableControls();
        tableMacros.getSelectionModel().setSelectionInterval(0, 0);
    }

    public void save() {
        getMacro().setName(macroName.getText());
        this.macroCodePanel.save();
    }

    public void reload() {
        this.macroCodePanel.reload();
    }

    public void editMacroActions(int row) {
        if (row >= 0) {
            saveUndo();
            Object editors[] = new Object[getMacro().columnNames.length];
            final JComboBox commandComboBox = new JComboBox();
            commandComboBox.setEditable(false);
            commandComboBox.setMaximumRowCount(20);

            for (int i = 0; i < getMacro().commandsEx.length; i++) {
                commandComboBox.addItem(getMacro().commandsEx[i]);
            }
            editors[0] = commandComboBox;
            final JComboBox newParamCombo = DataRowFrame.cloneComboBox(comboBoxParam1);
            editors[1] = newParamCombo;

            commandComboBox.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    String selItem = (String) newParamCombo.getSelectedItem();
                    String command = (String) commandComboBox.getSelectedItem();
                    Macro.loadParam1Combo(newParamCombo, selItem, command);
                }
            });

            new DataRowFrame(SketchletEditor.editorFrame,
                    Language.translate("Action"),
                    row,
                    getMacro().columnNames,
                    editors, null, null,
                    tableMacros,
                    model);

        }
    }

    public void enableControls() {
        this.delete.setEnabled(row >= 0);
        this.edit.setEnabled(row >= 0);
        this.moveUp.setEnabled(row > 0);
        this.moveDown.setEnabled(row >= 0 && row < numberOfActions - 1);
        this.duplicate.setEnabled(row >= 0 && row < numberOfActions - 1);
    }

    public Macro getMacro() {
        return macro;
    }

    public void setMacro(Macro macro) {
        this.macro = macro;
    }

    class MyTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return getMacro().columnNames.length;
        }

        public int getRowCount() {
            return numberOfActions;
        }

        public String getColumnName(int col) {
            return col == 0 ? "" : getMacro().columnNames[col - 1];
        }

        public Object getValueAt(int row, int col) {
            return getMacro().getActions()[row][col];
            /*if (col == 0) {
            return "";
            } else if (col == 1) {
            macro.calculateLevels();
            String strPrefix = "";
            for (int i = 0; i < macro.levels[row]; i++) {
            strPrefix += "   ";
            }
            return strPrefix + macro.getActions()[row][col - 1];
            } else {
            return macro.getActions()[row][col - 1];
            }*/
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editorPanel for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return String.class;
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return col > 0;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            getMacro().getActions()[row][col] = value;
            getMacro().calculateLevels();
            if (col == 2) {
                macroCodePanel.reload();
                ensureVisible(row);
            }

            /*            if (col > 0) {
            macro.getActions()[row][col - 1] = value;
            macro.calculateLevels();
            fireTableCellUpdated(row, col);
            }*/
        }
    }

    public void refreshComponents() {
        this.reload();
    }

    public void setColumnEditor(String strCommand) {
        for (int i = 0; i < getMacro().commandsEx.length; i++) {
            if (getMacro().commandsEx[i].equals(strCommand)) {
                Macro.loadParam1Combo(comboBoxParam1, getMacro().commandsEx[i], strCommand);
                break;
            }
        }
    }

    public void onStart() {
        progressBar.setValue(progressBar.getMinimum());
    }

    public void onStop() {
        progressBar.setValue(progressBar.getMinimum());
        if (macroThread != null) {
            macroThread.stop();
            macroThread = null;
        }
        test.setText("Test");
        this.bTestMacroRunning = false;
    }

    public void setMinimum(int value) {
        progressBar.setMinimum(value);
    }

    public void setMaximum(int value) {
        progressBar.setMaximum(value);
    }

    JComboBox prevCombo = null;

    public void setValue(int value) {
        progressBar.setValue(value);
        progressBar.setString("" + value);

        if (checkHighlight.isSelected()) {
            if (value > 0 && value <= numberOfActions) {
                if (!getMacro().getActions()[value - 1][0].toString().isEmpty()) {
                    JComboBox combo = this.macroCodePanel.combosAction[value - 1];
                    ensureVisible(value - 1);
                    combo.setBackground(Color.RED);
                    if (prevCombo != null) {
                        prevCombo.setBackground(Color.CYAN);
                    }
                    prevCombo = combo;
                }
            }
        }
    }

    public void ensureVisible(int index) {
        int h = macroCodePanel.getHeight();
        Rectangle rect = new Rectangle(0, (int) (((double) index / macroCodePanel.combosAction.length) * h), 20, 100);
        macroCodePanel.scrollRectToVisible(rect);
    }

    public void variableUpdated(String name, String value) {
    }

    class BlockCellRenderer extends JPanel implements TableCellRenderer {

        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;
        public static final int BLOCK_START = 0;
        public static final int BLOCK_PART = 1;
        public static final int BLOCK_END = 2;
        int type = BlockCellRenderer.BLOCK_PART;
        int level = 0;

        public BlockCellRenderer(boolean isBordered) {
            this.isBordered = isBordered;
            setOpaque(true); //MUST do this for background to show up.
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //super.paintComponent(g);


            for (int l = 0; l <= level; l++) {
                int x = l * 3;

                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));

                int t = l == level ? this.type : BLOCK_PART;

                switch (t) {
                    case BLOCK_START:
                        g2.drawLine(x, 5, 40, 5);
                        g2.drawLine(x, 5, x, 40);
                        break;
                    case BLOCK_PART:
                        g2.drawLine(x, 0, x, 40);
                        break;
                    case BLOCK_END:
                        g2.drawLine(x, 10, 40, 10);
                        g2.drawLine(x, 0, x, 10);
                        break;
                }
            }
        }

        public Component getTableCellRendererComponent(JTable table, Object _image, boolean isSelected, boolean hasFocus, int row, int column) {
            getMacro().calculateLevels();
            level = 0;

            String strCommand = "";
            if (row >= 0) {
                level = getMacro().getLevels()[row];
                strCommand = getMacro().getActions()[row][0].toString();
                if (strCommand.equalsIgnoreCase("if") || strCommand.equalsIgnoreCase("repeat")) {
                    type = BLOCK_START;
                    level++;
                } else if (strCommand.equalsIgnoreCase("end")) {
                    type = BLOCK_END;
                    level++;
                } else {
                    type = BLOCK_PART;
                }
            } else {
                type = BLOCK_PART;
            }


            if (isBordered) {
                if (isSelected) {
                    if (selectedBorder == null) {
                        selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                    }
                    setBorder(selectedBorder);
                } else {
                    if (unselectedBorder == null) {
                        unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                    }
                    setBorder(unselectedBorder);
                }
            }

            return this;
        }
    }

    class BlockPanel extends JPanel {

        Border unselectedBorder = null;
        Border selectedBorder = null;
        boolean isBordered = true;
        public static final int BLOCK_START = 0;
        public static final int BLOCK_PART = 1;
        public static final int BLOCK_END = 2;
        int type = BlockCellRenderer.BLOCK_PART;
        int level = 0;
        int index;

        public BlockPanel(int index, boolean isBordered) {
            this.index = index;
            this.isBordered = isBordered;
            this.setBorder(BorderFactory.createEmptyBorder());
            setOpaque(true); //MUST do this for background to show up.
        }

        public Dimension getPreferredSize() {
            level = getMacro().getLevels()[index];
            return new Dimension(20 + level * 10, 6);
        }

        public Dimension getMinimalSize() {
            return getPreferredSize();
        }

        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        protected void paintComponent(Graphics g) {
            prepare();
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            //super.paintComponent(g);


            for (int l = 1; l <= level; l++) {
                int x = l * 6;

                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2));

                int t = l == level ? this.type : BLOCK_PART;

                switch (t) {
                    case BLOCK_START:
                        g2.drawLine(x, 8, x + 40, 8);
                        g2.drawLine(x, 8, x, 40);
                        break;
                    case BLOCK_PART:
                        g2.drawLine(x, 0, x, 40);
                        break;
                    case BLOCK_END:
                        g2.drawLine(x, 10, x + 40, 10);
                        g2.drawLine(x, 0, x, 10);
                        break;
                }
            }
        }

        public void prepare() {
            getMacro().calculateLevels();
            level = 0;

            String strCommand = "";
            if (index >= 0) {
                level = getMacro().getLevels()[index];
                strCommand = getMacro().getActions()[index][0].toString();
                if (strCommand.equalsIgnoreCase("if") || strCommand.equalsIgnoreCase("repeat")) {
                    type = BLOCK_START;
                    level++;
                } else if (strCommand.equalsIgnoreCase("end")) {
                    type = BLOCK_END;
                    level++;
                } else {
                    type = BLOCK_PART;
                }
            } else {
                type = BLOCK_PART;
            }
        }
    }

    class MacroCodePanel extends JPanel {

        JPanel panelsBlock[];
        JComboBox combosAction[];
        JComboBox combosParam1[];
        JTextField fieldsParam2[];

        public MacroCodePanel() {
            panelsBlock = new JPanel[getMacro().getActions().length];
            combosAction = new JComboBox[getMacro().getActions().length];
            combosParam1 = new JComboBox[getMacro().getActions().length];
            fieldsParam2 = new JTextField[getMacro().getActions().length];

            this.preparePanel();
        }

        public void save() {
            if (combosAction == null) {
                return;
            }
            for (int i = 0; i < numberOfActions; i++) {
                if (combosAction[i] == null || combosParam1[i] == null || fieldsParam2[i] == null) {
                    return;
                }
                String strAction = (String) combosAction[i].getSelectedItem();
                String strParam1 = combosParam1[i].getEditor().getItem().toString();
                String strParam2 = fieldsParam2[i].getText();

                getMacro().getActions()[i][0] = strAction == null ? "" : strAction;
                getMacro().getActions()[i][1] = strParam1 == null ? "" : strParam1;
                getMacro().getActions()[i][2] = strParam2 == null ? "" : strParam2;
            }

            refresh();
        }

        public void reload() {
            bLoading = true;
            for (int i = 0; i < numberOfActions; i++) {
                final String strAction = (String) getMacro().getActions()[i][0];
                final String strParam1 = (String) getMacro().getActions()[i][1];
                final String strParam2 = (String) getMacro().getActions()[i][2];

                combosAction[i].setSelectedItem(strAction);
                combosParam1[i].setSelectedItem(strParam1);
                fieldsParam2[i].setText(strParam2);
            }
            refresh();
            bLoading = false;
        }

        public void refresh() {
            getMacro().calculateLevels();

            parent.revalidate();
            parent.repaint();
            revalidate();
            repaint();
            if (panelsBlock != null) {
                for (int i = 0; i < numberOfActions; i++) {
                    if (panelsBlock[i] == null) {
                        break;
                    }
                    panelsBlock[i].revalidate();
                    panelsBlock[i].repaint();
                }
            }
        }

        public void preparePanel() {
            preparePanel(0);
        }

        public void preparePanel(int start) {
            GridLayout gridLayout = new GridLayout(0, 3);
            gridLayout.setHgap(0);
            gridLayout.setVgap(0);
            setLayout(gridLayout);
            setBorder(BorderFactory.createEmptyBorder());

            getMacro().calculateLevels();

            for (int i = start; i < numberOfActions; i++) {
                final int row = i;
                final String strAction = (String) getMacro().getActions()[i][0];
                final String strParam1 = (String) getMacro().getActions()[i][1];
                final String strParam2 = (String) getMacro().getActions()[i][2];

                final JComboBox comboAction = new JComboBox(getMacro().commandsEx);
                comboAction.putClientProperty("JComponent.sizeVariant", "small");
                SwingUtilities.updateComponentTreeUI(comboAction);
                comboAction.setMaximumRowCount(20);
                comboAction.setSize(new Dimension(100, 6));
                comboAction.setBorder(BorderFactory.createEmptyBorder());
                final JComboBox comboParam1 = new JComboBox();
                comboParam1.putClientProperty("JComponent.sizeVariant", "small");
                SwingUtilities.updateComponentTreeUI(comboParam1);
                comboParam1.setMaximumRowCount(20);
                comboParam1.setSize(new Dimension(100, 6));
                comboParam1.setBorder(BorderFactory.createEmptyBorder());
                final JTextField fieldParam2 = new JTextField();
                fieldParam2.putClientProperty("JComponent.sizeVariant", "small");
                SwingUtilities.updateComponentTreeUI(fieldParam2);
                fieldParam2.addKeyListener(new KeyAdapter() {

                    public void keyTyped(KeyEvent e) {
                        getMacro().getActions()[row][2] = fieldParam2.getText();
                    }

                    public void keyPressed(KeyEvent e) {
                        saveUndo();
                        getMacro().getActions()[row][2] = fieldParam2.getText();
                    }

                    public void keyReleased(KeyEvent e) {
                        getMacro().getActions()[row][2] = fieldParam2.getText();
                    }
                });
                fieldParam2.setSize(new Dimension(100, 6));
                // fieldParam2.setBorder(BorderFactory.createEmptyBorder());
                BlockPanel blockPanel = new BlockPanel(i, false);
                panelsBlock[i] = blockPanel;
                combosAction[i] = comboAction;
                combosParam1[i] = comboParam1;
                fieldsParam2[i] = fieldParam2;

                comboParam1.setEditable(true);

                comboAction.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        int index = comboAction.getSelectedIndex();
                        if (index >= 0) {
                            saveUndo();
                            boolean bOldLoading = bLoading;
                            bLoading = true;
                            Macro.loadParam1Combo(comboParam1, (String) getMacro().getActions()[row][1], getMacro().commandsEx[index]);
                            Macro.loadParam2Field(comboParam1, fieldParam2, (String) getMacro().getActions()[index][1], getMacro().commandsEx[index]);
                            String strAction = (String) combosAction[row].getSelectedItem();

                            getMacro().getActions()[row][0] = strAction == null ? "" : strAction;

                            if (strAction.equalsIgnoreCase("if") || strAction.equalsIgnoreCase("repeat") || strAction.equalsIgnoreCase("end") || strAction.equalsIgnoreCase("wait until") || strAction.equalsIgnoreCase("wait for update") || strAction.equalsIgnoreCase("pause") || strAction.equalsIgnoreCase("pause (seconds)")) {
                                comboAction.setBackground(Color.YELLOW);
                            } else {
                                comboAction.setBackground(new JComboBox().getBackground());
                            }

                            refresh();
                            bLoading = bOldLoading;
                        }
                    }
                });
                comboAction.setSelectedItem(strAction);
                comboParam1.setSelectedItem(strParam1);
                fieldParam2.setText(strParam2);

                JPanel actionPanel = new JPanel(new BorderLayout());
                actionPanel.add(blockPanel, BorderLayout.WEST);
                actionPanel.add(comboAction, BorderLayout.CENTER);
                actionPanel.setBorder(BorderFactory.createEmptyBorder());
                add(actionPanel);
                add(comboParam1);
                comboParam1.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        saveUndo();
                        boolean bOldLoading = bLoading;
                        bLoading = true;
                        String strParam1 = combosParam1[row].getEditor().getItem().toString();
                        getMacro().getActions()[row][1] = strParam1 == null ? "" : strParam1;
                        bLoading = bOldLoading;
                    }
                });
                comboAction.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
                //comboAction.putClientProperty("JComponent.sizeVariant", "small");
                comboParam1.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
                //comboParam1.putClientProperty("JComponent.sizeVariant", "small");
                //fieldParam2.putClientProperty("JComponent.sizeVariant", "small");
                //SwingUtilities.updateComponentTreeUI(comboAction);
                //SwingUtilities.updateComponentTreeUI(comboParam1);
                //SwingUtilities.updateComponentTreeUI(fieldParam2);


                JToolBar toolbar = new JToolBar();
                toolbar.setMargin(new Insets(0, 0, 0, 0));
                toolbar.setFloatable(false);
                JButton delete = new JButton(iconDelete);
                delete.setMargin(new Insets(0, 0, 0, 0));
                delete.setToolTipText(Language.translate("delete command"));
                final JButton duplicate = new JButton(iconDuplicate);
                duplicate.setMargin(new Insets(0, 0, 0, 0));
                duplicate.setToolTipText(Language.translate("duplicate"));
                JButton insertAfter = new JButton(iconInsertAfter);
                insertAfter.setMargin(new Insets(0, 0, 0, 0));
                insertAfter.setToolTipText(Language.translate("insert after"));
                JButton insertBefore = new JButton(iconInsertBefore);
                insertBefore.setMargin(new Insets(0, 0, 0, 0));
                insertBefore.setToolTipText(Language.translate("insert before"));
                final JButton up = new JButton(iconMoveUp);
                up.setMargin(new Insets(0, 0, 0, 0));
                up.setToolTipText(Language.translate("move up"));
                final JButton down = new JButton(iconMoveDown);
                down.setMargin(new Insets(0, 0, 0, 0));
                down.setToolTipText(Language.translate("move down"));
                JButton edit = new JButton(iconEdit);
                edit.setMargin(new Insets(0, 0, 0, 0));
                edit.setToolTipText(Language.translate("edit command"));

                if (i == 0) {
                    up.setEnabled(false);
                }
                if (i == numberOfActions - 1) {
                    down.setEnabled(false);
                    insertAfter.setEnabled(false);
                    duplicate.setEnabled(false);
                }

                toolbar.add(duplicate);
                toolbar.add(insertBefore);
                toolbar.add(insertAfter);
                toolbar.add(up);
                toolbar.add(down);
                //toolbar.add(edit);
                toolbar.add(delete);
                up.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        if (row > 0) {
                            save();
                            Object[] rowData1 = getMacro().getActions()[row];
                            Object[] rowData2 = getMacro().getActions()[row - 1];

                            getMacro().getActions()[row] = rowData2;
                            getMacro().getActions()[row - 1] = rowData1;

                            Point pt1 = up.getLocationOnScreen();
                            Point pt2 = fieldsParam2[row - 1].getLocationOnScreen();

                            AWTRobotUtil.moveMouse(pt1.x + up.getWidth() / 2, pt2.y + fieldsParam2[row - 1].getHeight() / 2);

                            reload();
                        }
                    }
                });
                down.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        if (row < numberOfActions - 1) {
                            save();
                            Object[] rowData1 = getMacro().getActions()[row];
                            Object[] rowData2 = getMacro().getActions()[row + 1];

                            getMacro().getActions()[row] = rowData2;
                            getMacro().getActions()[row + 1] = rowData1;

                            Point pt1 = down.getLocationOnScreen();
                            Point pt2 = fieldsParam2[row + 1].getLocationOnScreen();

                            AWTRobotUtil.moveMouse(pt1.x + down.getWidth() / 2, pt2.y + fieldsParam2[row + 1].getHeight() / 2);

                            reload();
                        }
                    }
                });
                duplicate.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        if (row < numberOfActions - 1) {
                            save();
                            for (int i = numberOfActions - 2; i >= row + 1; i--) {
                                getMacro().getActions()[i + 1] = getMacro().getActions()[i];
                            }

                            getMacro().getActions()[row + 1] = new Object[]{"" + getMacro().getActions()[row][0], "" + getMacro().getActions()[row][1], "" + getMacro().getActions()[row][2]};

                            Point pt1 = duplicate.getLocationOnScreen();
                            Point pt2 = fieldsParam2[row + 1].getLocationOnScreen();

                            AWTRobotUtil.moveMouse(pt1.x + duplicate.getWidth() / 2, pt2.y + fieldsParam2[row + 1].getHeight() / 2);

                            reload();
                        }
                    }
                });
                insertAfter.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        if (row < numberOfActions - 1) {
                            save();
                            for (int i = numberOfActions - 2; i >= row + 1; i--) {
                                getMacro().getActions()[i + 1] = getMacro().getActions()[i];
                            }

                            getMacro().getActions()[row + 1] = new Object[]{"", "", ""};

                            reload();
                        }
                    }
                });
                insertBefore.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        if (row >= 0) {
                            save();
                            for (int i = numberOfActions - 2; i >= row; i--) {
                                getMacro().getActions()[i + 1] = getMacro().getActions()[i];
                            }

                            getMacro().getActions()[row] = new Object[]{"", "", ""};
                            reload();
                        }
                    }
                });
                delete.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        if (row >= 0) {
                            save();

                            for (int i = row; i < numberOfActions - 1; i++) {
                                getMacro().getActions()[i] = getMacro().getActions()[i + 1];
                            }

                            getMacro().getActions()[numberOfActions - 1] = new Object[]{"", "", ""};

                            reload();
                        }
                    }
                });
                edit.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        save();
                        editMacroActions(row);
                        reload();
                    }
                });

                BorderLayout layout = new BorderLayout();
                JPanel panelCmds = new JPanel(layout);
                panelCmds.add(fieldParam2, BorderLayout.CENTER);
                panelCmds.add(toolbar, BorderLayout.EAST);

                fieldParam2.setBorder(BorderFactory.createEmptyBorder());
                toolbar.setBorder(BorderFactory.createEmptyBorder());
                panelCmds.setBorder(BorderFactory.createEmptyBorder());

                add(panelCmds);
            }

            if (numberOfActions < getMacro().getActions().length) {
                final JButton btnExpand = new JButton(Language.translate("more commands..."));
                btnExpand.putClientProperty("JComponent.sizeVariant", "small");
                SwingUtilities.updateComponentTreeUI(btnExpand);
                btnExpand.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        save();
                        remove(btnExpand);
                        int start = numberOfActions;
                        numberOfActions = getMacro().getActions().length;
                        preparePanel(start);
                    }
                });
                add(btnExpand);
            }
        }
    }
}
