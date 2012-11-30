/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.util;

import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author zobrenovic
 */
public class XMLUtils {

    public static String prepareForXML(String strText) {
        if (strText != null) {
            return StringEscapeUtils.escapeXml(strText);
        } else {
            return "";
        }

    }

    private static String getEsc(String str, boolean isAttVal) {
        String result = "";
        for (int i = 0; i
                < str.length(); i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case '&':
                    result += "&amp;";
                    break;

                case '<':
                    result += "&lt;";
                    break;

                case '>':
                    result += "&gt;";
                    break;

                case '\"':
                    if (isAttVal) {
                        result += "&quot;";
                    } else {
                        result += '\"';
                    }

                    break;
                case '\n':
                    result += "\\n";

                    break;
                case '\r':
                    result += "\\r";

                    break;
                case '\t':
                    result += "\\t";

                    break;
                default:
                    if (ch > '\u007f' && ch < 2000) {
                        result += "&#";
                        result += Integer.toString(ch);
                        result += ';';
                    } else if (ch > '\u001f' && ch < 2000) {
                        result += ch;
                    } else {
                        result += " ";
                    }

            }
        }

        return result;
    }
}
