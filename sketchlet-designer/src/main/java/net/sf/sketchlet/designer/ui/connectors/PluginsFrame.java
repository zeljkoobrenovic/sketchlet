/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.connectors;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.ui.variables.VariablesPanel;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.plugin.SketchletPluginGUI;
import net.sf.sketchlet.pluginloader.GenericPluginFactory;
import net.sf.sketchlet.pluginloader.PluginInstance;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class PluginsFrame extends JFrame {

    JTabbedPane tabs = new JTabbedPane();

    public PluginsFrame() {
        super(Language.translate("Plugins"));
        frame = this;
        this.setIconImage(Workspace.createImageIcon("resources/plugin.png", "").getImage());

        tabs.setTabPlacement(JTabbedPane.LEFT);

        for (PluginInstance nc : GenericPluginFactory.getGenericPlugins()) {
            Component c = null;
            if (nc.getInstance() instanceof SketchletPluginGUI) {
                c = ((SketchletPluginGUI) nc.getInstance()).getGUI();
            }
            if (c == null) {
                c = new JLabel(Language.translate("Plugin does not have a user interface."));
            }
            if (c != null) {
                tabs.add(c, nc.getName());
                ImageIcon icon = null;
                if (nc.getInstance() instanceof SketchletPluginGUI) {
                    icon = ((SketchletPluginGUI) nc.getInstance()).getIcon();
                }
                if (icon == null) {
                    icon = Workspace.createImageIcon("resources/plugin.png");
                }
                tabs.setIconAt(tabs.getTabCount() - 1, icon);
            }
        }

        final JButton saveButton = new JButton(Language.translate("Save"), Workspace.createImageIcon("resources/ok.png", ""));
        saveButton.setToolTipText(Language.translate("Save configuration, init  variables, and links to loaded scripts"));
        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                save();
                setVisible(false);
            }
        });
        final JButton alwaysOnTopBtn = new JButton(Workspace.createImageIcon("resources/pin_up.png", ""));
        alwaysOnTopBtn.setActionCommand("pin");
        alwaysOnTopBtn.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (alwaysOnTopBtn.getActionCommand().equals("pin")) {
                    alwaysOnTopBtn.setActionCommand("unpin");
                    alwaysOnTopBtn.setIcon(Workspace.createImageIcon("resources/pin_down.png", ""));
                    setAlwaysOnTop(true);
                } else {
                    alwaysOnTopBtn.setActionCommand("pin");
                    alwaysOnTopBtn.setIcon(Workspace.createImageIcon("resources/pin_up.png", ""));
                    setAlwaysOnTop(false);
                }
            }
        });
        JToolBar toolbarPin = new JToolBar();
        toolbarPin.setFloatable(false);
        toolbarPin.add(alwaysOnTopBtn);
        final JButton help = new JButton("", Workspace.createImageIcon("resources/help-browser.png", ""));
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("URL Connector", "connector");
            }
        });
        help.setToolTipText(Language.translate("What is the URL Connector?"));
        toolbarPin.add(help);
        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!tabs.isShowing()) {
                    return;
                }

                help.setToolTipText(Language.translate("What are connector?"));
            }
        });

        JPanel panelBottom = new JPanel(new BorderLayout());

        JPanel panelSave = new JPanel(new FlowLayout(FlowLayout.LEFT));
        /*panelSave.add(new JPanel() {
        public Dimension getPreferredSize() {
        return new Dimension( 120, 20 );
        }
        });*/
        panelSave.add(saveButton);
        add(tabs);

        panelBottom.add(panelSave, BorderLayout.WEST);
        panelBottom.add(toolbarPin, BorderLayout.EAST);

        add(panelBottom, BorderLayout.SOUTH);
        if (tabs.getTabCount() == 0) {
            tabs.addTab("", new JLabel("    Generic plugins are not installed.    "));
        }
    }

    public void save() {
        GenericPluginFactory.save();
    }

    public static PluginsFrame frame;

    public static void showFrame() {
        try {
            Workspace.pluginsReady.await();
            if (frame == null) {
                frame = new PluginsFrame();
                frame.setSize(855, 500);
                if (VariablesPanel.referenceFrame != null) {
                    frame.setLocationRelativeTo(VariablesPanel.referenceFrame);
                }
            }
            frame.setVisible(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // frame.toFront();
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
}
