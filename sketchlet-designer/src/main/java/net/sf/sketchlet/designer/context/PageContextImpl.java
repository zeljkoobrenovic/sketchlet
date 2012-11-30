package net.sf.sketchlet.designer.context;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.PageContext;
import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.PageVariable;
import net.sf.sketchlet.framework.model.Page;
import net.sf.sketchlet.util.Colors;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class PageContextImpl extends PageContext {

    private Page page;

    public PageContextImpl(Page page) {
        this.page = page;
    }

    @Override
    public String getTitle() {
        return page.getTitle();
    }

    @Override
    public List<ActiveRegionContext> getActiveRegions() {
        Vector<ActiveRegionContext> regionsContext = new Vector<ActiveRegionContext>();
        for (ActiveRegion region : page.getRegions().getRegions()) {
            regionsContext.add(new ActiveRegionContextImpl(region, this));
        }

        return regionsContext;
    }

    @Override
    public ActiveRegionContext getActiveRegion(int index) {
        return new ActiveRegionContextImpl(page.getRegions().getRegionByNumber(index), this);
    }

    @Override
    public ActiveRegionContext getActiveRegion(String name) {
        return new ActiveRegionContextImpl(page.getRegions().getRegionByName(name), this);
    }

    @Override
    public SketchletContext getSketchletContext() {
        return SketchletContext.getInstance();
    }

    @Override
    public String getProperty(String strProperty) {
        return this.page.getPropertyValue(strProperty);
    }

    @Override
    public void setProperty(String strProperty, String strValue) {
        this.page.setProperty(strProperty, strValue);
    }

    @Override
    public Color getBackgroundColor() {
        Color color = Colors.getColor(this.getProperty("background color"));
        if (color == null) {
            return new Color(0, 0, 0, 0);
        }

        return color;
    }

    @Override
    public BufferedImage getImage(int layer) {
        File file = this.page.getLayerImageFile(layer);
        try {
            return ImageIO.read(file);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public int getWidth() {
        return this.page.getPageWidth();
    }

    @Override
    public int getHeight() {
        return this.page.getPageHeight();
    }

    @Override
    public int getImageLayersCount() {
        return Page.NUMBER_OF_LAYERS;
    }

    @Override
    public List<String> getPropertyNames() {
        List<String> list = new ArrayList<String>();

        return list;
    }

    @Override
    public List<String> getPageVariableNames() {
        List<String> variables = new ArrayList<String>();

        for (PageVariable variable : page.getPageVariables()) {
            variables.add(variable.getName());
        }

        return variables;
    }

    @Override
    public void addPageVariable(String name, String value) {
        page.getPageVariables().add(new PageVariable(name, value));
    }

    @Override
    public void setPageVariableValue(String name, String value) {
        for (PageVariable variable : page.getPageVariables()) {
            if (variable.getName().equalsIgnoreCase(name)) {
                variable.setValue(value);
                return;
            }
        }
    }

    @Override
    public String getPageVariableValue(String name) {
        for (PageVariable variable : page.getPageVariables()) {
            if (variable.getName().equalsIgnoreCase(name)) {
                return variable.getValue();
            }
        }

        return "";
    }

    @Override
    public int getPageVariableCount() {
        return page.getPageVariables().size();
    }

    @Override
    public void deletePageVariable(String name) {
        int index = -1;
        for (int i = 0; i < page.getPageVariables().size(); i++) {
            if (page.getPageVariables().get(i).getName().equalsIgnoreCase(name)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            page.getPageVariables().remove(index);
        }
    }

    @Override
    public String getPageVariableFormat(String name) {
        for (PageVariable variable : page.getPageVariables()) {
            if (variable.getName().equalsIgnoreCase(name)) {
                return variable.getFormat();
            }
        }

        return "";
    }

    @Override
    public void setPageVariableFormat(String name, String value) {
        for (PageVariable variable : page.getPageVariables()) {
            if (variable.getName().equalsIgnoreCase(name)) {
                variable.setFormat(value);
                return;
            }
        }
    }
}
