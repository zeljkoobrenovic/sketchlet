/*
 * VariablesPanel.java
 *
 * Created on 22 February 2006, 13:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.designer.editor.ui.variables;

import net.sf.sketchlet.communicator.ConfigurationData;
import net.sf.sketchlet.communicator.Global;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.tcp.TCPServer;
import net.sf.sketchlet.communicator.server.udp.UDPServer;
import net.sf.sketchlet.designer.ApplicationLifecycleCentre;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.ui.script.ScriptsTablePanel;
import net.sf.sketchlet.loaders.pluginloader.ScriptPluginFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Hashtable;

/**
 * @author Omnibook
 */
public class VariablesPanel extends JPanel {

    public JTabbedPane tabbedPane;
    public VariablesTablePanel globalVariablesPanel;
    public ScriptsTablePanel panel2;
    public JButton pauseButton;
    public JMenuItem pauseMenuItem;
    public VariablesPanel parent = this;
    public static JFrame frame;
    public static JFrame referenceFrame;
    public DataServer dataServer;
    // public DataServer dataServerDerived;
    public Hashtable menuItems;
    public Hashtable toolbarButtons;
    public static VariablesPanel mainFrame;

    public VariablesPanel(String configURL, JToolBar variablesToolbar, JToolBar variablesToolbarDown, JToolBar scriptsToolbar, JToolBar scriptsConsoleToolbar, Hashtable menuItems, Hashtable toolbarButtons) {
        super(new BorderLayout());
        mainFrame = this;
        this.menuItems = menuItems;
        this.toolbarButtons = toolbarButtons;

        if (configURL != null) {
            initMainFrame(configURL);
        }

        tabbedPane = new JTabbedPane();

        globalVariablesPanel = new VariablesTablePanel(variablesToolbar, variablesToolbarDown, this);
        tabbedPane.addTab("Blackboard", globalVariablesPanel);

        if (ScriptPluginFactory.getScriptTitles().length > 0) {
            panel2 = new ScriptsTablePanel(scriptsToolbar, scriptsConsoleToolbar, this);
            tabbedPane.addTab("Scripts", panel2);
        }

        //Add the tabbed pane to this panel.
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        final JButton saveButton = new JButton(createImageIcon(this, "resources/save.gif", ""));
        saveButton.setToolTipText("Save configuration, init  variables, and links to loaded scripts");
        final JButton saveAsButton = new JButton(createImageIcon(this, "resources/saveas.gif", ""));
        pauseButton = new JButton(createImageIcon(this, "resources/stop.gif", ""));
        pauseButton.setToolTipText("Ignores updates of variables send by modules");

        JMenuBar menuBar = new JMenuBar();
        // File Menu, F - Mnemonic
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        // File->New, N - Mnemonic
        JMenuItem saveMenuItem = new JMenuItem("Save", KeyEvent.VK_S);
        saveMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ConfigurationData.saveConfiguration();
            }
        });
        JMenuItem saveAsMenuItem = new JMenuItem("Save As...", KeyEvent.VK_A);
        saveAsMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                saveAs();
            }
        });
        JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });

        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(new JSeparator());
        fileMenu.add(exitMenuItem);

        // Communicator Menu, C - Mnemonic
        JMenu communicatorMenu = new JMenu("Communicator");
        fileMenu.setMnemonic(KeyEvent.VK_C);
        menuBar.add(communicatorMenu);

        // File->New, N - Mnemonic
        pauseMenuItem = new JMenuItem("Pause", KeyEvent.VK_S);
        pauseMenuItem.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                pause();
            }
        });
        pauseMenuItem.setToolTipText("Pause processing and ignores updates of variables send by modules");
        communicatorMenu.add(pauseMenuItem);

        if (frame != null && menuBar != null) {
            frame.setJMenuBar(menuBar);
        } else {
            // this.tabbedPane.setTabPlacement( JTabbedPane.BOTTOM );
        }

        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ConfigurationData.saveConfiguration();
            }
        });

        saveAsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                saveAs();
            }
        });

        pauseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                pause();
            }
        });


        //ImageIcon icon = createImageIcon(this, "resources/communicator.jpg", "");
        //JLabel label1 = new JLabel(icon);

        JToolBar toolBar = new JToolBar("Communicator");
        toolBar.add(saveButton);
        toolBar.add(saveAsButton);
        toolBar.add(new JLabel("    "));
        toolBar.add(pauseButton);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void enableMenuAndToolbar(String action[], boolean bEnable) {
        for (int i = 0; menuItems != null && i < action.length; i++) {
            Component c = (Component) menuItems.get(action[i]);

            if (c != null) {
                c.setEnabled(bEnable);
            }
        }
        for (int i = 0; toolbarButtons != null && i < action.length; i++) {
            Component c = (Component) toolbarButtons.get(action[i]);

            if (c != null) {
                c.setEnabled(bEnable);
            }
        }
    }

    public void restart(String configURL) {
        initMainFrame(configURL);

        ApplicationLifecycleCentre.afterProjectOpening();
    }

    public void initMainFrame(String configURL) {
        boolean paused = DataServer.isPaused();
        DataServer.setPaused(true);

        DataServer.setInstance(null);

        ConfigurationData.loadFromURL(configURL);

        dataServer = new DataServer();
        DataServer.setInstance(dataServer);

        if (Global.getServerUDP() == null) {
            Global.setServerUDP(new UDPServer(ConfigurationData.getUdpPort()));
        }
        if (Global.getServerTCP() == null) {
            Global.setServerTCP(new TCPServer(ConfigurationData.getTcpPort()));
        }


        if (this.globalVariablesPanel != null) {
            this.globalVariablesPanel.register();
        }

        DataServer.createScripts();
        //DataServer.initScripts();

        globalVariablesPanel.variablesTableModel.fireTableDataChanged();
        if (panel2 != null && panel2.scriptsTableModel != null) {
            panel2.scriptsTableModel.fireTableDataChanged();
            panel2.enableControls();
        }

        DataServer.setPaused(paused);
    }

    public void saveAs() {
        final JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("."));
        int returnVal = fc.showSaveDialog(parent);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                ConfigurationData.configURL = fc.getSelectedFile().toURL().toString();
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
            ConfigurationData.saveConfiguration();
        }
    }

    public void pause() {
        if (!DataServer.isPaused()) {
            DataServer.setPaused(true);
            // pauseButton.setText( "Start" );
            pauseButton.setIcon(createImageIcon(this, "resources/start.gif", ""));
            pauseButton.setToolTipText("Continues to receive and process updates of variables send by modules");

            pauseMenuItem.setText("Start");
            pauseMenuItem.setToolTipText("Continues to receive and process updates of variables send by modules");

            DataServer.unprotectAllVariables();
        } else {
            DataServer.setPaused(false);
            // pauseButton.setText( "Pause" );
            pauseButton.setIcon(createImageIcon(this, "resources/stop.gif", ""));
            pauseButton.setToolTipText("Pause processing and ignores updates of variables send by modules");

            pauseMenuItem.setText("Pause");
            pauseMenuItem.setToolTipText("Pause processing and ignores updates of variables send by modules");
        }
    }

    public static ImageIcon createImageIcon(Object object, String path, String description) {
        java.net.URL imgURL = Workspace.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
}
