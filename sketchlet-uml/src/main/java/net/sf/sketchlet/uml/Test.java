/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.uml;

import java.io.File;
import net.sf.sketchlet.common.file.FileUtils;

/**
 *
 * @author zeljko
 */
public class Test {

    public static void main(String args[]) {
        //FileUtils.saveFileText("c:/temp/out/extranet-security.txt", XsltUtils.transform(new File("files/xml/deployerConfigContext.xml"), new File("files/bbproject/xslt/security.xsl"), "*.xml"));
        FileUtils.saveFileText("c:/temp/out/extranet-app-context.txt", XsltUtils.transform(new File("files/xml/extranet"), new File("files/bbproject/xslt/security.xsl"), "*.xml"));
    }
}
