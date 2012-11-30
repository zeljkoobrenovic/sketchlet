package net.sf.sketchlet.loaders.pluginloader;

import net.sf.sketchlet.plugin.PluginInfo;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.plugin.WidgetPluginEvents;
import net.sf.sketchlet.plugin.WidgetPluginLinks;
import net.sf.sketchlet.plugin.WidgetPluginProperties;
import net.sf.sketchlet.plugin.WidgetPluginProperty;
import net.sf.sketchlet.plugin.WidgetPluginTextItems;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zobrenovic
 */
public class WidgetPluginHandler extends SketchletPluginHandler {
    private static final Logger log = Logger.getLogger(WidgetPluginHandler.class);

    public WidgetPluginHandler(String name, String type, String strClass, String strDescription, String filePath, String group, int position) {
        super(name, type, strClass, strDescription, filePath, group, position);
    }

    public boolean hasTextItems() {
        try {
            Class c = Class.forName(this.getClassName(), false, PluginLoader.getClassLoader());
            return c.isAnnotationPresent(WidgetPluginTextItems.class);
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin " + this.getFilePath() + "$" + getClassName() + ":\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }

        return false;
    }

    public boolean hasLinks() {
        try {
            Class c = Class.forName(this.getClassName(), false, PluginLoader.getClassLoader());
            return c.isAnnotationPresent(WidgetPluginLinks.class);
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin " + this.getFilePath() + "$" + getClassName() + ":\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }

        return false;
    }

    public String[][] getLinks() {
        List<String[]> linksList = new ArrayList<String[]>();
        try {
            Class c = Class.forName(this.getClassName(), false, PluginLoader.getClassLoader());
            if (c.isAnnotationPresent(WidgetPluginLinks.class)) {
                String sLink = ((WidgetPluginLinks) c.getAnnotation(WidgetPluginLinks.class)).link();
                if (!sLink.isEmpty()) {
                    return new String[][]{{"", sLink}};
                }
                String as[] = ((WidgetPluginLinks) c.getAnnotation(WidgetPluginLinks.class)).links();
                for (String s : as) {
                    String name = "";
                    String link = "";
                    int n = s.indexOf(";");
                    if (n >= 0) {
                        name = s.substring(0, n);
                        link = s.substring(n + 1);
                    } else {
                        link = s;
                    }

                    linksList.add(new String[]{name, link});
                }
            }
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin " + this.getFilePath() + "$" + getClassName() + ":\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }
        String a[][] = new String[linksList.size()][2];
        return linksList.toArray(a);
    }

    public String[] getActions() {
        try {
            Class c = Class.forName(this.getClassName(), false, PluginLoader.getClassLoader());
            if (c.isAnnotationPresent(WidgetPluginEvents.class)) {
                return ((WidgetPluginEvents) c.getAnnotation(WidgetPluginEvents.class)).events();
            }
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin " + this.getFilePath() + "$" + getClassName() + ":\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }

        return new String[]{};
    }

    public String getDefaultTextItems() {
        try {
            Class c = Class.forName(this.getClassName(), false, PluginLoader.getClassLoader());
            if (c.isAnnotationPresent(WidgetPluginTextItems.class)) {
                return ((WidgetPluginTextItems) c.getAnnotation(WidgetPluginTextItems.class)).initValue();
            }
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin " + this.getFilePath() + "$" + getClassName() + ":\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }

        return "";
    }

    public static boolean hasTextItems(WidgetPlugin plugin) {
        return plugin.getClass().isAnnotationPresent(WidgetPluginTextItems.class);
    }

