/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.uml;

import net.sf.net.logger.SketchletPluginLogger;
import net.sf.sketchlet.common.file.FileUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zeljko
 */
public class UmlGraphUtil {

    public static void generateImageFile(final String strUML, File rootFolder, File imgFile, String cmdLineParams) {
        File srcFile = null;
        File dotFile = null;
        try {
            srcFile = File.createTempFile("umlgraph", ".java");
            File dotDir = new File(rootFolder, "dot");
            dotDir.mkdirs();
            // dotFile = File.createTempFile("umlgraph", ".dot");
            dotFile = new File(dotDir, imgFile.getName() + ".dot");
            FileUtils.saveFileText(srcFile, strUML);
            // PrintWriter err = new PrintWriter(new StringWriter());
            StringWriter strw = new StringWriter();
            PrintWriter err = new PrintWriter(strw);
            com.sun.tools.javadoc.Main.execute("UmlGraph",
                    err, err, err, "net.sf.sketchlet.umlgraph.doclet.UmlGraph", new String[]{"-package", "-output", dotFile.getAbsolutePath(), srcFile.getAbsolutePath()});

            SketchletPluginLogger.debug(strw.toString());

            List<String> dotParams = new ArrayList<String>();

            dotParams.add(ExternalPrograms.getGraphVizDotPath());
            dotParams.add("-Tpng");

            String params[] = cmdLineParams.split(" ");
            for (String param : params) {
                dotParams.add(param);
            }

            dotParams.add("-o" + imgFile.getAbsolutePath());
            dotParams.add(dotFile.getAbsolutePath());

            ProcessBuilder processBuilder2 = new ProcessBuilder(dotParams.toArray(new String[dotParams.size()]));
            processBuilder2.directory(new File(ExternalPrograms.getGraphVizDotPath()).getParentFile());
            Process theProcess2 = processBuilder2.start();
            theProcess2.waitFor();

            if (imgFile.exists()) {
            } else {
                SketchletPluginLogger.error("Could not generate UML Graph image.");
                SketchletPluginLogger.error(strUML);
            }
        } catch (Exception e) {
            SketchletPluginLogger.error("Could not generate UML Graph image.", e);
            SketchletPluginLogger.error(strUML);
        } finally {
            if (srcFile != null) {
                srcFile.delete();
            }
            /*if (dotFile != null) {
                dotFile.delete();
            }      */
        }
    }
}
