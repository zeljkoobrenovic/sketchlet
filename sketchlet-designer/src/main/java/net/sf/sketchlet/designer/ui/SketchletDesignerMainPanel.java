/*
 * SketchletDesignerMainPanel.java
 *
 * Created on November 11, 2006, 4:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui;

import net.sf.sketchlet.common.QuotedStringTokenizer;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.system.PlatformManager;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.SketchletGraphicsContext;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.RecentFilesManager;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.context.SketchletContextImpl;
import net.sf.sketchlet.designer.data.Pages;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorFrame;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.install.Installer;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.programming.macros.Macros;
import net.sf.sketchlet.designer.programming.screenscripts.ScreenScripts;
import net.sf.sketchlet.designer.programming.timers.Timers;
import net.sf.sketchlet.designer.tools.imagecache.ImageCache;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import net.sf.sketchlet.designer.tools.reporting.ReportFrame;
import net.sf.sketchlet.designer.tools.vfs.RemoteLocationFrame;
import net.sf.sketchlet.designer.tools.vfs.RestoreRemoteLocationFrame;
import net.sf.sketchlet.designer.tools.zip.UnZip;
import net.sf.sketchlet.designer.tools.zip.ZipVersion;
import net.sf.sketchlet.designer.ui.codegenerator.CodeGeneratorDialog;
import net.sf.sketchlet.designer.ui.connectors.PluginsFrame;
import net.sf.sketchlet.designer.ui.desktop.DesktopPanel;
import net.sf.sketchlet.designer.ui.desktop.Notepad;
import net.sf.sketchlet.designer.ui.desktop.ProcessConsolePanel;
import net.sf.sketchlet.designer.ui.desktop.ProjectDialog;
import net.sf.sketchlet.designer.ui.desktop.ProjectSelectorPanel;
import net.sf.sketchlet.designer.ui.desktop.SelectionTree;
import net.sf.sketchlet.designer.ui.desktop.SystemVariablesDialog;
import net.sf.sketchlet.designer.ui.extraeditor.ExtraEditorPanel;
import net.sf.sketchlet.designer.ui.macros.MacrosFrame;
import net.sf.sketchlet.designer.ui.page.SketchStatePanel;
import net.sf.sketchlet.designer.ui.playback.displays.InteractionSpaceFrame;
import net.sf.sketchlet.designer.ui.playback.displays.PropertiesFrame;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.designer.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.ui.timers.curve.CurvesFrame;
import net.sf.sketchlet.designer.ui.variables.VariablesTableModel;
import net.sf.sketchlet.designer.ui.variables.recorder.VariablesRecorder;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.pluginloader.PluginData;
import net.sf.sketchlet.pluginloader.PluginLoader;
import net.sf.sketchlet.script.ScriptConsole;
import net.sf.sketchlet.util.XMLHelper;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * @author cuypers
 */
public class SketchletDesignerMainPanel extends JPanel implements ActionListener {
    private static final Logger log = Logger.getLogger(SketchletDesignerMainPanel.class);
    JFrame consoleFrame;
    public JComboBox comboProfiles;
    public Workspace processRunner;
    public JTable tableModules;
    JTable tableFiles;
    // JTable tableSketches;
    public ProcessTableModel tableModelModules;
    FileTableModel tableModelFiles;
    // public SketchesTableModel tableModelSketches;
    public String projectTitle = "";
    public JTextField projectFolderField;
    public JButton consoleButton;
    JButton editButton;
    public JMenu recentProjectsMenu;
    public JMenu systemSettingsMenu;
    public JMenu projectSettingsMenu;
    SketchletDesignerMainPanel thisPanel = this;
    private Hashtable commands;
    private Hashtable menuItems;
    private Hashtable toolbarItems;
    public JMenuBar menubar;
    private JToolBar toolbar;
    public JPanel panelFiles = new JPanel();
    public JPanel panelProcesses = new JPanel();
    String[] columnNames = {Language.translate("Service"), Language.translate("Status")};
    String[] columnNamesFiles = {Language.translate("File"), Language.translate("Size"), Language.translate("Date")};
    String[] columnNamesSketches = {Language.translate("Page Name"), Language.translate("Image")};
    private static ResourceBundle resources;
    private final static String EXIT_AFTER_PAINT = new String("-exit");
    private static boolean exitAfterFirstPaint;
    public JTabbedPane tabs;
    final static JFileChooser fc = new JFileChooser();
    public static JPanel desktopPanelFrame = new JPanel(new BorderLayout());
    public static JPanel desktopPanelAutoFrame = new JPanel(new BorderLayout());
    public static JScrollPane desktopPanelScrollPane = new JScrollPane(desktopPanelFrame);
    public static JScrollPane desktopPanelAutoScrollPane = new JScrollPane(desktopPanelAutoFrame);
    public static DesktopPanel desktopPanel = new DesktopPanel(DesktopPanel.MANUAL, SketchletDesignerMainPanel.desktopPanelScrollPane);
    public static DesktopPanel desktopPanelAuto = new DesktopPanel(DesktopPanel.AUTO, SketchletDesignerMainPanel.desktopPanelAutoScrollPane);
    public static ProjectSelectorPanel projectSelectorPanel;
    /**
     * Suffix applied to the key used in resource file lookups for an
     * currentSketch.images.
     */
    public static final String imageSuffix = "Image";
    /**
     * Suffix applied to the key used in resource file lookups for a label.
     */
    public static final String labelSuffix = "Label";
    /**
     * Suffix applied to the key used in resource file lookups for an action.
     */
    public static final String actionSuffix = "Action";
    /**
     * Suffix applied to the key used in resource file lookups for tooltip text.
     */
    public static final String tipSuffix = "Tooltip";
    public static final String textSuffix = "Text";
    public static final String openAction = "open";
    public static final String newAction = "new";
    public static final String saveAction = "save";
    public static final String exitAction = "exit";
    public net.sf.sketchlet.designer.ui.variables.VariablesPanel sketchletPanel;
    static Vector<String> advancedItems1 = new Vector<String>();
    static Vector<String> advancedItems2 = new Vector<String>();
    static Vector<String> advancedItems3 = new Vector<String>();

    static {
        try {
            resources = ResourceBundle.getBundle("net.sf.sketchlet.designer.resources.MainPanel", Locale.getDefault());
            java.net.URL fileURL1 = Workspace.class.getResource("resources/advanced_1.txt");
            java.net.URL fileURL2 = Workspace.class.getResource("resources/advanced_2.txt");
            java.net.URL fileURL3 = Workspace.class.getResource("resources/advanced_3.txt");


            BufferedReader in = new BufferedReader(new InputStreamReader(fileURL1.openStream()));
            String line;
            while ((line = in.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line);

                while (tokenizer.hasMoreTokens()) {
                    advancedItems1.add(tokenizer.nextToken());
                }
            }

            in = new BufferedReader(new InputStreamReader(fileURL2.openStream()));
            while ((line = in.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line);

                while (tokenizer.hasMoreTokens()) {
                    advancedItems2.add(tokenizer.nextToken());
                }
            }

            in = new BufferedReader(new InputStreamReader(fileURL3.openStream()));
            while ((line = in.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line);

                while (tokenizer.hasMoreTokens()) {
                    advancedItems3.add(tokenizer.nextToken());
                }
            }

        } catch (MissingResourceException mre) {
            System.err.println("resources/MainPanel.parameters not found");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String strHelpHTML = "";
    public JToolBar mainFrameToolbar;

    public SketchletDesignerMainPanel(JFrame console, Workspace runner) {
        super(new GridLayout(2, 0));

        strHelpHTML = "<html><body>\r\n";
        this.setLayout(new BorderLayout());

        commands = new Hashtable();
        menuItems = new Hashtable();
        toolbarItems = new Hashtable();

        Workspace.mainFrame.setJMenuBar(this.createMenubar("menubarMain"));
        strHelpHTML += "</body></html>\r\n";
        // FileUtils.saveFileText("C:\\temp\\help.html", strHelpHTML);
        strHelpHTML = "";

        this.consoleFrame = console;
        this.processRunner = runner;

        Object[][] data = new String[this.processRunner.processHandlers.size()][3];

        Iterator iterator = this.processRunner.processHandlers.iterator();

        int i = 0;
        while (iterator.hasNext()) {
            ProcessConsolePanel processPanel = (ProcessConsolePanel) iterator.next();
            data[i][0] = processPanel.title;
            data[i][1] = processPanel.status;
            data[i][2] = processPanel.description;
            i++;
        }

        this.tableModelModules = new ProcessTableModel();
        this.tableModelFiles = new FileTableModel();
        // this.tableModelSketches = new SketchesTableModel();

        tableModules = new JTable(this.tableModelModules);
        tableModules.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        // tableModules.setRowHeight(30);
        tableModules.getSelectionModel().addListSelectionListener(new RowListener());
        tableModules.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tableModules.getSelectedRow();

                    if (row >= 0) {
                        processRunner.tabbedPane.setSelectedIndex(row);
                    }

                    showConsoleIfNotVisible();
                }
            }
        });
        // tableModules.setPreferredScrollableViewportSize(new Dimension(500, 200));

        tableFiles = new JTable(this.tableModelFiles);
        tableFiles.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        tableFiles.getSelectionModel().addListSelectionListener(new RowListenerFiles());
        /*
         * tableSketches = new JTable(this.tableModelSketches);
         * tableSketches.putClientProperty("terminateEditOnFocusLost",
         * Boolean.TRUE);
         *
         * tableSketches.getColumnModel().getColumn(0).setPreferredWidth(200);
         * tableSketches.getColumnModel().getColumn(1).setPreferredWidth(300);
         * tableSketches.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         *
         * tableSketches.setRowHeight(100);
         * tableSketches.getColumnModel().getColumn(1).setCellRenderer(new
         * SketchCellRenderer(true));
         * tableSketches.getSelectionModel().addListSelectionListener(new
         * SketchRowListener());
         */
        // tableFiles.setPreferredScrollableViewportSize(new Dimension(500, 200));

        //Create the scroll pane and add the tableModules to it.
        JScrollPane scrollPane = new JScrollPane(tableModules);
        tableModules.setTableHeader(null);
        tableModules.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tableModules);
        JScrollPane scrollPaneFiles = new JScrollPane(tableFiles);
        // JScrollPane scrollPaneSketches = new JScrollPane(tableSketches);
        TutorialPanel.prepare(tableModules);

        JPanel processesPanel = new JPanel();
        processesPanel.setLayout(new BorderLayout());

        //Add the scroll pane to this panel.
        tabs = new JTabbedPane();
        tabs.setTabPlacement(JTabbedPane.RIGHT);
        tabs.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabs);

        panelProcesses.setLayout(new BorderLayout());

        JToolBar commandsPanel = this.createToolbar("toolbarModules");
        commandsPanel.setFloatable(false);

        // commandsPanel.setLayout( new FlowLayout() );
        consoleButton = new JButton(Workspace.createImageIcon("resources/details.gif", null));
        editButton = new JButton(Language.translate("Edit Configuration..."));
        JButton hideButton = new JButton("Hide");

        panelProcesses.add(scrollPane, BorderLayout.CENTER);
        final JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png", ""));
        help.setToolTipText(Language.translate("What are I/O Services?"));
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Services", "services");
            }
        });

        JToolBar toolbarHelp = new JToolBar();
        toolbarHelp.setFloatable(false);
        toolbarHelp.add(help);
        JPanel panelNorth = new JPanel(new BorderLayout());
        panelNorth.add(commandsPanel);
        panelNorth.add(toolbarHelp, BorderLayout.EAST);
        commandsPanel.setBorder(BorderFactory.createEmptyBorder());
        toolbarHelp.setBorder(BorderFactory.createEmptyBorder());

        add(panelNorth, BorderLayout.NORTH);

        panelProcesses.add(panelNorth, BorderLayout.NORTH);

        // panelProcesses.setPreferredSize( new Dimension( 500, 200 ) );
        mainFrameToolbar = new JToolBar();
        createToolbar(mainFrameToolbar, "toolbarVariables");
        TutorialPanel.prepare(mainFrameToolbar);
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
        flowLayout.setHgap(0);
        flowLayout.setVgap(0);
        mainFrameToolbar.setLayout(flowLayout);
        JToolBar mainFrameToolbarDown = new JToolBar();
        mainFrameToolbar.setFloatable(false);
        mainFrameToolbarDown.setFloatable(false);

        final JButton helpMainWindow = new JButton("", Workspace.createImageIcon("resources/help-browser.png"));
        helpMainWindow.setToolTipText(Language.translate("What is this window?"));
        helpMainWindow.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Sketchlet", "main_window");
            }
        });
        /*
         * final JButton alwaysOnTopBtn = new
         * JButton(Workspace.createImageIcon("resources/pin_up.png"));
         * alwaysOnTopBtn.setActionCommand("pin");
         * alwaysOnTopBtn.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent ae) { if
         * (alwaysOnTopBtn.getActionCommand().equals("pin")) {
         * alwaysOnTopBtn.setActionCommand("unpin");
         * alwaysOnTopBtn.setIcon(Workspace.createImageIcon("resources/pin_down.png"));
         * Workspace.mainFrame.setAlwaysOnTop(true); } else {
         * alwaysOnTopBtn.setActionCommand("pin");
         * alwaysOnTopBtn.setIcon(Workspace.createImageIcon("resources/pin_up.png"));
         * Workspace.mainFrame.setAlwaysOnTop(false); } } });
         */
        FlowLayout flowLayout3 = new FlowLayout(FlowLayout.LEFT);
        flowLayout3.setHgap(0);
        flowLayout3.setVgap(0);
        mainFrameToolbar.setLayout(flowLayout3);
        FlowLayout flowLayout2 = new FlowLayout(FlowLayout.LEFT);
        flowLayout2.setHgap(0);
        flowLayout2.setVgap(0);
        mainFrameToolbarDown.setLayout(flowLayout);

        final JTextField filterField = new JTextField(8);

        filterField.setPreferredSize(new Dimension(35, 22));

        filterField.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                actionPerformed(e);
            }

            public void removeUpdate(DocumentEvent e) {
                actionPerformed(e);
            }

            public void changedUpdate(DocumentEvent e) {
                // we won't ever getVariableValue this with PlainDocument
            }

            public void actionPerformed(DocumentEvent e) {
                VariablesTableModel.strFilter = filterField.getText();
                if (VariablesTableModel.model != null) {
                    VariablesTableModel.model.fireTableDataChanged();
                }
            }
        });

        mainFrameToolbarDown.add(new JLabel(Workspace.createImageIcon("resources/edit-find.png")));
        mainFrameToolbarDown.add(new JLabel(" "));
        mainFrameToolbarDown.add(filterField);
        // mainFrameToolbarDown.add(new JLabel(" sort by "));
        final JButton helpVariables = new JButton("", Workspace.createImageIcon("resources/help-browser.png"));
        helpVariables.setToolTipText(Language.translate("What are variables?"));
        helpVariables.setBorder(BorderFactory.createEmptyBorder());
        helpVariables.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Variables", "variables");
            }
        });
        final JComboBox comboSort = new JComboBox();
        comboSort.addItem("creation");
        comboSort.addItem("group");
        comboSort.addItem("name");

        comboSort.setPreferredSize(new Dimension(65, 22));

        comboSort.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String strSort = (String) comboSort.getSelectedItem();
                VariablesTableModel.strSort = strSort;
                if (VariablesTableModel.model != null) {
                    VariablesTableModel.model.fireTableDataChanged();
                }

            }
        });

        // mainFrameToolbarDown.add(comboSort);
        // mainFrameToolbarDown.add(new JLabel("  "));

        sketchletPanel = new net.sf.sketchlet.designer.ui.variables.VariablesPanel(null, mainFrameToolbar, mainFrameToolbarDown, createToolbar("toolbarScripts"), createToolbar("toolbarScriptsConsole"), menuItems, toolbarItems);

        // sketchletPanel.setPreferredSize( new Dimension( 550, 200 ) );

        panelFiles.setLayout(new BorderLayout());
        panelFiles.add(scrollPaneFiles, BorderLayout.CENTER);
        JPanel panelFileControls = new JPanel();

        JPanel panelSketches = new JPanel();
        panelSketches.setLayout(new BorderLayout());

        JTabbedPane tabsSketches = new JTabbedPane();
        tabsSketches.setTabPlacement(JTabbedPane.BOTTOM);

        desktopPanelFrame.add(desktopPanel);
        desktopPanelAutoFrame.add(desktopPanelAuto);

        tabsSketches.add(Language.translate("Auto Arrange"), desktopPanelAutoScrollPane);
        tabsSketches.add(Language.translate("Desktop"), desktopPanelScrollPane);
        tabsSketches.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabsSketches);

        // tabsSketches.add("As List", scrollPaneSketches);

        panelSketches.add(tabsSketches, BorderLayout.CENTER);

        //if (Installer.shouldDownload()) {
        /*
        * JToolBar panelDownload = new JToolBar(); //JLabel label = new
        * JLabel("New updateVariable is available "); JLabel label = new
        * JLabel(""); label.setForeground(Color.RED); JButton buttonDownload =
        * new JButton("Download Update"); buttonDownload.addActionListener(new
        * ActionListener() {
        *
        * public void actionPerformed(ActionEvent event) { checkForUpdates(); }
        * }); panelDownload.add(label); panelDownload.add(buttonDownload);
        * panelSketches.add(panelDownload, BorderLayout.SOUTH);
        */
        //}

        JButton openButton = new JButton("Open...");
        openButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                openFile();
            }
        });

        JButton filemanagerButton = new JButton("File Manager...");
        filemanagerButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                fileManager();
            }
        });

        panelFileControls.add(openButton);
        panelFileControls.add(filemanagerButton);

        final JButton helpFiles = new JButton(Workspace.createImageIcon("resources/help-browser.png", ""));
        helpFiles.setToolTipText(Language.translate("What is 'Files & JSPs'?"));
        helpFiles.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Files & JSPs", "files_and_jsp");
            }
        });

        JToolBar toolbarHelpFiles = new JToolBar();
        toolbarHelpFiles.setFloatable(false);
        toolbarHelpFiles.add(helpFiles);
        JPanel panelNorthFiles = new JPanel(new BorderLayout());
        panelNorthFiles.add(createToolbar("toolbarFiles"));
        panelNorthFiles.add(toolbarHelpFiles, BorderLayout.EAST);

        panelFiles.add(panelNorthFiles, BorderLayout.NORTH);
        JToolBar toolbar = createToolbar("toolbarSketches");
        panelSketches.add(toolbar, BorderLayout.NORTH);

        /*
         * toolbar.add(new JLabel("Profile: ")); comboProfiles =
         * Profiles.getComboBox(); toolbar.add(comboProfiles); JButton
         * manageProfiles = new JButton("...");
         * manageProfiles.addActionListener(new ActionListener() {
         *
         * public void actionPerformed(ActionEvent event) { int n =
         * Profiles.combo.getSelectedIndex();
         *
         * new ProfileManager(Workspace.mainFrame, n - 1); } });
         * toolbar.add(manageProfiles);
         */

        tabs.add(panelSketches, Language.translate("Pages"));
        // tabs.add(sketchletPanel.panel1, "Variables");
        // tabs.add(new JLabel(""), "Variables");

        tabs.setMnemonicAt(0, KeyEvent.VK_1);
