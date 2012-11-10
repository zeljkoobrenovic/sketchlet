/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.script;

import java.io.File;

/**
 *
 * @author zobrenovic
 */
public interface IScript {

    public void eval(String code);
    public void eval(File file);
}
