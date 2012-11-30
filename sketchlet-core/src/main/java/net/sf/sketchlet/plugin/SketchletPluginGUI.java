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
public interface SketchletPluginGUI {

    public Component getGUI();

    public ImageIcon getIcon();
}
