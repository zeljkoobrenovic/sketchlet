/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.page;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.programming.timers.curves.Curves;
import net.sf.sketchlet.designer.ui.UIUtils;
import net.sf.sketchlet.designer.ui.eventpanels.KeyboardEventsPanel;
import net.sf.sketchlet.designer.ui.eventpanels.MouseEventsPanel;
import net.sf.sketchlet.designer.ui.macros.MacroPanel;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.designer.ui.properties.PropertiesSetPanel;
import net.sf.sketchlet.designer.ui.region.PropertiesTableRenderer;
import net.sf.sketchlet.help.HelpUtils;

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

/**
 * @author zobrenovic
 */
public class SketchStatePanel extends JPanel {

    public static int actionsTabIndex = 0;
    public static int actionsOnEntrySubtabIndex = 0;
    public static int actionsOnExitSubtabIndex = 1;
    public static int actionsVariablesSubtabIndex = 2;
    public static int actionsKeyboardSubtabIndex = 3;
    public static int actionsMouseSubtabIndex = 3;
    public static int propertiesTabIndex = 1;
    public static int perspectiveTabIndex = 2;
    public static int spreadsheetTabIndex = 3;
    public JTabbedPane tabs = new JTabbedPane();
    public JTabbedPane tabs1 = new JTabbedPane();
    public JTabbedPane tabs2 = new JTabbedPane();
    public static SketchStatePanel statePanel;
    int height = 180;
    double _start = 0.0;
    double _end = 0.0;
    double _init = 0.0;
    boolean bCanUpdate = true;
    public static boolean bDockedPanel = true;

    public SketchStatePanel() {
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);

        statePanel = this;

