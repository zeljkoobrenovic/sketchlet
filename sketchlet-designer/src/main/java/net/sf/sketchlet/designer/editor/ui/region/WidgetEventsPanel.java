package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.context.ActiveRegionContextImpl;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.loaders.pluginloader.WidgetPluginFactory;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.events.EventMacroFactory;
import net.sf.sketchlet.framework.model.events.widget.WidgetEventMacro;

import javax.swing.*;
import java.util.List;

/**
 * @author zeljko
 */
public class WidgetEventsPanel extends AbstractEventsPanel {

    public WidgetEventsPanel(final ActiveRegion region) {
        super(new EventMacroFactory<WidgetEventMacro>() {
            @Override
            public WidgetEventMacro getNewEventMacroInstance(String... args) {
                String selectionValues[] = WidgetPluginFactory.getActions(new ActiveRegionContextImpl(region, new PageContextImpl(Workspace.getPage())));
                Object value = JOptionPane.showInputDialog(SketchletEditor.getInstance().getExtraEditorPanel(), Language.translate("Select Widget Event:"),
                        "Widget Event", JOptionPane.PLAIN_MESSAGE, null,
                        selectionValues, selectionValues[0]);
                if (value != null) {
                    return new WidgetEventMacro(value.toString());
                } else {
                    return null;
                }
            }

            @Override
            public String getEventTypeName(boolean plural) {
                return plural ? "widget events" : "widget event";
            }

            @Override
            public List<WidgetEventMacro> getEventMacroList() {
                return region.getWidgetEventMacros();
            }
        });
    }
}
