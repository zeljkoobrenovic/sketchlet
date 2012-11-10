/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.help;

import net.sf.sketchlet.common.Refresh;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.install.Version;
import net.sf.sketchlet.designer.programming.screenscripts.AWTRobotUtil;
import net.sf.sketchlet.designer.ui.macros.MacroPanel;
import net.sf.sketchlet.designer.ui.variables.VariablesTableInterface;
import net.sf.sketchlet.designer.ui.variables.VariablesTablePanel;
import net.sf.sketchlet.util.XMLUtils;
import net.sf.sketchlet.util.ui.DataRowActionListener;
import net.sf.sketchlet.util.ui.DataRowFrame;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class TutorialPanel extends JPanel {
    private static final Logger log = Logger.getLogger(TutorialPanel.class);
    public JTextArea textArea = new JTextArea(4, 40);
    public static TutorialPanel panel;
    public JCheckBox recordTutorial = new JCheckBox("Record", false);
    public JButton capture = new JButton("Recapture Screen");
    public JButton edit = new JButton("Edit Image");
    public JToggleButton crop = new JToggleButton("Crop Image");
    JTextField title = new JTextField(15);
    JTextArea brief = new JTextArea(5, 40);
    JTable table;
    AbstractTableModel model;
    TutorialLine currentLine = null;
    ImagePanel imagePanel = new ImagePanel();
    File file = null;
    File imageFolder = null;
    JPanel panelNorth2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    boolean inCropMode = false;
    int cx1, cy1, cx2, cy2;

    public TutorialPanel() {
        panel = this;
        setLayout(new BorderLayout());
        JPanel panelNorth1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelNorth1.add(new JLabel("Title: "));
        panelNorth1.add(title);
        panelNorth1.add(getToolbar());
        panelNorth2.add(recordTutorial);
        panelNorth2.add(capture);
        panelNorth2.add(edit);
        panelNorth2.add(crop);
        add(panelNorth1, BorderLayout.NORTH);

        JScrollPane scrlPane = new JScrollPane(brief);
        scrlPane.setBorder(BorderFactory.createTitledBorder("Tutorial Abstract"));
        add(scrlPane, BorderLayout.SOUTH);

        model = new AbstractTableModel() {

            public String getColumnName(int col) {
                return col == 1 ? "Description" : "";
            }

            public int getRowCount() {
                return lines.size();
            }

            public int getColumnCount() {
                return 2;
            }

            public Object getValueAt(int row, int col) {
                if (row >= 0) {
                    TutorialLine line = lines.elementAt(row);
                    if (col == 0 && line.icon != null) {
                        return "" + (row + 1);//line.icon;
                    } else if (col == 1) {
                        return line.strDescription;
                    }
                }
                return "";
            }

            public boolean isCellEditable(int row, int col) {
                return col >= 1;
            }

            public void setValueAt(Object value, int row, int col) {
                if (row >= 0) {
                    if (col == 1) {
                        TutorialLine line = lines.elementAt(row);
                        line.strDescription = value.toString();
                    }
                }
            }

            public Class getColumnClass(int c) {
                /*
                 * if (c == 0) { return ImageIcon.class;
                }
                 */
                return String.class;
            }
        };

        table = new JTable(model);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(25);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.setRowHeight(25);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        DataRowFrame.dataRowActionListener = new DataRowActionListener() {

            public void onOK(DataRowFrame frame) {
                TutorialPanel.addLine("cmd", "Enter data and press OK", "arrow_cursor.png", frame);
            }
        };

        crop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                inCropMode = crop.isSelected();
            }
        });

        capture.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                try {
                    panel.currentLine.currentImage = AWTRobotUtil.robot.createScreenCapture(SketchletEditor.editorFrame.getBounds());
                    panel.imagePanel.revalidate();
                    panel.imagePanel.repaint();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        });
        edit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (currentLine.currentImage != null) {
                    currentLine.saveImage();
                    SketchletContextUtils.editImages("\"" + currentLine.strImageFile + "\"", new Refresh() {

                        public void refreshImage(int index) {
                            refresh();
                        }

                        public void refresh() {
                            panel.imagePanel.revalidate();
                            panel.imagePanel.repaint();
                        }
                    }, 0);
                }
            }
        });

        ListSelectionListener listener = new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                enableControls();
                if (panel.currentLine != null) {
                    panel.currentLine.saveImage();
                }
                int r = table.getSelectedRow();
                if (r >= 0 && panel.lines != null && panel.lines.size() > r) {
                    currentLine = lines.elementAt(r);
                    if (panel.currentLine != null && panel.currentLine.strImageFile != null && !panel.currentLine.strImageFile.isEmpty()) {
                        try {
                            File file = new File(panel.currentLine.strImageFile);
                            panel.currentLine.currentImage = ImageIO.read(file);
                            panel.imagePanel.revalidate();
                            panel.imagePanel.repaint();

                            panel.textArea.setText(currentLine.strMemo);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            }
        };

        table.getSelectionModel().addListSelectionListener(listener);

        enableControls();

        VariablesTablePanel.variablesTableInterface = new VariablesTableInterface() {

            public void variableTableUpdate(String var, String value, Component c) {
                panel.addLine("cmd", "Update the value of variable " + var, c);
            }

            public void variableDialogAdded(String var, String value, Component c) {
                panel.addLine("cmd", "Set the value of the new variable " + var + " and press OK", c);
            }
        };
    }

    JButton duplicate = new JButton(MacroPanel.iconDuplicate);
    JButton up = new JButton(MacroPanel.iconMoveUp);
    JButton down = new JButton(MacroPanel.iconMoveDown);
    JButton delete = new JButton(MacroPanel.iconDelete);
    JButton insertAfter = new JButton(MacroPanel.iconInsertAfter);
    JButton insertBefore = new JButton(MacroPanel.iconInsertBefore);

    public void enableControls() {
        int row = table.getSelectedRow();
        int size = table.getRowCount();

        duplicate.setEnabled(row >= 0);
        insertAfter.setEnabled(row >= 0);
        insertBefore.setEnabled(row > 0);
        up.setEnabled(row > 0);
        down.setEnabled(row >= 0);
        delete.setEnabled(row >= 0);
    }

    public JToolBar getToolbar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        delete.setToolTipText("delete line");
        duplicate.setToolTipText("duplicate");
        insertAfter.setToolTipText("insert after");
        insertBefore.setToolTipText("insert before");
        up.setToolTipText("move up");
        down.setToolTipText("move down");

        toolbar.add(duplicate);
        toolbar.add(insertBefore);
        toolbar.add(insertAfter);
        toolbar.add(up);
        toolbar.add(down);
        toolbar.add(delete);
        up.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = panel.table.getSelectedRow();
                if (row > 0 && row < panel.lines.size()) {
                    TutorialLine line = panel.lines.elementAt(row);
                    panel.lines.removeElementAt(row);
                    panel.lines.insertElementAt(line, row - 1);
                    panel.model.fireTableRowsInserted(row - 1, row - 1);
                    panel.table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
                }
            }
        });
        down.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = panel.table.getSelectedRow();
                if (row >= 0 && row < panel.lines.size() - 1) {
                    TutorialLine line = panel.lines.elementAt(row);
                    panel.lines.removeElementAt(row);
                    panel.lines.insertElementAt(line, row + 1);
                    panel.model.fireTableRowsInserted(row + 1, row + 1);
                    panel.table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
                }
            }
        });
        duplicate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = panel.table.getSelectedRow();
                if (row >= 0 && row < panel.lines.size()) {
                    TutorialLine line = panel.lines.elementAt(row);
                    TutorialLine newLine = new TutorialLine("", "");
                    newLine.strDescription = line.strDescription;
                    newLine.strMemo = line.strMemo;
                    newLine.strShape = line.strShape;
                    newLine.p1 = new Point(line.p1);
                    newLine.p2 = new Point(line.p2);
                    panel.lines.removeElementAt(row);
                    if (row < panel.lines.size() - 1) {
                        panel.lines.insertElementAt(newLine, row + 1);
                    } else {
                        panel.lines.add(newLine);
                    }
                    panel.model.fireTableRowsInserted(row + 1, row + 1);
                    panel.table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
                }
            }
        });
        insertAfter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = panel.table.getSelectedRow();
                if (row >= 0 && row < panel.lines.size()) {
                    TutorialLine newLine = new TutorialLine("", "");

                    if (row == panel.lines.size() - 1) {
                        panel.lines.add(newLine);
                        panel.model.fireTableRowsInserted(row + 1, row + 1);
                    } else {
                        panel.lines.insertElementAt(newLine, row + 1);
                        panel.model.fireTableRowsInserted(row + 1, row + 1);
                    }

                    panel.table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
                }
            }
        });
        insertBefore.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = panel.table.getSelectedRow();
                if (row >= 0 && row < panel.lines.size()) {
                    TutorialLine newLine = new TutorialLine("", "");
                    panel.lines.insertElementAt(newLine, row);
                    panel.model.fireTableRowsInserted(row, row);
                    panel.table.getSelectionModel().setSelectionInterval(row, row);
                }
            }
        });
        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = panel.table.getSelectedRow();
                if (row >= 0 && row < panel.lines.size()) {
                    panel.lines.removeElementAt(row);
                    panel.model.fireTableRowsDeleted(row, row);
                }
                panel.table.getSelectionModel().setSelectionInterval(row, row);
            }
        });

        return toolbar;
    }

    static JFrame frame = new JFrame();

    public static void open() {
        if (panel == null) {
            panel = new TutorialPanel();
            if (frame == null) {
                frame = new JFrame();
            }
            frame.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    VariablesTablePanel.variablesTableInterface = null;
                    panel = null;
                    frame = null;
                }
            });
            frame.setLayout(new BorderLayout());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            JMenuBar menubar = new JMenuBar();
            frame.setJMenuBar(menubar);
            JMenu menuFile = new JMenu("Tutorial");
            menubar.add(menuFile);
            JMenuItem newItem = new JMenuItem("Create a New Tutorial", Workspace.createImageIcon("resources/add.gif"));
            final JMenuItem openItem = new JMenuItem("Open a Tutorial...", Workspace.createImageIcon("resources/open.gif"));
            final JMenuItem saveItem = new JMenuItem("Save", Workspace.createImageIcon("resources/save.gif"));
            JMenuItem saveAsItem = new JMenuItem("Save As...", Workspace.createImageIcon("resources/saveas.gif"));
            JMenuItem exportHTMLItem = new JMenuItem("Export As an HTML Page...", Workspace.createImageIcon("resources/export.gif"));
            JMenuItem exportHTMLSlidesItem = new JMenuItem("Export As HTML Slides...", Workspace.createImageIcon("resources/export.gif"));

            menuFile.add(newItem);
            menuFile.addSeparator();
            menuFile.add(openItem);
            menuFile.addSeparator();
            menuFile.add(saveItem);
            menuFile.add(saveAsItem);
            menuFile.addSeparator();
            // menuFile.add(exportHTMLItem);
            menuFile.add(exportHTMLSlidesItem);
            newItem.addActionListener(new TutorialActionAdapter() {

                public void actionPerformed(ActionEvent e) {
                    panel.lines.removeAllElements();
                    panel.file = null;
                    frame.setTitle("New Tutorial");
                    if (panel.currentLine != null) {
                        panel.currentLine.currentImage = null;
                    }
                    panel.currentLine = null;
                    panel.recordTutorial.setSelected(true);
                    panel.model.fireTableDataChanged();
                }
            });
            openItem.addActionListener(new TutorialActionAdapter() {

                public void actionPerformed(ActionEvent e) {
                    panel.openTutorial();
                    panel.recordTutorial.setSelected(false);
                }
            });
            saveItem.addActionListener(new TutorialActionAdapter() {

                public void actionPerformed(ActionEvent e) {
                    panel.save();
                }
            });
            saveAsItem.addActionListener(new TutorialActionAdapter() {

                public void actionPerformed(ActionEvent e) {
                    panel.saveAs();
                }
            });
            exportHTMLItem.addActionListener(new TutorialActionAdapter() {

                public void actionPerformed(ActionEvent e) {
                    panel.saveAsHTML();
                }
            });

            exportHTMLSlidesItem.addActionListener(new TutorialActionAdapter() {

                public void actionPerformed(ActionEvent e) {
                    panel.saveAsHTMLSlides();
                }
            });

            frame.add(panel, BorderLayout.WEST);

            JPanel panelImage = new JPanel(new BorderLayout());
            panelImage.add(panel.panelNorth2, BorderLayout.NORTH);

            panelImage.add(new JScrollPane(panel.imagePanel));
            panelImage.add(new JScrollPane(panel.textArea), BorderLayout.SOUTH);


            frame.add(panelImage, BorderLayout.CENTER);
            // frame.setSize(800, 600);
            frame.pack();
        }
        frame.setVisible(true);
    }

    static JFileChooser fileChooser;
    static JFileChooser fileChooserProject;

    public static JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(SketchletContextUtils.getSketchletDesignerTutorialsDir()));
        }
        return fileChooser;
    }

    public static JFileChooser getProjectFileChooser() {
        if (fileChooserProject == null) {
            fileChooserProject = new JFileChooser();
            if (GlobalProperties.get("archive-root") != null) {
                fileChooserProject.setCurrentDirectory(new File(GlobalProperties.get("archive-root")));
            }
            fileChooserProject.setDialogTitle("Select the project archive file");
        }
        return fileChooserProject;
    }

    public void newTutorial() {
        file = null;
        imageFolder = null;
    }

    public void save() {
        if (file == null) {
            new File(SketchletContextUtils.getSketchletDesignerTutorialsDir()).mkdirs();
            getFileChooser().setSelectedFile(new File(SketchletContextUtils.getSketchletDesignerTutorialsDir() + "tutorial.xml"));
            int n = getFileChooser().showSaveDialog(panel);
            if (n == JFileChooser.APPROVE_OPTION) {
                panel.file = getFileChooser().getSelectedFile();
                frame.setTitle("Tutorial: " + panel.file.getName());
            }
        }
        if (file != null) {
            save(file);
        }
    }

    public void openTutorial() {
        getFileChooser().setSelectedFile(new File(SketchletContextUtils.getSketchletDesignerTutorialsDir() + "tutorial.xml"));
        int n = getFileChooser().showOpenDialog(panel);
        if (n == JFileChooser.APPROVE_OPTION) {
            panel.file = getFileChooser().getSelectedFile();
            TutorialSaxLoader.getTutorial(panel.file.getPath(), panel);
            frame.setTitle("Tutorial: " + panel.file.getName());
        }
    }

    public void saveAs() {
        getFileChooser().setSelectedFile(new File(SketchletContextUtils.getSketchletDesignerTutorialsDir() + "tutorial.xml"));
        int n = getFileChooser().showSaveDialog(panel);
        if (n == JFileChooser.APPROVE_OPTION) {
            panel.file = getFileChooser().getSelectedFile();
            frame.setTitle("Tutorial: " + panel.file.getName());
        }
        if (file != null) {
            save(file);
        }
    }

    public void saveAsHTML() {
        new File(SketchletContextUtils.getSketchletDesignerHTMLTutorialsDir()).mkdirs();
        getFileChooser().setSelectedFile(new File(SketchletContextUtils.getSketchletDesignerHTMLTutorialsDir() + "tutorial.html"));
        int n = getFileChooser().showSaveDialog(panel);
        if (n == JFileChooser.APPROVE_OPTION) {
            File file = getFileChooser().getSelectedFile();
            saveAsHTML(file);
        }
    }

    public void saveAsHTMLSlides() {
        new File(SketchletContextUtils.getSketchletDesignerHTMLTutorialsDir()).mkdirs();
        getFileChooser().setSelectedFile(new File(SketchletContextUtils.getSketchletDesignerHTMLTutorialsDir() + "tutorial.html"));
        int n = getFileChooser().showSaveDialog(panel);
        if (n == JFileChooser.APPROVE_OPTION) {
            File file = getFileChooser().getSelectedFile();
            saveAsHTMLSlides(file);
        }
    }

    public void save(File file) {
        try {
            file.getParentFile().mkdirs();
            imageFolder = new File(file.getParentFile(), file.getName().replace(".xml", "") + "_files");

            FileUtils.deleteDir(imageFolder);
            imageFolder.mkdirs();

            PrintWriter out = new PrintWriter(new FileWriter(file));

            out.println("<?xml version='1.0' encoding='UTF-8'?>");
            out.println("<tutorial>");

            for (int i = 0; i < lines.size(); i++) {
                TutorialLine line = lines.elementAt(i);
                line.saveImage();
                out.println("<slide>");
                out.println("<slide-title>" + XMLUtils.prepareForXML(line.strDescription) + "</slide-title>");
                File imageFile = new File(imageFolder, "image" + (i + 1) + ".png");
                FileUtils.copyFile(new File(line.strImageFile), imageFile);
                out.println("<slide-image>" + XMLUtils.prepareForXML(imageFile.getName()) + "</slide-image>");
                out.println("<slide-memo>" + XMLUtils.prepareForXML(line.strMemo) + "</slide-memo>");
                if (line.p1 != null && line.p2 != null) {
                    out.println("<slide-shape x1='" + line.p1.x + "' y1='" + line.p1.y + "' x2='" + line.p2.x + "' y2='" + line.p2.y + "'>" + XMLUtils.prepareForXML(line.strShape) + "</slide-shape>");
                }
                out.println("</slide>");
            }
            out.println("</tutorial>");

            out.flush();
            out.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void saveAsHTML(File file) {
        try {
            file.getParentFile().mkdirs();
            imageFolder = new File(file.getParentFile(), file.getName().replace(".html", "") + "_files");

            FileUtils.deleteDir(imageFolder);
            imageFolder.mkdirs();

            PrintWriter out = new PrintWriter(new FileWriter(file));

            out.println("<html>");
            out.println("<head>");
            out.println("<title>" + "</title>");
            out.println("<link rel=\"STYLESHEET\" type=\"text/css\" href=\"http://www.w3.org/StyleSheets/Core/parser.css?family=6&amp;doc=XML\">");
            out.println("</head>");
            out.println("<body>");

            out.println("<h1>Tutorial</h1>");
            out.println("<h2>Table of Content</h2>");
            out.println("<ol>");
            for (int i = 0; i < lines.size(); i++) {
                TutorialLine line = lines.elementAt(i);
                line.saveImage();
                out.println("<li><a href='#line_" + (i + 1) + "'>" + line.strDescription + "</a></li>");
            }
            out.println("</ol>");

            for (int i = 0; i < lines.size(); i++) {
                out.println("<a name='line_" + (i + 1) + "' />");
                TutorialLine line = lines.elementAt(i);
                out.println("<h2>" + line.strDescription + "</h2>");
                File imageFile = new File(imageFolder, "image" + (i + 1) + ".png");
                writeImage(line, new File(line.strImageFile), imageFile);
                // FileUtils.copyFile(new File(line.strImageFile), imageFile);
                out.println("<img src='" + imageFolder.getName() + "/" + "image" + (i + 1) + ".png'" + " />");
                out.println("<p>" + line.strMemo + "</p>");
            }
            out.println("</body>");
            out.println("</html>");

            out.flush();
            out.close();

            SketchletContextUtils.openWebBrowser(file.getAbsolutePath());
        } catch (Exception e) {
            log.error(e);
        }
    }

    public void saveAsHTMLSlides(File file) {
        try {
            file.getParentFile().mkdirs();
            String subFolder = file.getName().replace(".html", "") + "_files";
            imageFolder = new File(file.getParentFile(), subFolder);

            FileUtils.deleteDir(imageFolder);
            imageFolder.mkdirs();

            PrintWriter out = new PrintWriter(new FileWriter(file));

            out.println("<html>");
            out.println("<head>");
            out.println("<title>Sketchlet Tutorial</title>");
            // out.println("<link rel=\"STYLESHEET\" type=\"text/css\" href=\"http://www.w3.org/StyleSheets/Core/parser.css?family=6&amp;doc=XML\">");
            printStyle(out);

            out.println("</head>");
            out.println("<body>");

            out.println("<p>Sketchlet Tutorial</p>");
            out.println("<h2>" + panel.title.getText() + "</h2>");
            out.println("<p><i>" + panel.brief.getText() + "</i></p>");


            int n = panel.getProjectFileChooser().showOpenDialog(panel);
            if (n == JFileChooser.APPROVE_OPTION) {
                File projectFile = getProjectFileChooser().getSelectedFile();

                FileUtils.copyFile(projectFile, new File(imageFolder, projectFile.getName()));

                out.println("<h3>The Sketchlet Project</h3>");
                out.println("<ol>");
                out.println("<li><a href='" + subFolder + "/" + projectFile.getName() + "'>The Project Archive File (for import from URL...)</a></li>");
                out.println("</ol>");
            }


            out.println("<h3>Table of Content</h3>");
            out.println("<ol>");
            for (int i = 0; i < lines.size(); i++) {
                TutorialLine line = lines.elementAt(i);
                currentLine.saveImage();
                out.println("<li><a href='" + subFolder + "/slide_" + (i + 1) + ".html'>" + line.strDescription + "</a></li>");
            }
            out.println("</ol>");
            out.println("<p>");
            out.println("Created by: " + Version.getVersion() + "<br />");
            out.println("</p>");
            out.println("</body>");
            out.println("</html>");

            out.flush();
            out.close();

            for (int i = 0; i < lines.size(); i++) {
                File f = new File(imageFolder, "slide_" + (i + 1) + ".html");
                out = new PrintWriter(new FileWriter(f));

                out.println("<html>");
                out.println("<head>");
                out.println("<title>" + "</title>");
                // out.println("<link rel=\"STYLESHEET\" type=\"text/css\" href=\"http://www.w3.org/StyleSheets/Core/parser.css?family=6&amp;doc=XML\">");
                printStyle(out);
                out.println("</head>");
                out.println("<body>");

                TutorialLine line = lines.elementAt(i);
                if (i == 0) {
                    out.println("<p> Previous&nbsp;&nbsp;&nbsp;<a href='slide_" + (i + 2) + ".html'>Next </a>&nbsp;&nbsp;&nbsp;&nbsp;<a href='../" + file.getName() + "'>TOC</a></p>");
                } else if (i == lines.size() - 1) {
                    out.println("<p><a href='slide_" + (i) + ".html'> Previous</a>&nbsp;&nbsp;&nbsp;Next &nbsp;&nbsp;&nbsp;&nbsp;<a href='../" + file.getName() + "'>TOC</a></p>");
                } else {
                    out.println("<p><a href='slide_" + (i) + ".html'> Previous</a>&nbsp;&nbsp;&nbsp;<a href='slide_" + (i + 2) + ".html'>Next </a>&nbsp;&nbsp;&nbsp;&nbsp;<a href='../" + file.getName() + "'>TOC</a></p>");
                }
                out.println("<h2>" + line.strDescription + "</h2>");
                out.println("<p>" + line.strMemo + "</p>");
                File imageFile = new File(imageFolder, "image" + (i + 1) + ".png");
                writeImage(line, new File(line.strImageFile), imageFile);
                // FileUtils.copyFile(new File(line.strImageFile), imageFile);
                out.println("<img src='image" + (i + 1) + ".png'" + " />");
                out.println("<p>" + line.strMemo + "</p>");
                out.println("</body>");
                out.println("</html>");

                out.flush();
                out.close();
            }
            SketchletContextUtils.openWebBrowser(file.getAbsolutePath());
        } catch (Exception e) {
            log.error(e);
        }
    }

    public static void printStyle(PrintWriter out) {
        out.println("<style>");

        out.println("body {");
        out.println("	font:76%/1.7 Verdana, Geneva, Arial, Helvetica, sans-serif;");
        out.println("}");
        out.println("h1,h2,h3 {");
        out.println("	font:normal 1.7em arial;");
        out.println("	color:#8b2;");
        out.println("	clear:left;");
        out.println("}");
        out.println("h1 {");
        out.println("	font-size:3em;");
        out.println("	padding-top:0.5em;");
        out.println("	color:#1B6FC0;");
        out.println("}");
        out.println("h3 { font-size:1.2em }");

        out.println("</style>");
    }

    public synchronized void writeImage(TutorialLine line, File imgFile, File newFile) {
        try {
            BufferedImage img = ImageIO.read(imgFile);
            if (img != null) {
                Graphics2D g2 = img.createGraphics();
                imagePanel.drawAnnotation(g2, line, false);
                g2.dispose();
                ImageIO.write(img, "png", newFile);
            }
        } catch (Exception e) {
            log.error(imgFile.getPath(), e);
        }
    }

    public static void addLine(String strAction, String strParams) {
        addLine(strAction, strParams, null, "", null, null);
    }

    public static void prepare(final Container container) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                for (Component component : container.getComponents()) {
                    if (component instanceof JButton) {
                        prepare((JButton) component);
                    } else if (component instanceof JTabbedPane) {
                        prepare((JTabbedPane) component);
                    } else if (component instanceof JTextField) {
                        prepare((JTextField) component);
                    } else if (component instanceof JTextArea) {
                        prepare((JTextArea) component);
                    } else if (component instanceof JCheckBox) {
                        prepare((JCheckBox) component);
                    } else if (component instanceof JRadioButton) {
                        prepare((JRadioButton) component);
                    } else if (component instanceof JToggleButton) {
                        prepare((JToggleButton) component);
                    } else if (component instanceof JSlider) {
                        prepare((JSlider) component);
                    } else if (component instanceof JComboBox) {
                        prepare((JComboBox) component);
                    } else if (component instanceof JComponent) {
                        prepare((Container) component);
                    }
                }
            }
        });
    }

    public static void addLine(String strAction, String strParams, Component c) {
        if (c.isShowing()) {
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());
            addLine(strAction, strParams, null, "rect", p1, p2);
        }
    }

    public static void addLine(JButton btn, String strIcon) {
        addLine(btn, strIcon, 1000);
    }

    public static void addLine(JButton btn, String strIcon, int delay) {
        if (btn.isShowing()) {
            Component c = btn;
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());
            String strText = "Click on a button";
            if (btn.getToolTipText() != null) {
                strText = "Click " + btn.getToolTipText();
            } else if (btn.getText() != null) {
                strText = "Click " + btn.getText();
            }
            addLine("cmd", strText, strIcon, "rect", p1, p2, delay);
        }
    }

    public static void addLine(JToggleButton btn, String strIcon) {
        addLine(btn, strIcon, 0);
    }

    public static void addLine(JToggleButton btn, String strIcon, long delay) {
        if (btn.isShowing()) {
            Component c = btn;
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());

            String strText = "Select " + (btn.getToolTipText() != null ? btn.getToolTipText() : btn.getText());

            addLine("cmd", strText + " (" + btn.isSelected() + ")", strIcon, "rect", p1, p2, delay);
        }
    }

    public static void addLine(JComboBox combo, long delay) {
        if (combo.isShowing()) {
            Component c = combo;
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());

            String strText = "Select " + combo.getSelectedItem();

            addLine("cmd", strText, "combo.png", "rect", p1, p2, delay);
        }
    }

    public static void addLine(JTabbedPane tabs) {
        if (tabs.isShowing()) {
            Component c = tabs;
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());

            int index = tabs.getSelectedIndex();

            if (index >= 0) {
                String strText = tabs.getTitleAt(index);
                if (strText == null || strText.isEmpty()) {
                    strText = tabs.getToolTipTextAt(index);
                }

                strText = "Select tab " + strText;

                addLine("cmd", strText, "tab.png", "rect", p1, p2, 1000);
            }
        }
    }

    public static void addLine(JSlider slider) {
        if (slider.isShowing()) {
            Component c = slider;
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());

            int index = slider.getValue();

            String strText = "Change slider position";
            if (slider.getToolTipText() != null && !slider.getToolTipText().isEmpty()) {
                strText = "Change " + slider.getToolTipText().toLowerCase();
            }

            addLine("cmd", strText, "slider.png", "rect", p1, p2, 1000);
        }
    }

    public static void prepare(final JSlider slider) {
        if (slider != null) {
            removeListeners(slider);
            slider.addMouseListener(new TutorialMouseAdapter() {

                public void mousePressed(MouseEvent e) {
                    mouseX = e.getXOnScreen();
                    mouseY = e.getYOnScreen();

                    addLine(slider);
                }
            });
        }
    }

    static int mouseX = 0;
    static int mouseY = 0;

    public static void prepare(final JTabbedPane tabs) {
        for (int i = 0; i < tabs.getTabCount(); i++) {
            writeTerm(tabs.getTitleAt(i));
        }
        if (tabs != null) {
            removeListeners(tabs);
            tabs.addMouseListener(new TutorialMouseAdapter() {

                public void mouseReleased(MouseEvent e) {
                    mouseX = e.getXOnScreen();
                    mouseY = e.getYOnScreen();

                    addLine(tabs);
                }
            });
            for (int i = 0; i < tabs.getTabCount(); i++) {
                Component c = tabs.getTabComponentAt(i);
                if (c != null) {
                    c.addMouseListener(new TutorialMouseAdapter() {

                        public void mouseReleased(MouseEvent e) {
                            mouseX = e.getXOnScreen();
                            mouseY = e.getYOnScreen();
                            addLine(tabs);
                        }
                    });
                }
            }
            prepare((Container) tabs);
        }
    }

    public static void prepare(final JTable table) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            writeTerm(table.getColumnName(i));
        }
        if (table != null) {
            removeListeners(table);

            table.addMouseListener(new TutorialMouseAdapter() {

                public void mouseClicked(MouseEvent e) {
                    mouseX = e.getXOnScreen();
                    mouseY = e.getYOnScreen();
                    Component c = table;
                    Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
                    Point l = c.getLocationOnScreen();
                    int x = l.x - lfh.x;
                    int y = l.y - lfh.y;
                    Point p1 = new Point(x, y);
                    Point p2 = new Point(x + c.getWidth(), y + c.getHeight());

                    if (e.getClickCount() >= 2) {
                        addLine("cmd", "Double-click on the table", null, "rect", p1, p2);
                    }
                }
            });
            table.getModel().addTableModelListener(new TutorialTableModelAdapter() {

                public void tableChanged(TableModelEvent e) {
                    addLine(table);
                }
            });
        }
    }

    public static Properties terms = new Properties();

    public static void writeTerm(String strText) {
        /*
         * if (strText != null) { terms.setProperty(strText, strText);
        }
         */
    }

    public static void saveTerms() {
        try {
            TutorialPanel.terms.storeToXML(new FileOutputStream("c:\\temp\\lang.xml"), "Language");
            PrintWriter pw = new PrintWriter(new FileWriter("c:\\temp\\lang.txt"));
            for (String strTerm : terms.stringPropertyNames()) {
                pw.println(strTerm);
            }
            pw.flush();
            pw.close();
        } catch (Exception ex) {
        }
    }

    public static void prepare(final JTextComponent field) {
        if (field != null) {
            KeyListener ls[] = field.getKeyListeners();

            for (int i = 0; i < ls.length; i++) {
                if (ls[i] instanceof TutorialKeyAdapter) {
                    field.removeKeyListener(ls[i]);
                }
            }

            field.addKeyListener(new TutorialKeyAdapter() {

                public void keyReleased(KeyEvent e) {
                    addLine("cmd", "Type text", "text-field.gif", field);
                }
            });
        }
    }

    public static void prepare(final JEditorPane editor) {
        if (editor != null) {
            KeyListener ls[] = editor.getKeyListeners();

            for (int i = 0; i < ls.length; i++) {
                if (ls[i] instanceof TutorialKeyAdapter) {
                    editor.removeKeyListener(ls[i]);
                }
            }

            editor.addKeyListener(new TutorialKeyAdapter() {

                public void keyReleased(KeyEvent e) {
                    addLine("cmd", "Type text", "text-field.gif", editor);
                }
            });
        }
    }

    public static void prepare(final JButton btn, final String strIcon) {
        writeTerm(btn.getText());
        writeTerm(btn.getToolTipText());
        if (btn != null) {
            removeListeners(btn);
            btn.addActionListener(new TutorialActionAdapter() {

                public void actionPerformed(ActionEvent e) {
                    addLine(btn, strIcon);
                }
            });
        }
    }

    public static void prepare(final JButton btn) {
        prepare(btn, "button.png");
    }

    public static void prepare(final JRadioButton btn) {
        if (btn != null) {
            writeTerm(btn.getText());
            writeTerm(btn.getToolTipText());
            removeListeners(btn);
            btn.addActionListener(new TutorialActionAdapter() {

                public void actionPerformed(ActionEvent e) {
                    addLine(btn, "radio.png", 1000);
                }
            });
        }
    }

    public static void prepare(final JComboBox combo) {
        if (combo != null) {
            writeTerm(combo.getToolTipText());
            removeListeners(combo);
            combo.addPopupMenuListener(new TutorialPopupMenuAdapter() {

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    addLine(combo, 1000);
                }
            });

            combo.getEditor().getEditorComponent().addKeyListener(new TutorialKeyAdapter() {

                public void keyReleased(KeyEvent e) {
                    addLine("cmd", "Type text in the combo box", "text-field.gif", combo);
                }
            });
        }
    }

    public static void prepare(final JPopupMenu menu) {
        prepare(menu, false);
    }

    public static void prepare(final JPopupMenu menu, final boolean rightClick) {
        if (menu != null) {
            removeListeners(menu);
            menu.addPopupMenuListener(new TutorialPopupMenuAdapter() {

                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    addLine(menu, rightClick);
                }
            });
        }
    }

    public static void prepare(final JCheckBox btn) {
        if (btn != null) {
            writeTerm(btn.getText());
            writeTerm(btn.getToolTipText());
            removeListeners(btn);
            btn.addActionListener(new TutorialActionAdapter() {

                public void actionPerformed(ActionEvent e) {
                    addLine(btn, "check.png", 1000);
                }
            });
        }
    }

    public static void prepare(final JToggleButton btn) {
        if (btn != null) {
            writeTerm(btn.getText());
            writeTerm(btn.getToolTipText());
            removeListeners(btn);
            btn.addActionListener(new TutorialActionAdapter() {

                public void actionPerformed(ActionEvent e) {
                    addLine(btn, "check.png", 1000);
                }
            });
        }
    }

    public static void removeListeners(JSlider slider) {
        MouseListener listeners[] = slider.getMouseListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof TutorialMouseAdapter) {
                slider.removeMouseListener(listeners[i]);
            }
        }
    }

    public static void removeListeners(JTabbedPane tabs) {
        MouseListener listeners[] = tabs.getMouseListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof TutorialMouseAdapter) {
                tabs.removeMouseListener(listeners[i]);
            }
        }
        for (int i = 0; i < tabs.getTabCount(); i++) {
            Component c = tabs.getTabComponentAt(i);
            if (c != null) {
                MouseListener listenersM[] = c.getMouseListeners();
                for (int j = 0; j < listenersM.length; j++) {
                    if (listenersM[j] instanceof TutorialMouseAdapter) {
                        c.removeMouseListener(listenersM[j]);
                    }
                }
            }
        }
    }

    public static void removeListeners(JButton btn) {
        ActionListener listeners[] = btn.getActionListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof TutorialActionAdapter) {
                btn.removeActionListener(listeners[i]);
            }
        }
    }

    public static void removeListeners(JComboBox combo) {
        ActionListener listeners[] = combo.getActionListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof TutorialActionAdapter) {
                combo.removeActionListener(listeners[i]);
            }
        }
        PopupMenuListener listenersp[] = combo.getPopupMenuListeners();
        for (int i = 0; i < listenersp.length; i++) {
            if (listenersp[i] instanceof TutorialPopupMenuAdapter) {
                combo.removePopupMenuListener(listenersp[i]);
            }
        }
        if (combo.getEditor() != null) {
            KeyListener listenersk[] = combo.getEditor().getEditorComponent().getKeyListeners();
            for (int i = 0; i < listenersk.length; i++) {
                if (listenersk[i] instanceof TutorialKeyAdapter) {
                    combo.getEditor().getEditorComponent().removeKeyListener(listenersk[i]);
                }
            }
        }
    }

    public static void removeListeners(JTable table) {
        MouseListener listeners[] = table.getMouseListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof TutorialMouseAdapter) {
                table.removeMouseListener(listeners[i]);
            }
        }
        if (table.getModel() instanceof AbstractTableModel) {
            TableModelListener listenersp[] = ((AbstractTableModel) table.getModel()).getTableModelListeners();
            for (int i = 0; i < listenersp.length; i++) {
                if (listenersp[i] instanceof TutorialTableModelAdapter) {
                    table.getModel().removeTableModelListener(listenersp[i]);
                }
            }
        }
    }

    public static void removeListeners(JCheckBox btn) {
        ActionListener listeners[] = btn.getActionListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof TutorialActionAdapter) {
                btn.removeActionListener(listeners[i]);
            }
        }
    }

    public static void removeListeners(JRadioButton btn) {
        ActionListener listeners[] = btn.getActionListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof TutorialActionAdapter) {
                btn.removeActionListener(listeners[i]);
            }
        }
    }

    public static void removeListeners(JToggleButton btn) {
        ActionListener listeners[] = btn.getActionListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof TutorialActionAdapter) {
                btn.removeActionListener(listeners[i]);
            }
        }
    }

    public static void removeListeners(JMenu menu) {
        ActionListener listeners[] = menu.getActionListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof TutorialActionAdapter) {
                menu.removeActionListener(listeners[i]);
            }
        }
        MouseListener mlisteners[] = menu.getMouseListeners();
        for (int i = 0; i < mlisteners.length; i++) {
            if (mlisteners[i] instanceof TutorialMouseAdapter) {
                menu.removeMouseListener(mlisteners[i]);
            }
        }
    }

    public static void removeListeners(JPopupMenu menu) {
        PopupMenuListener mlisteners[] = menu.getPopupMenuListeners();
        for (int i = 0; i < mlisteners.length; i++) {
            if (mlisteners[i] instanceof TutorialPopupMenuAdapter) {
                menu.removePopupMenuListener(mlisteners[i]);
            }
        }
    }

    public static void addLine(JMenu menu) {
        if (panel == null || !panel.recordTutorial.isSelected()) {
            return;
        }
        if (menu.isShowing() && SketchletEditor.editorFrame != null) {
            Component c = menu;
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());

            String strText = "Open the menu";
            addLine("cmd", strText, "menu.png", "rect", p1, p2);
        }
    }

    public static void addLine(JTable table) {
        if (panel == null || !panel.recordTutorial.isSelected()) {
            return;
        }
        if (table.isShowing() && SketchletEditor.editorFrame != null) {
            Component c = table.getParent();
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());

            String strText = "Change the data in the table";
            addLine("cmd", strText, "menu.png", "rect", p1, p2);
        }
    }

    public static void addLine(JPopupMenu menu) {
        addLine(menu, false);
    }

    public static void addLine(JPopupMenu menu, final boolean rightClick) {
        if (panel == null || !panel.recordTutorial.isSelected()) {
            return;
        }
        if (menu.isShowing() && SketchletEditor.editorFrame != null) {
            Component c = menu;
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());

            String strText = rightClick ? "Click with the right mouse button and select the menu item" : "Select the menu item";
            addLine("cmd", strText, "menu.png", "rect", p1, p2);
        }
    }

    public static void addLine(JMenuBar menu) {
        if (panel == null || !panel.recordTutorial.isSelected()) {
            return;
        }
        if (menu.isShowing() && SketchletEditor.editorFrame != null) {
            Component c = menu;
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());

            String strText = "Open the menu";
            addLine("cmd", strText, "menu.png", "rect", p1, p2, 1000);
        }
    }

    public static void addLine(JTable table, JScrollPane scrollPane, String strText) {
        if (panel == null || !panel.recordTutorial.isSelected()) {
            return;
        }
        if (table.isShowing()) {
            Component c = scrollPane != null ? scrollPane : table;
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());

            addLine("cmd", strText, "table.png", "rect", p1, p2, 1000);
        }
    }

    public static void addLine(String strAction, String strParams, String strIcon, Component c) {
        if (panel == null || !panel.recordTutorial.isSelected()) {
            return;
        }
        addLine(strAction, strParams, strIcon, c, 0);
    }

    public static void addLine(String strAction, String strParams, String strIcon, Component c, long delay) {
        if (panel == null || !panel.recordTutorial.isSelected()) {
            return;
        }
        if (c != null && c.isShowing()) {
            Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
            Point l = c.getLocationOnScreen();
            int x = l.x - lfh.x;
            int y = l.y - lfh.y;
            Point p1 = new Point(x, y);
            Point p2 = new Point(x + c.getWidth(), y + c.getHeight());
            addLine(strAction, strParams, strIcon, "rect", p1, p2, delay);
            /*
             * } else { addLine(strAction, strParams, strIcon, "", null, null);
             */
        }
    }

    public static void addLine(String strAction, String strParams, String strIcon, JTable table, Point targetPt) {
        if (panel == null || !panel.recordTutorial.isSelected()) {
            return;
        }
        if (table != null && table.isShowing()) {
            int row = table.getSelectedRow();
            int col = table.getSelectedColumn();
            if (col < 0) {
                col = 0;
            }
            if (row >= 0) {
                Point lfh = SketchletEditor.editorFrame.getLocationOnScreen();
                Point l = table.getLocationOnScreen();

                Rectangle r = table.getCellRect(row, col, true);

                int x1 = l.x - lfh.x + r.x + 5;// src.getWidth() / 2;
                int y1 = l.y - lfh.y + r.y + (int) (r.getHeight() / 2);// src.getHeight() / 2;
                int x2 = targetPt.x - lfh.x;
                int y2 = targetPt.y - lfh.y;
                Point p1 = new Point(x1, y1);
                Point p2 = new Point(x2, y2);
                addLine(strAction, strParams, strIcon, "arrow", p1, p2);
            }
            /*
             * } else { addLine(strAction, strParams, strIcon, "", null, null);
             */
        }
    }

    public static void addLine(String strAction, String strParams, String strIcon, Rectangle r) {
        if (panel == null || !panel.recordTutorial.isSelected()) {
            return;
        }
        if (r != null) {
            Point p1 = new Point(r.x, r.y);
            Point p2 = new Point(r.x + r.width, r.y + r.height);
            addLine(strAction, strParams, strIcon, "rect", p1, p2);
        }
    }

    public static void addLine(String strAction, String strParams, String strIcon, String strShape, Point p1, Point p2) {
        if (panel == null || !panel.recordTutorial.isSelected()) {
            return;
        }
        addLine(strAction, strParams, strIcon, strShape, p1, p2, 0);
    }

    public static void addLine(String strAction, String strParams, String strIcon, String strShape, Point p1, Point p2, final long delay) {
        if (panel == null || !panel.recordTutorial.isSelected()) {
            return;
        }
        final Point p = SketchletEditor.editorFrame.getMousePosition(true);
        final Cursor c = SketchletEditor.editorFrame.getCursor();
        if (panel != null && panel.recordTutorial.isSelected()) {// && SketchletEditor.editorPanel.tabsModes.getSelectedIndex() == 0) {
            String strLine = "";

            if (strAction.equalsIgnoreCase("setRegionMouseEvent")) {
                strLine = "Set region mouse event (" + strParams + ")";
            } else if (strAction.equalsIgnoreCase("setRegionContinousMouseEvent")) {
                strLine = "Set region motion event (" + strParams + ")";
            } else if (strAction.equalsIgnoreCase("setRegionContinousMouseEvent")) {
                strLine = "Set region move & rotate event (" + strParams + ")";
            } else if (strAction.equalsIgnoreCase("setRegionOverlapEvent")) {
                strLine = "Set region overlap & touch event (" + strParams + ")";
            } else if (strAction.equalsIgnoreCase("setRegionPropertiesAnimation")) {
                strLine = "Set region parameters animation (" + strParams + ")";
            } else if (strAction.equalsIgnoreCase("selectTabRegionImage")) {
                strLine = "Select the region image tab: " + strParams + "";
                /*
                 * } else if
                 * (strAction.equalsIgnoreCase("selectTabRegionTransform")) {
                 * strLine = "Go To Region Transformations Tab '" + strParams + "'";
                 */
            } else if (strAction.equalsIgnoreCase("selectTabRegionSettings")) {
                strLine = "Select the region tab: " + strParams + "";
            } else if (strAction.equalsIgnoreCase("setTool")) {
                strLine = "Select tool: " + strParams.toLowerCase() + "";
            } else if (strAction.equalsIgnoreCase("toolResult")) {
                strLine = strParams;
            } else if (strAction.equalsIgnoreCase("cmd")) {
                strLine = strParams;
            }

            if (strLine == null) {
                strLine = "";
            }

            String lastLine = panel.lines.size() > 0 ? panel.lines.lastElement().strDescription : "";

            if (!lastLine.equals(strLine) && !strLine.isEmpty()) {
                if (panel.currentLine != null) {
                    panel.currentLine.saveImage();
                }
                TutorialLine line = new TutorialLine(strLine, strIcon);
                line.p1 = p1;
                line.p2 = p2;
                line.strShape = strShape;
                panel.currentLine = line;
                panel.currentLine.currentImage = null;
                panel.lines.add(line);

                panel.model.fireTableDataChanged();

                panel.table.getSelectionModel().setSelectionInterval(panel.table.getRowCount() - 1, panel.table.getRowCount() - 1);
            }

            if (delay == 0) {
                panel.currentLine.currentImage = AWTRobotUtil.robot.createScreenCapture(SketchletEditor.editorFrame.getBounds());
            }
            /*
             * new Thread(new Runnable() {
             *
             * public void run() { try {
             */
            try {
                if (delay > 0) {
                    Thread.sleep(delay);
                    panel.currentLine.currentImage = AWTRobotUtil.robot.createScreenCapture(SketchletEditor.editorFrame.getBounds());
                    if (p != null) {
                        Graphics2D g2 = panel.currentLine.currentImage.createGraphics();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(255, 0, 0, 66));
                        g2.setStroke(new BasicStroke(2));
                        g2.drawOval(p.x - 8, p.y - 8, 17, 17);
                        g2.drawOval(p.x - 4, p.y - 4, 9, 9);
                        g2.drawImage(Workspace.createImageIcon("resources/arrow_cursor.png").getImage(), p.x - 5, p.y - 1, null);
                        g2.dispose();
                    }
                }
                /*
                 * File file = File.createTempFile("screen", ".png");
                 * file.deleteOnExit();
                 * ImageIO.write(panel.currentLine.currentImage, "PNG", file);
                 * panel.currentLine.strImageFile = file.getPath();
                 */
            } catch (Exception e) {
                log.error(e);
            }

            panel.imagePanel.revalidate();
            panel.imagePanel.repaint();
            /*
             * } catch (Exception e) { log.error(e); } }
            }).start();
             */
        } else {
            panel.currentLine.currentImage = null;
        }
    }

    Vector<TutorialLine> lines = new Vector<TutorialLine>();

    static class TutorialLine {

        public String strDescription = "";
        public String strIcon = null;
        public String strScreen = null;
        public String strShape = "";
        public String strImageFile = "";
        public String strMemo = "";
        public Point p1 = null;
        public Point p2 = null;
        public ImageIcon icon;
        BufferedImage currentImage = null;

        public TutorialLine(String strDescription, String strIcon) {
            this.strDescription = strDescription;
            this.strIcon = strIcon;

            if (strIcon != null) {
                icon = Workspace.createImageIcon("resources/" + strIcon);
            }
        }

        public int getSelectedRegion(int x, int y) {
            if (p1 != null && p2 != null) {
                int _x = (int) Math.min(p1.getX(), p2.getX()) - 3;
                int _y = (int) Math.min(p1.getY(), p2.getY()) - 3;
                int _w = (int) Math.abs(p1.getX() - p2.getX()) + 6;
                int _h = (int) Math.abs(p1.getY() - p2.getY()) + 6;
                int x1 = (int) p1.getX();
                int y1 = (int) p1.getY();
                int x2 = (int) p2.getX();
                int y2 = (int) p2.getY();
                if (strShape.equalsIgnoreCase("rect")) {
                    if (new Rectangle(_x - 2, _y - 2, 5, 5).contains(x, y)) {
                        return 1;
                    } else if (new Rectangle(_x + _w - 2, _y - 2, 5, 5).contains(x, y)) {
                        return 2;
                    } else if (new Rectangle(_x + _w - 2, _y + _h - 2, 5, 5).contains(x, y)) {
                        return 3;
                    } else if (new Rectangle(_x - 2, _y + _h - 2, 5, 5).contains(x, y)) {
                        return 4;
                    } else if (new Rectangle(_x, _y, _w, _h).contains(x, y)) {
                        return 0;
                    }
                } else if (strShape.equalsIgnoreCase("arrow")) {
                    if (new Rectangle(x1 - 2, y1 - 2, 5, 5).contains(x, y)) {
                        return 1;
                    } else if (new Rectangle(x2 - 2, y2 - 2, 5, 5).contains(x, y)) {
                        return 3;
                    } else if (new Rectangle(_x, _y, _w, _h).contains(x, y)) {
                        return 0;
                    }
                }
            }

            return -1;
        }

        public synchronized void saveImage() {
            if (panel.currentLine == null || panel.currentLine.currentImage == null) {
                return;
            }
            final TutorialLine _line = panel.currentLine;
            final BufferedImage img = panel.currentLine.currentImage;
            panel.currentLine.currentImage = null;

            _line.strMemo = panel.textArea.getText();

            /*
             * new Thread(new Runnable() {
             *
             * public void run() {
             */
            try {
                if (img != null && (_line.strImageFile == null || _line.strImageFile.isEmpty())) {
                    File file = File.createTempFile("screen", ".png");
                    _line.strImageFile = file.getPath();
                    ImageIO.write(img, "PNG", file);
                    file.deleteOnExit();
                }
            } catch (Exception e) {
                log.error(e);
            }
            /*
             * }
             * }).start();
             */
        }
    }

    class ImagePanel extends JPanel implements MouseListener, MouseMotionListener {

        public ImagePanel() {
            addMouseListener(this);
            addMouseMotionListener(this);

        }

        public int selectedRegion = -1;

        public void mouseClicked(MouseEvent e) {
        }

        int startX;
        int startY;

        public void mousePressed(MouseEvent e) {
            if (inCropMode) {
                cx1 = e.getX();
                cy1 = e.getY();
            } else {
                selectedRegion = -1;
                if (currentLine != null && currentLine.p1 != null && currentLine.p2 != null) {
                    selectedRegion = currentLine.getSelectedRegion(e.getX(), e.getY());
                    startX = e.getX();
                    startY = e.getY();
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
            selectedRegion = -1;
            if (inCropMode && currentLine.currentImage != null) {
                inCropMode = false;
                crop.setSelected(false);
                try {
                    int x = Math.min(cx1, cx2);
                    int y = Math.min(cy1, cy2);
                    int w = Math.abs(cx2 - cx1);
                    int h = Math.abs(cy2 - cy1);

                    x = Math.max(0, x);
                    y = Math.max(0, y);
                    w = Math.min(w, currentLine.currentImage.getWidth() - x - 1);
                    h = Math.min(h, currentLine.currentImage.getHeight() - y - 1);

                    currentLine.currentImage = currentLine.currentImage.getSubimage(x, y, w, h);
                    currentLine.strImageFile = "";

                    int pw = currentLine.p2.x - currentLine.p1.x;
                    int ph = currentLine.p2.y - currentLine.p1.y;

                    currentLine.p1.x -= x;
                    currentLine.p1.y -= y;

                    currentLine.p2.x = currentLine.p1.x + pw;
                    currentLine.p2.y = currentLine.p1.y + ph;
                    currentLine.saveImage();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                revalidate();
                repaint();
            }
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseDragged(MouseEvent e) {
            if (inCropMode) {
                cx2 = e.getX();
                cy2 = e.getY();
            } else {
                int dx = e.getX() - startX;
                int dy = e.getY() - startY;
                switch (selectedRegion) {
                    case 0:
                        currentLine.p1.x += dx;
                        currentLine.p1.y += dy;
                        currentLine.p2.x += dx;
                        currentLine.p2.y += dy;
                        break;
                    case 1:
                        currentLine.p1.x += dx;
                        currentLine.p1.y += dy;
                        break;
                    case 2:
                        currentLine.p2.x += dx;
                        currentLine.p1.y += dy;
                        break;
                    case 3:
                        currentLine.p2.x += dx;
                        currentLine.p2.y += dy;
                        break;
                    case 4:
                        currentLine.p1.x += dx;
                        currentLine.p2.y += dy;
                        break;
                }
                startX = e.getX();
                startY = e.getY();
            }
            imagePanel.repaint();
        }

        public void mouseMoved(MouseEvent e) {
        }

        public void paintComponent(Graphics g) {
            if (currentLine != null) {
                Graphics2D g2 = (Graphics2D) g;
                draw(g2, currentLine.currentImage, currentLine);
            }
        }

        public void draw(Graphics2D g2, BufferedImage currentImage, TutorialLine currentLine) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = this.getWidth();
            int h = this.getHeight();
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, w, h);
            if (currentImage != null) {
                g2.drawImage(currentImage, 0, 0, null);
            }
            drawAnnotation(g2, currentLine, true);

            if (inCropMode) {
                g2.setColor(new Color(0, 0, 255, 55));
                g2.fillRect(Math.min(cx1, cx2), Math.min(cy1, cy2), Math.abs(cx2 - cx1), Math.abs(cy2 - cy1));
            }
        }

        public void drawAnnotation(Graphics2D g2, TutorialLine currentLine, boolean bResizeCorners) {
            if (currentLine != null && currentLine.p1 != null && currentLine.p2 != null && currentLine.strShape != null) {
                float dash1[] = {10.0f};
                g2.setStroke(new BasicStroke(5.0f,
                        BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER,
                        10.0f, dash1, 0.0f));

                int _x = (int) Math.min(currentLine.p1.getX(), currentLine.p2.getX()) - 3;
                int _y = (int) Math.min(currentLine.p1.getY(), currentLine.p2.getY()) - 3;
                int _w = (int) Math.abs(currentLine.p1.getX() - currentLine.p2.getX()) + 6;
                int _h = (int) Math.abs(currentLine.p1.getY() - currentLine.p2.getY()) + 6;
                int x1 = (int) currentLine.p1.getX();
                int y1 = (int) currentLine.p1.getY();
                int x2 = (int) currentLine.p2.getX();
                int y2 = (int) currentLine.p2.getY();
                g2.setFont(g2.getFont().deriveFont(14.0f));
                String str = currentLine.strDescription;
                int fw = getTextWidth(g2, str);
                int fh = getTextHeight(g2, str);
                if (currentLine.strShape.isEmpty() || currentLine.strShape.equalsIgnoreCase("rect")) {
                    g2.setColor(Color.YELLOW);
                    g2.fillRect(_x - 5, _y - fh - 4, fw + 10, fh + 5);
                    g2.setColor(Color.BLACK);
                    g2.drawString(str, _x, _y - fh / 2 + 3);
                    g2.setColor(Color.RED);
                    g2.drawRect(_x, _y, _w, _h);

                    if (bResizeCorners) {
                        g2.fillRect(x1 - 2, y1 - 2, 5, 5);
                        g2.fillRect(x2 - 2, y1 - 2, 5, 5);
                        g2.fillRect(x1 - 2, y2 - 2, 5, 5);
                        g2.fillRect(x2 - 2, y2 - 2, 5, 5);
                    }
                } else if (currentLine.strShape.equalsIgnoreCase("arrow")) {
                    g2.setColor(Color.RED);
                    paintArrow(g2, x1, y1, x2, y2);
                    g2.setColor(Color.YELLOW);
                    g2.fillRect(_x + _w / 2 - fw / 2, _y + _h / 2 - fh - 4, fw, fh + 4);
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(5.0f));
                    g2.drawString(str, _x + _w / 2 - fw / 2, _y + _h / 2 - fh / 2);
                    if (bResizeCorners) {
                        g2.fillRect(x1 - 2, y1 - 2, 5, 5);
                        g2.fillRect(x2 - 2, y2 - 2, 5, 5);
                    }
                }
            }
        }

        public Dimension getPreferredSize() {
            if (currentLine != null && currentLine.currentImage != null) {
                return new Dimension(currentLine.currentImage.getWidth(), currentLine.currentImage.getHeight());
            } else {
                return new Dimension(1, 1);
            }
        }
    }

    private void paintArrow(Graphics2D g, int x0, int y0, int x1, int y1) {
        int deltaX = x1 - x0;
        int deltaY = y1 - y0;
        double frac = 0.05;

        g.setStroke(new BasicStroke(3.0f));
        g.drawLine(x0, y0, x1, y1);
        g.drawLine(x0 + (int) ((1 - frac) * deltaX + frac * deltaY),
                y0 + (int) ((1 - frac) * deltaY - frac * deltaX), x1, y1);
        g.drawLine(x0 + (int) ((1 - frac) * deltaX - frac * deltaY),
                y0 + (int) ((1 - frac) * deltaY + frac * deltaX), x1, y1);

    }

    public int getTextWidth(Graphics2D g2, String text) {
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();
        LineMetrics metrics = font.getLineMetrics(text, frc);
        return (int) font.getStringBounds(text, frc).getMaxX();
    }

    public int getTextHeight(Graphics2D g2, String text) {
        FontRenderContext frc = g2.getFontRenderContext();
        Font font = g2.getFont();
        LineMetrics metrics = font.getLineMetrics(text, frc);
        return (int) metrics.getHeight();
    }
}
