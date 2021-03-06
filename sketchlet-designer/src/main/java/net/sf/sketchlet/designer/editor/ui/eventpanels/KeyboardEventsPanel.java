package net.sf.sketchlet.designer.editor.ui.eventpanels;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.ui.ActionDialogUtils;
import net.sf.sketchlet.designer.editor.ui.region.AbstractEventsPanel;
import net.sf.sketchlet.designer.editor.ui.region.AddActionRunnable;
import net.sf.sketchlet.framework.model.events.EventMacroFactory;
import net.sf.sketchlet.framework.model.events.keyboard.KeyEvents;
import net.sf.sketchlet.framework.model.events.keyboard.KeyboardEventMacro;
import net.sf.sketchlet.framework.model.events.keyboard.KeyboardEventsProcessor;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author zeljko
 */
public class KeyboardEventsPanel extends AbstractEventsPanel {
    private KeyboardEventsProcessor keyboardEventsProcessor;

    public KeyboardEventsPanel(final KeyboardEventsProcessor keyboardEventsProcessor) {
        super();
        this.keyboardEventsProcessor = keyboardEventsProcessor;
        setEventMacroFactory(new EventMacroFactory<KeyboardEventMacro>() {
            @Override
            public KeyboardEventMacro getNewEventMacroInstance(String... args) {
                return KeyboardEventsPanel.this.addNewKeyboardEventMacro();
            }

            @Override
            public String getEventTypeName(boolean plural) {
                return plural ? "keyboard events" : "keyboard event";
            }

            @Override
            public List<KeyboardEventMacro> getEventMacroList() {
                return keyboardEventsProcessor.getKeyboardEventMacros();
            }

            @Override
            public String getEventDescription(KeyboardEventMacro eventMacro) {
                return (eventMacro.getModifiers() + " " + eventMacro.getKey()).trim() + " " + eventMacro.getEventName();
            }
        });
    }

    private KeyboardEventMacro keyboardEventMacro;

    public KeyboardEventMacro addNewKeyboardEventMacro() {
        final JCheckBox ctrl = new JCheckBox("Ctrl");
        final JCheckBox alt = new JCheckBox("Alt");
        final JCheckBox shift = new JCheckBox("Shift");
        final JComboBox events = new JComboBox();
        final JComboBox keys = KeyEvents.getComboBox();
        events.setEditable(true);
        events.addItem("pressed");
        events.addItem("released");
        events.addItem("hold 1");
        events.addItem("hold 1*");
        Runnable onOk = new Runnable() {
            @Override
            public void run() {
                keyboardEventMacro = new KeyboardEventMacro((String) events.getSelectedItem());
                keyboardEventMacro.setModifiers(((shift.isSelected() ? "SHIFT " : "") + (ctrl.isSelected() ? "CTRL " : "") + (alt.isSelected() ? "ALT " : "")).trim());
                keyboardEventMacro.setKey((String) keys.getSelectedItem());
            }
        };
        Component components[] = new Component[]{new JLabel(Language.translate("")), shift,
                new JLabel(Language.translate("")), ctrl,
                new JLabel(Language.translate("")), alt,
                new JLabel(Language.translate("Key")), keys,
                new JLabel(Language.translate("Event")), events};
        ActionDialogUtils.openAddActionDialog(Language.translate("Keyboard Event Action"), components, onOk);

        return keyboardEventMacro;
    }

    public void addNewEventMacro(String command, String param1, String param2) {
        final JCheckBox ctrl = new JCheckBox("Ctrl");
        final JCheckBox alt = new JCheckBox("Alt");
        final JCheckBox shift = new JCheckBox("Shift");
        final JComboBox events = new JComboBox();
        final JComboBox keys = KeyEvents.getComboBox();
        events.setEditable(false);
        events.addItem("pressed");
        events.addItem("released");
        AddActionRunnable onOk = new AddActionRunnable() {
            @Override
            public void addAction(String action, String param1, String param2) {
                keyboardEventMacro = new KeyboardEventMacro((String) events.getSelectedItem());
                keyboardEventMacro.setModifiers(((shift.isSelected() ? "SHIFT " : "") + (ctrl.isSelected() ? "CTRL " : "") + (alt.isSelected() ? "ALT " : "")).trim());
                keyboardEventMacro.setKey((String) keys.getSelectedItem());
                keyboardEventMacro.getMacro().addLine(action, param1, param2);
                if (keyboardEventMacro.getMacro().panel != null) {
                    keyboardEventMacro.getMacro().panel.reload();
                }
                keyboardEventsProcessor.getKeyboardEventMacros().add(keyboardEventMacro);
                refresh();
            }
        };
        Component components[] = new Component[]{new JLabel(Language.translate("")), shift,
                new JLabel(Language.translate("")), ctrl,
                new JLabel(Language.translate("")), alt,
                new JLabel(Language.translate("Key")), keys,
                new JLabel(Language.translate("Event")), events};
        ActionDialogUtils.openAddActionDialog(Language.translate("Keyboard Event Action"), components, onOk, command, param1, param2);
    }
}
