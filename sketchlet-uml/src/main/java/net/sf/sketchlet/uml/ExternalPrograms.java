/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.uml;

import java.io.File;
import net.sf.net.logger.SketchletPluginLogger;
import net.sf.sketchlet.common.context.SketchletContextUtils;

/**
 *
 * @author zeljko
 */
public class ExternalPrograms {

    private final static String graphviz = SketchletContextUtils.getSketchletDesignerHome() + "bin/plugins/plugin-graphs/tools/graphviz";
    private final static String plotutils = SketchletContextUtils.getSketchletDesignerHome() + "bin/plugins/plugin-graphs/tools/plotutils";
    private final static String txl = SketchletContextUtils.getSketchletDesignerHome() + "bin/plugins/plugin-graphs/tools/txl";

    public static String getGraphVizDotPath() {
        //GRAPHVIZ_DOT
        String path = getString("GRAPHVIZ_DOT", null);
        if (path == null) {
            File file = new File(getString("GRAPHVIZ_HOME", graphviz) + "/bin/dot.exe");
            if (!file.exists()) {
                file = new File(getString("GRAPHVIZ_HOME", graphviz) + "/bin/dot");
                if (!file.exists()) {
                    SketchletPluginLogger.error("Cannot file the Graphviz dot program. Set the system variable GRAPHVIZ_DOT to point to the Graphviz dot executable file.");
                }
            }

            path = file.getAbsolutePath();
        }
        return path;
    }

    public static String getPlotUtilsPath() {
        return new File(getString("PLOTUTILS_HOME", plotutils) + "/bin/pic2plot").getAbsolutePath();
    }

    public static String getTxlPath() {
        return new File(getString("TXL_HOME", txl) + "/bin/txl").getAbsolutePath();
    }

    private static String getString(String property, String defaultValue) {
        String value = System.getenv(property);

        if (value == null || value.isEmpty()) {
            value = System.getProperty(property);
            if (value == null || value.isEmpty()) {
                value = defaultValue;
            }
        }
        if (value == null) {
            return value;
        }
        if (value.endsWith("/") || value.endsWith("\\")) {
            value = value.substring(0, value.length() - 1);
        }
        return value.trim();
    }

    public static void main(String args[]) {
        System.out.println(getGraphVizDotPath());
    }
}
