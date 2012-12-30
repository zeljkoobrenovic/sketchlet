package net.sf.sketchlet.designer.help;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.desktop.Notepad;
import org.apache.log4j.Logger;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.swing.BasicPanel;
import org.xhtmlrenderer.swing.FSMouseListener;
import org.xhtmlrenderer.swing.LinkListener;
import org.xhtmlrenderer.swing.ScalableXHTMLPanel;

import javax.swing.*;
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
    private String prevURL = null;

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
                    historyForward.add(prevURL);
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
                Notepad.openNotepadFromFile(new File(prevURL), "Edit Help File", "text/html");
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
                        if (prevURL != null) {
                            history.add(prevURL);
                            back.setEnabled(history.size() > 0);
                        }
                        prevURL = SketchletContextUtils.getSketchletDesignerHelpDir() + uri;
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
            if (bAddToHistory && prevURL != null) {
                history.add(prevURL);
                back.setEnabled(history.size() > 0);
                historyForward.removeAllElements();
                historyForward.removeAllElements();
                forward.setEnabled(historyForward.size() > 0);
            }
            prevURL = strFile;
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
            SketchletEditor.getInstance().getTabsNavigator().setSelectedIndex(0);
        } else if (command.equalsIgnoreCase("goto services-panel")) {
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
            SketchletEditor.getInstance().getTabsNavigator().setSelectedIndex(1);
            SketchletEditor.getInstance().getTabsRight().setSelectedIndex(SketchletEditor.getIoservicesTabIndex());
        } else if (command.equalsIgnoreCase("goto timers-panel")) {
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
            SketchletEditor.getInstance().getTabsNavigator().setSelectedIndex(1);
            SketchletEditor.getInstance().getTabsRight().setSelectedIndex(SketchletEditor.getProgrammingTabIndex());
            SketchletEditor.getInstance().getTabsProgramming().setSelectedIndex(SketchletEditor.getTimersTabIndex());
        } else if (command.equalsIgnoreCase("goto macros-panel")) {
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
            SketchletEditor.getInstance().getTabsNavigator().setSelectedIndex(1);
            SketchletEditor.getInstance().getTabsRight().setSelectedIndex(SketchletEditor.getProgrammingTabIndex());
            SketchletEditor.getInstance().getTabsProgramming().setSelectedIndex(SketchletEditor.getMacrosTabIndex());
        } else if (command.equalsIgnoreCase("goto screen-poking-panel")) {
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
            SketchletEditor.getInstance().getTabsNavigator().setSelectedIndex(1);
            SketchletEditor.getInstance().getTabsRight().setSelectedIndex(SketchletEditor.getProgrammingTabIndex());
            SketchletEditor.getInstance().getTabsProgramming().setSelectedIndex(SketchletEditor.getScreenpokingTabIndex());
        } else if (command.equalsIgnoreCase("goto scripts-panel")) {
            SketchletEditor.getInstance().getTabsBrowser().setSelectedIndex(0);
            SketchletEditor.getInstance().getTabsNavigator().setSelectedIndex(1);
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

    public String getIndexPage() {
        return indexPage;
    }

    public void setIndexPage(String indexPage) {
        this.indexPage = indexPage;
    }
}
