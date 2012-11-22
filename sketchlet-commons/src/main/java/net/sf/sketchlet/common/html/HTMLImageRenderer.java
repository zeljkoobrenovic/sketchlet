/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.html;

import org.w3c.tidy.Configuration;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.swing.Java2DRenderer;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 */
public class HTMLImageRenderer {

    private static DocumentBuilderFactory factory;
    private static DocumentBuilder builder;

    public static BufferedImage getImage(String strHTML, int w, int h) {
        BufferedImage img = null;
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            factory.setIgnoringComments(true);
            factory.setValidating(false);
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {

                public void warning(SAXParseException exception) throws SAXException {
                }

                public void error(SAXParseException exception) throws SAXException {
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                }
            });
            Java2DRenderer renderer = new Java2DRenderer(builder.parse(getInputStream(strHTML)), w, h);
            // renderer.getSharedContext().setCss(new StyleReference("http://www.w3schools.com/stdtheme.css"));
            renderer.setBufferedImageType(BufferedImage.TYPE_INT_ARGB);
            img = renderer.getImage();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

        return img;
    }

    public static Tidy tidy = new Tidy();
    static boolean useTidy = false;

    public static InputStream getInputStream(String strHTML) {
        InputStream input = null;
        try {
            tidy = new Tidy(); // obtain a new Tidy instance
            tidy.setXmlOut(true);
            tidy.setXHTML(true);
            tidy.setDocType("omit");
            tidy.setNumEntities(true);
            // tidy.setXHTML(true);
            // tidy.setXmlTags(true);
            //tidy.setDropFontTags(true);
            tidy.setShowWarnings(false);
            //tidy.setErrout( new PrintWriter( System.out ));
            // tidy.setOnlyErrors(true);
            tidy.setErrfile("jtidy-errfile.txt");
            tidy.setQuiet(true);
            tidy.setMakeClean(true);
            tidy.setDropEmptyParas(true);
            tidy.setDropProprietaryAttributes(true);
            tidy.setFixBackslash(true);
            tidy.setFixComments(true);
            tidy.setFixUri(true);
            tidy.setForceOutput(true);
            tidy.setMakeBare(true);
            tidy.setRepeatedAttributes(Configuration.KEEP_FIRST);

            ByteArrayOutputStream tidyOutput = new ByteArrayOutputStream();
            // tidy.parse( input, System.out );
            tidy.parse(new ByteArrayInputStream(strHTML.getBytes("UTF-8")), tidyOutput);
            String result = removeTags(tidyOutput.toString(), new String[]{"head", "HEAD", "script", "noscript", "NOSCRIPT"});
            //String result = removeTags(tidyOutput.toString(), new String[]{"script", "noscript", "NOSCRIPT"});
            result = removeString(result, "&#0;");

            /*int n1 = result.indexOf("<!DOCTYPE");

            if (n1 >= 0) {
            int n2 = result.indexOf(">");

            if (n2 > n1) {
            result = result.substring(n2 + 1).trim();
            }
            }*/

            // result = "<?xml version='1.0' encoding='UTF-8'?>" + result.trim();
            input = new ByteArrayInputStream(result.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace(System.out);
            input = null;
        }

        return input;
    }

    public static String removeTags(String content, String tags[]) {
        String result = content;
        for (int i = 0; i < tags.length; i++) {
            int n1;
            while ((n1 = result.indexOf("<" + tags[i])) >= 0) {
                String endTag = "</" + tags[i] + ">";
                int n2 = result.indexOf(endTag, n1);

                if (n2 > 0) {
                    result = result.substring(0, n1) + result.substring(n2 + endTag.length());
                }
            }
        }

        return result;
    }

    public static String removeString(String content, String removeString) {
        String result = content;

        int n1;
        while ((n1 = result.indexOf(removeString)) >= 0) {
            result = result.substring(0, n1) + result.substring(n1 + removeString.length());
        }

        return result;
    }
}
