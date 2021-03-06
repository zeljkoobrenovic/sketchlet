package net.sf.sketchlet.designer.editor.ui.varspace;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.loaders.pluginloader.PluginInstance;
import net.sf.sketchlet.plugin.SketchletPluginGUI;
import net.sf.sketchlet.plugin.SketchletProjectAware;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author zobrenovic
 */
public class VariableSpacesFrame {

    private static JFrame frame;

    public static void hideFrame() {
        if (frame != null) {
            save();
            frame.setVisible(false);
        }
    }

    public static void showFrame() {
        if (frame == null) {
            frame = new JFrame("Variable Spaces");
            frame.setLayout(new BorderLayout());
            frame.setIconImage(Workspace.createImageIcon("resources/tables.gif").getImage());

            JTabbedPane tabs = new JTabbedPane();

            for (int i = 0; i < Workspace.getVariableSourcesNames().size(); i++) {
                String name = Workspace.getVariableSourcesNames().get(i);
                PluginInstance ds = Workspace.getVariableSpaces().get(i);
                Component c = null;
                if (ds.getInstance() instanceof SketchletPluginGUI) {
                    c = ((SketchletPluginGUI) ds.getInstance()).getGUI();
                }
                if (c == null) {
                    c = new JLabel(Language.translate("Plugin does not have a user interface."));
                }
                tabs.addTab(name, c);
            }

            frame.add(tabs, BorderLayout.CENTER);

            JButton btnClose = new JButton("Close", Workspace.createImageIcon("resources/ok.png"));
            btnClose.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent ae) {
                    VariableSpacesFrame.save();
                    frame.setVisible(false);
                }
            });
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panel.add(btnClose);
            frame.add(panel, BorderLayout.SOUTH);

            frame.setSize(500, 550);
            frame.setLocationRelativeTo(SketchletContext.getInstance().getEditorFrame());

            frame.addWindowListener(new WindowAdapter() {

                public void windowClosed(WindowEvent e) {
                    save();
                    frame = null;
                }
            });
        }
        frame.setVisible(true);
    }

    public static void save() {
        for (PluginInstance ds : Workspace.getVariableSpaces()) {
            if (ds.getInstance() instanceof SketchletProjectAware) {
                ((SketchletProjectAware) ds.getInstance()).onSave();
            }
        }
    }
}
