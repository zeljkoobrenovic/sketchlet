/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.util;

import java.awt.Color;
import java.util.Hashtable;
import javax.swing.JComboBox;

/**
 *
 * @author zobrenovic
 */
public class Colors {

    static Hashtable<String, Color> colors = new Hashtable<String, Color>();

    static {
        colors.put("white", Color.WHITE);
        colors.put("black", Color.BLACK);
        colors.put("red", Color.RED);
        colors.put("blue", Color.BLUE);
        colors.put("green", Color.GREEN);
        colors.put("yellow", Color.YELLOW);
        colors.put("gray", Color.GRAY);
        colors.put("dark gray", Color.DARK_GRAY);
        colors.put("light gray", Color.LIGHT_GRAY);
        colors.put("magenta", Color.MAGENTA);
        colors.put("orange", Color.ORANGE);
        colors.put("cyan", Color.CYAN);
        colors.put("pink", Color.PINK);
    }

    public static Color getColor(String name, Color defaultColor) {
        Color c = getColor(name);
        return c != null ? c : defaultColor;
    }

    public static Color getColor(String name) {
        if (name == null || name.length() == 0) {
            return null;
        } else if (name.charAt(0) >= '0' && name.charAt(0) <= '9') {
            try {
                name = name.replace(",", " ").trim();
                name = name.replace(";", " ").trim();
                String params[] = name.split(" ");

                if (params.length >= 3) {
                    int r = (int) Double.parseDouble(params[0]);
                    int g = (int) Double.parseDouble(params[1]);
                    int b = (int) Double.parseDouble(params[2]);
                    if (params.length == 4) {
                        int a = (int) Double.parseDouble(params[3]);
                        return new Color(r, g, b, a);
                    } else {
                        return new Color(r, g, b);
                    }

                }
            } catch (Exception e) {
            }
        } else {
            return colors.get(name.toLowerCase());
        }

        return null;
    }

    public static String[] getStandardColorNames() {
        return new String[]{"white", "black", "red",
                    "blue", "green", "yellow", "gray",
                    "dark_gray", "orange", "cyan", "pink"};
    }

    public static Color[] getStandardColors() {
        return new Color[]{Color.WHITE, Color.BLACK, Color.RED,
                    Color.BLUE, Color.GREEN, Color.YELLOW, Color.GRAY,
                    Color.DARK_GRAY, Color.ORANGE, Color.CYAN, Color.PINK};
    }

    public static void addColorNamesToCombo(JComboBox combo) {
        combo.addItem("white");
        combo.addItem("black");
        combo.addItem("red");
        combo.addItem("blue");
        combo.addItem("green");
        combo.addItem("yellow");
        combo.addItem("gray");
        combo.addItem("dark gray");
        combo.addItem("light gray");
        combo.addItem("magenta");
        combo.addItem("orange");
        combo.addItem("cyan");
        combo.addItem("pink");
    }
}
