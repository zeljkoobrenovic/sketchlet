package net.sf.sketchlet.framework.model;

import net.sf.sketchlet.framework.controller.ActiveRegionsMouseHelper;
import net.sf.sketchlet.framework.controller.ActiveRegionsOverlapHelper;
import net.sf.sketchlet.framework.controller.ActiveRegionsVariablesHelper;

import java.io.PrintWriter;
import java.util.Vector;

public class ActiveRegions {

    private Vector<ActiveRegion> regions = new Vector<ActiveRegion>();
    private Page page;

    private ActiveRegionsMouseHelper mouseHelper;
    private ActiveRegionsOverlapHelper overlapHelper;
    private ActiveRegionsVariablesHelper variablesHelper;

    public ActiveRegions(Page page) {
        this.setPage(page);
        setMouseHelper(new ActiveRegionsMouseHelper(page));
        setOverlapHelper(new ActiveRegionsOverlapHelper(page));
        setVariablesHelper(new ActiveRegionsVariablesHelper(page));
    }

    public ActiveRegions(ActiveRegions ars, Page page) {
        this(page);
        long lastTime = System.currentTimeMillis();
        for (ActiveRegion reg : ars.getRegions()) {
            while (lastTime == System.currentTimeMillis()) {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                }
            }
            this.getRegions().add(new ActiveRegion(reg, this, true));
            lastTime = System.currentTimeMillis();
        }
    }

    public ActiveRegions cloneEmbedded(Page page) {
        ActiveRegions _actions = new ActiveRegions(page);
        for (ActiveRegion reg : getRegions()) {
            _actions.getRegions().add(new ActiveRegion(reg, _actions, false));
        }

        return _actions;
    }

    public ActiveRegion getRegionByNumber(int num) {
        if (num <= this.getRegions().size()) {
            return getRegions().elementAt(getRegions().size() - num);
        }
        return null;
    }

    public ActiveRegion getRegionByName(String name) {
        for (ActiveRegion region : getRegions()) {
            if (region.getName().equalsIgnoreCase(name)) {
                return region;
            }
        }
        for (ActiveRegion region : getRegions()) {
            if (region.getNumber().equalsIgnoreCase(name)) {
                return region;
            }
        }
        return null;
    }

    public ActiveRegion getRegionByImageFileName(String name) {
        for (ActiveRegion region : getRegions()) {
            if (region.getDrawnImageFileName().equalsIgnoreCase(name)) {
                return region;
            }
        }
        return null;
    }

    public ActiveRegion getRegionById(String id) {
        for (ActiveRegion region : getRegions()) {
            if (region.getId().equalsIgnoreCase(id)) {
                return region;
            }
        }
        return null;
    }

    public int getActionIndex(String imageFile) {
        int i = 0;
        for (ActiveRegion a : getRegions()) {
            if (a.getDrawnImageFileName(0).equals(imageFile)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    private int offsetX = 0;
    private int offsetY = 0;

    public void save(PrintWriter out) {
        if (getRegions().size() > 0) {
            out.println("<active-regions>");
            for (ActiveRegion a : getRegions()) {
                a.save(out);
            }
            out.println("</active-regions>");
        }
    }

    public Vector<ActiveRegion> getRegions() {
        return regions;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public ActiveRegionsMouseHelper getMouseHelper() {
        return mouseHelper;
    }

    public void setMouseHelper(ActiveRegionsMouseHelper mouseHelper) {
        this.mouseHelper = mouseHelper;
    }

    public ActiveRegionsOverlapHelper getOverlapHelper() {
        return overlapHelper;
    }

    public void setOverlapHelper(ActiveRegionsOverlapHelper overlapHelper) {
        this.overlapHelper = overlapHelper;
    }

    public ActiveRegionsVariablesHelper getVariablesHelper() {
        return variablesHelper;
    }

    public void setVariablesHelper(ActiveRegionsVariablesHelper variablesHelper) {
        this.variablesHelper = variablesHelper;
    }
}
