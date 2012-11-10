/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.animation;

import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.data.ActiveRegions;
import net.sf.sketchlet.designer.data.Evaluator;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.programming.timers.TimerThread;
import net.sf.sketchlet.designer.programming.timers.curves.Curve;
import net.sf.sketchlet.designer.programming.timers.curves.Curves;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.util.RefreshTime;
import org.apache.log4j.Logger;

/**
 * @author zobrenovic
 */
public class AnimatePropertiesThread implements Runnable {
    private static final Logger log = Logger.getLogger(AnimatePropertiesThread.class);

    Page page;
    Thread t = new Thread(this);
    public boolean stopped = false;

    public AnimatePropertiesThread(Page page) {
        this.page = page;
        t.start();
    }

    public void stop() {
        stopped = true;
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        for (ActiveRegion region : page.regions.regions) {
            region.speed_prevX1 = region.playback_x1;
            region.speed_prevY1 = region.playback_y1;
            region.speed_prevX2 = region.playback_x2;
            region.speed_prevY2 = region.playback_y2;

            region.speed_w = Math.abs(region.playback_x2 - region.playback_x1);
            region.speed_h = Math.abs(region.playback_y2 - region.playback_y1);

            region.speed_x = region.playback_x1;
            region.speed_y = region.playback_y1;

            region.speed_prevDirection = region.playback_rotation;
        }
        while (!stopped && (SketchletEditor.editorPanel != null && (SketchletEditor.editorPanel.internalPlaybackPanel != null || PlaybackFrame.playbackFrame != null))) {
            try {
                if (shouldAnimate()) {
                    RefreshTime.update();
                    long time = System.currentTimeMillis() - startTime;

                    animateProperties(page, time);
                    animateTimers(page, time);

                    for (ActiveRegion region : page.regions.regions) {
                        animateRegionFrame(region);
                        animateProperties(region, time);
                        animateRegionDirection(region);
                    }

                    if (SketchletEditor.editorPanel.internalPlaybackPanel != null) {
                        SketchletEditor.editorPanel.internalPlaybackPanel.repaint();
                    }
                    PlaybackFrame.repaintAllFrames();
                    Thread.sleep(50);
                } else {
                    Thread.sleep(300);
                    startTime = System.currentTimeMillis();
                }
                // System.out.print(".");
            } catch (Throwable e) {
                log.error(e);
            }
        }
    }

    public void animateProperties(String propertiesAnimation[][], long time) {
    }

    public void animateRegionFrame(ActiveRegion region) {
        String strPause = region.processText(region.strAnimationMs).trim();

        int i = 0;

        try {
            i = (int) Double.parseDouble(region.processText(region.strImageIndex));
        } catch (Exception e) {
        }

        try {
            if (!strPause.isEmpty()) {
                long pause = (long) Double.parseDouble(strPause);

                if (System.currentTimeMillis() - region.lastFrameTime > pause) {
                    region.lastFrameTime = System.currentTimeMillis();
                    //region.imageIndex.setSelectedItem("" + (i % region.getImageCount() + 1));
                    region.strImageIndex = "" + (i % region.getImageCount() + 1);

                    SketchletEditor.editorPanel.repaintInternalPlaybackPanel();
                    PlaybackFrame.repaintAllFrames();
                }
            }
        } catch (Exception e) {
        }
    }

