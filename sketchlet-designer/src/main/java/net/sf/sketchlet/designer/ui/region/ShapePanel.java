/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.region;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.regions.shapes.RegularPolygon;
import net.sf.sketchlet.designer.editor.regions.shapes.StarPolygon;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.util.Colors;
import net.sf.sketchlet.util.RefreshTime;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public class ShapePanel extends JPanel {

    JComboBox lineColor = new JComboBox();
    JComboBox lineThickness = new JComboBox();
    JComboBox lineStyle = new JComboBox();
    JComboBox fillColor = new JComboBox();
    public JTextField shapeArguments = null;
    public JComboBox shapeList = null;
    ImageIcon[] images;
    public static String[] shapeIDs = {"None", "Rectangle", "Oval", "Rounded Rectangle", "Triangle 1", "Triangle 2", "Line 1", "Line 2",
            "Horizontal Line", "Vertical Line",
            "RegularPolygon 5", "RegularPolygon 6", "RegularPolygon 7", "RegularPolygon 8", "RegularPolygon 10",
            "StarPolygon 3", "StarPolygon 4", "StarPolygon 5", "StarPolygon 6", "StarPolygon 7", "StarPolygon 8", "StarPolygon 10", "Pie Slice"};
    public static ImageIcon[] shapeIcons = {Workspace.createImageIcon("resources/no_shape.png"), Workspace.createImageIcon("resources/rectangle.png"),
            Workspace.createImageIcon("resources/oval.png"), Workspace.createImageIcon("resources/rounded_rectangle.png"),
            Workspace.createImageIcon("resources/triangle_1.png"), Workspace.createImageIcon("resources/triangle_2.png"),
            Workspace.createImageIcon("resources/line_1.png"), Workspace.createImageIcon("resources/line_2.png"),
            Workspace.createImageIcon("resources/line_3.png"), Workspace.createImageIcon("resources/line_4.png"),
            createRegularShapeIcon(5), createRegularShapeIcon(6), createRegularShapeIcon(7), createRegularShapeIcon(8), createRegularShapeIcon(10),
            createStarShapeIcon(3), createStarShapeIcon(4), createStarShapeIcon(5), createStarShapeIcon(6), createStarShapeIcon(7), createStarShapeIcon(8), createStarShapeIcon(10),
            Workspace.createImageIcon("resources/pie_slice.png")};
    JLabel shapeArgsLabel = new JLabel(Language.translate("inner radius (0..1)"));

    public static String[] getShapeStrings() {
        String[] shapeStrings = {Language.translate("None"), Language.translate("Rectangle"), Language.translate("Oval"), Language.translate("Rounded Rectangle"), Language.translate("Triangle"), Language.translate("Triangle"), Language.translate("Line"), Language.translate("Line"),
                Language.translate("Horizontal Line"), Language.translate("Vertical Line"), Language.translate("Regular Pentagon"), Language.translate("Hexagone"), Language.translate("Heptagone"), Language.translate("Octagone"), Language.translate("Decagone"),
                Language.translate("3-point Star"), Language.translate("4-point Star"), Language.translate("5-point Star"), Language.translate("6-point Star"), Language.translate("7-point Star"), Language.translate("8-point Star"), Language.translate("10-point Star"), Language.translate("Pie Slice")};
        return shapeStrings;
    }

    /*
    * Despite its use of EmptyBorder, this panel makes a fine content
    * pane because the empty border just increases the panel's size
    * and is "painted" on top of the panel's normal background.  In
    * other words, the JPanel fills its entire background if it's
    * opaque (which it is by default); adding a border doesn't change
    * that.
    */
    ActiveRegion action;

    public static ImageIcon createRegularShapeIcon(int n) {
        BufferedImage img = Workspace.createCompatibleImage(24, 24);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.draw(new RegularPolygon(12, 12, 11, n));

        g2.dispose();

        return new ImageIcon(img);
    }

    public static ImageIcon createStarShapeIcon(int n) {
        BufferedImage img = Workspace.createCompatibleImage(24, 24);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.draw(new StarPolygon(12, 12, 11, 5, n));

        g2.dispose();

        return new ImageIcon(img);
    }

    public ShapePanel(final ActiveRegion region) {
        this.action = region;

        //Load the pet bahabahaImages and create an array of indexes.
        images = new ImageIcon[getShapeStrings().length];
        Integer[] intArray = new Integer[getShapeStrings().length];
        for (int i = 0; i < getShapeStrings().length; i++) {
            intArray[i] = new Integer(i);
            images[i] = shapeIcons[i];
            if (images[i] != null) {
                images[i].setDescription("  " + getShapeStrings()[i]);
            }
        }

        //Create the combo box.
        this.shapeList = new JComboBox(intArray);
        this.shapeArguments = new JTextField(21);
        this.shapeArguments.setText(region.strShapeArgs);
        boolean bIsStar = region.shape.toLowerCase().startsWith("starpolygon");
        boolean bIsPie = region.shape.toLowerCase().startsWith("pie");
        boolean bRoundedRectangle = region.shape.toLowerCase().startsWith("rounded rectangle");
        this.shapeArguments.setEnabled(bIsStar || bIsPie || bRoundedRectangle);
        if (bIsStar) {
            this.shapeArgsLabel.setText(Language.translate("inner radius (0..1)"));
        } else if (bIsPie) {
            this.shapeArgsLabel.setText(Language.translate("start angle, extent, internal radius (0..1)"));
        } else if (bRoundedRectangle) {
            this.shapeArgsLabel.setText(Language.translate("rounded corner radius"));
        } else {
            this.shapeArgsLabel.setText("");
        }
        this.shapeArguments.addKeyListener(new KeyAdapter() {

            public void keyTyped(KeyEvent e) {
                region.strShapeArgs = shapeArguments.getText();
            }

            public void keyPressed(KeyEvent e) {
                keyTyped(e);
            }

            public void keyReleased(KeyEvent e) {
                keyTyped(e);
            }
        });

        int comboIndex = 0;

        for (int i = 0; i < shapeIDs.length; i++) {
            if (shapeIDs[i].equalsIgnoreCase(region.shape)) {
                comboIndex = i;
                break;
            }
        }

        this.shapeList.setSelectedIndex(comboIndex);
        this.shapeList.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int index = shapeList.getSelectedIndex();
                if (index < 0) {
                    index = 0;
                }
                region.shape = shapeIDs[index];
                if (region.shape.toLowerCase().startsWith("starpolygon")) {
                    shapeArguments.setEnabled(true);
                    shapeArgsLabel.setText(Language.translate("inner radius (0..1)"));
                    if (shapeArguments.getText().isEmpty()) {
                        shapeArguments.setText("0.5");
                    }
                } else if (region.shape.toLowerCase().startsWith("pie")) {
                    shapeArguments.setEnabled(true);
                    shapeArgsLabel.setText(Language.translate("start angle, extent, internal radius (0..1)"));
                    if (shapeArguments.getText().isEmpty()) {
                        shapeArguments.setText("0,45,0.0");
                    }
                } else if (region.shape.toLowerCase().startsWith("rounded rectangle")) {
                    shapeArguments.setEnabled(true);
                    shapeArgsLabel.setText(Language.translate("rounded corner radius"));
                    if (shapeArguments.getText().isEmpty()) {
                        shapeArguments.setText("10");
                    }
                } else {
                    shapeArguments.setEnabled(false);
                    shapeArgsLabel.setText("");
                }
                TutorialPanel.addLine("cmd", "Set region shape: " + region.shape.toLowerCase());

                SketchletEditor.editorPanel.repaint();
            }
        });
        this.shapeArguments.setText(region.strShapeArgs);
        this.shapeArguments.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                region.strShapeArgs = shapeArguments.getText();
                RefreshTime.update();
                SketchletEditor.editorPanel.repaint();
            }
        });

        ComboBoxRenderer renderer = new ComboBoxRenderer();
        renderer.setPreferredSize(new Dimension(200, 35));
        this.shapeList.setRenderer(renderer);
        this.shapeList.setMaximumRowCount(10);

        JPanel panel = new JPanel(new BorderLayout());
        JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        //Lay out the demo.
        panel1.add(new JLabel(Language.translate("Shape")));
        panel1.add(this.shapeList);
        panel1.add(shapeArguments);
        TutorialPanel.prepare(shapeArguments);
        panel1.add(shapeArgsLabel);
        panel2.add(new JLabel(Language.translate("Line Style")));
        panel2.add(lineStyle);
        panel2.add(new JLabel(Language.translate("Line Thickness")));
        panel2.add(lineThickness);
        panel2.add(new JLabel(Language.translate("Line Color")));
        panel2.add(lineColor);
        JButton btnLineColor = new JButton("...");
        btnLineColor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.editorPanel,
                        Language.translate("Choose Color"),
                        SketchletEditor.editorPanel.color);

                if (newColor != null) {
                    SketchletEditor.editorPanel.setLineColor(newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
                }
            }
        });
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnLineColor);
        panel2.add(new JLabel(Language.translate("Fill Color")));
        panel2.add(fillColor);
        JButton btnFillColor = new JButton("...");
        btnFillColor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.editorPanel,
                        Language.translate("Choose Color"),
                        SketchletEditor.editorPanel.color);

                if (newColor != null) {
                    SketchletEditor.editorPanel.setFillColor(newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
                }
            }
        });
        btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(btnFillColor);
        panel2.add(btnPanel);
        panel2.add(btnPanel);

        panel.add(panel1, BorderLayout.NORTH);
        panel.add(panel2, BorderLayout.CENTER);

        add(panel);

        lineColor.setEditable(true);
        fillColor.setEditable(true);

        lineThickness.removeAllItems();
        lineThickness.setEditable(true);
        lineThickness.addItem("");
        lineStyle.removeAllItems();
        lineStyle.setEditable(true);
        lineStyle.addItem("");
        lineStyle.addItem("no outline");
        lineStyle.addItem("------");
        lineStyle.addItem("regular");
        lineStyle.addItem("wobble");
        lineStyle.addItem("brush");
        lineStyle.addItem("empty");
        lineStyle.addItem("dashed 1");
        lineStyle.addItem("dashed 2");
        lineStyle.addItem("------");

        lineColor.removeAllItems();
        lineColor.setEditable(true);
        lineColor.addItem("");
        Colors.addColorNamesToCombo(lineColor);
        lineColor.addItem("------");
        fillColor.removeAllItems();
        fillColor.addItem("");
        fillColor.setEditable(true);
        Colors.addColorNamesToCombo(fillColor);
        fillColor.addItem("------");
        for (int i = 1; i <= 10; i++) {
            lineThickness.addItem("" + i);
        }
        lineThickness.addItem("------");
        for (String strVar : DataServer.variablesServer.variablesVector) {
            lineColor.addItem("=" + strVar);
            lineThickness.addItem("=" + strVar);
            lineStyle.addItem("=" + strVar);
            fillColor.addItem("=" + strVar);
        }

        lineColor.setSelectedItem(region.strLineColor);
        lineThickness.setSelectedItem(region.strLineThickness);
        lineStyle.setSelectedItem(region.strLineStyle);
        fillColor.setSelectedItem(region.strFillColor);

        setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        lineColor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (lineColor.getSelectedItem() != null) {
                    region.strLineColor = (String) lineColor.getSelectedItem();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });
        lineThickness.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (lineThickness.getSelectedItem() != null) {
                    region.strLineThickness = (String) lineThickness.getSelectedItem();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });
        lineStyle.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (lineStyle.getSelectedItem() != null) {
                    region.strLineStyle = (String) lineStyle.getSelectedItem();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });
        fillColor.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (fillColor.getSelectedItem() != null) {
                    region.strFillColor = (String) fillColor.getSelectedItem();
                    SketchletEditor.editorPanel.repaint();
                }
            }
        });
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ShapePanel.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
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
        JComponent newContentPane = new ShapePanel(null);
        newContentPane.setOpaque(true); //content panes must be opaque
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

    class ComboBoxRenderer extends JLabel
            implements ListCellRenderer {

        private Font uhOhFont;

        public ComboBoxRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        /*
         * This method finds the bahabahaImages and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and bahabahaImages.
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
            String pet = getShapeStrings()[selectedIndex];
            setIcon(icon);
            if (icon != null) {
                setText(pet);
                setFont(list.getFont());
            } else {
                setUhOhText(pet + " (no image available)",
                        list.getFont());
            }

            return this;
        }

        //Set the font and text when no bahabahaImages was found.
        protected void setUhOhText(String uhOhText, Font normalFont) {
            if (uhOhFont == null) { //lazily create this font
                uhOhFont = normalFont.deriveFont(Font.ITALIC);
            }
            setFont(uhOhFont);
            setText(uhOhText);
        }
    }

    public static int getShapeIndex(String strShape) {
        for (int i = 0; i < shapeIDs.length; i++) {
            if (shapeIDs[i].equalsIgnoreCase(strShape)) {
                return i;
            }
        }
        return 0;
    }
}
