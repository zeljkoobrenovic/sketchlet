/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import net.sf.sketchlet.context.ActiveRegionContext;

/**
 *
 * @author zobrenovic
 */
public class RegionCodeFragment extends CodeFragment {

    public ActiveRegionContext regionContext;
    public RegionCodeFragment parent;
    // if a regionContext generator is marked as a container, than it may contain other regions generators
    protected Vector<RegionCodeFragment> embeddedRegionGenerators = new Vector<RegionCodeFragment>();

    public RegionCodeFragment(ActiveRegionContext regionContext, CodeFile container) {
        super(container);
        this.regionContext = regionContext;
        this.indentLevel = 1;
    }

    public void dispose() {
        this.parent = null;
        this.regionContext = null;

        for (RegionCodeFragment rcf : this.embeddedRegionGenerators) {
            rcf.dispose();
        }

        this.codeLines.removeAllElements();
        this.codeFile = null;
        this.embeddedRegionGenerators.removeAllElements();
    }

    @Override
    public void prepare() {
        Vector<RegionCodeFragment> deleteRegionGenerators = new Vector<RegionCodeFragment>();
        for (RegionCodeFragment regionGenerator1 : embeddedRegionGenerators) {
            for (RegionCodeFragment regionGenerator2 : embeddedRegionGenerators) {
                if (regionGenerator1 != regionGenerator2) {
                    if (regionGenerator1.embeddes(regionGenerator2) && !regionGenerator1.contains(regionGenerator2)) {
                        regionGenerator1.embedRegionGenerator(regionGenerator2);
                        deleteRegionGenerators.add(regionGenerator2);
                    }
                }
            }
        }
        for (RegionCodeFragment embeddedRegionGenerator : deleteRegionGenerators) {
            this.embeddedRegionGenerators.remove(embeddedRegionGenerator);
        }

        for (RegionCodeFragment regionGenerator : embeddedRegionGenerators) {
            regionGenerator.prepare();
        }

        removeDoubleEmbededRegions();
    }

    @Override
    public void generate() {
        this.generateCode(this.getLevel());
    }

    public int getLevel() {
        if (this.parent != null) {
            return this.parent.getLevel() + this.indentLevel + 1;
        } else {
            return this.indentLevel;
        }
    }

    public void generateCode(int level) {
        this.setIndentLevel(level);
        //StringBuffer result = new StringBuffer("");
        if (!this.isAtomic()) {
            this.generateFragmentHeader();
            for (RegionCodeFragment regionGenerator : this.embeddedRegionGenerators) {
                regionGenerator.generateCode(level + 1);
                this.appendLine(regionGenerator.toString());
            }
            this.generateFragmentFooter();
        } else {
            this.generateFragmentContent();
        }

        //return result.toString();
    }

    public boolean isAtomic() {
        return this.embeddedRegionGenerators.size() == 0;
    }

    public void sort() {
        RegionCodeFragment.sort(this.embeddedRegionGenerators);
        for (RegionCodeFragment regionGenerator : embeddedRegionGenerators) {
            regionGenerator.sort();
        }
    }

    public void setParentChildRelations() {
        for (RegionCodeFragment regionGenerator : embeddedRegionGenerators) {
            regionGenerator.parent = this;
            regionGenerator.setParentChildRelations();
        }
    }

    public void removeDoubleEmbededRegions() {
        Vector<RegionCodeFragment> deleteRegionGenerators = new Vector<RegionCodeFragment>();
        for (RegionCodeFragment regionGenerator : embeddedRegionGenerators) {
            if (this.containsAsChild(regionGenerator)) {
                deleteRegionGenerators.add(regionGenerator);
            }
        }

        for (RegionCodeFragment embeddedRegionGenerator : deleteRegionGenerators) {
            this.embeddedRegionGenerators.remove(embeddedRegionGenerator);
        }
    }

    public boolean embeddes(RegionCodeFragment regionGenerator) {
        Rectangle2D r1 = new Rectangle2D.Double(this.regionContext.getX1(), this.regionContext.getY1(), this.regionContext.getWidth(), this.regionContext.getHeight());
        Rectangle2D r2 = new Rectangle2D.Double(regionGenerator.regionContext.getX1(), regionGenerator.regionContext.getY1(), regionGenerator.regionContext.getWidth(), regionGenerator.regionContext.getHeight());
        return r1.contains(r2);
    }

    /**
     * 
     * @return active regionContext of this generator
     */
    public ActiveRegionContext getRegionContext() {
        return this.regionContext;
    }

    public void embedRegionGenerator(RegionCodeFragment childRegionGenerator) {
        if (!this.embeddedRegionGenerators.contains(childRegionGenerator)) {
            this.embeddedRegionGenerators.add(childRegionGenerator);
        }
    }

    public boolean contains(RegionCodeFragment regionGenerator) {
        return !this.regionContext.getProperty("type").isEmpty() && this.embeddedRegionGenerators.contains(regionGenerator);
    }

    public boolean containsAsChild(RegionCodeFragment regionGenerator) {
        for (RegionCodeFragment embededRegionGenerator : this.embeddedRegionGenerators) {
            if (regionGenerator != embededRegionGenerator) {
                if (embededRegionGenerator.contains(regionGenerator) || embededRegionGenerator.containsAsChild(regionGenerator)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void generateFragmentHeader() {
    }

    public void generateFragmentContent() {
    }

    public void generateFragmentFooter() {
    }

    public String getRegionImageFileNameWithoutExtension() {
        return regionContext.getName();
    }

    public boolean shouldSortTopToBottom() {
        return true;
    }

    public BufferedImage getImage() {
        BufferedImage img = regionContext.getImage(0);
        /*if (img == null) {
        img = SketchletContext.getInstance().createCompatibleImage(regionContext.getWidth(), regionContext.getHeight());
        }*/
        return img;
    }

    public static void sort(Vector<RegionCodeFragment> regionGenerators) {
        Collections.sort(regionGenerators, new Comparator<RegionCodeFragment>() {

            public int compare(RegionCodeFragment r1, RegionCodeFragment r2) {
                boolean bTopToBottom = true;

                if (r1.parent != null && r1.parent == r2.parent) {
                    bTopToBottom = r1.parent.shouldSortTopToBottom();
                }

                if (bTopToBottom) {
                    if (r1.getRegionContext().getY1() < r1.getRegionContext().getY2()) {
                        return 1;
                    } else if (r1.getRegionContext().getY1() > r1.getRegionContext().getY2()) {
                        return -1;
                    } else {
                        if (r1.getRegionContext().getX1() < r1.getRegionContext().getX2()) {
                            return -1;
                        } else if (r1.getRegionContext().getX1() > r1.getRegionContext().getX2()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                } else {
                    if (r1.getRegionContext().getX1() < r1.getRegionContext().getX2()) {
                        return 1;
                    } else if (r1.getRegionContext().getX1() > r1.getRegionContext().getX2()) {
                        return -1;
                    } else {
                        if (r1.getRegionContext().getY1() < r1.getRegionContext().getY2()) {
                            return -1;
                        } else if (r1.getRegionContext().getY1() > r1.getRegionContext().getY2()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                }
            }
        });
    }
}