    public void animateRegionDirection(ActiveRegion region) {
        String strSpeed = region.processText((region.strSpeed)).trim();
        String strDirection = region.processText((region.strSpeedDirection)).trim();
        String strRotation = region.processText((region.strRotate)).trim();
        if (!strSpeed.isEmpty()) {
            try {
                if (Math.abs(region.speed_x - region.playback_x1) > 3) {
                    region.speed_x = region.playback_x1;
                }
                if (Math.abs(region.speed_y - region.playback_y1) > 3) {
                    region.speed_y = region.playback_y1;
                }
                region.speed = Double.parseDouble(strSpeed);

                //speed = region.limitsHandler.processLimits("speed", speed, 0.0, 0, true);

                if (region.speed == 0.0) {
                    return;
                }

                double angle = region.playback_rotation;

                if (strDirection.equalsIgnoreCase("random") || strRotation.equalsIgnoreCase("random")) {
                    try {
                        angle = region.speed_prevDirection + (Math.random() - 0.5) * Math.PI / 3.5;
                        region.speed_prevDirection = angle;

                        if (strRotation.equalsIgnoreCase("random")) {
                            region.playback_rotation = angle;
                        }
                    } catch (Exception ex) {
                    }
                }
                if (!strDirection.equalsIgnoreCase("random") && !strDirection.isEmpty()) {
                    try {
                        angle = InteractionSpace.toRadians(Double.parseDouble(strDirection));
                    } catch (Exception ex) {
                    }
                }
                String strHAlign = region.processText(region.strHAlign);
                String strVAlign = region.processText(region.strVAlign);

                for (int i = 0; i < region.speed / 10; i++) {

                    region.speed_x += Math.cos(angle - Math.PI / 2);
                    region.speed_y += Math.sin(angle - Math.PI / 2);

                    region.playback_x1 = (int) region.speed_x;
                    region.playback_y1 = (int) region.speed_y;

                    region.playback_x1 = (int) region.limitsHandler.processLimits("position x", region.playback_x1, 0, region.speed_w, false);
                    if (strHAlign.equalsIgnoreCase("center")) {
                        region.limitsHandler.processLimits("position x", region.playback_x1 + region.speed_w / 2, region.speed_w / 2, region.speed_w / 2, true);
                    } else if (strHAlign.equalsIgnoreCase("right")) {
                        region.limitsHandler.processLimits("position x", region.playback_x1 + region.speed_w, region.speed_w, 0, true);
                    } else {
                        region.limitsHandler.processLimits("position x", region.playback_x1, 0, region.speed_w, true);
                    }
                    region.playback_y1 = (int) region.limitsHandler.processLimits("position y", region.playback_y1, 0, region.speed_h, false);
                    if (strVAlign.equalsIgnoreCase("center")) {
                        region.limitsHandler.processLimits("position y", region.playback_y1 + region.speed_h / 2, region.speed_h / 2, region.speed_h / 2, true);
                    } else if (strVAlign.equalsIgnoreCase("bottom")) {
                        region.limitsHandler.processLimits("position y", region.playback_y1 + region.speed_h, region.speed_h, 0, true);
                    } else {
                        region.limitsHandler.processLimits("position y", region.playback_y1, 0, region.speed_h, true);
                    }

                    region.playback_x2 = region.playback_x1 + region.speed_w;
                    region.playback_y2 = region.playback_y1 + region.speed_h;

                    // editorPanel.repaint();

                    // region.interactionHandler.processInteractionEvents(true, region.parent.sketch.activeTimers, region.parent.sketch.activeMacros);

                    if (!region.isWithinLimits(true) || region.interactionHandler.intersectsWithSolids(true)) {
                        ActiveRegions.findNonOverlapingLocationPlayback(region);
                        break;
                    } else {
                        region.speed_prevX1 = region.playback_x1;
                        region.speed_prevY1 = region.playback_y1;
                        region.speed_prevX2 = region.playback_x2;
                        region.speed_prevY2 = region.playback_y2;
                    }
                }
                region.interactionHandler.processInteractionEvents(true, region.parent.page.activeTimers, region.parent.page.activeMacros);
                region.getSketch().updateConnectors(region, true);

            } catch (NumberFormatException nfe) {
            } catch (Throwable e) {
                log.error(e);
            }
        }
    }

    public void animateTimers(Page page, long time) {
        for (TimerThread tt : page.activeTimers) {
            tt.tick(time);
        }
    }

