/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugin;

import java.io.File;
import java.util.List;

/**
 *
 * @author zobrenovic
 */
public interface CodeGenPlugin {

    public void prepare();

    public void dispose();

    public List<CodeGenPluginFile> getFiles();

    public List<CodeGenPluginSetting> getSettings();
}
