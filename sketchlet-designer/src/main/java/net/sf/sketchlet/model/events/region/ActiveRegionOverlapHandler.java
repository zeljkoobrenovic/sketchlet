/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.model.events.region;

import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.events.RegionOverlapEventMacro;
import net.sf.sketchlet.model.programming.macros.Macro;
import net.sf.sketchlet.model.programming.timers.TimerThread;
import net.sf.sketchlet.script.RunInterface;

import java.awt.geom.Area;
import java.util.List;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ActiveRegionOverlapHandler {

    private Vector<ActiveRegion> overlappingRegions = new Vector<ActiveRegion>();
    private Vector<ActiveRegion> containedRegions1 = new Vector<ActiveRegion>();
    private Vector<ActiveRegion> containedRegions2 = new Vector<ActiveRegion>();
    private Vector<ActiveRegion> containedRegions3 = new Vector<ActiveRegion>();
    private ActiveRegion region;

    public ActiveRegionOverlapHandler(ActiveRegion region) {
        this.region = region;
    }

    public void dispose() {
        region = null;
    }

    public void reset() {
        overlappingRegions.removeAllElements();
        containedRegions1.removeAllElements();
        containedRegions2.removeAllElements();
        containedRegions3.removeAllElements();
    }

    public void processInteractionEvents(boolean bPlayback, List<TimerThread> activeTimers, List<RunInterface> activeMacros) {
        if (!bPlayback) {
            return;
        }

        for (RegionOverlapEventMacro regionOverlapEventMacro : region.regionOverlapEventMacros) {
            String regionId = regionOverlapEventMacro.getRegionId();
            String event = regionOverlapEventMacro.getEventName();
            try {
                Area r1 = region.getTransformedArea(bPlayback);

                if (regionId.equals("")) {
                    continue;
                } else if (regionId.equalsIgnoreCase("Any Region")) {
                    for (ActiveRegion a : region.parent.getRegions()) {
                        if (a != region) {
                            processIntersection(bPlayback, region, a, r1, event, regionOverlapEventMacro.getMacro(), activeTimers, activeMacros);
                        }
                    }
                } else {
                    try {
                        int index = region.parent.getActionIndex(regionId);
                        if (index >= 0 && index < region.parent.getRegions().size()) {
                            ActiveRegion a = region.parent.getRegions().elementAt(index);
                            processIntersection(bPlayback, region, a, r1, event, regionOverlapEventMacro.getMacro(), activeTimers, activeMacros);
                        }
                    } catch (Exception e) {
                    }
                }
            } catch (Exception e) {
            }

        }

    }

    public boolean intersects(Area a1, Area a2) {
        if (a1.getBounds().getWidth() <= 0 || a2.getBounds().getWidth() <= 0 || a1.getBounds().getHeight() <= 0 || a2.getBounds().getHeight() <= 0) {
            return false;
        }
        Area _a1 = new Area(a1);
        Area _a2 = new Area(a2);
        _a1.intersect(a2);
        _a2.intersect(a1);
        return !_a1.isEmpty() || !_a2.isEmpty();
    }

    public boolean contains(Area a1, Area a2) {
        if (a1.getBounds().getWidth() <= 0 || a2.getBounds().getWidth() <= 0 || a1.getBounds().getHeight() <= 0 || a2.getBounds().getHeight() <= 0) {
            return false;
        }
        a1 = new Area(a1);
        a2 = new Area(a2);
        Area _a1 = new Area(a1);
        Area _a2 = new Area(a2);
        a1.intersect(a2);
        _a2.intersect(_a1);
        return a1.equals(a2) || _a2.equals(_a1);
    }

    public void processIntersection(boolean bPlayback, ActiveRegion sourceRegion, ActiveRegion region, Area r1, String event, Macro macro, List<TimerThread> activeTimers, List<RunInterface> activeMacros) {
        Area r2 = region.getTransformedArea(bPlayback);

        if (event.equalsIgnoreCase("touches")) {
            if (intersects(r1, r2)) {
                if (!overlappingRegions.contains(region)) {
                    overlappingRegions.add(region);
                    activeMacros.add(macro.startThread(sourceRegion, "", "", "", null));
                }
            } else {
                overlappingRegions.remove(region);
            }
        } else if (event.equalsIgnoreCase("inside")) {
            if (contains(r1, r2)) {
                if (!containedRegions1.contains(region)) {
                    containedRegions1.add(region);
                    activeMacros.add(macro.startThread(sourceRegion, "", "", "", null));
                }
            } else {
                containedRegions1.remove(region);
            }
        } else if (event.equalsIgnoreCase("outside")) {
            if (!containedRegions2.contains(region) && (contains(r1, r2))) {
                containedRegions2.add(region);
            } else if (containedRegions2.contains(region) && !(contains(r1, r2))) {
                activeMacros.add(macro.startThread(sourceRegion, "", "", "", null));
                containedRegions2.remove(region);
            }
        } else if (event.equalsIgnoreCase("completely outside")) {
            if (!containedRegions3.contains(region) && intersects(r1, r2)) {
                containedRegions3.add(region);
            } else if (containedRegions3.contains(region) && !intersects(r1, r2)) {
                activeMacros.add(macro.startThread(sourceRegion, "", "", "", null));
                containedRegions3.remove(region);
            }
        }
    }

    public boolean intersectsWithSolids(boolean bPlayback) {
        if (region.walkThroughEnabled) {
            for (ActiveRegion a : region.parent.getRegions()) {
                if (a != region) {
                    if (a.walkThroughEnabled) {
                        Area r1 = region.getTransformedArea(bPlayback);
                        Area r2 = a.getTransformedArea(bPlayback);

                        if (intersects(r1, r2)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}