    public void animateProperties(Page page, long time) {
        for (int i = 0; i < page.propertiesAnimation.length; i++) {
            String property = page.propertiesAnimation[i][0];
            String type = Evaluator.processText(page.propertiesAnimation[i][1], "", "");
            String start = Evaluator.processText(page.propertiesAnimation[i][2], "", "");
            String end = Evaluator.processText(page.propertiesAnimation[i][3], "", "");
            String cycle = Evaluator.processText(page.propertiesAnimation[i][4], "", "");
            if (type != null && !type.isEmpty() && !start.isEmpty() && !end.isEmpty() && !cycle.isEmpty()) {
                double nStart = Double.parseDouble(Evaluator.processText(page.propertiesAnimation[i][2], "", ""));
                double nEnd = Double.parseDouble(Evaluator.processText(page.propertiesAnimation[i][3], "", ""));
                double nCycle = Double.parseDouble(Evaluator.processText(page.propertiesAnimation[i][4], "", ""));
                String strCurve = Evaluator.processText(page.propertiesAnimation[i][5], "", "");

                if (nCycle <= 0) {
                    continue;
                }
                long step = 2 * time / (int) (nCycle * 1000);
                double relPos = (time % (int) (nCycle * 1000)) / (nCycle * 1000);
                Curve curve = null;
                if (!strCurve.isEmpty()) {
                    curve = Curves.globalCurves.getCurve(strCurve);
                }

                if (type.equalsIgnoreCase("loop forever")) {
                    if (curve != null) {
                        relPos = curve.getRelativeValue(nCycle, relPos);
                    }
                    page.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                } else if (type.equalsIgnoreCase("loop once")) {
                    if (step <= 1) {
                        if (curve != null) {
                            relPos = curve.getRelativeValue(nCycle, relPos);
                        }
                        page.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                    } else if (!page.getProperty(property).equalsIgnoreCase(end)) {
                        page.setProperty(property, end);
                    }

                } else if (type.equalsIgnoreCase("puls forever")) {
                    if (step % 2 == 0) {
                        if (curve != null) {
                            relPos = curve.getRelativeValue(nCycle, 2 * relPos);
                        } else {
                            relPos = 2 * relPos;
                        }
                        page.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                    } else {
                        if (curve != null) {
                            relPos = curve.getRelativeValue(nCycle, 2 * (1 - relPos));
                        } else {
                            relPos = 2 * (1 - relPos);
                        }
                        page.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                    }
                } else if (type.equalsIgnoreCase("puls once")) {
                    if (step <= 1) {
                        if (step % 2 == 0) {
                            if (curve != null) {
                                relPos = curve.getRelativeValue(nCycle, 2 * relPos);
                            } else {
                                relPos = 2 * relPos;
                            }
                            page.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                        } else {
                            if (curve != null) {
                                relPos = curve.getRelativeValue(nCycle, 2 * (1 - relPos));
                            } else {
                                relPos = 2 * (1 - relPos);
                            }
                            page.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                        }
                    } else if (!page.getProperty(property).equalsIgnoreCase(start)) {
                        page.setProperty(property, start);
                    }
                }
            }
        }

    }

