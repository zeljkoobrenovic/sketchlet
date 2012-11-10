/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.context;

import java.awt.image.BufferedImage;

/**
 *
 * @author zobrenovic
 */
public class CodeGeneratorContext {

    private static CodeGeneratorContext context;
    private static PageContext currentPageContext = null;

    public static CodeGeneratorContext getInstance() {
        if (context == null) {
            context = new CodeGeneratorContext();
        }
        return context;
    }

    public static void setInstance(CodeGeneratorContext context) {
        CodeGeneratorContext.context = context;
    }

    public PageContext getCurrentPageContext() {
        return currentPageContext;
    }

    public void setCurrentPageContext(PageContext pageContext) {
        currentPageContext = pageContext;
    }
}