    public static String getDefaultTextItems(WidgetPlugin plugin) {
        try {
            Class c = plugin.getClass();
            if (c.isAnnotationPresent(WidgetPluginTextItems.class)) {
                return ((WidgetPluginTextItems) c.getAnnotation(WidgetPluginTextItems.class)).initValue();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }

        return "";
    }

    public static String getName(PluginInstance plugin) {
        try {
            Class c = plugin.getInstance().getClass();
            if (c.isAnnotationPresent(PluginInfo.class)) {
                return ((PluginInfo) c.getAnnotation(PluginInfo.class)).name();
            } else {
                return plugin.getName();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }

        return "";
    }

    public static String getType(WidgetPlugin plugin) {
        try {
            Class c = plugin.getClass();
            if (c.isAnnotationPresent(PluginInfo.class)) {
                return ((PluginInfo) c.getAnnotation(PluginInfo.class)).type();
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }

        return "";
    }

    public String[][] getDefaultProperties() {
        try {
            Class c = Class.forName(this.getClassName(), false, PluginLoader.getClassLoader());
            if (c.isAnnotationPresent(WidgetPluginProperties.class)) {
                String props[] = ((WidgetPluginProperties) c.getAnnotation(WidgetPluginProperties.class)).properties();
                String properties[][] = new String[props.length][3];
                int i = 0;
                for (String p : props) {
                    String pdata[] = p.split("|");
                    properties[i][0] = pdata.length > 0 ? pdata[0] : "";
                    properties[i][1] = pdata.length > 1 ? pdata[1] : "";
                    properties[i][2] = pdata.length > 1 ? pdata[2] : "";
                    i++;
                }
            }
        } catch (Throwable th) {
            PluginConsole.appendToConsole("ERROR in plugin " + this.getFilePath() + "$" + getClassName() + ":\n");
            PluginConsole.appendToConsole("    Error message: " + th.getMessage() + "\n");
            th.printStackTrace();
        }

        return null;
    }

    public static String[][] getDefaultProperties(WidgetPlugin plugin) {
        try {
            Class c = plugin.getClass();
            List<String[]> properties = new ArrayList<String[]>();
            if (c.isAnnotationPresent(WidgetPluginProperties.class)) {
                String props[] = ((WidgetPluginProperties) c.getAnnotation(WidgetPluginProperties.class)).properties();
                for (String p : props) {
                    String pdata[] = p.split("\\|");
                    String pArray[] = new String[3];
                    pArray[0] = pdata.length > 0 ? pdata[0] : "";
                    pArray[1] = pdata.length > 1 ? pdata[1] : "";
                    pArray[2] = pdata.length > 1 ? pdata[2] : "";
                    properties.add(pArray);
                }
            }
            try {
                for (Field f : c.getDeclaredFields()) {
                    if (f.isAnnotationPresent(WidgetPluginProperty.class)) {
                        f.setAccessible(true);
                        WidgetPluginProperty p = (WidgetPluginProperty) f.getAnnotation(WidgetPluginProperty.class);
                        String pArray[] = new String[3];
                        pArray[0] = p.name();
                        pArray[1] = p.initValue();
                        pArray[2] = p.description();
                        properties.add(pArray);
                    }
                }
            } catch (Throwable e) {
                log.error(e);
            }
            return properties.toArray(new String[properties.size()][3]);
        } catch (Throwable th) {
            th.printStackTrace();
        }

        return new String[][]{};
    }

    public static String[] getValueList(WidgetPlugin plugin, String property) {
        try {
            Class c = plugin.getClass();
            try {
                for (Field f : c.getDeclaredFields()) {
                    if (f.isAnnotationPresent(WidgetPluginProperty.class)) {
                        f.setAccessible(true);
                        WidgetPluginProperty p = (WidgetPluginProperty) f.getAnnotation(WidgetPluginProperty.class);
                        if (p.name().equals(property)) {
                            return p.valueList();
                        }
                    }
                }
            } catch (Throwable e) {
                log.error(e);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }

        return new String[]{};
    }

    public static String[] getEvents(WidgetPlugin plugin, String property) {
        try {
            Class c = plugin.getClass();
            try {
                for (Field f : c.getDeclaredFields()) {
                    if (f.isAnnotationPresent(WidgetPluginProperty.class)) {
                        f.setAccessible(true);
                        WidgetPluginProperty p = (WidgetPluginProperty) f.getAnnotation(WidgetPluginProperty.class);
                        if (p.name().equals(property)) {
                            return p.valueList();
                        }
                    }
                }
            } catch (Throwable e) {
                log.error(e);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }

        return new String[]{};
    }

    public static void injectWidgetPropertiesValues(WidgetPlugin plugin) {
        if (plugin != null) {
            String name = "";
            try {
                Class c = plugin.getClass();
                for (Field f : c.getDeclaredFields()) {
                    if (f.isAnnotationPresent(WidgetPluginProperty.class)) {
                        f.setAccessible(true);
                        name = f.getAnnotation(WidgetPluginProperty.class).name();
                        String widgetProperty = plugin.getActiveRegionContext().getWidgetProperty(name);
                        try {
                            if (f.getType().equals(String.class)) {
                                f.set(plugin, widgetProperty);
                            } else if (f.getType().equals(double.class)) {
                                f.setDouble(plugin, Double.parseDouble(widgetProperty));
                            } else if (f.getType().equals(float.class)) {
                                f.setFloat(plugin, (float) Double.parseDouble(widgetProperty));
                            } else if (f.getType().equals(int.class)) {
                                f.setInt(plugin, (int) Double.parseDouble(widgetProperty));
                            } else if (f.getType().equals(byte.class)) {
                                f.setByte(plugin, (byte) Double.parseDouble(widgetProperty));
                            } else if (f.getType().equals(boolean.class)) {
                                f.setBoolean(plugin, Boolean.parseBoolean(widgetProperty));
                            }
                        } catch (Exception ex) {
                            log.error("Inject Widget Exception: " + name + " = " + widgetProperty, ex);
                        }
                    }
                }
            } catch (Throwable e) {
                log.error("Inject Widget Exception: " + name, e);
            }
        }
    }
}
