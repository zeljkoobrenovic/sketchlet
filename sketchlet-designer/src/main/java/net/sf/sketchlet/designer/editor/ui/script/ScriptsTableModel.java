package net.sf.sketchlet.designer.editor.ui.script;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.script.ScriptPluginProxy;
import org.apache.log4j.Logger;

import javax.swing.table.AbstractTableModel;
import java.io.File;


/**
 * @author cuypers
 */
public class ScriptsTableModel extends AbstractTableModel {
    private static final Logger log = Logger.getLogger(ScriptsTableModel.class);
    public static ScriptsTableModel model;
    ScriptsTablePanel panel;

    public ScriptsTableModel(ScriptsTablePanel panel) {
        model = this;
        this.panel = panel;
    }

    public static void refresh() {
        if (model != null) {
            model.fireTableDataChanged();
        }
    }

    public String[] getColumnNames() {
        String[] columnNames = {Language.translate("Script file"), Language.translate("Status")};
        return columnNames;
    }

    public int getColumnCount() {
        return getColumnNames().length;
    }

    public int getRowCount() {
        return VariablesBlackboard.getScripts().size();
    }

    public String getColumnName(int col) {
        return getColumnNames()[col];
    }

    public Object getValueAt(int row, int col) {
        if (VariablesBlackboard.getScripts().size() == 0) {
            return "";
        }
        ScriptPluginProxy script = VariablesBlackboard.getScripts().get(row);

        if (script == null) {
            return "";
        }

        if (col == 0) {
            String strValue = (String) VariablesBlackboard.getScriptFiles().get(row);

            try {
                strValue = new File(strValue).getName();
            } catch (Exception e) {
                log.error(e);
            }

            return strValue;
        } else if (col == 1) {
            return script.getStatus();
        }

        return "";
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void setValueAt(Object value, int row, int col) {
        if (col == 0) {
            this.panel.renameScript((String) value);
        }
    }
}
