/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.net;

import net.sf.sketchlet.blackboard.CommandHandler;
import net.sf.sketchlet.blackboard.VariablesBlackboard;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.SketchletGraphicsContext;
import org.apache.log4j.Logger;

/**
 * @author zobrenovic
 */
public class SketchletNetworkProtocol {
    private static final Logger log = Logger.getLogger(SketchletNetworkProtocol.class);

    public static void processCommand(String line) {
        if (line.startsWith("ADDVAR ")) {
            CommandHandler.processAddVarCommand(line);
        } else if (line.startsWith("SETA ")) {
            CommandHandler.processSetArrayCommand(line, true);
        } else if (line.startsWith("SETP ")) {
            CommandHandler.processSetCommand(line, false);
        } else if (line.startsWith("SET ")) {
            CommandHandler.processSetCommand(line, true);
        } else if (line.startsWith("GOTO ")) {
            String strPage = line.substring(5);
            if (SketchletContext.getInstance() != null) {
                SketchletContext.getInstance().goToPage(strPage);
            }
        } else if (line.startsWith("START ACTION ")) {
            String strMacro = line.substring(13);
            if (SketchletContext.getInstance() != null) {
                SketchletContext.getInstance().startMacro(strMacro);
            }
        } else if (line.startsWith("STOP ACTION ")) {
            String strMacro = line.substring(12);
            if (SketchletContext.getInstance() != null) {
                SketchletContext.getInstance().stopMacro(strMacro);
            }
        } else if (line.startsWith("START TIMER ")) {
            String strTimer = line.substring(12);
            if (SketchletContext.getInstance() != null) {
                SketchletContext.getInstance().startTimer(strTimer);
            }
        } else if (line.startsWith("PAUSE TIMER ")) {
            String strTimer = line.substring(12);
            if (SketchletContext.getInstance() != null) {
                SketchletContext.getInstance().pauseTimer(strTimer);
            }
        } else if (line.startsWith("STOP TIMER ")) {
            String strTimer = line.substring(11);
            if (SketchletContext.getInstance() != null) {
                SketchletContext.getInstance().stopTimer(strTimer);
            }
        } else if (line.startsWith("IMAGE ")) {
            VariablesBlackboard.setImageDrawnByExternalProcess(true);
            String strCommand = line.substring(6).trim();
            String params[] = strCommand.split(" ");
            SketchletNetworkProtocol.processDrawCommand(strCommand, params);
        } else {
            log.info("Command '" + line + "' not recognized.");
        }
    }

