package net.sf.sketchlet.designer.editor.ui;

import net.sf.sketchlet.designer.Workspace;

import javax.swing.*;
import java.awt.*;

/**
 * @author zobrenovic
 */
public class OutlineFillCombo extends JComboBox {

    public static String[] ids = {"outline", "fill"};//, "outline+fill"};
    public static ImageIcon[] images = {Workspace.createImageIcon("resources/outline_no_fill.png"), Workspace.createImageIcon("resources/no_outline_fill.png"), Workspace.createImageIcon("resources/outline_fill.png")};
    int width = 70;
    int height = 20;

    public String getStroke() {
        int sel = this.getSelectedIndex();
        if (sel >= 0 && sel < ids.length) {
            return ids[sel];
        }

        return "";
    }

    private OutlineFillCombo(Integer[] intArray) {
        super(intArray);
        ComboBoxRenderer renderer = new ComboBoxRenderer();
        renderer.setPreferredSize(new Dimension(20, 20));

        setRenderer(renderer);
        setMaximumRowCount(15);
    }

    public static OutlineFillCombo getInstance() {
        //Load the pet images and create an array of indexes.
        Integer[] intArray = new Integer[ids.length];
        for (int i = 0; i < ids.length; i++) {
            intArray[i] = new Integer(i);
        }

        OutlineFillCombo combo = new OutlineFillCombo(intArray);

        return combo;
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
        JComponent newContentPane = getInstance();
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

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
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
