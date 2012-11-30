package net.sf.sketchlet.designer.editor.tool;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public interface ToolInterface {

    public BufferedImage getImage();

    public void saveImageUndo();

    public void setImage(BufferedImage image);

    public void setImageUpdated(boolean bUpdated);

    public void setCursor();

    public Graphics2D getImageGraphics();

    public Graphics2D createGraphics();

    public void setImageCursor(Cursor cursor);

    public void repaintImage();

    public boolean isInShiftMode();

    public boolean isInCtrlMode();

    public void setColor(Color c);

    public Color getColor();

    public Stroke getStroke();

    public int getStrokeWidth();

    public int getImageWidth();

    public int getImageHeight();

    public BufferedImage extractImage(int x1, int y1, int w, int sh);

    public BufferedImage extractImage(Polygon polygon);

    public void setTool(Tool tool, Component source);

    public SelectTool getSelectTool();

    public float getWatering();

    public boolean shouldShapeFill();

    public boolean shouldShapeOutline();

    public Component getPanel();

    public String getName();
}
