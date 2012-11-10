/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.util;

import net.sf.sketchlet.common.XPathEvaluator;
import net.sf.sketchlet.context.SketchletContext;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.Vector;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import org.w3c.dom.NodeList;

/**
 *
 * @author zobrenovic
 */
public class XMLHelper {

    public static void save(String strFile, String strRoot, List<Object[]> dataList) {
        Object data[][] = new Object[dataList.size()][];
        int i = 0;
        for (Object row[] : dataList) {
            data[i++] = row;
        }
        XMLHelper.save(strFile, strRoot, data);
    }

    public static void save(String strFile, String strRoot, Object data[][]) {
        try {
            String workingDirectory = SketchletContext.getInstance().getCurrentProjectDirectory();
            if (!workingDirectory.endsWith("/") && !workingDirectory.endsWith("\\")) {
                workingDirectory += File.separator;
            }
            new File(workingDirectory).mkdirs();
            strFile = workingDirectory + SketchletContextUtils.sketchletDataDir() + "/conf/communicator/" + strFile;
            // System.out.println("Loading " + Global.workingDirectory + SketchletContextUtils.sketchletDataDir() + "/conf/communicator/" + strFile);

            PrintWriter out = new PrintWriter(new FileWriter(strFile));

            out.println("<?xml version='1.0' encoding='UTF-8'?>");
            out.println("<" + strRoot + ">");
            out.println("<data>");
            for (int i = 0; i < data.length; i++) {
                boolean bSaveRow = false;
                for (int j = 0; j < data[i].length; j++) {
                    if (!data[i][j].toString().isEmpty()) {
                        bSaveRow = true;
                        break;
                    }
                }
                if (!bSaveRow) {
                    continue;
                }
                out.println("<row>");
                for (int j = 0; j < data[i].length; j++) {
                    out.print("<col>");
                    out.print(XMLUtils.prepareForXML(data[i][j].toString()));
                    out.println("</col>");
                }
                out.println("</row>");
            }
            out.println("</data>");
            out.println("</" + strRoot + ">");

            out.flush();
            out.close();
        } catch (Exception e) {
        }
    }

