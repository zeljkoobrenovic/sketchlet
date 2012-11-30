package net.sf.sketchlet.designer.editor.ui;

/**
 * @author zobrenovic
 */
public interface ProgressMonitor {
    public void onStart();

    public void onStop();

    public void setMinimum(int value);

    public void setMaximum(int value);

    public void setValue(int value);

    public void variableUpdated(String name, String value);
}
