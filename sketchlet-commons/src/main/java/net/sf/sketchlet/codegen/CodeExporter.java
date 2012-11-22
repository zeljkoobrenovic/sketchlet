/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import net.sf.sketchlet.plugin.CodeGenPlugin;
import net.sf.sketchlet.plugin.CodeGenPluginFile;
import net.sf.sketchlet.plugin.CodeGenPluginSetting;

import java.io.File;
import java.util.Vector;

/**
 *
 * @author zobrenovic
 */
public class CodeExporter implements CodeGenPlugin {

    private Vector<CodeFile> generatedFiles = new Vector<CodeFile>();
    private Vector<CodeGenPluginSetting> settings = new Vector<CodeGenPluginSetting>();

    public CodeExporter() {
    }

    public void addFile(CodeFile file) {
        this.getGeneratedFiles().add(file);
    }

    public void addFile(Vector<CodeFile> files) {
        for (CodeFile file : files) {
            this.getGeneratedFiles().add(file);
        }
    }

    @Override
    public void prepare() {
        for (CodeFile cf : this.getGeneratedFiles()) {
            cf.prepare();
        }
    }

    @Override
    public Vector<CodeGenPluginFile> getFiles() {
        Vector<CodeGenPluginFile> files = new Vector<CodeGenPluginFile>();
        for (CodeFile cf : this.getGeneratedFiles()) {
            files.add(cf);
        }
        return files;
    }

    public Vector<CodeFile> getCodeFiles() {
        return this.getGeneratedFiles();
    }

    public void exportFiles(File dir) {
        dir.mkdirs();
        for (final CodeFile pg : this.getGeneratedFiles()) {
            if (pg.shouldExport()) {
                if (!pg.getSubDirectory().isEmpty()) {
                    new File(dir, pg.getSubDirectory()).mkdirs();
                }
                pg.exportFile(dir);
            }
        }
    }

    public void addSetting(CodeGenPluginSetting setting) {
        this.getSettings().add(setting);
    }

    public Vector<CodeGenPluginSetting> getSettings() {
        return this.settings;
    }

    public void dispose() {
    }

    public Vector<CodeFile> getGeneratedFiles() {
        return generatedFiles;
    }

    public void setGeneratedFiles(Vector<CodeFile> generatedFiles) {
        this.generatedFiles = generatedFiles;
    }

    public void setSettings(Vector<CodeGenPluginSetting> settings) {
        this.settings = settings;
    }
}
