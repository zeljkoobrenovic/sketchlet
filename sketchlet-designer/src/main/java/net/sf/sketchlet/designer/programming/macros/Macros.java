/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.programming.macros;

import net.sf.sketchlet.common.XPathEvaluator;
import net.sf.sketchlet.common.context.SketchletContextUtils;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class Macros {

    public Vector<Macro> macros = new Vector<Macro>();
    public static Macros globalMacros = new Macros();

    public Macros() {
        load();
    }

    public void load() {
        try {
            macros.removeAllElements();
            File file = new File(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "macros.xml");

            if (file.exists()) {
                XPathEvaluator xp = new XPathEvaluator();
                xp.createDocumentFromFile(file);

                NodeList sketcheNodes = xp.getNodes("/macros/macro");

                if (sketcheNodes != null) {
                    for (int i = 0; i < sketcheNodes.getLength(); i++) {
                        Macro m = new Macro();

                        m.name = xp.getString("/macros/macro[position()=" + (i + 1) + "]/name");
                        if (xp.getString("/macros/macro[position()=" + (i + 1) + "]/repeat").equals("")) {
                            m.repeat = 1;
                        } else {
                            m.repeat = xp.getInteger("/macros/macro[position()=" + (i + 1) + "]/repeat");
                        }
                        if (m.repeat < 0) {
                            m.repeat = 1;
                        }

                        int macroCount = xp.getInteger("count(/macros/macro[position()=" + (i + 1) + "]/action)");

                        for (int j = 0; j < m.actions.length && j < macroCount; j++) {
                            String strCommand = xp.getString("/macros/macro[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/type");
                            strCommand = m.checkOldCommand(strCommand);
                            m.actions[j][0] = strCommand;
                            m.actions[j][1] = xp.getString("/macros/macro[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/param1");
                            m.actions[j][2] = xp.getString("/macros/macro[position()=" + (i + 1) + "]/action[position()=" + (j + 1) + "]/param2");
                        }

                        macros.add(m);
                    }
                }
            } else {
                // this.addNewMacro();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getNewName() {
        int i = macros.size() + 1;
        while (true) {
            String name = "Actions " + i++;
            boolean nameExists = false;
            for (Macro t : this.macros) {
                if (t.name.equals(name)) {
                    nameExists = true;
                }
            }

            if (!nameExists) {
                return name;
            }
        }
    }

    public Macro addNewMacro() {
        Macro m = new Macro();
        m.name = this.getNewName();
        macros.add(m);

        save();

        return m;
    }

    public void save() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(SketchletContextUtils.getCurrentProjectSkecthletsDir() + "macros.xml"));
            out.println("<macros>");
            for (Macro m : this.macros) {
                m.save(out);
            }
            out.println("</macros>");

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Macro getMacro(String strMacroName) {
        for (Macro m : macros) {
            if (m.name.equals(strMacroName)) {
                return m;
            }
        }

        return null;
    }

    public void replaceReferences(String _action, String oldName, String newName) {
        for (Macro m : macros) {
            m.replaceReferences(_action, oldName, newName);
        }
    }

    public boolean isConnected(String strMacroName, String strSketch) {
        for (Macro m : macros) {
            if (m.name.equals(strMacroName)) {
                for (int i = 0; i < m.actions.length; i++) {
                    String action = (String) m.actions[i][0];
                    String param = (String) m.actions[i][1];

                    if (action.equalsIgnoreCase("Go To Page") && param.equals(strSketch)) {
                        return true;
                    }
                }
                break;
            }
        }

        return false;
    }

    public boolean macroExists(String strMacroName) {
        for (Macro m : macros) {
            if (m.name.equalsIgnoreCase(strMacroName)) {
                return true;
            }
        }

        return false;
    }

    public MacroThread startMacroThread(String strMacroName, String strParams, String strVarPrefix, String strVarPostfix) {
        MacroThread mt = null;

        for (Macro m : macros) {
            if (m.name.trim().equalsIgnoreCase(strMacroName.trim())) {
                return new MacroThread(m, strParams, strVarPrefix, strVarPostfix);
            }
        }

        return mt;
    }

    public MacroThread startMacroThreadFromString(String strSequence, String strVarPrefix, String strVarPostfix) {
        return new MacroThread(strSequence, strVarPrefix, strVarPostfix);
    }
}
