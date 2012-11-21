/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.ui.desktop;

import net.sf.sketchlet.designer.Workspace;

import javax.swing.*;
import java.awt.*;

public class SketchletDesignerSplashScreen extends JFrame implements Runnable {

    //Thread t = new Thread(this);
    Image image;
    String message = "";

    public SketchletDesignerSplashScreen() {
        this.setTitle("Sketchlet");
        image = Workspace.createImageIcon("resources/splash.png").getImage();
        setSize(image.getWidth(null), image.getHeight(null));
        this.setLocationRelativeTo(null);
        this.setAlwaysOnTop(true);
        this.setUndecorated(true);
        this.setIconImage(Workspace.createImageIcon("resources/sketcify24x24.png", "").getImage());
        setVisible(true);
    }

    public void run() {
        try {
            Thread.sleep(15000);
            setVisible(false);
        } catch (Exception e) {
        }
    }

    public void setMessage(String msg) {
        this.message = msg;
        repaint();
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        int w = image.getWidth(null);
        int h = image.getHeight(null);

        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w - 1, h - 1);

        g2.drawImage(image, 0, 0, null);

        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, w - 1, h - 1);

        Font font = g2.getFont().deriveFont(11).deriveFont(Font.ITALIC);
        g2.setFont(font);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("1.0", 431, 70);

        if (!message.isEmpty()) {
            font = g2.getFont().deriveFont(10).deriveFont(Font.PLAIN);
            g2.setFont(font);
            g2.setColor(Color.YELLOW);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillRect(1, 150, 400, 20);
            g2.setColor(Color.BLACK);
            g2.drawString(message, 5, 165);
        }
    }

    public static void main(String args[]) {
        new SketchletDesignerSplashScreen();
    }
}
