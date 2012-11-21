/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.extraeditor;

import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.script.ScriptsTablePanel;
import net.sf.sketchlet.script.ScriptOperations;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ScriptEditorExtraPanel extends JPanel implements ScriptOperations {

    public JTabbedPane tabs = new JTabbedPane();
    Vector<ScriptEditorPanel> editors = new Vector<ScriptEditorPanel>();
    JButton btnNew = new JButton(Workspace.createImageIcon("resources/add.gif"));
    JButton btnDelete = new JButton(Workspace.createImageIcon("resources/remove.gif"));

    public ScriptEditorExtraPanel() {
        ScriptsTablePanel.operations = this;
        setLayout(new BorderLayout());

        tabs.putClientProperty("JComponent.sizeVariant", "small");
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        SwingUtilities.updateComponentTreeUI(tabs);

        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = tabs.getSelectedIndex();
                if (SketchletEditor.getInstance() != null && index >= 0) {
                }
            }
        });
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOrientation(JToolBar.VERTICAL);
        toolbar.setBorder(BorderFactory.createEmptyBorder());
        toolbar.add(btnNew);
        toolbar.add(btnDelete);

        btnNew.setToolTipText("Create a new script");
        btnDelete.setToolTipText("Delete the selected script");

        btnNew.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Workspace.getMainPanel().sketchletPanel.panel2.createNewScript();
            }
        });
        btnDelete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tabs.getSelectedIndex();
                if (row >= 0) {
                    Workspace.getMainPanel().sketchletPanel.panel2.removeScript(row);
                }
            }
        });

        add(tabs);
        add(toolbar, BorderLayout.WEST);

        if (Workspace.getMainPanel().sketchletPanel.panel2 != null) {
            Workspace.getMainPanel().sketchletPanel.panel2.table.revalidate();
        }
        load();
    }

    public void save() {
        for (ScriptEditorPanel editor : editors) {
            if (editor.file.exists()) {
                editor.saveDocument();
            }
        }
    }

    public void load() {
        save();
        tabs.removeAll();
        editors.removeAllElements();

        for (Object strScriptFile : DataServer.scriptFiles) {
            if (strScriptFile != null) {
                ScriptEditorPanel editor = new ScriptEditorPanel();
                editor.openFile(strScriptFile.toString());
                tabs.add(editor.file.getName(), editor);
                editors.add(editor);
            }
        }
    }

    public void openScript(File file) {
        SketchletEditor.getInstance().showExtraEditorPanel(ExtraEditorPanel.indexScript);
        int i = 0;
        for (ScriptEditorPanel editor : this.editors) {
            String strName1 = editor.file.getName();
            String strName2 = file.getName();

            if (strName1.equalsIgnoreCase(strName2)) {
                tabs.setSelectedIndex(i);
                break;
            }
            i++;
        }
    }

    public void reloadAll() {
        this.load();
    }
}
