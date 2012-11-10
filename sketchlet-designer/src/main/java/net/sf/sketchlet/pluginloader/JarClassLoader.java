/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.pluginloader;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author zobrenovic
 */
public class JarClassLoader extends URLClassLoader {
    private static final Logger log = Logger.getLogger(URLClassLoader.class);

    public JarClassLoader(URL[] urls) {
        super(urls);
    }

    public void addFile(String path) throws MalformedURLException {
        String urlPath = "jar:file:///" + path + "!/";
        log.info(urlPath);
        addURL(new URL(urlPath));
    }

    public static void main(String args[]) {
        try {
            log.info("First attempt...");
            Class.forName("net.sf.sketchlet.common.config.ConfigItem");
        } catch (Exception ex) {
            log.info("Failed.");
        }

        try {
            URL urls[] = {};

            JarClassLoader cl = new JarClassLoader(urls);
            cl.addFile("/C:/Documents and Settings/zobrenovic/My Documents/NetBeansProjects/amico-script-cl/dist/amico-script-cl.jar");
            // /C:/Documents and Settings/zobrenovic/My Documents/NetBeansProjects/amico-script-cl
            log.info("Second attempt...");
            InputStream in = cl.getResourceAsStream("jar:file:///C:/Documents and Settings/zobrenovic/My Documents/NetBeansProjects/amico-script-cl/dist/amico-script-cl.jar!/META-INF/plugins.xml");
            int n;
            if (in != null) {
                while ((n = in.read()) != -1) {
                    System.out.print((char) n);
                }
            }
            Class c = cl.loadClass("net.sf.sketchlet.script.cl.SketchletScriptCl");
            Object o = c.newInstance();
            log.info(c.getMethod("init", Class.forName("net.sf.sketchlet.context.SketchletContext"), File.class).invoke(o, null, new File("")));
            log.info("Success!");
        } catch (Exception ex) {
            log.info("Failed.");
            ex.printStackTrace();
        }
    }
}
