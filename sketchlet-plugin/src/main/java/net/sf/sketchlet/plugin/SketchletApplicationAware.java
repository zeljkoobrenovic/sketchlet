/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.plugin;

/**
 * This interface enable plugins to receive notifications about the application
 * lifecycle events.
 * 
 * @author Zeljko Obrenovic
 */
public interface SketchletApplicationAware {

    /**
     * This method is called immediatelly after the start of Sketchlet Designer.
     */
    public void afterApplicationStart();

    /**
     * This method is called just beforte the end of Sketchlet Designer.
     */
    public void beforeApplicationEnd();
}
