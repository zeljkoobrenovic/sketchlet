package net.sf.sketchlet.framework.controller;

import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Page;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

/**
 * @author zeljko
 */
public class ActiveRegionsMouseHelper {

    private Vector<ActiveRegion> selectedRegions = null;
    private Point mousePressedPoint;
    private boolean newRegion = false;
    private boolean withoutEffect = false;

    private Page page;

    public ActiveRegionsMouseHelper(Page page) {
        this.page = page;
    }

    public ActiveRegion getLastSelectedRegion() {
        if (this.getSelectedRegions() != null && this.getSelectedRegions().size() > 0) {
            return getSelectedRegions().lastElement();
        }
        return null;
    }

    public void removeFromSelection(ActiveRegion region) {
        if (getSelectedRegions() != null && region != null && getSelectedRegions().contains(region)) {
            getSelectedRegions().remove(region);
        }
    }

    public void addToSelection(ActiveRegion region) {
        if (region != null) {
            if (getSelectedRegions() == null) {
                setSelectedRegions(new Vector<ActiveRegion>());
            }
            String strGroup = region.getRegionGrouping().trim();
            if (!strGroup.equals("")) {
                for (ActiveRegion reg : getPage().getRegions().getRegions()) {
                    if (reg.getRegionGrouping().equals(strGroup) && reg != region && !getSelectedRegions().contains(reg)) {
                        getSelectedRegions().add(reg);
                    }
                }
            }
            if (getSelectedRegions() != null && getSelectedRegions().contains(region)) {
                getSelectedRegions().remove(region);
            }
            getSelectedRegions().add(region);
        }
    }

    public void deselectRegions() {
        setSelectedRegions(null);
        SketchletEditor.getInstance().getActiveRegionMenu().setEnabled(isRegionSelected());
        SketchletEditor.getInstance().getFormulaToolbar().enableControls(isRegionSelected(), this.isConnectorSelected());
        SketchletEditor.getInstance().getFormulaToolbar().refresh();
    }


    public void mousePressed(MouseEvent e, double scale, JFrame frame, boolean bPlayback, boolean bAddNew) {
        int marginX = bPlayback ? 0 : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? 0 : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;

        mousePressed(x, y, e.getModifiers(), e.getWhen(), scale, e, frame, bPlayback, bAddNew);
    }

    public void mousePressed(int x, int y, int modifiers, long when, double scale, MouseEvent e, JFrame frame, boolean bPlayback, boolean bAddNew) {
        ActiveRegion reg = selectRegion(x, y, bPlayback);
        setMousePressedPoint(new Point(x, y));

        if (reg == null || bAddNew) {
            if (bAddNew) {
                setSelectedRegions(new Vector<ActiveRegion>());
                if ((modifiers & InputEvent.BUTTON1_MASK) == InputEvent.BUTTON1_MASK) {
                    setNewRegion(true);
                    reg = new ActiveRegion(this.getPage().getRegions(), x, y, x + 50, y + 20);
                    reg.setLayer(SketchletEditor.getInstance().getLayer());
                    if (SketchletEditor.getInitProperties() != null) {
                        for (int i = 0; i < SketchletEditor.getInitProperties().length; i++) {
                            String strProp = SketchletEditor.getInitProperties()[i][0];
                            if (!strProp.equalsIgnoreCase("x1") && !strProp.equalsIgnoreCase("y1") && !strProp.equalsIgnoreCase("x2") && !strProp.equalsIgnoreCase("y2") && !strProp.equalsIgnoreCase("position x") && !strProp.equalsIgnoreCase("position y")) {
                                reg.setProperty(strProp, SketchletEditor.getInitProperties()[i][1]);
                            }
                        }
                    }
                    addToSelection(reg);
                    reg.getMouseController().setSelectedCorner(ActiveRegionMouseController.BOTTOM_RIGHT);
                    getPage().getRegions().getRegions().insertElementAt(reg, 0);
                    reg.activate(bPlayback);
                }
            } else {
                setSelectedRegions(null);
            }
        } else {
            if (getSelectedRegions() == null) {
                setSelectedRegions(new Vector<ActiveRegion>());
                addToSelection(reg);
            } else if (SketchletEditor.getInstance().isInShiftMode() && !getSelectedRegions().contains(reg)) {
                addToSelection(reg);
            } else if (SketchletEditor.getInstance().isInShiftMode() && getSelectedRegions().contains(reg)) {
                removeFromSelection(reg);
            } else if (!SketchletEditor.getInstance().isInShiftMode() && !getSelectedRegions().contains(reg)) {
                setSelectedRegions(new Vector<ActiveRegion>());
                addToSelection(reg);
            } else {
                getSelectedRegions().remove(reg);
                addToSelection(reg);
            }

            reg.getMouseController().mousePressed(x, y, modifiers, when, e, frame, bPlayback);
            ActiveRegionsFrame.refresh(reg);
            SketchletEditor.getInstance().repaint();
        }

        SketchletEditor.getInstance().getActiveRegionMenu().setEnabled(isRegionSelected());
        SketchletEditor.getInstance().getFormulaToolbar().enableControls(isRegionSelected(), this.isConnectorSelected());
    }

