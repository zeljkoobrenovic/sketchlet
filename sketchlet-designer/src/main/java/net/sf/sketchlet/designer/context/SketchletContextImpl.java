/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.context;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.context.PageEventsListener;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.context.VariableUpdateListener;
import net.sf.sketchlet.designer.ApplicationLifecycleCentre;
import net.sf.sketchlet.designer.GlobalProperties;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.programming.macros.Commands;
import net.sf.sketchlet.designer.ui.MessageFrame;
import net.sf.sketchlet.designer.ui.connectors.PluginsFrame;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.designer.ui.playback.PlaybackPanel;
import net.sf.sketchlet.parser.JEParser;
import net.sf.sketchlet.pluginloader.PluginLoader;
import net.sf.sketchlet.script.ScriptPluginProxy;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class SketchletContextImpl extends SketchletContext {

    private boolean stopped;
    Object script;
    public static BufferedImage image = Workspace.createCompatibleImage(2000, 2000);
    Graphics2D g2;
    static JFileChooser fc;

    private static String getFilePath(String title, int selectionMode, boolean bAllFiles) {
        if (fc == null) {
            fc = new JFileChooser();
        }
        if (title == null) {
            title = "";
        }

        fc.setFileSelectionMode(selectionMode);
        fc.setAcceptAllFileFilterUsed(bAllFiles);
        String path = "";
        fc.setDialogTitle(title);

        int result = fc.showOpenDialog(PlaybackFrame.playbackFrame != null ? PlaybackFrame.playbackFrame[0] : SketchletEditor.editorFrame);
        if (result == JFileChooser.APPROVE_OPTION) {
            path = fc.getSelectedFile().getPath();
        }

        return path;
    }

    public SketchletContextImpl() {
    }

    public SketchletContextImpl(Object script) {
        this.script = script;
    }

    @Override
    public String getApplicationHomeDir() {
        String strHome = System.getenv("SKETCHLET_HOME");
        if (SketchletContextUtils.getTemporaryRuntimeDirectoryPath() != null) {
            strHome = new File(SketchletContextUtils.getTemporaryRuntimeDirectoryPath()).getParent();
        } else if (strHome == null || strHome.trim().isEmpty()) {
            strHome = System.getenv("SKETCHIFY_HOME");
            if (strHome == null || strHome.trim().isEmpty()) {
                URL url = SketchletContextImpl.class.getProtectionDomain().getCodeSource().getLocation();
                File f;
                try {
                    f = new File(url.toURI());
                } catch (Exception e) {
                    f = new File(url.getPath());
                }
                strHome = f.getParentFile().getParent() + "/";
            }
        }

        if (!strHome.endsWith("/") && !strHome.endsWith("\\")) {
            strHome += File.separator;
        }
        return strHome;
    }

    @Override
    public PageContext getCurrentPageContext() {
        return new PageContextImpl(this.getSketch());
    }

    @Override
    public String getCurrentProjectDirectory() {
        return SketchletContextUtils.getCurrentProjectDir();
    }

    @Override
    public String getCurrentProjectName() {
        return SketchletContextUtils.getCurrentProjectDirName();
    }

    private Page getSketch() {
        Page page;
        if ((PlaybackFrame.playbackFrame != null || SketchletEditor.editorPanel.internalPlaybackPanel != null) && PlaybackPanel.currentPage != null) {
            page = PlaybackPanel.currentPage;
        } else {
            page = SketchletEditor.editorPanel.currentPage;
        }
        return page;
    }

    @Override
    public SketchletContext getInstance(Object script) {
        return new SketchletContextImpl(script);
    }

    @Override
    public boolean isApplicationReady() {
        return DataServer.variablesServer != null;
    }

    @Override
    public void goToPage(String name) {
        Commands.execute(this, "Go to page", name, "", null, null, "", "", null);
    }

    @Override
    public void startMacro(String name) {
        Commands.execute(this, "Start action", name, "", getSketch().activeTimers, getSketch().activeMacros, "", "", null);
    }

    @Override
    public void startCommandSequence(String name) {
        Commands.execute(this, "Start sequence", name, "", getSketch().activeTimers, getSketch().activeMacros, "", "", null);
    }

    @Override
    public void stopMacro(String name) {
        Commands.execute(this, "Stop action", name, "", getSketch().activeTimers, getSketch().activeMacros, "", "", null);
    }

    @Override
    public void startTimer(String name) {
        Commands.execute(this, "Start timer", name, "", getSketch().activeTimers, getSketch().activeMacros, "", "", null);
    }

    @Override
    public void stopTimer(String name) {
        Commands.execute(this, "Stop timer", name, "", getSketch().activeTimers, getSketch().activeMacros, "", "", null);
    }

    @Override
    public void pauseTimer(String name) {
        Commands.execute(this, "Pause timer", name, "", getSketch().activeTimers, getSketch().activeMacros, "", "", null);
    }

    @Override
    public void pause(double seconds) {
        if (DataServer.variablesServer == null || DataServer.paused || stopped) {
            return;
        }
        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (Exception e) {
        }
    }

    boolean waiting = false;

    @Override
    public void waitForVariableUpdate(final String variable) {
        if (DataServer.variablesServer == null || DataServer.paused || stopped) {
            waiting = false;
            return;
        }
        try {
            while (waiting) {
                Thread.sleep(20);
            }
        } catch (Exception e) {
        }
        waiting = true;
        VariableUpdateListener ch = new VariableUpdateListener() {

            public void variableUpdated(String name, String value) {
                if (name.equalsIgnoreCase(variable)) {
                    waiting = false;
                }
            }
        };
        DataServer.variablesServer.addVariablesUpdateListener(ch);
        try {
            while (waiting) {
                Thread.sleep(20);
            }
        } catch (Exception e) {
        }
        DataServer.variablesServer.removeVariablesUpdateListener(ch);

        if (script != null && script instanceof ScriptPluginProxy) {
            ((ScriptPluginProxy) script).updateContext(variable, DataServer.variablesServer.getVariableValue(variable));
        }
    }

    @Override
    public void waitUntilExpressionTrue(String expression) {
        if (!expression.equals("")) {
            try {
                while (true) {
                    expression = DataServer.getTemplateFromApostrophes(expression);
                    expression = DataServer.populateTemplate(expression);
                    Object result = JEParser.getValue(expression);
                    if (result == null || !(result instanceof Double) || ((Double) result).doubleValue() == 0.0) {
                        Thread.sleep(100);
                    } else {
                        break;
                    }
                }

            } catch (Exception e) {
            }
        }
    }

    @Override
    public void updateVariable(String variable, String value) {
        if (DataServer.variablesServer == null || DataServer.paused || stopped) {
            return;
        }
        Commands.updateVariableOrProperty(this, variable, value, Commands.ACTION_VARIABLE_UPDATE);

        if (script != null && script instanceof ScriptPluginProxy) {
            ((ScriptPluginProxy) script).updateContext(variable, value);
        }
        try {
            Thread.sleep(1);
        } catch (Exception e) {
        }
    }

    @Override
    public String askFile(String dialogTitle) {
        return getFilePath(dialogTitle, JFileChooser.FILES_ONLY, true);
    }

    @Override
    public String askFolder(String dialogTitle) {
        return getFilePath(dialogTitle, JFileChooser.DIRECTORIES_ONLY, true);
    }

    @Override
    public String askFileOrFolder(String dialogTitle) {
        return getFilePath(dialogTitle, JFileChooser.FILES_AND_DIRECTORIES, true);
    }

    @Override
    public String ask(String question) {
        String strAnswer = JOptionPane.showInputDialog(question);

        return strAnswer == null ? "" : strAnswer;
    }

    @Override
    public int askInteger(String question) {
        int result = 0;

        try {
            String strAnswer = ask(question);
            if (!strAnswer.isEmpty()) {
                result = Integer.parseInt(strAnswer);
            }
        } catch (Exception e) {
        }

        return result;
    }

    @Override
    public double askDouble(String question) {
        double result = 0.0;

        try {
            String strAnswer = ask(question);
            if (!strAnswer.isEmpty()) {
                result = Double.parseDouble(strAnswer);
            }
        } catch (Exception e) {
        }

        return result;
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    @Override
    public String getVariableValue(String variable) {
        if (DataServer.variablesServer == null || DataServer.paused || stopped) {
            return null;
        }

        if (script != null && script instanceof ScriptPluginProxy) {
            ((ScriptPluginProxy) script).updateContext(variable, DataServer.variablesServer.getVariableValue(variable));
        }
        return DataServer.variablesServer.getVariableValue(variable);
    }

    @Override
    public int getVariableValueAsInteger(String variable) {
        int value = 0;
        try {
            value = (int) Double.parseDouble(getVariableValue(variable));
        } catch (Exception e) {
        }
        return value;
    }

    @Override
    public float getVariableValueAsFloat(String variable) {
        float value = 0.0f;
        try {
            value = (float) Double.parseDouble(getVariableValue(variable));
        } catch (Exception e) {
        }
        return value;
    }

    @Override
    public double getVariableValueAsDouble(String variable) {
        double value = 0;
        try {
            value = Double.parseDouble(getVariableValue(variable));
        } catch (Exception e) {
        }
        return value;
    }

    @Override
    public void setGlobalProperty(String property, String value) {
        GlobalProperties.set(property, value);
    }

    @Override
    public String getGlobalProperty(String property) {
        String value = GlobalProperties.get(property);
        if (value != null) {
            return value;
        }

        return "";

    }

    @Override
    public void repaint() {
        if (SketchletEditor.editorPanel != null) {
            SketchletEditor.editorPanel.repaintEverything();
        }
    }

    @Override
    public void requestFocus() {
        if (SketchletEditor.editorPanel.internalPlaybackPanel != null) {
            SketchletEditor.editorPanel.internalPlaybackPanel.requestFocus();
        }

        if (PlaybackFrame.playbackFrame != null) {
            for (int i = 0; i < PlaybackFrame.playbackFrame.length; i++) {
                PlaybackFrame frame = PlaybackFrame.playbackFrame[i];
                if (frame != null) {
                    frame.playbackPanel.requestFocus();
                    break;
                }
            }
        }
    }

    @Override
    public JFrame getMainFrame() {
        return Workspace.mainFrame;
    }

    @Override
    public JFrame getEditorFrame() {
        return SketchletEditor.editorFrame;
    }

    @Override
    public JFrame getPluginFrame() {
        return PluginsFrame.frame;
    }

    @Override
    public int getPageCount() {
        return SketchletEditor.pages.pages.size();
    }

    @Override
    public Vector<PageContext> getPages() {
        Vector<PageContext> regionsContext = new Vector<PageContext>();
        for (Page s : SketchletEditor.pages.pages) {
            regionsContext.add(new PageContextImpl(s));
        }

        return regionsContext;
    }

    @Override
    public boolean isInPlaybackMode() {
        return (PlaybackFrame.playbackFrame != null || (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.internalPlaybackPanel != null))
                && PlaybackPanel.currentPage != null;
    }

    public boolean isMessageShowing() {
        return MessageFrame.isOpen();
    }

    public ClassLoader getPluginClassLoader() {
        return PluginLoader.classLoader;
    }

    public void addPageEventsListener(PageEventsListener listener) {
        ApplicationLifecycleCentre.pageListeners.add(listener);
    }

    public void removePageEventsListener(PageEventsListener listener) {
        ApplicationLifecycleCentre.pageListeners.remove(listener);
    }

    public String getUserDirectory() {
        return SketchletContextUtils.getDefaultProjectsRootLocation();
    }
}