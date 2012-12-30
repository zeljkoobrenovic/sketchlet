package net.sf.sketchlet.designer.editor.dnd;

import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.framework.model.ActiveRegion;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author zobrenovic
 */
public class SelectDropFile extends JDialog {
    private static final Logger log = Logger.getLogger(SelectDropFile.class);

    JTextArea textArea = new JTextArea(2, 12);
    String[] transformations = new String[]{
            "Import image",
            "Use Image URL"};
    JButton okButton = new JButton("OK", Workspace.createImageIcon("resources/ok.png"));
    JButton cancelButton = new JButton("Cancel", Workspace.createImageIcon("resources/cancel.png"));

    public SelectDropFile(JFrame frame, final File file, final ActiveRegion region, final int x, final int y) {
        super(frame);
        setModal(true);
        setTitle("Dropped File Action:");

        final JComboBox combo = new JComboBox();
        for (int i = 0; i < transformations.length; i++) {
            combo.addItem(transformations[i]);
        }

        JPanel buttons = new JPanel();
        buttons.add(okButton);
        buttons.add(cancelButton);

        getRootPane().setDefaultButton(okButton);

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (region != null) {
                    int index = combo.getSelectedIndex();
                    if (index >= 0 && index < transformations.length) {
                        if (index == 0) {
                            try {
                                if (region != null) {
                                    BufferedImage img = ImageIO.read(file);
                                    if (img != null) {
                                        region.setDrawnImage(0, img);
                                        region.setDrawnImageChanged(0, true);

                                        region.setX2Value(region.getX1Value() + img.getWidth());
                                        region.setY2Value(region.getY1Value() + img.getHeight());
                                        region.saveImage();
                                    }
                                } else {
                                    SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.SKETCHING);
                                    SketchletEditor.getInstance().fromFile(x, y, file);
                                }
                            } catch (Exception e) {
                                log.error(e);
                            }
                        } else if (index == 1) {
                            region.setImageUrlField(file.getPath());
                        }
                    }
                }
                setVisible(false);
            }
        });
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });

        textArea.setText("Dropped file: \"" + file.getPath() + "\"");
        textArea.setFont(new Font("Arial", Font.PLAIN, 9));
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        add(new JScrollPane(textArea), BorderLayout.SOUTH);
        add(combo, BorderLayout.NORTH);
        add(buttons, BorderLayout.CENTER);

        pack();
        if (frame != null) {
            this.setLocationRelativeTo(frame);
        }
        setVisible(true);
    }

    public static void main(String args[]) {
        new SelectDropAction(null, "=test", null);
    }
}
