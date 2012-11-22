/*
 * Utils.java
 *
 * Created on October 8, 2007, 3:11 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common;

import java.util.Map;
import java.util.Vector;

/**
 * @author cuypers
 */
public class Utils {
    public static String[] getVariablesFromTemplate(String template) {
        Vector<String> variables = new Vector<String>();

        while (true) {
            int pos1 = template.indexOf("<%=");
            int pos2 = template.indexOf("%>");
            try {
                if (pos1 >= 0 && pos2 > pos1) {

                    String variable = template.substring(pos1, pos2 + 2);
                    String variableName = template.substring(pos1 + 3, pos2).trim();
                    String value = " ";

                    template = template.replaceAll(variable, value);

                    variables.add(variableName);
                } else {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }

        String[] _variables = new String[variables.size()];

        for (int i = 0; i < variables.size(); i++) {
            _variables[i] = variables.elementAt(i);
        }

        return _variables;
    }

    public static String replaceSystemVariables(String text) {
        java.util.Map<String, String> env = System.getenv();

        for (Map.Entry variable : env.entrySet()) {
            String name = (String) variable.getKey();
            String value = (String) variable.getValue();
            // System.out.println( variable );

            text = text.replace("%" + name + "%", value);
            text = text.replace("$" + name, value);
        }

        return text;
    }
}
