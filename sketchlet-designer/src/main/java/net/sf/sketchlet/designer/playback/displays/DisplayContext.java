/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.sketchlet.designer.playback.displays;

import java.awt.*;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class DisplayContext {
    int x;
    int y;
    int width = Toolkit.getDefaultToolkit().getScreenSize().width;
    int height = Toolkit.getDefaultToolkit().getScreenSize().height;

    Vector<SketchClip> clips = new Vector<SketchClip>();

    public DisplayContext() {
        clips.add(new SketchClip());
        clips.add(new SketchClip());
        clips.add(new SketchClip());
        clips.add(new SketchClip());
        clips.add(new SketchClip());
    }
}