    public static void processDrawCommand(String strCommand, String params[]) {
        log.info("PDC: " + params[0] + " " + params.length);
        try {
            if (params.length >= 4 && params[0].equalsIgnoreCase("SETCOLOR")) {
                int r = (int) Double.parseDouble(params[1]);
                int g = (int) Double.parseDouble(params[2]);
                int b = (int) Double.parseDouble(params[3]);
                SketchletGraphicsContext.getInstance().setColor(r, g, b);
            }
            if (params.length >= 5 && params[0].equalsIgnoreCase("SETCOLOR")) {
                int r = (int) Double.parseDouble(params[1]);
                int g = (int) Double.parseDouble(params[2]);
                int b = (int) Double.parseDouble(params[3]);
                int t = (int) Double.parseDouble(params[4]);
                SketchletGraphicsContext.getInstance().setColor(r, g, b, t);
            }
            if (params.length >= 2 && params[0].equalsIgnoreCase("SETTRANSPARENCY")) {
                float t = (float) Double.parseDouble(params[1]);
                SketchletGraphicsContext.getInstance().setTransparency(t);
            }
            if (params.length >= 2 && params[0].equalsIgnoreCase("SETLINEWIDTH")) {
                double w = Double.parseDouble(params[1]);
                SketchletGraphicsContext.getInstance().setLineWidth(w);
            }
            if (params.length >= 4 && params[0].equalsIgnoreCase("SETFONT")) {
                String name = params[1];
                String style = params[2];
                int size = (int) Double.parseDouble(params[3]);
                SketchletGraphicsContext.getInstance().setFont(name, style, size);
            }
            if (params.length >= 4 && params[0].equalsIgnoreCase("DRAWTEXT")) {
                String text = params[1];
                int x = (int) Double.parseDouble(params[2]);
                int y = (int) Double.parseDouble(params[3]);
                SketchletGraphicsContext.getInstance().drawText(text, x, y);
            }
            if (params.length >= 5 && params[0].equalsIgnoreCase("DRAWLINE")) {
                int x1 = (int) Double.parseDouble(params[1]);
                int y1 = (int) Double.parseDouble(params[2]);
                int x2 = (int) Double.parseDouble(params[3]);
                int y2 = (int) Double.parseDouble(params[4]);
                SketchletGraphicsContext.getInstance().drawLine(x1, y1, x2, y2);
            }
            if (params.length >= 5 && params[0].equalsIgnoreCase("DRAWRECT")) {
                int x1 = (int) Double.parseDouble(params[1]);
                int y1 = (int) Double.parseDouble(params[2]);
                int w = (int) Double.parseDouble(params[3]);
                int h = (int) Double.parseDouble(params[4]);
                SketchletGraphicsContext.getInstance().drawRect(x1, y1, w, h);
            }
            if (params.length >= 5 && params[0].equalsIgnoreCase("DRAWELLIPSE")) {
                int x1 = (int) Double.parseDouble(params[1]);
                int y1 = (int) Double.parseDouble(params[2]);
                int w = (int) Double.parseDouble(params[3]);
                int h = (int) Double.parseDouble(params[4]);
                SketchletGraphicsContext.getInstance().drawEllipse(x1, y1, w, h);
            }
            if (params.length >= 5 && params[0].equalsIgnoreCase("DRAWCIRCLE")) {
                int cx = (int) Double.parseDouble(params[1]);
                int cy = (int) Double.parseDouble(params[2]);
                int r = (int) Double.parseDouble(params[3]);
                SketchletGraphicsContext.getInstance().drawCircle(cx, cy, r);
            }
            if (params.length >= 5 && params[0].equalsIgnoreCase("FILLRECT")) {
                int x1 = (int) Double.parseDouble(params[1]);
                int y1 = (int) Double.parseDouble(params[2]);
                int w = (int) Double.parseDouble(params[3]);
                int h = (int) Double.parseDouble(params[4]);
                SketchletGraphicsContext.getInstance().fillRect(x1, y1, w, h);
            }
            if (params.length >= 5 && params[0].equalsIgnoreCase("FILLELLIPSE")) {
                int x1 = (int) Double.parseDouble(params[1]);
                int y1 = (int) Double.parseDouble(params[2]);
                int w = (int) Double.parseDouble(params[3]);
                int h = (int) Double.parseDouble(params[4]);
                SketchletGraphicsContext.getInstance().fillEllipse(x1, y1, w, h);
            }
            if (params.length >= 5 && params[0].equalsIgnoreCase("FILLCIRCLE")) {
                int cx = (int) Double.parseDouble(params[1]);
                int cy = (int) Double.parseDouble(params[2]);
                int r = (int) Double.parseDouble(params[3]);
                SketchletGraphicsContext.getInstance().fillCircle(cx, cy, r);
            }
            if (params.length >= 4 && params[0].equalsIgnoreCase("DRAWIMAGE")) {
                String url = params[1];
                int x = (int) Double.parseDouble(params[2]);
                int y = (int) Double.parseDouble(params[3]);
                SketchletGraphicsContext.getInstance().drawImage(url, x, y);
            }
            if (params.length >= 6 && params[0].equalsIgnoreCase("DRAWIMAGE")) {
                String url = params[1];
                int x = (int) Double.parseDouble(params[2]);
                int y = (int) Double.parseDouble(params[3]);
                int w = (int) Double.parseDouble(params[4]);
                int h = (int) Double.parseDouble(params[5]);
                SketchletGraphicsContext.getInstance().drawImage(url, x, y, w, h);
            }
            if (params.length >= 1 && params[0].equalsIgnoreCase("REPAINT")) {
                SketchletGraphicsContext.getInstance().repaint();
            }
            if (params.length >= 3 && params[0].equalsIgnoreCase("TRANSLATE")) {
                int x = (int) Double.parseDouble(params[1]);
                int y = (int) Double.parseDouble(params[2]);
                SketchletGraphicsContext.getInstance().translate(x, y);
            }
            if (params.length >= 4 && params[0].equalsIgnoreCase("ROTATE")) {
                int a = (int) Double.parseDouble(params[1]);
                int x = (int) Double.parseDouble(params[2]);
                int y = (int) Double.parseDouble(params[3]);
                SketchletGraphicsContext.getInstance().rotate(a, x, y);
            }
            if (params.length >= 3 && params[0].equalsIgnoreCase("SCALE")) {
                double sw = Double.parseDouble(params[1]);
                double sy = Double.parseDouble(params[2]);
                SketchletGraphicsContext.getInstance().scale(sw, sy);
            }
        } catch (Exception e) {
            log.error(e);
        }

    }
}
