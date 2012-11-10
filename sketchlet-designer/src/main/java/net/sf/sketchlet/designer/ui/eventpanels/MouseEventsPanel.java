package net.sf.sketchlet.designer.ui.eventpanels;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.data.EventMacroFactory;
import net.sf.sketchlet.designer.data.MouseEventMacro;
import net.sf.sketchlet.designer.data.MouseProcessor;
import net.sf.sketchlet.designer.ui.ActionDialogUtils;
import net.sf.sketchlet.designer.ui.region.AbstractEventsPanel;
import net.sf.sketchlet.designer.ui.region.AddActionRunnable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 11-10-12
 * Time: 9:13
 * To change this template use File | Settings | File Templates.
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
                return mouseProcessor.mouseEventMacros;
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
                        mouseProcessor.mouseEventMacros.add(mouseEventsMacro);
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
