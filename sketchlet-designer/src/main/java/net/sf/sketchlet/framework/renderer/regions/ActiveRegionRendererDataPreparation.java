package net.sf.sketchlet.framework.renderer.regions;

import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.framework.model.ActiveRegion;
import org.apache.log4j.Logger;

import java.awt.*;

public class ActiveRegionRendererDataPreparation {
    private static final Logger log = Logger.getLogger(ActiveRegionRenderer.class);
    ActiveRegionRenderer activeRegionRenderer;
    private final ActiveRegion activeRegion;

    public ActiveRegionRendererDataPreparation(ActiveRegionRenderer activeRegionRenderer) {
        this.activeRegion = activeRegionRenderer.getRegion();
    }

    public synchronized void prepare(boolean inPlaybackMode, boolean processLimitsEnabled) {
        if (activeRegion == null) {
            return;
        }
        try {
            RestoreData prev = new RestoreData();
            prev.x1 = activeRegion.getX1Value();
            prev.y1 = activeRegion.getY1Value();
            prev.x2 = activeRegion.getX2Value();
            prev.y2 = activeRegion.getY2Value();

            prev.rotation = activeRegion.getRotationValue();
            prev.shearX = activeRegion.getShearXValue();
            prev.shearY = activeRegion.getShearYValue();

            prepareAlignment();
            prepareXY(processLimitsEnabled);
            prepareSize();
            prepareTrajectory(processLimitsEnabled);
            prepareRotation();
            prepareShear();
            prepareInteractionEvents(inPlaybackMode);

            prepareIntersectionAndGrouping(inPlaybackMode, processLimitsEnabled, prev);
        } catch (Throwable e) {
            log.error(e);
            e.printStackTrace();
        }
    }

    private void prepareAlignment() {
        activeRegion.setHorizontalAlignment(activeRegion.processText(activeRegion.getHorizontalAlignment()));
        activeRegion.setVerticalAlignment(activeRegion.processText(activeRegion.getVerticalAlignment()));
    }


    private void prepareXY(boolean processLimitsEnabled) {
        String strX = activeRegion.processText(activeRegion.getX()).trim();
        String strY = activeRegion.processText(activeRegion.getY()).trim();
        String strX1 = activeRegion.processText(activeRegion.getX1()).trim();
        String strY1 = activeRegion.processText(activeRegion.getY1()).trim();
        String strX2 = activeRegion.processText(activeRegion.getX2()).trim();
        String strY2 = activeRegion.processText(activeRegion.getY2()).trim();
        String strRelX1 = activeRegion.processText(activeRegion.getRelativeX()).trim();
        String strRelY1 = activeRegion.processText(activeRegion.getRelativeY()).trim();
        int w = activeRegion.getWidthValue();
        int h = activeRegion.getHeightValue();

        if (!strX.isEmpty()) {
            try {
                int x = (int) InteractionSpace.getSketchX(Double.parseDouble(strX));

                if (activeRegion.getHorizontalAlignment().equalsIgnoreCase("center")) {
                    x -= w / 2;
                } else if (activeRegion.getHorizontalAlignment().equalsIgnoreCase("right")) {
                    x -= w;
                } else {
                }

                activeRegion.setX1Value(x);
                activeRegion.setX2Value(activeRegion.getX1Value() + w);

                if (processLimitsEnabled) {
                    activeRegion.processLimitsX();
                }
            } catch (Throwable e) {
            }
        } else if (!strRelX1.isEmpty()) {
            try {
                double limitsX[] = activeRegion.getMotionController().getLimits("position x", 0, w);
                int x = (int) InteractionSpace.getSketchX(limitsX[0] + (limitsX[1] - limitsX[0]) * Math.min(1.0, Double.parseDouble(strRelX1)));
                if (activeRegion.getHorizontalAlignment().equalsIgnoreCase("center")) {
                    x -= w / 2;
                } else if (activeRegion.getHorizontalAlignment().equalsIgnoreCase("right")) {
                    x -= w;
                } else {
                }

                activeRegion.setX2Value(activeRegion.getX2Value() + x - activeRegion.getX1Value());
                activeRegion.setX1Value(x);
                if (processLimitsEnabled) {
                    activeRegion.processLimitsX();
                }
            } catch (Throwable e) {
            }
        } else if (!strX1.isEmpty() || !strX2.isEmpty()) {
            if (!strX1.isEmpty()) {
                try {
                    int x = (int) InteractionSpace.getSketchX(Double.parseDouble(strX1));
                    activeRegion.setX1Value(x);
                } catch (Throwable e) {
                }
            }
            if (!strX2.isEmpty()) {
                try {
                    int x = (int) InteractionSpace.getSketchX(Double.parseDouble(strX2));
                    activeRegion.setX2Value(x);
                } catch (Throwable e) {
                }
            }
            if (processLimitsEnabled) {
                activeRegion.processLimitsX();
            }
        }
        if (!strY.isEmpty()) {
            try {
                int y = (int) InteractionSpace.getSketchY(Double.parseDouble(strY));
                if (activeRegion.getVerticalAlignment().equalsIgnoreCase("center")) {
                    y -= h / 2;
                } else if (activeRegion.getVerticalAlignment().equalsIgnoreCase("bottom")) {
                    y -= h;
                } else {
                }

                activeRegion.setY1Value(y);
                activeRegion.setY2Value(activeRegion.getY1Value() + h);
                if (processLimitsEnabled) {
                    activeRegion.processLimitsY();
                }
            } catch (Throwable e) {
            }
        } else if (!strRelY1.isEmpty()) {
            try {
                double limitsY[] = activeRegion.getMotionController().getLimits("position y", 0, h);
                int y = (int) InteractionSpace.getSketchY(limitsY[0] + (limitsY[1] - limitsY[0]) * Math.min(1.0, Double.parseDouble(strRelY1)));
                if (activeRegion.getVerticalAlignment().equalsIgnoreCase("center")) {
                    y -= h / 2;
                } else if (activeRegion.getVerticalAlignment().equalsIgnoreCase("bottom")) {
                    y -= h;
                } else {
                }

                activeRegion.setY2Value(activeRegion.getY2Value() + y - activeRegion.getY1Value());
                activeRegion.setY1Value(y);
                if (processLimitsEnabled) {
                    activeRegion.processLimitsY();
                }
            } catch (Throwable e) {
            }
        } else if (!strY1.isEmpty() || !strY2.isEmpty()) {
            if (!strY1.isEmpty()) {
                try {
                    int y = (int) InteractionSpace.getSketchX(Double.parseDouble(strY1));
                    activeRegion.setY1Value(y);
                } catch (Throwable e) {
                }
            }
            if (!strY2.isEmpty()) {
                try {
                    int y = (int) InteractionSpace.getSketchX(Double.parseDouble(strY2));
                    activeRegion.setY2Value(y);
                } catch (Throwable e) {
                }
            }
            if (processLimitsEnabled) {
                activeRegion.processLimitsY();
            }
        }
    }

