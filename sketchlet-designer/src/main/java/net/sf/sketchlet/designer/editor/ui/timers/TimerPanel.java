/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.timers;

import net.sf.sketchlet.common.file.FileDrop;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.ProgressMonitor;
import net.sf.sketchlet.designer.editor.ui.UIUtils;
import net.sf.sketchlet.model.programming.macros.Commands;
import net.sf.sketchlet.model.programming.timers.Timer;
import net.sf.sketchlet.model.programming.timers.TimerThread;
import net.sf.sketchlet.model.programming.timers.curves.Curves;
import net.sf.sketchlet.model.programming.timers.events.TimerEventsPanel;
import net.sf.sketchlet.util.SpringUtilities;
import net.sf.sketchlet.util.ui.DataRowFrame;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class TimerPanel extends JPanel implements ProgressMonitor {

    public JLabel labelName = new JLabel(Language.translate("Name: "));
    public JTextField fieldName = new JTextField(10);
    public JLabel labelDuration = new JLabel(Language.translate("Duration: "));
    public JTextField fieldDuration = new JTextField(3);
    public JLabel labelWait = new JLabel(Language.translate("Initial Delay: "));
    public JTextField fieldWait = new JTextField(3);
    public JLabel labelWaitAfter = new JLabel(Language.translate("Wait After: "));
    public JTextField fieldWaitAfter = new JTextField(3);
    // JLabel labelResolution = new JLabel("Resolution: ");
    // JTextField fieldResolution = new JTextField(10);
    public JCheckBox checkBoxLoop = new JCheckBox(Language.translate("Loop"));
    public JCheckBox checkBoxPulsar = new JCheckBox(Language.translate("Pulsar"));
    public JCheckBox checkBoxReset = new JCheckBox(Language.translate("Reset"));
    public JButton testBtn = new JButton(Language.translate("Test"));
    public boolean bTestTimerRunning = false;
    JButton resetBtn = new JButton(Language.translate("Reset"));
    JButton removeBtn = new JButton(Language.translate("Delete"));
    JButton duplicateBtn = new JButton(Language.translate("Duplicate"));
    JButton editBtn = new JButton(Language.translate("Edit"));
    JButton moveUp = new JButton(Language.translate("Move Up"));
    JButton moveDown = new JButton(Language.translate("Move Down"));
    JTable varTable;
    Timer timer;
    TimerThread timerThread;
    Vector<TimerThread> testTimers;
    int index;
    MyTableModel model;
    JProgressBar progressBar = new JProgressBar(0, 1000);
    public JComboBox comboCurve = Curves.getGlobalCurves().getComboBox();
    TimerEventsPanel timerEventsPanel;
    public JTabbedPane parentTab;
    public int tabIndex;

    public TimerPanel(final Timer timer, final Vector<TimerThread> testTimers, final int index) {
        this.testTimers = testTimers;
        timer.setPanel(this);
        this.index = index;
        this.fieldName.setText(timer.getName());
        this.comboCurve.setSelectedItem(timer.getDefaultCurve());
        this.fieldDuration.setText(timer.getStrDurationInSec());
        this.fieldWait.setText(timer.getStrPauseBefore());
        this.fieldWaitAfter.setText(timer.getStrPauseAfter());
        fieldName.addKeyListener(new KeyAdapter() {

            public void keyTyped(KeyEvent e) {
                if (!timer.getName().equals(fieldName.getText())) {
                    SketchletEditor.getInstance().saveTimerUndo(timer);
                    timer.setName(fieldName.getText());
                }
            }

            public void keyPressed(KeyEvent e) {
                keyTyped(e);
            }

            public void keyReleased(KeyEvent e) {
                keyTyped(e);
            }
        });
        fieldDuration.addKeyListener(new KeyAdapter() {

            public void keyTyped(KeyEvent e) {
                if (!timer.getStrDurationInSec().equals(fieldDuration.getText())) {
                    SketchletEditor.getInstance().saveTimerUndo(timer);
                    timer.setStrDurationInSec(fieldDuration.getText());
                }
            }

            public void keyPressed(KeyEvent e) {
                keyTyped(e);
            }

            public void keyReleased(KeyEvent e) {
                keyTyped(e);
            }
        });
        fieldWait.addKeyListener(new KeyAdapter() {

            public void keyTyped(KeyEvent e) {
                if (!timer.getStrPauseBefore().equals(fieldWait.getText())) {
                    SketchletEditor.getInstance().saveTimerUndo(timer);
                    timer.setStrPauseBefore(fieldWait.getText());
                }
            }

            public void keyPressed(KeyEvent e) {
                keyTyped(e);
            }

            public void keyReleased(KeyEvent e) {
                keyTyped(e);
            }
        });
        fieldWaitAfter.addKeyListener(new KeyAdapter() {

            public void keyTyped(KeyEvent e) {
                if (!timer.getStrPauseAfter().equals(fieldWaitAfter.getText())) {
                    SketchletEditor.getInstance().saveTimerUndo(timer);
                    timer.setStrPauseAfter(fieldWaitAfter.getText());
                }
            }

            public void keyPressed(KeyEvent e) {
                keyTyped(e);
            }

            public void keyReleased(KeyEvent e) {
                keyTyped(e);
            }
        });
        //this.fieldResolution.setText(timer.resolutionPerSec + "");
        this.checkBoxLoop.setSelected(timer.isLoop());
        checkBoxPulsar.setSelected(timer.isPulsar());
        checkBoxReset.setSelected(timer.isbResetAtEnd());

        this.checkBoxLoop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (timer.isLoop() != checkBoxLoop.isSelected()) {
                    SketchletEditor.getInstance().saveTimerUndo(timer);
                    timer.setLoop(checkBoxLoop.isSelected());
                }
            }
        });
        this.checkBoxPulsar.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (timer.isPulsar() != checkBoxPulsar.isSelected()) {
                    SketchletEditor.getInstance().saveTimerUndo(timer);
                    timer.setPulsar(checkBoxPulsar.isSelected());
                }
            }
        });
        this.checkBoxReset.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (timer.isbResetAtEnd() != checkBoxReset.isSelected()) {
                    SketchletEditor.getInstance().saveTimerUndo(timer);
                    timer.setbResetAtEnd(checkBoxReset.isSelected());
                }
            }
        });

        this.comboCurve.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String strCurve = (String) comboCurve.getSelectedItem();
                if (strCurve == null) {
                    strCurve = "";
                }
                if (!timer.getDefaultCurve().equals(strCurve)) {
                    SketchletEditor.getInstance().saveTimerUndo(timer);
                    timer.setDefaultCurve(strCurve);
                }
            }
        });


        JPanel header = new JPanel(new SpringLayout());

        JPanel panelName = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelName.add(labelName);
        panelName.add(fieldName);
        header.add(panelName);
        labelName.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(labelName);
        fieldName.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(fieldName);

        JPanel panelDuration = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelDuration.add(labelDuration);
        panelDuration.add(fieldDuration);
        panelDuration.add(labelWait);
        panelDuration.add(fieldWait);
        header.add(panelDuration);
        labelDuration.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(labelDuration);
        fieldDuration.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(fieldDuration);
        labelWait.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(labelWait);
        fieldWait.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(fieldWait);

        JPanel loopPulsar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        loopPulsar.add(checkBoxLoop);
        loopPulsar.add(checkBoxPulsar);
        loopPulsar.add(checkBoxReset);
        header.add(loopPulsar);

        checkBoxLoop.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(checkBoxLoop);
        checkBoxPulsar.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(checkBoxPulsar);
        checkBoxReset.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(checkBoxReset);

        SpringUtilities.makeCompactGrid(header,
                3, 1, //rows, cols
                0, 0, //initialX, initialY
                0, 0);//xPad, yPad    }

        setLayout(new BorderLayout());

        JPanel panelWest = new JPanel(new BorderLayout());
        panelWest.add(header, BorderLayout.NORTH);
        JPanel panelTest = new JPanel();
        panelTest.add(this.testBtn);
        panelTest.add(progressBar);
        panelWest.add(panelTest, BorderLayout.SOUTH);

        add(panelWest, BorderLayout.WEST);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        testBtn.setMargin(new Insets(0, 2, 0, 2));
        btnPanel.add(this.resetBtn);
        resetBtn.setMargin(new Insets(0, 2, 0, 2));
        btnPanel.add(this.removeBtn);
        removeBtn.setMargin(new Insets(0, 2, 0, 2));
        btnPanel.add(this.duplicateBtn);
        duplicateBtn.setMargin(new Insets(0, 2, 0, 2));
        btnPanel.add(this.moveUp);
        moveUp.setMargin(new Insets(0, 2, 0, 2));
        btnPanel.add(this.moveDown);
        moveDown.setMargin(new Insets(0, 2, 0, 2));
        btnPanel.add(this.editBtn);
        editBtn.setMargin(new Insets(0, 2, 0, 2));
        btnPanel.add(new JLabel(Language.translate("Default Curve:")));
        btnPanel.add(comboCurve);

        testBtn.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(testBtn);
        resetBtn.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(resetBtn);
        removeBtn.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(removeBtn);
        duplicateBtn.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(duplicateBtn);
        moveDown.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(moveDown);
        moveUp.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(moveUp);
        editBtn.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(editBtn);


        editBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = varTable.getSelectedRow();
                if (row >= 0) {
                    editTimerVariables(row);
                }
            }
        });

        bottomPanel.add(btnPanel, BorderLayout.CENTER);

        this.timer = timer;
        timer.setPanel(this);
        model = new MyTableModel();
        varTable = new JTable(model);
        varTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        varTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                enableControls();
            }
        });
        // varTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        varTable.setPreferredScrollableViewportSize(new Dimension(100, 200));

        JScrollPane varTableScrollPane = new JScrollPane(varTable);
        final JTabbedPane tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.LEFT);
        JPanel panelTable = new JPanel(new BorderLayout());
        panelTable.add(varTableScrollPane);

        panelTable.add(bottomPanel, BorderLayout.SOUTH);

        tabs.add(Language.translate("Interpolator"), panelTable);
        timerEventsPanel = new TimerEventsPanel(timer.getTimeline());
        tabs.add(Language.translate("Events"), timerEventsPanel);
        tabs.putClientProperty("JComponent.sizeVariant", "small");
        tabs.setSelectedIndex(timer.getTabIndex());
        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                timer.setTabIndex(tabs.getSelectedIndex());
            }
        });
        SwingUtilities.updateComponentTreeUI(tabs);
        add(tabs, BorderLayout.CENTER);

        setVariablesCombo();
        setFormatCombo();

        removeBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = varTable.getSelectedRow();
                if (row >= 0) {
                    int r = row;

                    for (int i = row; i < timer.getVariables().length - 1; i++) {
                        timer.getVariables()[i] = timer.getVariables()[i + 1];
                    }

                    timer.getVariables()[timer.getVariables().length - 1] = new Object[]{"", "", "", "", "", ""};

                    model.fireTableDataChanged();

                    varTable.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = varTable.getSelectedRow();
                if (row > 0) {
                    Object[] rowData1 = timer.getVariables()[row];
                    Object[] rowData2 = timer.getVariables()[row - 1];

                    timer.getVariables()[row] = rowData2;
                    timer.getVariables()[row - 1] = rowData1;

                    int r = row - 1;

                    model.fireTableDataChanged();

                    varTable.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        moveDown.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = varTable.getSelectedRow();
                if (row < timer.getVariables().length - 1) {
                    Object[] rowData1 = timer.getVariables()[row];
                    Object[] rowData2 = timer.getVariables()[row + 1];

                    timer.getVariables()[row] = rowData2;
                    timer.getVariables()[row + 1] = rowData1;

                    int r = row + 1;

                    model.fireTableDataChanged();

                    varTable.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        resetBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                for (int i = 0; i < timer.getVariables().length; i++) {
                    String strVar = (String) timer.getVariables()[i][0];
                    String strValue = (String) timer.getVariables()[i][1];

                    if (strVar.length() > 0) {
//                        VariablesBlackboard.variablesServer.updateVariable(strVar, strValue);
                        Commands.updateVariableOrProperty(this, strVar, strValue, Commands.ACTION_VARIABLE_UPDATE);
                    }
                }

            }
        });
        duplicateBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = varTable.getSelectedRow();
                if (row < timer.getVariables().length - 1) {
                    for (int i = timer.getVariables().length - 2; i >= row + 1; i--) {
                        timer.getVariables()[i + 1] = timer.getVariables()[i];
                    }

                    timer.getVariables()[row + 1] = new Object[]{
                            "" + timer.getVariables()[row][0],
                            "" + timer.getVariables()[row][1],
                            "" + timer.getVariables()[row][2],
                            "" + timer.getVariables()[row][3],
                            "" + timer.getVariables()[row][4],
                            "" + timer.getVariables()[row][5]};
                    //"" + timer.getVariables()[row][6],
                    //"" + timer.getVariables()[row][7],};

                    int r = row + 1;

                    model.fireTableDataChanged();
                    varTable.getSelectionModel().setSelectionInterval(r, r);
                }
            }
        });
        testBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                try {
                    if (!bTestTimerRunning) {
                        testBtn.setText(Language.translate("Stop Test"));
                        timer.setName(fieldName.getText());
                        timer.setDefaultCurve((String) comboCurve.getSelectedItem());
                        if (timer.getDefaultCurve() == null) {
                            timer.setDefaultCurve("");
                        }
                        timer.setStrDurationInSec(fieldDuration.getText());
                        timer.setLoop(checkBoxLoop.isSelected());
                        timer.setPulsar(checkBoxPulsar.isSelected());
                        timer.setbResetAtEnd(checkBoxReset.isSelected());
                        timer.setStrPauseBefore(fieldWait.getText());
                        timer.setStrPauseAfter(fieldWaitAfter.getText());
                        if (timerThread != null) {
                            timerThread.stop();
                            testTimers.remove(timerThread);
                        }

                        timerThread = timer.startThread("", testTimers, timer.getPanel());
                        timerThread.start();
                        if (parentTab != null) {
                            parentTab.setTitleAt(tabIndex, parentTab.getTitleAt(tabIndex) + "*");
                        }
                    } else {
                        if (timerThread != null) {
                            timerThread.stop();
                            timer.getTimeline().setDisplayPosition(0.0);
                            timerEventsPanel.repaint();
                            testTimers.remove(timerThread);
                        }
                        timerThread = null;

                        testBtn.setText(Language.translate("Test"));
                        if (parentTab != null) {
                            parentTab.setTitleAt(tabIndex, parentTab.getTitleAt(tabIndex).replace("*", ""));
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                bTestTimerRunning = !bTestTimerRunning;
            }
        });
        new FileDrop(System.out, varTableScrollPane, new FileDrop.Listener() {

            public void filesDropped(Point p, File[] files) {
            }

            public void dragOver(int x, int y) {
            }

            public void stringDropped(Point p, String strText) {
                DataRowFrame.emptyOnCancel = true;
                if (strText.startsWith("=") && strText.length() > 1) {
                    for (int i = 0; i < timer.getVariables().length; i++) {
                        if (timer.getVariables()[i][0].toString().isEmpty()) {
                            timer.getVariables()[i][0] = strText.substring(1);
                            editTimerVariables(i);
                            break;
                        }
                    }
                    DataRowFrame.emptyOnCancel = false;
                }
            }
        });
        varTable.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(varTable);

        enableControls();
    }

    public void refreshComponents() {
        UIUtils.refreshTable(this.varTable);
        this.fieldName.setText(timer.getName());
        this.comboCurve.setSelectedItem(timer.getDefaultCurve());
        this.fieldDuration.setText(timer.getStrDurationInSec());
        this.fieldWait.setText(timer.getStrPauseBefore());
        this.fieldWaitAfter.setText(timer.getStrPauseAfter() + "");
        this.checkBoxLoop.setSelected(timer.isLoop());
        this.checkBoxPulsar.setSelected(timer.isPulsar());
        this.checkBoxReset.setSelected(timer.isbResetAtEnd());
        this.timerEventsPanel.reload();
        this.timerEventsPanel.getTimelinePanel().repaint();
    }

    public void editTimerVariables(int row) {
        if (row >= 0) {
            String columns[] = {Language.translate("Variable"), Language.translate("Start value"), Language.translate("End value"), Language.translate("Format"), Language.translate("Curve")};
            Object editors[] = new Object[columns.length];

            editors[0] = DataRowFrame.cloneComboBox(comboBox);
            editors[1] = DataRowFrame.cloneComboBox(comboBox2);
            editors[2] = DataRowFrame.cloneComboBox(comboBox2);
            editors[3] = DataRowFrame.cloneComboBox(comboBoxFormat);
            //editors[4] = DataRowFrame.cloneComboBox(comboBox2);
            //editors[5] = DataRowFrame.cloneComboBox(comboBox2);
            editors[4] = Curves.getGlobalCurves().getComboBox();

            new DataRowFrame(SketchletEditor.editorFrame,
                    Language.translate("Timer Variable"),
                    row,
                    columns,
                    editors, null, null,
                    varTable,
                    model);
        }
    }

    public void enableControls() {
        removeBtn.setEnabled(varTable.getSelectedRow() >= 0);
        editBtn.setEnabled(varTable.getSelectedRow() >= 0);
        moveUp.setEnabled(varTable.getSelectedRow() > 0);
        moveDown.setEnabled(varTable.getSelectedRow() >= 0 && varTable.getSelectedRow() < timer.getVariables().length - 1);
        duplicateBtn.setEnabled(varTable.getSelectedRow() >= 0 && varTable.getSelectedRow() < timer.getVariables().length - 1);
    }

    JComboBox comboBoxFormat = new JComboBox();

    public void setFormatCombo() {
        TableColumn formatColumn = this.varTable.getColumnModel().getColumn(3);

        comboBoxFormat.setEditable(true);
        comboBoxFormat.removeAllItems();
        comboBoxFormat.addItem("");

        comboBoxFormat.addItem("0");
        comboBoxFormat.addItem("00");
        comboBoxFormat.addItem("000");
        comboBoxFormat.addItem("0.00");

        formatColumn.setCellEditor(new DefaultCellEditor(comboBoxFormat));
    }

    JComboBox comboBox = new JComboBox();
    JComboBox comboBox2 = new JComboBox();

    public void setVariablesCombo() {
        comboBox = new JComboBox();
        comboBox.setEditable(true);
        comboBox.addItem("");

        for (String strVar : VariablesBlackboard.getInstance().getVariablesList()) {
            comboBox.addItem(strVar);
        }
        varTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBox));

        comboBox2 = new JComboBox();
        comboBox2.setEditable(true);
        comboBox2.addItem("");

        for (String strVar : VariablesBlackboard.getInstance().getVariablesList()) {
            comboBox2.addItem("=" + strVar);
        }

        varTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBox2));
        varTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBox2));
        //varTable.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(comboBox2));
        //varTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(comboBox2));
        varTable.getColumnModel().getColumn(4).setCellEditor(new DefaultCellEditor(Curves.getGlobalCurves().getComboBox()));
    }

    class MyTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return timer.getColumns().length;
        }

        public int getRowCount() {
            return timer.getVariables().length;
        }

        public String getColumnName(int col) {
            return timer.getColumns()[col];
        }

        public Object getValueAt(int row, int col) {
            return timer.getVariables()[row][col];
        }

        public Class getColumnClass(int c) {
            return String.class;
        }

        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return col != 5;
        }

        public void setValueAt(Object value, int row, int col) {
            if (value != null && !value.toString().equals(timer.getVariables()[row][col].toString())) {
                SketchletEditor.getInstance().saveTimerUndo(timer);
                timer.getVariables()[row][col] = value;
                fireTableCellUpdated(row, col);
            }
        }
    }

    public void onStart() {
        progressBar.setValue(progressBar.getMinimum());
        timer.getTimeline().setDisplayPosition(0.0);
        timerEventsPanel.repaint();
    }

    public void onStop() {
        progressBar.setValue(progressBar.getMinimum());
        if (timerThread != null) {
            timerThread.stop();
            testTimers.remove(timerThread);
            timerThread = null;
        }
        testBtn.setText("Test");
        timer.getTimeline().setDisplayPosition(0.0);
        timerEventsPanel.repaint();
        if (parentTab != null) {
            parentTab.setTitleAt(tabIndex, parentTab.getTitleAt(tabIndex).replace("*", ""));
        }
        this.bTestTimerRunning = false;
    }

    public void setMinimum(int value) {
        progressBar.setMinimum(value);
    }

    public void setMaximum(int value) {
        progressBar.setMaximum(value);
    }

    public void variableUpdated(String name, String value) {
        model.fireTableDataChanged();
    }

    public void setValue(int value) {
        progressBar.setValue(value);
        progressBar.setString("" + value);
        timer.getTimeline().setDisplayPosition(((double) value - progressBar.getMinimum()) / (progressBar.getMaximum() - progressBar.getMinimum()));
        if (timer.isPulsar()) {
            if (timer.getTimeline().getDisplayPosition() <= 0.5) {
                timer.getTimeline().setDisplayPosition(timer.getTimeline().getDisplayPosition() * 2);
            } else {
                timer.getTimeline().setDisplayPosition((1 - timer.getTimeline().getDisplayPosition()) * 2);
            }
        }
        timerEventsPanel.repaint();
    }
}
