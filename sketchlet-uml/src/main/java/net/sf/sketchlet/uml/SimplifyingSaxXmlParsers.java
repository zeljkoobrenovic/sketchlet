/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.uml;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import net.sf.sketchlet.common.EscapeChars;
import net.sf.sketchlet.common.file.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author zeljko
 */
public class SimplifyingSaxXmlParsers extends DefaultHandler {

    private boolean firstElement = true;

    public static void main(String args[]) {
        System.out.println(SimplifyingSaxXmlParsers.parse(new File("C:/svnroot/projects/CAK/trunk/portal/src/main/resources/META-INF/spring/cak-portal-presentation-security.xml")));
    }

    public SimplifyingSaxXmlParsers() {
        super();
    }
    private String currentElement;
    private StringWriter simpleXml = new StringWriter();

    public static String parse(File xmlFile) {
        SimplifyingSaxXmlParsers handler = new SimplifyingSaxXmlParsers();
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);

            StringReader r = new StringReader(FileUtils.getFileText(xmlFile));
            xr.parse(new InputSource(r));
            r.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return handler.simpleXml.toString();
    }

    @Override
    public void startDocument() {
        path.clear();
        simpleXml = new StringWriter();
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }
        path.add(strElem);

        simpleXml.append("<" + strElem);
        for (int i = 0; i < atts.getLength(); i++) {
            if (firstElement) {
                break;
            }
            simpleXml.append(" " + atts.getLocalName(i) + "=\"" + EscapeChars.forHTMLTag(atts.getValue(i)) + "\"");
        }
        simpleXml.append(">");

        currentElement = strElem;
        strCharacters = "";
        firstElement = false;
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        this.processCharacters();
        currentElement = null;
        if (path.size() > 0) {
            path.remove(path.size() - 1);
        }
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }
        simpleXml.append("</" + strElem + ">");
    }
    private String strCharacters = "";

    @Override
    public void characters(char ch[], int start, int length) {
        if (currentElement != null) {
            String strValue = new String(ch, start, length);
            strCharacters += strValue;
        }
    }

    public void processCharacters() {
        simpleXml.append(EscapeChars.forHTMLTag(strCharacters));
    }
    List<String> path = new ArrayList<String>();

    private String path() {
        String strPath = "/";
        for (String p : path) {
            strPath += p + "/";
        }

        return strPath;
    }
}