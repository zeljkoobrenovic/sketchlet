/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.eye.eye;

import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.model.ActiveRegion;

import java.awt.*;

/**
 * @author zobrenovic
 */
public class RegionEyeSlot extends EyeSlot {

    ActiveRegion region;

    public RegionEyeSlot(ActiveRegion region, EyeData parent) {
        super(parent);
        this.region = region;
        this.name = "Region " + region.getNumber();
        this.backgroundColor = Color.BLACK;
    }

    public String getLongName() {
        return "Region " + this.name;
    }

    public void addRelatedSlot(EyeSlot relatedSlot) {
        if (relatedSlot instanceof VariableEyeSlot) {
            String value = getValue(region.updateTransformations, relatedSlot.name, 1, 0);
            String vname = relatedSlot.name;
            checkAndAdd(relatedSlot, region.updateTransformations, relatedSlot.name, 1,
                    "connected to dimension '" + value + "' of region " + region.getNumber(),
                    "dimension '" + value + "' connected to variable '" + vname + "'");

            /*value = getValue(region.variablesMappingHandler.data, relatedSlot.name, 3, 0);
            checkAndAdd(relatedSlot, region.variablesMappingHandler.data, relatedSlot.name, 3,
                    "connected to property '" + value + "' of region " + region.getNumber(),
                    "property '" + value + "' connected to variable '" + vname + "'");*/

            /*value = getValue(region.mouseEvents, relatedSlot.name, 2, 0);
            String value2 = getValue(region.mouseEvents, relatedSlot.name, 2, 3);
            checkAndAdd(relatedSlot, region.mouseEvents, "Variable update", relatedSlot.name, 1, 2,
                    "updated on '" + value + "' in region " + region.getNumber() + " to " + value2,
                    "on '" + value + "' updates variable '" + vname + "'" + " to " + value2);
            checkAndAdd(relatedSlot, region.mouseEvents, "Variable append", relatedSlot.name, 1, 2,
                    "appended on '" + value + "' in region " + region.getNumber() + " to " + value2,
                    "on '" + value + "' append variable '" + vname + "'" + " to " + value2);
            checkAndAdd(relatedSlot, region.mouseEvents, "Variable increment", relatedSlot.name, 1, 2,
                    "incremented on '" + value + "' in region " + region.getNumber() + " to " + value2,
                    "on '" + value + "' increment variable '" + vname + "'" + " to " + value2);*/
            for (int i = 0; i < ActiveRegion.propertiesInfo.length; i++) {
                if (region.getProperty(ActiveRegion.propertiesInfo[i][0]).equalsIgnoreCase("=" + relatedSlot.name)) {
                    value = ActiveRegion.propertiesInfo[i][0];
                    addRelationToSlot(relatedSlot,
                            "used as property '" + value + "' of region " + region.getNumber(),
                            "property '" + value + "' taken from variable '" + vname + "'");
                }
            }
        } else if (relatedSlot instanceof TimerEyeSlot) {
            /*String value = getValue(region.mouseEvents, relatedSlot.name, 2, 0);
            this.checkAndAdd(relatedSlot, region.mouseEvents, "Start timer", relatedSlot.name, 1, 2,
                    "started on '" + value + "' in region " + region.getNumber(),
                    "on '" + value + "' starts timer '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.mouseEvents, "Stop timer", relatedSlot.name, 1, 2,
                    "stopped on '" + value + "' in region " + region.getNumber(),
                    "on '" + value + "' stops timer '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.mouseEvents, "Pause timer", relatedSlot.name, 1, 2,
                    "paused on '" + value + "' in region " + region.getNumber(),
                    "on '" + value + "' pauses timer '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.interactionEvents, "Start timer", relatedSlot.name, 2, 3,
                    "started on interation event of region " + region.getNumber(),
                    "on interaction event within region " + region.getNumber() + "' starts timer '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.interactionEvents, "Stop timer", relatedSlot.name, 2, 3,
                    "started on interaction event of region " + region.getNumber(),
                    "on interaction event of region " + region.getNumber() + "' stops timer '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.interactionEvents, "Pause timer", relatedSlot.name, 2, 3,
                    "paused on interaction event of region " + region.getNumber(),
                    "on interaction event of region " + region.getNumber() + "' pauses timer '" + relatedSlot.name + "'");*/
        } else if (relatedSlot instanceof MacroEyeSlot) {
            /*String value = getValue(region.mouseEvents, relatedSlot.name, 2, 0);
            this.checkAndAdd(relatedSlot, region.mouseEvents, "Start action", relatedSlot.name, 1, 2,
                    "started on '" + value + "' in region " + region.getNumber(),
                    "on '" + value + "' starts action '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.mouseEvents, "Stop action", relatedSlot.name, 1, 2,
                    "stopped on '" + value + "' in region " + region.getNumber(),
                    "on '" + value + "' stops action '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.interactionEvents, "Start action", relatedSlot.name, 2, 3,
                    "started on interation event of region " + region.getNumber(),
                    "on interaction event within region " + region.getNumber() + "' starts action '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.interactionEvents, "Stop action", relatedSlot.name, 2, 3,
                    "started on interaction event of region " + region.getNumber(),
                    "on interaction event of region " + region.getNumber() + "' stops action '" + relatedSlot.name + "'");       */
        } else if (relatedSlot instanceof ScriptEyeSlot) {
            /*String value = getValue(region.mouseEvents, "Script:" + relatedSlot.name, 2, 0);
            this.checkAndAdd(relatedSlot, region.mouseEvents, "Start action", "Script:" + relatedSlot.name, 1, 2,
                    "started on '" + value + "' in region " + region.getNumber(),
                    "on '" + value + "' starts script '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.mouseEvents, "Stop action", "Script:" + relatedSlot.name, 1, 2,
                    "stopped on '" + value + "' in region " + region.getNumber(),
                    "on '" + value + "' stops script '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.interactionEvents, "Start action", "Script:" + relatedSlot.name, 2, 3,
                    "started on interation event of region " + region.getNumber(),
                    "on interaction event within region " + region.getNumber() + "' starts script '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.interactionEvents, "Stop action", "Script:" + relatedSlot.name, 2, 3,
                    "started on interaction event of region " + region.getNumber(),
                    "on interaction event of region " + region.getNumber() + "' stops script '" + relatedSlot.name + "'");  */
        } else if (relatedSlot instanceof ScreenActionEyeSlot) {
            /*String value = getValue(region.mouseEvents, "Screen:" + relatedSlot.name, 2, 0);
            this.checkAndAdd(relatedSlot, region.mouseEvents, "Start action", "Screen:" + relatedSlot.name, 1, 2,
                    "started on '" + value + "' in region " + region.getNumber(),
                    "on '" + value + "' starts screen action '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.mouseEvents, "Stop action", "Screen:" + relatedSlot.name, 1, 2,
                    "stopped on '" + value + "' in region " + region.getNumber(),
                    "on '" + value + "' stops screen action '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.interactionEvents, "Start action", "Screen:" + relatedSlot.name, 2, 3,
                    "started on interation event of region " + region.getNumber(),
                    "on interaction event within region " + region.getNumber() + "' starts screen action '" + relatedSlot.name + "'");
            this.checkAndAdd(relatedSlot, region.interactionEvents, "Stop action", "Screen:" + relatedSlot.name, 2, 3,
                    "started on interaction event of region " + region.getNumber(),
                    "on interaction event of region " + region.getNumber() + "' stops screen action '" + relatedSlot.name + "'");  */
        } else if (relatedSlot instanceof SketchEyeSlot) {
            /*String value = getValue(region.mouseEvents, "Screen:" + relatedSlot.name, 2, 0);
this.checkAndAdd(relatedSlot, region.mouseEvents, "Go to page", relatedSlot.name, 1, 2,
"started on '" + value + "' in region " + region.getNumber(),
"on '" + value + "' starts screen action '" + relatedSlot.name + "'");  */
        } else if (relatedSlot instanceof RegionEyeSlot) {
        }
    }

    public void openItem() {
        SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.ACTIONS);
        ActiveRegionsFrame.showRegionsAndActions();
        ActiveRegionsFrame.refresh(region);
    }
}
