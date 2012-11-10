/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import java.util.Vector;
import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.context.SketchletContext;

/**
 *
 * @author zobrenovic
 */
public abstract class PagedAppCodeGen extends CodeExporter {

    protected SketchletContext sketchletContext;
    // public Vector<CodeFile> applicationFiles = new Vector<CodeFile>();
    // public Vector<PageCodeFile> pageCodeFiles = new Vector<PageCodeFile>();
    public Vector<PageCodeFile> pageFiles = new Vector<PageCodeFile>();

    public PagedAppCodeGen() {
        this.sketchletContext = SketchletContext.getInstance();
    }

    public void dispose() {
        this.sketchletContext = null;

        for (PageCodeFile pf : this.pageFiles) {
            pf.dispose();
        }

        this.pageFiles.removeAllElements();
    }

    public abstract Vector<CodeFile> getPageGeneratorInstances(PageContext page);

    @Override
    public void prepare() {
        for (PageContext page : this.sketchletContext.getPages()) {
            Vector<CodeFile> pg = this.getPageGeneratorInstances(page);
            this.addFile(pg);
            for (CodeFile p : pg) {
                this.pageFiles.add((PageCodeFile) p);
            }
        }
        super.prepare();
    }

    /*    public void generateCodeFiles() {
    this.generateApplicationCodeFiles();
    this.generatePageCodeFiles();
    }
    
    public void generateApplicationCodeFiles() {
    for (CodeFile pcf : this.applicationFiles) {
    pcf.generate();
    if (pcf.shouldExport()) {
    this.exportFiles.add(pcf);
    }
    }
    }
    
    public void generatePageCodeFiles() {
    for (PageCodeFile pcf : this.pageCodeFiles) {
    pcf.generate();
    if (pcf.shouldExport()) {
    this.exportFiles.add(pcf);
    }
    }
    }*/
}
