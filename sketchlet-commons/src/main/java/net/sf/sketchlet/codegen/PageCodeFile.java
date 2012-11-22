/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.codegen;

import net.sf.sketchlet.context.ActiveRegionContext;
import net.sf.sketchlet.context.PageContext;

import java.util.Vector;

/**
 *
 * @author zobrenovic
 */
public abstract class PageCodeFile extends CodeFile {

    private PageContext pageContext;
    private PagedAppCodeGen application;
    private Vector<RegionCodeFragment> regionGenerators = new Vector<RegionCodeFragment>();

    public PageCodeFile(final PagedAppCodeGen application, final PageContext pageContext) {
        super(20); // six sections: header, imports, globals, code, main code, footer
        this.setPageContext(pageContext);
        this.setApplication(application);
        for (ActiveRegionContext regionContext : pageContext.getActiveRegions()) {
            RegionCodeFragment rg = this.getRegionGeneratorInstance(regionContext);
            getRegionGenerators().add(rg);
        }
    }

    public void dispose() {
        this.setPageContext(null);
        this.setApplication(null);

        for (RegionCodeFragment rcf : this.getRegionGenerators()) {
            rcf.dispose();
        }

        this.getSections().removeAllElements();
        for (ImageFile imgf : this.getImages()) {
            imgf.dispose();
        }
        this.getImages().removeAllElements();

        this.getRegionGenerators().removeAllElements();
    }

    public abstract RegionCodeFragment getRegionGeneratorInstance(ActiveRegionContext region);

    public boolean shouldEmbedRegions() {
        return true;
    }

    public void prepare() {
        if (shouldEmbedRegions()) {
            Vector<RegionCodeFragment> embeddedregionGenerators = new Vector<RegionCodeFragment>();
            for (RegionCodeFragment regionGenerator1 : getRegionGenerators()) {
                for (RegionCodeFragment regionGenerator2 : getRegionGenerators()) {
                    if (regionGenerator1 != regionGenerator2 && !regionGenerator1.contains(regionGenerator2)) {
                        if (regionGenerator1.embeddes(regionGenerator2)) {
                            regionGenerator1.embedRegionGenerator(regionGenerator2);
                            embeddedregionGenerators.add(regionGenerator2);
                            regionGenerator2.setParent(regionGenerator1);
                        }
                    }
                }
            }
            for (RegionCodeFragment embeddedRegionGenerator : embeddedregionGenerators) {
                this.getRegionGenerators().remove(embeddedRegionGenerator);
            }
        }

        for (RegionCodeFragment regionGenerator : getRegionGenerators()) {
            regionGenerator.prepare();
        }

        RegionCodeFragment.sort(getRegionGenerators());

        for (RegionCodeFragment regionGenerator : getRegionGenerators()) {
            regionGenerator.sort();
        }

        for (RegionCodeFragment regionGenerator : getRegionGenerators()) {
            regionGenerator.setParentChildRelations();
        }
    }

    public void generate() {
        this.generateCode(0);
    }

    public void generateCode(int level) {
        for (RegionCodeFragment regionGenerator : this.getRegionGenerators()) {
            regionGenerator.generate(level + 1);
            this.addCodeFragment(regionGenerator, CodeFile.SECTION_BODY);
        }
    }

    public PageContext getPageContext() {
        return pageContext;
    }

    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public PagedAppCodeGen getApplication() {
        return application;
    }

    public void setApplication(PagedAppCodeGen application) {
        this.application = application;
    }

    public Vector<RegionCodeFragment> getRegionGenerators() {
        return regionGenerators;
    }

    public void setRegionGenerators(Vector<RegionCodeFragment> regionGenerators) {
        this.regionGenerators = regionGenerators;
    }
}
