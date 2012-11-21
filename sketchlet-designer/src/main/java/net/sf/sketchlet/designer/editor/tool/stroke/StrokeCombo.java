/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.tool.stroke;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.ui.toolbars.ColorToolbar;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StrokeCombo extends JComboBox {

    ImageIcon[] images;
    public static String[] strokeIDs = {"wobble", "brush", "regular", "empty", "dashed 1", "dashed 2"};
    int width = 70;
    int height = 20;

    public String getStroke() {
        int sel = this.getSelectedIndex();
        if (sel >= 0 && sel < strokeIDs.length) {
            return strokeIDs[sel];
        }

        return "";
    }

    private StrokeCombo(Integer[] intArray) {
        super(intArray);
        ComboBoxRenderer renderer = new ComboBoxRenderer();
        renderer.setPreferredSize(new Dimension(40, 20));

        images = new ImageIcon[strokeIDs.length];
        for (int i = 0; i < strokeIDs.length; i++) {
            images[i] = createImageIcon(ColorToolbar.getStroke(strokeIDs[i], 3.0f), width, height);
        }

        setRenderer(renderer);
        setMaximumRowCount(15);
    }

    public static StrokeCombo getInstance() {
        //Load the pet images and create an array of indexes.
        Integer[] intArray = new Integer[strokeIDs.length];
        for (int i = 0; i < strokeIDs.length; i++) {
            intArray[i] = new Integer(i);
        }

        StrokeCombo strokeCombo = new StrokeCombo(intArray);

        return strokeCombo;
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    public static ImageIcon createImageIcon(Stroke stroke, int w, int h) {
        BufferedImage img = Workspace.createCompatibleImage(w, h);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(stroke);
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillRect(0, 0, w, h);
        g2.setColor(Color.BLACK);
        g2.drawLine(0, h / 2, w, h / 2);
        g2.dispose();
        return new ImageIcon(img);
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("CustomComboBoxDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = StrokeCombo.getInstance();
        // newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }

    class ComboBoxRenderer extends JLabel implements ListCellRenderer {

        public ComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        /*
         * This method finds the image and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and image.
         */
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {
            //Get the selected index. (The index param isn't
            //always valid, so just use the value.)
            int selectedIndex = ((Integer) value).intValue();

            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            //Set the icon and text.  If icon was null, say so.
            ImageIcon icon = images[selectedIndex];
            setIcon(icon);
            if (icon != null) {
                setFont(list.getFont());
            } else {
            }

            return this;
        }
    }
}
