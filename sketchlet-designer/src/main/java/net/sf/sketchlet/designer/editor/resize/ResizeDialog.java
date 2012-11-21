/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.resize;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.util.SpringUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author zobrenovic
 */
public class ResizeDialog extends JDialog {

    private JFrame frame;
    private double aspectRatio = 0.0;
    private ResizeInterface resizeInterface;
    private ButtonGroup group = new ButtonGroup();
    private JRadioButton radioImage = new JRadioButton(Language.translate("Scale Image"));
    private JRadioButton radioCanvas = new JRadioButton(Language.translate("Resize Canvas Only"));

    public ResizeDialog(JFrame frame, String strTitle, final ResizeInterface resizeInterface) {
        this(frame, strTitle, resizeInterface, resizeInterface.getImageWidth(), resizeInterface.getImageHeight());
    }

    public ResizeDialog(JFrame frame, String strTitle, final ResizeInterface resizeInterface, int w, int h) {
        this.frame = frame;
        this.resizeInterface = resizeInterface;
        setTitle(strTitle);
        this.setModal(true);
        setLayout(new BorderLayout());
        Panel p = new Panel();
        add(p);

        final JButton btnYes = new JButton(Language.translate("OK"));
        final JButton btnNo = new JButton(Language.translate("Cancel"));
        final JTextField width = new JTextField("" + w);
        final JTextField height = new JTextField("" + h);

        radioImage.setSelected(true);

        group.add(radioImage);
        group.add(radioCanvas);

        btnYes.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                int w = Integer.parseInt(width.getText());
                int h = Integer.parseInt(height.getText());
                if (radioImage.isSelected()) {
                    resizeInterface.resizeImage(w, h);
                } else {
                    resizeInterface.resizeCanvas(w, h);
                }
                setVisible(false);
            }
        });
        btnNo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                setVisible(false);
            }
        });

        JPanel panel = new JPanel();
        panel.add(btnYes);
        panel.add(btnNo);

        final JCheckBox keepAspect = new JCheckBox(Language.translate("Maintain Aspect Ratio"), true);

        JPanel centerPanel = new JPanel(new SpringLayout());
        aspectRatio = (double) resizeInterface.getImageWidth() / (double) resizeInterface.getImageHeight();

        width.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (keepAspect.isSelected()) {
                    try {
                        int w = Integer.parseInt(width.getText());

                        if (aspectRatio > 0 && w > 0) {
                            int h = (int) (w / aspectRatio);
                            height.setText("" + h);
                            btnYes.setEnabled(true);
                        } else {
                            btnYes.setEnabled(false);
                        }
                    } catch (Exception ex) {
                        btnYes.setEnabled(false);
                    }
                }
            }
        });
        height.addKeyListener(new KeyAdapter() {

            public void keyReleased(KeyEvent e) {
                if (keepAspect.isSelected()) {
                    try {
                        int h = Integer.parseInt(height.getText());

                        if (aspectRatio > 0 && h > 0) {
                            int w = (int) (h * aspectRatio);
                            width.setText("" + w);
                            btnYes.setEnabled(true);
                        } else {
                            btnYes.setEnabled(false);
                        }
                    } catch (Exception ex) {
                        btnYes.setEnabled(false);
                    }
                }
            }
        });

        centerPanel.add(new JLabel(Language.translate("Width: ")));
        centerPanel.add(width);
        centerPanel.add(new JLabel(Language.translate("Height: ")));
        centerPanel.add(height);
        centerPanel.add(radioImage);
        centerPanel.add(radioCanvas);

        SpringUtilities.makeCompactGrid(centerPanel,
                3, 2, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad    }


        add(keepAspect, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);

        this.getRootPane().setDefaultButton(btnYes);

        pack();
        this.setLocationRelativeTo(frame);
        setVisible(true);
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

    }
}
