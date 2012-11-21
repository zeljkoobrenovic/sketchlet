package net.sf.sketchlet.model.programming.screenscripts;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import net.sf.sketchlet.common.dnd.GenericTableTransferHandler;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.Workspace;
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
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Vector;

/**
 * @author cuypers
 */
public class ScreenScripts extends JPanel {

    private AbstractTableModel model;
    private JTable table;
    private JToolBar controlPanel = new JToolBar();
    private JToolBar toolbarHelp = new JToolBar();
    private JButton btnAdd = new JButton(Workspace.createImageIcon("resources/add.gif", ""));
    private JButton btnEdit = new JButton(Workspace.createImageIcon("resources/open.gif", ""));
    private JButton btnDelete = new JButton(Workspace.createImageIcon("resources/remove.gif", ""));
    private JButton help = new JButton(Workspace.createImageIcon("resources/help-browser.png", ""));
    private String scriptDir = net.sf.sketchlet.common.context.SketchletContextUtils.getCurrentProjectMouseAndKeyboardActionsDir();
    private int currentRow;
    private ScreenScriptRunner scriptRunner = null;
    private boolean started = false;
    private static ScreenScripts screenScriptsPanel = null;
    private Vector<ScreenScriptInfo> screenScripts = new Vector<ScreenScriptInfo>();
    private static ScreenScriptRunner publicScriptRunner = null;

    public static void closeScreenScripts() {
    }

    public static ScreenScripts createScreenScripts(boolean bRun) {
        if (getScreenScriptsPanel() == null) {
            setScreenScriptsPanel(new ScreenScripts());
        }

        // screenScriptsFrame.setVisible(true);

        //if (bRun) {
        //    screenScriptsPanel.btnRun.doClick();
        //}

        return getScreenScriptsPanel();
    }

