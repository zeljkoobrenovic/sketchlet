/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.regions.connector.Connector;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.tools.log.ActivityLog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class ActiveRegionConnectorTool extends Tool {

    int prevX, prevY;
    long prevTimestamp;
    double speeds[] = new double[5];
    int currentDistanceIndex = 0;
    double speed;
    Cursor cursor;
    Connector connector = null;

    public ActiveRegionConnectorTool(SketchletEditor freeHand) {
        super(freeHand);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(5, 4);
        Image cursorImage = Workspace.createImageIcon("resources/cursor_connector.png").getImage();
        cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Active Region Connector");
    }

    public String getName() {
        return Language.translate("Active Region Connector Tool");
    }

    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/connector.png");
    }

    public String getIconFileName() {
        return "connector.png";
    }

    public void mouseMoved(MouseEvent e, int x, int y) {
        ActiveRegion a = freeHand.currentPage.regions.selectRegion(x, y, false);
        if (x < 0 || y < 0) {
            freeHand.setCursor(Cursor.getDefaultCursor());
            /*} else if (a != null) {
            a.mouseHandler.mouseMoved(e, editorPanel.scale, SketchletEditor.editorFrame, false);
             */
        } else {
            freeHand.setCursor();
            if (this.connector != null) {
                this.connector.renderer.mouseX = x;
                this.connector.renderer.mouseY = y;
                freeHand.repaintEverything();
            }
        }
    }

    public void mousePressed(MouseEvent e, int x, int y) {
        prevX = x;
        prevY = y;
        prevTimestamp = e.getWhen();

        ActiveRegion region = freeHand.currentPage.regions.selectRegion(x, y, false);
        if (region != null) {
            if (connector == null) {
                connector = new Connector(region);
                connector.renderer.mouseX = x;
                connector.renderer.mouseY = y;
            } else {
                if (region == null || region == connector.getRegion1()) {
                    JOptionPane.showMessageDialog(SketchletEditor.editorFrame, Language.translate("You have to select another region."), Language.translate("Connector"), JOptionPane.WARNING_MESSAGE);
                } else {
                    connector.setRegion2(region);
                    SketchletEditor.editorPanel.currentPage.addConnector(this.connector);
                    SketchletEditor.editorPanel.currentPage.selectedConnector = connector;
                    connector = null;
                    SketchletEditor.editorPanel.setTool(SketchletEditor.editorPanel.activeRegionSelectTool, SketchletEditor.editorPanel);
                    freeHand.repaintEverything();
                }
            }
        } else {
            JOptionPane.showMessageDialog(SketchletEditor.editorFrame, Language.translate("You have to select a region."), Language.translate("Connector"), JOptionPane.WARNING_MESSAGE);
        }
    }

    public void mouseReleased(MouseEvent e, int x, int y) {
    }

    public void mouseDragged(MouseEvent e, int x, int y) {
        this.mouseMoved(e, x, y);
    }

    public void mouseMoved(int x, int y, int modifiers) {
    }

    public void mousePressed(int x, int y, int modifiers) {
    }

    public void mouseReleased(int x, int y, int modifiers) {
        ActivityLog.log("toolResult", "Create a new region");
        TutorialPanel.addLine("cmd", "Create a new region by dragging a rectangle area on the page", "", toolInterface.getPanel());
    }

    public void mouseDragged(int x, int y, int modifiers) {
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void draw(Graphics2D g2) {
        if (this.connector != null) {
            connector.renderer.draw(g2, false);
        }
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
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            connector.dispose();
            connector = null;
            freeHand.repaintEverything();
        }
    }

    public void keyReleased(KeyEvent e) {
    }
}