//        tabs.setMnemonicAt(1, KeyEvent.VK_2);

        /*
         * if (Workspace.userMode >= 1) { tabs.add(panelProcesses, "Samples");
         * tabs.setMnemonicAt(2, KeyEvent.VK_3); }
         *
         * if (Workspace.userMode >= 2) { tabs.add(sketchletPanel.panel2,
         * "Scripts"); tabs.setMnemonicAt(3, KeyEvent.VK_4); }
         *
         */
        tabs.add(panelFiles, Language.translate("Files & JSPs"));

        projectSelectorPanel = new ProjectSelectorPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, projectSelectorPanel, tabs);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(175);

        processesPanel.add(splitPane, BorderLayout.CENTER);

        // JPanel globalDataPanel = new JPanel();
        // globalDataPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        // this.confTitle = new JTextField(consoleFrame.getTitle().replace("Control Panel: ", ""), 40);
        // globalDataPanel.add(new JLabel("Project Title:    "));
        // globalDataPanel.add(confTitle);
        JButton saveButton = new JButton(Language.translate("Save"));
        JButton saveAsButton = new JButton(Language.translate("Save As..."));
        JButton addProcessButton = new JButton(Language.translate(" Add Process... "));

        // globalDataPanel.add(saveButton);
        // globalDataPanel.add(saveAsButton);
        //globalDataPanel.add(new JLabel("  "));
        // globalDataPanel.add(addProcessButton);
        //globalDataPanel.add(editButton);
        //globalDataPanel.add(hideButton);

        // globalDataPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        // panel.add(globalDataPanel);

        JPanel panelFolder = new JPanel();
        panelFolder.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        panelFolder.setLayout(new FlowLayout(FlowLayout.LEFT));
        // panelFolder.setAlignmentX( Component.RIGHT_ALIGNMENT );
        JLabel label = new JLabel(Language.translate("Project Folder: "));
        label.setEnabled(false);
        projectFolderField = new JTextField(55);
        projectFolderField.setEditable(false);
        if (SketchletContextUtils.getCurrentProjectDir() != null) {
            projectFolderField.setText(SketchletContextUtils.getCurrentProjectDir());
        }

        panelFolder.add(label);
        panelFolder.add(projectFolderField);
        panelFolder.add(new JLabel("    "));
        panelFolder.add(saveButton);

        panel.add(panelFolder);

        consoleFrame.getContentPane().add(panel, BorderLayout.NORTH);
        consoleFrame.setIconImage(Workspace.createImageIcon("resources/applications-system.png", "").getImage());
        saveButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                saveConfiguration();
            }
        });

        saveAsButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                saveConfigurationAs();
            }
        });

        addProcessButton.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        addNewProcess();
                    }
                });

        consoleButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                showConsole();
            }
        });


        editButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                editConfiguration();
            }
        });

        hideButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                showConsole();
            }
        });

        //ImageIcon icon = createImageIcon("resources/fire.jpg", "");
        //JLabel label1 = new JLabel(icon);
        //label1.setVerticalAlignment(JLabel.TOP);
        //FireImagePanel firePanel = new FireImagePanel();
        //firePanel.add(label1);
        //firePanel.setBackground(new Color(54, 54, 54));
        //add(firePanel, BorderLayout.WEST);

        // add(new JLabel(" "),BorderLayout.CENTER);

        add(processesPanel, BorderLayout.CENTER);
        add(createToolbar(), BorderLayout.NORTH);
        //toolbar.add(alwaysOnTopBtn);
        toolbar.add(helpMainWindow);

        enableMenuItems();

        enableToolbarItems();

        tableModules.addMouseListener(new PopupListenerModules());
        tableFiles.addMouseListener(new PopupListenerFiles());
        // tableSketches.addMouseListener(new PopupListenerSketches());

        /*
         * if (System.getenv("AMICO_HOME") == null ||
         * System.getenv("AMICO_HOME").trim().equals("")) {
         * JOptionPane.showMessageDialog(this, "System variable AMICO_HOME is
         * not set.\n" + "This system variable is used to determine the project
         * folder and settings.\n" + "We recommend you to set this variable, and
         * restart this application", "Warning!", JOptionPane.WARNING_MESSAGE);
         * }
         */
    }

    public void openFile() {
        if (selectedFile == null) {
            return;
        }

        String strOpener = PlatformManager.getDefaultFileOpenerCommand();

        strOpener = strOpener.replace("$f", selectedFile);

        try {
            Runtime.getRuntime().exec(strOpener);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void openFileInWebBrowser() {
        if (selectedFileShortName == null) {
            return;
        }

        String strOpener = PlatformManager.getDefaultFileOpenerCommand();

        strOpener = strOpener.replace("$f", "http://localhost:" + SketchletContextUtils.httpProjectPort + "/" + selectedFileShortName);

        try {
            Runtime.getRuntime().exec(strOpener);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void openFileInWebBrowser(File file) {
        try {
            String strOpener = PlatformManager.getDefaultFileOpenerCommand();
            strOpener = strOpener.replace("$f", file.toURI().toURL().toString());
            Runtime.getRuntime().exec(strOpener);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void editConfiguration() {
        if (SketchletContextUtils.getCurrentProjectFile() == null) {
            Object[] options = {Language.translate("Save Project..."), Language.translate("Cancel")};
            int selectedValue = JOptionPane.showOptionDialog(consoleFrame, Language.translate("You have to create a project before editong configuration file."), Language.translate("Create Configuration"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);

            switch (selectedValue) {
                case 0:
                    this.saveConfigurationAs();
                    break;

                case 1:
                    return;
            }

        } else {
            this.saveConfiguration();
        }

        if (SketchletContextUtils.getCurrentProjectFile() != null) {
            Notepad.openNotepad(SketchletContextUtils.getCurrentProjectFile(), new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    Workspace.processRunner.loadProcesses(new File(SketchletContextUtils.getCurrentProjectFile()), false);
                }
            });
        }

    }

    public void editCategories() {
        String strFile = SketchletContextUtils.getCurrentProjectNotebookDir() + "categories.txt";
        File file = new File(SketchletContextUtils.getCurrentProjectNotebookDir() + "categories.txt");

        if (!file.exists()) {
            FileUtils.saveFileText(strFile, "# Categories");
        }

        Notepad.openNotepad(strFile, new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
            }
        });
    }

    public void addNewProcess() {
        Workspace.processRunner.addProcess("0", "", "", "New Module", "", 0, false, "", "", "", "");

        processRunner.tabbedPane.setSelectedIndex(processRunner.tabbedPane.getTabCount() - 1);

        int index = Workspace.mainPanel.tableModules.getRowCount() - 1;

        if (index >= 0) {
            Workspace.mainPanel.tableModules.getSelectionModel().setSelectionInterval(index, index);
        }
        Workspace.mainPanel.refreshData(true);

    }

    public void refreshSketches() {
        this.desktopPanel.refresh();
        // this.tableModelSketches.refreshData();
    }

    public void exportProcesses() {
        final JFileChooser fc = new JFileChooser();
        String amicoHome = SketchletContext.getInstance().getApplicationHomeDir();
        if (amicoHome == null) {
            amicoHome = ".";
        }

        fc.setCurrentDirectory(new File(amicoHome));
        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            this.exportProcesses(file, false);
        } else {
            return;
        }

    }

    public void importProcess() {
        new SelectionTree("Select Process", "Import", "resources/import.gif", true);

        Workspace.mainPanel.refreshData(true);
    }

    public void openProject() {
        if (!SketchletEditor.close()) {
            return;
        }

//        new SelectionTree("Open Project", "Open", "resources/folder-open.png", false);
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(Language.translate("Select Project Folder"));
        fc.setApproveButtonText(Language.translate(fc.getApproveButtonText()));
        fc.setCurrentDirectory(new File(SketchletContextUtils.getDefaultProjectsRootLocation()));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showDialog(Workspace.mainFrame, "Open Project");

        if (returnVal == fc.APPROVE_OPTION) {
            String strPath = fc.getSelectedFile().getPath();

            Workspace.openProject(strPath, false);
        }

        Workspace.mainPanel.refreshData(false);
    }

    public void showConsoleIfNotVisible() {
        if (!consoleFrame.isVisible()) {
            showConsole();
        }

    }

    public void addFileToProject() {
        final JFileChooser fc = new JFileChooser();
        String fileHome = SketchletContextUtils.getSketchletDesignerTemplateFilesDir();
        if (fileHome == null) {
            fileHome = ".";
        }

        fc.setCurrentDirectory(new File(fileHome));
        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            String strName = JOptionPane.showInputDialog(Workspace.mainFrame, "File Name:", file.getName());

            if (strName != null) {
                File newFile = new File(SketchletContextUtils.getCurrentProjectDir() + strName);

                if (newFile.exists()) {
                    int response = JOptionPane.showConfirmDialog(this,
                            "File '" + strName + "' already exisits in the project folder.\nOverwrite existing file?", "Confirm Overwrite",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.CANCEL_OPTION) {
                        return;
                    }

                }

                FileUtils.copyFile(file, newFile);
            }
        } else {
            return;
        }
    }

    public void showConsole() {
        JMenuItem menuItem = (JMenuItem) menuItems.get("viewdetails");
        if (consoleFrame.isVisible()) {
            consoleFrame.setVisible(false);
            consoleButton.setText("Show Details and Console...");
            if (menuItem != null) {
                menuItem.setText("Show Details and Console...");
            }

        } else {
            projectFolderField.setText(SketchletContextUtils.getCurrentProjectDir());
            consoleFrame.pack();
            consoleFrame.setVisible(true);
            consoleButton.setText("Hide Details and Console");
            if (menuItem != null) {
                menuItem.setText("Hide Details and Console");
            }
        }
    }

    protected ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = Workspace.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the menubar for the app. By default this pulls the definition of
     * the menu from the associated resource file.
     */
    public JMenuBar createMenubar(String strMenubarID) {
        JMenuItem mi;
        JMenuBar mb = new JMenuBar();

        SketchletDesignerMainPanel.strHelpHTML += "<table>\r\n";

        String[] menuKeys = tokenize(getResourceString(strMenubarID));
        for (int i = 0; i < menuKeys.length; i++) {
            JMenu m = createMenu(menuKeys[i]);
            /*
             * String mnemonic = getResourceString(menuKeys[i] + "Mnemonic"); if
             * (mnemonic != null && mnemonic.length() > 0) {
             * m.setMnemonic(mnemonic.charAt(0)); }
             */

            if (m != null) {
                mb.add(m);
            }
        }

        SketchletDesignerMainPanel.strHelpHTML += "</table>\r\n";
        return mb;
    }

    public boolean isActive(String id) {
        if (id.equalsIgnoreCase("communicator") && !Profiles.isActive("variables")) {
            return false;
        } else if (id.equalsIgnoreCase("pageactions") && !Profiles.isActive("page_actions")) {
            return false;
        } else if (id.equalsIgnoreCase("pageproperties") && !Profiles.isActive("page_properties")) {
            return false;
        } else if (id.equalsIgnoreCase("pageperspective") && !Profiles.isActive("page_perspective")) {
            return false;
        } else if (id.equalsIgnoreCase("process") && !Profiles.isActive("io_services")) {
            return false;
        } else if (id.equalsIgnoreCase("scripts") && !Profiles.isActive("scripts")) {
            return false;
        } else if (id.equalsIgnoreCase("spreadsheets") && !Profiles.isActive("scripts")) {
            return false;
        } else if (id.equalsIgnoreCase("spreadsheet") && !Profiles.isActive("scripts")) {
            return false;
        } else if (id.equalsIgnoreCase("openexternaleditor") && !Profiles.isActive("scripts")) {
            return false;
        } else //  services - timers curves - macros - screenpoking - scripts
            if (id.equalsIgnoreCase("services") && !Profiles.isActive("io_services")) {
                return false;
            } else if (id.equalsIgnoreCase("timers") && !Profiles.isActive("timers")) {
                return false;
            } else if (id.equalsIgnoreCase("curves") && !Profiles.isActive("timers")) {
                return false;
            } else if (id.equalsIgnoreCase("macros") && !Profiles.isActive("macros")) {
                return false;
            } else if (id.equalsIgnoreCase("screenpoking") && !Profiles.isActive("screen_poking")) {
                return false;
            } else if (id.equalsIgnoreCase("activeregion") && !Profiles.isActive("active_regions_layer")) {
                return false;
            } else if (id.equalsIgnoreCase("mode2") && !Profiles.isActive("active_regions_layer")) {
                return false;
            } else /*
         * if (id.equalsIgnoreCase("mode3") &&
         * !Profiles.isActive("active_regions_layer")) { return false; }
         */ if (id.equalsIgnoreCase("infovar") && !Profiles.isActive("variables")) {
                return false;
            } else if (id.equalsIgnoreCase("infopage") && !Profiles.isActive("toolbar_textinfo")) {
                return false;
            } else if (id.equalsIgnoreCase("inforegions") && !Profiles.isActive("toolbar_textinfo")) {
                return false;
            } else if (id.equalsIgnoreCase("eye") && !Profiles.isActive("toolbar_eye")) {
                return false;
            } else if (id.equalsIgnoreCase("connectors") && !Profiles.isActive("variables_network_connectors")) {
                return false;
            } else if (id.equalsIgnoreCase("derivedvars")) {
                if (!Profiles.isActive("derived_variables")) {
                    return false;
                }
                if (!Profiles.isActive("variables_aggregate")) {
                    return false;
                }
            } else if (id.equalsIgnoreCase("tablevars") && !Profiles.isActive("table_variables")) {
                return false;
            } else if (id.equalsIgnoreCase("pausecomm") && !Profiles.isActive("variables_pause")) {
                return false;
            } else if (id.equalsIgnoreCase("sysvars") && !Profiles.isActive("variables_system")) {
                return false;
            }

        return true;
    }

    /**
     * Fetch the menu item that was created for the given command.
     *
     * @param cmd Name of the action. @returns item created for the given
     *            command or null if one wasn't created.
     */
    protected JMenuItem getMenuItem(String cmd) {
        return (JMenuItem) menuItems.get(cmd);
    }

    protected Action getAction(String cmd) {
        return (Action) commands.get(cmd);
    }

    protected String getResourceString(String nm) {
        String str;
        try {
            str = resources == null ? nm : resources.getString(nm);
        } catch (MissingResourceException mre) {
            str = null;
        }

        return str;
    }

    protected URL getResource(String key) {
        String name = getResourceString(key);

        if (name != null) {
            URL url = Workspace.class.getResource(name);

            return url;
        }
        return null;
    }

    /**
     * Take the given string and chop it up into a series of strings on
     * whitespace boundaries. This is useful for trying to getVariableValue an
     * array of strings out of the resource file.
     */
    protected String[] tokenize(String input) {
        Vector v = new Vector();
        StringTokenizer t = new StringTokenizer(input);
        String cmd[];

        while (t.hasMoreTokens()) {
            v.addElement(t.nextToken());
        }

        cmd = new String[v.size()];
        for (int i = 0; i
                < cmd.length; i++) {
            cmd[i] = (String) v.elementAt(i);
        }

        return cmd;
    }

    /**
     * Create a menu for the app. By default this pulls the definition of the
     * menu from the associated resource file.
     */
    protected JMenu createMenu(String key) {
        if (key.equalsIgnoreCase("activeregion")) {
            return SketchletEditor.editorPanel.activeRegionMenu;
        } else if (key.equalsIgnoreCase("profiles")) {
            return Profiles.getMenu();
        }
        String[] itemKeys = tokenize(getResourceString(key));
        String strMenuLabel = getResourceString(key + "Label");
        final JMenu menu = new JMenu(Language.translate(strMenuLabel));

        TutorialPanel.writeTerm(strMenuLabel);

        menu.addMouseMotionListener(new MouseAdapter() {

            public void mouseMoved(MouseEvent e) {
                TutorialPanel.addLine(menubar);
            }
        });

        String mnemonic = getResourceString(key + "Mnemonic");
        if (mnemonic != null && mnemonic.length() > 0) {
            menu.setMnemonic(mnemonic.charAt(0));
        }

        SketchletDesignerMainPanel.strHelpHTML += "<tr><td colspan='3'><b>" + strMenuLabel + "</b></td></tr>\r\n";

        boolean prevSeparator = false;
        boolean addSeparator = false;
        for (int i = 0; i < itemKeys.length; i++) {
            if (itemKeys[i].equals("-")) {
                if (!prevSeparator && menu.getItemCount() > 0) {
                    addSeparator = true;
                    prevSeparator = true;
                } else {
                    continue;
                }

            } else if (itemKeys[i].equals("openrecent")) {
                recentProjectsMenu = createMenu("openrecentmenu");
                menu.add(recentProjectsMenu);
                URL url = getResource("openrecentmenu" + this.imageSuffix);
                if (url != null) {
                    recentProjectsMenu.setHorizontalTextPosition(JButton.RIGHT);
                    recentProjectsMenu.setIcon(new ImageIcon(url));
                }

                prevSeparator = false;
            } else if (itemKeys[i].equals("projectsettings")) {
                projectSettingsMenu = createMenu("projectsettingsmenu");
                menu.add(projectSettingsMenu);
                URL url = getResource("projectsettingsmenu" + this.imageSuffix);
                if (url != null) {
                    projectSettingsMenu.setHorizontalTextPosition(JButton.RIGHT);
                    projectSettingsMenu.setIcon(new ImageIcon(url));
                }

                prevSeparator = false;
            } else if (itemKeys[i].equals("systemsettings")) {
                systemSettingsMenu = createMenu("systemsettingsmenu");
                menu.add(systemSettingsMenu);
                URL url = getResource("systemsettingsmenu" + this.imageSuffix);
                if (url != null) {
                    systemSettingsMenu.setHorizontalTextPosition(JButton.RIGHT);
                    systemSettingsMenu.setIcon(new ImageIcon(url));
                }

                populateSettingsMenu(systemSettingsMenu, null);
                prevSeparator = false;
            } else if (isActive(itemKeys[i])) {
                if (addSeparator) {
                    addSeparator = false;
                    menu.addSeparator();
                }
                JMenuItem mi = createMenuItem(itemKeys[i], menu);
                menu.add(mi);
                prevSeparator = false;
            }

        }
        SketchletDesignerMainPanel.strHelpHTML += "</td></tr>\r\n";
        return menu;
    }

    public static void populateSettingsMenu(JMenu menu, File confDir) {
        if (menu == null) {
            return;
        }

        menu.removeAll();

        if (confDir == null) {
            confDir = new File(SketchletContextUtils.getSketchletDesignerConfDir());
        }

        if (!confDir.isDirectory()) {
            return;
        }

        String strFiles[] = confDir.list();

        for (int i = 0; i < strFiles.length; i++) {
            final File file = new File(confDir + File.separator + strFiles[i]);

            if (file.isDirectory()) {
                final JMenu submenu = new JMenu(strFiles[i]);

                submenu.addMouseMotionListener(new MouseAdapter() {

                    public void mouseMoved(MouseEvent e) {
                        TutorialPanel.addLine(submenu);
                    }
                });

                populateSettingsMenu(submenu, file);

                menu.add(submenu);
            } else {
                JMenuItem menuItem = new JMenuItem(strFiles[i]);
                menu.add(menuItem);
                menuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent event) {
                        Notepad.openNotepad(file.getPath(), null);
                    }
                });
            }

        }
    }

    /**
     * This is the hook through which all menu items are created. It registers
     * the result with the menuitem hashtable so that it can be fetched with
     * getMenuItem().
     *
     * @see #getMenuItem
     */
    protected JMenuItem createMenuItem(String cmd, final JMenu menu) {
        final String strLabel = getResourceString(cmd + labelSuffix);
        String strTip = Language.translate(getResourceString(cmd + tipSuffix));
        String strURL = getResourceString(cmd + imageSuffix);
        JMenuItem mi = new JMenuItem(Language.translate(strLabel));
        TutorialPanel.writeTerm(strLabel);
        URL url = getResource(cmd + imageSuffix);
        if (url != null) {
            mi.setHorizontalTextPosition(JButton.RIGHT);
            mi.setIcon(new ImageIcon(url));
        }

        String astr = getResourceString(cmd + actionSuffix);
        if (astr == null) {
            astr = cmd;
        }
        SketchletDesignerMainPanel.strHelpHTML += "<tr>\r\n";
        SketchletDesignerMainPanel.strHelpHTML += "<td>" + (strURL == null ? "" : "<img src='" + strURL + "'>") + "</td>\r\n";
        SketchletDesignerMainPanel.strHelpHTML += "<td>" + strLabel + "</td>\r\n";
        SketchletDesignerMainPanel.strHelpHTML += "<td>" + (strTip == null ? "&nbsp;" : strTip) + "</td>\r\n";
        SketchletDesignerMainPanel.strHelpHTML += "</tr>\r\n";

        mi.setActionCommand(astr);

        String mnemonic = getResourceString(cmd + "Mnemonic");
        if (mnemonic != null && mnemonic.length() > 0) {
            mi.setMnemonic(mnemonic.charAt(0));
        }

        mi.addActionListener(this);
        mi.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                TutorialPanel.addLine("cmd", "Select the menu item " + strLabel, "menu.png", menu, 1000);
            }
        });

        setAccelerationKeys(mi, cmd);

        menuItems.put(cmd, mi);
        return mi;
    }

    public void setAccelerationKeys(JMenuItem item, String text) {
        if (text.equalsIgnoreCase("undo")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("redo")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("copy")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("paste")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("selectall")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("newsketch")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("helpcurrent")) {
            item.setAccelerator(KeyStroke.getKeyStroke("F1"));
        } else if (text.equalsIgnoreCase("execute")) {
            item.setAccelerator(KeyStroke.getKeyStroke("control F5"));
        } else if (text.equalsIgnoreCase("generatecode")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK));
        } else if (text.equalsIgnoreCase("imagecache")) {
            item.setAccelerator(KeyStroke.getKeyStroke("F12"));
        } else if (text.equalsIgnoreCase("savesketch")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("duplicatesketch")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("rulers")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("grid")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("variables")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("zoomreset")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("zoomin")) {
            item.setAccelerator(KeyStroke.getKeyStroke("control PLUS"));
        } else if (text.equalsIgnoreCase("zoomout")) {
            item.setAccelerator(KeyStroke.getKeyStroke("control MINUS"));
        } else if (text.equalsIgnoreCase("externalimageeditor")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("mode1")) {
            item.setAccelerator(KeyStroke.getKeyStroke("F4"));
        } else if (text.equalsIgnoreCase("mode2")) {
            item.setAccelerator(KeyStroke.getKeyStroke("F5"));
            /*
             * } else if (text.equalsIgnoreCase("mode3")) {
             * item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3,
             * Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
             */
        } else if (text.equalsIgnoreCase("infovar")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("infopage")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("inforegions")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else if (text.equalsIgnoreCase("eye")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            /*
             * } else if (text.equalsIgnoreCase("importimage")) {
             * item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
             * Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
             */
        } else if (text.equalsIgnoreCase("screenrecorder")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK));
        } else if (text.equalsIgnoreCase("pages")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + SketchletEditor.pageTabIndex, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK));
        } else if (text.equalsIgnoreCase("services")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + SketchletEditor.ioservicesTabIndex, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK));
        } else if (text.equalsIgnoreCase("timers")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + SketchletEditor.programmingTabIndex + SketchletEditor.timersTabIndex, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK));
        } else if (text.equalsIgnoreCase("macros")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + SketchletEditor.programmingTabIndex + SketchletEditor.macrosTabIndex, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK));
        } else if (text.equalsIgnoreCase("screenpoking")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + SketchletEditor.programmingTabIndex + SketchletEditor.screenpokingTabIndex, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK));
        } else if (text.equalsIgnoreCase("scripts")) {
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1 + SketchletEditor.programmingTabIndex + SketchletEditor.scriptsTabIndex, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | Event.SHIFT_MASK));
        }
    }

    public void actionPerformed(ActionEvent event) {
        String cmd = event.getActionCommand();
        this.executeCommand(cmd, event);
    }

    public void executeCommand(String cmd, ActionEvent event) {
        Component source = null;
        if (event.getSource() instanceof Component) {
            source = (Component) event.getSource();
        }
        ActivityLog.log(cmd, "");

        if (cmd.equals("new")) {
            createNewProject(true, true, "New Project");
        } else if (cmd.equals("save")) {
            this.saveConfiguration();
        } else if (cmd.equals("saveas")) {
            this.saveConfigurationAs();
        } else if (cmd.equals("importurl")) {
            this.importFromURL();
        } else if (cmd.equals("importzip")) {
            this.importFromZIP();
        } else if (cmd.equals("savehistory")) {
            this.saveToHistory();
        } else if (cmd.equals("open")) {
            openProject();
        } else if (cmd.equals("start-process")) {
            int rows[] = this.tableModules.getSelectedRows();
            for (int i = 0; i < rows.length; i++) {
                ((ProcessConsolePanel) Workspace.consolePane.tabbedPane.getComponentAt(rows[i])).startProcess();
                this.tableModelModules.fireTableDataChanged();
            }

        } else if (cmd.equals("stop-process")) {
            int rows[] = this.tableModules.getSelectedRows();
            for (int i = 0; i < rows.length; i++) {
                ((ProcessConsolePanel) Workspace.consolePane.tabbedPane.getComponentAt(rows[i])).stopProcess();
                this.tableModelModules.fireTableDataChanged();
            }

        } else if (cmd.equals("restart-process")) {
            int rows[] = this.tableModules.getSelectedRows();
            for (int i = 0; i
                    < rows.length; i++) {
                ((ProcessConsolePanel) Workspace.consolePane.tabbedPane.getComponentAt(rows[i])).stopProcess();
                ((ProcessConsolePanel) Workspace.consolePane.tabbedPane.getComponentAt(rows[i])).startProcess();
                this.tableModelModules.fireTableDataChanged();
            }

        } else if (cmd.equals("add-process")) {
            this.addNewProcess();
        } else if (cmd.equals("sketches")) {
            openSketches(false);
        } else if (cmd.equals("playsketches")) {
            openSketches(true);
        } else if (cmd.equals("newsketch")) {
            SketchletEditor.editorPanel.newSketch();
        } else if (cmd.equals("savesketch")) {
            GlobalProperties.save();
            SketchletEditor.editorPanel.saveAndWait();
            Timers.globalTimers.save();
            Macros.globalMacros.save();
            Workspace.mainPanel.saveConfiguration();
            Workspace.mainPanel.saveOriginal();
        } else if (cmd.equals("savesketchas")) {
            this.saveProjectAsFromEditor();
        } else if (cmd.equals("execute")) {
            SketchletEditor.editorPanel.play();
        } else if (cmd.equals("executerecord")) {
            SketchletEditor.editorPanel.playAndRecord();
        } else if (cmd.equals("exiteditor")) {
            SketchletEditor.close();
        } else if (cmd.equals("rotateanticlockwise")) {
            SketchletEditor.editorPanel.sketchletImages.rotateClockwise();
        } else if (cmd.equals("rotateclockwise")) {
            SketchletEditor.editorPanel.sketchletImages.rotateClockwise();
        } else if (cmd.equals("flipvertical")) {
            SketchletEditor.editorPanel.sketchletImages.flipVertical();
        } else if (cmd.equals("fliphorizontal")) {
            SketchletEditor.editorPanel.sketchletImages.flipHorizontal();
        } else if (cmd.equalsIgnoreCase("infovar")) {
            SketchletEditor.editorPanel.sketchToolbar.visualizeVariables.doClick();
        } else if (cmd.equalsIgnoreCase("infopage")) {
            SketchletEditor.editorPanel.sketchToolbar.visualizeInfoPage.doClick();
        } else if (cmd.equalsIgnoreCase("inforegions")) {
            SketchletEditor.editorPanel.sketchToolbar.visualizeInfo.doClick();
        } else if (cmd.equalsIgnoreCase("eye")) {
            SketchletEditor.editorPanel.sketchToolbar.visualizeEye.doClick();
        } else if (cmd.equals("mode1")) {
            SketchletEditor.editorPanel.tabsModes.setSelectedIndex(0);
            /*
             * } else if (cmd.equals("mode2")) {
             * SketchletEditor.editorPanel.tabsModes.setSelectedIndex(1);
             */
        } else if (cmd.equals("mode2")) {
            SketchletEditor.editorPanel.tabsModes.setSelectedIndex(1);
        } else if (cmd.equals("helptutor")) {
            TutorialPanel.open();
        } else if (cmd.equals("helpindex")) {
            SketchletEditor.editorPanel.sketchToolbar.showNavigator(true);
            SketchletEditor.editorPanel.tabsBrowser.setSelectedIndex(SketchletEditor.editorPanel.tabsBrowser.getTabCount() - 1);
            SketchletEditor.editorPanel.helpViewer.showHelpByID("index");
        } else if (cmd.equals("profile1")) {
            SketchletEditor.editorPanel.editorFrame.getContentPane().removeAll();
            Workspace.mainPanel.comboProfiles.setSelectedIndex(0);
            SketchletEditorFrame.fillFrame();
            SketchletEditor.editorPanel.loadLayersTab();


            SketchletEditor.editorPanel.enableControls();
            SketchletEditor.editorPanel.repaint();
            if (SketchletEditor.editorPanel.currentPage != null) {
                int index = SketchletEditor.editorPanel.pages.pages.indexOf(SketchletEditor.editorPanel.currentPage);
                SketchletEditor.editorPanel.sketchListPanel.table.getSelectionModel().setSelectionInterval(index, index);
                //freeHand.sketchListPanel.desktopPanel.ensureVisible(editorPanel.currentSketch);
            }

            SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.activeRegionSelectTool, null);
        } else if (cmd.equals("helpcurrent")) {
            SketchletEditor.editorPanel.sketchToolbar.showNavigator(true);
            SketchletEditor.editorPanel.tabsBrowser.setSelectedIndex(SketchletEditor.editorPanel.tabsBrowser.getTabCount() - 1);
        } else if (cmd.equals("helprandom")) {
            DidYouKnow.showFrame(Workspace.mainFrame);
        } else if (cmd.equals("duplicatesketch")) {
            SketchletEditor.editorPanel.duplicate();
        } else if (cmd.equals("deletesketch")) {
            SketchletEditor.editorPanel.delete();
        } else if (cmd.equals("generatecode")) {
            CodeGeneratorDialog.showFrame(SketchletEditor.editorFrame);
        } else if (cmd.equals("resizesketch")) {
            SketchletEditor.editorPanel.resize();
        } else if (cmd.equals("movesketchleft")) {
            SketchletEditor.editorPanel.moveLeft();
        } else if (cmd.equals("movesketchright")) {
            SketchletEditor.editorPanel.moveRight();
        } else if (cmd.equals("setmastersketch")) {
            SketchletEditor.editorPanel.setAsMaster();
        } else if (cmd.equals("reportsketch")) {
            new ReportFrame();
        } else if (cmd.equals("pageactions")) {
            SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, 0);
        } else if (cmd.equals("pageproperties")) {
            SketchStatePanel.showStateProperties(SketchStatePanel.propertiesTabIndex, 0);
        } else if (cmd.equals("pageperspective")) {
            SketchStatePanel.showStateProperties(SketchStatePanel.perspectiveTabIndex, 0);
        } else if (cmd.equals("playback")) {
            openSketches(true);
        } else if (cmd.equals("pages")) {
            if (!SketchletEditor.editorPanel.tabsVisible) {
                SketchletEditor.editorPanel.sketchToolbar.showHideProjectNavigator();
            }
            SketchletEditor.editorPanel.tabsRight.setSelectedIndex(SketchletEditor.pageTabIndex);
        } else if (cmd.equals("services")) {
            if (!SketchletEditor.editorPanel.tabsVisible) {
                SketchletEditor.editorPanel.sketchToolbar.showHideProjectNavigator();
            }
            SketchletEditor.editorPanel.tabsRight.setSelectedIndex(SketchletEditor.ioservicesTabIndex);
        } else if (cmd.equals("timers")) {
            if (!SketchletEditor.editorPanel.tabsVisible) {
                SketchletEditor.editorPanel.sketchToolbar.showHideProjectNavigator();
            }
            SketchletEditor.editorPanel.tabsRight.setSelectedIndex(SketchletEditor.timersTabIndex);
            SketchletEditor.editorPanel.showExtraEditorPanel(ExtraEditorPanel.indexTimer);
        } else if (cmd.equals("macros")) {
            if (!SketchletEditor.editorPanel.tabsVisible) {
                SketchletEditor.editorPanel.sketchToolbar.showHideProjectNavigator();
            }
            SketchletEditor.editorPanel.tabsRight.setSelectedIndex(SketchletEditor.macrosTabIndex);
            SketchletEditor.editorPanel.showExtraEditorPanel(ExtraEditorPanel.indexMacro);
        } else if (cmd.equals("screenpoking")) {
            if (!SketchletEditor.editorPanel.tabsVisible) {
                SketchletEditor.editorPanel.sketchToolbar.showHideProjectNavigator();
            }
            SketchletEditor.editorPanel.tabsRight.setSelectedIndex(SketchletEditor.screenpokingTabIndex);
        } else if (cmd.equals("scripts")) {
            if (!SketchletEditor.editorPanel.tabsVisible) {
                SketchletEditor.editorPanel.sketchToolbar.showHideProjectNavigator();
            }
            SketchletEditor.editorPanel.tabsRight.setSelectedIndex(SketchletEditor.scriptsTabIndex);
        } else if (cmd.equals("memory")) {
            if (SketchletEditor.editorPanel != null) {
                SketchletEditor.editorPanel.addMemoryPanel();
            }
        } else if (cmd.equals("curves")) {
            CurvesFrame.showFrame();
        } else if (cmd.equals("interactionspace")) {
            InteractionSpace.load();
            InteractionSpaceFrame.showFrame();
            /*
             * } else if (cmd.equals("history")) { //
             * fileManager(SketchletContextUtils.getCurrentProjectHistoryDir());
             * // new HistoryWindow();
             */
        } else if (cmd.equals("remove-process")) {
            int rows[] = this.tableModules.getSelectedRows();

            for (int i = rows.length - 1; i >= 0; i--) {
                ((ProcessConsolePanel) Workspace.consolePane.tabbedPane.getComponentAt(rows[i])).removeProcess();
            }

        } else if (cmd.equals("editconfig")) {
            this.editConfiguration();
        } else if (cmd.equals("editcategories")) {
            this.editCategories();
        } else if (cmd.equals("import-process")) {
            this.importProcess();
        } else if (cmd.equals("export-process")) {
            this.exportProcesses();
        } else if (cmd.equals("teammembers")) {
            this.teamMembers();
            /*
             * } else if (cmd.equals("addnote")) { HTMLEditor.createNewNote(); }
             * else if (cmd.equals("addscreenshot")) {
             * CaptureScreen.createNewScreenshot(); } else if
             * (cmd.equals("addimage")) { HTMLEditor.createNewImageNote(); }
             * else if (cmd.equals("addvideo")) {
             * HTMLEditor.createNewVideoNote(); } else if
             * (cmd.equals("importnotes")) { HTMLEditor.importNotes();
             */
        } else if (cmd.equals("view-details")) {
            this.showConsole();
        } else if (cmd.equals("spreadsheet")) {
            this.openDefaultSpreadsheet();
        } else if (cmd.equals("rdesktop")) {
            this.remoteDesktop();
        } else if (cmd.equals("undo")) {
            SketchletEditor.editorPanel.undo();
            //} else if (cmd.equals("redo")) {
            // SketchletEditor.editorPanel.redo();
        } else if (cmd.equals("copy")) {
            SketchletEditor.editorPanel.editorClipboard.copy();
        } else if (cmd.equals("paste")) {
            SketchletEditor.editorPanel.paste();
        } else if (cmd.equals("grid")) {
            SketchletEditor.editorPanel.sketchToolbar.snapGrid.doClick();
        } else if (cmd.equals("rulers")) {
            SketchletEditor.editorPanel.sketchToolbar.showRulers.doClick();
        } else if (cmd.equals("zoomin")) {
            SketchletEditor.editorPanel.sketchToolbar.bigger.doClick();
        } else if (cmd.equals("zoomreset")) {
            SketchletEditor.editorPanel.sketchToolbar.zoomBox.setSelectedItem("100%");
        } else if (cmd.equals("zoomout")) {
            SketchletEditor.editorPanel.sketchToolbar.smaller.doClick();
        } else if (cmd.equals("selectall")) {
            SketchletEditor.editorPanel.selectAll();
        } else if (cmd.equals("variables")) {
            SketchletEditor.editorPanel.sketchToolbar.showHideProjectNavigator();
            //services - timers curves - macros - screenpoking - scripts
        } else if (cmd.equals("import-sketches")) {
            SketchletEditor.editorPanel.importSketches();
        } else if (cmd.equals("cache-images")) {
            ImageCache.load();
        } else if (cmd.equals("clear-image-cache")) {
            ImageCache.clear();
            System.gc();
        } else if (cmd.equals("exportsketches")) {
            SketchletEditor.editorPanel.saveSketchAsImage();
        } else if (cmd.equals("printsketches")) {
            SketchletEditor.editorPanel.printPage();
        } else if (cmd.equals("sketch-parameters")) {
            PropertiesFrame.showFrame();
        } else if (cmd.equals("umlmodel")) {
            this.openDefaultUML();
        } else if (cmd.equals("reloadfiles")) {
            this.reloadFiles();
        } else if (cmd.equals("helpfiles")) {
            HelpUtils.openHelpFile("Sketchlet", "files_and_jsp");
        } else if (cmd.equals("helpservices")) {
            HelpUtils.openHelpFile("Sketchlet", "services");
        } else if (cmd.equals("helpscripts")) {
            HelpUtils.openHelpFile("Sketchlet", "scripts");
        } else if (cmd.equals("helptimers")) {
            HelpUtils.openHelpFile("Sketchlet", "timers");
        } else if (cmd.equals("helpmacros")) {
            HelpUtils.openHelpFile("Sketchlet", "timers");
        } else if (cmd.equals("helpscreenpoking")) {
            HelpUtils.openHelpFile("Sketchlet", "screen_poking");
        } else if (cmd.equals("systemlaf")) {
            GlobalProperties.setAndSave("look-and-feel", "System");
            JOptionPane.showMessageDialog(Workspace.mainFrame, Language.translate("You have to restart Sketchlet for this change to take effect."));
        } else if (cmd.equals("nimbuslaf")) {
            GlobalProperties.setAndSave("look-and-feel", "Nimbus");
            JOptionPane.showMessageDialog(Workspace.mainFrame, Language.translate("You have to restart Sketchlet for this change to take effect."));
        } else if (cmd.equals("substancelaf")) {
            GlobalProperties.setAndSave("look-and-feel", "Substance");
            JOptionPane.showMessageDialog(Workspace.mainFrame, Language.translate("You have to restart Sketchlet for this change to take effect."));
        } else if (cmd.equals("motiflaf")) {
            GlobalProperties.setAndSave("look-and-feel", "Motif");
            JOptionPane.showMessageDialog(Workspace.mainFrame, Language.translate("You have to restart Sketchlet for this change to take effect."));
        } else if (cmd.equals("screenscripts")) {
            // ScreenScripts.openScreenScripts(false);
        } else if (cmd.equals("screenrecorder")) {
            this.screenRecorder();
        } else if (cmd.equals("projecttitle")) {
            this.changeProjectTitle();
        } else if (cmd.equals("update")) {
            this.checkForUpdates();
        } else if (cmd.equals("about")) {
            this.about();
        } else if (cmd.equals("update")) {
            this.checkForUpdates();
        } else if (cmd.equals("credits")) {
            this.credits();
        } else if (cmd.equals("plugins")) {
            this.listPlugins();
        } else if (cmd.equals("viewinbrowser")) {
            SketchletContextUtils.openWebBrowser("http://localhost:" + SketchletContextUtils.httpBlogPort + "/ListNotes.jsp?dir=" + SketchletContextUtils.getCurrentProjectNotebookDir() + "&projectTitle=" + SketchletContextUtils.getCurrentProjectDirName());
        } else if (cmd.equals("projectsroot")) {
            Notepad.openNotepad(SketchletContextUtils.getSketchletDesignerConfDir() + "default_project_location.txt", null);
        } else if (cmd.equals("imageeditor")) {
            this.imageEditor();
        } else if (cmd.equals("externalimageeditor")) {
            SketchletEditor.editorPanel.openExternalEditor();
        } else if (cmd.equals("refreshimage")) {
            SketchletEditor.editorPanel.refresh();
        } else if (cmd.equals("importimage")) {
            SketchletEditor.editorPanel.fromFile();
        } else if (cmd.equals("clearall")) {
            SketchletEditor.editorPanel.clearAll();
        } else if (cmd.equals("clearallimage")) {
            SketchletEditor.editorPanel.clearAllImage();
        } else if (cmd.equals("filemanager")) {
            this.fileManager();
            /*
             * } else if (cmd.equals("noteorganizer")) {
             * Organizer.createAndShowGUI();
             */
        } else if (cmd.equals("remotebackup")) {
            RemoteLocationFrame.createAndShowGUI();
        } else if (cmd.equals("restorebackup")) {
            RestoreRemoteLocationFrame.createAndShowGUI();
        } else if (cmd.equals("helpusing")) {
            try {
                SketchletContextUtils.openWebBrowser("http://amico.sourceforge.net");
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (cmd.equals("exit")) {
            Workspace.mainPanel.saveConfiguration();
            Workspace.processRunner.killProcesses();
            System.exit(0);
        } else if (cmd.equals("newscript")) {
            this.sketchletPanel.panel2.createNewScript();
        } else if (cmd.equals("importscript")) {
            this.sketchletPanel.panel2.importScript();
        } else if (cmd.equals("editscript")) {
            this.sketchletPanel.panel2.editScript();
        } else if (cmd.equals("editscriptexternal")) {
            this.sketchletPanel.panel2.editScriptExternal();
        } else if (cmd.equals("removescript")) {
            this.sketchletPanel.panel2.removeScript();
        } else if (cmd.equals("startscript")) {
            this.sketchletPanel.panel2.startScript();
        } else if (cmd.equals("stopscript")) {
            this.sketchletPanel.panel2.stopScript();
        } else if (cmd.equals("reloadallscripts")) {
            this.sketchletPanel.panel2.reloadAll();
        } else if (cmd.equals("savescriptimage")) {
            try {
                BufferedImage img = SketchletEditor.editorPanel.currentPage.images[0];
                Graphics2D g2 = img.createGraphics();
                g2.drawImage(SketchletContextImpl.image, 0, 0, null);
                g2.dispose();
                SketchletGraphicsContext.getInstance().clearCanvas();
                SketchletGraphicsContext.getInstance().repaint();
                SketchletEditor.editorPanel.repaint();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (cmd.equals("showconsole")) {
            // this.sketchletPanel.panel2.showConsole();
            SketchletGraphicsContext.getInstance().clearCanvas();
            SketchletGraphicsContext.getInstance().repaint();
            ScriptConsole.console.textArea.setText("");
        } else if (cmd.equals("addvar")) {
            sketchletPanel.globalVariablesPanel.addVariable();
        } else if (cmd.equals("sysvar")) {
            new SystemVariablesDialog(SketchletEditor.editorFrame);
        } else if (cmd.equals("removevar")) {
            sketchletPanel.globalVariablesPanel.removeVariable();
        } else if (cmd.equals("copyvarname")) {
            sketchletPanel.globalVariablesPanel.copyVariableName();
        } else if (cmd.equals("aggregatevalueview")) {
            sketchletPanel.globalVariablesPanel.calculateAggregateVariables();
        } else if (cmd.equals("aggregatevalue")) {
            sketchletPanel.globalVariablesPanel.calculateAggregateVariables();
        } else if (cmd.equals("derivedvars")) {
            SketchletEditor.editorPanel.showDerivedVariablesPopupMenu(event);
        } else if (cmd.equals("tablevars")) {
            SketchletEditor.editorPanel.showTableVariables();
        } else if (cmd.equals("connectors")) {
            sketchletPanel.globalVariablesPanel.networkConnectors();
        } else if (cmd.equals("serializeview")) {
            sketchletPanel.globalVariablesPanel.serializeVariable();
        } else if (cmd.equals("serialize")) {
            sketchletPanel.globalVariablesPanel.serializeVariable();
        } else if (cmd.equals("recordvars")) {
            new VariablesRecorder(SketchletEditor.editorFrame);
        } else if (cmd.equals("countfilter")) {
            sketchletPanel.globalVariablesPanel.setCountFilter();
        } else if (cmd.equals("pausecomm")) {
            sketchletPanel.globalVariablesPanel.pauseUpdates();
        } else if (cmd.equals("shellexecute")) {
            this.openFile();
        } else if (cmd.equals("openinwebbrowser")) {
            this.openFileInWebBrowser();
        } else if (cmd.equals("openinexternaleditor")) {
            this.editFileExternal();
        } else if (cmd.equals("openexternaleditor")) {
            this.editFileExternalWithoutFile();
        } else if (cmd.equals("addfile")) {
            this.addFileToProject();
            this.tableModelFiles.fireTableDataChanged();
        }

    }

    public static boolean bSavingOriginal = false;
    public static boolean bSavingOriginalSuccess = false;

    public void saveOriginal() {
        bSavingOriginal = true;
        bSavingOriginalSuccess = false;
        new Thread(new Runnable() {

            public void run() {
                try {
                    FileUtils.deleteDir(new File(SketchletContextUtils.getCurrentProjectOriginalDir()));
                    FileUtils.restore(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir(), SketchletContextUtils.getCurrentProjectOriginalDir());
                    bSavingOriginalSuccess = true;
                } catch (Exception e) {
                    bSavingOriginalSuccess = false;
                    e.printStackTrace();
                }
                bSavingOriginal = false;
            }
        }).start();
    }

    public void restoreOriginal() {
        if (bSavingOriginalSuccess && new File(SketchletContextUtils.getCurrentProjectOriginalDir()).exists()) {
            log.debug("Restoring original...");
            try {
                log.debug("Waiting...");
                while (bSavingOriginal) {
                    Thread.sleep(50);
                }
                log.debug(" DONE.");
                if (new File(SketchletContextUtils.getCurrentProjectOriginalDir()).exists()) {
                    FileUtils.deleteDir(new File(SketchletContextUtils.getCurrentProjectSkecthletsDir()));
                    FileUtils.restore(SketchletContextUtils.getCurrentProjectOriginalDir(), SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir());
                    FileUtils.deleteDir(new File(SketchletContextUtils.getCurrentProjectOriginalDir()));
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    public void openSketches(final boolean play) {
        try {
            if (!Workspace.bCloseOnPlaybackEnd) {
                MessageFrame.showMessage(Workspace.mainFrame, Language.translate("Please wait..."), Workspace.mainFrame);
            }
            Workspace.ready.await();
        } catch (Exception e) {
        }
        /*
         * amico.workspace.sketches.SketchletEditor.createAndShowGui(sketchRow,
         * play); if (!play && SketchletEditor.editorPanel != null) {
         * SketchletEditor.editorPanel.requestFocus(); }
         */

        if (!Workspace.bCloseOnPlaybackEnd) {
            MessageFrame.showMessage(Workspace.mainFrame, Language.translate("Loading pages..."), Workspace.mainFrame);
        } else {
            Workspace.splashScreen.setMessage(Language.translate("loading pages..."));
        }
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                ActiveRegionPanel.getFileChooser();

                try {
                    if (!play) {
                        saveOriginal();
                    }
                    SketchletEditorFrame.createAndShowGui(DesktopPanel.selectedIndex, play);
                } catch (Throwable e) {
                    e.printStackTrace();
                } finally {
                    MessageFrame.closeMessage();
                    if (Workspace.bCloseOnPlaybackEnd) {
                        Workspace.closeSplashScreen();
                    }
                }

                if (play) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                    }
                    SketchletEditor.editorPanel.play();
                }
                Workspace.mainFrame.toBack();

            }
        });
    }

    /*
     * public void setUserMode(int mode) {
     * FileUtils.saveFileText(SketchletContextUtils.getDefaultProjectsRootLocation()
     * + "user_level.txt", "" + mode);
     * JOptionPane.showMessageDialog(Workspace.mainFrame,
     * Language.translate("You have to restart Sketchlet for this change to take
     * effect.")); }
     */
    public void checkForUpdates() {
        try {
            Installer.open();
        } catch (Exception e) {
        }
    }

    public void about() {
        JOptionPane.showMessageDialog(Workspace.mainFrame,
                "Sketchlet\nVersion 1.1beta\n\nAuthor: \u017Deljko Obrenovi\u0107\nobren@acm.org\nobren.info/\n\nBackbase\nAmsterdam\n ", "About", JOptionPane.INFORMATION_MESSAGE);

    }

    public void credits() {
        String strCredits = "People:\n";
        strCredits += "   Berke Atasoy designed the logo and proposed some of the ideas about user interface.\n";
        strCredits += "   Javier Quevedo provided numerous suggestions for improvements.\n";
        strCredits += "   Jean-Bernard Mattens, Dragan Gasevice and Lynda Hardman helped discussing many conceptual issues.\n";
        strCredits += "\n";
        strCredits += "Open Source Projects in Sketchlet Core:\n";
        strCredits += "   Apache Commons, http://commons.apache.org/\n";
        strCredits += "   Java Scripting Project, https://scripting.dev.java.net/\n";
        strCredits += "   Java Image Editor, http://www.jhlabs.com/ie/\n";
        strCredits += "   Java OSC, http://www.illposed.com/software/javaosc.html\n";
        strCredits += "   Apache XML-RPC, http://ws.apache.org/xmlrpc/\n";
        strCredits += "   Apache Batik SVG Toolkit, http://xmlgraphics.apache.org/batik/\n";
        strCredits += "   jsyntaxpane, http://code.google.com/p/jsyntaxpane/\n";
        strCredits += "   JTidy, http://jtidy.sourceforge.net/\n";
        strCredits += "   muCommander, http://www.mucommander.com/\n";
        strCredits += "   Jep Java - Math Expression Parser, http://sourceforge.net/projects/jep/\n";
        strCredits += "   CamStudio, http://camstudio.org/\n";
        strCredits += "   Open Office, http://www.openoffice.org/\n";
        strCredits += "Open Source Projects used in Sketchlet Services:\n";
        strCredits += "   OpenCV, http://sourceforge.net/projects/opencvlibrary/\n";
        strCredits += "   FreeTTS, http://freetts.sourceforge.net/\n";
        strCredits += "   Sphinx-4, http://cmusphinx.sourceforge.net/sphinx4/\n";
        strCredits += "   WiimoteLib, http://www.codeplex.com/WiimoteLib\n";
        strCredits += "   jWordNet, http://sourceforge.net/projects/jwordnet/\n";
        strCredits += "   jlGUI, jlGUI \n";
        strCredits += "   Expression Toolkit, http://expression.sourceforge.net/\n";
        strCredits += "   VRPN, http://www.cs.unc.edu/Research/vrpn/\n";
        strCredits += "   jWordNet, http://sourceforge.net/projects/jwordnet/\n";


        JTextArea textArea = new JTextArea(strCredits, 10, 30);
        textArea.setFont(new Font("Verdana", Font.PLAIN, 9));
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(Workspace.mainFrame, new JScrollPane(textArea), "Credits", JOptionPane.INFORMATION_MESSAGE);

    }

    public void listPlugins() {
        // JTextArea textArea = new JTextArea(PluginLoader.pluginsInfo.toString(), 40, 50);
        // textArea.setFont(new Font("Verdana", Font.PLAIN, 9));
        // textArea.setEditable(false);
        List<PluginData> info = PluginLoader.getPluginInfo();
        String[][] array = new String[info.size()][4];
        for (int i = 0; i < array.length; i++) {
            PluginData data = info.get(i);
            array[i][0] = data.getType();
            array[i][1] = data.getName();
            array[i][2] = data.getClassName();
            array[i][3] = data.getDescription();
        }

        Arrays.sort(array, new Comparator<String[]>() {
            public int compare(String o1[], String o2[]) {
                return o1[0].compareTo(o2[0]);
            }

            public boolean equals(Object obj) {
                return this == obj;
            }
        });
        JTable table = new JTable(array, new String[]{"Plugin Type", "Name", "Class", "Description"});
        JOptionPane.showMessageDialog(Workspace.mainFrame, new JScrollPane(table), "Plugins", JOptionPane.INFORMATION_MESSAGE);

    }

    public void openDefaultSpreadsheet() {
        try {
            String strSpreadsheetFile = SketchletContextUtils.getCurrentProjectDir() + "default.ods";
            if (!new File(strSpreadsheetFile).exists()) {
                FileUtils.copyFile(
                        new File(SketchletContextUtils.getSketchletDesignerTemplateFilesDir() + "OpenOffice.org" + File.separator + "spreadsheet.ods"),
                        new File(strSpreadsheetFile));
            }

            String strOpener = PlatformManager.getDefaultFileOpenerCommand();
            strOpener = strOpener.replace("$f", strSpreadsheetFile);

            Runtime.getRuntime().exec(strOpener);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void openDefaultUML() {
        try {
            String strSpreadsheetFile = SketchletContextUtils.getCurrentProjectDir() + "model.zargo";
            if (!new File(strSpreadsheetFile).exists()) {
                FileUtils.copyFile(
                        new File(SketchletContextUtils.getSketchletDesignerTemplateFilesDir() + "ArgoUML" + File.separator + "model.zargo"),
                        new File(strSpreadsheetFile));
            }

            String strOpener = PlatformManager.getDefaultFileOpenerCommand();
            strOpener = strOpener.replace("$f", strSpreadsheetFile);

            Runtime.getRuntime().exec(strOpener);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void reloadFiles() {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        this.tableModelFiles.fireTableDataChanged();
        this.setCursor(Cursor.getDefaultCursor());
    }

    public void changeProjectTitle() {
        String inputValue = JOptionPane.showInputDialog(this, "Project title:", this.projectTitle);

        if (inputValue != null) {
            this.projectTitle = inputValue;
        }

    }

    public void teamMembers() {
        String projectTeamDir = SketchletContextUtils.getCurrentProjectTeamDir();
        new File(projectTeamDir).mkdirs();

        String confFile = SketchletContextUtils.getSketchletDesignerConfDir() + "team";

        FileUtils.restore(projectTeamDir, confFile, null);

        Notepad.openNotepad(projectTeamDir + "members.txt", new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
            }
        });
    }

    Vector processes = new Vector();

    public void screenRecorder() {
        String command = SketchletContextUtils.getCommandFromFile("screen_recorder.txt");

        try {
            if (command == null || command.trim().equals("")) {
                JOptionPane.showMessageDialog(this, "Error starting Screen Recorder.\n Check configuration file 'screen_recorder.txt'", "Error", JOptionPane.ERROR_MESSAGE);
                return;

            }


            command = Workspace.replaceSystemVariables(command);

            String args[] = QuotedStringTokenizer.parseArgs(command);

            ProcessBuilder processBuilder = new ProcessBuilder(args);
            Process process = processBuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error starting File Manager.\n Check configuration file 'screen_recorder.txt'", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void remoteDesktop() {
        String command = SketchletContextUtils.getCommandFromFile("remote_desktop.txt");

        try {
            if (command == null || command.trim().equals("")) {
                JOptionPane.showMessageDialog(this, "Error starting Remote Desktop.\n Check the configuration file 'remote_desktop.txt'.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            command = Workspace.replaceSystemVariables(command);

            String args[] = QuotedStringTokenizer.parseArgs(command);

            ProcessBuilder processBuilder = new ProcessBuilder(args);
            Process process = processBuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error starting Remote Desktop.\n Check the configuration file 'remote_desktop.txt'.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void fileManager() {
        fileManager(null);
    }

    public void fileManager(String secondFolder) {
        String projectFilemanagerConf = SketchletContextUtils.getCurrentProjectConfDir() + "filemanager";
        new File(projectFilemanagerConf).mkdirs();

        String confFile = SketchletContextUtils.getSketchletDesignerConfDir() + "filemanager";

        FileUtils.restore(projectFilemanagerConf, confFile, null);

        String configurationFile = SketchletContextUtils.getSketchletDesignerConfDir() + "file_namager.txt";

        String command = SketchletContextUtils.getCommandFromFile("file_manager.txt");

        try {
            if (command == null || command.trim().equals("")) {
                JOptionPane.showMessageDialog(this, "Error starting File Manager.\n Check configuration file " + configurationFile, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (secondFolder != null) {
                command += " -c \"" + projectFilemanagerConf + File.separator + "preferences.xml\" \"" + secondFolder + "\"";
                command +=
                        " \"" + SketchletContextUtils.getCurrentProjectDir() + "\"";
            } else {
                command += " -c \"" + projectFilemanagerConf + File.separator + "preferences.xml\" \"" + SketchletContextUtils.getCurrentProjectDir() + "\"";
            }
// command += " \"" + projectDir + "\"";

            command = Workspace.replaceSystemVariables(command);

            String args[] = QuotedStringTokenizer.parseArgs(command);

            ProcessBuilder processBuilder = new ProcessBuilder(args);
            Process process = processBuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error starting File Manager.\n Check configuration file " + configurationFile, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void imageEditor() {
        String command = SketchletContextUtils.getCommandFromFile("image_editor.txt");
        command = command.replace("${image-files}", "");
        command = command.replace("<%=image-files%>", "");
        try {
            if (command == null || command.trim().equals("")) {
                JOptionPane.showMessageDialog(this, "Error starting Image Editor.\n Check configuration file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;

            }


            command = Workspace.replaceSystemVariables(command);

            String args[] = QuotedStringTokenizer.parseArgs(command);

            ProcessBuilder processBuilder = new ProcessBuilder(args);
            Process process = processBuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error starting Image Editor.\n Check configuration file.", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void editFileExternalWithoutFile() {
        try {
            String command = SketchletContextUtils.getCommandFromFile("script_editor.txt");

            try {
                if (command == null || command.trim().equals("")) {
                    JOptionPane.showMessageDialog(this, "Error starting External Script Editor.\n Check configuration file.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;

                }


                String args[] = QuotedStringTokenizer.parseArgs(command);

                ProcessBuilder processBuilder = new ProcessBuilder(args);
                Process process = processBuilder.start();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error starting File Manager.\n Check configuration file.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception t) {
            t.printStackTrace();
        }

    }

    public void editFileExternal() {
        if (selectedFile == null) {
            return;
        }

        try {
            String command = SketchletContextUtils.getCommandFromFile("script_editor.txt");

            try {
                if (command == null || command.trim().equals("")) {
                    JOptionPane.showMessageDialog(this, "Error starting External Script Editor.\n Check configuration file.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;

                }


                File file = new File(selectedFile);
                command += " \"" + file.getPath() + "\"";

                command = Workspace.replaceSystemVariables(command);

                String args[] = QuotedStringTokenizer.parseArgs(command);

                ProcessBuilder processBuilder = new ProcessBuilder(args);
                Process process = processBuilder.start();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error starting File Manager.\n Check configuration file.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception t) {
            log.error(t);
            t.printStackTrace();
        }

    }

    public void createNewProject(boolean createNewProcess, boolean killProcess, String title) {
        if (!SketchletEditor.close()) {
            return;
        }
        if (ProjectDialog.openDialog(Workspace.mainFrame, title)) {
            if (ProjectDialog.templateDir == null) {
                SketchletContextUtils.projectFolder = ProjectDialog.projectFolder;
                Workspace.filePath = SketchletContextUtils.getCurrentProjectFile();

                System.setProperty("user.dir", SketchletContextUtils.projectFolder);

                this.projectFolderField.setText(SketchletContextUtils.projectFolder);

                if (killProcess) {
                    Workspace.processRunner.killProcesses();
                }

                if (net.sf.sketchlet.communicator.Global.workingDirectory != null) {
                    Workspace.mainPanel.refreshData(false);
                }

                net.sf.sketchlet.communicator.Global.workingDirectory = SketchletContextUtils.getCurrentProjectDir();
                sketchletPanel.restart(SketchletContextUtils.getCurrentProjectConfDir() + "communicator/config.xml");

                saveConfiguration(false);

                Workspace.mainPanel.refreshData(false);

                Workspace.openProject(ProjectDialog.projectFolder, false);
            } else {
                FileUtils.restore(ProjectDialog.projectFolder, ProjectDialog.templateDir, null);

                SketchletContextUtils.projectFolder = ProjectDialog.projectFolder;
                Workspace.filePath = SketchletContextUtils.getCurrentProjectFile();

                System.setProperty("user.dir", SketchletContextUtils.projectFolder);

                this.projectFolderField.setText(SketchletContextUtils.projectFolder);

                Workspace.mainPanel.refreshData(false);

                net.sf.sketchlet.communicator.Global.workingDirectory = SketchletContextUtils.getCurrentProjectDir();
                sketchletPanel.restart(SketchletContextUtils.getCurrentProjectConfDir() + "communicator/config.xml");

                saveConfiguration(false);

                Workspace.mainPanel.refreshData(false);

                Workspace.openProject(ProjectDialog.projectFolder + SketchletContextUtils.sketchletDataDir(), false);
            }
            SketchletDesignerMainPanel.projectSelectorPanel.populate();
        }
    }

    // File tempDir;
    boolean tempDirCreationInProgress = false;

    public void saveProjectAsFromEditor() {
        // this.createNewProject(false, false, "Save Project As");
        if (SketchletEditor.editorPanel == null) {
            return;
        }
        GlobalProperties.save();
        SketchletEditor.editorPanel.saveAndWait();
        Timers.globalTimers.save();
        Macros.globalMacros.save();
        Workspace.mainPanel.saveConfiguration();
        this.saveCommunicator();
        this.saveConfiguration();

        final String projectTitle = Workspace.mainPanel.projectTitle;
        if (ProjectDialog.openDialog(SketchletEditor.editorFrame, Language.translate("Save Project As"), SketchletContextUtils.getCurrentProjectDirName())) {
            MessageFrame.showMessage(Workspace.mainFrame, Language.translate("Please wait..."), Workspace.mainFrame);
            try {
                tempDirCreationInProgress = true;
                java.awt.EventQueue.invokeLater(new Runnable() {

                    public void run() {
                        try {
                            FileUtils.restore(SketchletContextUtils.getCurrentProjectDir(), ProjectDialog.projectFolder);
                            SketchletEditor.close(false);

                            Workspace.openProject(ProjectDialog.projectFolder + SketchletContextUtils.sketchletDataDir(), false);
                            Workspace.mainPanel.refreshData(false);
                            SketchletDesignerMainPanel.projectSelectorPanel.populate();

                            SketchletEditorFrame.createAndShowGui(-1, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            MessageFrame.closeMessage();
                        }
                    }
                });
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public void saveConfigurationAs() {
        // this.createNewProject(false, false, "Save Project As");
        if (!SketchletEditor.close()) {
            return;
        }
        if (ProjectDialog.openDialog(Workspace.mainFrame, "Save Project As", SketchletContextUtils.getCurrentProjectDirName())) {
            FileUtils.restore(ProjectDialog.projectFolder, SketchletContextUtils.projectFolder, null);
            if (ProjectDialog.templateDir != null) {
                FileUtils.restore(ProjectDialog.projectFolder, ProjectDialog.templateDir, null);
            }

            SketchletContextUtils.projectFolder = ProjectDialog.projectFolder;
            Workspace.filePath = SketchletContextUtils.getCurrentProjectFile();

            System.setProperty("user.dir", SketchletContextUtils.projectFolder);

            this.projectFolderField.setText(SketchletContextUtils.projectFolder);

            Workspace.mainPanel.refreshData(false);

            net.sf.sketchlet.communicator.Global.workingDirectory = SketchletContextUtils.getCurrentProjectDir();
            sketchletPanel.restart(SketchletContextUtils.getCurrentProjectConfDir() + "communicator/config.xml");

            saveConfiguration(false);

            Workspace.mainPanel.refreshData(false);

            Workspace.openProject(ProjectDialog.projectFolder + SketchletContextUtils.sketchletDataDir(), false);
            SketchletDesignerMainPanel.projectSelectorPanel.populate();
        }

    }

    public void importFromURL() {
        if (!SketchletEditor.close()) {
            return;
        }
        final String strURL = JOptionPane.showInputDialog(Workspace.mainFrame, "Enter the project URL:", "Import from URL", JOptionPane.QUESTION_MESSAGE);
        if (strURL == null) {
            return;
        }

        Workspace.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        MessageFrame.showMessage(Workspace.mainFrame, "Downloading project...", Workspace.mainFrame);
        new Thread(new Runnable() {

            public void run() {
                try {
                    File tempZip = File.createTempFile("sketchlet_", "zip");
                    tempZip.deleteOnExit();
                    boolean bDown = FileUtils.copyURLToFile(new URL(strURL), tempZip);
                    if (!bDown) {
                        MessageFrame.closeMessage();
                        JOptionPane.showMessageDialog(Workspace.mainFrame, "Could not open the URL.", "Error", JOptionPane.ERROR_MESSAGE);
                        Workspace.mainFrame.setCursor(Cursor.getDefaultCursor());
                        return;
                    }

                    MessageFrame.closeMessage();

                    if (ProjectDialog.openDialog(Workspace.mainFrame, "Save Imported Project As")) {
                        Workspace.processRunner.killProcesses();
                        if (ProjectDialog.templateDir != null) {
                            FileUtils.restore(true, ProjectDialog.projectFolder, ProjectDialog.templateDir, null);
                        }
                        UnZip.unzipArchive(tempZip, new File(ProjectDialog.projectFolder));

                        SketchletContextUtils.projectFolder = ProjectDialog.projectFolder;
                        Workspace.filePath = SketchletContextUtils.getCurrentProjectFile();

                        System.setProperty("user.dir", SketchletContextUtils.projectFolder);

                        projectFolderField.setText(SketchletContextUtils.projectFolder);

                        if (net.sf.sketchlet.communicator.Global.workingDirectory != null) {
                            Workspace.mainPanel.refreshData(false);
                        }

                        net.sf.sketchlet.communicator.Global.workingDirectory = SketchletContextUtils.getCurrentProjectDir();
                        sketchletPanel.restart(SketchletContextUtils.getCurrentProjectConfDir() + "communicator/config.xml");

                        saveConfiguration(false);

                        Workspace.mainPanel.refreshData(false);

                        Workspace.openProject(ProjectDialog.projectFolder + SketchletContextUtils.sketchletDataDir(), false);
                        SketchletDesignerMainPanel.projectSelectorPanel.populate();
                    }
                } catch (Exception e) {
                    // e.printStackTrace();
                    JOptionPane.showMessageDialog(Workspace.mainFrame, "Could not open the URL '" + strURL + "'", "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    MessageFrame.closeMessage();
                }
                Workspace.mainFrame.setCursor(Cursor.getDefaultCursor());
            }
        }).start();
    }

    public void importFromZIP() {
        if (!SketchletEditor.close()) {
            return;
        }
        Workspace.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            // fc.setCurrentDirectory(new File(SketchletContextUtils.getCurrentProjectHistoryDir()).getParentFile());
            File tempZip;
            if (fc.showOpenDialog(Workspace.mainFrame) != JFileChooser.APPROVE_OPTION) {
                Workspace.mainFrame.setCursor(Cursor.getDefaultCursor());
                return;
            }
            tempZip = fc.getSelectedFile();
            String strName = tempZip.getName();
            int n = strName.indexOf(".");
            if (n > 0) {
                strName = strName.substring(0, n);
            }
            if (ProjectDialog.openDialog(Workspace.mainFrame, "Save Project As", strName)) {
                Workspace.processRunner.killProcesses();
                if (ProjectDialog.templateDir != null) {
                    FileUtils.restore(ProjectDialog.projectFolder, ProjectDialog.templateDir, null);
                }
                UnZip.unzipArchive(tempZip, new File(ProjectDialog.projectFolder));

                SketchletContextUtils.projectFolder = ProjectDialog.projectFolder;
                Workspace.filePath = SketchletContextUtils.getCurrentProjectFile();

                System.setProperty("user.dir", SketchletContextUtils.projectFolder);

                this.projectFolderField.setText(SketchletContextUtils.projectFolder);

                if (net.sf.sketchlet.communicator.Global.workingDirectory != null) {
                    Workspace.mainPanel.refreshData(false);
                }

                net.sf.sketchlet.communicator.Global.workingDirectory = SketchletContextUtils.getCurrentProjectDir();
                sketchletPanel.restart(SketchletContextUtils.getCurrentProjectConfDir() + "communicator/config.xml");

                saveConfiguration(false);

                Workspace.mainPanel.refreshData(false);
                Workspace.openProject(ProjectDialog.projectFolder + SketchletContextUtils.sketchletDataDir(), false);
                SketchletDesignerMainPanel.projectSelectorPanel.populate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showConfirmDialog(Workspace.mainFrame, "Could not open the archive.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        Workspace.mainFrame.setCursor(Cursor.getDefaultCursor());
    }

    public synchronized void saveConfiguration() {
        if (SketchletContextUtils.getCurrentProjectFile() == null) {
            return;
        } else {
            boolean isXML = SketchletContextUtils.getCurrentProjectFile().toLowerCase().endsWith(".xml");
            this.saveConfiguration(isXML);
        }
    }

    public void saveConfiguration(boolean isXML) {
        saveConfiguration(isXML, projectTitle.replace("Control Panel: ", ""));
    }

    public void saveConfiguration(boolean isXML, String strTitle) {
        try {
            new File(SketchletContextUtils.getCurrentProjectFile()).getParentFile().mkdirs();
            RecentFilesManager.addRecentFile(SketchletContextUtils.getCurrentProjectFile());
            PrintWriter out = new PrintWriter(new FileWriter(SketchletContextUtils.getCurrentProjectFile()));

            if (isXML) {
                out.println("<?xml version='1.0' encoding='UTF-8'?>");
                out.println("<process-runner title='" + strTitle + "'>");
            } else {
                out.println("Title " + strTitle);
                out.println();
            }

            Iterator iterator = processRunner.processHandlers.iterator();

            while (iterator.hasNext()) {
                ProcessConsolePanel processPanel = (ProcessConsolePanel) iterator.next();
                if (isXML) {
                    out.println("    " + processPanel.getXmlString());
                } else {
                    out.println(processPanel.getTxtString());
                }
            }

            if (isXML) {
                out.println("</process-runner>");
            }

            out.flush();
            out.close();

            // saveToHistory();

            saveCommunicator();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveCommunicator() {
        net.sf.sketchlet.communicator.ConfigurationData.saveConfiguration(SketchletContextUtils.getCurrentProjectConfDir() + "communicator/config.xml", SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/init-variables.xml");
    }

    public void exportProcesses(File file, boolean isXML) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(file));

            if (isXML) {
                out.println("<?xml version='1.0' encoding='UTF-8'?>");
                out.println("<process-runner>");
            }

            int indexes[] = this.tableModules.getSelectedRows();

            for (int i = 0; i
                    < indexes.length; i++) {
                ProcessConsolePanel processPanel = (ProcessConsolePanel) processRunner.processHandlers.elementAt(indexes[i]);
                if (isXML) {
                    out.println("    " + processPanel.getXmlString());
                } else {
                    out.println(processPanel.getTxtString());
                }

            }

            if (isXML) {
                out.println("</process-runner>");
            }

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveToHistory() {
        // Calendar date = Calendar.getInstance();
        // String historyFileName = "Version " + date.getVariableValue(Calendar.YEAR) + "_" + (date.getVariableValue(Calendar.MONTH) < Calendar.OCTOBER ? "0" : "") + (date.getVariableValue(Calendar.MONTH) + 1) + "_" + (date.getVariableValue(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + date.getVariableValue(Calendar.DAY_OF_MONTH) + " at " + (date.getVariableValue(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + date.getVariableValue(Calendar.HOUR_OF_DAY) + "." + (date.getVariableValue(Calendar.MINUTE) < 10 ? "0" : "") + date.getVariableValue(Calendar.MINUTE) + "." + (date.getVariableValue(Calendar.SECOND) < 10 ? "0" : "") + date.getVariableValue(Calendar.SECOND);
        String historyFileName = Workspace.mainPanel.projectTitle.replace(" ", "_").toLowerCase();
        ZipVersion.createAndShowGUI(historyFileName);

        // new File(historyDir).mkdirs();
        // saveToHistory( historyDir, root, null );
    }

    public void saveToHistory(String historyDir, String directory, String subdirectory) {
        if (subdirectory != null) {
            new File(historyDir + File.separator + subdirectory).mkdirs();
        }

        File files[] = new File(directory).listFiles();

        for (int i = 0; i
                < files.length; i++) {
            File file = files[i];
            String filename = file.getName();

            if (file.isDirectory()) {
                if (!filename.equalsIgnoreCase("history") && !filename.equalsIgnoreCase("notebook") && !filename.equalsIgnoreCase("index")) {
                    String dir = subdirectory == null ? filename : subdirectory + File.separator + filename;
                    saveToHistory(historyDir, directory + File.separator + filename, dir);
                }

            } else {
                if (subdirectory != null) {
                    FileUtils.copyFile(file, new File(historyDir + File.separator + subdirectory + File.separator + filename));
                } else {
                    FileUtils.copyFile(file, new File(historyDir + File.separator + filename));
                }

            }
        }
    }

    public void saveConfigurationAsJNPL(String filePath) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(filePath));

            out.println("<?xml version='1.0' encoding='UTF-8'?>");
            // out.println("<process-runner title='" + confTitle.getText() + "' show-gui='true'>");

            out.println("<jnlp spec='1.0+' codebase='http://amico.sourceforge.net/jws/bin'>");
            out.println("<information>");
            out.println("  <title>" + projectTitle + "</title>");
            out.println("  <vendor>AMICO</vendor>");
            out.println("  <homepage href='http://amico.sourceforge.net/' />");
            out.println("  <description>AMICO Web Start</description>");
            out.println("</information>");

            out.println("<offline-allowed/>");
            out.println("<security>");
            out.println("    <j2ee-application-client-permissions/>");
            out.println("</security>");
            out.println("<resources>");
            out.println("  <j2se version='1.2+' />");
            out.println("  <jar href='process-runner.jar'/>");
            out.println("</resources>");
            out.println("<application-desc main-class='amico.workspace.ProcessRunner'>");
            out.println("  <argument>http://amico.sourceforge.net/jws/examples/" + SketchletContextUtils.getCurrentProjectFile() + "</argument>");
            out.println("</application-desc>");
            out.println("</jnlp>");

            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Yarked from JMenu, ideally this would be public.
    protected PropertyChangeListener createActionChangeListener(JMenuItem b) {
        return new ActionChangedListener(b);
    }

    // Yarked from JMenu, ideally this would be public.
    private class ActionChangedListener
            implements PropertyChangeListener {

        JMenuItem menuItem;

        ActionChangedListener(JMenuItem mi) {
            super();
            this.menuItem = mi;
        }

        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (e.getPropertyName().equals(Action.NAME)) {
                String text = (String) e.getNewValue();
                menuItem.setText(text);
            } else if (propertyName.equals("enabled")) {
                Boolean enabledState = (Boolean) e.getNewValue();
                menuItem.setEnabled(enabledState.booleanValue());
            }
        }
    }

    public void refreshData(boolean bAppend) {
        Workspace.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new File(SketchletContextUtils.getCurrentProjectConfDir()).mkdirs();
        // new File(SketchletContextUtils.getCurrentProjectHistoryDir()).mkdirs();
        new File(SketchletContextUtils.getCurrentProjectNotebookDir()).mkdirs();

        this.tableModelModules.fireTableDataChanged();
        int row = processRunner.consolePane.tabbedPane.getSelectedIndex();

        if (row >= 0) {
            this.tableModules.getSelectionModel().setSelectionInterval(row, row);
        }

        tableModelFiles.fireTableDataChanged();

        if (!bAppend) {
            /*
             * if (Workspace.sketchbookOrganiser != null &&
             * Workspace.sketchbookOrganiserFrame != null) {
             * Workspace.sketchbookOrganiserFrame.setVisible(false);
             * Workspace.sketchbookOrganiser = null;
             * Workspace.sketchbookOrganiserFrame = null; }
             */

            if (SketchletEditor.editorFrame != null) {
                if (!SketchletEditor.close()) {
                    return;
                }
            }

            if (ScreenScripts.screenScriptsPanel != null) {
                ScreenScripts.screenScriptsPanel.close();
            }

            MacrosFrame.hideMacros();
            // TimersFrame.hideTimers();
            InteractionSpaceFrame.closeFrame();
            PluginsFrame.hideFrame();

            // tableModelSketches.refreshData();

            XMLHelper.load("system_variables.xml", "system_variables", SystemVariablesDialog.data);
            SystemVariablesDialog.startThread();

            if (SketchletEditor.pages != null) {
                SketchletEditor.pages.dispose();
                new Thread(new Runnable() {

                    public void run() {
                        try {
                            /*
                             * System.gc(); Thread.sleep(5000); System.gc();
                             */
                        } catch (Exception e) {
                        }
                    }
                }).start();
            }
            SketchletEditor.pages = new Pages();
            SketchletDesignerMainPanel.desktopPanel.refresh();
            SketchletDesignerMainPanel.desktopPanelAuto.refresh();
        }
        Workspace.mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    class ProcessTableModel
            extends AbstractTableModel {

        public String getColumnName(int col) {
            return columnNames[col].toString();
        }

        public int getRowCount() {
            return processRunner.processHandlers.size();
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public Object getValueAt(int row, int col) {
            ProcessConsolePanel processPanel = (ProcessConsolePanel) processRunner.processHandlers.get(row);

            switch (col) {
                case 0:
                    return processPanel.titleField.getText();
                case 1:
                    return processPanel.status;
                case 2:
                    return processPanel.descriptionField.getText();
                default:
                    return "";
            }
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public void setValueAt(Object value, int row, int col) {
            // rowData[row][col] = value;
            // fireTableCellUpdated(row, col);
        }
    }

    class FileTableModel extends AbstractTableModel {

        public String getColumnName(int col) {
            return columnNamesFiles[col].toString();
        }

        public int getRowCount() {
            if (SketchletContextUtils.getCurrentProjectDir() == null) {
                return 0;
            } else {
                return new File(SketchletContextUtils.getCurrentProjectDir()).list().length;
            }
        }

        public int getColumnCount() {
            return columnNamesFiles.length;
        }

        public Object getValueAt(int row, int col) {
            File files[] = getFiles();
            File file = files[row];

            switch (col) {
                case 0:
                    if (file.isDirectory()) {
                        return "[" + file.getName() + "]";
                    } else {
                        return file.getName();
                    }
                case 1:
                    if (file.isDirectory()) {
                        return "<DIR>";
                    } else {
                        long size = file.length();

                        String strSize;

                        if (size < 1000) {
                            strSize = size + " bytes";
                        } else {
                            strSize = size / 1000 + " kB";
                        }

                        return strSize;
                    }
                case 2:
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(file.lastModified());
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    int month = cal.get(Calendar.MONTH);
                    String strDate = ((day < 10) ? "0" : "") + day + "/" + ((month < 10) ? "0" : "") + month + "/" + cal.get(Calendar.YEAR);
                    return strDate;
            }

            return "";
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public void setValueAt(Object value, int row, int col) {
            // rowData[row][col] = value;
            // fireTableCellUpdated(row, col);
        }
    }

    class SketchesTableModel extends AbstractTableModel {

        Object data[][];

        public void refreshData() {
            // data = Sketches.getSketchInfoFromDir();
            this.fireTableDataChanged();
        }

        public String getColumnName(int col) {
            return columnNamesSketches[col].toString();
        }

        public int getRowCount() {
            if (data == null) {
                return 0;
            } else {
                return data.length;
            }
        }

        public int getColumnCount() {
            return columnNamesSketches.length;
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public void setValueAt(Object value, int row, int col) {
        }
    }

    private class RowListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }
            int row = tableModules.getSelectedRow();

            if (row >= 0) {
                Workspace.consolePane.tabbedPane.setSelectedIndex(row);
            }

            enableMenuItems();
            enableToolbarItems();
        }
    }

    private class SketchRowListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting()) {
                return;
            }
            // sketchRow = tableSketches.getSelectedRow();

            enableMenuItems();
            enableToolbarItems();
        }
    }

    String selectedFile;
    String selectedFileShortName;

    private class RowListenerFiles implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent event) {
            if (event.getValueIsAdjusting() || SketchletContextUtils.getCurrentProjectDir() == null) {
                return;
            }
            int row = tableFiles.getSelectedRow();

            if (row >= 0) {
                selectedFile = getFiles()[row].getPath();
                selectedFileShortName = getFiles()[row].getName();
            } else {
                selectedFile = null;
                selectedFileShortName = null;
            }

            enableMenuItems();
            enableToolbarItems();
        }
    }

    public File[] getFiles() {
        File files[] = new File(SketchletContextUtils.getCurrentProjectDir()).listFiles();

        Arrays.sort(files, new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1 instanceof File && o2 instanceof File) {
                    File f1 = (File) o1;
                    File f2 = (File) o2;
                    if (f1.isDirectory() && !f2.isDirectory()) {
                        return -1;
                    } else if (!f1.isDirectory() && f2.isDirectory()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
                return 0;
            }

            public boolean equals(Object o) {
                return this == o;
            }
        });

        return files;
    }

    /**
     * Create the toolbar. By default this reads the resource file for the
     * definition of the toolbar.
     */
    private Component createToolbar() {
        toolbar = new JToolBar();
        // toolbar.setOrientation( JToolBar.VERTICAL );
        String[] toolKeys = tokenize(getResourceString("toolbar"));
        for (int i = 0; i < toolKeys.length; i++) {
            if (toolKeys[i].equals("-")) {
                toolbar.add(Box.createHorizontalStrut(5));
            } else {
                if (isActive(toolKeys[i])) {
                    toolbar.add(createTool(toolKeys[i], new Insets(2, 2, 2, 2)));
                }
            }

        }
        toolbar.add(Box.createHorizontalGlue());
        return toolbar;
    }

    /**
     * Create the toolbar. By default this reads the resource file for the
     * definition of the toolbar.
     */
    private JToolBar createToolbar(String name) {
        JToolBar toolbar = new JToolBar();
        return createToolbar(toolbar, name);
    }

    public JToolBar createToolbar(JToolBar toolbar, String name) {
        toolbar.removeAll();
        toolbar.setFloatable(false);
        String[] toolKeys = tokenize(getResourceString(name));
        for (int i = 0; i < toolKeys.length; i++) {
            if (toolKeys[i].equals("-")) {
                toolbar.add(Box.createHorizontalStrut(5));
            } else {
                if (isActive(toolKeys[i])) {
                    toolbar.add(createTool(toolKeys[i], new Insets(0, 0, 0, 0)));
                }
            }

        }
        toolbar.add(Box.createHorizontalGlue());
        return toolbar;
    }

    /**
     * Hook through which every toolbar item is created.
     */
    protected Component createTool(String key) {
        return createToolbarButton(key);
    }

    /**
     * Hook through which every toolbar item is created.
     */
    protected Component createTool(String key, Insets insets) {
        return createToolbarButton(key, insets);
    }

    /**
     * Create a button to go inside of the toolbar. By default this will load an
     * currentSketch.images resource. The currentSketch.images filename is
     * relative to the classpath (including the '.' directory if its a part of
     * the classpath), and may either be in a JAR file or a separate file.
     *
     * @param key The key in the resource file to serve as the basis of lookups.
     */
    protected JButton createToolbarButton(String key) {
        URL url = getResource(key + imageSuffix);
        JButton b = new JButton(
                new ImageIcon(url)) {

            public float getAlignmentY() {
                return 0.5f;
            }
        };
        b.setRequestFocusEnabled(false);
        b.setMargin(new Insets(1, 1, 1, 1));

        String astr = getResourceString(key + actionSuffix);
        if (astr == null) {
            astr = key;
        }

        b.setActionCommand(astr);
        b.addActionListener(this);

        toolbarItems.put(key, b);

        String tip = Language.translate(getResourceString(key + tipSuffix));
        if (tip != null) {
            b.setToolTipText(tip);
        }

        return b;
    }

    protected JButton createToolbarButton(String key, Insets insets) {
        URL url = getResource(key + imageSuffix);
        JButton b = new JButton(new ImageIcon(url)) {

            public float getAlignmentY() {
                return 0.5f;
            }
        };
        b.setRequestFocusEnabled(false);
        b.setMargin(insets);

        String astr = getResourceString(key + actionSuffix);
        if (astr == null) {
            astr = key;
        }

        b.setActionCommand(astr);
        b.addActionListener(this);

        toolbarItems.put(key, b);

        String tip = Language.translate(getResourceString(key + tipSuffix));
        if (tip != null) {
            b.setToolTipText(tip);
        }
        String text = Language.translate(getResourceString(key + textSuffix));
        if (text != null) {
            b.setText(text);
        }

        return b;
    }

    public void enableMenuItems() {
        if (SketchletContextUtils.getCurrentProjectFile() == null) {
            setEnabledAll(false);
        } else {
            setEnabledAll(true);

            String selectionItems[] = {"start", "stop", "restart", "removeprocess", "exportprocess"};

            boolean enable = this.tableModules.getSelectedRow() >= 0;

            for (int i = 0; i < selectionItems.length; i++) {
                JMenuItem menuItem = (JMenuItem) this.menuItems.get(selectionItems[i]);

                if (menuItem != null) {
                    menuItem.setEnabled(enable);
                }

            }

            int row = this.tableModules.getSelectedRow();
            if (row >= 0 && Workspace.consolePane.tabbedPane.getTabCount() > row) {
                boolean stopped = ((ProcessConsolePanel) Workspace.consolePane.tabbedPane.getComponentAt(row)).stop.isEnabled();
                JMenuItem menuItem = (JMenuItem) this.menuItems.get("start");

                if (menuItem != null) {
                    menuItem.setEnabled(!stopped);
                }

                menuItem = (JMenuItem) this.menuItems.get("stop");

                if (menuItem != null) {
                    menuItem.setEnabled(stopped);
                }

            }
        }
    }

    public void setEnabledAll(boolean enable) {
        Enumeration elements = this.menuItems.elements();

        while (elements.hasMoreElements()) {
            JMenuItem menuItem = (JMenuItem) elements.nextElement();
            if (!menuItem.getActionCommand().equals("new")
                    && !menuItem.getActionCommand().equals("importurl")
                    && !menuItem.getActionCommand().equals("importzip")
                    && !menuItem.getActionCommand().equals("open")
                    && !menuItem.getActionCommand().equals("helpusing")
                    && !menuItem.getActionCommand().equals("projectsroot")
                    && !menuItem.getActionCommand().equals("exit")
                    && !menuItem.getActionCommand().equals("about")) {
                menuItem.setEnabled(enable);
            }

        }
    }

    public void setEnabledAllToolbar(boolean enable) {
        Enumeration elements = this.toolbarItems.elements();

        while (elements.hasMoreElements()) {
            JButton menuItem = (JButton) elements.nextElement();
            if (!menuItem.getActionCommand().equals("new")
                    && !menuItem.getActionCommand().equals("importurl")
                    && !menuItem.getActionCommand().equals("importzip")
                    && !menuItem.getActionCommand().equals("open")
                    && !menuItem.getActionCommand().equals("helpusing")
                    && !menuItem.getActionCommand().equals("projectsroot")
                    && !menuItem.getActionCommand().equals("exit")
                    && !menuItem.getActionCommand().equals("about")) {
                menuItem.setEnabled(enable);
            }

        }
    }

    public void enableToolbarItems() {
        if (SketchletContextUtils.getCurrentProjectFile() == null) {
            setEnabledAllToolbar(false);
        } else {
            setEnabledAllToolbar(true);

            JButton shellBtn = (JButton) this.toolbarItems.get("shellexecute");
            JButton browserBtn = (JButton) this.toolbarItems.get("openinwebbrowser");
            JButton editorBtn = (JButton) this.toolbarItems.get("openinexternaleditor");

            if (shellBtn != null) {
                shellBtn.setEnabled(tableFiles.getSelectedRow() >= 0);
            }

            if (browserBtn != null) {
                browserBtn.setEnabled(tableFiles.getSelectedRow() >= 0);
            }

            if (editorBtn != null) {
                editorBtn.setEnabled(tableFiles.getSelectedRow() >= 0);
            }

            enableModuleToolbar();
            enableAmicoPanel();

        }


    }

    public void enableAmicoPanel() {
        this.sketchletPanel.globalVariablesPanel.enableControls();
        if (this.sketchletPanel.panel2 != null) {
            this.sketchletPanel.panel2.enableControls();
        }
    }

    public void enableModuleToolbar() {
        String selectionItems[] = {"start", "stop", "restart", "removeprocess", "exportprocess"};

        boolean enable = this.tableModules.getSelectedRow() >= 0;

        for (int i = 0; i
                < selectionItems.length; i++) {
            JButton toolbarItem = (JButton) this.toolbarItems.get(selectionItems[i]);

            if (toolbarItem != null) {
                toolbarItem.setEnabled(enable);
            }

        }

        int row = this.tableModules.getSelectedRow();
        if (row >= 0 && Workspace.consolePane.tabbedPane.getTabCount() > 0) {
            boolean stopped = ((ProcessConsolePanel) Workspace.consolePane.tabbedPane.getComponentAt(row)).stop.isEnabled();
            JButton toolbarItem = (JButton) this.toolbarItems.get("start");

            if (toolbarItem != null) {
                toolbarItem.setEnabled(!stopped);
            }

            toolbarItem = (JButton) this.toolbarItems.get("stop");

            if (toolbarItem != null) {
                toolbarItem.setEnabled(stopped);
            }

        }
    }

    JPopupMenu popupMenuModules = new JPopupMenu();
    JPopupMenu popupMenuFiles = new JPopupMenu();
    JPopupMenu popupMenuSketches = new JPopupMenu();

    class PopupListenerModules extends MouseAdapter {

        JMenuItem menuItemStart;
        JMenuItem menuItemStop;

        public PopupListenerModules() {
            menuItemStart = new JMenuItem(Language.translate("Start"));
            menuItemStart.setActionCommand("start-process");
            menuItemStart.addActionListener(thisPanel);
            popupMenuModules.add(menuItemStart);

            menuItemStop = new JMenuItem(Language.translate("Stop"));
            menuItemStop.setActionCommand("stop-process");
            menuItemStop.addActionListener(thisPanel);
            popupMenuModules.add(menuItemStop);

            JMenuItem menuItem = new JMenuItem(Language.translate("Restart"));
            menuItem.setActionCommand("restart-process");
            menuItem.addActionListener(thisPanel);
            popupMenuModules.add(menuItem);

            popupMenuModules.addSeparator();

            menuItem = new JMenuItem(Language.translate("Edit..."));
            menuItem.setActionCommand("view-details");
            menuItem.addActionListener(thisPanel);
            popupMenuModules.add(menuItem);

            popupMenuModules.addSeparator();

            menuItem = new JMenuItem(Language.translate("Remove"));
            menuItem.setActionCommand("remove-process");
            menuItem.addActionListener(thisPanel);
            popupMenuModules.add(menuItem);
            TutorialPanel.prepare(popupMenuModules, true);

        }

        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        private void showPopup(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                int row = tableModules.rowAtPoint(e.getPoint());
                tableModules.getSelectionModel().setSelectionInterval(row, row);

                if (row >= 0) {
                    boolean stopped = ((ProcessConsolePanel) Workspace.consolePane.tabbedPane.getComponentAt(row)).stop.isEnabled();
                    menuItemStart.setEnabled(!stopped);
                    menuItemStop.setEnabled(stopped);
                }
                popupMenuModules.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class PopupListenerFiles extends MouseAdapter {

        JMenuItem menuItemOpen;
        JMenuItem menuItemOpenInWebBrowser;
        JMenuItem menuItemOpenInExternalEditor;

        public PopupListenerFiles() {
            menuItemOpen = new JMenuItem(Language.translate("Open"));
            menuItemOpen.setActionCommand("shellexecute");
            menuItemOpen.addActionListener(thisPanel);
            menuItemOpenInWebBrowser = new JMenuItem(Language.translate("Open in Web Browser"));
            menuItemOpenInWebBrowser.setActionCommand("openinwebbrowser");
            menuItemOpenInWebBrowser.addActionListener(thisPanel);
            menuItemOpenInExternalEditor = new JMenuItem(Language.translate("Open in Text Editor"));
            menuItemOpenInExternalEditor.setActionCommand("openinexternaleditor");
            menuItemOpenInExternalEditor.addActionListener(thisPanel);

            popupMenuFiles.add(menuItemOpen);
            popupMenuFiles.addSeparator();
            popupMenuFiles.add(menuItemOpenInWebBrowser);
            popupMenuFiles.add(menuItemOpenInExternalEditor);
            TutorialPanel.prepare(popupMenuFiles, true);
        }

        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                openFile();
            }
        }

        private void showPopup(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                int row = tableFiles.rowAtPoint(e.getPoint());
                tableFiles.getSelectionModel().setSelectionInterval(row, row);

                popupMenuFiles.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    class PopupListenerSketches extends MouseAdapter {

        JMenuItem menuItemOpenSketches;
        JMenuItem menuItemPlaySketches;

        public PopupListenerSketches() {
            menuItemOpenSketches = new JMenuItem(Language.translate("Open Sketches..."));
            menuItemOpenSketches.setActionCommand("sketches");
            menuItemOpenSketches.addActionListener(thisPanel);

            menuItemPlaySketches = new JMenuItem(Language.translate("Play Sketches..."));
            menuItemPlaySketches.setActionCommand("playsketches");
            menuItemPlaySketches.addActionListener(thisPanel);

            popupMenuSketches.add(menuItemOpenSketches);
            popupMenuSketches.add(menuItemPlaySketches);
            TutorialPanel.prepare(popupMenuSketches, true);
        }

        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                openSketches(false);
            }
        }

        private void showPopup(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                // int row = tableSketches.rowAtPoint(e.getPoint());
                // tableSketches.getSelectionModel().setSelectionInterval(row, row);

                popupMenuSketches.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
