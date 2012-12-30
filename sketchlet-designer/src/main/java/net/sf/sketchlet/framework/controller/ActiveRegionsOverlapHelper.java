package net.sf.sketchlet.framework.controller;

import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.Page;

/**
 * @author zeljko
 */
public class ActiveRegionsOverlapHelper {
    private Page page;

    public ActiveRegionsOverlapHelper(Page page) {
        this.page = page;
    }

    public static void findNonOverlappingLocationPlayback(ActiveRegion a) {
        int ox1 = a.getX1Value();
        int ox2 = a.getX2Value();
        int oy1 = a.getY1Value();
        int oy2 = a.getY2Value();
        for (int i = 1; i < 500; i++) {
            a.setX1Value(ox1 + i);
            a.setX2Value(ox2 + i);
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
            }
            a.setY1Value(oy1 + i);
            a.setY2Value(oy2 + i);
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }
            a.setX1Value(ox1 - i);
            a.setX2Value(ox2 - i);
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
            }
            a.setY1Value(oy1 - i);
            a.setY2Value(oy2 - i);
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }

            a.setX1Value(ox1 + i);
            a.setX2Value(ox2 + i);
            a.setY1Value(oy1 + i);
            a.setY2Value(oy2 + i);
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }

            a.setX1Value(ox1 - i);
            a.setX2Value(ox2 - i);
            a.setY1Value(oy1 + i);
            a.setY2Value(oy2 + i);
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }

            a.setX1Value(ox1 + i);
            a.setX2Value(ox2 + i);
            a.setY1Value(oy1 - i);
            a.setY2Value(oy2 - i);
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }

            a.setX1Value(ox1 - i);
            a.setX2Value(ox2 - i);
            a.setY1Value(oy1 - i);
            a.setY2Value(oy2 - i);
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }
        }
    }

    public static void findNonOverlappingLocation(ActiveRegion a) {
        int ox1 = a.getX1Value();
        int oy1 = a.getY1Value();
        int ox2 = a.getX2Value();
        int oy2 = a.getY2Value();
        for (int i = 1; i < 500; i++) {
            a.setX1Value(ox1 + i);
            a.setX2Value(ox2 + i);
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
            }
            a.setY1Value(oy1 + i);
            a.setY2Value(oy2 + i);
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }
            a.setX1Value(ox1 - i);
            a.setX2Value(ox2 - i);
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
            }
            a.setY1Value(oy1 - i);
            a.setY2Value(oy2 - i);
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }

            a.setX1Value(ox1 + i);
            a.setX2Value(ox2 + i);
            a.setY1Value(oy1 + i);
            a.setY2Value(oy2 + i);
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }

            a.setX1Value(ox1 - i);
            a.setX2Value(ox2 - i);
            a.setY1Value(oy1 + i);
            a.setY2Value(oy2 + i);
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }

            a.setX1Value(ox1 + i);
            a.setX2Value(ox2 + i);
            a.setY1Value(oy1 - i);
            a.setY2Value(oy2 - i);
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }

            a.setX1Value(ox1 - i);
            a.setX2Value(ox2 - i);
            a.setY1Value(oy1 - i);
            a.setY2Value(oy2 - i);
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.setX1Value(ox1);
                a.setX2Value(ox2);
                a.setY1Value(oy1);
                a.setY2Value(oy2);
            }
        }
    }
}
