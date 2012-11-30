package net.sf.sketchlet.designer.editor.ui.region;

import javax.swing.*;
import java.awt.*;

/**
 * @author zeljko
 */
public class ComboBoxRenderer extends JLabel implements ListCellRenderer {
    private ImageIcon images[];
    private String labels[];

    public ComboBoxRenderer(String[] labels, ImageIcon[] images) {
        this.labels = labels;
        this.images = images;
        setOpaque(true);
        setHorizontalAlignment(LEFT);
        setVerticalAlignment(CENTER);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        int selectedIndex = ((Integer) value).intValue();

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        ImageIcon icon = images[selectedIndex];
        String pet = labels[selectedIndex];
        setIcon(icon);
        setText(pet);
        setFont(list.getFont());

        return this;
    }
}
