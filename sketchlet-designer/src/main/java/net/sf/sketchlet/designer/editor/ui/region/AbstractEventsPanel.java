package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.UIUtils;
import net.sf.sketchlet.designer.editor.ui.macros.MacroPanel;
import net.sf.sketchlet.model.EventMacro;
import net.sf.sketchlet.model.EventMacroFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 11-10-12
 * Time: 9:13
 * To change this template use File | Settings | File Templates.
 */
public class AbstractEventsPanel<T extends EventMacro> extends JPanel {

    private JTable table;
    private JScrollPane scrollEventsList;
    private AbstractTableModel model;
    private JButton addBtn = new JButton(Workspace.createImageIcon("resources/add.gif"));
    private JButton deleteBtn = new JButton(Workspace.createImageIcon("resources/remove.gif"));
    private int selectedRow = -1;
    private MacroPanel macroPanel = null;
    private JLabel noMacrosLabel;
    private EventMacroFactory<T> eventMacroFactory;

    public AbstractEventsPanel() {
    }

    public AbstractEventsPanel(EventMacroFactory<T> eventMacroFactory) {
        this.eventMacroFactory = eventMacroFactory;
        noMacrosLabel = new JLabel("  No " + eventMacroFactory.getEventTypeName(true) + ". Click on the add button to add a new " + eventMacroFactory.getEventTypeName(false) + ".");
        init();
    }

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

    protected void init() {
        setLayout(new BorderLayout());
        addBtn.setToolTipText(Language.translate("Add a new " + eventMacroFactory.getEventTypeName(false)));
        deleteBtn.setToolTipText(Language.translate("Delete the " + eventMacroFactory.getEventTypeName(false)));
        model = getModel();
        table = new JTable(model);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.setFillsViewportHeight(true);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                selectedRow = table.getSelectedRow();
                enableControls();
                refreshMacroPanel();
            }
        });
        scrollEventsList = new JScrollPane(table);
        JPanel panel2 = new JPanel();
        panel2.setLayout(new BorderLayout());
        panel2.add(scrollEventsList, BorderLayout.CENTER);
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        this.putClientProperty("JComponent.sizeVariant", "small");
        table.getTableHeader().putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(table.getTableHeader());
        addBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                T item = eventMacroFactory.getNewEventMacroInstance();
                if (item != null) {
                    eventMacroFactory.getEventMacroList().add(item);
                    model.fireTableDataChanged();
                    selectedRow = eventMacroFactory.getEventMacroList().size() - 1;
                    table.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
                }
            }
        });
        deleteBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int selectedOption = JOptionPane.showConfirmDialog(SketchletEditor.editorFrame,
                        Language.translate("Are you sure you want to delete the selected " + eventMacroFactory.getEventTypeName(false) + "?"),
                        Language.translate("Delete the " + eventMacroFactory.getEventTypeName(false)),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (selectedOption == JOptionPane.YES_OPTION) {
                    UIUtils.deleteTableRows(table, model, eventMacroFactory.getEventMacroList());
                }
            }
        });
        toolbar.setOrientation(JToolBar.VERTICAL);
        toolbar.setSize(28, 150);
        toolbar.add(addBtn);
        toolbar.add(deleteBtn);
        panel2.add(toolbar, BorderLayout.SOUTH);

        JPanel eventList = new JPanel(new BorderLayout());

        eventList.setSize(200, 300);
        eventList.setPreferredSize(new Dimension(200, 300));

        eventList.add(toolbar, BorderLayout.WEST);
        eventList.add(scrollEventsList, BorderLayout.CENTER);
        splitPane.add(eventList);

        add(splitPane);

        enableControls();
        if (eventMacroFactory.getEventMacroList().size() > 0) {
            table.getSelectionModel().setSelectionInterval(0, 0);
        } else {
            refreshMacroPanel();
        }
        splitPane.setDividerLocation(250);
    }

    public void enableControls() {
        deleteBtn.setEnabled(selectedRow >= 0 && selectedRow < eventMacroFactory.getEventMacroList().size());
    }

    private AbstractTableModel getModel() {
        return new AbstractTableModel() {

            public String getColumnName(int col) {
                return eventMacroFactory.getEventTypeName(false);
            }

            public int getRowCount() {
                return eventMacroFactory.getEventMacroList().size();
            }

            public int getColumnCount() {
                return 1;
            }

            public Object getValueAt(int row, int col) {
                if (row >= 0 && row < eventMacroFactory.getEventMacroList().size()) {
                    T eventMacro = eventMacroFactory.getEventMacroList().get(row);
                    return eventMacroFactory.getEventDescription(eventMacro);
                }
                return "";
            }

            public boolean isCellEditable(int row, int col) {
                return false;
            }

            public void setValueAt(Object value, int row, int col) {
            }

            public Class getColumnClass(int c) {
                return String.class;
            }
        };
    }

    public void refresh() {
        model.fireTableDataChanged();
        refreshMacroPanel();
        boolean enable = eventMacroFactory.getEventMacroList().size() > 0;
        if (!enable) {
            noMacrosLabel.setText("  No " + eventMacroFactory.getEventTypeName(true) + ". Click on the add button to add a new " + eventMacroFactory.getEventTypeName(false) + ".");
        }
        revalidate();
    }

    public void refreshMacroPanel() {
        int dividerLocation = splitPane.getDividerLocation();

        try {
            if (macroPanel != null) {
                macroPanel.save();
                splitPane.remove(macroPanel);
                macroPanel = null;
            }
            splitPane.remove(noMacrosLabel);
            selectedRow = table.getSelectedRow();
            if (eventMacroFactory.getEventMacroList().size() == 0) {
                splitPane.setRightComponent(noMacrosLabel);
                revalidate();
                repaint();
            } else {
                if (selectedRow == -1) {
                    table.getSelectionModel().setSelectionInterval(0, 0);
                    selectedRow = 0;
                }
                EventMacro eventMacro = eventMacroFactory.getEventMacroList().get(selectedRow);
                macroPanel = new MacroPanel(eventMacro.getMacro(), false, true);
                splitPane.setRightComponent(macroPanel);
                revalidate();
                repaint();
            }
        } finally {
            splitPane.setDividerLocation(dividerLocation);
        }
    }

    public EventMacroFactory<T> getEventMacroFactory() {
        return eventMacroFactory;
    }

    public void setEventMacroFactory(EventMacroFactory<T> eventMacroFactory) {
        this.eventMacroFactory = eventMacroFactory;
        noMacrosLabel = new JLabel("  No " + eventMacroFactory.getEventTypeName(true) + ". Click on the add button to add a new " + eventMacroFactory.getEventTypeName(false) + ".");
        init();
    }
}
