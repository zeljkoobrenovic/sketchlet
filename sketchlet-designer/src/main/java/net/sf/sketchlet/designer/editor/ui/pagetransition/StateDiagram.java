package net.sf.sketchlet.designer.editor.ui.pagetransition;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.framework.model.Pages;
import org.apache.log4j.Logger;
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
    private static final Logger log = Logger.getLogger(StateDiagram.class);

    private static JFrame diagramFrame;

    private static Page selectedPage = null;
    private static DefaultGraphCell selectedCell = null;

    private static int posX = 0;
    private static int posY = 0;

    private static String graphvizPath = SketchletContextUtils.getSketchletDesignerHome() + "bin/plugins/plugin-graphs/tools/graphviz/bin/dot";
    private static final String GRAPHVIZ_DOT_SYSTEM_VARIABLE = "GRAPHVIZ_DOT";

    static {
        if (System.getenv(GRAPHVIZ_DOT_SYSTEM_VARIABLE) != null) {
            graphvizPath = System.getenv(GRAPHVIZ_DOT_SYSTEM_VARIABLE);
        }
    }

    public static void hideDiagram() {
        if (diagramFrame != null && diagramFrame.isVisible()) {
            diagramFrame.setVisible(false);
        }
        diagramFrame = null;
    }

    public static void showDiagram(final Pages pages) {
        JFrame oldFrame = diagramFrame;

        final BufferedImage image = getDotImage(pages);

        if (image == null) {
            createGraph(pages);
        }

        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        diagramFrame = new JFrame();
        diagramFrame.setIconImage(Workspace.createImageIcon("resources/states.png", "").getImage());
        diagramFrame.setTitle("Page Transition Diagram");
        JPanel paneDot = null;
        JScrollPane scrollPane = null;

        if (image == null) {
            diagramFrame.getContentPane().add(new JScrollPane(graph));
        } else {
            paneDot = new StateDiagramPanel(image);
            scrollPane = new JScrollPane(paneDot);
            scrollPane.getViewport().setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            diagramFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
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
        diagramFrame.getContentPane().add(panel, BorderLayout.NORTH);

        if (oldFrame != null && oldFrame.isVisible()) {
            posX = oldFrame.getLocation().x;
            posY = oldFrame.getLocation().y;
            oldFrame.setVisible(false);
        }

        diagramFrame.pack();
        if (posX > 0) {
            diagramFrame.setLocation(posX, posY);
        } else {
            diagramFrame.setLocationRelativeTo(SketchletEditor.editorFrame);
        }
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

        for (Page s : pages.getPages()) {
            strDot += "\"" + s.getTitle() + "\";\n";
        }

        for (int i = 0; i < pages.getPages().size(); i++) {
            Page s1 = pages.getPages().elementAt(i);

            for (int j = 0; j < i; j++) {
                Page s2 = pages.getPages().elementAt(j);
                Set<String> connection1To2 = s1.getConnections(s2);
                Set<String> connection2To1 = s2.getConnections(s1);
                for (String conn : connection1To2) {
                    strDot += "\"" + s1.getTitle() + "\" -> \"" + s2.getTitle() + "\" [label=\"" + conn.toLowerCase() + "\"];\n";
                }
                for (String conn : connection2To1) {
                    strDot += "\"" + s2.getTitle() + "\" -> \"" + s1.getTitle() + "\" [label=\"" + conn.toLowerCase() + "\"];\n";
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
        DefaultGraphCell[] cells = new DefaultGraphCell[pages.getPages().size()];

        final Hashtable hash = new Hashtable();

        int c = 20;

        for (int i = 0; i < pages.getPages().size(); i++) {
            Page s = pages.getPages().elementAt(i);
            double x = s.getStateDiagramX();
            double y = s.getStateDiagramY();
        }

        Object[][] sketchInfo = Pages.getSketchInfoFromDir(50, 600);

        for (int i = 0; i < pages.getPages().size(); i++) {
            Page s = pages.getPages().elementAt(i);
            double x = s.getStateDiagramX();
            double y = s.getStateDiagramY();

            if (Double.isNaN(x) || x <= 0) {
                x = 20;
            }

            if (x == 20) {
                y = c;
                c += 110;
            }


            DefaultGraphCell cell = createVertex(s.getTitle(), x, y, 90, 80, Color.LIGHT_GRAY, false);
            cells[i] = cell;
            graph.getGraphLayoutCache().insert(cell);

            GraphConstants.setIcon(cell.getAttributes(), new ImageIcon((Image) sketchInfo[i][1]));

            s.setStateDiagramGraph(graph);
            s.setStateDiagramCell(cell);

            if (SketchletEditor.getInstance().getCurrentPage() == s) {
                graph.setSelectionCell(cell);
                selectedPage = s;
            }

            hash.put(s.getTitle(), s);

            for (int j = 0; j < i; j++) {
                boolean connected1 = s.isConnectedTo(pages.getPages().elementAt(j));
                boolean connected2 = pages.getPages().elementAt(j).isConnectedTo(s);
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
                            selectedPage.setStateDiagramX(bounds.getX());
                            selectedPage.setStateDiagramY(bounds.getY());

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
                        selectedPage.setStateDiagramX(bounds.getX());
                        selectedPage.setStateDiagramY(bounds.getY());

                        if (currentPage != selectedPage) {
                            diagramFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            SketchletEditor.getInstance().selectSketch(selectedPage);
                            diagramFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                            currentPage = selectedPage;
                        }
                    }
                }
            });

        }

        return graph;
    }

    public static BufferedImage getDotImage(Pages pages) {
        String strDot = getDot(pages);
        File dotFile = null;
        File imgFile = null;
        try {
            dotFile = File.createTempFile("umlgraph", ".dot");
            imgFile = File.createTempFile("umlgraph", ".png");
            FileUtils.saveFileText(dotFile, strDot);

            List<String> dotParams = new ArrayList<String>();

            dotParams.add(new File(graphvizPath).getAbsolutePath());
            dotParams.add("-Tpng");
            dotParams.add("-o" + imgFile.getAbsolutePath());
            dotParams.add(dotFile.getAbsolutePath());

            ProcessBuilder processBuilder = new ProcessBuilder(dotParams.toArray(new String[dotParams.size()]));
            processBuilder.directory(new File(graphvizPath).getParentFile());
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