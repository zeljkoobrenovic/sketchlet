/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.loaders.pluginloader;

import net.sf.sketchlet.codegen.CodeGenUtils;

/**
 * @author zobrenovic
 */
public class CodeGenPluginHandler extends SketchletPluginHandler {

    public CodeGenPluginHandler(String name, String type, String strClass, String strDescription, String filePath, String group, int position) {
        super(name, type, strClass, strDescription, filePath, group, position);
    }

    public void prepare() {
        String param = this.getParam("controls");
        if (param != null && !param.isEmpty()) {
            String controls[] = param.split(",");

            for (int i = 0; i < controls.length; i++) {
                controls[i] = controls[i].trim();
            }

            CodeGenUtils.getExtraControls().put(getName(), controls);
        }
    }
}