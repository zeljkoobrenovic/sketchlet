/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.model.evaluator;

import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.designer.playback.ui.PlaybackPanel;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.model.ActiveRegions;
import net.sf.sketchlet.model.Page;
import net.sf.sketchlet.model.PropertiesInterface;
import org.apache.log4j.Logger;

/**
 * @author zobrenovic
 */
public class Evaluator {
    private static final Logger log = Logger.getLogger(Evaluator.class);

    public static String processText(String strText, String strVarPrefix, String strVarPostfix) {
        return processText(null, strText, strVarPrefix, strVarPostfix);

    }

    public static String processText(Object context, String strText, String strVarPrefix, String strVarPostfix) {
        if (strText == null) {
            return "";
        }
        ActiveRegions regions;
        Page page;
        if (SketchletEditor.getInstance() != null) {
            if ((PlaybackFrame.playbackFrame != null || SketchletEditor.getInstance().getInternalPlaybackPanel() != null) && PlaybackPanel.currentPage != null) {
                regions = PlaybackPanel.currentPage.getRegions();
                page = PlaybackPanel.currentPage;
            } else {
                regions = SketchletEditor.getInstance().getCurrentPage().getRegions();
                page = SketchletEditor.getInstance().getCurrentPage();
            }
        } else {
            page = PlaybackPanel.currentPage;
            regions = null;
        }

        for (int i = 0; i < 10; i++) {
            strText = DataServer.populateTemplateSimple(strText, false);
            strText = processRegionReferences(regions, strText);
            if (strText.startsWith("=")) {
                strText = DataServer.processForFormulas(strText);
            } else {
                break;
            }
        }

        if (strText.contains("\\r\\n")) {
            strText = strText.replace("\\r\\n", "\n");
        }

        if (strText.contains("\\n")) {
            strText = strText.replace("\\n", "\n");
        }

        if (strText.contains("\\r\\n")) {
            strText = strText.replace("\\r\\n", "\n");
        }

        if (strText.contains("\\t")) {
            strText = strText.replace("\\t", "\t");
        }

        return strText;
    }

    public static String processRegionReferences(ActiveRegion region, String strText) {
        SketchletContext context = SketchletContext.getInstance();
        return processRegionReferences(region, region.parent, strText, context != null ? context.isInPlaybackMode() : true);
    }

    public static String processRegionReferences(ActiveRegions regions, String strText) {
        SketchletContext context = SketchletContext.getInstance();
        return processRegionReferences(null, regions, strText, context != null ? context.isInPlaybackMode() : true);
    }

    public static String processRegionReferences(ActiveRegion thisRegion, ActiveRegions regions, String text, boolean bPlayback) {
        String[][] markers = {{"this[", "]", "this"}, {"region[", "]", "region"}, {"page[", "]", "page"}, {"page[", "]", "page"}, {"global[", "]", "global"}};

        Page page = SketchletEditor.getInstance().getCurrentPage();

        for (String marker[] : markers) {
            String startMarker = marker[0];
            String endMarker = marker[1];
            String context = marker[2];
            while (true) {
                int markerStartPosition = text.indexOf(startMarker);
                if (markerStartPosition >= 0) {
                    int markerEndPosition = text.indexOf(endMarker, markerStartPosition);
                    if (markerEndPosition > 0) {
                        String propertyName = text.substring(markerStartPosition + startMarker.length(), markerEndPosition).replace("'", "").replace("\"", "");
                        PropertiesInterface properties = null;
                        if (context.equalsIgnoreCase("page")) {
                            properties = page;
                        } else if (context.equalsIgnoreCase("this")) {
                            properties = (PropertiesInterface) thisRegion;
                        } else {
                            int separatorPosition = propertyName.indexOf(":");
                            if (separatorPosition < 0) {
                                separatorPosition = propertyName.indexOf(".");
                            }
                            if (separatorPosition >= 0) {
                                String regionId = propertyName.substring(0, separatorPosition);
                                propertyName = propertyName.substring(0, separatorPosition);
                                properties = page.getRegions().getRegionByName(regionId);
                            }
                        }

                        if (properties != null) {
                            text = text.substring(0, markerStartPosition) + properties.getProperty(propertyName) + text.substring(markerEndPosition + 1);
                            continue;
                        }

                        break;
                    }
                    break;
                }
                break;
            }
        }

        return text;
    }
}