    private void prepareSize() {
        String strWidth = activeRegion.processText(activeRegion.getWidth()).trim();
        String strHeight = activeRegion.processText(activeRegion.getHeight()).trim();
        int w = activeRegion.getWidthValue();
        int h = activeRegion.getHeightValue();
        if (!strWidth.isEmpty()) {
            try {
                int oldW = activeRegion.getX2Value() - activeRegion.getX1Value();
                int centerX = activeRegion.getX1Value() + oldW / 2;
                int newW = (int) InteractionSpace.getSketchWidth(Double.parseDouble(strWidth));
                if (activeRegion.getHorizontalAlignment().equalsIgnoreCase("center")) {
                    activeRegion.setX1Value(centerX - newW / 2);
                    activeRegion.setX2Value(centerX + newW / 2);
                } else if (activeRegion.getHorizontalAlignment().equalsIgnoreCase("right")) {
                    activeRegion.setX1Value(activeRegion.getX2Value() - newW);
                } else {
                    activeRegion.setX2Value(activeRegion.getX1Value() + newW);
                }
                w = newW;
            } catch (Throwable e) {
                //log.error(e);
            }
        }
        if (!strHeight.isEmpty()) {
            try {
                int oldH = activeRegion.getY2Value() - activeRegion.getY1Value();
                int centerY = activeRegion.getY1Value() + oldH / 2;
                int newH = (int) InteractionSpace.getSketchHeight(Double.parseDouble(strHeight));
                if (activeRegion.getVerticalAlignment().equalsIgnoreCase("center")) {
                    activeRegion.setY1Value(centerY - newH / 2);
                    activeRegion.setY2Value(centerY + newH / 2);
                } else if (activeRegion.getVerticalAlignment().equalsIgnoreCase("bottom")) {
                    activeRegion.setY1Value(activeRegion.getY2Value() - newH);
                } else {
                    activeRegion.setY2Value(activeRegion.getY1Value() + newH);
                }
                h = newH;
            } catch (Throwable e) {
                //log.error(e);
            }
        }
    }

    private void prepareRotation() {
        String strRotate = activeRegion.processText(activeRegion.getRotation()).trim();
        if (!strRotate.isEmpty()) {
            try {
                double rotDeg = Double.parseDouble(strRotate);
                rotDeg = InteractionSpace.wrapPhysicalAngle(rotDeg);

                activeRegion.setRotationValue(InteractionSpace.toRadians(rotDeg));
            } catch (Throwable e) {
            }
        }
    }

