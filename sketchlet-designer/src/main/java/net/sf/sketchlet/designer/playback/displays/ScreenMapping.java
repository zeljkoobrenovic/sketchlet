/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.playback.displays;

import net.sf.sketchlet.common.filter.Filters;
import net.sf.sketchlet.common.filter.PerspectiveFilter;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.model.evaluator.Evaluator;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.PrintWriter;

/**
 * @author zobrenovic
 */
public class ScreenMapping {

    public Object[][] pageClip = {
            {"screen x", "", Language.translate("position of display on the screen (left)")},
            {"screen y", "", Language.translate("position of display on the screen (top)")},
            {"screen width", "", Language.translate("width of the screen")},
            {"screen height", "", Language.translate("height of the screen")},
            {"visible area x", "", Language.translate("visible area position")},
            {"visible area y", "", Language.translate("visible area position")},
            {"visible area width", "", Language.translate("visible area width")},
            {"visible area height", "", Language.translate("visible area height")},
            {"calibration x1", "", Language.translate("0 to 1, x top left corner")},
            {"calibration y1", "", Language.translate("0 to 1, y top left corner")},
            {"calibration x2", "", Language.translate("0 to 1, x top right corner")},
            {"calibration y2", "", Language.translate("0 to 1, x top right corner")},
            {"calibration x3", "", Language.translate("0 to 1, x bottom right corner")},
            {"calibration y3", "", Language.translate("0 to 1, x bottom right corner")},
            {"calibration x4", "", Language.translate("0 to 1, x bottom left corner")},
            {"calibration y4", "", Language.translate("0 to 1, x bottom left corner")},
            {"window transparency", "", Language.translate("0.0 to 1.0")},
            {"window shape", "", Language.translate("rectangle, oval, round rectangle")},};
    public Object[][] transformations = {
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""},
            {"", ""}
    };
    public JComboBox exportFileVariableCombo = new JComboBox();
    public JComboBox exportBase64VariableCombo = new JComboBox();
    public JComboBox exportOn = new JComboBox();
    public String exportStrFileVariableCombo = "";
    public String exportStrBase64VariableCombo = "";
    public String exportStrOn = "";
    public JCheckBox exportDisplay = new JCheckBox(Language.translate("Save display"), false);
    public JTextField exportFileField = new JTextField(15);
    public JTextField exportFrequency = new JTextField("1.0", 5);
    public JLabel exportLabel1 = new JLabel(Language.translate(" every"));
    public JLabel exportLabel2 = new JLabel(Language.translate(" seconds"));
    public JLabel exportLabel3 = new JLabel(Language.translate(" Update variable"));
    public JLabel exportLabel4 = new JLabel(Language.translate(" with the path to temporary image file"));
    public JLabel exportLabel5 = new JLabel(Language.translate(" Update variable"));
    public JLabel exportLabel6 = new JLabel(Language.translate(" with base 64 encoded image"));
    public JButton exportFileButton;
    public JCheckBox enable = new JCheckBox(Language.translate("Active"), true);
    public JCheckBox showToolbar = new JCheckBox(Language.translate("Show Toolbar"), false);
    public JCheckBox showMaximized = new JCheckBox(Language.translate("Show Maximized"), true);
    public JComboBox screenIndex = new JComboBox();
    public JCheckBox showDecoration = new JCheckBox(Language.translate("Show Decoration"), false);
    public JCheckBox fitToScreen = new JCheckBox(Language.translate("Fit to Screen"), false);
    public JCheckBox alwaysOnTop = new JCheckBox(Language.translate("Always On Top"), false);
    public String showOnDisplay = "1";
    public String strName = "";
    public PerspectiveFilter perspectiveFilter = new PerspectiveFilter();
    public boolean inPerspective = false;
    public int p_x, p_y;

    public ScreenMapping() {
        this.screenIndex = new JComboBox();
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] allScreens = env.getScreenDevices();

        this.screenIndex.addItem("Same Screen");
        int i = 1;
        for (GraphicsDevice device : allScreens) {
            String strScreen = i + ": " + device.getDisplayMode().getWidth() + "x" + device.getDisplayMode().getHeight() + "";
            this.screenIndex.addItem(strScreen);
            i++;
        }
    }

    public ScreenMapping(boolean bActive) {
        this();
        this.enable.setSelected(bActive);
    }

    public void save(PrintWriter out) {
        try {
            out.println("   <display>");
            out.println("      <display-name>" + strName + "</display-name>");
            out.println("      <enable-display>" + enable.isSelected() + "</enable-display>");
            out.println("      <showToolbar>" + showToolbar.isSelected() + "</showToolbar>");
            out.println("      <showMaximized>" + showMaximized.isSelected() + "</showMaximized>");
            out.println("      <screenIndex>" + screenIndex.getSelectedIndex() + "</screenIndex>");
            out.println("      <showDecoration>" + showDecoration.isSelected() + "</showDecoration>");
            out.println("      <fitToScreen>" + fitToScreen.isSelected() + "</fitToScreen>");
            out.println("      <alwaysOnTop>" + alwaysOnTop.isSelected() + "</alwaysOnTop>");
            out.println("      <showOnDisplay>" + showOnDisplay + "</showOnDisplay>");

            out.println("         <cutFromSketch>");
            for (int i = 0; i < pageClip.length; i++) {
                out.println("            <cut>");
                out.println("               <cut-action>" + pageClip[i][0] + "</cut-action>");
                out.println("               <cut-param1>" + pageClip[i][1] + "</cut-param1>");
                out.println("               <cut-param2>" + pageClip[i][2] + "</cut-param2>");
                out.println("            </cut>");
            }
            out.println("         </cutFromSketch>");

            out.println("         <transformations>");
            for (int i = 0; i < transformations.length; i++) {
                out.println("            <transform>");
                out.println("               <transform-action>" + transformations[i][0] + "</transform-action>");
                out.println("               <transform-param1>" + transformations[i][1] + "</transform-param1>");
                out.println("            </transform>");
            }
            out.println("         </transformations>");
            out.println("         <export>");
            out.println("               <export>" + this.exportDisplay.isSelected() + "</export>");
            out.println("               <export-variable-path>" + this.exportStrFileVariableCombo + "</export-variable-path>");
            out.println("               <export-file-path>" + this.exportFileField.getText() + "</export-file-path>");
            out.println("               <export-on>" + this.exportStrOn + "</export-on>");
            out.println("               <export-frequency>" + this.exportFrequency.getText() + "</export-frequency>");
            out.println("         </export>");

            out.println("   </display>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean shouldProcessImage() {
        for (int i = 0; i < this.transformations.length; i++) {
            if (!((String) transformations[i][0]).isEmpty()) {
                return true;
            }
        }
        for (int i = 8; i < this.pageClip.length; i++) {
            if (!((String) pageClip[i][1]).isEmpty()) {
                return true;
            }
        }

        if (this.exportDisplay.isSelected()) {
            return true;
        }

        return false;
    }

    public BufferedImage doPerspective(BufferedImage image, int width, int height) {
        if (!this.fitToScreen.isSelected()) {
            for (int i = 8; i <= 15; i++) {
                if (this.pageClip[i][1].toString().length() == 0) {
                    this.inPerspective = false;
                    return image;
                }
            }
        }

        this.inPerspective = true;

        float x0 = 0.0f;
        float y0 = 0.0f;
        float x1 = 1.0f;
        float y1 = 0.0f;
        float x2 = 1.0f;
        float y2 = 1.0f;
        float x3 = 0.0f;
        float y3 = 1.0f;
        if (!pageClip[8][1].toString().isEmpty()) {
            try {
                x0 = Float.parseFloat(Evaluator.processText(pageClip[8][1].toString(), "", ""));
            } catch (Exception e) {
            }
        }
        if (!pageClip[9][1].toString().isEmpty()) {
            try {
                y0 = Float.parseFloat(Evaluator.processText(pageClip[9][1].toString(), "", ""));
            } catch (Exception e) {
            }
        }
        if (!pageClip[10][1].toString().isEmpty()) {
            try {
                x1 = Float.parseFloat(Evaluator.processText(pageClip[10][1].toString(), "", ""));
            } catch (Exception e) {
            }
        }
        if (!pageClip[11][1].toString().isEmpty()) {
            try {
                y1 = Float.parseFloat(Evaluator.processText(pageClip[11][1].toString(), "", ""));
            } catch (Exception e) {
            }
        }
        if (!pageClip[12][1].toString().isEmpty()) {
            try {
                x2 = Float.parseFloat(Evaluator.processText(pageClip[12][1].toString(), "", ""));
            } catch (Exception e) {
            }
        }
        if (!pageClip[13][1].toString().isEmpty()) {
            try {
                y2 = Float.parseFloat(Evaluator.processText(pageClip[13][1].toString(), "", ""));
            } catch (Exception e) {
            }
        }
        if (!pageClip[14][1].toString().isEmpty()) {
            try {
                x3 = Float.parseFloat(Evaluator.processText(pageClip[14][1].toString(), "", ""));
            } catch (Exception e) {
            }
        }
        if (!pageClip[15][1].toString().isEmpty()) {
            try {
                y3 = Float.parseFloat(Evaluator.processText(pageClip[15][1].toString(), "", ""));
            } catch (Exception e) {
            }
        }

        x0 *= width;
        x1 *= width;
        x2 *= width;
        x3 *= width;

        y0 *= height;
        y1 *= height;
        y2 *= height;
        y3 *= height;

        this.perspectiveFilter.setCorners(x0, y0, x1, y1, x2, y2, x3, y3);

        image = this.perspectiveFilter.processImage(image);

        p_x = (int) Math.min(x0, x1);
        p_x = (int) Math.min(p_x, x2);
        p_x = (int) Math.min(p_x, x3);
        p_y = (int) Math.min(y0, y1);
        p_y = (int) Math.min(p_y, y2);
        p_y = (int) Math.min(p_y, y3);

        return image;
    }

    public void enableControls() {
        boolean bEnable = this.exportDisplay.isSelected();

        this.exportFileField.setEnabled(bEnable);
        this.exportFileVariableCombo.setEnabled(bEnable);

        this.exportLabel3.setEnabled(bEnable);
        this.exportLabel4.setEnabled(bEnable);
        this.exportLabel5.setEnabled(bEnable);
        this.exportLabel6.setEnabled(bEnable);
        this.exportFileButton.setEnabled(bEnable);

        this.exportFrequency.setEnabled(bEnable && this.exportOn.getSelectedIndex() == 1);
        this.exportLabel1.setEnabled(bEnable && this.exportOn.getSelectedIndex() == 1);
        this.exportLabel2.setEnabled(bEnable && this.exportOn.getSelectedIndex() == 1);

        this.exportOn.setEnabled(bEnable);
    }

    public boolean isEmpty(String strDimension) {
        for (int i = 0; i < this.pageClip.length; i++) {
            if (((String) this.pageClip[i][0]).equalsIgnoreCase(strDimension)) {
                return ((String) this.pageClip[i][1]).equals("");
            }
        }

        return false;
    }

    public double getValue(String strDimension) {
        for (int i = 0; i < this.pageClip.length; i++) {
            if (((String) this.pageClip[i][0]).equalsIgnoreCase(strDimension)) {
                try {
                    return Double.parseDouble(Evaluator.processText((String) pageClip[i][1], "", ""));
                } catch (Exception e) {
                }
            }
        }

        return 0.0;
    }

    public String getString(String strDimension) {
        for (int i = 0; i < this.pageClip.length; i++) {
            if (((String) this.pageClip[i][0]).equalsIgnoreCase(strDimension)) {
                return (String) pageClip[i][1];
            }
        }

        return "";
    }

    public double[] getClipRect(Graphics2D g2) {
        double clipX = 0;
        double clipY = 0;
        double clipW = 0;
        double clipH = 0;

        for (int i = 0; i < this.pageClip.length; i++) {
            String strTranform = Evaluator.processText((String) pageClip[i][0], "", "");
            String strParam = Evaluator.processText((String) pageClip[i][1], "", "");

            if (strTranform.length() == 0 || strParam.length() == 0) {
                continue;
            }

            double param = 0.0;

            try {
                param = Double.parseDouble(strParam);
            } catch (Exception e) {
                continue;
            }

            if (strTranform.equals("visible area x")) {
                clipX = param;
            } else if (strTranform.equals("visible area y")) {
                clipY = param;
            } else if (strTranform.equals("visible area width")) {
                clipW = param;
            } else if (strTranform.equals("visible area height")) {
                clipH = param;
            } else if (g2 != null && strTranform.equals("transparency")) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) param));
            }
        }

        return new double[]{clipX, clipY, clipW, clipH};
    }

    public void clip(Graphics2D g2) {
    }

    public AffineTransform transform(AffineTransform g2) {

        for (int i = 0; i < this.transformations.length; i++) {
            String strTranform = Evaluator.processText((String) transformations[i][0], "", "");
            String strParam = Evaluator.processText((String) transformations[i][1], "", "");

            if (strTranform.length() == 0 || strParam.length() == 0) {
                continue;
            }
            double param = 0.0;
            try {
                param = Double.parseDouble(strParam);
            } catch (Exception e) {
                continue;
            }

            if (strTranform.equals("translate x")) {
                g2.translate(param, 0);
            } else if (strTranform.equals("translate y")) {
                g2.translate(0, param);
            } else if (strTranform.equals("scale x")) {
                g2.scale(param, 1);
            } else if (strTranform.equals("scale y")) {
                g2.scale(1, param);
            } else if (strTranform.equals("shear x")) {
                g2.shear(param, 0);
            } else if (strTranform.equals("shear y")) {
                g2.shear(0, param);
            } else if (strTranform.equals("rotate")) {
                g2.rotate(InteractionSpace.toRadians(param));
            }
        }

        return g2;
    }

    public BufferedImage filter(BufferedImage image) {

        for (int i = 0; i < this.transformations.length; i++) {
            String strTranform = Evaluator.processText((String) transformations[i][0], "", "");
            String strParam = Evaluator.processText((String) transformations[i][1], "", "");

            if (strTranform.isEmpty()) {
                continue;
            }

            image = Filters.filter(strTranform, strParam, image);
        }

        return image;
    }
}
