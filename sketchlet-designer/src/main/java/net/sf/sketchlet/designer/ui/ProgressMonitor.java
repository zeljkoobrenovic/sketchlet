/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.designer.ui;

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
