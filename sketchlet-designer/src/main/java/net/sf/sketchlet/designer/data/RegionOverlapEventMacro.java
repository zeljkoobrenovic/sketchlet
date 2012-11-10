package net.sf.sketchlet.designer.data;

/**
 * Created with IntelliJ IDEA.
 * User: zeljko
 * Date: 10-10-12
 * Time: 21:01
 * To change this template use File | Settings | File Templates.
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