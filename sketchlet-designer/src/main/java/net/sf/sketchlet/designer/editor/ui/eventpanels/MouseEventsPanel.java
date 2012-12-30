package net.sf.sketchlet.designer.editor.ui.eventpanels;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.ui.ActionDialogUtils;
import net.sf.sketchlet.designer.editor.ui.region.AbstractEventsPanel;
import net.sf.sketchlet.designer.editor.ui.region.AddActionRunnable;
import net.sf.sketchlet.framework.model.events.EventMacroFactory;
import net.sf.sketchlet.framework.model.events.mouse.MouseEventMacro;
import net.sf.sketchlet.framework.model.events.mouse.MouseEventsProcessor;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author zeljko
 */
public class MouseEventsPanel extends AbstractEventsPanel {
    private MouseEventsProcessor mouseEventsProcessor;

    public MouseEventsPanel(final MouseEventsProcessor mouseEventsProcessor) {
        setEventMacroFactory(new EventMacroFactory<MouseEventMacro>() {
            @Override
            public MouseEventMacro getNewEventMacroInstance(String... args) {
                return MouseEventsPanel.this.addNewActionToMouseEventMacro();
            }

            @Override
            public String getEventTypeName(boolean plural) {
                return plural ? "mouse events" : "mouse event";
            }

            @Override
            public List<MouseEventMacro> getEventMacroList() {
                return mouseEventsProcessor.getMouseEventMacros();
            }
        });
        this.mouseEventsProcessor = mouseEventsProcessor;
    }

    private MouseEventMacro mouseEventsMacro;

    public MouseEventMacro addNewActionToMouseEventMacro() {
        final JComboBox events = new JComboBox();
        events.setEditable(false);
        for (String event : MouseEventsProcessor.MOUSE_EVENT_TYPES) {
            if (mouseEventsProcessor.getMouseEventMacro(event) == null) {
                events.addItem(event);
            }
        }
        if (events.getItemCount() > 0) {
            Runnable onOk = new Runnable() {
                @Override
                public void run() {
                    String event = (String) events.getSelectedItem();
                    if (event != null) {
                        mouseEventsMacro = mouseEventsProcessor.getMouseEventMacro(event);

                        if (mouseEventsMacro == null) {
                            mouseEventsMacro = new MouseEventMacro(event);
                        }
                    }
                }
            };

            Component components[] = new Component[]{new JLabel(Language.translate("Mouse Event: ")), events};
            ActionDialogUtils.openAddActionDialog(Language.translate("Mouse Event Action"), components, onOk);
            return mouseEventsMacro;
        } else {
            return null;
        }
    }

    public void addNewEventMacro(final String action, final String param1, final String param2) {
        final JComboBox events = new JComboBox();
        events.setEditable(false);
        for (String event : MouseEventsProcessor.MOUSE_EVENT_TYPES) {
            events.addItem(event);
        }
        AddActionRunnable onOk = new AddActionRunnable() {
            @Override
            public void addAction(String action, String param1, String param2) {
                String event = (String) events.getSelectedItem();
                if (event != null) {
                    MouseEventMacro mouseEventsMacro = mouseEventsProcessor.getMouseEventMacro(event);

                    if (mouseEventsMacro == null) {
                        mouseEventsMacro = new MouseEventMacro(event);
                        mouseEventsProcessor.getMouseEventMacros().add(mouseEventsMacro);
                    }
                    mouseEventsMacro.getMacro().addLine(action, param1, param2);
                    if (mouseEventsMacro.getMacro().panel != null) {
                        mouseEventsMacro.getMacro().panel.reload();
                    }
                    refresh();
                }
            }
        };
        Component components[] = new Component[]{new JLabel(Language.translate("Mouse Event: ")), events};
        ActionDialogUtils.openAddActionDialog(Language.translate("Mouse Event Action"), components, onOk, action, param1, param2);
    }
}
