/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.renderer.regions;

import net.sf.sketchlet.designer.context.ActiveRegionContextImpl;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.loaders.pluginloader.PluginInstance;
import net.sf.sketchlet.loaders.pluginloader.WidgetPluginFactory;
import net.sf.sketchlet.loaders.pluginloader.WidgetPluginHandler;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.Page;
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
        if (region == null) {
            return;
        }
        if (!region.strWidget.isEmpty()) {
            String strControl = region.processText(region.strWidget.trim());
            if (!strControl.isEmpty()) {
                if (getWidgetPlugin() == null && WidgetPluginFactory.exists(strControl)) {
                    if (region.parent != null) {
                        widgetPluginInstance = WidgetPluginFactory.getWidgetPluginInstance(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.getPage())));
                    } else {
                        widgetPluginInstance = WidgetPluginFactory.getWidgetPluginInstance(new ActiveRegionContextImpl(region, new PageContextImpl(new Page("", ""))));
                    }
                    setWidgetPlugin((WidgetPlugin) widgetPluginInstance.getInstance());
                    getWidgetPlugin().activate(bPlayback);
                } else if (getWidgetPlugin() != null && !WidgetPluginHandler.getName(widgetPluginInstance).equalsIgnoreCase(strControl)) {
                    widgetPluginInstance = WidgetPluginFactory.getWidgetPluginInstance(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.getPage())));
                    setWidgetPlugin((WidgetPlugin) widgetPluginInstance.getInstance());
                    getWidgetPlugin().activate(bPlayback);
                }
                if (getWidgetPlugin() != null) {
                    WidgetPluginHandler.injectWidgetPropertiesValues(getWidgetPlugin());
                    AffineTransform t = g2.getTransform();
                    g2.translate(region.getX1(), region.getY1());
                    getWidgetPlugin().paint(g2);
                    g2.setTransform(t);
                }
            }
        } else {
            setWidgetPlugin(null);
        }
    }

    public void mousePressed(MouseEvent e, int x, int y) {
        if (getWidgetPlugin() != null) {
            x -= this.region.getX1();
            y -= this.region.getY1();
            WidgetPlugin.setActiveWidget(getWidgetPlugin());
            WidgetPluginHandler.injectWidgetPropertiesValues(getWidgetPlugin());
            getWidgetPlugin().mousePressed(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        }
    }

    public void mouseReleased(MouseEvent e, int x, int y) {
        if (getWidgetPlugin() != null) {
            x -= this.region.getX1();
            y -= this.region.getY1();
            WidgetPluginHandler.injectWidgetPropertiesValues(getWidgetPlugin());
            getWidgetPlugin().mouseReleased(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        }
    }

    public void mouseDragged(MouseEvent e, int x, int y) {
        if (getWidgetPlugin() != null) {
            x -= this.region.getX1();
            y -= this.region.getY1();
            WidgetPluginHandler.injectWidgetPropertiesValues(getWidgetPlugin());
            getWidgetPlugin().mouseDragged(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        }
    }

    public void mouseMoved(MouseEvent e, int x, int y) {
        if (getWidgetPlugin() != null) {
            x -= this.region.getX1();
            y -= this.region.getY1();
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
