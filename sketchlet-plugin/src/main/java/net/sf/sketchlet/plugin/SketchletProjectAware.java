/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugin;

/**
 *
 * @author zobrenovic
 */
public interface SketchletProjectAware {

    public void afterProjectOpening();

    public void beforeProjectClosing();

    public void onSave();
}
