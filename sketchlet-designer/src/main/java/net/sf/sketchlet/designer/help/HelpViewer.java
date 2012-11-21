/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.help;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.html.HTMLImageRenderer;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.model.ActiveRegion;
import org.apache.log4j.Logger;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.swing.ScalableXHTMLPanel;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class HelpViewer extends JPanel {
    private static final Logger log = Logger.getLogger(HelpViewer.class);

    private ScalableXHTMLPanel panel;
    private FSScrollPane scrollPane;
    private JCheckBox autoHelp = new JCheckBox("context sensitive", true);
    private JButton back = new JButton(Workspace.createImageIcon("resources/go-previous.png"));
    private JButton forward = new JButton(Workspace.createImageIcon("resources/go-next.png"));
    private JButton home = new JButton("Help Index", Workspace.createImageIcon("resources/go-home.png"));
    private Vector<String> history = new Vector<String>();
    private Vector<String> historyForward = new Vector<String>();
    private JButton edit = new JButton(".");

    private String indexPage = "";
    private String strHTML = "";
    private String strPrevURL = null;
    private static DocumentBuilderFactory factory;
    private static DocumentBuilder builder;

    public HelpViewer() {
        this("index");
    }

    public HelpViewer(String _indexPage) {
        this(_indexPage, true);
    }

    public HelpViewer(String _indexPage, boolean bShowToolbar) {
        this.setIndexPage(_indexPage);
        setLayout(new BorderLayout());
        JToolBar panelSouth = new JToolBar();
        panelSouth.setFloatable(false);
        back.setToolTipText("Back");
        forward.setToolTipText("Forward");
        panelSouth.add(back);
        panelSouth.add(forward);
        back.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (history.size() > 0) {
                    historyForward.add(strPrevURL);
                    String strPath = history.lastElement();
                    history.removeElement(strPath);
                    showHelp(strPath, false);
                    back.setEnabled(history.size() > 0);
                    forward.setEnabled(historyForward.size() > 0);
                }
            }
        });
        edit.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                try {
                    String strCmd = "\"C:\\Program Files\\Adobe\\Adobe Dreamweaver CS3\\Dreamweaver.exe\" \"" + strPrevURL + "\"";
                    Runtime.getRuntime().exec(strCmd);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        });
        forward.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                if (historyForward.size() > 0) {
                    String strPath = historyForward.lastElement();
                    historyForward.removeElement(strPath);
                    showHelp(strPath);
                    forward.setEnabled(historyForward.size() > 0);
                }
            }
        });
        home.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                showHelpByID(getIndexPage());
            }
        });
        back.setEnabled(false);
        back.setToolTipText("Back");
        panelSouth.add(home);
        autoHelp.setToolTipText("If selecteed, the content of the help window will change automatically as you select different tools and modes");
        panelSouth.add(autoHelp);
        panelSouth.add(edit);
        autoHelp.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(autoHelp);
        home.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(home);
        if (bShowToolbar) {
            add(panelSouth, BorderLayout.NORTH);
        }
    }

    public void showHelp(String strFile) {
        showHelp(strFile, true);
    }

    public void showHelp(String strFile, boolean bAddToHistory) {
        try {
            panel = new ScalableXHTMLPanel();
            List l = panel.getMouseTrackingListeners();
            for (Iterator i = l.iterator(); i.hasNext(); ) {
                FSMouseListener listener = (FSMouseListener) i.next();
                if (listener instanceof LinkListener) {
                    panel.removeMouseTrackingListener(listener);
                }
            }

            panel.addMouseTrackingListener(new LinkListener() {

                public void linkClicked(BasicPanel panel, String uri) {
                    if (uri.toLowerCase().startsWith("sketchify://") || uri.toLowerCase().startsWith("sketchlet://")) {
                        processSketchletURL(uri.substring(12));
                    } else if (uri.contains("://")) {
                        SketchletContextUtils.openWebBrowser(uri);
                    } else {
                        super.linkClicked(panel, uri);
                        if (strPrevURL != null) {
                            history.add(strPrevURL);
                            back.setEnabled(history.size() > 0);
                        }
                        strPrevURL = SketchletContextUtils.getSketchletDesignerHelpDir() + uri;
                    }
                }
            });
            if (strFile.contains("://")) {
                panel.setDocument(strFile);
            } else {
                panel.setDocument(new File(strFile));
            }
            if (scrollPane != null) {
                remove(scrollPane);
            }

            scrollPane = new FSScrollPane(panel);
            add(scrollPane);
            revalidate();
            if (bAddToHistory && strPrevURL != null) {
                history.add(strPrevURL);
                back.setEnabled(history.size() > 0);
                historyForward.removeAllElements();
                historyForward.removeAllElements();
                forward.setEnabled(historyForward.size() > 0);
            }
            strPrevURL = strFile;
        } catch (Exception e) {
            System.err.println("Error in the Help File: " + strFile);
            System.err.println("    " + e.getMessage());
        }
    }

    private void processSketchletURL(String command) {
        if (SketchletEditor.getInstance() == null) {
            return;
        }
        if (command.equalsIgnoreCase("goto variables-panel")) {
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
        } else if (command.equalsIgnoreCase("goto services-panel")) {
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
            SketchletEditor.getInstance().getTabsRight().setSelectedIndex(SketchletEditor.getIoservicesTabIndex());
        } else if (command.equalsIgnoreCase("goto timers-panel")) {
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
            SketchletEditor.getInstance().getTabsRight().setSelectedIndex(SketchletEditor.getProgrammingTabIndex());
            SketchletEditor.getInstance().getTabsProgramming().setSelectedIndex(SketchletEditor.getTimersTabIndex());
        } else if (command.equalsIgnoreCase("goto macros-panel")) {
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
            SketchletEditor.getInstance().getTabsRight().setSelectedIndex(SketchletEditor.getProgrammingTabIndex());
            SketchletEditor.getInstance().getTabsProgramming().setSelectedIndex(SketchletEditor.getMacrosTabIndex());
        } else if (command.equalsIgnoreCase("goto screen-poking-panel")) {
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
            SketchletEditor.getInstance().getTabsRight().setSelectedIndex(SketchletEditor.getProgrammingTabIndex());
            SketchletEditor.getInstance().getTabsProgramming().setSelectedIndex(SketchletEditor.getScreenpokingTabIndex());
        } else if (command.equalsIgnoreCase("goto scripts-panel")) {
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
            SketchletEditor.getInstance().getTabsRight().setSelectedIndex(SketchletEditor.getProgrammingTabIndex());
            SketchletEditor.getInstance().getTabsProgramming().setSelectedIndex(SketchletEditor.getScriptsTabIndex());
        } else if (command.equalsIgnoreCase("page://")) {
            String strPage = command.substring(7);
            SketchletEditor.getInstance().selectSketch(strPage);
        } else if (command.startsWith("region-")) {
            regionAction(command);
        } else if (command.startsWith("menu ")) {
            String strMenuCommand = command.substring(5).trim();
            Workspace.getMainPanel().executeCommand(strMenuCommand, null);
        }
    }

    private ActiveRegion createActiveRegion(int x, int y, int width, int height) {
        ActiveRegion reg = new ActiveRegion(SketchletEditor.getInstance().getCurrentPage().getRegions());
        reg.x1 = x;
        reg.y1 = y;
        reg.x2 = x + width;
        reg.y2 = y + height;
        SketchletEditor.getInstance().setEditorMode(SketchletEditorMode.ACTIONS);
        SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().insertElementAt(reg, 0);
        SketchletEditor.getInstance().getCurrentPage().getRegions().setSelectedRegions(null);
        SketchletEditor.getInstance().getCurrentPage().getRegions().addToSelection(reg);

        return reg;
    }

    private void refreshRegion(ActiveRegion reg, int tab, int subtab) {
        SketchletEditor.getInstance().repaint();
        ActiveRegionsFrame.showRegionsAndActions();
        ActiveRegionsFrame.reload(reg);

        switch (tab) {
            case 0:
                tab = ActiveRegionPanel.indexGraphics;
                break;
            case 1:
                tab = ActiveRegionPanel.indexWidget;
                break;
            case 2:
                tab = ActiveRegionPanel.indexTransform;
                break;
            case 3:
                tab = ActiveRegionPanel.indexEvents;
                break;
            case 6:
                tab = ActiveRegionPanel.indexGeneral;
                break;
        }

        final ActiveRegionPanel ap = ActiveRegionsFrame.refresh(reg, tab);
        if (ap != null && subtab >= 0) {
            if (tab == ActiveRegionPanel.indexGraphics) {
                ap.tabsImage.setSelectedIndex(subtab);
            }
        }
    }

    private void regionAction(String command) {
    }

    public void showHelpByID(String strID) {
        if (SketchletEditor.getInstance() != null) {
            SketchletEditor.getInstance().getSketchToolbar().showNavigator(true);
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(SketchletEditor.getInstance().getTabsBrowser().getTabCount() - 1);
        }
        String strPath = SketchletContextUtils.getSketchletDesignerHelpDir() + strID + ".html";
        showHelp(strPath);
    }

    public void showAutoHelpByID(String strID) {
        if (this.autoHelp.isSelected()) {
            String strPath = SketchletContextUtils.getSketchletDesignerHelpDir() + strID + ".html";
            showHelp(strPath);
        }
    }

    public void showHelpByID(String strID, boolean bForceShow) {
        if (bForceShow && SketchletEditor.getInstance() != null) {
            SketchletEditor.getInstance().getSketchToolbar().showNavigator(true);
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(SketchletEditor.getInstance().getTabsBrowser().getTabCount() - 1);
        }
        String strPath = SketchletContextUtils.getSketchletDesignerHelpDir() + strID + ".html";
        showHelp(strPath);
    }

    private void fillHTMLPanel() {
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            factory.setIgnoringComments(true);
            factory.setValidating(false);
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {

                public void warning(SAXParseException exception) throws SAXException {
                }

                public void error(SAXParseException exception) throws SAXException {
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                }
            });
            panel = new ScalableXHTMLPanel();
            panel.setDocument(builder.parse(HTMLImageRenderer.getInputStream(strHTML)));
            panel.relayout();
        } catch (Exception e) {
            log.error(e);
        }
    }

    public String getIndexPage() {
        return indexPage;
    }

    public void setIndexPage(String indexPage) {
        this.indexPage = indexPage;
    }
}
