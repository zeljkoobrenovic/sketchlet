/*
 * Script.java
 *
 * Created on April 21, 2008, 2:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugin;

import java.util.List;
import java.util.Map;

/**
 *
 * @author cuypers
 */
public interface ScriptPluginAutoCompletion {

    public Map<String, List<String>> getAutoCompletionPairs();
}