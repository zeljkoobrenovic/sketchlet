package net.sf.sketchlet.designer.editor.ui.script;

import net.sf.sketchlet.common.QuotedStringTokenizer;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.framework.blackboard.ConfigurationData;
import net.sf.sketchlet.net.NetUtils;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.ui.TextTransfer;
import net.sf.sketchlet.designer.editor.ui.variables.VariablesPanel;
import net.sf.sketchlet.help.HelpUtils;
import net.sf.sketchlet.loaders.pluginloader.PluginLoader;
import net.sf.sketchlet.script.ScriptConsole;
import net.sf.sketchlet.script.ScriptOperations;
import net.sf.sketchlet.script.ScriptPluginProxy;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * @author Omnibook
 */
public class ScriptsTablePanel extends JPanel {
    private static final Logger log = Logger.getLogger(ScriptsTablePanel.class);
    public int selectedRow = -1;
    public TextTransfer clipboard = new TextTransfer();
    public static ScriptsTablePanel parentPanel;
    public JTable table;
    public ScriptsTableModel scriptsTableModel;
    public JButton reloadAllButton;
    public JButton showConsoleButton;
    JToolBar toolbar;
    JToolBar toolbarConsole;
    VariablesPanel mainFrame;
    public static ScriptOperations operations;

    public ScriptsTablePanel(JToolBar toolbar, JToolBar toolbarConsole, VariablesPanel mainFrame) {
        super(new BorderLayout());
        parentPanel = this;
        this.toolbar = toolbar;
        this.toolbarConsole = toolbarConsole;
        this.mainFrame = mainFrame;

        scriptsTableModel = new ScriptsTableModel(this);

        table = new JTable(scriptsTableModel);
        // table.setTableHeader(null);
        table.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(table);

        // table.setRowHeight(30);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.addMouseListener(new PopupListener());
        // table.setPreferredScrollableViewportSize(new Dimension(500, 400));

        JPanel commandsPanel = new JPanel();
        commandsPanel.setLayout(new FlowLayout());
        JButton loadButton = new JButton("Load...");
        reloadAllButton = new JButton("Reload All");
        reloadAllButton.setEnabled(VariablesBlackboard.getScriptFiles().size() > 0);
        final JButton removeButton = new JButton("Remove");

        removeButton.setEnabled(false);

        showConsoleButton = new JButton("Show Console");


        final JButton newButton = new JButton("New...");
        final JButton editButton = new JButton("Edit...");
        final JButton editButtonExternal = new JButton("Edit in External Editor...");

        // commandsPanel.add( loadButton );
        commandsPanel.add(newButton);
        commandsPanel.add(editButton);
        commandsPanel.add(editButtonExternal);
        commandsPanel.add(new JLabel("    "));
        commandsPanel.add(reloadAllButton);
        commandsPanel.add(removeButton);
        commandsPanel.add(new JLabel("    "));
        commandsPanel.add(showConsoleButton);

        loadButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                final JFileChooser fc = new JFileChooser();
                fc.setApproveButtonText("Load Transformation");
                fc.setDialogTitle("Select AMICO XSLT Transformation");
                fc.setCurrentDirectory(new File(SketchletContextUtils.sketchletDataDir() + "/conf/communicator"));
                //In response to a button click:
                int returnVal = fc.showOpenDialog(parentPanel);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();

                    try {
                        VariablesBlackboard.addScript(file.getPath());
                        scriptsTableModel.fireTableDataChanged();
                    } catch (Exception e) {
                        e.printStackTrace(System.out);
                    }
                }

