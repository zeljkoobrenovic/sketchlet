package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.editor.SketchletEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public abstract class Tool implements KeyListener {

    protected BufferedImage image;
    protected SketchletEditor editor;
    protected ToolInterface toolInterface;
    protected String settings[] = new String[]{};

    public Tool(SketchletEditor editor) {
        this.editor = editor;
        this.toolInterface = editor;
    }

    public Tool(SketchletEditor editor, String settings[]) {
        this.editor = editor;
        this.toolInterface = editor;
        this.settings = settings;
    }

    public Tool(ToolInterface toolInterface) {
        this.editor = null;
        this.toolInterface = toolInterface;
    }

    public Tool(ToolInterface toolInterface, String settings[]) {
        this.editor = null;
        this.toolInterface = toolInterface;
        this.settings = settings;
    }

    public ToolInterface getToolInterface() {
        return toolInterface;
    }

    public void setToolInterface(ToolInterface toolInterface) {
        this.toolInterface = toolInterface;
    }

    public void dispose() {
        this.editor = null;
        this.toolInterface = null;
        if (image != null) {
            image.flush();
            image = null;
        }
    }

    public ImageIcon getIcon() {
        return null;
    }

    public String getIconFileName() {
        return null;
    }

    public String getName() {
        return Language.translate("");
    }

    public boolean needSetting(String setting) {
        for (int i = 0; i < settings.length; i++) {
            if (settings[i].equalsIgnoreCase(setting)) {
                return true;
            }
        }
        return false;
    }

    public void mouseMoved(MouseEvent me, int x, int y) {
        mouseMoved(x, y, me.getModifiers());
    }

    public void mousePressed(MouseEvent me, int x, int y) {
        mouseMoved(x, y, me.getModifiers());
    }

    public void mouseReleased(MouseEvent me, int x, int y) {
        mouseMoved(x, y, me.getModifiers());
    }

    public void mouseDragged(MouseEvent me, int x, int y) {
        mouseMoved(x, y, me.getModifiers());
    }

    public void mouseMoved(int x, int y, int modifiers) {
    }

    public void mousePressed(int x, int y, int modifiers) {
    }

    public void mouseReleased(int x, int y, int modifiers) {
    }

    public void mouseDragged(int x, int y, int modifiers) {
    }

    public Cursor getCursor() {
        return Cursor.getDefaultCursor();
    }

    public void draw(Graphics2D g2) {
    }

    public void activate() {
    }

    public void deactivate() {
    }

    public void onUndo() {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }
}
