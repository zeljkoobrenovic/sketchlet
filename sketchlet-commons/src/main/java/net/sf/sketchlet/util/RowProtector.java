/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.util;

import java.util.Vector;

/**
 *
 * @author zobrenovic
 */
public class RowProtector {

    private Vector<Integer> ignoreRows = new Vector<Integer>();

    public void protectRow(int row) {
        this.ignoreRows.add(new Integer(row));
    }

    public void unprotectRow(int row) {
        ignoreRows.remove(new Integer(row));
    }

    public boolean isRowProtected(int row) {
        return this.ignoreRows.contains(row);
    }

    public static void main(String args[]) {
        RowProtector rp = new RowProtector();

        rp.protectRow(1);
        rp.protectRow(2);
        rp.unprotectRow(1);

        System.out.println( rp.isRowProtected(1) );
        System.out.println( rp.isRowProtected(2) );
    }
}
