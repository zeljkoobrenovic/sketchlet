/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.tools.reporting;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.data.Pages;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.macros.Macro;
import net.sf.sketchlet.designer.programming.macros.Macros;
import net.sf.sketchlet.designer.ui.MessageFrame;
import net.sf.sketchlet.designer.ui.SketchletDesignerMainPanel;
import net.sf.sketchlet.designer.ui.toolbars.SketchToolbar;
import net.sf.sketchlet.script.ScriptPluginProxy;
import net.sf.sketchlet.util.XMLUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;

/**
 * @author zobrenovic
 */
public class ReportFrame extends JDialog {

    JCheckBox printStateDiagram = new JCheckBox("State diagram", true);
    JCheckBox printMacros = new JCheckBox("Action Lists", true);
    JCheckBox printSceenActions = new JCheckBox("Screen Actions", true);
    JCheckBox printScripts = new JCheckBox("Scripts", true);
    JCheckBox printTimers = new JCheckBox("Timers", true);
    JCheckBox printTOC = new JCheckBox("Generate TOC", true);
    JButton btnOK = new JButton("Create Report");
    JButton btnCancel = new JButton("Cancel");
    JButton selectAll = new JButton("Select All");
    JButton deselectAll = new JButton("Deselect All");
    JTextField title = new JTextField(20);
    int width = Toolkit.getDefaultToolkit().getScreenSize().width;
    int height = Toolkit.getDefaultToolkit().getScreenSize().height;
    JTextField imgWidth = new JTextField("" + width, 6);
    JTextField imgHeight = new JTextField("" + height, 6);
    JTextArea shortIntro = new JTextArea(5, 20);
    JProgressBar progress = new JProgressBar();
    JCheckBox showRegionsInfo = new JCheckBox("Show Active Regions Info", true);
    JCheckBox showVariables = new JCheckBox("Show Used Variables", true);
    Properties reportProps = new Properties();

