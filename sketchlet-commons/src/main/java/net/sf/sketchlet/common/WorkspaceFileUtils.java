/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author cuypers
 */
public class WorkspaceFileUtils {

    public static void createNewFiles(File dir, String fileName) {
        File oldFile = new File(dir.getAbsolutePath() + File.separator + fileName);
        File oldFile2 = new File(dir.getAbsolutePath() + File.separator + "run.bat");

        if (oldFile.exists()) {
            File amicoDir = new File(dir.getAbsolutePath() + File.separator + SketchletContextUtils.sketchletDataDir());
            amicoDir.mkdirs();

            try {
                PrintWriter out = new PrintWriter(new FileWriter(new File(amicoDir, "workspace.txt")));
                transform(oldFile, out);
                out.flush();
                out.close();

                oldFile.delete();
                oldFile2.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            File files[] = dir.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    createNewFiles(files[i], fileName);
                }
            }
        }
    }

    public static void transform(File configFile, PrintWriter out) {
        try {
            DocumentBuilderFactory factory;
            DocumentBuilder builder;
            TransformerFactory tFactory;
            Transformer transformers[];

            try {
                factory = DocumentBuilderFactory.newInstance();
                builder = factory.newDocumentBuilder();
                tFactory = TransformerFactory.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            Document docConfig = builder.parse(configFile);

            XPath xpath = XPathFactory.newInstance().newXPath();

            String expression;
            String str;
            double number;


            expression = "/process-runner/@title";
            String title = (String) xpath.evaluate(expression, docConfig, XPathConstants.STRING);

            out.println("Title " + title);
            String xmlRpcPort = (String) xpath.evaluate("/process-runner/@xmlrpc-server-port", docConfig, XPathConstants.STRING);
            if (!xmlRpcPort.trim().equals("")) {
                out.println("XMLRPCPort " + xmlRpcPort);
            }
            out.println();

            expression = "/process-runner/process";
            NodeList processes = (NodeList) xpath.evaluate(expression, docConfig, XPathConstants.NODESET);

            for (int i = 0; i < processes.getLength(); i++) {
                Node process = processes.item(i);

                String processTitle = (String) xpath.evaluate("@title", process, XPathConstants.STRING);
                String id = (String) xpath.evaluate("@id", process, XPathConstants.STRING);
                String workingDirectory = (String) xpath.evaluate("@working-directory", process, XPathConstants.STRING);

                String strAutoStart = (String) xpath.evaluate("@auto-start", process, XPathConstants.STRING);
                boolean autoStart = !(strAutoStart.equals("no") || strAutoStart.equals("false"));

                if (id.equals("")) {
                    id = "" + i;
                }

                String description = (String) xpath.evaluate("@description", process, XPathConstants.STRING);
                String command = (String) xpath.evaluate(".", process, XPathConstants.STRING);

                if (command.contains("amico.jar")) {
                    continue;
                }

                int offset = 0;
                String strOffset = (String) xpath.evaluate("@timeOffsetMs", process, XPathConstants.STRING);

                if (strOffset != null && !strOffset.equals("")) {
                    offset = Integer.parseInt(strOffset);
                }

                out.println("AddProcess " + id);
                out.println("ProcessTitle " + processTitle);
                out.println("Description " + description);
                out.println("WorkingDirectory " + workingDirectory);
                out.println("AutoStart " + autoStart);
                out.println("TimeOffsetMs " + offset);
                out.println("CommandLine " + command);
                out.println();
            }

        } catch (XPathExpressionException xpee) {
            xpee.printStackTrace();
        } catch (SAXException sxe) {
            sxe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
