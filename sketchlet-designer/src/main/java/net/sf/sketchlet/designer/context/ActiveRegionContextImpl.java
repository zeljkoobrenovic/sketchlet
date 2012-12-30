package net.sf.sketchlet.designer.context;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.designer.playback.ui.PlaybackPanel;
import net.sf.sketchlet.framework.model.imagecache.ImageCache;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.events.mouse.MouseEventMacro;
import net.sf.sketchlet.framework.model.events.widget.WidgetEventMacro;
import net.sf.sketchlet.framework.model.programming.macros.MacroThread;

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
        return region.getWidget();
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
        File file = new File(this.region.getDrawnImagePath(index));
        if (file.exists()) {
            if (region.getDrawnImage(index) != null) {
                return region.getDrawnImage(index);
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
        return region.getX1Value();
    }

    @Override
    public int getY1() {
        return region.getY1Value();
    }

    @Override
    public int getX2() {
        return region.getX2Value();
    }

    @Override
    public int getY2() {
        return region.getY2Value();
    }

    @Override
    public int getWidth() {
        return region.getWidthValue();
    }

    @Override
    public int getHeight() {
        return region.getHeightValue();
    }

    @Override
    public String getWidgetItemText() {
        return region.processText(region.getWidgetItems());
    }

    @Override
    public Stroke getStroke() {
        return region.getStrokeValue();
    }

    @Override
    public Color getLineColor() {
        return region.getLineColorValue();
    }

    @Override
    public Color getFillColor() {
        return region.getBackgroundColorValue();
    }

    @Override
    public int getLineThickness() {
        return region.getLineThicknessValue();
    }

    @Override
    public Color getTextColor() {
        return region.getFontColorValue();
    }

    @Override
    public Font getFont(float size) {
        return region.getFontValue(size);
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
        for (MouseEventMacro mouseEventMacro : region.getMouseEventsProcessor().getMouseEventMacros()) {
            for (int i = 0; i < mouseEventMacro.getMacro().getActions().length; i++) {
                String action = mouseEventMacro.getMacro().getActions()[i][1].toString();
                String param1 = mouseEventMacro.getMacro().getActions()[i][2].toString();

                if (action.equalsIgnoreCase("go to page") && !param1.isEmpty()) {
                    return param1;
                }
            }
        }
        return "";
    }

    @Override
    public boolean fitToBox() {
        return region.isFitToBoxEnabled();
    }

    @Override
    public boolean isAdjusting() {
        return region.isAdjusting();
    }

    @Override
    public void processEvent(String actionId, String... params) {
        for (WidgetEventMacro widgetEventMacro : region.getWidgetEventMacros()) {
            if (widgetEventMacro.getEventName().equalsIgnoreCase(actionId)) {
                Page page;
                if (PlaybackFrame.playbackFrame != null || (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getInternalPlaybackPanel() != null)) {
                    page = PlaybackPanel.getCurrentPage();
                } else {
                    page = SketchletEditor.getInstance().getCurrentPage();
                }

                try {
                    Thread.sleep(1);
                    String strParams = "";
                    for (String param : params) {
                        strParams += "\"" + param + "\"";
                    }
                    MacroThread mt = new MacroThread(widgetEventMacro.getMacro(), strParams, "", "");
                    page.getActiveMacros().add(mt);
                } catch (Throwable e) {
                }
            }
        }
    }
}