    private void prepareTrajectory(boolean processLimitsEnabled) {
        int w = activeRegion.getWidthValue();
        int h = activeRegion.getHeightValue();
        String strTrajectoryPosition = activeRegion.processText(activeRegion.getTrajectoryPosition()).trim();
        if (!activeRegion.isInTrajectoryMode() && !activeRegion.isInTrajectoryMode2() && !activeRegion.getTrajectory1().trim().isEmpty()) {
            if (!strTrajectoryPosition.isEmpty() && activeRegion.isStickToTrajectoryEnabled()) {
                try {
                    double pos = Double.parseDouble(strTrajectoryPosition);
                    if (!Double.isNaN(pos)) {
                        Point p = activeRegionRenderer.getTrajectoryDrawingLayer().getTrajectoryPoint(pos);
                        if (p != null) {
                            activeRegion.setX1Value((int) (p.x - w * activeRegion.getCenterOfRotationX()));
                            activeRegion.setY1Value((int) (p.y - h * activeRegion.getCenterOfRotationY()));
                            activeRegion.setX2Value(activeRegion.getX1Value() + w);
                            activeRegion.setY2Value(activeRegion.getY1Value() + h);
                            if (processLimitsEnabled) {
                                activeRegion.processLimitsX();
                                activeRegion.processLimitsY();
                                activeRegion.processLimitsTrajectory(p);
                            }
                            if (activeRegion.isChangingOrientationOnTrajectoryEnabled()) {
                                activeRegion.setRotationValue(activeRegionRenderer.getTrajectoryDrawingLayer().trajectoryOrientationFromPoint);
                            }
                        }
                    }
                } catch (Throwable e) {
                    //log.error(e);
                }
            }
        }
    }

    private void prepareIntersectionAndGrouping(boolean inPlaybackMode, boolean processLimitsEnabled, RestoreData prev) {
        if (activeRegion != null && activeRegion.getInteractionController() != null) {
            if (!activeRegion.isWithinLimits(inPlaybackMode) || activeRegion.getInteractionController().intersectsWithSolids(inPlaybackMode)) {
                activeRegion.getParent().getOverlapHelper().findNonOverlappingLocation(activeRegion);
                activeRegion.setRotationValue(prev.rotation);
                activeRegion.setShearXValue(prev.shearX);
                activeRegion.setShearYValue(prev.shearY);
            } else {
                prepareGrouping(inPlaybackMode, processLimitsEnabled, prev);
            }
        }
    }

    private void prepareGrouping(boolean inPlaybackMode, boolean processLimitsEnabled, RestoreData prev) {
        if (!activeRegion.getRegionGrouping().isEmpty()) {
            for (ActiveRegion as : activeRegion.getParent().getRegions()) {
                if (as != activeRegion && as.getRegionGrouping().equals(activeRegion.getRegionGrouping())) {
                    if (inPlaybackMode) {
                        as.setX1Value(as.getX1Value() + activeRegion.getX1Value() - prev.x1);
                        as.setY1Value(as.getY1Value() + activeRegion.getY1Value() - prev.y1);
                        as.setX2Value(as.getX2Value() + activeRegion.getX2Value() - prev.x2);
                        as.setY2Value(as.getY2Value() + activeRegion.getY2Value() - prev.y2);
                        if (processLimitsEnabled) {
                            as.getMotionController().processLimits("position x", as.getX1Value(), 0, 0, true);
                            as.getMotionController().processLimits("position y", as.getY1Value(), 0, 0, true);
                        }
                    } else {
                        if (activeRegion.getX1Value() - prev.x1 != 0 || activeRegion.getY1Value() - prev.y1 != 0) {
                            as.setX1Value(as.getX1Value() + activeRegion.getX1Value() - prev.x1);
                            as.setY1Value(as.getY1Value() + activeRegion.getY1Value() - prev.y1);
                            as.setX2Value(as.getX2Value() + activeRegion.getX2Value() - prev.x2);
                            as.setY2Value(as.getY2Value() + activeRegion.getY2Value() - prev.y2);
                            if (processLimitsEnabled) {
                                as.getMotionController().processLimits("position x", as.getX1Value(), 0, 0, true);
                                as.getMotionController().processLimits("position y", as.getY1Value(), 0, 0, true);
                            }
                        }
                    }
                }
            }
        }
    }

    private void prepareInteractionEvents(boolean inPlaybackMode) {
        if (activeRegion.getParent() != null) {
            activeRegion.getInteractionController().processInteractionEvents(inPlaybackMode, activeRegion.getParent().getPage().getActiveTimers(), activeRegion.getParent().getPage().getActiveMacros());
            activeRegion.getSketch().updateConnectors(activeRegion, inPlaybackMode);
        }
    }

    private void prepareShear() {
        String strShearX = activeRegion.processText(activeRegion.getShearX()).trim();
        String strShearY = activeRegion.processText(activeRegion.getShearY()).trim();
        if (!strShearX.isEmpty()) {
            try {
                activeRegion.setShearXValue(Double.parseDouble(strShearX));
            } catch (Throwable e) {
            }
        } else {
            activeRegion.setShearXValue(0.0);
        }

        if (!strShearY.isEmpty()) {
            try {
                activeRegion.setShearYValue(Double.parseDouble(strShearY));
            } catch (Throwable e) {
            }
        } else {
            activeRegion.setShearYValue(0.0);
        }
    }

    class RestoreData {
        int x1, y1, x2, y2;
        double rotation, shearX, shearY;
    }
}