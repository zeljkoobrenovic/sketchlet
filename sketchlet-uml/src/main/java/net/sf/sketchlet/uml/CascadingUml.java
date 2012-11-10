/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.uml;

import net.sf.net.logger.SketchletPluginLogger;
import net.sf.sketchlet.common.file.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zeljko
 */
public class CascadingUml {

    public static void main(String args[]) {

        if (args.length > 0) {
            System.out.println(args[0]);
            VisualsConfigSaxLoader.parsePipe(new File(args[0]), false);
        } else {
            VisualsConfigSaxLoader.parsePipe(new File("/myprojects/project1/visuals"), false);
        }
    }
}

class VisualsConfigSaxLoader extends DefaultHandler {

    private File rootFolder;
    File configFile;
    StringWriter strImages = new StringWriter();
    StringWriter strChainedPipes = new StringWriter();
    boolean chained = false;

    public VisualsConfigSaxLoader(File configFile, File folder, boolean chained) {
        super();
        this.configFile = configFile;
        this.rootFolder = folder;
        this.chained = chained;
    }
    private String currentElement;
    static File storageDirectory;

    public static String parsePipe(File configFile, boolean chained) {
        return parse(configFile, configFile.getParentFile(), chained);
    }

    public static String parse(File configFile, File root, boolean chained) {
        if (configFile.isDirectory()) {
            File file = new File(configFile, "visualization-pipe.xml");
            String s = "";
            if (file.exists()) {
                if (!chained) {
                    storageDirectory = file.getParentFile();
                }
                s += parsePipe(file, chained);
            }

            for (File dir : configFile.listFiles()) {
                if (dir.isDirectory()) {
                    s += parsePipe(dir, chained);
                }
            }
            return "index.html";
        }
        if (!configFile.exists()) {
            SketchletPluginLogger.error("Visualization pipe configuration file '" + configFile.getPath() + "' does not exist.");
            return "";
        }
        VisualsConfigSaxLoader handler = new VisualsConfigSaxLoader(configFile, root, chained);
        try {
            XMLReader xr = XMLReaderFactory.createXMLReader();
            xr.setContentHandler(handler);
            xr.setErrorHandler(handler);

            StringReader r = new StringReader(FileUtils.getFileText(configFile));
            xr.parse(new InputSource(r));
            r.close();

            return handler.getHtmlFileName();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    public void startDocument() {
        path.clear();
        strImages = new StringWriter();
    }

    @Override
    public void endDocument() {
        strImages.flush();
        File dir = storageDirectory;
        String content = heading;
        content += "<h2>Generated Images</h2>";
        content += strImages.toString();
        if (!strChainedPipes.toString().isEmpty()) {
            content += "<h2>Chained Pipes</h2><ul>" + strChainedPipes.toString() + "</ul>" + footing;
        }
        FileUtils.saveFileText(new File(dir, getHtmlFileName()), content);
    }

    private String getHtmlFileName() {
        return "index" + (pipeName.isEmpty() ? "" : "-" + pipeName) + ".html";
    }
    private static String heading = "<html>"
            + "<head>"
            + "<style>body {font-family:Helvetica,Serif; font-size:10;} </style>"
            + "<link rel=\"STYLESHEET\" type=\"text/css\" href=\"http://www.w3.org/StyleSheets/Core/parser.css?family=3&doc=XML\">"
            + "</head>"
            + "<body>";
    private static String footing = "</body></html>";

    private File getFile(String strFile) {
        File file = new File(strFile);
        String name = file.getPath();
        name = name.replace("\\", "/").trim();
        boolean absolute = name.contains(":") || name.startsWith("/");
        System.out.println("GETFILE: " + strFile);
        System.out.println(absolute);
        System.out.println(file.exists());
        System.out.println(file.getParentFile());
        if (absolute && file.exists() && file.getParentFile() != null) {
            return file;
        } else {
            return new File(rootFolder, strFile);
        }
    }
    private String pipeName = "";

    @Override
    public void startElement(String uri, String name, String qName, Attributes atts) {
        String strElem = "";
        if ("".equals(uri)) {
            strElem = qName;
        } else {
            strElem = name;
        }
        path.add(strElem);

        if (path().equals("/visualization-pipe/")) {
            String value = process(atts.getValue("name"));
            if (value != null) {
                this.pipeName = value;
            }
        } else if (path().equals("/visualization-pipe/chain/")) {
            String src = process(atts.getValue("src"));
            if (src != null) {
                String strFile = parse(new File(src), rootFolder, true);
                strChainedPipes.append("<li><a href='" + strFile + "' target='_blank'>" + strFile + "</a></li>");
            }
        } else if (path().equals("/visualization-pipe/delete/")) {
            String path = process(atts.getValue("path"));
            if (path != null) {
                FileUtils.deleteDir(getFile(path));
            }
        } else if (path().equals("/visualization-pipe/java-parser/")) {
            String src = process(atts.getValue("src"));
            String output = process(atts.getValue("output"));
            if (src != null && output != null) {
                String strUml = new JavaUmlParser().parseFileXml(getFile(src));
                strUml = "<?xml version='1.0' encoding='UTF-8'?>\n<java>\n" + strUml + "</java>";
                getFile(output).getParentFile().mkdirs();
                FileUtils.saveFileText(getFile(output), strUml);
                SketchletPluginLogger.info("Executing Java Parser src='" + src + "' output='" + output + "'");
            } else {
                SketchletPluginLogger.error("Error executing Java Parser src='" + src + "' output='" + output + "'");
            }
        } else if (path().equals("/visualization-pipe/xslt/")) {
            String src = process(atts.getValue("src"));
            String transformation = process(atts.getValue("transformation"));
            String output = process(atts.getValue("output"));
            if (src != null && output != null && transformation != null) {
                String filter = process(atts.getValue("path-filter"));
                if (filter == null) {
                    filter = "*.xml";
                }
                SketchletPluginLogger.info("Executing XSLT src='" + src + "' output='" + output + "' transformation='" + transformation + "'");
                getFile(output).getParentFile().mkdirs();
                String xsltResult = XsltUtils.transform(getFile(src), getFile(transformation), filter);
                if (!xsltResult.trim().isEmpty()) {
                    FileUtils.saveFileText(getFile(output), xsltResult);
                } else {
                    getFile(output).delete();
                }
            } else {
                SketchletPluginLogger.error("Error executing XSLT src='" + src + "' output='" + output + "' transformation='" + transformation + "'");
            }
        } else if (path().equals("/visualization-pipe/image-generator/")) {
            String src = process(atts.getValue("src"));
            String output = process(atts.getValue("output"));
            String title = process(atts.getValue("title"));
            if (src != null && output != null) {
                generateImage(src, output, title, atts);
            } else {
                SketchletPluginLogger.error("Error executing image generator src='" + src + "' output='" + output);
            }
        } else if (path().equals("/visualization-pipe/append-file/")) {
            String src = process(atts.getValue("src"));
            String output = process(atts.getValue("output"));
            if (src != null && output != null && getFile(src).exists()) {
                SketchletPluginLogger.info("Appending file '" + src + "' to file '" + output + "'");
                getFile(output).getParentFile().mkdirs();
                try {
                    FileWriter fstream = new FileWriter(getFile(output), true);
                    BufferedWriter out = new BufferedWriter(fstream);
                    out.write(FileUtils.getFileText(getFile(src)));
                    out.close();
                } catch (Exception e) {//Catch exception if any
                    SketchletPluginLogger.error("Error", e);
                }
            }
        }
        currentElement = strElem;
        strCharacters = "";
    }

    private void generateImage(String src, String output, String title, Attributes atts) {
        String params = process(atts.getValue("params"));
        String strUmlGraph = new CascadingUmlUtil().getUmlGraphCode(FileUtils.getFileText(getFile(src)));
        if (!strUmlGraph.trim().isEmpty() && getFile(src).exists()) {
            File outFile = getFile(output);
            outFile.getParentFile().mkdirs();
            SketchletPluginLogger.info("Executing image generator src='" + src + "' output='" + output);
            UmlGraphUtil.generateImageFile(strUmlGraph, outFile.getParentFile(), outFile, params == null ? "" : params);

            strImages.append("<li><a href='" + output + "' target='_blank'>" + (title == null ? getFile(output).getName() : title) + "</a></li>\n");
        } else {
            getFile(output).delete();
        }
    }

    @Override
    public void endElement(String uri, String name, String qName) {
        this.processCharacters();
        currentElement = null;
        if (path.size() > 0) {
            path.remove(path.size() - 1);
        }
    }
    private String strCharacters = "";
    private String strFile = "";

    @Override
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
        if (path().equals("/visualization-pipe/append-text/")) {
            if (strFile != null) {
                SketchletPluginLogger.info("Appending text to file='" + strFile);
                getFile(strFile).getParentFile().mkdirs();
                try {
                    FileWriter fstream = new FileWriter(getFile(strFile), true);
                    BufferedWriter out = new BufferedWriter(fstream);
                    out.write(strCharacters);
                    out.close();
                } catch (Exception e) {//Catch exception if any
                    SketchletPluginLogger.error("Error", e);
                }

            } else {
                SketchletPluginLogger.info("Error appending text to file='" + strFile);
            }
        }

    }
    List<String> path = new ArrayList<String>();

    private String path() {
        String strPath = "/";
        for (String p : path) {
            strPath += p + "/";
        }

        return strPath;
    }

    private String process(String text) {
        String home = getCascadingUmlHome();
        if (home != null && !home.isEmpty() && text != null) {
            return text.replace("$cascading_uml_home", home).replace("$CASCADING_UML_HOME", home);
        }
        return text;
    }

    private String getCascadingUmlHome() {
        String strHome = System.getenv("CASCADING_UML_HOME");
        if (strHome == null || strHome.trim().isEmpty()) {
            URL url = CascadingUml.class.getProtectionDomain().getCodeSource().getLocation();
            File f;
            try {
                f = new File(url.toURI());
            } catch (Exception e) {
                f = new File(url.getPath());
            }
            strHome = f.getParentFile().getParent() + "/";
        }

        if (!strHome.endsWith("/") && !strHome.endsWith("\\")) {
            strHome += File.separator;
        }
        return strHome;
    }
}
