/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.context;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.tools.imagecache.ImageCache;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.designer.playback.ui.PlaybackPanel;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.events.MouseEventMacro;
import net.sf.sketchlet.model.Page;
import net.sf.sketchlet.model.events.WidgetEventMacro;
import net.sf.sketchlet.model.programming.macros.Macro;
import net.sf.sketchlet.model.programming.macros.MacroThread;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author zobrenovic
 */
public class ActiveRegionContextImpl extends ActiveRegionContext {

    private ActiveRegion region;
    private PageContext pageContext;

    public ActiveRegionContextImpl(ActiveRegion region, PageContext pageContext) {
        this.region = region;
        this.pageContext = pageContext;
    }

    @Override
    public PageContext getPageContext() {
        return this.pageContext;
    }

    @Override
    public String getProperty(String strProperty) {
        return region.getPropertyValue(strProperty);
    }

    @Override
    public void setProperty(String strProperty, String strValue) {
        region.setProperty(strProperty, strValue);
    }

    @Override
    public String getWidgetType() {
        return region.strWidget;
    }

    @Override
    public String getWidgetProperty(String strProperty) {
        return region.getWidgetProperty(strProperty);
    }

    @Override
    public void setWidgetProperty(String strProperty, String strValue) {
        region.setWidgetProperty(strProperty, strValue);
    }

    @Override
    public BufferedImage getImage(int index) {
        File file = new File(this.region.getDrawImagePath(index));
        if (file.exists()) {
            if (region.getDrawImage(index) != null) {
                return region.getDrawImage(index);
            }
            try {
                return ImageCache.read(file);
            } catch (Exception e) {
            }
        }

        return null;
    }

    @Override
    public int getX1() {
        return region.getX1();
    }

    @Override
    public int getY1() {
        return region.getY1();
    }

    @Override
    public int getX2() {
        return region.getX2();
    }

    @Override
    public int getY2() {
        return region.getY2();
    }

    @Override
    public int getWidth() {
        return region.getWidth();
    }

    @Override
    public int getHeight() {
        return region.getHeight();
    }

    @Override
    public String getWidgetItemText() {
        return region.processText(region.widgetItems);
    }

    @Override
    public Stroke getStroke() {
        return region.getStroke();
    }

    @Override
    public Color getLineColor() {
        return region.getLineColor();
    }

    @Override
    public Color getFillColor() {
        return region.getBackgroundColor();
    }

    @Override
    public int getLineThickness() {
        return region.getLineThickness();
    }

    @Override
    public Color getTextColor() {
        return region.getFontColor();
    }

    @Override
    public Font getFont(float size) {
        return region.getFont(size);
    }

    @Override
    public int getImageCount() {
        return region.getImageCount();
    }

    @Override
    public Dimension getSize() {
        return region.getSize();
    }

    @Override
    public String getWidgetPropertiesString(boolean bProcess) {
        return region.getWidgetPropertiesString(bProcess);
    }

    @Override
    public String getName() {
        return region.getName();
    }

    @Override
    public String getNumber() {
        return region.getNumber();
    }

    @Override
    public Object[][] getMouseEvents() {
        return null;
    }

    @Override
    public String getFirstMousePageLink() {
        for (MouseEventMacro mouseEventMacro : region.mouseProcessor.getMouseEventMacros()) {
            for (int i = 0; i < mouseEventMacro.getMacro().getActions().length; i++) {
                String event = mouseEventMacro.getMacro().getActions()[i][0].toString();
                String action = mouseEventMacro.getMacro().getActions()[i][1].toString();
                String param1 = mouseEventMacro.getMacro().getActions()[i][2].toString();
                String param2 = mouseEventMacro.getMacro().getActions()[i][3].toString();

                if (action.equalsIgnoreCase("go to page") && !param1.isEmpty()) {
                    return param1;
                }
            }
        }
        return "";
    }

    @Override
    public boolean fitToBox() {
        return region.fitToBoxEnabled;
    }

    @Override
    public boolean isAdjusting() {
        return region.bAdjusting;
    }

    @Override
    public void processEvent(String actionId, String... params) {
        for (WidgetEventMacro widgetEventMacro : region.widgetEventMacros) {
            if (widgetEventMacro.getEventName().equalsIgnoreCase(actionId)) {
                Page page = null;
                if (PlaybackFrame.playbackFrame != null || (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getInternalPlaybackPanel() != null)) {
                    page = PlaybackPanel.currentPage;
                } else {
                    page = SketchletEditor.getInstance().getCurrentPage();
                }

                try {
                    Thread.sleep(1);
                    String strParams = "";
                    for (String param : params) {
                        strParams += "\"" + param + "\"";
                    }
                    Macro macro = widgetEventMacro.getMacro();
                    MacroThread mt = new MacroThread(widgetEventMacro.getMacro(), strParams, "", "");
                    page.getActiveMacros().add(mt);
                } catch (Throwable e) {
                }
            }
        }
    }
}
