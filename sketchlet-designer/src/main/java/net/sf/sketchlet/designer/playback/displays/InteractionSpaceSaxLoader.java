/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.playback.displays;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileReader;
import java.util.Vector;

public class InteractionSpaceSaxLoader extends DefaultHandler {

    Vector<ScreenMapping> displays;

    public InteractionSpaceSaxLoader(Vector<ScreenMapping> displays) {
        super();
        this.displays = displays;
    }

    public static void getScreens(String strFile, Vector<ScreenMapping> displays) {
        InteractionSpaceSaxLoader handler = new InteractionSpaceSaxLoader(displays);
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);

            FileReader r = new FileReader(strFile);
            xr.parse(new InputSource(r));
            r.close();
        } catch (Throwable e) {
//            e.printStackTrace();
        }
    }

    String currentElement;
    ScreenMapping currentScreen = null;
    int indexCut = -1;
    int indexTransform = -1;

    public void startDocument() {
        displays.removeAllElements();
    }

    public void endDocument() {
        if (InteractionSpace.gridSpacing <= 0) {
            InteractionSpace.gridSpacing = 30;
        }
    }

    public void startElement(String uri, String name, String qName, Attributes atts) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }

        currentElement = strElem;
        strCharacters = "";

        if (strElem.equalsIgnoreCase("display")) {
            currentScreen = new ScreenMapping();
            displays.add(currentScreen);
        }
    }

    public void endElement(String uri, String name, String qName) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }

        this.processCharacters();

        currentElement = null;

        if (strElem.equalsIgnoreCase("display")) {
            currentScreen = null;
            indexCut = -1;
            indexTransform = -1;
        }
    }

    String strCharacters = "";

    public void characters(char ch[], int start, int length) {
        if (currentElement != null) {
            String strValue = new String(ch, start, length);
            strCharacters += strValue;
        }
    }

    public void processCharacters() {
        strCharacters = strCharacters.replace("\\n", "\n");
        strCharacters = strCharacters.replace("\\r", "\r");
        strCharacters = strCharacters.replace("\\t", "\t");
        strCharacters = strCharacters.replace("&lt;", "<");
        strCharacters = strCharacters.replace("&gt;", ">");
        strCharacters = strCharacters.replace("&amp;", "&");
        if (currentElement == null) {
            return;
        }
        if (currentElement.equalsIgnoreCase("sketch-width")) {
            try {
                InteractionSpace.sketchWidth = Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("sketch-height")) {
            try {
                InteractionSpace.sketchHeight = Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("sketch-left")) {
            try {
                InteractionSpace.left = Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("sketch-right")) {
            try {
                InteractionSpace.right = Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("sketch-top")) {
            try {
                InteractionSpace.top = Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("sketch-bottom")) {
            try {
                InteractionSpace.bottom = Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("start-angle")) {
            try {
                InteractionSpace.angleStart = Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("end-angle")) {
            try {
                InteractionSpace.angleEnd = Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("grid-spacing")) {
            try {
                InteractionSpace.gridSpacing = Integer.parseInt(strCharacters);
            } catch (Exception e) {
            }
        }

        if (currentScreen != null) {
            if (currentElement.equalsIgnoreCase("alwaysOnTop")) {
                currentScreen.alwaysOnTop.setSelected(strCharacters.equalsIgnoreCase("true"));
            }
            if (currentElement.equalsIgnoreCase("enable-display")) {
                currentScreen.enable.setSelected(strCharacters.equalsIgnoreCase("true"));
            }
            if (currentElement.equalsIgnoreCase("fitToScreen")) {
                currentScreen.fitToScreen.setSelected(strCharacters.equalsIgnoreCase("true"));
            }
            if (currentElement.equalsIgnoreCase("showDecoration")) {
                currentScreen.showDecoration.setSelected(strCharacters.equalsIgnoreCase("true"));
            }
            if (currentElement.equalsIgnoreCase("showMaximized")) {
                currentScreen.showMaximized.setSelected(strCharacters.equalsIgnoreCase("true"));
            }
            if (currentElement.equalsIgnoreCase("screenIndex")) {
                try {
                    currentScreen.screenIndex.setSelectedIndex((int) Double.parseDouble(strCharacters));
                } catch (Exception e) {
                }
            }
            if (currentElement.equalsIgnoreCase("showToolbar")) {
                currentScreen.showToolbar.setSelected(strCharacters.equalsIgnoreCase("true"));
            }
            if (currentElement.equalsIgnoreCase("showOnDisplay")) {
                currentScreen.showOnDisplay = strCharacters;
            }

            if (currentElement.equalsIgnoreCase("export")) {
                currentScreen.exportDisplay.setSelected(strCharacters.equalsIgnoreCase("true"));
            }
            if (currentElement.equalsIgnoreCase("export-variable-path")) {
                currentScreen.exportStrFileVariableCombo = strCharacters;
            }
            if (currentElement.equalsIgnoreCase("export-file-path")) {
                currentScreen.exportFileField.setText(strCharacters);
            }
            if (currentElement.equalsIgnoreCase("export-on")) {
                currentScreen.exportStrOn = strCharacters;
            }
            if (currentElement.equalsIgnoreCase("export-frequency")) {
                currentScreen.exportFrequency.setText(strCharacters);
            }

            if (currentElement.equalsIgnoreCase("cut-param1")) {
                this.indexCut++;
                currentScreen.cutFromSketch[indexCut][1] = strCharacters;
            }
            if (currentElement.equalsIgnoreCase("transform-action")) {
                this.indexTransform++;
                currentScreen.transformations[indexTransform][0] = strCharacters;
            }
            if (currentElement.equalsIgnoreCase("transform-param1")) {
                currentScreen.transformations[indexTransform][1] = strCharacters;
            }
        }
    }
}
