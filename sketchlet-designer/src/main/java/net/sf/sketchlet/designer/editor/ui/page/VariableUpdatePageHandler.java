package net.sf.sketchlet.designer.editor.ui.page;

import net.sf.sketchlet.common.QuotedStringTokenizer;
import net.sf.sketchlet.blackboard.VariablesBlackboard;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.blackboard.evaluator.Evaluator;
import net.sf.sketchlet.model.Page;
import net.sf.sketchlet.model.VariableUpdateEventMacro;
import net.sf.sketchlet.model.events.hold.HoldData;
import net.sf.sketchlet.model.events.hold.HoldProcessor;
import net.sf.sketchlet.model.events.hold.HoldThreads;

import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 5-11-12
 * Time: 11:23
 * To change this template use File | Settings | File Templates.
 */
public class VariableUpdatePageHandler {
    private HoldThreads holdThreads = new HoldThreads();

    class EventHoldData extends HoldData {

        public EventHoldData(VariableUpdateEventMacro eventMacro) {
            this.eventMacro = eventMacro;
        }

        VariableUpdateEventMacro eventMacro;
    }

    class EventHoldProcessor extends HoldProcessor {

        public void process(HoldData data) {
            EventHoldData d = (EventHoldData) data;
            d.eventMacro.startMacro();
        }
    }

    public boolean process(String variable, String value) {
        Vector<String> removeOccuredIDs = new Vector<String>();
        boolean bProcessed = false;
        Page page = Workspace.getPage();
        for (VariableUpdateEventMacro variableUpdateEventMacro : page.getVariableUpdateEventMacros()) {
            String var = variableUpdateEventMacro.getVariable();
            String operator = variableUpdateEventMacro.getOperator();
            String val = variableUpdateEventMacro.getValue();
            Vector<String> vars1 = VariablesBlackboard.getVariablesInTemplate(var);
            Vector<String> vars2 = VariablesBlackboard.getVariablesInTemplate(val);

            var = Evaluator.processText(var, page.getVarPrefix(), page.getVarPostfix());
            val = Evaluator.processText(val, page.getVarPrefix(), page.getVarPostfix());

            value = VariablesBlackboard.getInstance().getVariableValue(var);

            if (!var.isEmpty() && (var.equals(variable) || vars1.contains(variable) || vars2.contains(variable))) {
                boolean bDoIt = false;

                if (operator.equalsIgnoreCase("updated")) {
                    bDoIt = true;
                } else {
                    try {
                        double val1 = Double.parseDouble(val);
                        double val2 = Double.parseDouble(value);

                        if (operator.equals("") || operator.equals("=")) {
                            bDoIt = val1 == val2;
                        } else if (operator.equals(">")) {
                            bDoIt = val2 > val1;
                        } else if (operator.equals(">=")) {
                            bDoIt = val2 >= val1;
                        } else if (operator.equals("<")) {
                            bDoIt = val2 < val1;
                        } else if (operator.equals("<=")) {
                            bDoIt = val2 <= val1;
                        } else if (operator.equals("!=") || operator.equals("<>")) {
                            bDoIt = val2 != val1;
                        }
                    } catch (Exception e) {
                        if (operator.equals("") || operator.equals("=")) {
                            bDoIt = val.equalsIgnoreCase(value);
                        } else if (operator.equals(">")) {
                            bDoIt = value.compareTo(val) > 0;
                        } else if (operator.equals(">=")) {
                            bDoIt = value.compareTo(val) >= 0;
                        } else if (operator.equals("<")) {
                            bDoIt = value.compareTo(val) < 0;
                        } else if (operator.equals("<=")) {
                            bDoIt = value.compareTo(val) <= 0;
                        } else if (operator.equals("!=") || operator.equals("<>")) {
                            bDoIt = value.compareTo(val) != 0;
                        } else if (operator.equalsIgnoreCase("in")) {
                            bDoIt = isIn(val, value);
                        } else if (operator.equalsIgnoreCase("not in")) {
                            bDoIt = !isIn(val, value);
                        }
                    }
                }

                String strFormula = variableUpdateEventMacro.getVariable() + variableUpdateEventMacro.getOperator() + variableUpdateEventMacro.getValue();
                String type = Evaluator.processText(variableUpdateEventMacro.getEventName(), page.getVarPrefix(), page.getVarPostfix());

                if (bDoIt) {
                    bProcessed = true;

                    if (type.isEmpty()) {
                        variableUpdateEventMacro.startMacro();
                    } else if (type.equals("occurred") || type.equals("occured")) {
                        if (operator.equalsIgnoreCase("updated") || !this.holdThreads.hasOccured(strFormula)) {
                            variableUpdateEventMacro.startMacro();
                        }
                    } else if (type.startsWith("lasted ")) {
                        if (!this.holdThreads.hasOccured(strFormula + " " + type)) {
                            holdThreads.setOccured(strFormula + " " + type);
                            try {
                                boolean bRepeat = type.endsWith("*");
                                if (bRepeat) {
                                    type = type.substring(0, type.length() - 1).trim();
                                }
                                int timeMs = (int) (Double.parseDouble(type.substring(7).trim()) * 1000);
                                this.holdThreads.processHold(timeMs, new EventHoldData(variableUpdateEventMacro), new EventHoldProcessor(), strFormula, bRepeat);
                            } catch (Exception e2) {
                            }
                        }
                    }
                    holdThreads.setOccured(strFormula);
                } else {
                    if (type.equals("ended") && holdThreads.hasOccured(strFormula)) {
                        variableUpdateEventMacro.startMacro();
                    }

                    removeOccuredIDs.add(strFormula);
                }
            }
        }

        for (String strID : removeOccuredIDs) {
            holdThreads.stopHold(strID);
            holdThreads.removeOccured(strID);
        }

        return bProcessed;
    }

    public static boolean isIn(String val, String value) {
        val = val.replace(",", " ").replace(";", " ");
        val = val.replace("  ", " ");
        String values[] = QuotedStringTokenizer.parseArgs(val);
        boolean bDoIt = false;
        for (int s = 0; s < values.length; s++) {
            String range[] = values[s].split("-");
            if (range.length == 1) {
                if (values[s].equalsIgnoreCase(value)) {
                    bDoIt = true;
                    break;
                }
            } else if (range.length == 2) {
                try {
                    double nVal = Double.parseDouble(value);
                    double nVal1 = Double.parseDouble(range[0]);
                    double nVal2 = Double.parseDouble(range[1]);

                    if (nVal >= nVal1 && nVal <= nVal2) {
                        bDoIt = true;
                        break;
                    }
                } catch (Exception e2) {
                }
            }
        }
        return bDoIt;
    }
}