        JButton btnClose = new JButton(Workspace.createImageIcon("resources/close_small.png"));
        btnClose.setToolTipText(Language.translate("Close"));
        btnClose.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                SketchletEditor.showStatePanel();
            }
        });

        final JButton btnUndock = new JButton(Workspace.createImageIcon("resources/undock.png"));
        btnUndock.setToolTipText("undock");
        btnUndock.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (SketchStatePanel.bDockedPanel) {
                    SketchletEditor.undockStatePanel();
                    btnUndock.setToolTipText(Language.translate("dock"));
                } else {
                    SketchletEditor.dockStatePanel();
                    btnUndock.setToolTipText(Language.translate("undock"));
                }

                SketchStatePanel.bDockedPanel = !SketchStatePanel.bDockedPanel;
            }
        });

        JToolBar tbRight = new JToolBar();
        tbRight.setFloatable(false);
        tbRight.setBorder(BorderFactory.createEmptyBorder());
        tbRight.setOrientation(JToolBar.VERTICAL);
        tbRight.add(btnClose);
        tbRight.add(btnUndock);
        final JButton help = new JButton("", Workspace.createImageIcon("resources/help-browser.png", ""));
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                switch (tabs.getSelectedIndex()) {
                    case 0:
                        HelpUtils.openHelpFile("Sketchlet Actions", "sketchlet_actions");
                        break;
                    case 1:
                        HelpUtils.openHelpFile("Sketchlet Properties", "sketchlet_properties");
                        break;
                    case 2:
                        HelpUtils.openHelpFile("Perspective", "sketchlet_perspective");
                        break;
                }
            }
        });

        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!tabs.isShowing()) {
                    return;
                }
                int n = tabs.getSelectedIndex();
                if (n == SketchStatePanel.actionsTabIndex) {
                    SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("sketchlet_actions");
                } else if (n == SketchStatePanel.propertiesTabIndex) {
                    SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("sketchlet_properties");
                } else if (n == SketchStatePanel.perspectiveTabIndex) {
                    SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("sketchlet_perspective");
                } else if (n == SketchStatePanel.spreadsheetTabIndex) {
                    SketchletEditor.editorPanel.helpViewer.showAutoHelpByID("sketchlet_spreadsheet");
                }
            }
        });
        TutorialPanel.prepare(tabs);

        final JButton maximize = new JButton(Workspace.createImageIcon("resources/maximize_16.png"));
        maximize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (height == 180) {
                    height = 350;
                    maximize.setIcon(Workspace.createImageIcon("resources/minimize_16.png"));
                } else {
                    height = 180;
                    maximize.setIcon(Workspace.createImageIcon("resources/maximize_16.png"));
                }
                revalidate();
                repaint();
            }
        });
        help.setToolTipText("What are page events?");
        help.setMargin(new Insets(0, 0, 0, 0));
        tbRight.add(help);

        tbRight.addSeparator();
        tbRight.add(maximize);

        tabs1.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabs1);

        // add(tbRight, BorderLayout.EAST);

        load();
    }

    public MacroPanel pOnEntry;
    public MacroPanel pOnExit;
    // public StateEventHandlerPanel stateEventHandlerPanel;
    public KeyboardEventsPanel keyboardEventsPanel;
    public MouseEventsPanel mouseEventsPanel;
    public VariableUpdateEventsPanel variableUpdateEventsPanel;
    public PropertiesSetPanel setPanel;

    public void save() {
        pOnEntry.save();
        pOnExit.save();
    }

    public void refreshComponenets() {
        this.pOnEntry.reload();
        this.pOnExit.reload();
        UIUtils.refreshTable(this.tableAnimation);
        this.keyboardEventsPanel.refresh();
        this.mouseEventsPanel.refresh();
        this.variableUpdateEventsPanel.refresh();
        UIUtils.refreshTable(this.setPanel.table);
    }

    public void load() {
        int nTab = tabs.getSelectedIndex();
        tabs.setTabPlacement(JTabbedPane.LEFT);
        tabs.removeAll();
        int nTab1 = tabs1.getSelectedIndex();
        tabs1.removeAll();
        int nTab2 = tabs1.getSelectedIndex();
        tabs2.removeAll();
        pOnEntry = new MacroPanel(SketchletEditor.editorPanel.currentPage.onEntryMacro, false, true);
        pOnEntry.setSaveUndoAction(new Runnable() {

            public void run() {
                SketchletEditor.editorPanel.saveSketchUndo();
            }
        });
        pOnExit = new MacroPanel(SketchletEditor.editorPanel.currentPage.onExitMacro, false, true);
        pOnExit.setSaveUndoAction(new Runnable() {

            public void run() {
                SketchletEditor.editorPanel.saveSketchUndo();
            }
        });
        variableUpdateEventsPanel = new VariableUpdateEventsPanel(SketchletEditor.editorPanel.currentPage);
        keyboardEventsPanel = new KeyboardEventsPanel(SketchletEditor.editorPanel.getSketch().keyboardProcessor);
        mouseEventsPanel = new MouseEventsPanel(SketchletEditor.editorPanel.getSketch().mouseProcessor);
        tabs1.setTabPlacement(JTabbedPane.LEFT);
        tabs1.addTab("", Workspace.createImageIcon("resources/entry2.gif"), pOnEntry, Language.translate("On Page Entry"));
        tabs1.addTab("", Workspace.createImageIcon("resources/exit2.gif"), pOnExit, Language.translate("On Page Exit"));
        if (Profiles.isActive("variables")) {
            tabs1.addTab("", Workspace.createImageIcon("resources/variable_in.png"), variableUpdateEventsPanel, Language.translate("On Variable Updates"));
        }
        SketchStatePanel.actionsKeyboardSubtabIndex = tabs1.getTabCount();
        tabs1.addTab("", Workspace.createImageIcon("resources/keyboard2.png"), keyboardEventsPanel, Language.translate("On Keyboard Events"));
        SketchStatePanel.actionsMouseSubtabIndex = tabs1.getTabCount();
        tabs1.addTab("", Workspace.createImageIcon("resources/mouse.png"), mouseEventsPanel, Language.translate("On Mouse Events"));
        setPanel = getPanelSet();
        setPanel.setSaveUndoAction(new Runnable() {

            public void run() {
                SketchletEditor.editorPanel.saveSketchUndo();
            }
        });

        tabs2.add(Language.translate("Set Properties"), setPanel);
        tabs2.add(Language.translate("Animate Properties"), getPanelAnimate());
        tabs2.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabs2);

        if (Profiles.isActive("page_actions")) {
            SketchStatePanel.actionsTabIndex = tabs.getTabCount();
            tabs.add(tabs1);
            tabs.setIconAt(tabs.getTabCount() - 1, Workspace.createImageIcon("resources/actions.png"));
            tabs.setToolTipTextAt(tabs.getTabCount() - 1, Language.translate("Page Events"));
        }
        if (Profiles.isActive("page_properties")) {
            SketchStatePanel.propertiesTabIndex = tabs.getTabCount();
            tabs.add(tabs2);
            tabs.setIconAt(tabs.getTabCount() - 1, Workspace.createImageIcon("resources/details.gif"));
            tabs.setToolTipTextAt(tabs.getTabCount() - 1, Language.translate("Page Properties"));
        }
        if (Profiles.isActive("page_perspective")) {
            SketchStatePanel.perspectiveTabIndex = tabs.getTabCount();
            tabs.add(SketchletEditor.editorPanel.perspectivePanel);
            tabs.setIconAt(tabs.getTabCount() - 1, Workspace.createImageIcon("resources/perspective_lines.png"));
            tabs.setToolTipTextAt(tabs.getTabCount() - 1, Language.translate("3D Perspective"));
        }
        if (Profiles.isActive("page_spreadsheet")) {
            SketchStatePanel.spreadsheetTabIndex = tabs.getTabCount();
            JPanel sPane = new JPanel(new BorderLayout());
            sPane.add(SketchletEditor.editorPanel.spreadsheetPanel, BorderLayout.CENTER);
            tabs.add(sPane);
            tabs.setIconAt(tabs.getTabCount() - 1, Workspace.createImageIcon("resources/spreadsheet-icon.png"));
            tabs.setToolTipTextAt(tabs.getTabCount() - 1, Language.translate("Spreadsheet"));
        }

        if (nTab >= 0 && nTab < tabs.getTabCount()) {
            tabs.setSelectedIndex(nTab);
        }
        if (nTab1 >= 0 && nTab1 < tabs1.getTabCount()) {
            tabs1.setSelectedIndex(nTab1);
        }
        if (nTab2 >= 0 && nTab2 < tabs2.getTabCount()) {
            tabs2.setSelectedIndex(nTab2);
        }
    }

    public static SketchStatePanel showStateProperties(Page page) {
        return showStateProperties(SketchStatePanel.propertiesTabIndex, 0);
    }

    public static SketchStatePanel showStateProperties(int tabIndex, int subTabIndex) {
        if (!SketchletEditor.editorPanel.bShowStatePanel) {
            SketchletEditor.editorPanel.showStatePanel();
        }
        statePanel.tabs.setSelectedIndex(tabIndex);
        if (tabIndex == 0) {
            statePanel.tabs1.setSelectedIndex(subTabIndex);
        } else {
            statePanel.tabs2.setSelectedIndex(subTabIndex);
        }
        return statePanel;
    }

    public int getFreeEntryRow() {
        return getFreeRow(this.pOnEntry.model, 0);
    }

    public int getFreeExitRow() {
        return getFreeRow(this.pOnExit.model, 0);
    }

    public int getFreeRow(AbstractTableModel model, int column) {
        int row = -1;
        for (int i = 0; i < model.getRowCount(); i++) {
            String str = (String) model.getValueAt(i, column);
            if (str.isEmpty()) {
                row = i;
                break;
            }
        }

        return row;
    }

    public void hideProperties() {
        this.save();
        if (pOnEntry.macroThread != null) {
            pOnEntry.macroThread.stop();
        }
        if (pOnExit.macroThread != null) {
            pOnExit.macroThread.stop();
        }
    }

    public static void main(String args[]) {
        showStateProperties(null);
    }

    public Dimension getPreferredSize() {
        return new Dimension(100, height);
    }

    public PropertiesSetPanel getPanelSet() {
        return new PropertiesSetPanel(SketchletEditor.editorPanel.currentPage);
    }

    String[] columnNamesAnimation = {Language.translate("Dimension"), Language.translate("Animation Type"), Language.translate("Start Value"), Language.translate("End Value"), Language.translate("Cycle Duration"), Language.translate("Curve")};
    JTable tableAnimation;

    public JPanel getPanelAnimate() {
        final AbstractTableModel modelAnimation = new AbstractTableModel() {

            public String getColumnName(int col) {
                return columnNamesAnimation[col].toString();
            }

            public int getRowCount() {
                if (SketchletEditor.editorPanel == null || SketchletEditor.editorPanel.currentPage == null) {
                    return 0;
                } else {
                    return SketchletEditor.editorPanel.currentPage.propertiesAnimation.length;
                }
            }

            public int getColumnCount() {
                return columnNamesAnimation.length;
            }

            public Object getValueAt(int row, int col) {
                if (SketchletEditor.editorPanel == null || SketchletEditor.editorPanel.currentPage == null) {
                    return "";
                } else {
                    return SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][col] == null ? "" : SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][col];
                }
            }

            public boolean isCellEditable(int row, int col) {
                return SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.currentPage != null && col > 0 && SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][1] != null;
            }

            public void setValueAt(Object value, int row, int col) {
                if (SketchletEditor.editorPanel == null || SketchletEditor.editorPanel.currentPage == null) {
                    return;
                }
                SketchletEditor.editorPanel.saveSketchUndo();
                SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][col] = value.toString();
                if (col == 1) {
                    if (value == null || value.toString().isEmpty()) {
                        SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][2] = "";
                        SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][3] = "";
                        SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][4] = "";
                    } else {
                        String strProperty = SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][0];
                        String start = SketchletEditor.editorPanel.currentPage.getMinValue(strProperty);
                        String end = SketchletEditor.editorPanel.currentPage.getMaxValue(strProperty);

                        if (SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][2].isEmpty() && start != null) {
                            SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][2] = start;
                        }
                        if (SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][3].isEmpty() && end != null) {
                            SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][3] = end;
                        }
                        if (SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][4].isEmpty()) {
                            SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][4] = "1.0";
                        }
                    }

                    this.fireTableRowsUpdated(row, row);
                    if (tableAnimation != null) {
                        TutorialPanel.addLine("cmd", "Set the page animation property " + SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][0], "details.gif", tableAnimation.getParent());
                    }
                }
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };
        JPanel panelSet = new JPanel(new BorderLayout());

        tableAnimation = new JTable(modelAnimation);
        TableColumn col = tableAnimation.getColumnModel().getColumn(2);
        tableAnimation.setDefaultRenderer(String.class, new PropertiesTableRenderer(SketchletEditor.editorPanel.currentPage.propertiesAnimation));

        col = tableAnimation.getColumnModel().getColumn(1);
        JComboBox animationType = new JComboBox();
        animationType.setEditable(true);
        animationType.addItem("");
        //animationType.addItem("No Animation");
        animationType.addItem("Loop Forever");
        animationType.addItem("Loop Once");
        animationType.addItem("Puls Forever");
        animationType.addItem("Puls Once");
        col.setCellEditor(new DefaultCellEditor(animationType));

        JComboBox animationTime = new JComboBox();
        animationTime.setEditable(true);
        animationTime.addItem("");
        animationTime.addItem("1");
        animationTime.addItem("2");
        animationTime.addItem("3");
        animationTime.addItem("4");
        animationTime.addItem("5");
        animationTime.addItem("6");
        animationTime.addItem("7");
        animationTime.addItem("8");
        animationTime.addItem("9");
        animationTime.addItem("10");
        col = tableAnimation.getColumnModel().getColumn(4);
        col.setCellEditor(new DefaultCellEditor(animationTime));

        col = tableAnimation.getColumnModel().getColumn(5);
        col.setCellEditor(new DefaultCellEditor(Curves.globalCurves.getComboBox()));

        tableAnimation.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tableAnimation.setFillsViewportHeight(true);

        panelSet.add(new JScrollPane(tableAnimation), BorderLayout.CENTER);

        JPanel panelButtons = new JPanel(new BorderLayout());
        final JButton btnClear = new JButton(Language.translate("Clear"));
        btnClear.setEnabled(false);
        btnClear.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAnimation.getSelectedRow();
                if (row >= 0 && SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][1] != null) {
                    SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][1] = "";
                    SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][2] = "";
                    SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][3] = "";
                    SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][4] = "";
                    SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][5] = "";
                    modelAnimation.fireTableDataChanged();
                }
            }
        });
        JPanel buttonsPanel = new JPanel(new BorderLayout());
        final JTextField startField = new JTextField(5);
        final JSlider slider = new JSlider(JSlider.VERTICAL, 0, 100, 0);
        final JTextField endField = new JTextField(5);
        startField.setHorizontalAlignment(JTextField.CENTER);
        endField.setHorizontalAlignment(JTextField.CENTER);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!bCanUpdate) {
                    return;
                }

                bCanUpdate = false;
                try {
                    int row = tableAnimation.getSelectedRow();
                    if (row >= 0) {
                        String start = startField.getText();
                        String end = endField.getText();
                        if (start != null && end != null) {
                            _start = Double.parseDouble(start);
                            _end = Double.parseDouble(end);
                            int fps = (int) slider.getValue();

                            SketchletEditor.editorPanel.currentPage.setProperty(SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][0], "" + (Math.min(_start, _end) + Math.abs(_start - _end) * fps / (slider.getMaximum() - slider.getMinimum())));
                            SketchletEditor.editorPanel.repaint();
                        }
                    }
                } catch (Exception e2) {
                }

                bCanUpdate = true;
            }
        });

        TutorialPanel.prepare(slider);

        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.add(endField, BorderLayout.NORTH);
        sliderPanel.add(slider, BorderLayout.CENTER);
        sliderPanel.add(startField, BorderLayout.SOUTH);
        sliderPanel.setBorder(BorderFactory.createTitledBorder(Language.translate("Explore")));

        endField.setEnabled(false);
        slider.setEnabled(false);
        startField.setEnabled(false);
        final JButton btnStart = new JButton(Language.translate("<- Start"));
        btnStart.setEnabled(false);
        btnStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAnimation.getSelectedRow();
                if (row >= 0 && SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][1] != null) {
                    SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][2] = SketchletEditor.editorPanel.currentPage.getProperty(SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][0]);
                    modelAnimation.fireTableRowsUpdated(row, row);
                }
            }
        });
        final JButton btnEnd = new JButton(Language.translate("<- End"));
        btnEnd.setEnabled(false);
        btnEnd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tableAnimation.getSelectedRow();
                if (row >= 0 && SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][1] != null) {
                    SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][3] = SketchletEditor.editorPanel.currentPage.getProperty(SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][0]);
                    modelAnimation.fireTableRowsUpdated(row, row);
                }
            }
        });

        JPanel panel3 = new JPanel(new GridLayout(3, 1));
        panel3.add(btnStart);
        panel3.add(btnEnd);
        panel3.add(btnClear);

        buttonsPanel.add(panel3, BorderLayout.SOUTH);
        buttonsPanel.add(sliderPanel, BorderLayout.CENTER);
        panelButtons.add(buttonsPanel, BorderLayout.CENTER);
        panelSet.add(panelButtons, BorderLayout.EAST);
        tableAnimation.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                int row = tableAnimation.getSelectedRow();
                boolean bEnable = row >= 0 && SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][1] != null;
                btnClear.setEnabled(bEnable);
                btnStart.setEnabled(bEnable);
                btnEnd.setEnabled(bEnable);
                slider.setEnabled(bEnable);
                startField.setEnabled(bEnable);
                endField.setEnabled(bEnable);

                String start = null;
                String end = null;
                String init = "";
                if (bEnable) {
                    start = SketchletEditor.editorPanel.currentPage.getMinValue(SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][0]);
                    end = SketchletEditor.editorPanel.currentPage.getMaxValue(SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][0]);
                    init = SketchletEditor.editorPanel.currentPage.getProperty(SketchletEditor.editorPanel.currentPage.propertiesAnimation[row][0]);

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
                if (bEnable) {
                    startField.setText(start);
                    endField.setText(end);
                }
            }
        });
        return panelSet;
    }
}
