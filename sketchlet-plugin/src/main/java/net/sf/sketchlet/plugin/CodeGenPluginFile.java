/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugin;

import java.io.File;

/**
 *
 * @author zobrenovic
 */
public interface CodeGenPluginFile {

    /**
     * @return a name of the file.
     */
    public String getFileName();

    /**
     * @return a text of the code that will be saved. Has to be textually encode (no binary files).
     */
    public String getPreviewText();

    public void exportFile(File directory);

    public String getFileMimeType();
}
