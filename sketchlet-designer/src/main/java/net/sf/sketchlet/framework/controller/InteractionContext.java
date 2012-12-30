package net.sf.sketchlet.framework.controller;

import net.sf.sketchlet.designer.playback.displays.ScreenMapping;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.Project;

import javax.swing.*;
import java.awt.geom.AffineTransform;

/**
 * @author zeljko
 */
public class InteractionContext {
    private Project project;
    private Page currentPage;
    private Page masterPage;
    private static ActiveRegion selectedRegion;
    private ActiveRegion mouseOverRegion = null;
    private double scale = 1.0;
    private JFrame frame;
    private AffineTransform affineTransform;
    private KeyboardController keyboardController;
    private MouseController mouseController;
    private ScreenMapping screenMapping;

    public double getScale() {
        return scale;
    }

    public void setScale(double scale) {
        scale = scale;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Page getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Page currentPage) {
        this.currentPage = currentPage;
    }

    public Page getMasterPage() {
        return masterPage;
    }

    public void setMasterPage(Page masterPage) {
        this.masterPage = masterPage;
    }

    public static ActiveRegion getSelectedRegion() {
        return selectedRegion;
    }

    public static void setSelectedRegion(ActiveRegion selectedRegion) {
        InteractionContext.selectedRegion = selectedRegion;
    }

    public void repaint() {

    }

    public JFrame getFrame() {
        return frame;
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public AffineTransform getAffineTransform() {
        return affineTransform;
    }

    public void setAffineTransform(AffineTransform affineTransform) {
        this.affineTransform = affineTransform;
    }

    public KeyboardController getKeyboardController() {
        return keyboardController;
    }

    public void setKeyboardController(KeyboardController keyboardController) {
        this.keyboardController = keyboardController;
    }

    public MouseController getMouseController() {
        return mouseController;
    }

    public void setMouseController(MouseController mouseController) {
        this.mouseController = mouseController;
    }

    public ActiveRegion getMouseOverRegion() {
        return mouseOverRegion;
    }

    public void setMouseOverRegion(ActiveRegion mouseOverRegion) {
        this.mouseOverRegion = mouseOverRegion;
    }

    public ScreenMapping getScreenMapping() {
        return screenMapping;
    }

    public void setScreenMapping(ScreenMapping screenMapping) {
        this.screenMapping = screenMapping;
    }
}
