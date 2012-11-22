/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import net.sf.sketchlet.context.ActiveRegionContext;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/**
 *
 * @author zobrenovic
 */
public class RegionCodeFragment extends CodeFragment {

    private ActiveRegionContext regionContext;
    private RegionCodeFragment parent;
    // if a regionContext generator is marked as a container, than it may contain other regions generators
    private Vector<RegionCodeFragment> embeddedRegionGenerators = new Vector<RegionCodeFragment>();

    public RegionCodeFragment(ActiveRegionContext regionContext, CodeFile container) {
        super(container);
        this.setRegionContext(regionContext);
        this.setIndentLevel(1);
    }

    public void dispose() {
        this.setParent(null);
        this.setRegionContext(null);

        for (RegionCodeFragment rcf : this.getEmbeddedRegionGenerators()) {
            rcf.dispose();
        }

        this.getCodeLines().removeAllElements();
        this.setCodeFile(null);
        this.getEmbeddedRegionGenerators().removeAllElements();
    }

    @Override
    public void prepare() {
        Vector<RegionCodeFragment> deleteRegionGenerators = new Vector<RegionCodeFragment>();
        for (RegionCodeFragment regionGenerator1 : getEmbeddedRegionGenerators()) {
            for (RegionCodeFragment regionGenerator2 : getEmbeddedRegionGenerators()) {
                if (regionGenerator1 != regionGenerator2) {
                    if (regionGenerator1.embeddes(regionGenerator2) && !regionGenerator1.contains(regionGenerator2)) {
                        regionGenerator1.embedRegionGenerator(regionGenerator2);
                        deleteRegionGenerators.add(regionGenerator2);
                    }
                }
            }
        }
        for (RegionCodeFragment embeddedRegionGenerator : deleteRegionGenerators) {
            this.getEmbeddedRegionGenerators().remove(embeddedRegionGenerator);
        }

        for (RegionCodeFragment regionGenerator : getEmbeddedRegionGenerators()) {
            regionGenerator.prepare();
        }

        removeDoubleEmbededRegions();
    }

    @Override
    public void generate() {
        this.generateCode(this.getLevel());
    }

    public int getLevel() {
        if (this.getParent() != null) {
            return this.getParent().getLevel() + this.getIndentLevel() + 1;
        } else {
            return this.getIndentLevel();
        }
    }

    public void generateCode(int level) {
        this.setIndentLevel(level);
        //StringBuffer result = new StringBuffer("");
        if (!this.isAtomic()) {
            this.generateFragmentHeader();
            for (RegionCodeFragment regionGenerator : this.getEmbeddedRegionGenerators()) {
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
        return this.getEmbeddedRegionGenerators().size() == 0;
    }

    public void sort() {
        RegionCodeFragment.sort(this.getEmbeddedRegionGenerators());
        for (RegionCodeFragment regionGenerator : getEmbeddedRegionGenerators()) {
            regionGenerator.sort();
        }
    }

    public void setParentChildRelations() {
        for (RegionCodeFragment regionGenerator : getEmbeddedRegionGenerators()) {
            regionGenerator.setParent(this);
            regionGenerator.setParentChildRelations();
        }
    }

    public void removeDoubleEmbededRegions() {
        Vector<RegionCodeFragment> deleteRegionGenerators = new Vector<RegionCodeFragment>();
        for (RegionCodeFragment regionGenerator : getEmbeddedRegionGenerators()) {
            if (this.containsAsChild(regionGenerator)) {
                deleteRegionGenerators.add(regionGenerator);
            }
        }

        for (RegionCodeFragment embeddedRegionGenerator : deleteRegionGenerators) {
            this.getEmbeddedRegionGenerators().remove(embeddedRegionGenerator);
        }
    }

    public boolean embeddes(RegionCodeFragment regionGenerator) {
        Rectangle2D r1 = new Rectangle2D.Double(this.getRegionContext().getX1(), this.getRegionContext().getY1(), this.getRegionContext().getWidth(), this.getRegionContext().getHeight());
        Rectangle2D r2 = new Rectangle2D.Double(regionGenerator.getRegionContext().getX1(), regionGenerator.getRegionContext().getY1(), regionGenerator.getRegionContext().getWidth(), regionGenerator.getRegionContext().getHeight());
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
        if (!this.getEmbeddedRegionGenerators().contains(childRegionGenerator)) {
            this.getEmbeddedRegionGenerators().add(childRegionGenerator);
        }
    }

    public boolean contains(RegionCodeFragment regionGenerator) {
        return !this.getRegionContext().getProperty("type").isEmpty() && this.getEmbeddedRegionGenerators().contains(regionGenerator);
    }

    public boolean containsAsChild(RegionCodeFragment regionGenerator) {
        for (RegionCodeFragment embededRegionGenerator : this.getEmbeddedRegionGenerators()) {
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
        return getRegionContext().getName();
    }

    public boolean shouldSortTopToBottom() {
        return true;
    }

    public BufferedImage getImage() {
        BufferedImage img = getRegionContext().getImage(0);
        /*if (img == null) {
        img = SketchletContext.getInstance().createCompatibleImage(regionContext.getWidth(), regionContext.getHeight());
        }*/
        return img;
    }

    public static void sort(Vector<RegionCodeFragment> regionGenerators) {
        Collections.sort(regionGenerators, new Comparator<RegionCodeFragment>() {

            public int compare(RegionCodeFragment r1, RegionCodeFragment r2) {
                boolean bTopToBottom = true;

                if (r1.getParent() != null && r1.getParent() == r2.getParent()) {
                    bTopToBottom = r1.getParent().shouldSortTopToBottom();
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

    public void setRegionContext(ActiveRegionContext regionContext) {
        this.regionContext = regionContext;
    }

    public RegionCodeFragment getParent() {
        return parent;
    }

    public void setParent(RegionCodeFragment parent) {
        this.parent = parent;
    }

    public Vector<RegionCodeFragment> getEmbeddedRegionGenerators() {
        return embeddedRegionGenerators;
    }

    public void setEmbeddedRegionGenerators(Vector<RegionCodeFragment> embeddedRegionGenerators) {
        this.embeddedRegionGenerators = embeddedRegionGenerators;
    }
}
