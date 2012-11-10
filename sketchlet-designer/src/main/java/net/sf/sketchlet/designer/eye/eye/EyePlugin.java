package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.plugin.AbstractPlugin;
import net.sf.sketchlet.plugin.PluginInfo;

import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 20-9-12
 * Time: 16:00
 * To change this template use File | Settings | File Templates.
 */
@PluginInfo(name = "eye", type = "generic", description = "Visualization of Sketchlet objects")
public class EyePlugin extends AbstractPlugin {
    @Override
    public Component getGUI() {
        EyeFrame eyeFrame = new EyeFrame();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, eyeFrame.selectorPanel, eyeFrame);
        return splitPane;
    }
}
