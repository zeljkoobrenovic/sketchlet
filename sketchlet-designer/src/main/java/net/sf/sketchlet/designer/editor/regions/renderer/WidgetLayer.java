/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.regions.renderer;

import net.sf.sketchlet.designer.context.ActiveRegionContextImpl;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.pluginloader.PluginInstance;
import net.sf.sketchlet.pluginloader.WidgetPluginFactory;
import net.sf.sketchlet.pluginloader.WidgetPluginHandler;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

/**
 * @author zobrenovic
 */
public class WidgetLayer extends DrawingLayer {

    public WidgetLayer(ActiveRegion region) {
        super(region);
    }

    public void dispose() {
        region = null;
        if (widgetControl != null) {
            widgetControl.setActiveRegionContext(null);
            widgetControl.dispose();
            widgetControl = null;
        }
    }

    public WidgetPlugin widgetControl = null;
    public PluginInstance widgetPlugin = null;

    public void draw(Graphics2D g2, Component component, boolean bPlayback) {
        if (region == null) {
            return;
        }
        if (!region.strWidget.isEmpty()) {
            String strControl = region.processText(region.strWidget.trim());
            if (!strControl.isEmpty()) {
                if (widgetControl == null && WidgetPluginFactory.exists(strControl)) {
                    if (region.parent != null) {
                        widgetPlugin = WidgetPluginFactory.getWidgetPluginInstance(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.page)));
                    } else {
                        widgetPlugin = WidgetPluginFactory.getWidgetPluginInstance(new ActiveRegionContextImpl(region, new PageContextImpl(new Page("", ""))));
                    }
                    widgetControl = (WidgetPlugin) widgetPlugin.getInstance();
                    widgetControl.activate(bPlayback);
                } else if (widgetControl != null && !WidgetPluginHandler.getName(widgetPlugin).equalsIgnoreCase(strControl)) {
                    widgetPlugin = WidgetPluginFactory.getWidgetPluginInstance(new ActiveRegionContextImpl(region, new PageContextImpl(region.parent.page)));
                    widgetControl = (WidgetPlugin) widgetPlugin.getInstance();
                    widgetControl.activate(bPlayback);
                }
                if (widgetControl != null) {
                    WidgetPluginHandler.injectWidgetPropertiesValues(widgetControl);
                    AffineTransform t = g2.getTransform();
                    g2.translate(region.getX1(), region.getY1());
                    widgetControl.paint(g2);
                    g2.setTransform(t);
                }
            }
        } else {
            widgetControl = null;
        }
    }

    public void mousePressed(MouseEvent e, int x, int y) {
        if (widgetControl != null) {
            x -= this.region.getX1();
            y -= this.region.getY1();
            WidgetPlugin.setActiveWidget(widgetControl);
            WidgetPluginHandler.injectWidgetPropertiesValues(widgetControl);
            widgetControl.mousePressed(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        }
    }

    public void mouseReleased(MouseEvent e, int x, int y) {
        if (widgetControl != null) {
            x -= this.region.getX1();
            y -= this.region.getY1();
            WidgetPluginHandler.injectWidgetPropertiesValues(widgetControl);
            widgetControl.mouseReleased(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        }
    }

    public void mouseDragged(MouseEvent e, int x, int y) {
        if (widgetControl != null) {
            x -= this.region.getX1();
            y -= this.region.getY1();
            WidgetPluginHandler.injectWidgetPropertiesValues(widgetControl);
            widgetControl.mouseDragged(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        }
    }

    public void mouseMoved(MouseEvent e, int x, int y) {
        if (widgetControl != null) {
            x -= this.region.getX1();
            y -= this.region.getY1();
            WidgetPluginHandler.injectWidgetPropertiesValues(widgetControl);
            widgetControl.mouseMoved(new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiers(), x, y, e.getClickCount(), e.isPopupTrigger(), e.getButton()));
        }
    }
}
