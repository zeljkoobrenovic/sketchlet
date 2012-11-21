/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui.toolbars;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.editor.tool.BucketTool;
import net.sf.sketchlet.designer.editor.tool.PenTool;
import net.sf.sketchlet.designer.editor.tool.ShapeTool;
import net.sf.sketchlet.designer.editor.tool.Tool;
import net.sf.sketchlet.designer.editor.tool.TransparentColorTool;
import net.sf.sketchlet.designer.editor.tool.stroke.BrushStroke;
import net.sf.sketchlet.designer.editor.tool.stroke.CompositeStroke;
import net.sf.sketchlet.designer.editor.tool.stroke.StrokeCombo;
import net.sf.sketchlet.designer.editor.tool.stroke.WobbleStroke;
import net.sf.sketchlet.designer.tools.log.ActivityLog;
import net.sf.sketchlet.designer.editor.ui.OutlineFillCombo;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

/**
 * @author zobrenovic
 */
public class ColorToolbar extends JToolBar {

    public JSlider slider;
    public JSlider sliderWatering;
    public StrokeCombo strokeType;
    public OutlineFillCombo outlineCombo;
    public JLabel toolLabel = new JLabel(Language.translate("Tool: "));
    Color[] colors = {
            Color.BLACK, Color.DARK_GRAY, Color.GRAY, Color.WHITE, Color.RED,
            Color.GREEN.darker(), Color.BLUE, Color.CYAN, Color.MAGENTA, Color.ORANGE, Color.YELLOW
    };
    JButton buttons[] = new JButton[colors.length];
    JToolBar leftPanel = new JToolBar();

    public ColorToolbar() {
        setFloatable(false);
        FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEFT);
        flowLayout1.setVgap(0);
        flowLayout1.setHgap(0);
        FlowLayout flowLayout2 = new FlowLayout(FlowLayout.LEFT);
        flowLayout2.setVgap(0);
        flowLayout2.setHgap(0);

        leftPanel.setLayout(flowLayout1);
        leftPanel.setFloatable(false);
        leftPanel.setBorder(BorderFactory.createEmptyBorder());

        toolLabel.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(toolLabel);

