package net.sf.sketchlet.designer.editor.ui.variables;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.framework.blackboard.Variable;
import net.sf.sketchlet.designer.editor.ui.TextTransfer;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class CopyExpression {

    public static TextTransfer clipboard = new TextTransfer();
    public String strTitle = "";
    public String strHeader = null;
    public String strRow = "";
    public int startRow = 1;
    public String strFooter = null;
    public boolean isSeparator = false;
    public static Vector<CopyExpression> copySpreadsheetExpressions;
    public static Vector<CopyExpression> copyScriptExpressionsJS;
    public static Vector<CopyExpression> copyScriptExpressionsSketches;
    public static Vector<CopyExpression> copyScriptExpressionsBeanShell;
    public static Vector<CopyExpression> copyScriptExpressionsPython;
    public VariablesTablePanel tablePanel;

    public CopyExpression(VariablesTablePanel panel) {
        this.tablePanel = panel;
    }

    public static void load(VariablesTablePanel tablePanel) {
        CopyExpression.copySpreadsheetExpressions = load(tablePanel, "copy_spreadsheets.txt");
        CopyExpression.copyScriptExpressionsSketches = load(tablePanel, "copy_sketches.txt");
        CopyExpression.copyScriptExpressionsJS = load(tablePanel, "copy_scripts_js.txt");
        CopyExpression.copyScriptExpressionsBeanShell = load(tablePanel, "copy_scripts_beanshell.txt");
        CopyExpression.copyScriptExpressionsPython = load(tablePanel, "copy_scripts_python.txt");
    }

    public static Vector<CopyExpression> load(VariablesTablePanel tablePanel, String strConfFile) {
        String strFile = SketchletContextUtils.getSketchletDesignerConfDir() + "copy/" + strConfFile;
        Vector<CopyExpression> copyExpressions = new Vector<CopyExpression>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(strFile));

            CopyExpression e = null;

            String line;

            while ((line = in.readLine()) != null) {
                String strLastPart = "";
                int n = line.indexOf(" ");
                if (n > 0) {
                    strLastPart = line.substring(n);
                }

                if (line.startsWith("AddExpression")) {
                    if (e != null) {
                        copyExpressions.add(e);
                    }
                    e = new CopyExpression(tablePanel);
                } else if (line.startsWith("AddSeparator")) {
                    if (e != null) {
                        copyExpressions.add(e);
                    }
                    e = new CopyExpression(tablePanel);
                    e.isSeparator = true;
                } else if (line.startsWith("Title")) {
                    if (e != null) {
                        e.strTitle = strLastPart;
                    }
                } else if (line.startsWith("Expression")) {
                    if (e != null) {
                        e.strRow = strLastPart;
                    }
                } else if (line.startsWith("Header")) {
                    if (e != null) {
                        e.strHeader = strLastPart;
                    }
                } else if (line.startsWith("Footer")) {
                    if (e != null) {
                        e.strFooter = strLastPart;
                    }
                }
            }
            if (e != null) {
                copyExpressions.add(e);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return copyExpressions;
    }

    public static CopyExpression currentExpression = null;

    public String copyExpressions() {
        // currentExpression = this;
        if (tablePanel.selectedRow == -1 && tablePanel.selectedRow2 == -1) {
            return "";
        }

        boolean paused = VariablesBlackboard.getInstance().isPaused();
        VariablesBlackboard.getInstance().setPaused(true);

        int start, end;

        if (tablePanel.selectedRow2 == -1) {
            start = end = tablePanel.selectedRow;
        } else {
            start = Math.min(tablePanel.selectedRow, tablePanel.selectedRow2);
            end = Math.max(tablePanel.selectedRow, tablePanel.selectedRow2);
        }

        String strCopy = "";
        if (this.strHeader != null) {
            strCopy += strHeader + "\r\n";
        }

        String strInc = "";
        int index = 1;
        String strRowToShow = strRow;

        if (strRow.contains("<%=inc_row ")) {
            int n = strRow.indexOf("<%=inc_row ");
            int n2 = strRow.indexOf("%>", n + 1);

            if (n > -1 && n2 > n) {
                String strIncExpression = strRow.substring(n + 11, n2);
                StringTokenizer t = new StringTokenizer(strIncExpression);

                if (t.countTokens() == 2) {
                    strInc = t.nextToken();
                    try {
                        index = Integer.parseInt(t.nextToken());
                    } catch (Exception e) {
                    }
                } else {
                    try {
                        index = Integer.parseInt(t.nextToken());
                    } catch (Exception e) {
                    }
                }

                strRowToShow = strRow.substring(0, n) + "<%=incremental%>" + strRow.substring(n2 + 2);
            }
        }

        for (int i = start; i <= end; i++) {
            Variable v = VariablesTableModel.variableRows.elementAt(i);
            String strExp = strRowToShow.replace("<%=name%>", v.getName());
            strExp = strExp.replace("<%=value%>", v.getValue());
            strExp = strExp.replace("<%=description%>", v.getDescription());
            strExp = strExp.replace("<%=incremental%>", strInc + index++);
            strCopy += strExp.trim();
            if (start < end) {
                strCopy += "\r\n";
            }
        }

        VariablesBlackboard.getInstance().setPaused(paused);

        clipboard.setClipboardContents(strCopy);

        return strCopy;
    }

    public static void populateSpreadsheetsMenu(JMenu menu) {
        populateMenu(menu, CopyExpression.copySpreadsheetExpressions);
    }

    public static void populateSketchesMenu(JMenu menu) {
        populateMenu(menu, CopyExpression.copyScriptExpressionsSketches);
    }

    public static void populateScriptsMenu(JMenu menu) {
        JMenu menuJS = new JMenu("Javascript");
        populateMenu(menuJS, CopyExpression.copyScriptExpressionsJS);
        JMenu menuBeanShell = new JMenu("BeanShell");
        populateMenu(menuBeanShell, CopyExpression.copyScriptExpressionsBeanShell);
        JMenu menuPython = new JMenu("Python");
        populateMenu(menuPython, CopyExpression.copyScriptExpressionsPython);

        menu.add(menuJS);
        menu.add(menuBeanShell);
        menu.add(menuPython);
    }

    public static void populateMenu(JMenu menu, Vector<CopyExpression> expressions) {
        for (final CopyExpression e : expressions) {
            if (!e.isSeparator) {
                JMenuItem menuItem = new JMenuItem();
                menuItem.setText(e.strTitle);

                menuItem.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ae) {
                        e.copyExpressions();
                    }
                });

                menu.add(menuItem);
            } else {
                menu.addSeparator();
            }
        }
    }
}
