package net.sf.sketchlet.designer.playback.ui;

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
        if (PlaybackPanel.getRefreshCaptureThread() == null) {
            PlaybackPanel.setRefreshCaptureThread(new RefreshScreenCaptureThread());
        }
    }

    public static void stop() {
        if (PlaybackPanel.getRefreshCaptureThread() != null) {
            PlaybackPanel.getRefreshCaptureThread().stopped = true;
            PlaybackPanel.setRefreshCaptureThread(null);
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
            if (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getInternalPlaybackPanel() != null) {
                SketchletEditor.getInstance().getInternalPlaybackPanel().repaint();
            }
            RefreshTime.update();

            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
        }
    }
}
