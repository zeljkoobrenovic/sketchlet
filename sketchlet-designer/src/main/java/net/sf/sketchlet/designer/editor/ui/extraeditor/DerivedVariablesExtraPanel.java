/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.extraeditor;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.UIUtils;
import net.sf.sketchlet.loaders.pluginloader.GenericPluginFactory;
import net.sf.sketchlet.loaders.pluginloader.PluginInstance;
import net.sf.sketchlet.plugin.SketchletPluginGUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class DerivedVariablesExtraPanel extends JPanel {

    public JTabbedPane tabs = new JTabbedPane();
    JComboBox varCombo = new JComboBox();

    public DerivedVariablesExtraPanel() {
        setLayout(new BorderLayout());
        UIUtils.populateVariablesCombo(varCombo, false);

        tabs.setTabPlacement(JTabbedPane.LEFT);

        try {
            Workspace.getDerivedVariablesReadyCountDownLatch().await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for (PluginInstance dv : GenericPluginFactory.getDerivedVariablesPlugins()) {
            Component c = null;
            ImageIcon icon = null;
            if (dv.getInstance() instanceof SketchletPluginGUI) {
                c = ((SketchletPluginGUI) dv.getInstance()).getGUI();
                icon = ((SketchletPluginGUI) dv.getInstance()).getIcon();
            }
            if (c == null) {
                c = new JLabel(Language.translate("Plugin does not have a user interface."));
            }
            if (icon == null) {
                icon = Workspace.createImageIcon("resources/plugin.png");
            }
            tabs.add(c, dv.getName());
            tabs.setIconAt(tabs.getTabCount() - 1, icon);
        }

        if (tabs.getTabCount() > 0) {
            tabs.addChangeListener(new ChangeListener() {

                public void stateChanged(ChangeEvent e) {
                    if (!tabs.isShowing()) {
                        return;
                    }
                    SketchletEditor.getInstance().getExtraEditorPanel().openRelevantHelpPage(false);
                }
            });
        } else {
            tabs.addTab("", new JLabel("    Plugins for derived variables are not installed.    "));
        }

        add(tabs);
    }

    public static void save() {
        GenericPluginFactory.save();
    }

    static DerivedVariablesExtraPanel frame;

    public static void showFrame() {
        if (frame == null) {
            frame = new DerivedVariablesExtraPanel();
            frame.setSize(650, 300);
            if (Workspace.getReferenceFrame() != null) {
            }
        }
        UIUtils.populateVariablesCombo(frame.varCombo, false);
        frame.setVisible(true);
    }

    public static void showFrame(int tab) {
        showFrame();
        frame.tabs.setSelectedIndex(tab);
    }

    public static void hideFrame() {
        if (frame != null) {
            frame.setVisible(false);
            frame = null;
        }
    }

    static JComboBox lastComboBoxParam1 = null;
}
