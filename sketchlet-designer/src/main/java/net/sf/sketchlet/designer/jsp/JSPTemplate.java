/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.jsp;

import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.context.SketchletContext;

import java.util.Vector;

/**
 * @author zobrenovic
 */
public class JSPTemplate {

    public static final int APPLICATION = 0;
    public static final int PAGE = 1;
    String fileName = "";
    public String genFileName = "";
    public String template = "";
    public int type = APPLICATION;
    Vector<JSPGeneratedFile> generatedFiles = new Vector<JSPGeneratedFile>();

    public JSPTemplate(int type, String name, String template) {
        this.type = type;
        this.fileName = name;
        this.template = template;
    }

    public JSPTemplate(String name, String template) {
        this.type = type;
        this.fileName = name;
        this.processText(template);
    }

    public void processText(String text) {
        int n1 = text.indexOf("\t");
        int n2 = text.indexOf("\t", n1 + 1);

        if (n1 >= 0 && n2 > n1) {
            this.template = text.substring(n2 + 1);
            this.genFileName = text.substring(0, n1);
            String strt = text.substring(n1 + 1, n2);
            this.type = strt.equalsIgnoreCase("1") ? PAGE : APPLICATION;
        } else {
            this.template = text;
        }
    }

    public void prepare() {
        this.generatedFiles.removeAllElements();
        if (this.type == APPLICATION) {
            this.generatedFiles.add(new JSPGeneratedFile(this.getGeneratedFileName(), template));
        } else {
            for (PageContext pageContext : SketchletContext.getInstance().getPages()) {
                this.generatedFiles.add(new JSPGeneratedFile(pageContext, this.getGeneratedFileName(), template));
            }
        }
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getTemplateString() {
        return this.template;
    }

    public String getGeneratedFileName() {
        return this.genFileName.isEmpty() ? this.fileName : this.genFileName;
    }

    public String getText() {
        return this.getGeneratedFileName() + "\t" + this.type + "\t" + this.template;
    }
}
