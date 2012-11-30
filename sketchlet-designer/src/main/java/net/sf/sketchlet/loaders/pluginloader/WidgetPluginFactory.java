package net.sf.sketchlet.loaders.pluginloader;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.plugin.WidgetPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class WidgetPluginFactory {

    private static Vector<SketchletPluginHandler> widgetPlugins = new Vector<SketchletPluginHandler>();

    public static PluginInstance getWidgetPluginInstance(ActiveRegionContext regionContext) {
        for (SketchletPluginHandler _plugin : getWidgetPlugins()) {
            if (_plugin instanceof WidgetPluginHandler) {
                WidgetPluginHandler plugin = (WidgetPluginHandler) _plugin;
                if (plugin.getName().equalsIgnoreCase(regionContext.getWidgetType())) {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(PluginLoader.getClassLoader());
                    PluginInstance widget = plugin.getInstance(ActiveRegionContext.class, regionContext);
                    Thread.currentThread().setContextClassLoader(cl);
                    return widget;
                }
            }
        }

        return null;
    }

    public static WidgetPluginHandler getPlugin(ActiveRegionContext regionContext) {
        for (SketchletPluginHandler _plugin : getWidgetPlugins()) {
            if (_plugin instanceof WidgetPluginHandler) {
                WidgetPluginHandler plugin = (WidgetPluginHandler) _plugin;
                if (plugin.getName().equalsIgnoreCase(regionContext.getWidgetType())) {
                    return plugin;
                }
            }
        }

        return null;
    }

    public static String[] getWidgetList() {
        List<String> list = new ArrayList<String>();
        for (SketchletPluginHandler _plugin : getWidgetPlugins()) {
            if (_plugin instanceof WidgetPluginHandler) {
                WidgetPluginHandler plugin = (WidgetPluginHandler) _plugin;
                list.add(plugin.getName());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public static String[][] getWidgetListWithGroups() {
        List<String[]> list = new ArrayList<String[]>();
        for (SketchletPluginHandler _plugin : getWidgetPlugins()) {
            if (_plugin instanceof WidgetPluginHandler) {
                WidgetPluginHandler plugin = (WidgetPluginHandler) _plugin;
                list.add(new String[]{plugin.getStrGroup(), plugin.getName()});
            }
        }
        return list.toArray(new String[list.size()][2]);
    }

    public static boolean exists(String strName) {
        for (String strCtrl : getWidgetList()) {
            if (strCtrl.equalsIgnoreCase(strName)) {
                return true;
            }
        }
        return false;
    }

    public static String[][] getDefaultProperties(ActiveRegionContext region) {
        String defaults[][] = new String[][]{};

        PluginInstance pluginInstance = getWidgetPluginInstance(region);
        if (pluginInstance != null) {
            WidgetPlugin widget = (WidgetPlugin) pluginInstance.getInstance();

            if (widget != null) {
                return WidgetPluginHandler.getDefaultProperties(widget);
            }
        }

        return defaults;
    }

    public static String[] getValueList(ActiveRegionContext region, String property) {
        String defaults[] = new String[]{};

        PluginInstance pluginInstance = getWidgetPluginInstance(region);
        if (pluginInstance != null) {
            WidgetPlugin widget = (WidgetPlugin) pluginInstance.getInstance();

            if (widget != null) {
                return WidgetPluginHandler.getValueList(widget, property);
            }
        }

        return defaults;
    }

    public static String getDefaultPropertiesValue(ActiveRegionContext regionContext) {
        PluginInstance pluginInstance = getWidgetPluginInstance(regionContext);
        if (pluginInstance != null) {
            WidgetPlugin widget = (WidgetPlugin) pluginInstance.getInstance();
            if (widget != null) {
                return getDefaultPropertiesValueString(widget);
            }
        }

        return "";
    }

    private static String getDefaultPropertiesValueString(WidgetPlugin widget) {
        String strValue = "";

        String[][] defaultProperties = WidgetPluginHandler.getDefaultProperties(widget);

        for (int i = 0; i < defaultProperties.length; i++) {
            if (!strValue.isEmpty()) {
                strValue += ";";
            }

            strValue += defaultProperties[i][0] + "=" + defaultProperties[i][1];
        }

        return strValue;
    }

    public static boolean hasTextItems(ActiveRegionContext regionContext) {
        WidgetPluginHandler plugin = getPlugin(regionContext);
        if (plugin != null) {
            if (plugin.hasTextItems()) {
                return true;
            }
        }
        PluginInstance pluginInstance = getWidgetPluginInstance(regionContext);

        if (pluginInstance != null) {
            WidgetPlugin widget = (WidgetPlugin) pluginInstance.getInstance();

            if (widget != null) {
                return WidgetPluginHandler.hasTextItems(widget);
            }
        }

        return true;
    }

    public static boolean hasLinks(ActiveRegionContext regionContext) {
        WidgetPluginHandler plugin = getPlugin(regionContext);
        if (plugin != null) {
            return plugin.hasLinks();
        }
        return false;
    }

    public static String[][] getLinks(ActiveRegionContext regionContext) {
        WidgetPluginHandler plugin = getPlugin(regionContext);
        if (plugin != null) {
            return plugin.getLinks();
        }
        return null;
    }

    public static String[] getActions(ActiveRegionContext regionContext) {
        WidgetPluginHandler plugin = getPlugin(regionContext);
        if (plugin != null) {
            return plugin.getActions();
        }
        return new String[]{};
    }

    public static String getDefaultItemsText(ActiveRegionContext regionContext) {
        WidgetPluginHandler plugin = getPlugin(regionContext);
        if (plugin != null) {
            if (plugin.hasTextItems()) {
                return plugin.getDefaultTextItems();
            }
        }
        WidgetPlugin widget = (WidgetPlugin) getWidgetPluginInstance(regionContext).getInstance();

        if (widget != null) {
            return WidgetPluginHandler.getDefaultTextItems(widget);
        }

        return "";
    }

    public static Vector<SketchletPluginHandler> getWidgetPlugins() {
        return widgetPlugins;
    }
}