        JToolBar rightPanel = new JToolBar();
        rightPanel.setLayout(flowLayout1);
        rightPanel.setFloatable(false);
        rightPanel.setBorder(BorderFactory.createEmptyBorder());

        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());
        ActionListener l = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (SketchletEditor.getInstance().getTool() != null) {
                    SketchletEditor.getInstance().getTool().deactivate();
                }
                JButton button = (JButton) e.getSource();
                button.setMargin(new Insets(0, 0, 0, 0));
                SketchletEditor.getInstance().setColor(button.getBackground());
                SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.SKETCHING);
                if (!(SketchletEditor.getInstance().getTool() instanceof PenTool || SketchletEditor.getInstance().getTool() instanceof ShapeTool || SketchletEditor.getInstance().getTool() instanceof BucketTool || SketchletEditor.getInstance().getTool() instanceof TransparentColorTool)) {
                    SketchletEditor.getInstance().setTool(SketchletEditor.getInstance().getPenTool(), null);
                }
                SketchletEditor.getInstance().createGraphics();
                SketchletEditor.getInstance().setCursor();
                ActivityLog.log("setColorQuick", button.getBackground().getRed() + " " + button.getBackground().getGreen() + " " + button.getBackground().getBlue());
            }
        };
        for (int j = 0; j < colors.length; j++) {
            buttons[j] = new JButton("   ") {

                public void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    super.paintComponent(g);

                    int w = getWidth();
                    int h = getHeight();
                    g2.setPaint(getBackground());
                    g2.fillRect(3, 3, w - 6, h - 6);
                    super.paintBorder(g);
                }
            };
            // button.setFocusPainted(false);
            buttons[j].setBackground(colors[j]);
            buttons[j].setForeground(colors[j]);
            buttons[j].addActionListener(l);
        }
        slider = new JSlider(JSlider.HORIZONTAL, 1, 12, 3);
        slider.setToolTipText(Language.translate("Pen/brush width (shortcut keys: + bigger, - smaller)"));
        slider.setPreferredSize(new Dimension(80, 20));
        slider.addMouseListener(new MouseAdapter() {

            public void mouseReleased() {
                ActivityLog.log("strokeSlider", slider.getValue() + "");
            }
        });
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                setStroke();
            }
        });
        sliderWatering = new JSlider(JSlider.HORIZONTAL, 0, 10, 0);
        sliderWatering.setToolTipText(Language.translate("Color transparency"));
        sliderWatering.setPreferredSize(new Dimension(50, 20));
        sliderWatering.addMouseListener(new MouseAdapter() {

            public void mouseReleased() {
                ActivityLog.log("sliderWatering", sliderWatering.getValue() + "");
            }
        });
        sliderWatering.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                setWatering();
            }
        });

        strokeType = StrokeCombo.getInstance();
        strokeType.setToolTipText(Language.translate("Pen stroke type"));
        strokeType.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (strokeType.getSelectedItem() != null) {
                    ActivityLog.log("setStrokeType", strokeType.getSelectedItem() + "");
                }
                setStroke();
            }
        });
        strokeType.setSelectedIndex(0);

        outlineCombo = OutlineFillCombo.getInstance();
        outlineCombo.setToolTipText(Language.translate("Outline or fill the shape"));
        outlineCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (outlineCombo.getSelectedItem() != null) {
                    ActivityLog.log("setOutline", outlineCombo.getSelectedItem() + "");
                }
                setOutline();
            }
        });
        outlineCombo.setSelectedIndex(0);

        toolLabel.setHorizontalTextPosition(JLabel.LEFT);

        for (int i = 0; buttons != null && i < buttons.length; i++) {
            rightPanel.add(buttons[i]);
        }

        final JButton moreColors = new JButton(Workspace.createImageIcon("resources/palette.png"));
        moreColors.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Color newColor = JColorChooser.showDialog(
                        SketchletEditor.getInstance(),
                        Language.translate("Choose Color"),
                        SketchletEditor.getInstance().getColor());

                if (newColor != null) {
                    SketchletEditor.getInstance().setColor(newColor);
                    ActivityLog.log("setColorMore", newColor.getRed() + " " + newColor.getGreen() + " " + newColor.getBlue());
                }
            }
        });

        rightPanel.add(moreColors);
        JLabel label = new JLabel(Language.translate("   watering "));
        label.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(label);
        rightPanel.add(label);
        rightPanel.add(sliderWatering);

        strokeType.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(strokeType);

        outlineCombo.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(outlineCombo);

        add(leftPanel, BorderLayout.WEST);
        add(new JPanel());
        add(rightPanel, BorderLayout.EAST);
    }

    public void toolChanged(Tool tool) {
        ImageIcon icon = tool.getIcon();
        this.toolLabel.setIcon(icon);
        this.toolLabel.setToolTipText(tool.getName());
        setToolSettings();
    }

    public void setToolSettings() {
        leftPanel.removeAll();
        Tool tool = SketchletEditor.getInstance().getTool();
        leftPanel.add(toolLabel);

        if (tool.needSetting("outline/filling")) {
            leftPanel.add(outlineCombo);
        }
        if (tool.needSetting("stroke type")) {
            leftPanel.add(strokeType);
        }
        if (tool.needSetting("stroke width")) {
            leftPanel.add(slider);
        }
        leftPanel.revalidate();
    }

    public void setStroke() {
        String strStroke = strokeType.getStroke();
        float value = ((Integer) slider.getValue()).floatValue();

        Stroke s = getStroke(strStroke, value);
        if (s != null) {
            SketchletEditor.getInstance().setStroke(s);
            SketchletEditor.getInstance().createGraphics();
        }
        SketchletEditor.getInstance().requestFocus();
    }

    public void setOutline() {
        SketchletEditor.getInstance().setOutlineType(outlineCombo.getSelectedIndex());
        SketchletEditor.getInstance().requestFocus();
    }

    public void setWatering() {
        float value = ((Integer) sliderWatering.getValue()).floatValue();

        SketchletEditor.getInstance().setWatering(1 - value / sliderWatering.getMaximum());
        SketchletEditor.getInstance().createGraphics();
        SketchletEditor.getInstance().requestFocus();
    }

    public static Stroke getStroke(String strStroke, float value) {
        if (strStroke.equals("regular")) {
            return new BasicStroke(value, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        } else if (strStroke.equals("brush")) {
            return new BrushStroke(value, (float) Math.min(value / 20.f, 1.0f));
        } else if (strStroke.equals("wobble")) {
            float v = 1.0f;
            if (value < 5) {
                v = 1.0f;
            } else if (value < 10) {
                v = 1.5f;
            } else {
                v = 2.0f;
            }

            return new WobbleStroke((int) value, v, v);
        } else if (strStroke.equals("empty")) {
            return new CompositeStroke(new BasicStroke(value), new BasicStroke(0.5f));
        } else if (strStroke.equals("dashed 1")) {
            float[] dashPattern = {10, 10, 10, 10};
            return new BasicStroke(value, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10,
                    dashPattern, 0);
        } else if (strStroke.equals("dashed 2")) {
            float[] dashPattern = {30, 10, 10, 10};
            return new BasicStroke(value, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10,
                    dashPattern, 0);
        } else if (strStroke.equals("no outline")) {
            return null;
        }

        return new BasicStroke(value, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
        // return new BrushStroke(value, (float) Math.min(value / 20.f, 1.0f));
    }
}
