/*
 * VariableUpdateListener.java
 *
 * Created on 24 February 2006, 13:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
package net.sf.sketchlet.context;

/**
 * The listener interface for receiving page events. 
 * 
 * The class that is interested in page events, 
 * should implement this interface, and register it using the <tt>SketchletContext#addPageEventsListener()</tt> method.
 * An example:
 * <pre>
 * SketchletContext context = SketchletContext.getInstance();
 * context.addPageEventsListener(new PageEventsListener() {
 *     public void afterPageEntry(PageContext pageContext) {
 *         System.out.prinltn("The user has entered the page '" + pageContext.getName() + "'");
 *     }
 *     public void beforePageEntry(PageContext pageContext) {
 *         System.out.prinltn("The user has left the page '" + pageContext.getName() + "'");
 *     }
 * });
 * </pre>
 * 
 * @author Zeljko Obrenovic
 */
public interface PageEventsListener {

    /**
     * This method is called each time when after a user has made a transition to a page.
     *      
     * @param pageContext the context of the page
     */
    public void afterPageEntry(PageContext pageContext);


    /**
     * This method is called each time before a user goes to another page.
     *      
     * @param pageContext the context of the page
     */
    public void beforePageExit(PageContext pageContext);
}
