/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.help;

import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.designer.help.TutorialPanel.TutorialLine;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.awt.*;
import java.io.File;
import java.io.FileReader;

class TutorialSaxLoader extends DefaultHandler {
    private static final Logger log = Logger.getLogger(TutorialSaxLoader.class);

    private String strCharacters = "";
    private TutorialPanel panel;
    private String currentElement;

    public TutorialSaxLoader(TutorialPanel panel) {
        super();
        this.panel = panel;
    }

    public static void getTutorial(String strFile, TutorialPanel panel) {
        TutorialSaxLoader handler = new TutorialSaxLoader(panel);
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);

            FileReader r = new FileReader(strFile);
            xr.parse(new InputSource(r));
            r.close();
        } catch (Throwable e) {
            log.error(e);
        }
    }

    @Override
    public void startDocument() {
        panel.lines.removeAllElements();
    }

    @Override
    public void endDocument() {
        panel.model.fireTableDataChanged();
        if (panel.table.getRowCount() > 0) {
            panel.table.getSelectionModel().setSelectionInterval(0, 0);
            panel.imagePanel.revalidate();
            panel.imagePanel.repaint();
        }
    }

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }

        currentElement = strElem;
        strCharacters = "";

        if (strElem.equalsIgnoreCase("slide")) {
            panel.currentLine = new TutorialLine("", "");
            panel.lines.add(panel.currentLine);
        } else if (strElem.equalsIgnoreCase("slide-shape")) {
            try {
                panel.currentLine.p1 = new Point();
                panel.currentLine.p2 = new Point();
                panel.currentLine.p1.x = Integer.parseInt(atts.getValue("x1"));
                panel.currentLine.p1.y = Integer.parseInt(atts.getValue("y1"));
                panel.currentLine.p2.x = Integer.parseInt(atts.getValue("x2"));
                panel.currentLine.p2.y = Integer.parseInt(atts.getValue("y2"));
            } catch (Exception e) {
                log.error(e);
            }
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        this.processCharacters();
        currentElement = null;
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (currentElement != null) {
            String strValue = new String(ch, start, length);
            strCharacters += strValue;
        }
    }

    private void processCharacters() {
        strCharacters = strCharacters.replace("\\n", "\n");
        strCharacters = strCharacters.replace("\\r", "\r");
        strCharacters = strCharacters.replace("\\t", "\t");
        strCharacters = strCharacters.replace("&lt;", "<");
        strCharacters = strCharacters.replace("&gt;", ">");
        strCharacters = strCharacters.replace("&amp;", "&");
        if (currentElement == null) {
            return;
        }
        if (currentElement.equalsIgnoreCase("slide-title")) {
            panel.currentLine.strDescription = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("slide-image")) {
            try {
                File file = File.createTempFile("screen", ".png");
                file.deleteOnExit();
                FileUtils.copyFile(new File(new File(panel.file.getParentFile(), panel.file.getName().replace(".xml", "")) + "_files/" + strCharacters), file);
                panel.currentLine.strImageFile = file.getPath();
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("slide-shape")) {
            panel.currentLine.strShape = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("slide-memo")) {
            panel.currentLine.strMemo = strCharacters;
        }
    }
}
