package net.sf.sketchlet.framework.renderer.regions;

import net.sf.sketchlet.designer.context.ActiveRegionContextImpl;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.loaders.pluginloader.PluginInstance;
import net.sf.sketchlet.loaders.pluginloader.WidgetPluginFactory;
import net.sf.sketchlet.loaders.pluginloader.WidgetPluginHandler;
import net.sf.sketchlet.plugin.WidgetPlugin;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

/**
 * @author zobrenovic
 */
public class WidgetLayer extends DrawingLayer {
    private WidgetPlugin widgetPlugin = null;
    private PluginInstance widgetPluginInstance = null;

    public WidgetLayer(ActiveRegion region) {
        super(region);
    }

    public void dispose() {
        region = null;
        if (getWidgetPlugin() != null) {
            getWidgetPlugin().setActiveRegionContext(null);
            getWidgetPlugin().dispose();
            setWidgetPlugin(null);
        }
    }

    public void draw(Graphics2D g2, Component component, boolean bPlayback) {
        ActiveRegion region = this.region;
        if (region == null) {
            return;
        }
        if (!region.getWidget().isEmpty()) {
            String strControl = region.processText(region.getWidget().trim());
            if (!strControl.isEmpty()) {
                WidgetPlugin widgetPlugin = getWidgetPlugin();
                if (widgetPlugin == null && WidgetPluginFactory.exists(strControl)) {
                    if (region.getParent() != null) {
                        widgetPluginInstance = WidgetPluginFactory.getWidgetPluginInstance(new ActiveRegionContextImpl(region, new PageContextImpl(region.getParent().getPage())));
                    } else {
                        widgetPluginInstance = WidgetPluginFactory.getWidgetPluginInstance(new ActiveRegionContextImpl(region, new PageContextImpl(new Page("", ""))));
                    }
                    widgetPlugin = (WidgetPlugin) widgetPluginInstance.getInstance();
                    setWidgetPlugin(widgetPlugin);
                    widgetPlugin.activate(bPlayback);
                } else if (widgetPlugin != null && !WidgetPluginHandler.getName(widgetPluginInstance).equalsIgnoreCase(strControl)) {
                    widgetPluginInstance = WidgetPluginFactory.getWidgetPluginInstance(new ActiveRegionContextImpl(region, new PageContextImpl(region.getParent().getPage())));
                    widgetPlugin = (WidgetPlugin) widgetPluginInstance.getInstance();
                    setWidgetPlugin(widgetPlugin);
                    widgetPlugin.activate(bPlayback);
                }
                if (widgetPlugin != null) {
                    WidgetPluginHandler.injectWidgetPropertiesValues(widgetPlugin);
                    AffineTransform t = g2.getTransform();
                    g2.translate(region.getX1Value(), region.getY1Value());
                    widgetPlugin.paint(g2);
                    g2.setTransform(t);
                }
            }
        } else {
            setWidgetPlugin(null);
        }
    }

    public void mousePressed(MouseEvent e, int x, int y) {
        if (getWidgetPlugin() != null) {
            x -= this.region.getX1Value();
            y -= this.region.getY1Value();
            WidgetPlugin.setActiveWidget(getWidgetPlugin());
            WidgetPluginHandler.injectWidgetPropertiesValues(getWidgetPlugin());
            getWidgetPlugin().mousePressed(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        }
    }

    public void mouseReleased(MouseEvent e, int x, int y) {
        if (getWidgetPlugin() != null) {
            x -= this.region.getX1Value();
            y -= this.region.getY1Value();
            WidgetPluginHandler.injectWidgetPropertiesValues(getWidgetPlugin());
            getWidgetPlugin().mouseReleased(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        }
    }

    public void mouseDragged(MouseEvent e, int x, int y) {
        if (getWidgetPlugin() != null) {
            x -= this.region.getX1Value();
            y -= this.region.getY1Value();
            WidgetPluginHandler.injectWidgetPropertiesValues(getWidgetPlugin());
            MouseEvent me = new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton());
            getWidgetPlugin().mouseDragged(me);
            if (me.isConsumed()) {
                e.consume();
            }
        }
    }

    public void mouseMoved(MouseEvent e, int x, int y) {
        if (getWidgetPlugin() != null) {
            x -= this.region.getX1Value();
            y -= this.region.getY1Value();
            WidgetPluginHandler.injectWidgetPropertiesValues(getWidgetPlugin());
            getWidgetPlugin().mouseMoved(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        }
    }

    public WidgetPlugin getWidgetPlugin() {
        return widgetPlugin;
    }

    public void setWidgetPlugin(WidgetPlugin widgetPlugin) {
        this.widgetPlugin = widgetPlugin;
    }
}
