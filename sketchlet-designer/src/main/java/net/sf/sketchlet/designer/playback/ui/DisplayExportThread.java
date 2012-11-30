package net.sf.sketchlet.designer.playback.ui;

import net.sf.sketchlet.common.base64.Base64Coder;
import net.sf.sketchlet.framework.blackboard.VariablesBlackboard;
import net.sf.sketchlet.framework.blackboard.evaluator.Evaluator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author zeljko
 */
public class DisplayExportThread implements Runnable {

    private Thread thread = new Thread(this);
    private PlaybackPanel playbackPanel;

    public DisplayExportThread(PlaybackPanel playbackPanel) {
        setPlaybackPanel(playbackPanel);
        thread.start();
    }

    public void run() {
        if (playbackPanel.getDisplay() != null && playbackPanel.getDisplay().exportDisplay.isSelected()) {
            try {
                String strType = playbackPanel.getDisplay().exportStrOn;
                if (strType != null && strType.equalsIgnoreCase("periodically")) {
                    String strTime = Evaluator.processText(playbackPanel.getDisplay().exportFrequency.getText(), "", "");
                    int nPause = (int) (1000 * Double.parseDouble(strTime));
                    while (PlaybackFrame.playbackFrame != null) {
                        exportDisplay();
                        Thread.sleep(nPause);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public void exportDisplay() {
        if (playbackPanel.getDisplay() != null && playbackPanel.getDisplay().exportDisplay.isSelected()) {
            if (playbackPanel.getDisplay().exportStrBase64VariableCombo.trim().length() > 0) {
                try {
                    String strVar = playbackPanel.getDisplay().exportStrBase64VariableCombo;
                    int x = 0;
                    int y = 0;
                    int w = playbackPanel.getWidth();
                    int h = playbackPanel.getHeight();
                    if (w > 0 && h > 0) {
                        BufferedImage image = playbackPanel.paintImage(x, y, w, h);
                        if (image != null) {
                            String strValue = new String(Base64Coder.encode(Base64Coder.getCompressedImageBytes(image, w, h)));
                            VariablesBlackboard.getInstance().updateVariable(strVar, strValue);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            if (!playbackPanel.getDisplay().exportStrFileVariableCombo.isEmpty()) {
                String strVar = playbackPanel.getDisplay().exportStrFileVariableCombo;
                String strFile = playbackPanel.getDisplay().exportFileField.getText();
                try {
                    int x = 0;
                    int y = 0;
                    int w = playbackPanel.getWidth();
                    int h = playbackPanel.getHeight();
                    if (w > 0 && h > 0) {
                        BufferedImage image = playbackPanel.paintImage(x, y, w, h);

                        if (image != null && strFile == null || strFile.equals("")) {
                            File file = File.createTempFile("capture_image_temp", ".png");
                            ImageIO.write(image, "PNG", file);
                            VariablesBlackboard.getInstance().updateVariable(strVar, file.getAbsolutePath());
                            file.deleteOnExit();
                        } else if (image != null) {
                            ImageIO.write(image, "PNG", new File(strFile));
                            VariablesBlackboard.getInstance().updateVariable(strVar, strFile);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setPlaybackPanel(PlaybackPanel playbackPanel) {
        this.playbackPanel = playbackPanel;
    }
}