package net.sf.sketchlet.framework.model;

import javax.swing.*;
import java.awt.*;

/**
 * @author zobrenovic
 */
public interface PropertiesInterface {

    public void setProperty(String name, String value);

    public String getProperty(String name);

    public String getPropertyValue(String name);

    public String getPropertyDescription(String name);

    public JComboBox getPropertiesCombo();

    public int getPropertiesCount();

    public String[][] getData();

    public int getPropertyRow(String strProperty);

    public void repaintProperties();

    public String getDefaultValue(String strProperty);

    public String getMinValue(String strProperty);

    public String getMaxValue(String strProperty);

    public String getTransferString(String strProperty);

    public void logPropertyUpdate(String name, String value, Component source);
}
