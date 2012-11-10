/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import java.util.Vector;
import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.PageContext;

/**
 *
 * @author zobrenovic
 */
public abstract class PageCodeFile extends CodeFile {

    public PageContext pageContext;
    public PagedAppCodeGen application;
    Vector<RegionCodeFragment> regionGenerators = new Vector<RegionCodeFragment>();

    public PageCodeFile(final PagedAppCodeGen application, final PageContext pageContext) {
        super(20); // six sections: header, imports, globals, code, main code, footer
        this.pageContext = pageContext;
        this.application = application;
        for (ActiveRegionContext regionContext : pageContext.getActiveRegions()) {
            RegionCodeFragment rg = this.getRegionGeneratorInstance(regionContext);
            regionGenerators.add(rg);
        }
    }

    public void dispose() {
        this.pageContext = null;
        this.application = null;

        for (RegionCodeFragment rcf : this.regionGenerators) {
            rcf.dispose();
        }

        this.sections.removeAllElements();
        for (ImageFile imgf : this.images) {
            imgf.dispose();
        }
        this.images.removeAllElements();

        this.regionGenerators.removeAllElements();
    }

    public abstract RegionCodeFragment getRegionGeneratorInstance(ActiveRegionContext region);

    public boolean shouldEmbedRegions() {
        return true;
    }

    public void prepare() {
        if (shouldEmbedRegions()) {
            Vector<RegionCodeFragment> embeddedregionGenerators = new Vector<RegionCodeFragment>();
            for (RegionCodeFragment regionGenerator1 : regionGenerators) {
                for (RegionCodeFragment regionGenerator2 : regionGenerators) {
                    if (regionGenerator1 != regionGenerator2 && !regionGenerator1.contains(regionGenerator2)) {
                        if (regionGenerator1.embeddes(regionGenerator2)) {
                            regionGenerator1.embedRegionGenerator(regionGenerator2);
                            embeddedregionGenerators.add(regionGenerator2);
                            regionGenerator2.parent = regionGenerator1;
                        }
                    }
                }
            }
            for (RegionCodeFragment embeddedRegionGenerator : embeddedregionGenerators) {
                this.regionGenerators.remove(embeddedRegionGenerator);
            }
        }

        for (RegionCodeFragment regionGenerator : regionGenerators) {
            regionGenerator.prepare();
        }

        RegionCodeFragment.sort(regionGenerators);

        for (RegionCodeFragment regionGenerator : regionGenerators) {
            regionGenerator.sort();
        }

        for (RegionCodeFragment regionGenerator : regionGenerators) {
            regionGenerator.setParentChildRelations();
        }
    }

    public void generate() {
        this.generateCode(0);
    }

    public void generateCode(int level) {
        for (RegionCodeFragment regionGenerator : this.regionGenerators) {
            regionGenerator.generate(level + 1);
            this.addCodeFragment(regionGenerator, CodeFile.SECTION_BODY);
        }
    }
}