                reloadAllButton.setEnabled(VariablesBlackboard.getScriptFiles().size() > 0);
            }
        });

        reloadAllButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                reloadAll();
            }
        });


        showConsoleButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                showConsole();
            }
        });

        removeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                removeScript();
            }
        });

        newButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                createNewScript();
            }
        });

        editButton.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent event) {
                        editScript();
                    }
                });

        editButtonExternal.setEnabled(false);

        editButtonExternal.addActionListener(
                new ActionListener() {

                    public void actionPerformed(ActionEvent event) {
                        editScriptExternal();
                    }
                });

        editButtonExternal.setEnabled(false);

        //Ask to be notified of selection changes.
        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }

                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                selectedRow = lsm.getMinSelectionIndex();

                enableControls();
            }
        });

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        JPanel consolePanel = new JPanel(new BorderLayout());
        consolePanel.add(new JScrollPane(ScriptConsole.getConsole().getTextArea()), BorderLayout.CENTER);
        consolePanel.add(toolbarConsole, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane, consolePanel);
        splitPane.setResizeWeight(0.5);
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        //Add the scroll pane to this panel.
        add(splitPane, BorderLayout.CENTER);

        if (toolbar != null) {
            final JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png", ""));
            toolbar.setBorder(BorderFactory.createEmptyBorder());
            help.setToolTipText("What are Scripts?");
            help.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    HelpUtils.openHelpFile("Scripts", "scripts");
                }
            });

            JToolBar toolbarHelp = new JToolBar();
            toolbarHelp.setFloatable(false);
            toolbarHelp.add(help);
            JPanel panelNorth = new JPanel(new BorderLayout());
            panelNorth.add(toolbar);
            panelNorth.add(toolbarHelp, BorderLayout.EAST);
            add(panelNorth, BorderLayout.NORTH);

            add(panelNorth, BorderLayout.NORTH);
        } else {
            add(commandsPanel, BorderLayout.SOUTH);
        }

        enableControls();
    }

    static JFileChooser fc = new JFileChooser();

    public void importScript() {
        fc.setDialogTitle("Import Script");

        fc.setCurrentDirectory(new File(SketchletContextUtils.getSketchletDesignerScriptTemplatesDir()));

        int returnVal = fc.showSaveDialog(parentPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            createNewScript(file.getName(), file, "*", true);
        }
    }

    public void createNewScript() {
        ScriptDialog dlg = new ScriptDialog(this, null, false);
    }

    public File createNewScript(String fileName, File templateFile, String strTriggers, boolean bReload) {
        File file;
        if (NetUtils.getWorkingDirectory() == null) {
            file = new File(SketchletContextUtils.sketchletDataDir() + "/scripts/" + fileName);
        } else {
            File dir = new File(NetUtils.getWorkingDirectory() + SketchletContextUtils.sketchletDataDir() + "/scripts");
            dir.mkdirs();
            file = new File(NetUtils.getWorkingDirectory() + SketchletContextUtils.sketchletDataDir() + "/scripts/" + fileName);
        }

        if (file.exists()) {
            int response = JOptionPane.showConfirmDialog(null,
                    "Overwrite existing file?", "Confirm Overwrite",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.CANCEL_OPTION) {
                return null;
            }
        }

        if (templateFile.exists()) {
            FileUtils.copyFile(templateFile, file);
        } else {
            FileUtils.saveFileText(file.getPath(), "");
        }
        FileUtils.saveFileText(file.getPath() + ".triggers", strTriggers);

        if (bReload) {
            reloadAll();
        }

        enableControls();

        return file;

    }

    public void renameScript(String newName) {
        File file = new File((String) VariablesBlackboard.getInstance().getScriptFiles().get(selectedRow));

        String strOldName = file.getName();

        if (strOldName.equals(newName) || newName.equals("")) {
            return;
        }

        if (createNewScript(newName, file, FileUtils.getFileText(file.getPath() + ".triggers"), false) != null) {
            removeScript();
        }
    }

    public void editScript() {
        try {
            ScriptPluginProxy script = VariablesBlackboard.getScripts().get(selectedRow);
            editScript(script.getScriptFile());
        } catch (Exception t) {
            log.error("uncaught exception", t);
        }
    }

    public static void editScript(File file) {
        try {
            if (operations != null) {
                operations.openScript(file);
            }

        } catch (Exception t) {
            log.error("uncaught exception", t);
        }
    }

    public void editScriptExternal() {
        editScriptExternal(new File((String) VariablesBlackboard.getInstance().getScriptFiles().get(selectedRow)));
    }

    public static void editScriptExternal(File file) {
        try {
            String command = SketchletContextUtils.getCommandFromFile("script_editor.txt");

            try {
                if (command == null || command.trim().equals("")) {
                    JOptionPane.showMessageDialog(null, "Error starting External Script Editor.\n Check configuration file.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                command += " \"" + file.getPath() + "\"";
                command = SketchletContextUtils.replaceSystemVariables(command);

                String args[] = QuotedStringTokenizer.parseArgs(command);

                ProcessBuilder processBuilder = new ProcessBuilder(args);
                Process process = processBuilder.start();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error starting File Manager.\n Check configuration file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception t) {
            log.error("uncaught exception", t);
        }
    }

    public void showConsole() {
        ScriptConsole.showConsole();
    }

    public void reloadAll() {
        try {
            ScriptConsole.getConsole().getTextArea().setText("");

            ConfigurationData.getScriptFiles().clear();

            File files[] = new File(NetUtils.getWorkingDirectory() + SketchletContextUtils.sketchletDataDir() + "/scripts").listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isDirectory() && !files[i].getPath().endsWith(".triggers")) {
                        ConfigurationData.addScriptFile(files[i].getPath());
                    }

                }
            }

            VariablesBlackboard.createScripts();
            //VariablesBlackboard.initScripts();

            VariablesBlackboard.getInstance().updateVariable("amico-transformations", "", "", "");
            VariablesBlackboard.getInstance().removeVariable("amico-transformations");

            if (operations != null) {
                operations.reloadAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        scriptsTableModel.fireTableDataChanged();
    }

    public void startScript() {
        new Thread(new Runnable() {

            public void run() {
                try {
                    File scriptFile = new File((String) VariablesBlackboard.getInstance().getScriptFiles().get(selectedRow));

                    if (VariablesBlackboard.getScripts() != null) {
                        if (selectedRow >= 0 && selectedRow < VariablesBlackboard.getInstance().getScripts().size()) {
                            ScriptPluginProxy script = VariablesBlackboard.getInstance().getScripts().get(selectedRow);
                            if (script != null) {
                                ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
                                Thread.currentThread().setContextClassLoader(PluginLoader.getClassLoader());

                                script = VariablesBlackboard.createScript(selectedRow, scriptFile);
                                if (script != null) {
                                    script.start();
                                }

                                Thread.currentThread().setContextClassLoader(oldCL);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                scriptsTableModel.fireTableDataChanged();
            }
        }).start();
    }

    public void stopScript() {
        try {
            File scriptFile = new File((String) VariablesBlackboard.getInstance().getScriptFiles().get(selectedRow));

            if (VariablesBlackboard.getScripts() != null) {
                ScriptPluginProxy script = VariablesBlackboard.getInstance().getScripts().get(selectedRow);
                script.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        scriptsTableModel.fireTableDataChanged();
    }

    public void removeScript() {
        removeScript(selectedRow);
    }

    public void removeScript(int row) {
        try {
            File scriptFile = new File((String) VariablesBlackboard.getInstance().getScriptFiles().get(row));
            File triggerFile = new File(((String) VariablesBlackboard.getInstance().getScriptFiles().get(row)) + ".triggers");

            VariablesBlackboard.getScripts().clear();
            VariablesBlackboard.getScriptFiles().clear();
            System.gc();

            scriptFile.delete();
            triggerFile.delete();

            reloadAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        enableControls();
    }

    public void enableControls() {
        mainFrame.enableMenuAndToolbar(new String[]{"editscript", "editscriptexternal", "removescript", "startscript", "stopscript"}, selectedRow >= 0);
        mainFrame.enableMenuAndToolbar(new String[]{"reloadallscripts"}, VariablesBlackboard.getScriptFiles().size() > 0);
    }

    JPopupMenu popupMenu = new JPopupMenu();

    class PopupListener extends MouseAdapter {

        JMenuItem menuItemOpen;
        JMenuItem menuItemOpenExternal;
        JMenuItem menuItemRemove;
        JMenuItem menuItemStart;
        JMenuItem menuItemStop;

        public PopupListener() {
            menuItemOpen = new JMenuItem("Edit...");
            menuItemOpen.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    editScript();
                }
            });
            menuItemOpenExternal = new JMenuItem("Edit in External Editor...");
            menuItemOpenExternal.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    editScriptExternal();
                }
            });
            menuItemRemove = new JMenuItem("Remove");
            menuItemRemove.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    removeScript();
                }
            });
            menuItemStart = new JMenuItem("Start");
            menuItemStart.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    startScript();
                }
            });
            menuItemStop = new JMenuItem("Stop");
            menuItemStop.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    stopScript();
                }
            });

            popupMenu.add(menuItemStart);
            popupMenu.add(menuItemStop);
            //popupMenu.addSeparator();
            //popupMenu.add(menuItemVariables);
            popupMenu.addSeparator();
            popupMenu.add(menuItemOpen);
            popupMenu.add(menuItemOpenExternal);
            popupMenu.add(menuItemRemove);
        }

        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                editScript();
            }
        }

        private void showPopup(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                int row = table.rowAtPoint(e.getPoint());
                table.getSelectionModel().setSelectionInterval(row, row);

                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
