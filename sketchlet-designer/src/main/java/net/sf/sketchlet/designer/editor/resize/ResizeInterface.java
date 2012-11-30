package net.sf.sketchlet.designer.editor.resize;

/**
 * @author zobrenovic
 */
public interface ResizeInterface {
    public void resizeImage(int w, int h);

    public void resizeCanvas(int w, int h);

    public int getImageWidth();

    public int getImageHeight();
}
