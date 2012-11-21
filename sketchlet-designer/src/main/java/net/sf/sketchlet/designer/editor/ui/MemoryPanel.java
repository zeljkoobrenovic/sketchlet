/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.ui;

import net.sf.sketchlet.designer.editor.SketchletEditor;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author zobrenovic
 */
public class MemoryPanel extends JToolBar implements Runnable {

    JProgressBar progress = new JProgressBar();
    Thread t = new Thread(this);
    boolean stopped = false;
    JButton gc = new JButton("gc");

    public MemoryPanel() {
        this.setFloatable(false);
        progress.setMaximum((int) (Runtime.getRuntime().maxMemory() / 1000));
        progress.setStringPainted(true);
        // gc.setMargin(new Insets(0, 0, 0, 0));
        add(progress);
        add(gc);
        gc.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Runtime.getRuntime().gc();
                Runtime.getRuntime().runFinalization();
            }
        });
        progress.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(progress);
        this.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(this);
        gc.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(gc);
        t.start();
    }

    public void stop() {
        this.stopped = true;
    }

    public void run() {
        while (!stopped && SketchletEditor.getInstance() != null) {
            try {
                Runtime r = Runtime.getRuntime();
                progress.setValue((int) ((r.totalMemory() - r.freeMemory()) / 1000));
                progress.setString((r.totalMemory() - r.freeMemory()) / (1024 * 1024) + "M / " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "M / " + Runtime.getRuntime().maxMemory() / (1024 * 1024) + "M");
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
