package net.sf.sketchlet.framework.model.events.overlap;

import net.sf.sketchlet.framework.model.events.EventMacro;

/**
 *
 * @author zeljko
 */
public class RegionOverlapEventMacro extends EventMacro {
    public static final String PARAMETER_REGION_ID = "regionId";

    public RegionOverlapEventMacro(String eventName) {
        super(eventName);
        parameters().put(PARAMETER_REGION_ID, "");
    }

    public String getRegionId() {
        return getMacro().getParameters().get(RegionOverlapEventMacro.PARAMETER_REGION_ID);
    }
}