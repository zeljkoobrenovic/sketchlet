/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.jsp;

import net.sf.sketchlet.codegen.ImageFile;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;

import java.io.File;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class JSPCodeGen {

    Vector<JSPTemplate> templateFiles = new Vector<JSPTemplate>();
    public Vector<ImageFile> images = new Vector<ImageFile>();

    public JSPCodeGen() {
        this.templateFiles = this.loadTemplates();
    }

    public void addTemplate(int type, String fileName, String template) {
        JSPTemplate jspt = new JSPTemplate(type, fileName, template);
        this.templateFiles.add(jspt);
    }

    public void addFile(String fileName) {
        JSPTemplate jspt = new JSPTemplate(JSPTemplate.APPLICATION, fileName, "");
        this.templateFiles.add(jspt);
    }

    public void saveTemplates() {
        File templateDir = new File(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/templates");
        templateDir.mkdirs();
        for (JSPTemplate template : this.templateFiles) {
            FileUtils.saveFileText(new File(templateDir, template.getFileName()), template.getText());
        }
    }

    public Vector<JSPTemplate> getTemplates() {
        return this.templateFiles;
    }

    public Vector<JSPTemplate> loadTemplates() {
        File templateDir = new File(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/templates");
        templateDir.mkdirs();

        Vector<JSPTemplate> templateFiles = new Vector<JSPTemplate>();

        File files[] = templateDir.listFiles();

        for (int i = 0; i < files.length; i++) {
            for (int j = 0; j < files.length; j++) {
                if (i != j) {
                    if (files[i].lastModified() < files[j].lastModified()) {
                        File tempFile = files[i];
                        files[i] = files[j];
                        files[j] = tempFile;
                    }
                }
            }
        }

        for (File file : files) {
            JSPTemplate jspt = new JSPTemplate(file.getName(), FileUtils.getFileText(file));
            templateFiles.add(jspt);
        }

        return templateFiles;
    }

    public Vector<JSPGeneratedFile> getGeneratedFiles() {
        Vector<JSPGeneratedFile> generatedFiles = new Vector<JSPGeneratedFile>();
        Vector<JSPTemplate> templates = this.getTemplates();
        for (JSPTemplate jspt : templates) {
            jspt.prepare();
            for (JSPGeneratedFile genFile : jspt.generatedFiles) {
                generatedFiles.add(genFile);
            }
        }

        new Thread(new Runnable() {

            public void run() {
                try {
                    Thread.sleep(10000);
                    for (File file : JSPGeneratedFile.deleteFiles) {
                        file.delete();
                    }
                    Thread.sleep(20000);
                    for (File file : JSPGeneratedFile.deleteFiles) {
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                    Thread.sleep(30000);
                    for (File file : JSPGeneratedFile.deleteFiles) {
                        if (file.exists()) {
                            if (!file.delete()) {
                                file.deleteOnExit();
                            }
                        }
                    }
                    JSPGeneratedFile.deleteFiles.removeAllElements();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return generatedFiles;
    }

    public void exportFiles(File dir) {
        for (JSPGeneratedFile genFile : this.getGeneratedFiles()) {
            FileUtils.saveFileText(new File(dir, genFile.getFileName()).getPath(), genFile.getText());
        }
    }

    public void dispose() {
        this.templateFiles.removeAllElements();
        this.images.removeAllElements();
    }
}