    public void animateProperties(ActiveRegion region, long time) {
        for (int i = 0; i < region.propertiesAnimation.length; i++) {
            if (region.propertiesAnimation[i][1] == null) {
                continue;
            }
            String property = region.propertiesAnimation[i][0];
            String type = region.processText(region.propertiesAnimation[i][1]);
            String start = region.processText(region.propertiesAnimation[i][2]);
            String end = region.processText(region.propertiesAnimation[i][3]);
            String cycle = region.processText(region.propertiesAnimation[i][4]);
            if (type != null && !type.isEmpty() && !start.isEmpty() && !end.isEmpty() && !cycle.isEmpty()) {
                double nStart = Double.parseDouble(region.processText(region.propertiesAnimation[i][2]));
                double nEnd = Double.parseDouble(region.processText(region.propertiesAnimation[i][3]));
                double nCycle = Double.parseDouble(region.processText(region.propertiesAnimation[i][4]));
                String strCurve = region.processText(region.propertiesAnimation[i][5]);

                long step = 2 * time / (int) (nCycle * 1000);
                double relPos = (time % (int) (nCycle * 1000)) / (nCycle * 1000);
                Curve curve = null;
                if (!strCurve.isEmpty()) {
                    curve = Curves.globalCurves.getCurve(strCurve);
                }

                if (type.equalsIgnoreCase("loop forever")) {
                    if (curve != null) {
                        relPos = curve.getRelativeValue(nCycle, relPos);
                    }
                    region.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                } else if (type.equalsIgnoreCase("loop once")) {
                    if (step <= 1) {
                        if (curve != null) {
                            relPos = curve.getRelativeValue(nCycle, relPos);
                        }
                        region.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                    } else if (!region.getProperty(property).equalsIgnoreCase(end)) {
                        region.setProperty(property, end);
                    }
                } else if (type.equalsIgnoreCase("puls forever")) {
                    if (step % 2 == 0) {
                        if (curve != null) {
                            relPos = curve.getRelativeValue(nCycle, 2 * relPos);
                        } else {
                            relPos = 2 * relPos;
                        }
                        region.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                    } else {
                        if (curve != null) {
                            relPos = curve.getRelativeValue(nCycle, 2 * (1 - relPos));
                        } else {
                            relPos = 2 * (1 - relPos);
                        }
                        region.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                    }
                } else if (type.equalsIgnoreCase("puls once")) {
                    if (step <= 1) {
                        if (step % 2 == 0) {
                            if (curve != null) {
                                relPos = curve.getRelativeValue(nCycle, 2 * relPos);
                            } else {
                                relPos = 2 * relPos;
                            }
                            region.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                        } else {
                            if (curve != null) {
                                relPos = curve.getRelativeValue(nCycle, 2 * (1 - relPos));
                            } else {
                                relPos = 2 * (1 - relPos);
                            }
                            region.setProperty(property, "" + (nStart + (nEnd - nStart) * relPos));
                        }
                    } else if (!region.getProperty(property).equalsIgnoreCase(start)) {
                        region.setProperty(property, start);
                    }
                }
            }
        }

    }

    public boolean shouldAnimate() {
        return shouldAnimate(page);
    }

    public boolean shouldAnimate(ActiveRegion region) {
        for (int i = 0; i < region.propertiesAnimation.length; i++) {
            String type = region.propertiesAnimation[i][1];
            String start = region.propertiesAnimation[i][2];
            String end = region.propertiesAnimation[i][3];
            String cycle = region.propertiesAnimation[i][4];
            if (type != null && !type.isEmpty() && !start.isEmpty() && !end.isEmpty() && !cycle.isEmpty()) {
                return true;
            }

        }
        if (!region.strSpeed.isEmpty()) {
            return true;
        }

        if (region.getImageCount() > 1 && !region.strAnimationMs.isEmpty()) {
            return true;
        }

        return false;
    }

    public boolean shouldAnimate(Page page) {
        for (int i = 0; i < page.propertiesAnimation.length; i++) {
            String type = page.propertiesAnimation[i][1];
            String start = page.propertiesAnimation[i][2];
            String end = page.propertiesAnimation[i][3];
            String cycle = page.propertiesAnimation[i][4];
            if (type != null && !type.isEmpty() && !start.isEmpty() && !end.isEmpty() && !cycle.isEmpty()) {
                return true;
            }

        }
        for (ActiveRegion r : page.regions.regions) {
            if (shouldAnimate(r)) {
                return true;
            }
        }
        if (page.activeTimers.size() > 0) {
            return true;
        }
        return false;
    }
}
