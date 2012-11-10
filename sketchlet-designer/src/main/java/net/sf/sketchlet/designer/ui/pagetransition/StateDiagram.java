/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.ui.pagetransition;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.data.Pages;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import org.jgraph.JGraph;
import org.jgraph.event.GraphLayoutCacheEvent;
import org.jgraph.event.GraphLayoutCacheListener;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class StateDiagram {

    static StateDiagram diagram;
    static JFrame diagramFrame;

    public static void main(String[] args) {
        showDiagram(null);
    }

    public static Page selectedPage = null;
    public static DefaultGraphCell selectedCell = null;

    public static void hideDiagram() {
        if (diagramFrame != null && diagramFrame.isVisible()) {
            diagramFrame.setVisible(false);
        }
        diagramFrame = null;
    }

    static int posX = 0;
    static int posY = 0;
    static JCheckBox synchWithSketches;

    public static void showDiagram(final Pages pages) {
        JFrame oldFrame = diagramFrame;

        final BufferedImage image = getDotImage(pages);

        if (image == null) {
            createGraph(pages);
        }

        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // Construct Model and Graph
        diagramFrame = new JFrame();
        diagramFrame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                diagramFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                if (touchedSketches != null) {
                    for (Page s : touchedSketches.values()) {
                        s.save(false);
                    }
                }
                diagramFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
        diagramFrame.setIconImage(Workspace.createImageIcon("resources/states.png", "").getImage());
        //diagramFrame.setAlwaysOnTop(true);
        diagramFrame.setTitle("Page Transition Diagram");
        if (image == null) {
            diagramFrame.getContentPane().add(new JScrollPane(graph));
        } else {
            JPanel paneDot = new JPanel() {

                @Override
                public void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.setColor(Color.WHITE);
                    g.fillRect(0, 0, this.getWidth(), this.getHeight());
                    g.drawImage(image, 0, 0, null);
                }

                @Override
                public Dimension getPreferredSize() {
                    return getSize();
                }

                @Override
                public Dimension getSize() {
                    return new Dimension(image.getWidth(), image.getHeight());
                }
            };
            diagramFrame.getContentPane().add(new JScrollPane(paneDot));
        }
        diagramFrame.pack();
        if (posX > 0) {
            diagramFrame.setLocation(posX, posY);
        } else {
            diagramFrame.setLocationRelativeTo(SketchletEditor.editorFrame);
        }
        JButton refresh = new JButton("refresh", Workspace.createImageIcon("resources/view-refresh.png"));
        refresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                showDiagram(pages);
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(refresh);
        synchWithSketches = new JCheckBox("Synchronize with sketch window");
        panel.add(synchWithSketches);
        diagramFrame.getContentPane().add(panel, BorderLayout.NORTH);

        if (oldFrame != null && oldFrame.isVisible()) {
            posX = oldFrame.getLocation().x;
            posY = oldFrame.getLocation().y;
            oldFrame.setVisible(false);
        }

        int sw = Math.max(300, diagramFrame.getWidth());
        int sh = Math.max(300, diagramFrame.getHeight());

        diagramFrame.setSize(sw, sh);
        diagramFrame.setVisible(true);
        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public static DefaultGraphCell createVertex(String name, double x,
                                                double y, double w, double h, Color bg, boolean raised) {

        // Create vertex with the given name
        DefaultGraphCell cell = new DefaultGraphCell(name);

        // Set bounds
        GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(
                x, y, w, h));

        // Set fill color
        if (bg != null) {
            GraphConstants.setGradientColor(cell.getAttributes(), bg);
            GraphConstants.setOpaque(cell.getAttributes(), true);
        }

        // Set raised border
        if (raised) {
            GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createRaisedBevelBorder());
        } else // Set black border
        {
            GraphConstants.setBorderColor(cell.getAttributes(), Color.black);        // Add a Floating Port
        }
        cell.addPort();

        return cell;
    }

    static Hashtable<Page, Page> touchedSketches;
    static JGraph graph = null;

    public static String getDot(Pages pages) {
        String strDot = "digraph \"unix\" {\n";
        strDot += "graph [\n";
        strDot += "    splines=true,\n";
        strDot += "    overlap=true,\n";
        strDot += "    rankdir=LR\n";
        strDot += "]\n";
        strDot += "node [\n";
        strDot += "    shape = note,\n";
        strDot += "    fontsize = 11,\n";
        strDot += "    color = skyblue,\n";
        strDot += "    style = filled,\n";
        strDot += "    fontname = \"Helvetica-Outlined\" ];\n";
        strDot += "edge [\n";
        strDot += "    fontsize = 8,\n";
        strDot += "    fontname = \"Helvetica-Outlined\" ];\n";

        for (Page s : pages.pages) {
            strDot += "\"" + s.title + "\";\n";
        }

        for (int i = 0; i < pages.pages.size(); i++) {
            Page s1 = pages.pages.elementAt(i);

            for (int j = 0; j < i; j++) {
                Page s2 = pages.pages.elementAt(j);
                Set<String> connection1To2 = s1.getConnections(s2);
                Set<String> connection2To1 = s2.getConnections(s1);
                for (String conn : connection1To2) {
                    strDot += "\"" + s1.title + "\" -> \"" + s2.title + "\" [label=\"" + conn.toLowerCase() + "\"];\n";
                }
                for (String conn : connection2To1) {
                    strDot += "\"" + s2.title + "\" -> \"" + s1.title + "\" [label=\"" + conn.toLowerCase() + "\"];\n";
                }
            }
        }
        strDot += "}";
        return strDot;
    }

    public static JGraph createGraph(Pages pages) {
        touchedSketches = new Hashtable<Page, Page>();
        GraphModel model = new DefaultGraphModel();
        model.addGraphModelListener(new GraphModelListener() {

            public void graphChanged(GraphModelEvent gme) {
            }
        });

        graph = new JGraph(model);

        graph.getGraphLayoutCache().addGraphLayoutCacheListener(new GraphLayoutCacheListener() {

            public void graphLayoutCacheChanged(GraphLayoutCacheEvent e) {
            }
        });

        // Control-drag should clone selection
        graph.setCloneable(true);

        // Enable edit without final RETURN keystroke
        graph.setInvokesStopCellEditing(true);

        // When over a cell, jump to its default port (we only have one, anyway)
        graph.setJumpToDefaultPort(true);

        // Insert all three cells in one call, so we need an array to store them
        DefaultGraphCell[] cells = new DefaultGraphCell[pages.pages.size()];

        final Hashtable hash = new Hashtable();

        int c = 20;

        for (int i = 0; i < pages.pages.size(); i++) {
            Page s = pages.pages.elementAt(i);
            double x = s.stateDiagramX;
            double y = s.stateDiagramY;
        }

        Object[][] sketchInfo = Pages.getSketchInfoFromDir(50, 600);

        for (int i = 0; i < pages.pages.size(); i++) {
            Page s = pages.pages.elementAt(i);
            double x = s.stateDiagramX;
            double y = s.stateDiagramY;

            if (Double.isNaN(x) || x <= 0) {
                x = 20;
            }

            if (x == 20) {
                y = c;
                c += 110;
            }


            DefaultGraphCell cell = createVertex(s.title, x, y, 90, 80, Color.LIGHT_GRAY, false);
            cells[i] = cell;
            graph.getGraphLayoutCache().insert(cell);

            GraphConstants.setIcon(cell.getAttributes(), new ImageIcon((Image) sketchInfo[i][1]));

            s.stateDiagramGraph = graph;
            s.stateDiagramCell = cell;

            if (SketchletEditor.editorPanel.currentPage == s) {
                graph.setSelectionCell(cell);
                selectedPage = s;
            }

            hash.put(s.title, s);

            for (int j = 0; j < i; j++) {
                boolean connected1 = s.isConnectedTo(pages.pages.elementAt(j));
                boolean connected2 = pages.pages.elementAt(j).isConnectedTo(s);
                if (connected1 || connected2) {
                    DefaultEdge edge = new DefaultEdge();
                    // Fetch the ports from the new vertices, and connect them with the edge
                    edge.setSource(cells[i].getChildAt(0));
                    edge.setTarget(cells[j].getChildAt(0));
                    int arrow = GraphConstants.ARROW_CLASSIC;

                    if (connected1) {
                        GraphConstants.setLineEnd(edge.getAttributes(), arrow);
                        GraphConstants.setEndFill(edge.getAttributes(), true);
                    }
                    if (connected2) {
                        GraphConstants.setLineBegin(edge.getAttributes(), arrow);
                        GraphConstants.setBeginFill(edge.getAttributes(), true);
                    }

                    GraphConstants.setLineWidth(edge.getAttributes(), 1.0f);
                    GraphConstants.setLineStyle(edge.getAttributes(), GraphConstants.STYLE_SPLINE);
                    GraphConstants.setBendable(edge.getAttributes(), true);
                    GraphConstants.setRouting(edge.getAttributes(), GraphConstants.ROUTING_DEFAULT);

                    graph.getGraphLayoutCache().insert(edge);
                }
            }

            graph.addGraphSelectionListener(new GraphSelectionListener() {

                public void valueChanged(GraphSelectionEvent gse) {
                    DefaultGraphCell prevCell = selectedCell;

                    if (prevCell != null) {
                        Rectangle2D bounds = GraphConstants.getBounds(selectedCell.getAttributes());
                        if (selectedPage != null && bounds != null) {
                            selectedPage.stateDiagramX = bounds.getX();
                            selectedPage.stateDiagramY = bounds.getY();

                            touchedSketches.put(selectedPage, selectedPage);
                        }
                    }
                    selectedCell = (DefaultGraphCell) gse.getCell();
                    if (selectedCell == null) {
                        selectedPage = null;
                    } else {
                        selectedPage = (Page) hash.get(gse.getCell().toString());
                    }
                }
            });

            graph.addMouseListener(new MouseAdapter() {

                Page currentPage = null;

                public void mouseReleased(MouseEvent me) {
                    if (selectedPage != null && selectedCell != null) {
                        Rectangle2D bounds = GraphConstants.getBounds(selectedCell.getAttributes());
                        selectedPage.stateDiagramX = bounds.getX();
                        selectedPage.stateDiagramY = bounds.getY();

                        if (currentPage != selectedPage && synchWithSketches.isSelected()) {
                            diagramFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            SketchletEditor.editorPanel.selectSketch(selectedPage);
                            diagramFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            currentPage = selectedPage;

                        }
                    }
                }
            });

        }

        return graph;
    }

    static String dirGraphviz = SketchletContextUtils.getSketchletDesignerHome() + "bin/plugins/plugin-graphs/tools/graphviz/bin/";
    static String cmdDot = "dot";
    //

    public static BufferedImage getDotImage(Pages pages) {
        String strDot = getDot(pages);
        File dotFile = null;
        File imgFile = null;
        try {
            dotFile = File.createTempFile("umlgraph", ".dot");
            imgFile = File.createTempFile("umlgraph", ".png");
            FileUtils.saveFileText(dotFile, strDot);

            List<String> dotParams = new ArrayList<String>();

            dotParams.add(new File(dirGraphviz).getAbsolutePath() + File.separator + cmdDot);
            dotParams.add("-Tpng");
            dotParams.add("-o" + imgFile.getAbsolutePath());
            dotParams.add(dotFile.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(dotParams.toArray(new String[dotParams.size()]));
            processBuilder.directory(new File(dirGraphviz));
            processBuilder.redirectErrorStream(true);
            Process theProcess = processBuilder.start();
            theProcess.waitFor();

            if (imgFile.exists()) {
                return ImageIO.read(imgFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dotFile != null) {
                dotFile.delete();
            }
            if (imgFile != null) {
                imgFile.delete();
            }
        }

        return null;
    }
}