    public static void save(String strFile, String strRoot, Object data[][], Vector<Object[][]> paramsVector) {
        try {
            String workingDirectory = SketchletContext.getInstance().getCurrentProjectDirectory();
            if (!workingDirectory.endsWith("/") && !workingDirectory.endsWith("\\")) {
                workingDirectory += File.separator;
            }
            new File(workingDirectory).mkdirs();
            strFile = workingDirectory + SketchletContextUtils.sketchletDataDir() + "/conf/communicator/" + strFile;
            // System.out.println("Loading " + Global.workingDirectory + SketchletContextUtils.sketchletDataDir() + "/conf/communicator/" + strFile);

            PrintWriter out = new PrintWriter(new FileWriter(strFile));

            out.println("<?xml version='1.0' encoding='UTF-8'?>");
            out.println("<" + strRoot + ">");
            out.println("<data>");
            for (int i = 0; i < data.length; i++) {
                out.println("<row>");
                for (int j = 0; j < data[i].length; j++) {
                    out.print("<col>");
                    out.print(XMLUtils.prepareForXML(data[i][j].toString()));
                    out.println("</col>");
                }
                out.println("<params>");
                Object[][] params = paramsVector.elementAt(i);
                for (int k = 0; k < params.length; k++) {
                    boolean bSaveRow = false;
                    for (int l = 0; l < params[k].length; l++) {
                        if (!params[k][l].toString().isEmpty()) {
                            bSaveRow = true;
                            break;
                        }
                    }
                    if (!bSaveRow) {
                        continue;
                    }
                    out.print("<param-row>");
                    for (int l = 0; l < params[k].length; l++) {
                        out.print("<param-col>");
                        out.print(XMLUtils.prepareForXML(params[k][l].toString()));
                        out.print("</param-col>");
                    }
                    out.println("</param-row>");
                }
                out.println("</params>");
                out.println("</row>");
            }
            out.println("</data>");
            out.println("</" + strRoot + ">");

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadAsList(String strFile, String strRoot, List<Object[]> dataList, int cols) {
        try {
            String workingDirectory = SketchletContext.getInstance().getCurrentProjectDirectory();
            if (!workingDirectory.endsWith("/") && !workingDirectory.endsWith("\\")) {
                workingDirectory += File.separator;
            }
            // System.out.println("Loading " + Global.workingDirectory + SketchletContextUtils.sketchletDataDir() + "/conf/communicator/" + strFile);
            new File(workingDirectory).mkdirs();
            File file = new File(workingDirectory + SketchletContextUtils.sketchletDataDir() + "/conf/communicator/" + strFile);

            if (file.exists()) {
                XPathEvaluator xp = new XPathEvaluator();
                xp.createDocumentFromFile(file);

                NodeList dataNodes = xp.getNodes("/" + strRoot + "/data/row");

                if (dataNodes != null) {
                    for (int i = 0; i < dataNodes.getLength(); i++) {
                        Object row[] = new Object[cols];
                        for (int j = 0; j < cols; j++) {
                            row[j] = xp.getString("/" + strRoot + "/data/row[position()=" + (i + 1) + "]/col[position()=" + (j + 1) + "]").trim();
                        }
                        dataList.add(row);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load(String strFile, String strRoot, Object data[][]) {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (data[i][j] instanceof Boolean) {
                    data[i][j] = new Boolean(false);
                } else {
                    data[i][j] = "";
                }
            }
        }
        try {
            String workingDirectory = SketchletContext.getInstance().getCurrentProjectDirectory();
            if (!workingDirectory.endsWith("/") && !workingDirectory.endsWith("\\")) {
                workingDirectory += File.separator;
            }
            // System.out.println("Loading " + Global.workingDirectory + SketchletContextUtils.sketchletDataDir() + "/conf/communicator/" + strFile);
            new File(workingDirectory).mkdirs();
            File file = new File(workingDirectory + SketchletContextUtils.sketchletDataDir() + "/conf/communicator/" + strFile);

            if (file.exists()) {
                XPathEvaluator xp = new XPathEvaluator();
                xp.createDocumentFromFile(file);

                NodeList dataNodes = xp.getNodes("/" + strRoot + "/data");

                if (dataNodes != null) {
                    for (int i = 0; i < data.length; i++) {
                        for (int j = 0; j < data[i].length; j++) {
                            String strData = xp.getString("/" + strRoot + "/data/row[position()=" + (i + 1) + "]/col[position()=" + (j + 1) + "]").trim();
                            if (data[i][j] instanceof String) {
                                data[i][j] = strData;
                            } else if (data[i][j] instanceof Boolean) {
                                data[i][j] = new Boolean(strData.equalsIgnoreCase("true"));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load(String strFile, String strRoot, Object data[][], Vector<Object[][]> params) {
        try {
            String workingDirectory = SketchletContext.getInstance().getCurrentProjectDirectory();
            if (!workingDirectory.endsWith("/") && !workingDirectory.endsWith("\\")) {
                workingDirectory += File.separator;
            }
            // System.out.println("Loading " + Global.workingDirectory + SketchletContextUtils.sketchletDataDir() + "/conf/communicator/" + strFile);
            new File(workingDirectory).mkdirs();
            File file = new File(workingDirectory + SketchletContextUtils.sketchletDataDir() + "/conf/communicator/" + strFile);

            if (file.exists()) {
                XPathEvaluator xp = new XPathEvaluator();
                xp.createDocumentFromFile(file);

                NodeList dataNodes = xp.getNodes("/" + strRoot + "/data");

                if (dataNodes != null) {
                    for (int i = 0; i < data.length; i++) {
                        for (int j = 0; j < data[i].length; j++) {
                            String strData = xp.getString("/" + strRoot + "/data/row[position()=" + (i + 1) + "]/col[position()=" + (j + 1) + "]").trim();
                            if (data[i][j] instanceof String) {
                                data[i][j] = strData;
                            } else if (data[i][j] instanceof Boolean) {
                                data[i][j] = new Boolean(strData.equalsIgnoreCase("true"));
                            }
                        }
                        Object[][] param = params.elementAt(i);
                        for (int pr = 0; pr < param.length; pr++) {
                            for (int pc = 0; pc < param[pr].length; pc++) {
                                String strParam = xp.getString("/" + strRoot + "/data/row[position()=" + (i + 1) + "]/params/param-row[position()=" + (pr + 1) + "]/param-col[position()=" + (pc + 1) + "]").trim();
                                if (param[pr][pc] instanceof String) {
                                    param[pr][pc] = strParam;
                                } else if (param[pr][pc] instanceof Boolean) {
                                    param[pr][pc] = new Boolean(strParam.equalsIgnoreCase("true"));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
