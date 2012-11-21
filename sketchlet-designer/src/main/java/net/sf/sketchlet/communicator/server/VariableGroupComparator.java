/*
 * Variable.java
 *
 * Created on April 23, 2008, 2:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package net.sf.sketchlet.communicator.server;

import java.util.Comparator;

/**
 * @author cuypers
 */
public class VariableGroupComparator implements Comparator {

    /**
     * Creates a new instance of Variable
     */
    public VariableGroupComparator() {
    }

    public int compare(Object o1, Object o2) {
        if (o1 instanceof Variable && o2 instanceof Variable) {
            return ((Variable) o1).getGroup().compareTo(((Variable) o2).getGroup());
        } else {
            return 0;
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof VariableGroupComparator) {
            return obj == this;
        } else {
            return false;
        }
    }
}
