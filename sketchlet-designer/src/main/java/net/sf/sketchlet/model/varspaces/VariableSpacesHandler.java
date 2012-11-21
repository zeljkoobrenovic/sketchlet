package net.sf.sketchlet.model.varspaces;

import net.sf.sketchlet.communicator.server.AdditionalVariables;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.communicator.server.Variable;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.model.evaluator.CellReferenceResolver;
import net.sf.sketchlet.model.evaluator.Evaluator;
import net.sf.sketchlet.model.evaluator.JEParser;
import net.sf.sketchlet.loaders.pluginloader.PluginInstance;
import net.sf.sketchlet.model.ActiveRegions;
import net.sf.sketchlet.model.LocalVariable;
import net.sf.sketchlet.model.Page;
import net.sf.sketchlet.model.programming.macros.Commands;
import net.sf.sketchlet.plugin.VariableSpacePlugin;
import org.apache.log4j.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 13-11-12
 * Time: 16:18
 * To change this template use File | Settings | File Templates.
 */
public class VariableSpacesHandler {
    private static final Logger log = Logger.getLogger(VariableSpacesHandler.class);

    public void prepareAdditionalVariables() {

        DataServer.getInstance().addAdditionalVariables(new AdditionalVariables() {
            public Variable getVariable(String variableName) {
                Page page = Workspace.getPage();
                for (LocalVariable localVariable : page.getLocalVariables()) {
                    if (localVariable.getName().equalsIgnoreCase(variableName)) {
                        Variable v = new Variable() {
                            @Override
                            public void save() {
                                savePageVariable(getName(), getValue());
                            }
                        };
                        v.setName(localVariable.getName());
                        v.setValue(localVariable.getValue());
                        v.setFormat(localVariable.getFormat());
                        v.setTimestamp(v.getTimestamp() - 1);
                        return v;
                    }
                }

                return null;
            }


            public void updateVariable(String variableName, String value) {
                savePageVariable(variableName, value);
            }
        });
        DataServer.getInstance().addAdditionalVariables(new AdditionalVariables() {

            @Override
            public Variable getVariable(String name) {
                if (!name.isEmpty()) {
                    String strCol = name.substring(0, 1);
                    if (strCol.charAt(0) >= 'A' && strCol.charAt(0) <= 'Z') {
                        try {
                            String strRow = name.substring(1);
                            final int col = strCol.charAt(0) - 'A' + 1;
                            final int row = Integer.parseInt(strRow) - 1;
                            Variable v = new Variable() {

                                @Override
                                public void save() {
                                    if (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getSpreadsheetPanel() != null) {
                                        SketchletEditor.getInstance().getSpreadsheetPanel().model.setValueAt(this.getValue(), row, col);
                                    }
                                }
                            };
                            v.setTimestamp(v.getTimestamp() - 1);
                            v.setName(name);
                            String strValue = Workspace.getPage().getSpreadsheetCellValue(row, col);
                            if (strValue.startsWith("=")) {
                                strValue = Evaluator.processText(strValue, "", "");
                                while (strValue.startsWith("=")) {
                                    strValue = Evaluator.processText(strValue, "", "");
                                }
                            }
                            v.setValue(strValue);
                            return v;
                        } catch (Exception e) {
                            //log.error(e);
                        }
                    }
                }
                return null;
            }
        });


        DataServer.getInstance().addAdditionalVariables(new AdditionalVariables() {

            @Override
            public Variable getVariable(String id) {
                if (!id.isEmpty()) {
                    for (int i = 0; i < Workspace.getVariableSourcesNames().size(); i++) {
                        String dsName = Workspace.getVariableSourcesNames().get(i);
                        if (id.startsWith(dsName)) {
                            final PluginInstance ds = Workspace.getVariableSpaces().get(i);
                            if (ds.getInstance() instanceof VariableSpacePlugin) {
                                id = id.substring(dsName.length() + 1);
                                try {
                                    Variable v = new Variable() {

                                        @Override
                                        public void save() {
                                            ((VariableSpacePlugin) ds.getInstance()).update(getName(), getValue());
                                        }
                                    };
                                    v.setTimestamp(v.getTimestamp() - 1);
                                    v.setName(id);
                                    String strValue = ((VariableSpacePlugin) ds.getInstance()).evaluate(id);
                                    if (strValue.startsWith("=")) {
                                        strValue = Evaluator.processText(strValue, "", "");
                                        while (strValue.startsWith("=")) {
                                            strValue = Evaluator.processText(strValue, "", "");
                                        }
                                    }
                                    v.setValue(strValue);
                                    return v;
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
                return null;
            }
        });

        DataServer.getInstance().addAdditionalVariables(new AdditionalVariables() {

            @Override
            public Variable getVariable(String name) {
                name = name.trim();
                if (name.startsWith("[") && name.endsWith("]")) {
                    try {
                        Variable v = new Variable() {

                            @Override
                            public void save() {
                                Commands.updateVariableOrProperty(this, getName(), getValue(), Commands.ACTION_VARIABLE_UPDATE);
                            }
                        };
                        v.setTimestamp(v.getTimestamp() - 1);
                        v.setName(name);
                        ActiveRegions regions = Workspace.getPage().getRegions();
                        v.setValue(Evaluator.processRegionReferences(regions, name));
                        return v;
                    } catch (Exception e) {
                        log.error(e);
                    }

                }
                return null;
            }
        });

        JEParser.setResolver(new CellReferenceResolver() {

            @Override
            public String getValue(String strReference) {
                if (!strReference.isEmpty() && SketchletEditor.getInstance() != null) {
                    ActiveRegions regions = Workspace.getPage().getRegions();

                    strReference = DataServer.populateTemplateSimple(strReference, false);
                    strReference = Evaluator.processRegionReferences(regions, strReference);
                    try {
                        String expression = "";
                        int col = 0;
                        int row = 0;
                        if (DataServer.getInstance().variableExists(strReference)) {
                            return DataServer.getInstance().getVariableValue(strReference);
                        }
                        String prevValue = Workspace.getPage().getSpreadsheetCellValue(row, col);
                        if (!expression.equals(prevValue)) {
                            if (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getSpreadsheetPanel() != null) {
                                SketchletEditor.getInstance().getSpreadsheetPanel().model.fireTableCellUpdated(row, col);
                            }
                        }
                        return expression;
                    } catch (Exception e) {
                    }
                }
                return "";
            }
        });
    }

    private static void savePageVariable(String variableName, String value) {
        Page page = Workspace.getPage();
        for (LocalVariable localVariable : page.getLocalVariables()) {
            if (localVariable.getName().equalsIgnoreCase(variableName)) {
                localVariable.setValue(value);
                SketchletEditor.getInstance().getPageVariablesPanel().refreshComponents();
                break;
            }
        }
    }
}
