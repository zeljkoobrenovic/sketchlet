package net.sf.sketchlet.framework.blackboard;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.blackboard.evaluator.CellReferenceResolver;
import net.sf.sketchlet.framework.blackboard.evaluator.Evaluator;
import net.sf.sketchlet.framework.blackboard.evaluator.JEParser;
import net.sf.sketchlet.loaders.pluginloader.PluginInstance;
import net.sf.sketchlet.framework.model.ActiveRegions;
import net.sf.sketchlet.framework.model.PageVariable;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.programming.macros.Commands;
import net.sf.sketchlet.plugin.VariableSpacePlugin;
import org.apache.log4j.Logger;

/**
 * @author zeljko
 */
public class VariableSpacesHandler {
    private static final Logger log = Logger.getLogger(VariableSpacesHandler.class);

    public void prepareAdditionalVariables() {

        VariablesBlackboard.getInstance().addAdditionalVariables(new AdditionalVariables() {
            public Variable getVariable(String variableName) {
                Page page = Workspace.getPage();
                for (PageVariable pageVariable : page.getPageVariables()) {
                    if (pageVariable.getName().equalsIgnoreCase(variableName)) {
                        Variable v = new Variable() {
                            @Override
                            public void save() {
                                savePageVariable(getName(), getValue());
                            }
                        };
                        v.setName(pageVariable.getName());
                        v.setValue(pageVariable.getValue());
                        v.setFormat(pageVariable.getFormat());
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
        VariablesBlackboard.getInstance().addAdditionalVariables(new AdditionalVariables() {

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


        VariablesBlackboard.getInstance().addAdditionalVariables(new AdditionalVariables() {

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

        VariablesBlackboard.getInstance().addAdditionalVariables(new AdditionalVariables() {

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

                    strReference = VariablesBlackboard.populateTemplateSimple(strReference, false);
                    strReference = Evaluator.processRegionReferences(regions, strReference);
                    try {
                        String expression = "";
                        int col = 0;
                        int row = 0;
                        if (VariablesBlackboard.getInstance().variableExists(strReference)) {
                            return VariablesBlackboard.getInstance().getVariableValue(strReference);
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
        for (PageVariable pageVariable : page.getPageVariables()) {
            if (pageVariable.getName().equalsIgnoreCase(variableName)) {
                pageVariable.setValue(value);
                SketchletEditor.getInstance().getPageVariablesPanel().refreshComponents();
                break;
            }
        }
    }
}
