package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.EventMacroFactory;
import net.sf.sketchlet.model.events.WidgetEventMacro;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 6-10-12
 * Time: 17:42
 * To change this template use File | Settings | File Templates.
 */
public class WidgetEventsPanel extends AbstractEventsPanel {

    public WidgetEventsPanel(final ActiveRegion region) {
        super(new EventMacroFactory<WidgetEventMacro>() {
            @Override
            public WidgetEventMacro getNewEventMacroInstance(String... args) {
                return new WidgetEventMacro(args[0]);
            }

            @Override
            public String getEventTypeName(boolean plural) {
                return plural ? "widget events" : "widget event";
            }

            @Override
            public List<WidgetEventMacro> getEventMacroList() {
                return region.widgetEventMacros;
            }
        });
    }
}
