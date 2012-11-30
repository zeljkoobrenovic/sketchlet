package net.sf.sketchlet.designer.editor.ui.eventpanels;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.ui.ActionDialogUtils;
import net.sf.sketchlet.designer.editor.ui.region.AbstractEventsPanel;
import net.sf.sketchlet.designer.editor.ui.region.AddActionRunnable;
import net.sf.sketchlet.framework.model.events.EventMacroFactory;
import net.sf.sketchlet.framework.model.events.mouse.MouseEventMacro;
import net.sf.sketchlet.framework.model.events.mouse.MouseProcessor;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * @author zeljko
 */
public class MouseEventsPanel extends AbstractEventsPanel {
    private MouseProcessor mouseProcessor;

    public MouseEventsPanel(final MouseProcessor mouseProcessor) {
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
                return mouseProcessor.getMouseEventMacros();
            }
        });
        this.mouseProcessor = mouseProcessor;
    }

    private MouseEventMacro mouseEventsMacro;

    public MouseEventMacro addNewActionToMouseEventMacro() {
        final JComboBox events = new JComboBox();
        events.setEditable(false);
        for (String event : MouseProcessor.MOUSE_EVENT_TYPES) {
            if (mouseProcessor.getMouseEventMacro(event) == null) {
                events.addItem(event);
            }
        }
        if (events.getItemCount() > 0) {
            Runnable onOk = new Runnable() {
                @Override
                public void run() {
                    String event = (String) events.getSelectedItem();
                    if (event != null) {
                        mouseEventsMacro = mouseProcessor.getMouseEventMacro(event);

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
        for (String event : MouseProcessor.MOUSE_EVENT_TYPES) {
            events.addItem(event);
        }
        AddActionRunnable onOk = new AddActionRunnable() {
            @Override
            public void addAction(String action, String param1, String param2) {
                String event = (String) events.getSelectedItem();
                if (event != null) {
                    MouseEventMacro mouseEventsMacro = mouseProcessor.getMouseEventMacro(event);

                    if (mouseEventsMacro == null) {
                        mouseEventsMacro = new MouseEventMacro(event);
                        mouseProcessor.getMouseEventMacros().add(mouseEventsMacro);
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
