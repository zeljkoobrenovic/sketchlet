/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui.properties;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.blackboard.VariablesBlackboard;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.model.PropertiesInterface;
import net.sf.sketchlet.model.programming.macros.Commands;
import net.sf.sketchlet.model.programming.timers.curves.Curve;
import net.sf.sketchlet.model.programming.timers.curves.Curves;

import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

public class PropertiesVariableMappingHandler implements VariableUpdateListener {

    Hashtable<String, String> lastUpdateVariable = new Hashtable<String, String>();
    PropertiesInterface properties;
    PropertiesSetPanel setPanel = null;

    public PropertiesVariableMappingHandler(PropertiesInterface properties) {
        this.properties = properties;
    }

    public void dispose() {
        properties = null;
        setPanel = null;
        lastUpdateVariable.clear();
        data = null;
    }

    public String columnNames[] = {Language.translate("Property"), Language.translate("Min"), Language.translate("Max"), Language.translate("Variable"), Language.translate("Min"), Language.translate("Max"), Language.translate("Format"), Language.translate("Curve")};
    public Object data[][] = {
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""},
            {"", "", "", "", "", "", "", ""}};

    public AbstractTableModel getTableModel() {
        return new AbstractTableModel() {

            public int getColumnCount() {
                return columnNames.length;
            }

            public String getColumnName(int col) {
                return columnNames[col];
            }

            public int getRowCount() {
                return data.length;
            }

            public Object getValueAt(int row, int col) {
                return data[row][col];
            }

            public void setValueAt(Object value, int row, int col) {
                data[row][col] = value;
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }
        };

    }

    static boolean bUpdating = false;

    public synchronized void variableUpdated(String var, String value) {
        changePerformed(var, value);
    }

    Vector<Integer> ignoreRows = new Vector<Integer>();
    Vector<String> ignoreVars = new Vector<String>();

    public void refreshPropertiesFromVariables() {
        Vector<String> alreadyUpdated = new Vector<String>();
        for (int i = 0; i < data.length; i++) {
            String strVar = data[i][3].toString();
            if (!strVar.isEmpty()) {
                String value = VariablesBlackboard.getInstance().getVariableValue(strVar);
                if (value != null && !value.isEmpty()) {
                    changePerformed(strVar, value);
                }
                alreadyUpdated.add(strVar);
            }
        }
    }

    public void changePerformed(String var, String value) {
        if (/*VariablesBlackboard.paused || bUpdating || */ignoreVars.contains(var) || data == null) {
            return;
        }
        ignoreVars.add(var);
        for (int i = 0; i < data.length; i++) {
            String property = "@property:" + (String) data[i][0];
            for (Integer ignoreRow : ignoreRows) {
                if (ignoreRow.intValue() == i) {
                    continue;
                }
            }

            ignoreVars.add("@property:" + property);

            String _var = (String) data[i][3];
            if (property.length() > 0 && _var.length() > 0 && (var.equalsIgnoreCase(property) || var.equalsIgnoreCase(_var))) {
                String strMin1;
                String strMax1;
                String strMin2;
                String strMax2;
                if (var.equals(property)) {
                    strMin1 = (String) data[i][1];
                    strMax1 = (String) data[i][2];
                    strMin2 = (String) data[i][4];
                    strMax2 = (String) data[i][5];
                } else {
                    strMin1 = (String) data[i][4];
                    strMax1 = (String) data[i][5];
                    strMin2 = (String) data[i][1];
                    strMax2 = (String) data[i][2];
                }
                String strFormat = (String) data[i][6];
                String strCurve = (String) data[i][7];

                double min1 = Double.NaN;
                double max1 = Double.NaN;
                double min2 = Double.NaN;
                double max2 = Double.NaN;

                try {
                    if (strMin1.length() > 0) {
                        min1 = Double.parseDouble(strMin1);
                    }
                    if (strMax1.length() > 0) {
                        max1 = Double.parseDouble(strMax1);
                    }
                    if (strMin2.length() > 0) {
                        min2 = Double.parseDouble(strMin2);
                    }
                    if (strMax2.length() > 0) {
                        max2 = Double.parseDouble(strMax2);
                    }

                    double doubleValue = Double.parseDouble(value);
                    double newValue = Double.NaN;

                    if (!Double.isNaN(min1)) {
                        if (!Double.isNaN(max1)) {
                            doubleValue = Math.max(Math.min(min1, max1), doubleValue);
                        } else {
                            doubleValue = Math.max(min1, doubleValue);
                        }
                    }
                    if (!Double.isNaN(max1)) {
                        if (!Double.isNaN(min1)) {
                            doubleValue = Math.min(Math.max(min1, max1), doubleValue);
                        } else {
                            doubleValue = Math.min(min1, doubleValue);
                        }
                    }

                    if (!Double.isNaN(min1) && !Double.isNaN(min2)) {
                        if (!Double.isNaN(max1) && !Double.isNaN(max2)) {
                            if (max1 >= min1) {
                                if (max2 > min2) {
                                    newValue = min2 + (max2 - min2) * applyCurve(((doubleValue - min1) / (max1 - min1)), strCurve);
                                } else {
                                    newValue = min2 - (min2 - max2) * applyCurve(((doubleValue - min1) / (max1 - min1)), strCurve);
                                }
                            } else {
                                if (max2 > min2) {
                                    newValue = min2 + (max2 - min2) * applyCurve(((min1 - doubleValue) / (min1 - max1)), strCurve);
                                    ;
                                } else {
                                    newValue = min2 - (min2 - max2) * applyCurve(((min1 - doubleValue) / (min1 - max1)), strCurve);
                                }
                            }
                        } else if (Double.isNaN(max2)) {
                            newValue = min2 + (doubleValue - min1);
                        } else {
                            newValue = doubleValue;
                        }
                    } else {
                        newValue = doubleValue;
                    }

                    if (!Double.isNaN(min2)) {
                        if (!Double.isNaN(max2)) {
                            doubleValue = Math.max(Math.min(min2, max2), doubleValue);
                        } else {
                            doubleValue = Math.max(min2, doubleValue);
                        }
                    }
                    if (!Double.isNaN(max2)) {
                        if (!Double.isNaN(min2)) {
                            doubleValue = Math.min(Math.max(min2, max2), doubleValue);
                        } else {
                            doubleValue = Math.min(min2, doubleValue);
                        }
                    }

                    if (!Double.isNaN(doubleValue)) {
                        DecimalFormat df = new DecimalFormat(strFormat, new DecimalFormatSymbols(Locale.US));
                        String strNumber = strFormat.length() > 0 ? df.format(newValue) : "" + newValue;

                        if (var.equals(property)) {
                            if (VariablesBlackboard.getInstance() != null) {
                                String strOldValue = lastUpdateVariable.get(_var);
                                if (strOldValue == null || !strNumber.equals(strOldValue)) {
                                    Integer intValue = new Integer(i);
                                    ignoreRows.add(intValue);
                                    // VariablesBlackboard.variablesServer.updateVariableIfDifferent(_var, strNumber);
                                    Commands.updateVariableOrProperty(this.properties, _var, strNumber, Commands.ACTION_VARIABLE_UPDATE, true);
                                    ignoreRows.remove(intValue);
                                }
                                lastUpdateVariable.put(_var, strNumber);
                            }
                        } else {
                            if (VariablesBlackboard.getInstance() != null) {
                                String strOldValue = lastUpdateVariable.get(property);
                                if (strOldValue == null || !strNumber.equals(strOldValue)) {
                                    // VariablesBlackboard.variablesServer.updateVariable(property, strNumber);
                                    ignoreRows.add(new Integer(i));
                                    properties.setProperty(property.replace("@property:", ""), strNumber);
                                    this.changePerformed(property, strNumber);
                                    ignoreRows.remove(ignoreRows.lastElement());
                                }
                                lastUpdateVariable.put(property, strNumber);
                            }
                            if (setPanel != null && !setPanel.bUpdating) {
                                int r = properties.getPropertyRow(property);
                                if (r >= 0) {
                                    setPanel.model.fireTableRowsUpdated(r, r);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                }
            }
            ignoreVars.remove("@property:" + property);
        }
        ignoreVars.remove(var);
    }

    public double applyCurve(double value, String strCurve) {
        if (!strCurve.isEmpty()) {
            Curve curve = Curves.getGlobalCurves().getCurve(strCurve);
            if (curve != null) {
                return curve.getRelativeValue(value);
            }
        }
        return value;
    }
}
