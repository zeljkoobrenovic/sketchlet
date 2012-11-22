/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.util;

import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

/**
 *
 * @author zobrenovic
 */
public class Colors {

    private static Hashtable<String, Color> colors = new Hashtable<String, Color>();

    static {
        getColors().put("white", Color.WHITE);
        getColors().put("black", Color.BLACK);
        getColors().put("red", Color.RED);
        getColors().put("blue", Color.BLUE);
        getColors().put("green", Color.GREEN);
        getColors().put("yellow", Color.YELLOW);
        getColors().put("gray", Color.GRAY);
        getColors().put("dark gray", Color.DARK_GRAY);
        getColors().put("light gray", Color.LIGHT_GRAY);
        getColors().put("magenta", Color.MAGENTA);
        getColors().put("orange", Color.ORANGE);
        getColors().put("cyan", Color.CYAN);
        getColors().put("pink", Color.PINK);
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
            return getColors().get(name.toLowerCase());
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

    public static Hashtable<String, Color> getColors() {
        return colors;
    }

    public static void setColors(Hashtable<String, Color> colors) {
        Colors.colors = colors;
    }
}
