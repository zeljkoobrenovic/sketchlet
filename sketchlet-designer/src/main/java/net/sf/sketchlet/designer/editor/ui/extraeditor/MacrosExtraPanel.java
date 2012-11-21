/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.extraeditor;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.macros.MacroPanel;
import net.sf.sketchlet.model.programming.macros.Macro;
import net.sf.sketchlet.model.programming.macros.Macros;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class MacrosExtraPanel extends JPanel {

    JTabbedPane tabs = new JTabbedPane();
    public static MacrosExtraPanel macrosExtraPanel;
    JButton btnNew = new JButton(Workspace.createImageIcon("resources/add.gif"));
    JButton btnSave = new JButton(Workspace.createImageIcon("resources/save.gif"));
    JButton btnDelete = new JButton(Workspace.createImageIcon("resources/remove.gif"));

    public MacrosExtraPanel() {
        setLayout(new BorderLayout());
        add(tabs, BorderLayout.CENTER);

        tabs.setFont(tabs.getFont().deriveFont(9.0f));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabs);
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOrientation(JToolBar.VERTICAL);
        toolbar.setBorder(BorderFactory.createEmptyBorder());
        toolbar.add(btnNew);
        toolbar.add(btnSave);
        toolbar.add(btnDelete);

        btnNew.setToolTipText(Language.translate("Create a new action list"));
        btnSave.setToolTipText(Language.translate("Save action list"));
        btnDelete.setToolTipText(Language.translate("Delete the selected action list"));

        btnNew.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Macros.globalMacros.addNewMacro();
                SketchletEditor.getInstance().getMacrosTablePanel().model.fireTableDataChanged();
                showMacros(Macros.globalMacros.macros.size() - 1);
            }
        });
        btnSave.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                save();
                SketchletEditor.getInstance().getMacrosTablePanel().model.fireTableDataChanged();
            }
        });
        btnDelete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tabs.getSelectedIndex();
                if (row >= 0) {
                    Macros.globalMacros.macros.remove(row);
                    SketchletEditor.getInstance().getMacrosTablePanel().model.fireTableDataChanged();
                    load();
                }
            }
        });

        macrosExtraPanel = this;
        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = tabs.getSelectedIndex();
                if (SketchletEditor.getInstance() != null && index >= 0) {
//                    SketchletEditor.editorPanel.tabsRight.setSelectedIndex(SketchletEditor.editorPanel.macrosTabIndex);
                    //                  SketchletEditor.editorPanel.macrosTablePanel.table.getSelectionModel().setSelectionInterval(index, index);
                }
            }
        });
        add(toolbar, BorderLayout.WEST);

        load();
        setSize(500, 400);
    }

    public void onHide() {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            MacroPanel p = (MacroPanel) tabs.getComponentAt(i);
            if (p.macroThread != null) {
                p.macroThread.stop();
            }
        }
        Macros.globalMacros.save();
    }

    public void load() {
        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        tabs.removeAll();
        int i = 0;
        for (final Macro m : Macros.globalMacros.macros) {
            MacroPanel p = new MacroPanel(m, true, true, i);
            p.setSaveUndoAction(new Runnable() {

                public void run() {
                    SketchletEditor.getInstance().saveMacroUndo(m);
                }
            });
            tabs.add(m.getName(), p);
            i++;
        }
        SketchletEditor.editorFrame.setCursor(Cursor.getDefaultCursor());
    }

    public void save() {
        Macros.globalMacros.save();
        int i = 0;
        for (Macro m : Macros.globalMacros.macros) {
            tabs.setTitleAt(i++, m.getName());
        }
    }

    public Macro newMacro() {
        Macro m = Macros.globalMacros.addNewMacro();
        tabs.add(m.getName(), new MacroPanel(m, true, true, Macros.globalMacros.macros.size() - 1));
        tabs.setSelectedIndex(tabs.getTabCount() - 1);

        return m;
    }

    public static void showMacros(int index) {
        macrosExtraPanel.load();
        if (index >= 0 && index < macrosExtraPanel.tabs.getTabCount()) {
            macrosExtraPanel.tabs.setSelectedIndex(index);
        }
        SketchletEditor.getInstance().showExtraEditorPanel();
        SketchletEditor.getInstance().getExtraEditorPanel().tabs.setSelectedIndex(1);
    }

    public static void showMacros(String strName, boolean bModal) {
        for (int i = 0; i < Macros.globalMacros.macros.size(); i++) {
            Macro m = Macros.globalMacros.macros.elementAt(i);
            if (m.getName().equalsIgnoreCase(strName)) {
                macrosExtraPanel.tabs.setSelectedIndex(i);
                break;
            }
        }
        SketchletEditor.getInstance().showExtraEditorPanel();
        SketchletEditor.getInstance().getExtraEditorPanel().tabs.setSelectedIndex(1);
    }

    public void deleteMacro(int index) {
        if (index >= 0 && index < Macros.globalMacros.macros.size()) {
            Macros.globalMacros.macros.remove(index);
            load();
        }
    }
}
