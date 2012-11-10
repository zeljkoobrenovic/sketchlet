/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.playback;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.util.RefreshTime;

/**
 * @author zobrenovic
 */
public class RefreshScreenCaptureThread implements Runnable {

    Thread t = new Thread(this);
    boolean stopped = false;

    private RefreshScreenCaptureThread() {
        t.start();
    }

    public static void start() {
        if (PlaybackPanel.refreshCaptureThread == null) {
            PlaybackPanel.refreshCaptureThread = new RefreshScreenCaptureThread();
        }
    }

    public static void stop() {
        if (PlaybackPanel.refreshCaptureThread != null) {
            PlaybackPanel.refreshCaptureThread.stopped = true;
            PlaybackPanel.refreshCaptureThread = null;
        }
    }

    public void run() {
        while (!this.stopped) {
            RefreshTime.update();
            if (PlaybackFrame.playbackFrame != null) {
                for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
                    if (PlaybackFrame.playbackFrame[i] != null) {
                        PlaybackFrame.playbackFrame[i].playbackPanel.repaint();
                    }
                }
            }
            if (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.internalPlaybackPanel != null) {
                SketchletEditor.editorPanel.internalPlaybackPanel.repaint();
            }
            RefreshTime.update();

            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
    }
}
