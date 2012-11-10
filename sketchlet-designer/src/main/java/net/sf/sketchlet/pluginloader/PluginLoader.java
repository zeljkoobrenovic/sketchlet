/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.pluginloader;

import net.sf.net.logger.SketchletPluginLogger;
import net.sf.sketchlet.common.XPathEvaluator;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.plugin.PluginInfo;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author zobrenovic
 */
public class PluginLoader {
    private static final Logger log = Logger.getLogger(PluginLoader.class);

    public static JarClassLoader classLoader = new JarClassLoader(new URL[]{});
    public static StringBuffer pluginsInfo = new StringBuffer("");

    public static void loadPlugins() {
        File pluginDir = new File(PluginLoader.getPluginDir());
        PluginLoader.loadPlugins(pluginDir);
        if (PluginLoader.getUserPluginDir() != null) {
            pluginDir = new File(PluginLoader.getUserPluginDir());
            if (pluginDir != null) {
                PluginLoader.loadPlugins(pluginDir);
            }
        }
    }

    public static void loadPlugins(File pluginDir) {
        PluginLoader.pluginsInfo.append("Plugins loaded from " + pluginDir.getPath() + ":\n");
        pluginDir.mkdirs();
        File pluginLibDir = new File(pluginDir, "lib");
        pluginLibDir.mkdirs();

        File libJars[] = pluginLibDir.listFiles();
        if (libJars != null) {
            for (File libJar : libJars) {
                try {
                    if (!libJar.isDirectory() && libJar.getName().toLowerCase().endsWith(".jar")) {
                        classLoader.addFile(libJar.getPath());
                    }
                } catch (Exception e) {
                    log.info("Could not load a plugin file '" + libJar.getPath() + "'");
                    PluginLoader.pluginsInfo.append("Could not load a plugin file '" + libJar.getPath() + "'\n");
                    log.error(e);
                }
            }
        }

        File pluginJars[] = pluginDir.listFiles();
        if (pluginJars != null) {
            for (File pluginJar : pluginJars) {
                if (!pluginJar.isDirectory() && pluginJar.getName().toLowerCase().endsWith(".jar")) {
                    try {
                        //classLoader.addFile(pluginJar.getPath());
                        InputStream in = classLoader.getResourceAsStream("jar:file:///" + pluginJar.getPath() + "!/META-INF/plugin.xml");
                        if (in == null) {
                            File confFile = new File(pluginDir, "plugin.xml");
                            if (confFile.exists()) {
                                in = new FileInputStream(confFile);
                            } else {
                                confFile = new File(pluginDir + "/META-INF", "plugin.xml");
                                if (confFile.exists()) {
                                    in = new FileInputStream(confFile);
                                }
                            }
                        }
                        if (in != null) {
                            PluginLoader.pluginsInfo.append("  + Plugin Container: " + pluginJar.getName() + "\n");
                            classLoader.addFile(pluginJar.getPath());
                            XPathEvaluator xpath = new XPathEvaluator();
                            xpath.createDocumentFromInputStream(in);
                            NodeList nodes = xpath.getNodes("/plugins/plugin");
                            for (int i = 0; i < nodes.getLength(); i++) {
                                String strPrefix = "/plugins/plugin[position()=" + (i + 1) + "]/";
                                String strName = xpath.getString(strPrefix + "@name");
                                String strType = xpath.getString(strPrefix + "@type");
                                String strClass = xpath.getString(strPrefix + "@class");
                                String strPos = xpath.getString(strPrefix + "@position");
                                String strGroup = xpath.getString(strPrefix + "@group");
                                String strDescription = xpath.getString(strPrefix + "/description");

                                SketchletPluginHandler plugin = addPlugin(strClass, strName, strType, strPos, strDescription, strGroup, new String[]{}, pluginJar.getPath());

                                if (plugin != null) {
                                    NodeList nodeParams = xpath.getNodes(strPrefix + "param");
                                    if (nodeParams != null) {
                                        for (int j = 0; j < nodeParams.getLength(); j++) {
                                            String strPrefixParam = strPrefix + "/param[position()=" + (j + 1) + "]/";
                                            String strParamName = xpath.getString(strPrefixParam + "@name");
                                            String strParamValue1 = xpath.getString(strPrefixParam + "@value");
                                            String strParamValue2 = xpath.getString(strPrefixParam + ".");

                                            plugin.addParam(strParamName, !strParamValue1.isEmpty() ? strParamValue1 : strParamValue2);
                                        }
                                    }
                                }

                            }

                            nodes = xpath.getNodes("/plugins/plugin-scan");
                            for (int i = 0; i < nodes.getLength(); i++) {
                                String basePackage = xpath.getString("/plugins/plugin-scan/@base-package");
                                scan(pluginJar, basePackage);
                            }
                        }
                    } catch (Throwable e) {
                        log.info("Could not load a plugin file '" + pluginJar.getPath() + "'");
                        PluginLoader.pluginsInfo.append("Could not load a plugin file '" + pluginJar.getPath() + "'\n");
                        log.error(e);
                    }
                } else if (pluginJar.isDirectory() && !pluginJar.getName().toLowerCase().equals("lib")) {
                    loadPlugins(pluginJar);
                }
            }

        }
        scanMainJarFile();
        sortPlugins();
    }

