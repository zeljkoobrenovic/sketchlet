package net.sf.sketchlet.designer.editor.tool;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.Connector;
import net.sf.sketchlet.framework.model.log.ActivityLog;
import net.sf.sketchlet.framework.model.ActiveRegion;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * @author zobrenovic
 */
public class ActiveRegionConnectorTool extends Tool {

    private Cursor cursor;
    private Connector connector = null;

    public ActiveRegionConnectorTool(SketchletEditor freeHand) {
        super(freeHand);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(5, 4);
        Image cursorImage = Workspace.createImageIcon("resources/cursor_connector.png").getImage();
        cursor = toolkit.createCustomCursor(cursorImage, hotSpot, "Active Region Connector");
    }

    @Override
    public String getName() {
        return Language.translate("Active Region Connector Tool");
    }

    @Override
    public ImageIcon getIcon() {
        return Workspace.createImageIcon("resources/connector.png");
    }

    @Override
    public String getIconFileName() {
        return "connector.png";
    }

    @Override
    public void mouseMoved(MouseEvent e, int x, int y) {
        if (x < 0 || y < 0) {
            editor.setCursor(Cursor.getDefaultCursor());
        } else {
            editor.setCursor();
            if (this.connector != null) {
                this.connector.getRenderer().setMouseX(x);
                this.connector.getRenderer().setMouseY(y);
                editor.repaintEverything();
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e, int x, int y) {
        ActiveRegion region = editor.getCurrentPage().getRegions().getMouseHelper().selectRegion(x, y, false);
        if (region != null) {
            if (connector == null) {
                connector = new Connector(region);
                connector.getRenderer().setMouseX(x);
                connector.getRenderer().setMouseY(y);
            } else {
                if (region == connector.getRegion1()) {
                    JOptionPane.showMessageDialog(SketchletEditor.editorFrame, Language.translate("You have to select another region."), Language.translate("Connector"), JOptionPane.WARNING_MESSAGE);
                } else {
                    connector.setRegion2(region);
                    SketchletEditor.getInstance().getCurrentPage().addConnector(this.connector);
                    SketchletEditor.getInstance().getCurrentPage().setSelectedConnector(connector);
                    connector = null;
                    SketchletEditor.getInstance().setTool(SketchletEditor.getInstance().getActiveRegionSelectTool(), SketchletEditor.getInstance());
                    editor.repaintEverything();
                }
            }
        } else {
            JOptionPane.showMessageDialog(SketchletEditor.editorFrame, Language.translate("You have to select a region."), Language.translate("Connector"), JOptionPane.WARNING_MESSAGE);
        }
    }

    @Override
    public void mouseReleased(int x, int y, int modifiers) {
        ActivityLog.log("toolResult", "Create a new region");
    }

    @Override
    public Cursor getCursor() {
        return cursor;
    }

    @Override
    public void draw(Graphics2D g2) {
        if (this.connector != null) {
            connector.getRenderer().draw(g2, false);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            connector.dispose();
            connector = null;
            editor.repaintEverything();
        }
    }
}
