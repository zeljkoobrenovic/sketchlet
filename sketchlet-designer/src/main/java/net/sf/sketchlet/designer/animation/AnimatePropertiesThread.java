package net.sf.sketchlet.designer.animation;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.framework.blackboard.evaluator.Evaluator;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.programming.timers.TimerThread;
import net.sf.sketchlet.framework.model.programming.timers.curves.Curve;
import net.sf.sketchlet.framework.model.programming.timers.curves.Curves;
import net.sf.sketchlet.util.RefreshTime;
import org.apache.log4j.Logger;

/**
 * @author zobrenovic
 */
public class AnimatePropertiesThread implements Runnable {
    private static final Logger log = Logger.getLogger(AnimatePropertiesThread.class);

    private Page page;
    private Thread thread = new Thread(this);
    private boolean stopped = false;

    public AnimatePropertiesThread(Page page) {
        this.page = page;
        thread.start();
    }

    public void stop() {
        setStopped(true);
    }

    public void run() {
        long startTime = System.currentTimeMillis();
        for (ActiveRegion region : page.getRegions().getRegions()) {
            region.setSpeedPrevX1(region.getX1Value());
            region.setSpeedPrevY1(region.getY1Value());
            region.setSpeedPrevX2(region.getX2Value());
            region.setSpeedPrevY2(region.getY2Value());

            region.setSpeedWidth(Math.abs(region.getX2Value() - region.getX1Value()));
            region.setSpeedHeight(Math.abs(region.getY2Value() - region.getY1Value()));

            region.setSpeedX(region.getX1Value());
            region.setSpeedY(region.getY1Value());

            region.setSpeedPrevDirection(region.getRotationValue());
        }
        while (!isStopped() && (SketchletEditor.getInstance() != null && (SketchletEditor.getInstance().getInternalPlaybackPanel() != null || PlaybackFrame.playbackFrame != null))) {
            try {
                if (shouldAnimate()) {
                    RefreshTime.update();
                    long time = System.currentTimeMillis() - startTime;

                    animateProperties(page, time);
                    animateTimers(page, time);

                    for (ActiveRegion region : page.getRegions().getRegions()) {
                        animateRegionFrame(region);
                        animateProperties(region, time);
                        animateRegionDirection(region);
                    }

                    if (SketchletEditor.getInstance().getInternalPlaybackPanel() != null) {
                        SketchletEditor.getInstance().getInternalPlaybackPanel().repaint();
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
        String strPause = region.processText(region.getAnimationFrameRateMs()).trim();

        int i = 0;

        try {
            i = (int) Double.parseDouble(region.processText(region.getImageIndex()));
        } catch (Exception e) {
        }

        try {
            if (!strPause.isEmpty()) {
                long pause = (long) Double.parseDouble(strPause);

                if (System.currentTimeMillis() - region.getLastFrameTime() > pause) {
                    region.setLastFrameTime(System.currentTimeMillis());
                    //region.imageIndex.setSelectedItem("" + (i % region.getImageCount() + 1));
                    region.setImageIndex("" + (i % region.getImageCount() + 1));

                    SketchletEditor.getInstance().repaintInternalPlaybackPanel();
                    PlaybackFrame.repaintAllFrames();
                }
            }
        } catch (Exception e) {
        }
    }

    public void animateRegionDirection(ActiveRegion region) {
        String strSpeed = region.processText((region.getSpeed())).trim();
        String strDirection = region.processText((region.getSpeedDirection())).trim();
        String strRotation = region.processText((region.getRotation())).trim();
        if (!strSpeed.isEmpty()) {
            try {
                if (Math.abs(region.getSpeedX() - region.getX1Value()) > 3) {
                    region.setSpeedX(region.getX1Value());
                }
                if (Math.abs(region.getSpeedY() - region.getY1Value()) > 3) {
                    region.setSpeedY(region.getY1Value());
                }
                region.setSpeedValue(Double.parseDouble(strSpeed));

                //speed = region.limitsHandler.processLimits("speed", speed, 0.0, 0, true);

                if (region.getSpeedValue() == 0.0) {
                    return;
                }

                double angle = region.getRotationValue();

                if (strDirection.equalsIgnoreCase("random") || strRotation.equalsIgnoreCase("random")) {
                    try {
                        angle = region.getSpeedPrevDirection() + (Math.random() - 0.5) * Math.PI / 3.5;
                        region.setSpeedPrevDirection(angle);

                        if (strRotation.equalsIgnoreCase("random")) {
                            region.setRotationValue(angle);
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
                String strHAlign = region.processText(region.getHorizontalAlignment());
                String strVAlign = region.processText(region.getVerticalAlignment());

                for (int i = 0; i < region.getSpeedValue() / 10; i++) {

                    region.setSpeedX(region.getSpeedX() + Math.cos(angle - Math.PI / 2));
                    region.setSpeedY(region.getSpeedY() + Math.sin(angle - Math.PI / 2));

                    region.setX1Value((int) region.getSpeedX());
                    region.setY1Value((int) region.getSpeedY());

                    region.setX1Value((int) region.getMotionController().processLimits("position x", region.getX1Value(), 0, region.getSpeedWidth(), false));
                    if (strHAlign.equalsIgnoreCase("center")) {
                        region.getMotionController().processLimits("position x", region.getX1Value() + region.getSpeedWidth() / 2, region.getSpeedWidth() / 2, region.getSpeedWidth() / 2, true);
                    } else if (strHAlign.equalsIgnoreCase("right")) {
                        region.getMotionController().processLimits("position x", region.getX1Value() + region.getSpeedWidth(), region.getSpeedWidth(), 0, true);
                    } else {
                        region.getMotionController().processLimits("position x", region.getX1Value(), 0, region.getSpeedWidth(), true);
                    }
                    region.setY1Value((int) region.getMotionController().processLimits("position y", region.getY1Value(), 0, region.getSpeedHeight(), false));
                    if (strVAlign.equalsIgnoreCase("center")) {
                        region.getMotionController().processLimits("position y", region.getY1Value() + region.getSpeedHeight() / 2, region.getSpeedHeight() / 2, region.getSpeedHeight() / 2, true);
                    } else if (strVAlign.equalsIgnoreCase("bottom")) {
                        region.getMotionController().processLimits("position y", region.getY1Value() + region.getSpeedHeight(), region.getSpeedHeight(), 0, true);
                    } else {
                        region.getMotionController().processLimits("position y", region.getY1Value(), 0, region.getSpeedHeight(), true);
                    }

                    region.setX2Value(region.getX1Value() + region.getSpeedWidth());
                    region.setY2Value(region.getY1Value() + region.getSpeedHeight());

                    if (!region.isWithinLimits(true) || region.getInteractionController().intersectsWithSolids(true)) {
                        region.getParent().getOverlapHelper().findNonOverlappingLocationPlayback(region);
                        break;
                    } else {
                        region.setSpeedPrevX1(region.getX1Value());
                        region.setSpeedPrevY1(region.getY1Value());
                        region.setSpeedPrevX2(region.getX2Value());
                        region.setSpeedPrevY2(region.getY2Value());
                    }
                }
                region.getInteractionController().processInteractionEvents(true, region.getParent().getPage().getActiveTimers(), region.getParent().getPage().getActiveMacros());
                region.getSketch().updateConnectors(region, true);

            } catch (NumberFormatException nfe) {
            } catch (Throwable e) {
                log.error(e);
            }
        }
    }

    public void animateTimers(Page page, long time) {
        for (TimerThread tt : page.getActiveTimers()) {
            tt.tick(time);
        }
    }

    public void animateProperties(Page page, long time) {
        for (int i = 0; i < page.getPropertiesAnimation().length; i++) {
            String property = page.getPropertiesAnimation()[i][0];
            String type = Evaluator.processText(page.getPropertiesAnimation()[i][1], "", "");
            String start = Evaluator.processText(page.getPropertiesAnimation()[i][2], "", "");
            String end = Evaluator.processText(page.getPropertiesAnimation()[i][3], "", "");
            String cycle = Evaluator.processText(page.getPropertiesAnimation()[i][4], "", "");
            if (type != null && !type.isEmpty() && !start.isEmpty() && !end.isEmpty() && !cycle.isEmpty()) {
                double nStart = Double.parseDouble(Evaluator.processText(page.getPropertiesAnimation()[i][2], "", ""));
                double nEnd = Double.parseDouble(Evaluator.processText(page.getPropertiesAnimation()[i][3], "", ""));
                double nCycle = Double.parseDouble(Evaluator.processText(page.getPropertiesAnimation()[i][4], "", ""));
                String strCurve = Evaluator.processText(page.getPropertiesAnimation()[i][5], "", "");

                if (nCycle <= 0) {
                    continue;
                }
                long step = 2 * time / (int) (nCycle * 1000);
                double relPos = (time % (int) (nCycle * 1000)) / (nCycle * 1000);
                Curve curve = null;
                if (!strCurve.isEmpty()) {
                    curve = Curves.getGlobalCurves().getCurve(strCurve);
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
        for (int i = 0; i < region.getPropertiesAnimation().length; i++) {
            if (region.getPropertiesAnimation()[i][1] == null) {
                continue;
            }
            String property = region.getPropertiesAnimation()[i][0];
            String type = region.processText(region.getPropertiesAnimation()[i][1]);
            String start = region.processText(region.getPropertiesAnimation()[i][2]);
            String end = region.processText(region.getPropertiesAnimation()[i][3]);
            String cycle = region.processText(region.getPropertiesAnimation()[i][4]);
            if (type != null && !type.isEmpty() && !start.isEmpty() && !end.isEmpty() && !cycle.isEmpty()) {
                double nStart = Double.parseDouble(region.processText(region.getPropertiesAnimation()[i][2]));
                double nEnd = Double.parseDouble(region.processText(region.getPropertiesAnimation()[i][3]));
                double nCycle = Double.parseDouble(region.processText(region.getPropertiesAnimation()[i][4]));
                String strCurve = region.processText(region.getPropertiesAnimation()[i][5]);

                long step = 2 * time / (int) (nCycle * 1000);
                double relPos = (time % (int) (nCycle * 1000)) / (nCycle * 1000);
                Curve curve = null;
                if (!strCurve.isEmpty()) {
                    curve = Curves.getGlobalCurves().getCurve(strCurve);
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
        for (int i = 0; i < region.getPropertiesAnimation().length; i++) {
            String type = region.getPropertiesAnimation()[i][1];
            String start = region.getPropertiesAnimation()[i][2];
            String end = region.getPropertiesAnimation()[i][3];
            String cycle = region.getPropertiesAnimation()[i][4];
            if (type != null && !type.isEmpty() && !start.isEmpty() && !end.isEmpty() && !cycle.isEmpty()) {
                return true;
            }

        }
        if (!region.getSpeed().isEmpty()) {
            return true;
        }

        if (region.getImageCount() > 1 && !region.getAnimationFrameRateMs().isEmpty()) {
            return true;
        }

        return false;
    }

    public boolean shouldAnimate(Page page) {
        for (int i = 0; i < page.getPropertiesAnimation().length; i++) {
            String type = page.getPropertiesAnimation()[i][1];
            String start = page.getPropertiesAnimation()[i][2];
            String end = page.getPropertiesAnimation()[i][3];
            String cycle = page.getPropertiesAnimation()[i][4];
            if (type != null && !type.isEmpty() && !start.isEmpty() && !end.isEmpty() && !cycle.isEmpty()) {
                return true;
            }

        }
        for (ActiveRegion r : page.getRegions().getRegions()) {
            if (shouldAnimate(r)) {
                return true;
            }
        }
        if (page.getActiveTimers().size() > 0) {
            return true;
        }
        return false;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }
}
