/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.renderer.page;

import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.model.Page;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public interface PanelRenderer {
    public Page getSketch();

    public boolean isActive();

    public Page getMasterPage();

    public BufferedImage getImage(int layer);

    public void setImage(int layer, BufferedImage image);

    public int getImageCount();

    public BufferedImage getMasterImage();

    public void setMasterImage(BufferedImage image);

    public int getLayer();

    public int getWidth();

    public int getHeight();

    public SketchletEditorMode getMode();

    public int getMarginX();

    public int getMarginY();

    public double getScaleX();

    public double getScaleY();

    public void extraDraw(Graphics2D g2);

    public void parentPaintComponent(Graphics2D g2);

    public void enableControls();

    public void revalidate();

    public void save();

    public Graphics2D createGraphics();

    public void setCursor(Cursor cursor);

    public Component getComponent();
}
