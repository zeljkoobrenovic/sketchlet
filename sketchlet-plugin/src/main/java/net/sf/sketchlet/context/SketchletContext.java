/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.context;

import java.util.List;
import javax.swing.JFrame;

/**
 * Provides a general interface to Sketchlet Builder's functionality. Through
 * this class you can get and change Sketchlet Builder settings, and control its
 * execution.
 *
 * <p>This is an abstract class. Its implementation is provided by Sketchlet
 * Builder. You can get an instance of this class using
 * <tt>SketchletContent.getInstance()</tt>. For example:</p>
 *
 * <pre>
 *     SketchletContext context = SketchletContext.getInstance();
 *     System.out.println("The project '" + context.getCurrentProjectName() + "' has " + context.getPageCount() + " pages.");
 * </pre>
 *
 * @author zobrenovic
 */
public abstract class SketchletContext {

    private static SketchletContext context;
    private boolean inBatchMode = false;

    /**
     * Returns the implementation object of this abstract class. This is a
     * singleton method, and all modules will get the same reference.
     *
     * @return the reference to the implementation object
     */
    public static SketchletContext getInstance() {
        return context;
    }

    public boolean isInBatchMode() {
        return inBatchMode;
    }

    public void setBatchMode(boolean mode) {
        inBatchMode = mode;
    }

    /**
     * @deprecated
     */
    public abstract SketchletContext getInstance(Object script);

    /**
     * Sets the implementation object of this abstract class. This is done by
     * Sketchlet Designer.
     *
     * @param contextImpl the reference to the implementation object
     */
    public static void setInstance(SketchletContext contextImpl) {
        context = contextImpl;
    }

    /**
     * Returns the installation directory of Sketchlet Designer.
     *
     * @return the installation directory of Sketchlet Designer
     */
    public abstract String getApplicationHomeDir();

    /**
     * Returns the name of the current project.
     *
     * @return the current project's name
     */
    public abstract String getCurrentProjectName();

    /**
     * Returns the directory of the current project. Each project has its own
     * directory where all the data related to the project are stored.
     *
     * @return the directory of the current project
     */
    public abstract String getCurrentProjectDirectory();

    /**
     * Returns the sketchlet directory of the user.
     *
     * @return the directory of the current project
     */
    public abstract String getUserDirectory();

    /**
     * Return the class loader used to load plugins and its libraries.
     *
     * @return the class loader used to load plugins and their libraries
     */
    public abstract ClassLoader getPluginClassLoader();

    /**
     * Returns the number of pages in the current project.
     *
     * @return the number of pages in the current project
     */
    public abstract int getPageCount();

    /**
     * Returns the list with all the pages in the current project. The project
     * always has at least one page, so this list should never be empty.
     *
     * @return the list of pages from the current project
     */
    public abstract List<PageContext> getPages();

    /**
     * Returns the current page context of the current project. In the project
     * only one page at the time may be active, and this method return an
     * instance of this page's context.
     *
     * @return the current page's context
     */
    public abstract PageContext getCurrentPageContext();

    /**
     * Return true if the application is ready. This means that Sketchlet
     * Designer has created and initialized all necessary objects.
     *
     * @return true if the application is ready
     */
    public abstract boolean isApplicationReady();

    /**
     * Causes transition to another page, making it a current page. If the page
     * with the given name does not exist, or if the page is already the current
     * page, nothing happens.
     *
     * @param name the page name
     */
    public abstract void goToPage(String name);

    /**
     * Starts the macro with the given name. If the macro with the given name
     * does not exist, nothing happens. If the macro with the given name is
     * already running it will be stopped, and restarted.
     *
     * @param name the name of the macro
     */
    public abstract void startMacro(String name);

    /**
     * Stops the macro with the given name. If the macro with the given name
     * does not exist or is not running, nothing happens.
     *
     * @param name the name of the macro
     */
    public abstract void stopMacro(String name);

