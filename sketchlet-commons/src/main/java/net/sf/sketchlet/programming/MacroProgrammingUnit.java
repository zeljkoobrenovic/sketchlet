/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.programming;

/**
 *
 * @author zobrenovic
 */
public class MacroProgrammingUnit extends ProgrammingUnit {
    private int repeat = 1;
    private String name = "";

    private Object[][] actions = {
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},
        {"", "", ""},};
    
    private int[] levels = new int[getActions().length];

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object[][] getActions() {
        return actions;
    }

    public void setActions(Object[][] actions) {
        this.actions = actions;
    }

    public int[] getLevels() {
        return levels;
    }

    public void setLevels(int[] levels) {
        this.levels = levels;
    }
}
