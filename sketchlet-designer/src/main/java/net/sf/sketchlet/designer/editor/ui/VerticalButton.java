package net.sf.sketchlet.designer.editor.ui;

import net.sf.sketchlet.designer.editor.media.ImageOperations;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VerticalButton extends JButton {

    protected double angle = Math.PI / 2;

    public VerticalButton() {
        this(null, null);
    }

    public VerticalButton(Action a) {
        this();
        setAction(a);
    }

    public VerticalButton(Icon icon) {
        this(null, icon);
    }

    public VerticalButton(String text) {
        this(text, null);
    }

    public VerticalButton(String text, Icon icon) {
        super(text, icon);
    }

    public void paint(Graphics g) {
        final int w = getPreferredSize().width;
        final int h = getPreferredSize().height;
        BufferedImage image = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D imgG = image.createGraphics();
        imgG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        imgG.setColor(this.getBackground());
        imgG.fillRect(0, 0, h, w);
        imgG.setColor(this.getForeground());
        imgG.setFont(this.getFont());
        Icon icon = this.getIcon();
        int offset = icon == null ? 0 : icon.getIconWidth() + 2;
        imgG.drawString(this.getText(), offset + 5, 17);
        if (icon != null) {
            imgG.drawImage(((ImageIcon) icon).getImage(), 4, 4, null);
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(ImageOperations.rotateClockwise(image), 0, 0, null);
        this.paintBorder(g);
        if (this.getModel().isRollover()) {
            g2.setColor(Color.BLACK);
            g2.drawRect(2, 2, w - 5, h - 5);
        }
        if (this.getModel().isPressed()) {
            g2.setColor(new Color(0, 0, 0, 58));
            g2.fillRect(2, 2, w - 5, h - 5);
        }
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
        repaint();
    }

    public Dimension getPreferredSize() {
        Dimension d = new Dimension(super.getPreferredSize().height, super.getPreferredSize().width);
        return d;
    }

    protected int getLargest() {
        int w = super.getPreferredSize().width;
        int h = super.getPreferredSize().height;

        return Math.max(w, h);
    }
}