    /**
     * Starts a simple sequence of events. For example, the sequence
     * <pre>"a=1;b=2;start macro macro1; pause 2; go to page Start;"</pre> is a
     * sequence of five comments equivalent to:
     * <pre>
     * updateVariable("a", "1");
     * updateVariable("b", "2");
     * startMacro("Macro1");
     * pause(2);
     * goToPage("Start");
     * </pre>
     *
     * @param sequence the simple sequence of events
     */
    public abstract void startCommandSequence(String sequence);

    /**
     * Starts the timer with the given name. If the timer with the given name
     * does not exist, nothing happens. If the timer with the given name is
     * already running it will be stopped, and restarted.
     *
     * @param name the name of the timer
     */
    public abstract void startTimer(String name);

    /**
     * Pauses the timer with the given name. The execution of the timer may be
     * resumed using <tt>startTimer()</tt> method. If the timer with the given
     * name does not exist or is not running, nothing happens.
     *
     * @param name the name of the timer
     */
    public abstract void pauseTimer(String name);

    /**
     * Stops the timer with the given name. If the timer with the given name
     * does not exist or is not running, nothing happens.
     *
     * @param name the name of the timer
     */
    public abstract void stopTimer(String name);

    /**
     * Pauses the execution of the current Thread for a given number of seconds.
     *
     * @param durationSeconds the duration of the pause in seconds
     */
    public abstract void pause(double durationSeconds);

    /**
     * Pauses the execution of the current thread until the variable with the
     * given name is updated.
     *
     * @param variable the name of the variable
     */
    public abstract void waitForVariableUpdate(String variable);

    /**
     * Pauses the execution of the current thread until the given expression is
     * true. For example, <tt>waitForVariableUpdate("a > b+ 2")</tt>>, will
     * pause the execution until the value of the variable a is bigger than the
     * value of variable b plus 2.
     *
     * @param expression the expression to be evaluated
     */
    public abstract void waitUntilExpressionTrue(String expression);

    /**
     * Prompts the user with a dialog box in which the user may enter the
     * textual data.
     *
     * @param question the text to be displayed to the user in the dialog
     * @return the user textual answer or <tt>null</tt> if the user presses
     * cancel
     */
    public abstract String ask(String question);

    /**
     * Prompts the user with a dialog box in which the user may enter the
     * integer data. For example:
     * <pre>
     * SketchifyContext context = SketchifyContext.getInstance();
     * int a = context.askInteger("Enter the value for a");
     * int b = context.askInteger("Enter the value for b");
     * context.showMessage("a + b = " + (a + b));
     * </pre>
     *
     * @param question the text to be displayed to the user in the dialog
     * @return the user answer converted to integer;
     */
    public abstract int askInteger(String question);

    /**
     * Prompts the user with a dialog box in which the user may enter the
     * decimal numeric data.
     *
     * @param question the text to be displayed to the user in the dialog
     * @return the user answer converted to double;
     */
    public abstract double askDouble(String question);

    /**
     * Prompts the user with a dialog box in which the user may select the file.
     *
     * @param title the title of the file chooser dialog
     * @return the full path of the selected file or <tt>null</tt> if user
     * presses cancel
     */
    public abstract String askFile(String title);

    /**
     * Prompts the user with a dialog box in which the user may select the
     * folder.
     *
     * @param title the title of the file chooser dialog
     * @return the full path of the selected folder or <tt>null</tt> if user
     * presses cancel
     */
    public abstract String askFolder(String title);

    /**
     * Prompts the user with a dialog box in which the user may select the file
     * or folder.
     *
     * @param title the title of the file chooser dialog
     * @return the full path of the selected file/folder or <tt>null</tt> if
     * user presses cancel
     */
    public abstract String askFileOrFolder(String title);

    /**
     * Prompts the user with a textual message. Blocks the execution until the
     * user closes the message box.
     *
     * @param message the message to be shown to the user
     */
    public abstract void showMessage(String message);

    /**
     * Updates the variable with the given name. This is an auxiliary method,
     * equivalent to
     * <tt>VariablesBlackboardContext.getInstance().updateVariable()</tt>
     *
     * @param name the name of the variable
     * @param value the new value of the variable
     * @see VariablesBlackboardContext#updateVariable(String, String)
     */
    public abstract void updateVariable(String name, String value);

