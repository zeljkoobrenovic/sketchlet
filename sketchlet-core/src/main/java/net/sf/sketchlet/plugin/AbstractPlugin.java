/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugin;

import java.awt.Component;
import javax.swing.ImageIcon;

/**
 *
 * @author zobrenovic
 */
public class AbstractPlugin implements SketchletPluginGUI, SketchletApplicationAware, SketchletProjectAware {

    @Override
    public void afterProjectOpening() {
    }

    @Override
    public void beforeProjectClosing() {
    }

    @Override
    public void afterApplicationStart() {
    }

    @Override
    public void beforeApplicationEnd() {
    }

    @Override
    public Component getGUI() {
        return null;
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public void onSave() {
    }
}
