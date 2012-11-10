/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.context;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.MouseEventMacro;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.data.WidgetEventMacro;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.macros.Macro;
import net.sf.sketchlet.designer.programming.macros.MacroThread;
import net.sf.sketchlet.designer.tools.imagecache.ImageCache;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.designer.ui.playback.PlaybackPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author zobrenovic
 */
public class ActiveRegionContextImpl extends ActiveRegionContext {

    ActiveRegion region;
    PageContext pageContext;

    public ActiveRegionContextImpl(ActiveRegion region, PageContext pageContext) {
        this.region = region;
        this.pageContext = pageContext;
    }

    public PageContext getPageContext() {
        return this.pageContext;
    }

    public String getProperty(String strProperty) {
        return region.getPropertyValue(strProperty);
    }

    public String getRegionPropertyXMLEncoded(String strProperty) {
        return region.getPropertyXMLEncoded(strProperty);
    }

    public void setProperty(String strProperty, String strValue) {
        region.setProperty(strProperty, strValue);
    }

    public String getWidgetType() {
        return region.strWidget;
    }

    public String getWidgetProperty(String strProperty) {
        return region.getWidgetProperty(strProperty);
    }

    public void setWidgetProperty(String strProperty, String strValue) {
        region.setWidgetProperty(strProperty, strValue);
    }

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

    public int getX1() {
        return region.getX1();
    }

    public int getY1() {
        return region.getY1();
    }

    public int getX2() {
        return region.getX2();
    }

    public int getY2() {
        return region.getY2();
    }

    public int getWidth() {
        return region.getWidth();
    }

    public int getHeight() {
        return region.getHeight();
    }

    public String getWidgetItemText() {
        return region.processText(region.strWidgetItems);
    }

    public Stroke getStroke() {
        return region.getStroke();
    }

    public Color getLineColor() {
        return region.getLineColor();
    }

    public Color getFillColor() {
        return region.getBackgroundColor();
    }

    public int getLineThickness() {
        return region.getLineThickness();
    }

    public Color getTextColor() {
        return region.getFontColor();
    }

    public Font getFont(float size) {
        return region.getFont(size);
    }

    public int getImageCount() {
        return region.getImageCount();
    }

    public Dimension getSize() {
        return region.getSize();
    }

    public String getWidgetPropertiesString(boolean bProcess) {
        return region.getWidgetPropertiesString(bProcess);
    }

    public String getName() {
        return region.getName();
    }

    public String getNumber() {
        return region.getNumber();
    }

    public Object[][] getMouseEvents() {
        return null;
    }

    public String getFirstMousePageLink() {
        for (MouseEventMacro mouseEventMacro : region.mouseProcessor.mouseEventMacros) {
            for (int i = 0; i < mouseEventMacro.getMacro().actions.length; i++) {
                String event = mouseEventMacro.getMacro().actions[i][0].toString();
                String action = mouseEventMacro.getMacro().actions[i][1].toString();
                String param1 = mouseEventMacro.getMacro().actions[i][2].toString();
                String param2 = mouseEventMacro.getMacro().actions[i][3].toString();

                if (action.equalsIgnoreCase("go to page") && !param1.isEmpty()) {
                    return param1;
                }
            }
        }
        return "";
    }

    public Shape getShape() {
        return this.region.getShape(true);
    }

    public boolean fitToBox() {
        return region.bFitToBox;
    }

    public boolean isAdjusting() {
        return region.bAdjusting;
    }

    @Override
    public void processEvent(String actionId, String... params) {
        for (WidgetEventMacro widgetEventMacro : region.widgetEventMacros) {
            if (widgetEventMacro.getEventName().equalsIgnoreCase(actionId)) {
                Page page = null;
                if (PlaybackFrame.playbackFrame != null || (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.internalPlaybackPanel != null)) {
                    page = PlaybackPanel.currentPage;
                } else {
                    page = SketchletEditor.editorPanel.currentPage;
                }

                try {
                    Thread.sleep(1);
                    String strParams = "";
                    for (String param : params) {
                        strParams += "\"" + param + "\"";
                    }
                    Macro macro = widgetEventMacro.getMacro();
                    MacroThread mt = new MacroThread(widgetEventMacro.getMacro(), strParams, "", "");
                    page.activeMacros.add(mt);
                } catch (Throwable e) {
                }
            }
        }
    }
}
