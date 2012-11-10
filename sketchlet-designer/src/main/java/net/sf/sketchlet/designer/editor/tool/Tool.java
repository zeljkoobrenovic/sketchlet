/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
    SketchletEditor freeHand;
    public ToolInterface toolInterface;
    String settings[] = new String[]{};

    public Tool(SketchletEditor freeHand) {
        this.freeHand = freeHand;
        this.toolInterface = freeHand;
    }

    public Tool(SketchletEditor freeHand, String settings[]) {
        this.freeHand = freeHand;
        this.toolInterface = freeHand;
        this.settings = settings;
    }

    public Tool(ToolInterface toolInterface) {
        this.freeHand = null;
        this.toolInterface = toolInterface;
    }

    public Tool(ToolInterface toolInterface, String settings[]) {
        this.freeHand = null;
        this.toolInterface = toolInterface;
        this.settings = settings;
    }

    public void dispose() {
        this.freeHand = null;
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
