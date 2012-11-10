/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.undo;

import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.ui.region.ActiveRegionPanel;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class RegionChangedUndoActionWithComponent extends RegionChangedUndoAction {

    Component component;
    Object restoreValue = "";

    public RegionChangedUndoActionWithComponent(ActiveRegion action, Component component, Object value) {
        super(action);
        //this.component = component;
        //this.restoreValue = value;
    }

    @Override
    public void restore() {
        if (region != null) {
            super.restore();
            if (ActiveRegionPanel.currentActiveRegionPanel != null) {
                ActiveRegionPanel.currentActiveRegionPanel.refreshComponents();
            }
            /*if (this.component != null) {
            if (restoreValue != null) {
            if (this.component instanceof JTextComponent) {
            if (restoreValue != null) {
            ((JTextComponent) component).setText(restoreValue.toString());
            }
            } else if (this.component instanceof JComboBox) {
            ((JComboBox) component).setSelectedItem(restoreValue);
            } else if (this.component instanceof JTable) {
            TableCellEditor tce = ((JTable) component).getCellEditor();
            if (tce != null) {
            tce.cancelCellEditing();
            }
            SketchletEditor.editorPanel.sketchToolbar.undo.requestFocusInWindow();
            TableModel model = ((JTable) component).getModel();
            if (model != null && model instanceof AbstractTableModel) {
            ((AbstractTableModel) model).fireTableDataChanged();
            } else {
            component.repaint();
            }
            } else {
            component.repaint();
            }
            } else {
            component.repaint();
            }
            }*/
        }
    }
}
