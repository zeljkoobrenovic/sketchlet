/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.jsp;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.context.CodeGeneratorContext;
import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.util.HTTPUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class JSPGeneratedFile {

    public String fileName = "";
    public String template = "";
    PageContext pageContext = null;
    public static Vector<File> deleteFiles = new Vector<File>();

    public JSPGeneratedFile() {
    }

    public JSPGeneratedFile(String name, String template) {
        this.setFileName(name);
        this.setTemplate(template);
    }

    public JSPGeneratedFile(PageContext pageContext, String name, String template) {
        this.pageContext = pageContext;
        this.setFileName(name);
        this.setTemplate(template);
    }

    public void setFileName(String name) {
        this.fileName = name;
    }

    public String getFileName() {
        if (pageContext == null) {
            return this.fileName.replace("<%=page-name%>", "").replace("<%=page-name-underscore%>", "");
        } else {
            return this.fileName.replace("<%=page-name%>", pageContext.getTitle()).replace("<%=page-name-underscore%>", pageContext.getTitle().replace(" ", "_"));
        }
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getExtension() {
        int n = this.fileName.indexOf(".");
        if (n > 0) {
            return this.fileName.substring(n + 1);
        } else {
            return "";
        }
    }

    public String getText() {
        if (pageContext != null) {
            CodeGeneratorContext.getInstance().setCurrentPageContext(pageContext);
        }
        new File(SketchletContextUtils.getCurrentProjectDir() + "temp").mkdirs();
        File tempFile = new File(SketchletContextUtils.getCurrentProjectDir() + "temp/tempjsp" + System.currentTimeMillis() + (int) (Math.random() * 100000) + ".jsp");

        try {
            PrintWriter out = new PrintWriter(new FileWriter(tempFile));
            out.print(this.template);
            out.flush();
            out.close();

            String strURL = "http://localhost:" + SketchletContextUtils.httpProjectPort + "/temp/" + tempFile.getName();
            return HTTPUtils.getTextFromURL(strURL);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (!tempFile.delete()) {
                this.deleteFiles.add(tempFile);
            }
        }
        return this.template;
    }
}