    public boolean isRegionSelected() {
        return getSelectedRegions() != null && getSelectedRegions().size() > 0;
    }

    public boolean isConnectorSelected() {
        return SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getCurrentPage().getSelectedConnector() != null;
    }

    public void mouseDragged(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        if (getSelectedRegions() != null && getSelectedRegions().size() > 0) {
            getSelectedRegions().lastElement().getMouseController().mouseDragged(e, scale, frame, bPlayback);
        }
    }

    public void mouseDragged(int x, int y, int modifiers, long when, double scale, MouseEvent e, JFrame frame, boolean bPlayback) {
        if (getSelectedRegions() != null && getSelectedRegions().size() > 0) {
            getSelectedRegions().lastElement().getMouseController().mouseDragged(x, y, modifiers, when, scale, e, frame, bPlayback);
        }
    }

    public void mouseReleased(MouseEvent e, double scale, JFrame frame, boolean bPlayback) {
        int marginX = bPlayback ? 0 : SketchletEditor.getInstance().getMarginX();
        int marginY = bPlayback ? 0 : SketchletEditor.getInstance().getMarginY();
        int x = (int) ((e.getPoint().x) / scale) - marginX;
        int y = (int) ((e.getPoint().y) / scale) - marginY;

        mouseReleased(x, y, e.getModifiers(), e.getWhen(), scale, e, frame, bPlayback);
    }

    public void mouseReleased(int x, int y, int modifiers, long when, double scale, MouseEvent e, JFrame frame, boolean bPlayback) {
        setWithoutEffect(false);
        if (getSelectedRegions() != null && getSelectedRegions().size() > 0) {
            if (getSelectedRegions().lastElement().getWidthValue() > 0 || (getMousePressedPoint() != null && !(Math.abs(getMousePressedPoint().x - x) <= 0 && Math.abs(getMousePressedPoint().y - y) <= 0))) {
                if (isNewRegion()) {
                    ActiveRegionsFrame.reload(getSelectedRegions().lastElement());
                }

                getSelectedRegions().lastElement().getMouseController().mouseReleased(x, y, modifiers, when, e, frame, bPlayback);
            } else {
                if (isNewRegion()) {
                    for (ActiveRegion as : getSelectedRegions()) {
                        as.deactivate(bPlayback);
                        getPage().getRegions().getRegions().remove(as);
                    }
                    setSelectedRegions(null);
                }

                setWithoutEffect(true);
            }

            SketchletEditor.getInstance().repaint();

            setMousePressedPoint(null);
        }
        setNewRegion(false);
    }

    public ActiveRegion selectRegion(int x, int y, boolean bPlayback) {
        if (!this.getPage().isRegionsLayer()) {
            return null;
        }
        for (ActiveRegion a : getPage().getRegions().getRegions()) {
            if (a.isActive(bPlayback) && a.getMouseController().inRect(x, y, bPlayback)) {
                return a;
            }
        }

        return null;
    }

    public void defocusAllRegions() {
        for (ActiveRegion ar : getPage().getRegions().getRegions()) {
            ar.setInFocus(false);
        }
        SketchletContext.getInstance().repaint();
    }

    public Vector<ActiveRegion> getSelectedRegions() {
        return selectedRegions;
    }

    public void setSelectedRegions(Vector<ActiveRegion> selectedRegions) {
        this.selectedRegions = selectedRegions;
    }

    public Page getPage() {
        return page;
    }

    public Point getMousePressedPoint() {
        return mousePressedPoint;
    }

    public void setMousePressedPoint(Point mousePressedPoint) {
        this.mousePressedPoint = mousePressedPoint;
    }

    public boolean isNewRegion() {
        return newRegion;
    }

    public void setNewRegion(boolean newRegion) {
        this.newRegion = newRegion;
    }

    public void setWithoutEffect(boolean withoutEffect) {
        this.withoutEffect = withoutEffect;
    }

    public boolean isWithoutEffect() {
        return withoutEffect;
    }
}