    public static void scanMainJarFile() {
        File mainJarFile;
        URL url = PluginLoader.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            mainJarFile = new File(url.toURI());
        } catch (Exception e) {
            mainJarFile = new File(url.getPath());
        }

        scan(mainJarFile, "net.sf.sketchlet.plugins");
        scan(mainJarFile, "net.sf.sketchlet.designer.eye.eye");
    }

    public static void sortPlugins() {
        sort(GenericPluginFactory.allPlugins);
        sort(ScriptPluginFactory.scriptPlugins);
        sort(WidgetPluginFactory.widgetPlugins);
        sort(CodeGenPluginFactory.codegenPlugins);
    }

    private static List<String> alreadyScannedPackages = new ArrayList<String>();

    private static void scan(File pluginJar, String basePackage) {
        try {
            if (StringUtils.isNotBlank(basePackage) && !alreadyScannedPackages.contains(basePackage)) {
                alreadyScannedPackages.add(basePackage);
                ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
                scanner.setResourceLoader(new PathMatchingResourcePatternResolver());
                scanner.addIncludeFilter(new AnnotationTypeFilter(PluginInfo.class));

                for (BeanDefinition bd : scanner.findCandidateComponents(basePackage)) {
                    addPlugin(bd.getBeanClassName(), pluginJar.getPath());
                }
            }
        } catch (Throwable e) {
            log.error(e);
        }
    }

    private static SketchletPluginHandler addPlugin(String strClass, String jarPath) {
        try {
            Class c = classLoader.loadClass(strClass);
            if (c.isAnnotationPresent(PluginInfo.class)) {
                PluginInfo pi = (PluginInfo) c.getAnnotation(PluginInfo.class);
                return addPlugin(strClass, pi.name(), pi.type(), pi.position() + "", pi.description(), pi.group(), pi.properties(), jarPath);
            }
        } catch (Throwable e) {
            log.error(e);
        }

        return null;
    }

    private static SketchletPluginHandler addPlugin(String strClass, String strName, String strType, String strPos, String strDescription, String group, String properties[], String jarPath) {
        for (SketchletPluginHandler sp : GenericPluginFactory.allPlugins) {
            if (sp.strClass.equalsIgnoreCase(strClass)) {
                SketchletPluginLogger.error("Duplicate plugin definition: " + strClass);
                SketchletPluginLogger.error("    file: " + jarPath);
                return null;
            }
        }
        try {
            Class c = classLoader.loadClass(strClass);
            if (c.isAnnotationPresent(PluginInfo.class)) {
                String value = ((PluginInfo) c.getAnnotation(PluginInfo.class)).name();
                if (!StringUtils.isBlank(value)) {
                    strName = value;
                }
                value = ((PluginInfo) c.getAnnotation(PluginInfo.class)).type();
                if (!StringUtils.isBlank(value)) {
                    strType = value;
                }
            }
        } catch (Throwable e) {
            return null;
        }

        int position = Integer.MAX_VALUE;
        try {
            position = (int) Double.parseDouble(strPos);
        } catch (Exception ex) {
        }

        SketchletPluginHandler plugin = null;
        if (strType.equalsIgnoreCase("script")) {
            plugin = new ScriptPlugin(strName, strType, strClass, strDescription, jarPath, group, position);
            ScriptPluginFactory.scriptPlugins.add((ScriptPlugin) plugin);
        } else if (strType.equalsIgnoreCase("widget")) {
            plugin = new WidgetPluginHandler(strName, strType, strClass, strDescription, jarPath, group, position);
            WidgetPluginFactory.widgetPlugins.add((WidgetPluginHandler) plugin);
        } else if (strType.equalsIgnoreCase("codegen")) {
            plugin = new CodeGenPluginHandler(strName, strType, strClass, strDescription, jarPath, group, position);
            CodeGenPluginFactory.codegenPlugins.add((CodeGenPluginHandler) plugin);
        } else {
            plugin = new GenericPluginHandler(strName, strType, strClass, strDescription, jarPath, group, position);
        }

        for (String property : properties) {
            plugin.addParam(property);
        }
        GenericPluginFactory.allPlugins.add(plugin);
        plugin.prepare();

        PluginLoader.pluginsInfo.append("      - Plugin: " + strType + " / " + plugin.name + " - " + plugin.strClass + "\n");

        return plugin;
    }

    public static List<PluginData> getPluginInfo() {
        List<PluginData> info = new ArrayList<PluginData>();

        for (SketchletPluginHandler handler : GenericPluginFactory.allPlugins) {
            info.add(new PluginData(handler.name, handler.strClass, handler.type, handler.strDescription));
        }

        return info;
    }

    public static String getPluginDir() {
        if (SketchletContext.getInstance() != null) {
            return SketchletContext.getInstance().getApplicationHomeDir() + "bin/plugins/";
        }
        return System.getenv("SKETCHLET_HOME") + "/bin/plugins/";
    }

    public static String getUserPluginDir() {
        if (SketchletContext.getInstance() != null) {
            return SketchletContextUtils.getApplicationSettingsDir() + ".plugins/";
        }
        return null;
    }

    public static void sort(List<SketchletPluginHandler> plugins) {
        Collections.sort(plugins, new Comparator<SketchletPluginHandler>() {

            public int compare(SketchletPluginHandler o1, SketchletPluginHandler o2) {
                if (o1.position < o2.position) {
                    return -1;
                } else if (o1.position > o2.position) {
                    return 1;
                } else {
                    return 0;
                }
            }

            public boolean equals(Object obj) {
                return this == obj;
            }
        });
    }

    public static <T> T getPluginInstance(String type, String pluginName) {
        for (SketchletPluginHandler plugin : GenericPluginFactory.allPlugins) {
            if (plugin.type.equalsIgnoreCase(type) && plugin.name.equalsIgnoreCase(pluginName)) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(PluginLoader.classLoader);
                Object pluginInstance = plugin.getInstance();
                Thread.currentThread().setContextClassLoader(cl);
                return (T) pluginInstance;
            }
        }

        return null;
    }

    public static List<PluginInstance> getPluginInstances(String... types) {
        List<PluginInstance> list = new ArrayList<PluginInstance>();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(PluginLoader.classLoader);
        for (SketchletPluginHandler plugin : GenericPluginFactory.allPlugins) {
            for (String type : types) {
                if (plugin.type.equalsIgnoreCase(type)) {
                    PluginInstance pluginInstance = plugin.getInstance();
                    list.add(pluginInstance);
                }
            }
        }
        Thread.currentThread().setContextClassLoader(cl);

        return list;
    }

    public static List<String> getPluginNames(String... types) {
        List<String> names = new ArrayList<String>();
        for (SketchletPluginHandler plugin : GenericPluginFactory.allPlugins) {
            for (String type : types) {
                if (plugin.type.equalsIgnoreCase(type)) {
                    names.add(plugin.name);
                }
            }
        }

        return names;
    }
}
