/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.loaders.pluginloader;

import org.apache.log4j.Logger;

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
}
