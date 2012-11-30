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
        int ox1 = a.x1;
        int ox2 = a.x2;
        int oy1 = a.y1;
        int oy2 = a.y2;
        for (int i = 1; i < 500; i++) {
            a.x1 = ox1 + i;
            a.x2 = ox2 + i;
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
            }
            a.y1 = oy1 + i;
            a.y2 = oy2 + i;
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.y1 = oy1;
                a.y2 = oy2;
            }
            a.x1 = ox1 - i;
            a.x2 = ox2 - i;
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
            }
            a.y1 = oy1 - i;
            a.y2 = oy2 - i;
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 + i;
            a.x2 = ox2 + i;
            a.y1 = oy1 + i;
            a.y2 = oy2 + i;
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 - i;
            a.x2 = ox2 - i;
            a.y1 = oy1 + i;
            a.y2 = oy2 + i;
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 + i;
            a.x2 = ox2 + i;
            a.y1 = oy1 - i;
            a.y2 = oy2 - i;
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 - i;
            a.x2 = ox2 - i;
            a.y1 = oy1 - i;
            a.y2 = oy2 - i;
            if (a.isWithinLimits(true) && !a.getInteractionController().intersectsWithSolids(true)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }
        }
    }

    public static void findNonOverlappingLocation(ActiveRegion a) {
        int ox1 = a.x1;
        int oy1 = a.y1;
        int ox2 = a.x2;
        int oy2 = a.y2;
        for (int i = 1; i < 500; i++) {
            a.x1 = ox1 + i;
            a.x2 = ox2 + i;
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
            }
            a.y1 = oy1 + i;
            a.y2 = oy2 + i;
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.y1 = oy1;
                a.y2 = oy2;
            }
            a.x1 = ox1 - i;
            a.x2 = ox2 - i;
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
            }
            a.y1 = oy1 - i;
            a.y2 = oy2 - i;
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 + i;
            a.x2 = ox2 + i;
            a.y1 = oy1 + i;
            a.y2 = oy2 + i;
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 - i;
            a.x2 = ox2 - i;
            a.y1 = oy1 + i;
            a.y2 = oy2 + i;
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 + i;
            a.x2 = ox2 + i;
            a.y1 = oy1 - i;
            a.y2 = oy2 - i;
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }

            a.x1 = ox1 - i;
            a.x2 = ox2 - i;
            a.y1 = oy1 - i;
            a.y2 = oy2 - i;
            if (a.isWithinLimits(false) && !a.getInteractionController().intersectsWithSolids(false)) {
                break;
            } else {
                a.x1 = ox1;
                a.x2 = ox2;
                a.y1 = oy1;
                a.y2 = oy2;
            }
        }
    }
}
