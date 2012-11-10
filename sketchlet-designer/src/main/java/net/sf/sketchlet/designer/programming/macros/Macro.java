/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.programming.macros;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.programming.screenscripts.ScreenScripts;
import net.sf.sketchlet.designer.programming.timers.Timer;
import net.sf.sketchlet.designer.programming.timers.Timers;
import net.sf.sketchlet.designer.ui.ProgressMonitor;
import net.sf.sketchlet.designer.ui.macros.ImageAreaSelect;
import net.sf.sketchlet.designer.ui.macros.MacroPanel;
import net.sf.sketchlet.designer.ui.macros.MacrosFrame;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.programming.MacroProgrammingUnit;
import net.sf.sketchlet.script.ScriptPluginProxy;
import net.sf.sketchlet.util.XMLUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Macro extends MacroProgrammingUnit {

    public final static String columnNames[] = {"Action", "Param 1", "Param 2"};
    public static String commands[] = {
            "",
            "Go to page",
            "Variable update",
            "Variable append",
            "Variable increment",
            "Variable glide",
            "Start timer",
            "Pause timer",
            "Stop timer",
            "Start action",
            "Stop action",
            "Stop sequence"
    };
    public static String commandsEx[] = {
            "",
            "Go to page",
            "Variable update",
            "Variable append",
            "Variable increment",
            "Variable glide",
            "Start timer",
            "Pause timer",
            "Stop timer",
            "Start action",
            "Stop action",
            "Start sequence",
            "IF",
            "REPEAT",
            "END",
            "PAUSE",
            "WAIT UNTIL",
            "WAIT FOR UPDATE",
            "STOP",};

    public static void prepareCommandList() {
        Vector<String> commandsVector = new Vector<String>();
        Vector<String> commandsVectorEx = new Vector<String>();
        commandsVector.add("");
        commandsVectorEx.add("");
        commandsVector.add("Go to page");
        commandsVectorEx.add("Go to page");
        if (Profiles.isActive("variables")) {
            commandsVector.add("Variable update");
            commandsVector.add("Variable append");
            commandsVector.add("Variable increment");
            commandsVector.add("Variable glide");
            commandsVectorEx.add("Variable update");
            commandsVectorEx.add("Variable append");
            commandsVectorEx.add("Variable increment");
            commandsVectorEx.add("Variable glide");
        }
        if (Profiles.isActive("timers")) {
            commandsVector.add("Start timer");
            commandsVector.add("Pause timer");
            commandsVector.add("Stop timer");
            commandsVectorEx.add("Start timer");
            commandsVectorEx.add("Pause timer");
            commandsVectorEx.add("Stop timer");
        }
        if (Profiles.isActiveAny("macros,scripts,screen_poking")) {
            commandsVector.add("Start action");
            commandsVector.add("Stop action");
            commandsVector.add("Start sequence");
            commandsVectorEx.add("Start action");
            commandsVectorEx.add("Stop action");
            commandsVectorEx.add("Start sequence");
        }
        if (Profiles.isActiveAny("macros,page_actions")) {
            commandsVectorEx.add("IF");
            commandsVectorEx.add("REPEAT");
            commandsVectorEx.add("END");
            commandsVectorEx.add("PAUSE");
            if (Profiles.isActive("variables")) {
                commandsVectorEx.add("WAIT UNTIL");
                commandsVectorEx.add("WAIT FOR UPDATE");
            }
            commandsVectorEx.add("STOP");
        }

        commands = commandsVector.toArray(new String[0]);
        commandsEx = commandsVectorEx.toArray(new String[0]);
    }

    public MacroPanel panel;

    public Macro() {
    }

    public void dispose() {
        if (panel != null) {
            panel.dispose();
            this.panel = null;
        }
    }

    private Map<String, String> parameters = new LinkedHashMap<String, String>();

    public Macro(Macro m) {
        for (int i = 0; i < this.actions.length; i++) {
            for (int j = 0; j < this.actions[i].length; j++) {
                this.actions[i][j] = (String) m.actions[i][j];
            }
        }

        calculateLevels();

        this.repeat = m.repeat;
    }

    public Macro getUndoCopy() {
        Macro m = new Macro();

        m.name = this.name;
        m.repeat = this.repeat;
        Page.copyArray(this.actions, m.actions);

        return m;
    }

    public void restore(Macro m) {
        this.name = m.name;
        this.repeat = m.repeat;
        Page.copyArray(m.actions, this.actions);
    }

    public static String checkOldCommand(String strCommand) {
        if (strCommand.toLowerCase().startsWith("pause")) {
            if (!strCommand.equalsIgnoreCase("pause timer")) {
                return "PAUSE";
            }
        } else if (strCommand.trim().equalsIgnoreCase("Go to sketch")) {
            return "Go to page";
        } else if (strCommand.trim().equalsIgnoreCase("start macro")) {
            return "Start action";
        } else if (strCommand.trim().equalsIgnoreCase("stop macro")) {
            return "Stop action";
        }
        return strCommand;
    }

    public int getLastNonEmptyRow() {
        int row = -1;

        for (int i = 0; i < actions.length; i++) {
            if (!actions[i][0].toString().isEmpty() || !actions[i][1].toString().isEmpty() || !actions[i][2].toString().isEmpty()) {
                row = i;
            }
        }

        return row;
    }

    public void calculateLevels() {
        int level = 0;
        for (int i = 0; i < actions.length; i++) {
            String strAction = (String) actions[i][0];
            if (strAction.equalsIgnoreCase("end") || strAction.isEmpty()) {
                if (level > 0) {
                    level--;
                }
            }

            levels[i] = level;

            if (strAction.equalsIgnoreCase("if") || strAction.equalsIgnoreCase("repeat")) {
                level++;
            }
        }
    }

    public MacroThread startThread(String strParams, String strVarPrefix, String strVarPostfix) {
        return startThread(strParams, strVarPrefix, strVarPostfix, null);
    }

    public MacroThread startThread(String strParams, String strVarPrefix, String strVarPostfix, ProgressMonitor progressMonitor) {
        if (panel != null) {
            panel.save();
            try {
                this.repeat = Integer.parseInt(this.panel.repeatField.getEditor().getItem().toString());
            } catch (Exception e) {
                this.repeat = 1;
            }
        }
        return new MacroThread(this, strParams, strVarPrefix, strVarPostfix, progressMonitor);
    }

    public MacroThread startThread(Object source, String strParams, String strVarPrefix, String strVarPostfix, ProgressMonitor progressMonitor) {
        if (panel != null) {
            panel.save();
            try {
                this.repeat = Integer.parseInt(this.panel.repeatField.getEditor().getItem().toString());
            } catch (Exception e) {
                this.repeat = 1;
            }
        }
        return new MacroThread(source, this, strParams, strVarPrefix, strVarPostfix, progressMonitor);
    }

    public void save(PrintWriter out) {
        if (panel != null) {
            this.name = this.panel.macroName.getText();
            try {
                this.repeat = Integer.parseInt(this.panel.repeatField.getEditor().getItem().toString());
            } catch (Exception e) {
                this.repeat = 1;
            }
            complete();
        }
        save(out, "macro");
    }

    static Vector<String[]> copiedCommands = new Vector<String[]>();

    public void copy() {
        copiedCommands.removeAllElements();
        for (int i = 0; i < actions.length; i++) {
            String a = actions[i][0].toString();
            String p1 = actions[i][1].toString();
            String p2 = actions[i][2].toString();

            if (!a.isEmpty() || !p1.isEmpty() || !p2.isEmpty()) {
                copiedCommands.add(new String[]{a + "", p1 + "", p2 + ""});
            }
        }
    }

    public void paste() {
        int last = this.getLastNonEmptyRow();

        int r = 0;
        for (int i = last + 1; i < actions.length && r < copiedCommands.size(); i++, r++) {
            String a[] = copiedCommands.elementAt(r);
            actions[i][0] = a[0] + "";
            actions[i][1] = a[1] + "";
            actions[i][2] = a[2] + "";
        }
    }

    public void complete() {
        this.calculateLevels();
        int start = 0;
        int end = 0;
        for (int i = 0; i < actions.length; i++) {
            String strAction = (String) actions[i][0];
            int level = levels[i];
            if (strAction.equalsIgnoreCase("if") || strAction.equalsIgnoreCase("repeat")) {
                start++;
            } else if (strAction.equalsIgnoreCase("end")) {
                end++;
            }
        }

        for (int i = 0; end < start && i < actions.length; i++) {
            String strAction = (String) actions[i][0];
            if (strAction.isEmpty()) {
                actions[i][0] = "END";
                actions[i][1] = "";
                actions[i][2] = "";
                end++;
            }
        }

        for (int i = actions.length - 1; end > start && i >= 0; i--) {
            String strAction = (String) actions[i][0];
            if (strAction.equalsIgnoreCase("end")) {
                actions[i][0] = "";
                actions[i][1] = "";
                actions[i][2] = "";
                end--;
            }
        }
    }

    public void save(PrintWriter out, String strMacroTag) {
        if (panel != null) {
            String oldName = this.name;
            this.name = this.panel.macroName.getText();
            if (!this.name.equals(oldName)) {
                SketchletEditor.editorPanel.pages.replaceReferences("Call Macro", oldName, this.name);
                SketchletEditor.editorPanel.pages.replaceReferences("Start Action", oldName, this.name);
                SketchletEditor.editorPanel.pages.replaceReferences("Stop Action", oldName, this.name);
            }
            try {
                this.repeat = Integer.parseInt(this.panel.repeatField.getEditor().getItem().toString());
            } catch (Exception e) {
                this.repeat = 1;
            }
        }

        saveSimple(out, strMacroTag, "");
    }

    public void saveSimple(PrintWriter out, String strMacroTag, String linePrefix) {
        out.println(linePrefix + "<" + strMacroTag + ">");
        out.println(linePrefix + "<name>" + XMLUtils.prepareForXML(this.name) + "</name>");
        out.println(linePrefix + "<repeat>" + this.repeat + "</repeat>");
        for (int i = 0; i < actions.length; i++) {
            boolean bSave = false;
            for (int j = 0; j < actions[i].length; j++) {
                if (!actions[i][j].equals("")) {
                    bSave = true;
                    break;
                }
            }
            if (!bSave) {
                continue;
            }
            out.println(linePrefix + "    <action>");
            out.println(linePrefix + "      <type>" + XMLUtils.prepareForXML((String) actions[i][0]) + "</type>");
            out.println(linePrefix + "      <param1><![CDATA[" + actions[i][1] + "]]></param1>");
            out.println(linePrefix + "      <param2><![CDATA[" + actions[i][2] + "]]></param2>");
            out.println(linePrefix + "    </action>");
        }

        if (parameters.size() > 0) {
            out.println(linePrefix + "    <named-parameters>");
            for (String paramName : parameters.keySet()) {
                out.println(linePrefix + "        <named-parameter>");
                out.println(linePrefix + "            <parameter-name>" + paramName + "</parameter-name>");
                out.println(linePrefix + "            <parameter-value><![CDATA[" + parameters.get(paramName) + "]]></parameter-value>");
                out.println(linePrefix + "        </named-parameter>");
            }
            out.println(linePrefix + "    </named-parameters>");
        }

        out.println(linePrefix + "</" + strMacroTag + ">");
    }

    public static void setCombos(final JTable table, final JComboBox paramComboBox, final Object data[][], final int columnAction, final int columnParam) {
        final JComboBox comboBox = new JComboBox();
        comboBox.setEditable(false);
        paramComboBox.setEditable(true);

        for (int i = 0; i < Macro.commands.length; i++) {
            comboBox.addItem(Macro.commands[i]);
        }

        comboBox.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                String text = (String) comboBox.getSelectedItem();
                if (text != null && table.getSelectedRow() >= 0) {
                    Object selItem = table.getModel().getValueAt(table.getSelectedRow(), columnParam);
                    if (selItem != null) {
                        loadParam1Combo(paramComboBox, selItem.toString(), text);
                    }
                }
            }
        });
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String strParam = data[row][columnParam] + "";
                    Macro.refreshParam1Combo((String) data[row][columnAction], paramComboBox);
                    paramComboBox.setSelectedItem(strParam);
                    data[row][columnParam] = strParam;
                    // modelEvents.fireTableDataChanged();
                }
            }
        });

        table.getColumnModel().getColumn(columnAction).setCellEditor(new DefaultCellEditor(comboBox));
        table.getColumnModel().getColumn(columnParam).setCellEditor(new DefaultCellEditor(paramComboBox));

        table.addMouseListener(new PopupListener(table, data, columnAction));
    }

    static JComboBox lastComboBoxParam1 = null;

    public static void refreshParam1Combo(String strCommand, JComboBox comboBoxParam1) {
        lastComboBoxParam1 = comboBoxParam1;

        if (comboBoxParam1 == null) {
            return;
        }

        for (int i = 0; i < Macro.commands.length; i++) {
            if (Macro.commands[i].equalsIgnoreCase(strCommand)) {
                Macro.loadParam1Combo(comboBoxParam1, Macro.commands[i], strCommand);
                break;
            }
        }
    }

    public static void loadParam1Combo(JComboBox comboBoxParam1, String selectedItem, String strCommand) {
        if (strCommand == null) {
            return;
        }
        strCommand = strCommand.toLowerCase();
        lastComboBoxParam1 = comboBoxParam1;
        comboBoxParam1.setEditable(true);
        comboBoxParam1.removeAllItems();
        comboBoxParam1.addItem("");

        if (strCommand.equalsIgnoreCase("pause") || strCommand.equalsIgnoreCase("pause (seconds)")) {
            for (double i = 1.0; i <= 10.0; i += 1.0) {
                comboBoxParam1.addItem(i + "");
            }
        } else if (strCommand.startsWith("variable") || strCommand.startsWith("wait for update")) {
            for (String strVar : DataServer.variablesServer.variablesVector) {
                comboBoxParam1.addItem(strVar);
            }
        } else if (strCommand.endsWith("timer") && Timers.globalTimers != null) {
            for (Timer t : Timers.globalTimers.timers) {
                comboBoxParam1.addItem(t.name);
            }
        } else if (strCommand.equalsIgnoreCase("Go to page") && SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.pages != null) {
            Page currentPage = SketchletEditor.editorPanel.currentPage;
            for (Page s : SketchletEditor.editorPanel.pages.pages) {
                if (!currentPage.getTitle().equalsIgnoreCase(s.getTitle())) {
                    comboBoxParam1.addItem(s.title);
                }
            }
        } else if (strCommand.endsWith("macro") && Macros.globalMacros != null) {
            for (Macro m : Macros.globalMacros.macros) {
                comboBoxParam1.addItem(m.name);
            }
            if (ScreenScripts.publicScriptRunner != null) {
                ScreenScripts.publicScriptRunner.setCombos(comboBoxParam1);
            }

            for (ScriptPluginProxy script : DataServer.scripts) {
                comboBoxParam1.addItem("Script:" + script.scriptFile.getName());
            }
        } else if (strCommand.equalsIgnoreCase("repeat")) {
            comboBoxParam1.addItem("Forever");
            for (int i = 2; i <= 10; i++) {
                comboBoxParam1.addItem(i + "");
            }
        }

        if (selectedItem != null) {
            comboBoxParam1.setSelectedItem(selectedItem);
        }
    }

    public static void loadParam2Field(JComboBox comboParam1, JTextField fieldParam2, String selectedItem, String strCommand) {
        strCommand = strCommand.toLowerCase();

        if (strCommand.startsWith("variable")) {
            comboParam1.setEnabled(true);
            fieldParam2.setEditable(true);
            fieldParam2.setEnabled(true);
        } else if (strCommand.isEmpty() || strCommand.equalsIgnoreCase("end")) {
            comboParam1.setSelectedItem("");
            comboParam1.setEnabled(false);
            fieldParam2.setText("");
            fieldParam2.setEditable(false);
            fieldParam2.setEnabled(false);
        } else {
            comboParam1.setEnabled(true);
            fieldParam2.setText("");
            fieldParam2.setEditable(false);
            fieldParam2.setEnabled(false);
        }
    }

    public boolean replaceReferences(String _action, String oldName, String newName) {
        boolean replaced = false;
        for (int i = 0; i < this.actions.length; i++) {
            String action = (String) actions[i][0];
            String param = (String) actions[i][1];
            if (action.equalsIgnoreCase(_action) && param.equals(oldName)) {
                replaced = true;
                actions[i][1] = newName;
            }
        }

        return replaced;
    }

    public void getHTMLCode(PrintWriter out, String titleTag) {
        boolean bSaveMacro = false;
        for (int i = 0; i < actions.length; i++) {
            for (int j = 0; j < actions[i].length; j++) {
                if (!actions[i][j].equals("")) {
                    bSaveMacro = true;
                    break;
                }
            }
            if (bSaveMacro) {
                break;
            }
        }

        if (!bSaveMacro) {
            return;
        }
        out.println("<" + titleTag + ">" + XMLUtils.prepareForXML(this.name) + "</" + titleTag + ">");
        if (this.repeat != 1) {
            out.println("<p>");
            out.println("Repeat: " + (repeat == 0 ? "Forever" : repeat + " times"));
            out.println("</p>");
        }
        out.println("<pre>");
        this.calculateLevels();
        int n = 0;
        for (int i = 0; i < actions.length; i++) {
            boolean bSave = false;
            for (int j = 0; j < actions[i].length; j++) {
                if (!actions[i][j].equals("")) {
                    bSave = true;
                    break;
                }
            }
            if (!bSave) {
                continue;
            }
            n++;
            String strN = "00" + n;
            strN = strN.substring(strN.length() - 2);

            String prefix = "";
            int l = levels[i];
            for (int il = 0; il < l; il++) {
                prefix += "  ";
            }
            out.println(strN + "   " + prefix + (String) actions[i][0] + " " + (String) actions[i][1] + " " + (String) actions[i][2]);
        }
        out.println("</pre>");
    }


    public void addLine(String action, String param1, String param2) {
        for (int i = actions.length - 2; i >= 0; i--) {
            if (!actions[i][0].toString().isEmpty()) {
                actions[i + 1][0] = action;
                actions[i + 1][1] = param1;
                actions[i + 1][2] = param2;
                return;
            }
        }

        actions[0][0] = action;
        actions[0][1] = param1;
        actions[0][2] = param2;
    }

    public void setLastLineValue(int column, String value) {
        for (int i = actions.length - 1; i >= 0; i--) {
            if (!actions[i][0].toString().isEmpty()) {
                actions[i][column] = value;
                return;
            }
        }
    }

    static class PopupListener extends MouseAdapter {

        JTable table;
        Object data[][];
        int cmdCol = 0;
        public JMenuItem menuItemEdit;
        public JMenuItem menuItemImageEdit;
        public JMenuItem menuItemNew;
        JPopupMenu popupMenu = new JPopupMenu();
        JPopupMenu popupImageMenu = new JPopupMenu();
        String strAction = "";
        String strParam = "";
        int row = -1;

        public PopupListener(JTable table, final Object data[][], final int cmdCol) {
            this.table = table;
            this.data = data;
            this.cmdCol = cmdCol;

            menuItemEdit = new JMenuItem(Language.translate("Edit..."));
            menuItemEdit.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    if (strAction.equalsIgnoreCase("Start Timer") || strAction.equalsIgnoreCase("Stop Timer") || strAction.equalsIgnoreCase("Pause Timer")) {
                        SketchletEditor.editorPanel.extraEditorPanel.timersExtraPanel.showTimers(strParam);
                    } else if (strAction.equalsIgnoreCase("Stop Action") || strAction.equalsIgnoreCase("Call Macro") || strAction.equalsIgnoreCase("Stop Macro")) {
                        MacrosFrame.showMacros(strParam, false);
                    } else if (strAction.equalsIgnoreCase("Go to page")) {
                        SketchletEditor.editorPanel.selectSketch(strParam);
                    }
                }
            });
            menuItemNew = new JMenuItem("Create New...");
            menuItemNew.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    if (strAction.equalsIgnoreCase("Start Timer") || strAction.equalsIgnoreCase("Stop Timer") || strAction.equalsIgnoreCase("Pause Timer")) {
                        Timer t = SketchletEditor.editorPanel.extraEditorPanel.timersExtraPanel.newTimer();
                        data[row][cmdCol + 1] = t.name;
                    } else if (strAction.equalsIgnoreCase("Stop Action") || strAction.equalsIgnoreCase("Call Macro") || strAction.equalsIgnoreCase("Stop Macro")) {
                        MacrosFrame.showMacros(false);
                        Macro m = MacrosFrame.frame.newMacro();
                        data[row][cmdCol + 1] = m.name;
                    } else if (strAction.equalsIgnoreCase("Go to page")) {
                        Page s = SketchletEditor.editorPanel.newSketch();
                        if (s != null) {
                            data[row][cmdCol + 1] = s.title;
                        }
                    }
                }
            });
            popupMenu.add(menuItemEdit);
            popupMenu.addSeparator();
            popupMenu.add(menuItemNew);

            menuItemImageEdit = new JMenuItem("Define image area...");
            menuItemImageEdit.setIcon(Workspace.createImageIcon("resources/computer.png"));
            menuItemImageEdit.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    BufferedImage image = SketchletEditor.editorPanel.renderer.paintImage(0, 0, 2000, 2000);

                    String strTemp = data[row][cmdCol + 2].toString().trim();
                    int n = strTemp.indexOf(" ");
                    if (n > 0) {
                        ImageAreaSelect.strVariable = strTemp.substring(0, n);
                        if (strTemp.length() > n + 1) {
                            ImageAreaSelect.strFile = strTemp.substring(n + 1);
                        }
                    } else {
                        ImageAreaSelect.strVariable = strTemp;
                    }
                    int x = 0;
                    int y = 0;
                    int w = 100;
                    int h = 100;
                    try {
                        String params[] = data[row][cmdCol + 1].toString().split(" ");
                        x = Integer.parseInt(params[0]);
                        y = Integer.parseInt(params[1]);
                        w = Integer.parseInt(params[2]);
                        h = Integer.parseInt(params[3]);
                    } catch (Exception eNum) {
                    }
                    ImageAreaSelect.createAndShowGUI(null, image, x, y, w, h, true);
                    if (ImageAreaSelect.bSaved) {
                        lastComboBoxParam1.addItem(ImageAreaSelect.strArea);
                        data[row][cmdCol + 1] = ImageAreaSelect.strArea;
                        data[row][cmdCol + 2] = ImageAreaSelect.strVariable + " " + ImageAreaSelect.strFile;
                    }
                }
            });
            popupImageMenu.add(menuItemImageEdit);
            TutorialPanel.prepare(popupMenu, true);
            TutorialPanel.prepare(popupImageMenu, true);

        }

        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }

        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
            }
        }

        private void showPopup(MouseEvent e) {
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
                row = table.rowAtPoint(e.getPoint());
                table.getSelectionModel().setSelectionInterval(row, row);

                strAction = (String) data[row][this.cmdCol];
                strParam = (String) data[row][this.cmdCol + 1];

                if (!strParam.startsWith("=")) {
                    menuItemEdit.setEnabled(!strParam.trim().equals(""));
                    if (strAction.equalsIgnoreCase("Start Timer") || strAction.equalsIgnoreCase("Stop Timer") || strAction.equalsIgnoreCase("Pause Timer")) {
                        menuItemEdit.setText("Edit Timer '" + strParam + "'");
                        menuItemEdit.setIcon(Workspace.createImageIcon("resources/timer.png"));
                        menuItemNew.setText("New Timer");
                        menuItemNew.setIcon(Workspace.createImageIcon("resources/timer.png"));
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    } else if (strAction.equalsIgnoreCase("Stop Action") || strAction.equalsIgnoreCase("Call Macro") || strAction.equalsIgnoreCase("Stop Macro")) {
                        menuItemEdit.setText("Edit Macro '" + strParam + "'");
                        menuItemEdit.setIcon(Workspace.createImageIcon("resources/macros.png"));
                        menuItemNew.setText("New Macro");
                        menuItemNew.setIcon(Workspace.createImageIcon("resources/macros.png"));
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    } else if (strAction.equalsIgnoreCase("Go to page")) {
                        menuItemEdit.setText("Edit Sketch '" + strParam + "'");
                        menuItemEdit.setIcon(Workspace.createImageIcon("resources/editor.gif"));
                        menuItemNew.setText("New Sketch");
                        menuItemNew.setIcon(Workspace.createImageIcon("resources/editor.gif"));
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    } /*else if (strAction.equalsIgnoreCase("Copy sketch to clipboard")) {
                    popupImageMenu.show(e.getComponent(), e.getX(), e.getY());
                    } else if (strAction.equalsIgnoreCase("Save sketch to file")) {
                    popupImageMenu.show(e.getComponent(), e.getX(), e.getY());
                    }*/
                }
            }
        }
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
