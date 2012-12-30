package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.designer.context.ActiveRegionContextImpl;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.loaders.pluginloader.WidgetPluginFactory;
import net.sf.sketchlet.framework.model.ActiveRegion;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * @author zeljko
 */
public class RegionWidgetPropertiesRowEditor implements TableCellEditor {

    protected TableCellEditor editor, defaultEditor;
    JTable table;
    ActiveRegion region;

    /**
     * Constructs a EachRowEditor. create default editor
     *
     * @see TableCellEditor
     * @see DefaultCellEditor
     */
    public RegionWidgetPropertiesRowEditor(JTable table, ActiveRegion region) {
        this.table = table;
        this.region = region;
        defaultEditor = table.getDefaultEditor(String.class);
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return editor.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    public Object getCellEditorValue() {
        return editor.getCellEditorValue();
    }

    public boolean stopCellEditing() {
        return editor.stopCellEditing();
    }

    public void cancelCellEditing() {
        editor.cancelCellEditing();
    }

    public boolean isCellEditable(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            selectEditor((MouseEvent) anEvent);
            return editor.isCellEditable(anEvent);
        } else {
            return false;
        }
    }

    public void addCellEditorListener(CellEditorListener l) {
        editor.addCellEditorListener(l);
    }

    public void removeCellEditorListener(CellEditorListener l) {
        editor.removeCellEditorListener(l);
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            selectEditor((MouseEvent) anEvent);
            return editor.shouldSelectCell(anEvent);
        } else {
            return false;
        }
    }

    private int lastRow = -1;

    protected void selectEditor(MouseEvent e) {
        int row;
        if (e == null) {
            row = table.getSelectionModel().getAnchorSelectionIndex();
        } else {
            row = table.rowAtPoint(e.getPoint());
        }
        String property = table.getModel().getValueAt(row, 0).toString();
        String values[] = WidgetPluginFactory.getValueList(new ActiveRegionContextImpl(region, new PageContextImpl(region.getParent().getPage())), property);
        if (values == null || values.length == 0) {
            editor = defaultEditor;
            lastRow = -1;
        } else if (row != lastRow) {
            lastRow = row;
            JComboBox cmb = new JComboBox(values);
            cmb.setEditable(true);
            cmb.insertItemAt("", 0);

            cmb.setSelectedItem(table.getModel().getValueAt(row, 1));

            editor = new DefaultCellEditor(cmb);
        }
    }
}
