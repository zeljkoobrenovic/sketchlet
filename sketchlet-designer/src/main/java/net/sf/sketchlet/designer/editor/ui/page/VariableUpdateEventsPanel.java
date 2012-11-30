package net.sf.sketchlet.designer.editor.ui.page;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.ActionDialogUtils;
import net.sf.sketchlet.designer.editor.ui.region.AbstractEventsPanel;
import net.sf.sketchlet.designer.editor.ui.region.AddActionRunnable;
import net.sf.sketchlet.framework.model.events.EventMacroFactory;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.events.variable.VariableUpdateEventMacro;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author zeljko
 */
public class VariableUpdateEventsPanel extends AbstractEventsPanel {
    private Page page;

    public VariableUpdateEventsPanel(final Page page) {
        super();
        this.page = page;
        setEventMacroFactory(new EventMacroFactory<VariableUpdateEventMacro>() {
            @Override
            public VariableUpdateEventMacro getNewEventMacroInstance(String... args) {
                return VariableUpdateEventsPanel.this.addNewVariableUpdateEventMacro();
            }

            @Override
            public String getEventTypeName(boolean plural) {
                return plural ? "variable update events" : "variable update event";
            }

            @Override
            public List<VariableUpdateEventMacro> getEventMacroList() {
                return SketchletEditor.getInstance().getPage().getVariableUpdateEventMacros();
            }

            @Override
            public String getEventDescription(VariableUpdateEventMacro eventMacro) {
                return eventMacro.getVariable()
                        + " "
                        + eventMacro.getOperator()
                        + " "
                        + eventMacro.getValue()
                        + " "
                        + eventMacro.getEventName();
            }
        });
    }

    private VariableUpdateEventMacro variableUpdateEventMacro;
    private JComboBox variables;
    private JComboBox operatorComboBox;
    private JTextField value;
    JComboBox events;

    private Component[] getDialogComponents() {
        variables = new JComboBox();
        variables.setEditable(true);
        for (String variableName : SketchletContext.getInstance().getVariablesBlackboardContext().getVariableNames()) {
            variables.addItem(variableName);
        }

        operatorComboBox = new JComboBox();
        operatorComboBox.setEditable(false);
        operatorComboBox.addItem("=");
        operatorComboBox.addItem(">");
        operatorComboBox.addItem(">=");
        operatorComboBox.addItem("<");
        operatorComboBox.addItem("<=");
        operatorComboBox.addItem("<>");
        operatorComboBox.addItem("in");
        operatorComboBox.addItem("not in");
        operatorComboBox.addItem("updated");

        value = new JTextField();

        events = new JComboBox();
        events.setEditable(true);
        events.addItem("occurred");
        events.addItem("ended");
        events.addItem("lasted 1");
        events.addItem("lasted 1*");

        return new Component[]{new JLabel(Language.translate("Variable")), variables,
                new JLabel(Language.translate("Operator")), operatorComboBox,
                new JLabel(Language.translate("Value")), value,
                new JLabel(Language.translate("Event")), events};
    }

    public VariableUpdateEventMacro addNewVariableUpdateEventMacro() {

        Runnable onOk = new Runnable() {
            @Override
            public void run() {
                variableUpdateEventMacro = new VariableUpdateEventMacro((String) events.getSelectedItem());
                variableUpdateEventMacro.setVariable((String) variables.getSelectedItem());
                variableUpdateEventMacro.setOperator((String) operatorComboBox.getSelectedItem());
                variableUpdateEventMacro.setValue(value.getText());
            }
        };

        ActionDialogUtils.openAddActionDialog(Language.translate("Keyboard Event Action"), getDialogComponents(), onOk);

        return variableUpdateEventMacro;
    }

    public void addNewEventMacro(String command, String param1, String param2) {

        AddActionRunnable onOk = new AddActionRunnable() {
            @Override
            public void addAction(String action, String param1, String param2) {
                variableUpdateEventMacro = new VariableUpdateEventMacro((String) events.getSelectedItem());
                variableUpdateEventMacro.setVariable((String) variables.getSelectedItem());
                variableUpdateEventMacro.setOperator((String) operatorComboBox.getSelectedItem());
                variableUpdateEventMacro.setValue(value.getText());
                variableUpdateEventMacro.getMacro().addLine(action, param1, param2);
                if (variableUpdateEventMacro.getMacro().panel != null) {
                    variableUpdateEventMacro.getMacro().panel.reload();
                }
                page.getVariableUpdateEventMacros().add(variableUpdateEventMacro);
                refresh();
            }
        };

        ActionDialogUtils.openAddActionDialog(Language.translate("Variable Update Event Action"), getDialogComponents(), onOk, command, param1, param2);
    }
}
