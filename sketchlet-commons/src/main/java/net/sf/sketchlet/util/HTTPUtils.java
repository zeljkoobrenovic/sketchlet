/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import net.sf.sketchlet.common.DefaultSettings;
/**
 *
 * @author zobrenovic
 */
public class HTTPUtils {

    public static String getTextFromURL(String strURL) {
        try {
            URLConnection urlConnection = new URL(strURL).openConnection();
            urlConnection.setRequestProperty("User-agent", DefaultSettings.getHTTPUserAgent());
            InputStream input = urlConnection.getInputStream();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            int result;
            while ((result = input.read()) != -1) {
                byte b = (byte) result;
                buf.write(b);
            }
            return buf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String args[]) {
        System.out.println(getTextFromURL("http://www.tno.nl/werkenbij/content.cfm?context=vacature&content=vacatures_view&vacnr=50111799&sort=1"));
    }
}
