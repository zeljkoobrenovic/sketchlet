package net.sf.sketchlet.designer;

import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.context.PageEventsListener;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.loaders.pluginloader.GenericPluginFactory;
import net.sf.sketchlet.loaders.pluginloader.PluginInstance;
import net.sf.sketchlet.loaders.pluginloader.PluginLoader;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.plugin.SketchletProjectAware;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zobrenovic
 */
public class ApplicationLifecycleCentre {
    private static List<PageEventsListener> pageListeners = new Vector<PageEventsListener>();

    public static void afterApplicationStart() {
        Workspace.setVariableSpaces(PluginLoader.getPluginInstances("varspace", "datasource"));
        Workspace.setVariableSourcesNames(PluginLoader.getPluginNames("varspace", "datasource"));

        Workspace.getDerivedVariablesReadyCountDownLatch().countDown();
        GenericPluginFactory.createPluginInstances();
        Workspace.getPluginsReadyCountDownLatch().countDown();
        GenericPluginFactory.afterApplicationStart();
    }

    public static void beforeApplicationEnd() {
        GenericPluginFactory.beforeApplicationEnd();
    }

    public static void afterProjectOpening() {
        if (Workspace.getApplicationReadyCountDownLatch().getCount() > 0) {
            return;
        }
        loadVariableSpaces();
        GenericPluginFactory.afterProjectOpening();
    }

    public static void beforeProjectClosing() {
        try {
            Workspace.getPluginsReadyCountDownLatch().await();
            Workspace.getDerivedVariablesReadyCountDownLatch().await();
            GenericPluginFactory.beforeProjectClosing();
            closeVariableSpaces();
        } catch (InterruptedException e) {
        }
    }

    private static void closeVariableSpaces() {
        if (Workspace.getVariableSpaces() != null) {
            for (PluginInstance ds : Workspace.getVariableSpaces()) {
                if (ds.getInstance() instanceof SketchletProjectAware) {
                    ((SketchletProjectAware) ds.getInstance()).onSave();
                    ((SketchletProjectAware) ds.getInstance()).beforeProjectClosing();
                }
            }
        }
    }

    private static void loadVariableSpaces() {
        for (PluginInstance ds : Workspace.getVariableSpaces()) {
            if (ds.getInstance() instanceof SketchletProjectAware) {
                ((SketchletProjectAware) ds.getInstance()).afterProjectOpening();
            }
        }
    }

    public static void afterPageEntry(final Page sketch) {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable() {

            @Override
            public void run() {
                PageContext page = new PageContextImpl(sketch);
                for (PageEventsListener l : getPageListeners()) {
                    l.afterPageEntry(page);
                }
            }
        });
    }

    public static void beforePageExit(final Page sketch) {
        ExecutorService executor = Executors.newCachedThreadPool();
        executor.execute(new Runnable() {

            @Override
            public void run() {
                PageContext page = new PageContextImpl(sketch);
                for (PageEventsListener l : getPageListeners()) {
                    l.afterPageEntry(page);
                }
            }
        });
    }

    public static List<PageEventsListener> getPageListeners() {
        return pageListeners;
    }
}