    private ScreenScripts() {
        //super("Mouse and Keyboard Actions");
        //this.setIconImage(Workspace.createImageIcon("resources/mouse.png").getImage());
        setPublicScriptRunner(new ScreenScriptRunner(getScriptDir()));
        setLayout(new BorderLayout());
        setModel(new MyTableModel());
        table = new JTable(getModel());
        table.setTableHeader(null);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(table);

        table.setPreferredScrollableViewportSize(new Dimension(300, 150));
        table.setFillsViewportHeight(true);
        table.setDragEnabled(true);
        table.setTransferHandler(new GenericTableTransferHandler("@macro Screen:", 0));

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        //Set up column sizes.
        //initColumnSizes(table);


        //Add the scroll pane to this panel.
        add(scrollPane);
        btnAdd.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                String name = null; // = (String) JOptionPane.showInputDialog("Action Name:");

                int i = getModel().getRowCount() + 1;
                name = "Action " + (i < 10 ? "0" : "") + i;
                File file = new File(getScriptDir() + name + ".txt");
                if (file.exists()) {
                    i++;
                    name = "Action " + (i < 10 ? "0" : "") + i;
                    file = new File(getScriptDir() + name + ".txt");
                }

                try {
                    PrintWriter out = new PrintWriter(new FileWriter(file));
                    out.println();
                    out.flush();
                    out.close();

                    loadScreenScripts();
                    for (ScreenScriptInfo script : getScreenScripts()) {
                        if (script.getName().equalsIgnoreCase(name)) {
                            openScript(script);
                            break;
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnEdit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                openScript();
            }
        });

        btnDelete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                int sel = table.getSelectedRow();
                if (sel == -1) {
                    return;
                }

                ScreenScriptInfo s = getScreenScripts().elementAt(sel);

                s.getFile().renameTo(new File(getScriptDir() + "deleted" + File.separator + s.getFile().getName()));
                s.getFile().delete();

                loadScreenScripts();
            }
        });

        btnAdd.setToolTipText(Language.translate("Create a new screen poking script"));
        btnEdit.setToolTipText(Language.translate("Edit the selected screen poking script"));
        btnDelete.setToolTipText(Language.translate("Delete the selected screen poking script"));

        /*btnRun.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent e) {
        run();
        }
        });*/


        ListSelectionModel rowSM = table.getSelectionModel();
        rowSM.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) {
                    return;
                }

                /*
                ListSelectionModel lsm = (ListSelectionModel) e.getSource();
                if (lsm.isSelectionEmpty()) {
                } else {
                int selectedRow = lsm.getMinSelectionIndex();
                currentRow = selectedRow;
                
                if (CaptureFrame.captureFrame != null) {
                ScreenScriptInfo s = screenScripts.elementAt(selectedRow);
                CaptureFrame.openCaptureFrame(s.file, s.name);
                }
                }*/

                enableControls();
            }
        });

        table.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    openScript();
                }

            }
        });

        controlPanel.setFloatable(false);
        controlPanel.add(btnAdd);
        controlPanel.add(btnEdit);
        controlPanel.add(btnDelete);
        controlPanel.add(new JLabel("    "));
        //controlPanel.add(btnRun);
        toolbarHelp.setFloatable(false);
        controlPanel.setBorder(BorderFactory.createEmptyBorder());

        toolbarHelp.add(help);
        toolbarHelp.setBorder(BorderFactory.createEmptyBorder());
        help.setToolTipText(Language.translate("What is Screen Poking?"));
        help.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                HelpUtils.openHelpFile("Screen Poking", "screen_poking");
            }
        });

        JPanel panelNorth = new JPanel(new BorderLayout());
        panelNorth.add(controlPanel);
        panelNorth.add(toolbarHelp, BorderLayout.EAST);
        add(panelNorth, BorderLayout.NORTH);

        loadScreenScripts();
    }

    public static ScreenScripts getScreenScriptsPanel() {
        return screenScriptsPanel;
    }

    public static void setScreenScriptsPanel(ScreenScripts screenScriptsPanel) {
        ScreenScripts.screenScriptsPanel = screenScriptsPanel;
    }

    public static ScreenScriptRunner getPublicScriptRunner() {
        return publicScriptRunner;
    }

    public static void setPublicScriptRunner(ScreenScriptRunner publicScriptRunner) {
        ScreenScripts.publicScriptRunner = publicScriptRunner;
    }

    public void run() {
        if (!started) {
            if (CaptureFrame.getCaptureFrame() != null) {
                CaptureFrame.getCaptureFrame().setVisible(false);
                CaptureFrame.getCaptureFrame().close();
            }
            /*btnRun.setText("Stop");
            btnRun.setIcon(Workspace.createImageIcon("resources/stop2.gif", ""));*/

            if (scriptRunner != null) {
                scriptRunner.setStopped(true);
                DataServer.getInstance().removeVariablesUpdateListener(scriptRunner);
            }

            scriptRunner = new ScreenScriptRunner(getScriptDir());
            DataServer.getInstance().addVariablesUpdateListener(scriptRunner);
        } else {
            // btnRun.setText("Start");
            // btnRun.setIcon(Workspace.createImageIcon("resources/start.gif", ""));
            if (scriptRunner != null) {
                scriptRunner.setStopped(true);
                DataServer.getInstance().removeVariablesUpdateListener(scriptRunner);
            }

            scriptRunner = null;
        }

        started = !started;
    }

    public void enableControls() {
        btnEdit.setEnabled(table.getSelectedRow() >= 0);
        btnDelete.setEnabled(table.getSelectedRow() >= 0);
        // btnRun.setEnabled(table.getRowCount() > 0);
    }

    public void close() {
        setVisible(false);
        setScreenScriptsPanel(null);
        if (CaptureFrame.getCaptureFrame() != null) {
            CaptureFrame.getCaptureFrame().setVisible(false);
            CaptureFrame.getCaptureFrame().close();
        }
    }

    public void openScript() {
        int sel = table.getSelectedRow();
        if (sel == -1) {
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ScreenScriptInfo s = getScreenScripts().elementAt(sel);

        openScript(s);
    }

    private void openScript(ScreenScriptInfo s) {
        CaptureFrame.openCaptureFrame(s.getFile(), s.getName());
        CaptureFrame.getCaptureFrame().setVisible(true);
        this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void loadScreenScripts() {
        try {
            new File(getScriptDir()).mkdirs();
            new File(getScriptDir() + "deleted").mkdirs();

            this.getScreenScripts().removeAllElements();

            File dir = new File(this.getScriptDir());
            File scriptFiles[] = dir.listFiles();
            Arrays.sort(scriptFiles);

            long lastModified = 0;
            int lastIndex = 0;

            for (int i = 0, is = 0; i < scriptFiles.length; i++) {
                File file = scriptFiles[i];

                if (!file.getName().endsWith(".txt")) {
                    continue;
                }

                if (file.lastModified() > lastModified) {
                    lastModified = file.lastModified();
                    lastIndex = is;
                }

                ScreenScriptInfo s = new ScreenScriptInfo();
                s.setFile(file);
                s.setName(file.getName().substring(0, file.getName().length() - 4));
                s.setStatus("loaded");
                this.getScreenScripts().add(s);
                is++;
            }

            getModel().fireTableDataChanged();

            if (getScreenScripts().size() > 0) {
                table.getSelectionModel().setSelectionInterval(lastIndex, lastIndex);
            }

            enableControls();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setPublicScriptRunner(new ScreenScriptRunner(getScriptDir()));
    }

    public void loadScreenScripts(File selectFile) {
        try {
            new File(getScriptDir()).mkdirs();
            new File(getScriptDir() + "deleted").mkdirs();

            this.getScreenScripts().removeAllElements();

            File dir = new File(this.getScriptDir());
            File scriptFiles[] = dir.listFiles();
            Arrays.sort(scriptFiles);

            int lastIndex = -1;

            for (int i = 0, is = 0; i < scriptFiles.length; i++) {
                File file = scriptFiles[i];

                if (!file.getName().endsWith(".txt")) {
                    continue;
                }

                if (file.getName().equals(selectFile.getName())) {
                    lastIndex = is;
                }

                ScreenScriptInfo s = new ScreenScriptInfo();
                s.setFile(file);
                s.setName(file.getName().substring(0, file.getName().length() - 4));
                s.setStatus("loaded");
                this.getScreenScripts().add(s);
                is++;
            }

            getModel().fireTableDataChanged();
            if (getScreenScripts().size() > 0) {
                table.getSelectionModel().setSelectionInterval(lastIndex, lastIndex);
            }
            enableControls();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        new ScreenScripts();
    }

    public Vector<ScreenScriptInfo> getScreenScripts() {
        return screenScripts;
    }

    public void setScreenScripts(Vector<ScreenScriptInfo> screenScripts) {
        this.screenScripts = screenScripts;
    }

    public AbstractTableModel getModel() {
        return model;
    }

    public void setModel(AbstractTableModel model) {
        this.model = model;
    }

    public String getScriptDir() {
        return scriptDir;
    }

    public void setScriptDir(String scriptDir) {
        this.scriptDir = scriptDir;
    }

    public int getCurrentRow() {
        return currentRow;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }

    class MyTableModel extends AbstractTableModel {

        public String[] getColumnNames() {
            String[] columnNames = {Language.translate("Actions")};
            return columnNames;
        }

        public String[] longValues = {
                "Action name", "100, 100", "This is a description"
        };

        public int getColumnCount() {
            return getColumnNames().length;
        }

        public int getRowCount() {
            return getScreenScripts().size();
        }

        public String getColumnName(int col) {
            return getColumnNames()[col];
        }

        public Object getValueAt(int row, int col) {
            ScreenScriptInfo script = getScreenScripts().elementAt(row);

            switch (col) {
                case 0:
                    return script.getName();
                case 1:
                    return script.getDescription();
            }

            return "";
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return String.class;
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return false;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            ScreenScriptInfo script = getScreenScripts().elementAt(row);

            switch (col) {
                case 0:
                    script.setName((String) value);
                    script.getFile().renameTo(new File(script.getFile().getParent() + File.separator + (String) value + ".txt"));
                    loadScreenScripts();
                    break;
                case 1:
                    script.setDescription((String) value);
                    break;
            }

            fireTableCellUpdated(row, col);
        }

        private void printDebugData() {
        }
    }
}
