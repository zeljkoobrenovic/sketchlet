/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer;

import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.context.PageEventsListener;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.designer.data.Page;
import net.sf.sketchlet.plugin.SketchletProjectAware;
import net.sf.sketchlet.pluginloader.GenericPluginFactory;
import net.sf.sketchlet.pluginloader.PluginInstance;
import net.sf.sketchlet.pluginloader.PluginLoader;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zobrenovic
 */
public class ApplicationLifecycleCentre {

    public static void afterApplicationStart() {
        Workspace.variableSpaces = PluginLoader.getPluginInstances("varspace", "datasource");
        Workspace.variableSourcesNames = PluginLoader.getPluginNames("varspace", "datasource");

        Workspace.derivedVariablesReady.countDown();
        GenericPluginFactory.createPluginInstances();
        Workspace.pluginsReady.countDown();
        GenericPluginFactory.afterApplicationStart();
    }

    public static void beforeApplicationEnd() {
        GenericPluginFactory.beforeApplicationEnd();
    }

    public static void afterProjectOpening() {
        if (Workspace.applicationReady.getCount() > 0) {
            return;
        }
        loadVariableSpaces();
        GenericPluginFactory.afterProjectOpening();
    }

    public static void beforeProjectClosing() {
        try {
            Workspace.pluginsReady.await();
            Workspace.derivedVariablesReady.await();
            GenericPluginFactory.beforeProjectClosing();
            closeVariableSpaces();
        } catch (InterruptedException e) {
        }
    }

    private static void closeVariableSpaces() {
        if (Workspace.variableSpaces != null) {
            for (PluginInstance ds : Workspace.variableSpaces) {
                if (ds.getInstance() instanceof SketchletProjectAware) {
                    ((SketchletProjectAware) ds.getInstance()).onSave();
                    ((SketchletProjectAware) ds.getInstance()).beforeProjectClosing();
                }
            }
        }
    }

    private static void loadVariableSpaces() {
        for (PluginInstance ds : Workspace.variableSpaces) {
            if (ds.getInstance() instanceof SketchletProjectAware) {
                ((SketchletProjectAware) ds.getInstance()).afterProjectOpening();
            }
        }
    }

    public static List<PageEventsListener> pageListeners = new Vector<PageEventsListener>();

    public static void afterPageEntry(final Page sketch) {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable() {

            public void run() {
                PageContext page = new PageContextImpl(sketch);
                for (PageEventsListener l : pageListeners) {
                    l.afterPageEntry(page);
                }
            }
        });
    }

    public static void beforePageExit(final Page sketch) {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable() {

            public void run() {
                PageContext page = new PageContextImpl(sketch);
                for (PageEventsListener l : pageListeners) {
                    l.afterPageEntry(page);
                }
            }
        });
    }
}
