package net.sf.sketchlet.designer.editor.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author zobrenovic
 */
public class SketchCellRenderer extends JPanel implements TableCellRenderer {

    Border unselectedBorder = null;
    Border selectedBorder = null;
    boolean isBordered = true;

    public SketchCellRenderer(boolean isBordered) {
        this.isBordered = isBordered;
        setOpaque(true); //MUST do this for background to show up.
    }

    BufferedImage image;

    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (image != null) {
            int w = 100;
            int h = 100;
            g2.setPaint(getBackground());
            g2.fillRect(0, 0, w, h);
            g2.drawImage(image, 0, 0, null);
        }
    }

    public Component getTableCellRendererComponent(JTable table, Object _image, boolean isSelected, boolean hasFocus, int row, int column) {
        try {
            if (_image != null) {
                if (_image instanceof File) {
                    File file = (File) _image;
                    image = ImageIO.read(file);
                } else {
                    image = (BufferedImage) _image;
                }
            } else {
                image = null;
            }
        } catch (Exception e) {
            // e.printStackTrace();
        }
        if (isBordered) {
            if (isSelected) {
                if (selectedBorder == null) {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                            table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                            table.getBackground());
                }
                setBorder(unselectedBorder);
            }
        }

        return this;
    }
}
