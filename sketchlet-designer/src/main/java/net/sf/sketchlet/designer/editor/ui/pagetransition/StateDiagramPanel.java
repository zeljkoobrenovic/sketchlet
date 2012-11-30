package net.sf.sketchlet.designer.editor.ui.pagetransition;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author zeljko
 */
public class StateDiagramPanel extends JPanel {
    private static final Logger log = Logger.getLogger(StateDiagramPanel.class);
    private BufferedImage image;

    public StateDiagramPanel(BufferedImage image) {
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        log.info("paintComponent");
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, this.getWidth(), this.getHeight());
        g2.drawImage(image, 0, 0, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return  new Dimension(image.getWidth(), image.getHeight());
    }
}
