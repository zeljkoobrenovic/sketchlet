package net.sf.sketchlet.designer.editor.ui.region;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.ui.ActionDialogUtils;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.events.EventMacroFactory;
import net.sf.sketchlet.framework.model.events.overlap.RegionOverlapEventMacro;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zeljko
 */
public class RegionOverlapEventsPanel extends AbstractEventsPanel {
    private ActiveRegion region;

    public RegionOverlapEventsPanel(final ActiveRegion region) {
        super();
        this.region = region;
        setEventMacroFactory(new EventMacroFactory<RegionOverlapEventMacro>() {
            @Override
            public RegionOverlapEventMacro getNewEventMacroInstance(String... args) {
                return addNewActionToRegionOverlapMacro();
            }

            @Override
            public String getEventTypeName(boolean plural) {
                return plural ? "region overlap events" : "region overlap event";
            }

            @Override
            public List<RegionOverlapEventMacro> getEventMacroList() {
                return region.regionOverlapEventMacros;
            }

            @Override
            public String getEventDescription(RegionOverlapEventMacro eventMacro) {
                String regionName = eventMacro.getRegionId();
                if (!regionName.equalsIgnoreCase("Any Region")) {
                    ActiveRegion overlapRegion = region.parent.getRegionById(regionName);
                    if (overlapRegion != null) {
                        regionName = "region " + overlapRegion.getName();
                    }
                }

                return eventMacro.getEventName() + " " + regionName.toLowerCase();
            }
        });
    }

    private RegionOverlapEventMacro regionOverlapEventMacro = null;

    public RegionOverlapEventMacro addNewActionToRegionOverlapMacro() {
        final List<String> regionIds = new ArrayList<String>();
        final String labels[] = new String[]{"touches", "inside", "outside", "completely outside"};
        ImageIcon images[] = new ImageIcon[]{Workspace.createImageIcon("resources/region_overlap_touch.png"),
                Workspace.createImageIcon("resources/region_overlap_inside.png"),
                Workspace.createImageIcon("resources/region_overlap_outside.png"),
                Workspace.createImageIcon("resources/region_overlap_completely_outside.png")
        };
        Integer[] intArray = new Integer[labels.length];
        for (int i = 0; i < labels.length; i++) {
            intArray[i] = new Integer(i);
        }

        final JComboBox regions = new JComboBox();
        regions.addItem("Any Region");
        regionIds.add("Any Region");
        for (int i = region.parent.getRegions().size() - 1; i >= 0; i--) {
            ActiveRegion r = region.parent.getRegions().get(i);
            if (r != region) {
                regions.addItem(r.getName());
                regionIds.add(r.getId());
            }
        }
        final JComboBox events = new JComboBox(intArray);
        events.setRenderer(new ComboBoxRenderer(labels, images));

        Runnable onOk = new Runnable() {
            @Override
            public void run() {
                regionOverlapEventMacro = new RegionOverlapEventMacro(labels[events.getSelectedIndex()]);
                regionOverlapEventMacro.parameters().put(RegionOverlapEventMacro.PARAMETER_REGION_ID, regionIds.get(regions.getSelectedIndex()));
            }
        };

        Component components[] = new Component[]{new JLabel(Language.translate("Region: ")), regions, new JLabel(Language.translate("Overlap Event: ")), events};
        ActionDialogUtils.openAddActionDialog(Language.translate("Region Overlap Action"), components, onOk);
        return regionOverlapEventMacro;
    }

    public void addNewRegionOverlapMacro(final String action, final String param1, final String param2) {
        final List<String> regionIds = new ArrayList<String>();
        final String labels[] = new String[]{"touches", "inside", "outside", "completely outside"};
        ImageIcon images[] = new ImageIcon[]{Workspace.createImageIcon("resources/region_overlap_touch.png"),
                Workspace.createImageIcon("resources/region_overlap_inside.png"),
                Workspace.createImageIcon("resources/region_overlap_outside.png"),
                Workspace.createImageIcon("resources/region_overlap_completely_outside.png")
        };
        Integer[] intArray = new Integer[labels.length];
        for (int i = 0; i < labels.length; i++) {
            intArray[i] = new Integer(i);
        }

        final JComboBox regions = new JComboBox();
        regions.addItem("Any Region");
        regionIds.add("Any Region");
        for (int i = region.parent.getRegions().size() - 1; i >= 0; i--) {
            ActiveRegion r = region.parent.getRegions().get(i);
            if (r != region) {
                regions.addItem(r.getName());
                regionIds.add(r.getId());
            }
        }
        final JComboBox events = new JComboBox(intArray);
        events.setRenderer(new ComboBoxRenderer(labels, images));
        AddActionRunnable onOk = new AddActionRunnable() {
            @Override
            public void addAction(String action, String param1, String param2) {
                regionOverlapEventMacro = new RegionOverlapEventMacro(labels[events.getSelectedIndex()]);
                regionOverlapEventMacro.parameters().put(RegionOverlapEventMacro.PARAMETER_REGION_ID, regionIds.get(regions.getSelectedIndex()));

                region.regionOverlapEventMacros.add(regionOverlapEventMacro);

                regionOverlapEventMacro.getMacro().addLine(action, param1, param2);
                refreshMacroPanel();
            }
        };
        Component components[] = new Component[]{new JLabel(Language.translate("Region: ")), regions, new JLabel(Language.translate("Overlap Event: ")), events};
        ActionDialogUtils.openAddActionDialog(Language.translate("Region Overlap Action"), components, onOk, action, param1, param2);
    }
}

