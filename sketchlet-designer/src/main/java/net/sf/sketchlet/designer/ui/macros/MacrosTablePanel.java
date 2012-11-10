/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.macros;

import net.sf.sketchlet.common.dnd.GenericTableTransferHandler;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.programming.macros.Macro;
import net.sf.sketchlet.designer.programming.macros.Macros;
import net.sf.sketchlet.help.HelpUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class MacrosTablePanel extends JPanel {

    public JTable table;
    public AbstractTableModel model = new MyTableModel();
    JButton btnNew = new JButton(Workspace.createImageIcon("resources/add.gif"));
    JButton btnEdit = new JButton(Workspace.createImageIcon("resources/open.gif"));
    JButton btnDelete = new JButton(Workspace.createImageIcon("resources/remove.gif"));
    JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png", ""));

    public MacrosTablePanel() {
        createGUI();
    }

    public void createGUI() {
        setLayout(new BorderLayout());
        JToolBar toolbar = new JToolBar();
        JToolBar toolbarHelp = new JToolBar();
        toolbar.setFloatable(false);
        toolbarHelp.setFloatable(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder());
        toolbarHelp.setBorder(BorderFactory.createEmptyBorder());
        toolbar.add(btnNew);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);

        btnNew.setToolTipText(Language.translate("Create a new action list"));
        btnEdit.setToolTipText(Language.translate("Edit the selected action list"));
        btnDelete.setToolTipText(Language.translate("Delete the selected action list"));

        btnNew.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Macros.globalMacros.addNewMacro();
                model.fireTableDataChanged();
                SketchletEditor.editorPanel.extraEditorPanel.macrosExtraPanel.showMacros(Macros.globalMacros.macros.size() - 1);
            }
        });
        btnEdit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                open();
            }
        });
        btnDelete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    Macros.globalMacros.macros.remove(row);
                    model.fireTableDataChanged();
                    SketchletEditor.editorPanel.extraEditorPanel.macrosExtraPanel.load();
                }
            }
        });
        toolbarHelp.add(help);
        help.setToolTipText("What are Macros?");
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Macros", "macros");
            }
        });

        JPanel panelNorth = new JPanel(new BorderLayout());
        panelNorth.add(toolbar);
        panelNorth.add(toolbarHelp, BorderLayout.EAST);
        add(panelNorth, BorderLayout.NORTH);

        table = new JTable(model);
        table.setTableHeader(null);
        table.setTableHeader(null);
        table.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(table);
        table.setDragEnabled(true);
        table.setTransferHandler(new GenericTableTransferHandler("@macro ", 0));

        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }

                enableControls();
            }
        });

        table.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        SketchletEditor.editorPanel.extraEditorPanel.macrosExtraPanel.showMacros(Macros.globalMacros.macros.elementAt(row).name, false);
                    }
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(table);
        add(tableScroll, BorderLayout.CENTER);

        enableControls();

        TutorialPanel.prepare(table);
    }

    public void open() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            SketchletEditor.editorPanel.extraEditorPanel.macrosExtraPanel.showMacros(Macros.globalMacros.macros.elementAt(row).name, false);
        }
    }

    public void enableControls() {
        int row = table.getSelectedRow();
        boolean bEnable = row >= 0;

        btnEdit.setEnabled(bEnable);
        btnDelete.setEnabled(bEnable);
    }

    class MyTableModel extends AbstractTableModel {

        private String[] columnNames = {
                "Action List"
        };

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return Macros.globalMacros.macros.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            Macro macro = Macros.globalMacros.macros.elementAt(row);
            return macro.name;
        }
    }
}
