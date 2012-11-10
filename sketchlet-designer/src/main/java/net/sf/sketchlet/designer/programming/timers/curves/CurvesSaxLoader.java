/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.timers.curves;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.FileReader;
import java.util.Vector;

public class CurvesSaxLoader extends DefaultHandler {

    Vector<Curve> curves;

    public CurvesSaxLoader(Vector<Curve> curves) {
        super();
        this.curves = curves;
    }

    public static void getCurves(String strFile, Vector<Curve> curves) {
        CurvesSaxLoader handler = new CurvesSaxLoader(curves);
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
    Curve currentCurve = null;
    CurveSegment currentSegment = null;

    public void startDocument() {
    }

    public void endDocument() {
    }

    boolean bFirstStiffnessSegment = true;

    public void startElement(String uri, String name, String qName, Attributes atts) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }

        currentElement = strElem;
        strCharacters = "";

        if (strElem.equalsIgnoreCase("curve")) {
            currentCurve = new Curve();
            currentCurve.segments.removeAllElements();
            bFirstStiffnessSegment = true;
            curves.add(currentCurve);
        } else if (currentCurve != null && strElem.equalsIgnoreCase("segment")) {
            currentSegment = new CurveSegment();
            currentCurve.segments.add(currentSegment);
        } else if (currentElement.equalsIgnoreCase("constraint-segment")) {
            if (bFirstStiffnessSegment) {
                currentCurve.stiffnessCurve.segments.removeAllElements();
                bFirstStiffnessSegment = false;
            }
            StiffnessSegment ss = new StiffnessSegment();
            try {
                ss.endTime = Double.parseDouble(atts.getValue("end-time"));
            } catch (Exception e) {
            }
            ss.minDuration = atts.getValue("min-duration");
            ss.maxDuration = atts.getValue("max-duration");
            currentCurve.stiffnessCurve.segments.add(ss);
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

        if (strElem.equalsIgnoreCase("curve")) {
            currentCurve = null;
        } else if (strElem.equalsIgnoreCase("sement")) {
            currentSegment = null;
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
        if (currentElement.equalsIgnoreCase("name") && this.currentCurve != null) {
            currentCurve.name = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("start-value") && this.currentCurve != null) {
            try {
                currentCurve.startValue = Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("end-time") && this.currentSegment != null) {
            try {
                currentSegment.endTime = Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        if (currentElement.equalsIgnoreCase("relative-value") && this.currentSegment != null) {
            try {
                currentSegment.relativeValue = Double.parseDouble(strCharacters);
            } catch (Exception e) {
            }
        }
        /*if (currentElement.equalsIgnoreCase("min-duration") && this.currentSegment != null) {
        currentSegment.minDuration = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("max-duration") && this.currentSegment != null) {
        currentSegment.maxDuration = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("start-after") && this.currentSegment != null) {
        currentSegment.startAfter = strCharacters;
        }
        if (currentElement.equalsIgnoreCase("finish-before") && this.currentSegment != null) {
        currentSegment.finishBefore = strCharacters;
        }*/
    }
}

