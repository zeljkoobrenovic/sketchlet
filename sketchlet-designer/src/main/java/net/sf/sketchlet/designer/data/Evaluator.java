/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.data;

import net.sf.sketchlet.common.template.TemplateMarkers;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.designer.ui.playback.PlaybackPanel;
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
        if (SketchletEditor.editorPanel != null) {
            if ((PlaybackFrame.playbackFrame != null || SketchletEditor.editorPanel.internalPlaybackPanel != null) && PlaybackPanel.currentPage != null) {
                regions = PlaybackPanel.currentPage.regions;
                page = PlaybackPanel.currentPage;
            } else {
                regions = SketchletEditor.editorPanel.currentPage.regions;
                page = SketchletEditor.editorPanel.currentPage;
            }
        } else {
            page = PlaybackPanel.currentPage;
            regions = null;
        }

        // maximal depth of referencing
        /*Vector<AdditionalVariables> additionalVariables = new Vector<AdditionalVariables>();
        additionalVariables.addAll(DataServer.variablesServer.additionalVariables);

        for (final LocalVariable localVariable: sketch.localVariables) {
            DataServer.variablesServer.additionalVariables.add(new AdditionalVariables(){
                public Variable getVariable(String variableName) {
                    Variable v = new Variable();
                    v.name = localVariable.getName();
                    v.value = localVariable.getValue();

                    return v;
                }
                public void updateVariable(String variableName, String value) {
                    if (variableName.equalsIgnoreCase(localVariable.getName())) {
                        localVariable.setValue(value);
                    }
                }
            });
        }      */

        for (int i = 0; i < 10; i++) {
            strText = DataServer.populateTemplateSimple(strText, false);
            strText = processRegionReferences(regions, strText);
            if (strText.startsWith("=")) {
                strText = DataServer.processForFormulas(strText);
            } else {
                break;
            }
        }

        // DataServer.variablesServer.additionalVariables = additionalVariables;

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

        Page page = SketchletEditor.editorPanel.currentPage;

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
                            // region['a:position x']
                            // region['a.position x']
                            int separatorPosition = propertyName.indexOf(":");
                            if (separatorPosition < 0) {
                                separatorPosition = propertyName.indexOf(".");
                            }
                            if (separatorPosition >= 0) {
                                String regionId = propertyName.substring(0, separatorPosition);
                                propertyName = propertyName.substring(0, separatorPosition);
                                properties = page.regions.getRegionByName(regionId);
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

    public static String processRegionReferences2(ActiveRegion thisRegion, ActiveRegions regions, String strText, boolean bPlayback) {
        if (strText == null) {
            return "";
        }
        if (!(strText.startsWith("=") || TemplateMarkers.containsStartMarker(strText))) {
            return strText;
        }
        while (true) {
            int n1, nDot, n2;
            n1 = strText.lastIndexOf("[");
            nDot = strText.indexOf(".", n1 + 1);
            n2 = strText.indexOf("]", n1 + 1);
            int o1 = 1;
            int o2 = 1;

            if (n1 == -1 || n2 == -1 || n2 <= n1) {
                break;
            }

            try {
                String strValue = "";
                String strID;
                String strProperty;
                if (nDot >= 0) {
                    if (nDot < n1 || nDot >= n2) {
                        return strText;
                    }
                    strID = strText.substring(n1 + o1, nDot).trim();
                    strProperty = strText.substring(nDot + 1, n2).trim();
                } else {
                    strID = "this";
                    strProperty = strText.substring(n1 + 1, n2).trim();
                }

                ActiveRegion region;
                if ((strID.equalsIgnoreCase("this") || strID.equalsIgnoreCase("region") || strID.equalsIgnoreCase("selectedRegion")) && thisRegion != null) {
                    region = thisRegion;
                    strValue = region == null ? "" : region.getPropertyValue(strProperty, bPlayback);
                } else if (strID.equalsIgnoreCase("page")) {
                    strValue = regions.page.getPropertyValue(strProperty);
                } else {
                    region = regions.getRegionByName(strID);
                    strValue = region == null ? "" : region.getPropertyValue(strProperty, bPlayback);
                }

                String strResult = strText.substring(0, n1);
                strResult += strValue;
                if (n2 < strText.length()) {
                    strResult += strText.substring(n2 + o2);
                }
                strText = strResult;
            } catch (Exception e) {
                log.error("Error processing '" + strText + "'", e);
                break;
            }
        }

        return strText;
    }
}
