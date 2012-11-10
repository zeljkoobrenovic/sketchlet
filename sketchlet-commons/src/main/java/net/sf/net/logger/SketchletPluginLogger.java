/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.net.logger;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author zeljko
 */
public class SketchletPluginLogger {

    private static Logger logger;

    static {
        try {
            BasicConfigurator.configure();
            logger = Logger.getLogger("net.sf.sketchlet.plugin");
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
        }
    }

    // printing methods:
    public static void trace(Object message) {
        logger.trace(message);
    }

    public static void debug(Object message) {
        logger.debug(message);
    }

    public static void info(Object message) {
        logger.info(message);
    }

    public void warn(Object message) {
        logger.warn(message);
    }

    public static void error(Object message) {
        logger.error(message);
    }

    public static void error(Object message, Throwable t) {
        logger.error(message, t);
    }

    public static void fatal(Object message) {
        logger.fatal(message);
    }

    public static void main(String... args) {
        SketchletPluginLogger.trace("ha");
        SketchletPluginLogger.fatal("ha");
        SketchletPluginLogger.fatal("ha");
        SketchletPluginLogger.fatal("ha");
        SketchletPluginLogger.fatal("ha");
        SketchletPluginLogger.fatal("ha");
        SketchletPluginLogger.fatal("ha");
    }
}
