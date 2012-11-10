/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.uml;

import net.sf.net.logger.SketchletPluginLogger;
import net.sf.sketchlet.common.file.FileUtils;

import java.io.File;
import java.io.StringWriter;
import java.util.List;

/**
 * @author zeljko
 */
public class XsltUtils {

    public static String transform(File inputFile, File xsltFile) {
        return transform(inputFile, new File[]{xsltFile}, "");
    }

    public static String transform(File inputFile, File xsltFile, String filter) {
        return transform(inputFile, new File[]{xsltFile}, filter);
    }

    public static String transform(File inputFile, File xsltFiles[], String filter) {
        if (!inputFile.exists()) {
            SketchletPluginLogger.error("XML file '" + inputFile.getPath() + "' does not exist.");
            return "";
        }
        if (inputFile.isDirectory()) {
            StringWriter str = new StringWriter();
            for (File child : inputFile.listFiles()) {
                if (child.isDirectory()) {
                    str.append(transform(child, xsltFiles, filter));
                    continue;
                }
                String fileName = child.getName();
                if (!filter.isEmpty()) {
                    if (filter.startsWith("*") && filter.endsWith("*")) {
                        if (!fileName.contains(filter.substring(1, filter.length() - 2))) {
                            continue;
                        }
                    } else if (filter.endsWith("*")) {
                        if (!fileName.startsWith(filter.substring(0, filter.length() - 1))) {
                            continue;
                        }
                    } else if (filter.startsWith("*")) {
                        if (!fileName.endsWith(filter.substring(1))) {
                            continue;
                        }
                    }
                }
                str.append(transform(child, xsltFiles, filter));
            }
            return str.toString();
        } else {
            StringWriter str = new StringWriter();
            if (inputFile.exists()) {
                for (File xsltFile : xsltFiles) {
                    String strFile = SimplifyingSaxXmlParsers.parse(inputFile);
                    str.append(transform(strFile, xsltFile));
                }
            }
            return str.toString();
        }
    }

    public static String transform(String source, File xsltFile) {
        if (xsltFile.isDirectory()) {
            StringWriter str = new StringWriter();
            for (File child : xsltFile.listFiles()) {
                str.append(transform(source, child));
            }
            return str.toString();
        } else {
            try {
                source = removeNamesapces(source);
                File inFile = File.createTempFile("xsltinput", ".txt");
                File outputFile = File.createTempFile("xsltoutput", ".txt");
                FileUtils.saveFileText(inFile, source);
                net.sf.saxon.Transform.main(new String[]{
                        "-o", outputFile.getPath(),
                        "" + inFile.getPath() + "",
                        "" + xsltFile.getPath() + ""
                });
                return FileUtils.getFileText(outputFile);
            } catch (Exception e) {
                SketchletPluginLogger.error("XSLT Error", e);
            }
        }

        return "";
    }

    private static String removeNamesapces(String text) {
        int n = text.indexOf("?>");

        if (n < 0) {
            n = 0;
        }

        int n1 = text.indexOf("<", n);
        if (n1 >= 0) {
            int n2 = text.indexOf(" ", n1 + 1);
            int n3 = text.indexOf(">", n1 + 1);
            if (n2 > 0 && n3 > 0 && n3 > n2) {
                return text.substring(0, n2) + text.substring(n3);
            }
        }
        return text;
    }

    public static void main(String args[]) {
        //FileUtils.saveFileText("c:/temp/out/xslt.txt", transform(new File("files/xml/"), new File("files/xslt/"), "*.xml"));
        FileUtils.saveFileText("c:/temp/out/xslt-java2.txt", transform(new File("files/bbproject/xml-java/java2.xml"), new File("files/bbproject/xslt/java.xsl"), "*.xml"));
        FileUtils.saveFileText("c:/temp/out/xslt-java.txt", transform(new File("files/bbproject/xml-java/java.xml"), new File("files/bbproject/xslt/java.xsl"), "*.xml"));
        FileUtils.saveFileText("c:/temp/out/xslt-filters.txt", transform(new File("C:/tutorials/LDAP/final/src/main/webapp/WEB-INF/web.xml"), new File("files/bbproject/xslt/filters.xsl"), "*.xml"));
        FileUtils.saveFileText("c:/temp/out/xslt-security.txt", transform(new File("C:/tutorials/LDAP/final/src/main/resources/META-INF/spring/backbase-portal-business-security.xml"), new File("files/bbproject/xslt/security.xsl"), "*.xml"));
        UmlGraphUtil.generateImageFile(new CascadingUmlUtil().getUmlGraphCode(FileUtils.getFileText("c:/temp/out/xslt-java2.txt")), new File("c:/temp/out"), new File("c:/temp/out/uml.png"), "");
        //FileUtils.saveFileText("c:/temp/out/uml.txt", new JavaUmlParser().parseFile(new File("files/java/")));
        //FileUtils.saveFileText("c:/temp/out/uml.txt", new JavaUmlParser().parseFile(new File("files/java/")));
    }

    public static String getValueFromTagAttribute(List<String> before, List<String> after, String attName) {

        return "";
    }
}