    public ReportFrame() {
        this.setTitle("HTML Report");
        this.setIconImage(Workspace.createImageIcon("resources/report.gif").getImage());
        this.setModal(true);
        this.setLayout(new BorderLayout());
        this.setAlwaysOnTop(true);

        JPanel panelWest = new JPanel(new GridLayout(0, 1));
        panelWest.add(printStateDiagram);
        panelWest.add(printMacros);
        panelWest.add(printSceenActions);
        panelWest.add(printScripts);
        panelWest.add(printTimers);

        JPanel imgSizePanel = new JPanel();
        imgSizePanel.add(new JLabel("Image size: "));
        imgSizePanel.add(this.imgWidth);
        imgSizePanel.add(new JLabel(" x "));
        imgSizePanel.add(this.imgHeight);
        imgSizePanel.add(printTOC);

        JPanel selectPanel = new JPanel();
        selectPanel.add(deselectAll);
        selectPanel.add(selectAll);
        selectPanel.add(showRegionsInfo);
        selectPanel.add(showVariables);

        panelWest.setBorder(BorderFactory.createTitledBorder("Global Objects"));

        this.getRootPane().setDefaultButton(btnOK);

        JPanel panelWest2 = new JPanel(new BorderLayout());
        panelWest2.add(panelWest, BorderLayout.NORTH);
        add(panelWest2, BorderLayout.WEST);

        JPanel panelSouth = new JPanel(new BorderLayout());
        JPanel buttons = new JPanel();
        buttons.add(btnOK);
        buttons.add(btnCancel);
        panelSouth.add(buttons, BorderLayout.WEST);
        panelSouth.add(progress, BorderLayout.CENTER);

        JPanel panelNorth = new JPanel(new BorderLayout());
        panelNorth.setBorder(BorderFactory.createTitledBorder("Header"));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.add(new JLabel("Title: "), BorderLayout.WEST);
        titlePanel.add(title, BorderLayout.CENTER);
        panelNorth.add(titlePanel, BorderLayout.NORTH);

        add(panelNorth, BorderLayout.NORTH);

        title.setText(Workspace.mainPanel.projectTitle);
        shortIntro.setText(FileUtils.getFileText(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/report_abstract.txt"));

        try {
            FileInputStream in = new FileInputStream(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/report_properties.txt");
            reportProps.load(in);
            in.close();

            if (reportProps.containsKey("image-width")) {
                this.imgWidth.setText(reportProps.getProperty("image-width"));
            }
            if (reportProps.containsKey("image-height")) {
                this.imgHeight.setText(reportProps.getProperty("image-height"));
            }
            if (reportProps.containsKey("print-toc")) {
                this.printTOC.setSelected(reportProps.getProperty("print-toc").equalsIgnoreCase("true"));
            }
            if (reportProps.containsKey("print-states")) {
                this.printStateDiagram.setSelected(reportProps.getProperty("print-states").equalsIgnoreCase("true"));
            }
            if (reportProps.containsKey("print-macros")) {
                this.printMacros.setSelected(reportProps.getProperty("print-macros").equalsIgnoreCase("true"));
            }
            if (reportProps.containsKey("print-scripts")) {
                this.printScripts.setSelected(reportProps.getProperty("print-scripts").equalsIgnoreCase("true"));
            }
            if (reportProps.containsKey("print-timers")) {
                this.printTimers.setSelected(reportProps.getProperty("print-timers").equalsIgnoreCase("true"));
            }
            if (reportProps.containsKey("show-region-info")) {
                this.showRegionsInfo.setSelected(reportProps.getProperty("show-region-info").equalsIgnoreCase("true"));
            }
            if (reportProps.containsKey("show-variables")) {
                this.showVariables.setSelected(reportProps.getProperty("show-variables").equalsIgnoreCase("true"));
            }

        } catch (Exception e) {
        }

        JScrollPane scrollPane = new JScrollPane(shortIntro);
        panelNorth.add(scrollPane, BorderLayout.CENTER);
        panelNorth.add(imgSizePanel, BorderLayout.SOUTH);

        add(panelNorth, BorderLayout.NORTH);

        prepareSketchesTable();

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Sketchlets"));
        JPanel panelTable = new JPanel(new BorderLayout());
        panelTable.add(tableScroll, BorderLayout.CENTER);
        panelTable.add(selectPanel, BorderLayout.SOUTH);

        add(panelTable);

        btnOK.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                FileUtils.saveFileText(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/report_abstract.txt", shortIntro.getText());

                btnOK.setEnabled(false);
                btnCancel.setEnabled(false);
                new Thread(new Runnable() {

                    public void run() {
                        generateReport();
                        setVisible(false);
                    }
                }).start();
            }
        });
        btnCancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                setVisible(false);
            }
        });
        selectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                for (int i = 0; i < data.length; i++) {
                    data[i][0] = new Boolean(true);
                    model.fireTableDataChanged();
                }
            }
        });
        deselectAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                for (int i = 0; i < data.length; i++) {
                    data[i][0] = new Boolean(false);
                    model.fireTableDataChanged();
                }
            }
        });

        add(panelSouth, BorderLayout.SOUTH);

        pack();

        setVisible(true);
    }

    JTable table = new JTable();
    AbstractTableModel model;
    Object data[][];

    public void prepareSketchesTable() {
        data = new Object[SketchletEditor.pages.pages.size()][2];

        for (int i = 0; i < SketchletEditor.pages.pages.size(); i++) {
            Page s = SketchletEditor.pages.pages.elementAt(i);
            data[i][0] = new Boolean(true);
            data[i][1] = s.title;
        }

        model = new AbstractTableModel() {

            public String getColumnName(int col) {
                return "";
            }

            public int getRowCount() {
                return data.length;
            }

            public int getColumnCount() {
                return 2;
            }

            public Object getValueAt(int row, int col) {
                return data[row][col];
            }

            public boolean isCellEditable(int row, int col) {
                return true;
            }

            public void setValueAt(Object value, int row, int col) {
                data[row][col] = value;
            }

            public Class getColumnClass(int c) {
                if (c == 0) {
                    return Boolean.class;
                } else {
                    return String.class;
                }
            }
        };

        table.setTableHeader(null);
        table.setModel(model);
        TableColumn col = table.getColumnModel().getColumn(0);
        col.setPreferredWidth(50);
        col.setMaxWidth(50);
    }

    static JFileChooser fc = new JFileChooser();
    String strId = "";

    public void generateReport() {
        try {
            int imageCount = 0;
            File dir = new File(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/report");
            File file = new File(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/report/report.html");
            dir.mkdirs();
            fc.setCurrentDirectory(dir);
            fc.setSelectedFile(file);

            int returnVal = fc.showSaveDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                if (Pages.msgFrame == null) {
                    MessageFrame.showMessage(SketchletEditor.editorFrame, "Generating report...", SketchletEditor.editorFrame);
                }
                file = fc.getSelectedFile();
                strId = file.getName().replace(".html", "").replace(".htm", "") + "_files";

                File dirFiles = new File(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/report/" + strId);
                FileUtils.deleteDir(dirFiles);
                dirFiles.mkdirs();

                //FileUtils.copyFile(new File(WorkspaceUtils.getSketchletDesignerConfDir() + "reports/style.css"), new File(dirFiles, "style.css"));

                PrintWriter out = new PrintWriter(new FileWriter(file));

                out.println("<html>");
                out.println("<head>");
                out.println("<title>" + this.title.getText() + "</title>");
                //out.println("<link type='text/css' href='" + strId + "/style.css' rel='stylesheet'/>");
                out.println("</head>");
                out.println("<body>");
                out.println("<h1>" + this.title.getText() + "</h1>");
                out.println("<p>" + this.shortIntro.getText().trim() + "</p>");
                if (printTOC.isSelected()) {
                    out.println("<h2>Table of Content</h2>");
                    generateTOC(out);
                }

                if (this.printStateDiagram.isSelected()) {
                    out.println("<h2>Pages Transition State Diagram</h2>");
                    String strImage = "state_diagram.png";
                    File imgFile = new File(dirFiles, strImage);
                    this.createStateDiagram(imgFile);
                    out.println("<p><a name='state_diagram'></a><img border='1' src='" + strId + "/" + strImage + "' /></p>");
                }

                printSketches(out);

                if (this.printMacros.isSelected()) {
                    out.println("<h2><a name='macros'></a>Macros</h2>");
                    for (Macro m : Macros.globalMacros.macros) {
                        m.getHTMLCode(out, "h3");
                    }
                }
                if (this.printScripts.isSelected()) {
                    out.println("<h2><a name='scripts'></a>Scripts</h2>");
                    getScriptsHTMLCode(out);
                }
                if (this.printSceenActions.isSelected()) {
                    out.println("<h2><a name='screen_actions'></a>Screen Actions</h2>");
                }
                if (this.printTimers.isSelected()) {
                    out.println("<h2><a name='timers'></a>Timers</h2>");
                }

                out.println("</body>");
                out.println("</html>");

                out.flush();
                out.close();

                SketchletDesignerMainPanel.openFileInWebBrowser(file);

                if (Pages.msgFrame != null) {
                    MessageFrame.closeMessage();
                }

                reportProps.setProperty("image-width", this.width + "");
                reportProps.setProperty("image-height", this.height + "");
                reportProps.setProperty("print-toc", this.printTOC.isSelected() + "");
                reportProps.setProperty("print-states", this.printStateDiagram.isSelected() + "");
                reportProps.setProperty("print-macros", this.printMacros.isSelected() + "");
                reportProps.setProperty("print-scripts", this.printScripts.isSelected() + "");
                reportProps.setProperty("print-timers", this.printTimers.isSelected() + "");
                reportProps.setProperty("show-region-info", this.showRegionsInfo.isSelected() + "");
                reportProps.setProperty("show-variables", this.showVariables.isSelected() + "");

                FileOutputStream outProps = new FileOutputStream(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/report_properties.txt");
                reportProps.store(outProps, "---No Comment---");
                out.close();
            } else {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateTOC(PrintWriter out) {
        try {
            if (this.printStateDiagram.isSelected()) {
                out.println("<p><a href='#state_diagram'>Sketch State Diagram</a></p>");
            }
            if (this.printMacros.isSelected() || this.printScripts.isSelected() || this.printSceenActions.isSelected() || this.printTimers.isSelected()) {
                out.println("<p>Global Object</p>");
                out.println("<ul>");
                if (this.printMacros.isSelected()) {
                    out.println("<li><a href='#macros'>Macros</a></li>");
                }
                if (this.printScripts.isSelected()) {
                    out.println("<li><a href='#scripts'>Scripts</a></li>");
                }
                if (this.printSceenActions.isSelected()) {
                    out.println("<li><a href='#screen_actions'>Screen Actions</a></li>");
                }
                if (this.printTimers.isSelected()) {
                    out.println("<li><a href='#timers'>Timers</a></li>");
                }
                out.println("</ul>");
            }

            out.println("<p>Sketches</p>");
            for (int i = 0; i < data.length; i++) {
                out.println("<ul>");
                if (((Boolean) data[i][0]).booleanValue()) {
                    out.println("<li><a href='#sketch_" + (i + 1) + "'>" + data[i][1] + "</a></li>");
                }
                out.println("</ul>");
            }

            out.println("</ul>");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getImageXY() {
        try {
            width = (int) Double.parseDouble(imgWidth.getText());
            height = (int) Double.parseDouble(imgHeight.getText());
        } catch (Exception e) {
        }
    }

    public void createStateDiagram(File file) {
        try {
            getImageXY();
            BufferedImage img = Workspace.createCompatibleImage(width, height);
            Graphics2D g2 = img.createGraphics();
            SketchletDesignerMainPanel.desktopPanel.paintComponent(g2);

            g2.dispose();
            ImageIO.write(img, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createSketchImage(int index, File file) {
        try {
            getImageXY();
            BufferedImage img = Workspace.createCompatibleImage(width, height);
            Graphics2D g2 = img.createGraphics();


            if (SketchletEditor.editorPanel.tabsModes.getSelectedIndex() != 0) {
                SketchletEditor.editorPanel.tabsModes.setSelectedIndex(0);
            }
            SketchletEditor.editorPanel.openSketchByIndex(index);
            while (SketchletEditor.editorPanel.bLoading) {
                Thread.sleep(10);
            }

            boolean bPlayback = !this.showRegionsInfo.isSelected() && !this.showVariables.isSelected();
            if (!bPlayback) {
                g2.translate(-SketchletEditor.marginX * SketchletEditor.editorPanel.scale, -SketchletEditor.marginY * SketchletEditor.editorPanel.scale);
                SketchletEditor.editorPanel.paintComponent(g2);
            } else {
                if (SketchletEditor.editorPanel.tabsModes.getSelectedIndex() != 1) {
                    SketchletEditor.editorPanel.tabsModes.setSelectedIndex(1);
                }
                SketchletEditor.editorPanel.internalPlaybackPanel.paint(g2);
            }

            g2.dispose();
            ImageIO.write(img, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void printSketches(PrintWriter out) {

        Page _page = SketchletEditor.editorPanel.currentPage;
        int _nMode = SketchletEditor.editorPanel.tabsModes.getSelectedIndex();

        boolean bVisualizeInfoSketch = SketchToolbar.bVisualizeInfoSketch;
        boolean bVisualizeInfoRegions = SketchToolbar.bVisualizeInfoRegions;
        boolean bVisualizeInfoVariables = SketchToolbar.bVisualizeInfoVariables;
        boolean bVisualizeVariables = SketchToolbar.bVisualizeVariables;
        SketchToolbar.bVisualizeInfoSketch = false;
        SketchToolbar.bVisualizeInfoRegions = showRegionsInfo.isSelected();
        SketchToolbar.bVisualizeInfoVariables = false;
        SketchToolbar.bVisualizeVariables = showVariables.isSelected();

        int imgN = 0;
        int total = 0;
        for (int i = 0; i < this.data.length; i++) {
            boolean sel = ((Boolean) data[i][0]).booleanValue();
            if (sel) {
                total++;
            }
        }

        progress.setMinimum(0);
        progress.setMaximum(total - 1);

        for (int i = 0; i < this.data.length; i++) {
            boolean sel = ((Boolean) data[i][0]).booleanValue();
            if (sel) {
                progress.setValue(imgN);
                imgN++;
                File dirImages = new File(SketchletContextUtils.getCurrentProjectDir() + SketchletContextUtils.sketchletDataDir() + "/report/" + strId);
                String strImage = "sketch" + imgN + ".png";
                File imgFile = new File(dirImages, strImage);

                Page page = SketchletEditor.pages.pages.elementAt(i);

                out.println("<br />");
                out.println("<a  style='page-break-before:always' name='sketch_" + (i + 1) + "'></a><h3>" + page.title + "</h3>");

                this.createSketchImage(i, imgFile);
                out.println("<p><img border='1' src='" + strId + "/" + strImage + "' /></p>");
                page.onEntryMacro.name = "on entry";
                page.onEntryMacro.getHTMLCode(out, "b");
                page.onExitMacro.name = "on exit";
                page.onExitMacro.getHTMLCode(out, "b");
            }
        }
        SketchToolbar.bVisualizeInfoSketch = bVisualizeInfoSketch;
        SketchToolbar.bVisualizeInfoRegions = bVisualizeInfoRegions;
        SketchToolbar.bVisualizeInfoVariables = bVisualizeInfoVariables;
        SketchToolbar.bVisualizeVariables = bVisualizeVariables;

        SketchletEditor.editorPanel.tabsModes.setSelectedIndex(_nMode);
        SketchletEditor.editorPanel.openSketchByIndex(SketchletEditor.editorPanel.pages.pages.indexOf(_page));
    }

    public void getScriptsHTMLCode(PrintWriter out) {
        for (ScriptPluginProxy script : DataServer.variablesServer.scripts) {
            out.println("<h3>" + XMLUtils.prepareForXML(script.scriptFile.getName()) + "</h3>");
            out.println("<pre>");
            int n = 0;
            try {
                BufferedReader in = new BufferedReader(new FileReader(script.scriptFile));

                String line;

                while ((line = in.readLine()) != null) {
                    n++;
                    String strN = "00" + n;
                    strN = strN.substring(strN.length() - 2);
                    out.println(strN + "   " + line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            out.println("</pre>");
        }
    }
}
