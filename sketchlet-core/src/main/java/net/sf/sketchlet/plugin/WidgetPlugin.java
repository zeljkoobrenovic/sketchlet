/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugin;

import java.awt.Graphics2D;
import net.sf.sketchlet.context.ActiveRegionContext;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import net.sf.sketchlet.context.VariablesBlackboardContext;

/**
 *
 * @author zobrenovic
 */
public class WidgetPlugin {

    private ActiveRegionContext regionContext;
    private static WidgetPlugin activeWidget = null;

    public WidgetPlugin(ActiveRegionContext region) {
        this.regionContext = region;
    }

    public static WidgetPlugin getActiveWidget() {
        return WidgetPlugin.activeWidget;
    }

    public static void setActiveWidget(WidgetPlugin activeWidget) {
        WidgetPlugin.activeWidget = activeWidget;
    }

    public void activate(boolean bPlayback) {
    }

    public void deactivate(boolean bPlayback) {
    }

    public void dispose() {
    }

    public void paint(Graphics2D g2) {
    }

    protected void repaint() {
        if (regionContext != null) {
            regionContext.getPageContext().getSketchletContext().repaint();
        }
    }

    public void mousePressed(MouseEvent me) {
    }

    public void mouseReleased(MouseEvent me) {
    }

    public void mouseClicked(MouseEvent me) {
    }

    public void mouseMoved(MouseEvent me) {
    }

    public void mouseDragged(MouseEvent me) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    public void variableUpdated(String triggerVariable, String value) {
    }

    public void setActiveRegionContext(ActiveRegionContext regionContext) {
        this.regionContext = regionContext;
    }

    public ActiveRegionContext getActiveRegionContext() {
        return this.regionContext;
    }

    protected void requestFocus() {
        ImageCachingWidgetPlugin.setActiveWidget(this);
        regionContext.getPageContext().getSketchletContext().requestFocus();
    }

    protected boolean hasFocus() {
        return this == WidgetPlugin.getActiveWidget();
    }

    public final void updateSketchletVariable(String name, String value) {
        if (name != null && value != null && !name.isEmpty() && !value.isEmpty()) {
            VariablesBlackboardContext.getInstance().updateVariable(name, value);
        }
    }

    public final String getSketchletVariable(String name) {
        VariablesBlackboardContext.getInstance().getVariableValue(name);

        return "";
    }

    public final int getWidth() {
        return this.getActiveRegionContext().getWidth();
    }

    public final int getHeight() {
        return this.getActiveRegionContext().getHeight();
    }
}
