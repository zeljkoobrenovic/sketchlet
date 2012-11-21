/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.model.programming.screenscripts;

import java.io.File;

public class ScreenScriptInfo {

    private String name;
    private String status;
    private String description;
    private File file;

    private boolean whenAllConditions = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isWhenAllConditions() {
        return whenAllConditions;
    }

    public void setWhenAllConditions(boolean whenAllConditions) {
        this.whenAllConditions = whenAllConditions;
    }
}
