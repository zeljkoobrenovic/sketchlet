/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import java.util.Hashtable;

/**
 *
 * @author zobrenovic
 */
public class CodeGenUtils {

    public static final String[][] STANDARD_WIDGET_IDs = {
        {"group", "", ""},
        {"vertical group", "", ""},
        {"horizontal group", "", ""},
        {"tiled group", "", ""},
        {"tab group", "", ""},
        {"switch group", "", ""},
        
        {"form", "", ""},
        
        {"text label", "", ""},
        {"rich text label", "", ""},
        {"button", "", ""},
        {"radio button", "", ""},
        {"toggle button", "", ""},
        {"checkbox", "", ""},
        {"text field", "", ""},
        {"text area", "", ""},
        {"rich text area", "", ""},
        {"list", "", ""},
        {"dropdown list", "", ""},
        {"hslider", "", ""},
        {"vslider", "", ""},
        {"table", "", ""},
        {"menu", "", ""},
        {"tree", "", ""},
        
        {"image", "", ""},
        {"graphics", "", ""},};
    public static Hashtable<String, String[]> extraControls = new Hashtable<String, String[]>();

    public static String[] getControlTypeIDs() {
        String typeIDs[] = new String[STANDARD_WIDGET_IDs.length];
        for (int i = 0; i < STANDARD_WIDGET_IDs.length; i++) {
            typeIDs[i] = STANDARD_WIDGET_IDs[i][0];
        }

        return typeIDs;
    }

    public static String getTabSpaces(int level) {
        StringBuffer str = new StringBuffer("");

        for (int i = 0; i < level; i++) {
            str.append("    ");
        }

        return str.toString();
    }

    public static String getJavaIdentifier(String str, boolean bClass) {
        str = str.replace("-", " ");
        str = str.replace("/", " ");
        str = str.replace(".", " ");
        String a[] = str.split(" ");

        StringBuffer buff = new StringBuffer("");
        for (int i = 0; i < a.length; i++) {
            String s = a[i];
            if (s.isEmpty()) {
                continue;
            }
            if (i == 0 && !bClass) {
                buff.append(s.toLowerCase());
            } else {
                if (s.length() > 1) {
                    buff.append(s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase());
                } else {
                    buff.append(s.toUpperCase());
                }
            }
        }

        return buff.toString();
    }

    public static String getSQLIdentifier(String str) {
        str = str.replace("-", " ");
        str = str.replace("/", " ");
        str = str.replace(".", " ");
        str = str.replace(" ", "_");

        return str.toUpperCase();
    }

    public static String getActionScriptIdentifier(String str) {
        str = str.replace("-", " ");
        str = str.replace("/", " ");
        str = str.replace(".", " ");
        str = str.replace(" ", "_");

        return str;
    }

    public static String getTypeByValue(String strValue) {
        try {
            Integer.parseInt(strValue);
            return "integer";
        } catch (Exception e) {
        }

        try {
            Double.parseDouble(strValue);
            return "decimal";
        } catch (Exception e) {
        }

        return "string";
    }

    public static String getJavaTypeByValue(String strValue) {
        String type = getTypeByValue(strValue);

        if (type.equalsIgnoreCase("integer")) {
            return "int";
        } else if (type.equalsIgnoreCase("decimal")) {
            return "double";
        } else {
            return "String";
        }
    }

    public static String getActionScriptTypeByValue(String strValue) {
        String type = getTypeByValue(strValue);

        if (type.equalsIgnoreCase("integer")) {
            return "int";
        } else if (type.equalsIgnoreCase("decimal")) {
            return "Number";
        } else {
            return "String";
        }
    }

    public static String getSQLTypeByValue(String strValue) {
        String type = getTypeByValue(strValue);

        if (type.equalsIgnoreCase("integer")) {
            return "INTEGER";
        } else if (type.equalsIgnoreCase("decimal")) {
            return "DOUBLE";
        } else {
            return "VARCHAR(255)";
        }
    }

    public static String getHibernateTypeByValue(String strValue) {
        String type = getTypeByValue(strValue);

        if (type.equalsIgnoreCase("integer")) {
            return "int";
        } else if (type.equalsIgnoreCase("decimal")) {
            return "double";
        } else {
            return "string";
        }
    }

    public static String getJavaLiteralExpressionByValue(String strValue) {
        String strType = getJavaTypeByValue(strValue);
        if (strType.equalsIgnoreCase("String")) {
            return "\"" + strValue + "\"";
        } else {
            return strValue;
        }

    }

    public static String getActionScriptLiteralExpressionByValue(String strValue) {
        String strType = getActionScriptTypeByValue(strValue);
        if (strType.equalsIgnoreCase("String")) {
            return "\"" + strValue + "\"";
        } else {
            return strValue;
        }

    }

    public static void main(String args[]) {
        System.out.println(CodeGenUtils.getJavaIdentifier("a bb ccc", false));
        System.out.println(CodeGenUtils.getJavaIdentifier("zeljko-obren", true));
        System.out.println(CodeGenUtils.getJavaIdentifier("z.kllo/ko", true));
    }
}