    /**
     * Returns the value of the variable with the given name. This is an
     * auxiliary method, equivalent to
     * <tt>VariablesBlackboardContext.getInstance().getVariableValue()</tt>
     *
     * @param name the name of the variable
     * @return the value of the variable
     * @see VariablesBlackboardContext#getVariableValue(String)
     */
    public abstract String getVariableValue(String variable);

    /**
     * Returns the value of the variable converted to integer.
     *
     *
     * @param name the name of the variable
     * @return the value of the variable converted to integer
     * @see #getVariableValue(String)
     */
    public abstract int getVariableValueAsInteger(String variable);

    /**
     * Returns the value of the variable converted to float.
     *
     *
     * @param name the name of the variable
     * @return the value of the variable converted to float
     * @see #getVariableValue(String)
     */
    public abstract float getVariableValueAsFloat(String variable);

    /**
     * Returns the value of the variable converted to double.
     *
     *
     * @param name the name of the variable
     * @return the value of the variable converted to double
     * @see #getVariableValue(String)
     */
    public abstract double getVariableValueAsDouble(String variable);

    /**
     * Sets the value of the global property. If the global property does not
     * exist it will be created. Global properties are saved and shared among
     * projects.
     *
     * @param name the name of the property
     * @param value the new value of the property
     */
    public abstract void setGlobalProperty(String name, String value);

    /**
     * Returns the value of the global property.
     *
     * @param name the name of the global property
     * @return the value of the global property
     */
    public abstract String getGlobalProperty(String name);

    /**
     * Causes Sketchlet Builder to repaint the current page.
     */
    public abstract void repaint();

    /**
     * Request the focus for the playback or editor panel.
     */
    public abstract void requestFocus();

    /**
     * Adds the page events listener.
     *
     * @param listener the instance of the page events listener
     */
    public abstract void addPageEventsListener(PageEventsListener listener);

    /**
     * Removes the page events listener.
     *
     * @param listener the instance of the page events listener
     */
    public abstract void removePageEventsListener(PageEventsListener listener);

    /**
     * Returns the the reference to the Sketchlet Builder main frame.
     *
     * @return the main frame
     */
    public abstract JFrame getMainFrame();

    /**
     * Returns the the reference to the Sketchlet Builder editor frame.
     *
     * @return the editor frame
     */
    public abstract JFrame getEditorFrame();

    /**
     * Returns the the reference to the frame where interfaces of general
     * plugins are being shown.
     *
     * @return the plugin frame
     */
    public abstract JFrame getPluginFrame();

    /**
     * Returns true if the Sketchlet Designer is in the Playback mode.
     *
     * @return true if the Sketchlet Designer is in the Playback mode.
     */
    public abstract boolean isInPlaybackMode();

    /**
     * Returns true if the Sketchlet Builder is showing a message.
     *
     * @return true if a mesasage is being shown
     */
    public abstract boolean isMessageShowing();

    /**
     * Return the variables blackboard context. This is the auxiliary method,
     * the same as VariablesBlackboardContext.getInstance().
     *
     * @return the reference to the variables balckboard context
     * @see VariablesBlackboardContext#getInstance()
     */
    public VariablesBlackboardContext getVariablesBlackboardContext() {
        return VariablesBlackboardContext.getInstance();
    }

    /**
     * Return the playback context. This is the auxiliary method, the same as
     * SketchletPlaybackContext.getInstance().
     *
     * @return the reference to the playback context
     * @see SketchletPlaybackContext#getInstance()
     */
    public SketchletPlaybackContext getPlaybackContext() {
        return SketchletPlaybackContext.getInstance();
    }

    /**
     * Return the graphics context. This is the auxiliary method, the same as
     * SketchletGraphicsContext.getInstance().
     *
     * @return the reference to the graphics context
     * @see SketchletGraphicsContext#getInstance()
     */
    public SketchletGraphicsContext getGraphicsContext() {
        return SketchletGraphicsContext.getInstance();
    }
}
