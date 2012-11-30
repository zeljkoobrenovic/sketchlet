package net.sf.sketchlet.designer.editor.ui.wizard;

import net.sf.sketchlet.designer.animation.AnimationTimerDialog;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.framework.model.log.ActivityLog;
import net.sf.sketchlet.designer.editor.ui.extraeditor.ActiveRegionsExtraPanel;
import net.sf.sketchlet.designer.editor.ui.page.PageDetailsPanel;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.programming.timers.Timer;
import org.netbeans.spi.wizard.DeferredWizardResult;
import org.netbeans.spi.wizard.ResultProgressHandle;
import org.netbeans.spi.wizard.Summary;
import org.netbeans.spi.wizard.Wizard;
import org.netbeans.spi.wizard.WizardException;
import org.netbeans.spi.wizard.WizardPage;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Vector;

public class WizActiveRegionEvent {

    public static String eventType = "";

    public static void main(String[] args) {
        showWizard(0, "");
    }

    public static void showWizard(int type, final String strTitle) {
        ActivityLog.log("showWizard", type + " " + strTitle);
        final MyProducer mp = new MyProducer(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() == null ? null : SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement());

        final WizardPage[] pages;

        mp.type = type;

        if (type == 5) {
            pages = new WizardPage[]{mp.variableUpdateEventPage, mp.actionPage, mp.actionParamPage};
        } else if (type == 6) {
            pages = new WizardPage[]{mp.keyboardEventPage, mp.actionPage, mp.actionParamPage};
        } else if (type == 3 || type == 4) {
            pages = new WizardPage[]{mp.actionPage, mp.actionParamPage};
        } else if (type == 0) {
            pages = new WizardPage[]{mp.startPage, mp.eventPage, mp.actionPage, mp.actionParamPage};
        } else {
            if (type == 1) {
                mp.startPage.mouseEvent.setSelected(true);
                mp.eventPage.setPanel(0);
            } else {
                mp.startPage.interactionEvent.setSelected(true);
                mp.eventPage.setPanel(1);
            }
            pages = new WizardPage[]{mp.eventPage, mp.actionPage, mp.actionParamPage};
        }

        Runnable r;
        r = new Runnable() {

            public void run() {
                int w = 600;
                int h = 500;
                int x = SketchletEditor.editorFrame.getX() + SketchletEditor.editorFrame.getWidth() / 2 - w / 2;
                int y = SketchletEditor.editorFrame.getY() + SketchletEditor.editorFrame.getHeight() / 2 - h / 2;
                Wizard wizard = WizardPage.createWizard(strTitle, pages, mp);
                if (SketchletEditor.getInstance().getTimersTablePanel() != null) {
                    SketchletEditor.getInstance().getTimersTablePanel().model.fireTableDataChanged();
                    SketchletEditor.getInstance().getExtraEditorPanel().timersExtraPanel.load();
                }
            }
        };

        EventQueue.invokeLater(r);
    }
}

class MyProducer implements WizardPage.WizardResultProducer {

    EventPage eventPage = new EventPage();
    StartPage startPage = new StartPage(eventPage);
    ActionParamPage actionParamPage = new ActionParamPage();
    ActionPage actionPage = new ActionPage(actionParamPage);
    VariableUpdateEventPage variableUpdateEventPage = new VariableUpdateEventPage();
    KeyboardEventPage keyboardEventPage = new KeyboardEventPage();
    ActiveRegion region;
    int type;

    public MyProducer(ActiveRegion region) {
        this.region = region;
    }

    public Object finish(Map wizardData) throws WizardException {
        return new Result(this);
    }

    public boolean cancel(Map settings) {
        return true; // Allow the user to cancel the wizard.
    }
}

class Result extends DeferredWizardResult {

    MyProducer mp;

    public Result(MyProducer mp) {
        this.mp = mp;
    }

    public void start(Map settings, ResultProgressHandle progress) {
        Vector<String> comments = new Vector<String>();

        if (mp.actionPage.actionParamPage.action.equalsIgnoreCase("Region Animate")) {
            animateRegion(mp.actionParamPage.activeRegionAnimatePanel.timer, comments);
        } else if (mp.actionPage.actionParamPage.action.equalsIgnoreCase("Region")) {
            changeRegionProperties(comments);
        } else {
            try {
                String strCommentPrefix = "On '" + SketchletEditor.getInstance().getCurrentPage().getTitle() + "' ";

                if (mp.type == 1) {
                    processMouseEvent(comments);
                } else if (mp.type == 2) {
                    processInteractionEvent(comments);
                } else if (mp.type == 3) {
                    comments.add(strCommentPrefix + "entry");
                    setSketchEntryEvent(mp.actionParamPage.action, mp.actionParamPage.param1, mp.actionParamPage.param2, comments);
                } else if (mp.type == 4) {
                    comments.add(strCommentPrefix + "exit");
                    setSketchExitEvent(mp.actionParamPage.action, mp.actionParamPage.param1, mp.actionParamPage.param2, comments);
                } else if (mp.type == 5) {
                    comments.add(strCommentPrefix + "variable update");
                    setSketchVariableEvent(mp.variableUpdateEventPage.variableCombo.getSelectedItem().toString(), mp.variableUpdateEventPage.operatorCombo.getSelectedItem().toString(), mp.variableUpdateEventPage.value.getText(), mp.actionParamPage.action, mp.actionParamPage.param1, mp.actionParamPage.param2, comments);
                } else if (mp.type == 6) {
                    comments.add(strCommentPrefix + "keyboard event");
                    setSketchKeyboardEvent(mp.keyboardEventPage.ctrlBox.isSelected(), mp.keyboardEventPage.altBox.isSelected(), mp.keyboardEventPage.shiftBox.isSelected(), mp.keyboardEventPage.variableKeys.getSelectedItem().toString(), mp.keyboardEventPage.variableEvents.getSelectedItem().toString(), mp.actionParamPage.action, mp.actionParamPage.param1, mp.actionParamPage.param2, comments);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String[] items = new String[comments.size()];
        items = comments.toArray(items);

        // Replace null with an object reference to have this object returned
        // from the showWizard() method.

        progress.finished(Summary.create(items, null));
    }

    public void changeRegionProperties(Vector<String> comments) {
        Object[][] transformations = mp.actionParamPage.activeRegionPanel.transformations;
        int regionIndex = Integer.parseInt((String) mp.actionParamPage.activeRegionPanel.regionCombo.getSelectedItem());
        if (mp.type == 1) {
            int index = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size() - SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(mp.region);
            comments.add("On '" + mp.eventPage.mouseEvent.getSelectedItem().toString().toLowerCase() + "' in Region '" + (index) + "' do:");
        } else if (mp.type == 2) {
            comments.add("When Region '" + mp.eventPage.regionsCombo.getSelectedItem() + "' " + mp.eventPage.interactionEvent.getSelectedItem() + " Region '" + (SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size() - SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(mp.region)) + "' do:");
        } else if (mp.type == 3) {
            comments.add("On '" + SketchletEditor.getInstance().getCurrentPage().getTitle() + "' entry");
        } else if (mp.type == 5) {
            comments.add("On '" + SketchletEditor.getInstance().getCurrentPage().getTitle() + "' variable update");
        } else if (mp.type == 6) {
            comments.add("On '" + SketchletEditor.getInstance().getCurrentPage().getTitle() + "' keyboard event");
        } else if (mp.type == 4) {
            comments.add("On '" + SketchletEditor.getInstance().getCurrentPage().getTitle() + "' exit");
        }
        for (int p = 0; p < transformations.length; p++) {
            JCheckBox checkBox = (JCheckBox) transformations[p][0];
            JTextField value = (JTextField) transformations[p][1];
            JTextField variable = (JTextField) transformations[p][2];

            if (checkBox.isSelected()) {
                if (mp.type == 1) {
                    int index = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size() - SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(mp.region);
                    setMouseEvent("Variable update", variable.getText(), value.getText(), comments);
                } else if (mp.type == 2) {
                    setInteractionEvent("Variable update", variable.getText(), value.getText(), comments);
                } else if (mp.type == 3) {
                    setSketchEntryEvent("Variable update", variable.getText(), value.getText(), comments);
                } else if (mp.type == 5) {
                    setSketchVariableEvent(mp.variableUpdateEventPage.variableCombo.getSelectedItem().toString(), mp.variableUpdateEventPage.operatorCombo.getSelectedItem().toString(), mp.variableUpdateEventPage.value.getText(), mp.actionParamPage.action, mp.actionParamPage.param1, mp.actionParamPage.param2, comments);
                } else if (mp.type == 6) {
                    setSketchKeyboardEvent(mp.keyboardEventPage.ctrlBox.isSelected(), mp.keyboardEventPage.altBox.isSelected(), mp.keyboardEventPage.shiftBox.isSelected(), mp.keyboardEventPage.variableKeys.getSelectedItem().toString(), mp.keyboardEventPage.variableEvents.getSelectedItem().toString(), mp.actionParamPage.action, mp.actionParamPage.param1, mp.actionParamPage.param2, comments);
                } else if (mp.type == 4) {
                    setSketchExitEvent("Variable update", variable.getText(), value.getText(), comments);
                }
            }
        }

        comments.add("");
        comments.add("");
        comments.add("In Region " + regionIndex + "'");
        for (int p = 0; p < transformations.length; p++) {
            JCheckBox checkBox = (JCheckBox) transformations[p][0];
            JTextField value = (JTextField) transformations[p][1];
            JTextField variable = (JTextField) transformations[p][2];

            if (checkBox.isSelected()) {
                setRegionProperty(regionIndex, checkBox.getText(), p, "=" + variable.getText(), comments);
            }
        }
    }

    public void animateRegion(Timer timer, Vector<String> comments) {
        if (timer == null) {
            return;
        }
        Object[][] transformations = mp.actionParamPage.activeRegionAnimatePanel.transformations;
        int regionIndex = Integer.parseInt((String) mp.actionParamPage.activeRegionAnimatePanel.regionCombo.getSelectedItem());

        int nextIndex = AnimationTimerDialog.addToTimer(timer, transformations, mp.actionParamPage.activeRegionAnimatePanel.region, mp.actionParamPage.activeRegionAnimatePanel.restartCheck.isSelected());

        comments.add("Timer '" + timer.getName() + "' is configured to:");
        for (int p = 0; p < transformations.length; p++) {
            JCheckBox checkBox = (JCheckBox) transformations[p][0];
            JTextField start = (JTextField) transformations[p][1];
            JTextField end = (JTextField) transformations[p][2];
            JTextField variable = (JTextField) transformations[p][3];

            if (checkBox.isSelected()) {
                comments.add("    Update variable '" + variable.getText() + "' from '" + start.getText() + "' to '" + end.getText() + "'");
            }
        }
        comments.add("");
        comments.add("");
        comments.add("In Region '" + regionIndex + "'");
        for (int p = 0; p < transformations.length; p++) {
            JCheckBox checkBox = (JCheckBox) transformations[p][0];
            JTextField variable = (JTextField) transformations[p][3];

            if (checkBox.isSelected()) {
                setRegionPropertyAnimate(regionIndex, checkBox.getText(), p, "=" + variable.getText(), comments);
            }
        }
        if (mp.type == 1) {
            int index = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size() - SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(mp.region);
            comments.add("On '" + mp.eventPage.mouseEvent.getSelectedItem().toString().toLowerCase() + "' in Region '" + (index) + "' do:");
            setMouseEvent("Start timer", timer.getName(), "", comments);
        } else if (mp.type == 2) {
            comments.add("When Region '" + mp.eventPage.regionsCombo.getSelectedItem() + "' " + mp.eventPage.interactionEvent.getSelectedItem() + " Region '" + (SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size() - SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(mp.region)) + "' do:");
            setInteractionEvent("Start timer", timer.getName(), "", comments);
        } else if (mp.type == 3) {
            comments.add("On '" + SketchletEditor.getInstance().getCurrentPage().getTitle() + "' entry:");
            setSketchEntryEvent("Start timer", timer.getName(), "", comments);
        } else if (mp.type == 5) {
            comments.add("On '" + SketchletEditor.getInstance().getCurrentPage().getTitle() + "' variable update:");
            setSketchVariableEvent(mp.variableUpdateEventPage.variableCombo.getSelectedItem().toString(), mp.variableUpdateEventPage.operatorCombo.getSelectedItem().toString(), mp.variableUpdateEventPage.value.getText(), "Start timer", timer.getName(), "", comments);
        } else if (mp.type == 6) {
            comments.add("On '" + SketchletEditor.getInstance().getCurrentPage().getTitle() + "' keyboard event:");
            setSketchKeyboardEvent(mp.keyboardEventPage.ctrlBox.isSelected(), mp.keyboardEventPage.altBox.isSelected(), mp.keyboardEventPage.shiftBox.isSelected(), mp.keyboardEventPage.variableKeys.getSelectedItem().toString(), mp.keyboardEventPage.variableEvents.getSelectedItem().toString(), "Start timer", timer.getName(), "", comments);
        } else if (mp.type == 4) {
            comments.add("On '" + SketchletEditor.getInstance().getCurrentPage().getTitle() + "' exit:");
            setSketchExitEvent("Start timer", timer.getName(), "", comments);
        }
    }

    public void processMouseEvent(Vector<String> comments) {
        try {
            int index = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size() - SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(mp.region);
            comments.add("On '" + mp.eventPage.mouseEvent.getSelectedItem().toString().toLowerCase() + "' in Region '" + (index) + "' do:");
            if (mp.actionPage.actionParamPage.action.equalsIgnoreCase("Region")) {
                Object[][] transformations = mp.actionParamPage.activeRegionPanel.transformations;
                int regionIndex = Integer.parseInt((String) mp.actionParamPage.activeRegionPanel.regionCombo.getSelectedItem());

                for (int p = 0; p < transformations.length; p++) {
                    JCheckBox checkBox = (JCheckBox) transformations[p][0];
                    JTextField value = (JTextField) transformations[p][1];
                    JTextField variable = (JTextField) transformations[p][2];

                    if (checkBox.isSelected()) {
                        setMouseEvent("Variable update", variable.getText(), value.getText(), comments);
                    }
                }

                comments.add("");
                comments.add("");
                comments.add("In Region '" + regionIndex + "'");
                for (int p = 0; p < transformations.length; p++) {
                    JCheckBox checkBox = (JCheckBox) transformations[p][0];
                    JTextField value = (JTextField) transformations[p][1];
                    JTextField variable = (JTextField) transformations[p][2];

                    if (checkBox.isSelected()) {
                        setRegionProperty(regionIndex, checkBox.getText(), p, "=" + variable.getText(), comments);
                    }
                }
            } else {
                setMouseEvent(mp.actionPage.actionParamPage.action, mp.actionPage.actionParamPage.param1, mp.actionPage.actionParamPage.param2, comments);
            }
            ActiveRegionsExtraPanel.showRegionsAndActions();
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion());
            if (ActiveRegionsExtraPanel.regionsAndActions != null) {
                net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel panel = (net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel) ActiveRegionsExtraPanel.regionsAndActions.tabs.getSelectedComponent();

                if (panel != null) {
                    panel.getTabs().setSelectedIndex(net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel.getIndexEvents());
                    panel.getTabsRegionEvents().setSelectedIndex(net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel.getIndexMouseEvents());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processInteractionEvent(Vector<String> comments) {
        try {
            comments.add("When Region '" + mp.eventPage.regionsCombo.getSelectedItem() + "' " + mp.eventPage.interactionEvent.getSelectedItem() + " Region '" + (SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size() - SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().indexOf(mp.region)) + "' do:");
            if (mp.actionPage.actionParamPage.action.equalsIgnoreCase("Region")) {
                Object[][] transformations = mp.actionParamPage.activeRegionPanel.transformations;
                int regionIndex = Integer.parseInt((String) mp.actionParamPage.activeRegionPanel.regionCombo.getSelectedItem());
                for (int p = 0; p < transformations.length; p++) {
                    JCheckBox checkBox = (JCheckBox) transformations[p][0];
                    JTextField value = (JTextField) transformations[p][1];
                    JTextField variable = (JTextField) transformations[p][2];

                    if (checkBox.isSelected()) {
                        setInteractionEvent("Variable update", variable.getText(), value.getText(), comments);
                    }
                }

                comments.add("");
                comments.add("");
                comments.add("In Region " + regionIndex + "'");
                for (int p = 0; p < transformations.length; p++) {
                    JCheckBox checkBox = (JCheckBox) transformations[p][0];
                    JTextField value = (JTextField) transformations[p][1];
                    JTextField variable = (JTextField) transformations[p][2];

                    if (checkBox.isSelected()) {
                        setRegionProperty(regionIndex, checkBox.getText(), p, "=" + variable.getText(), comments);
                    }
                }
            } else {
                setInteractionEvent(mp.actionParamPage.action, mp.actionParamPage.param1, mp.actionParamPage.param2, comments);
            }
            ActiveRegionsExtraPanel.showRegionsAndActions();
            ActiveRegionsExtraPanel.reload(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getLastSelectedRegion());
            if (ActiveRegionsExtraPanel.regionsAndActions != null) {
                net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel panel = (net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel) ActiveRegionsExtraPanel.regionsAndActions.tabs.getSelectedComponent();

                if (panel != null) {
                    panel.getTabs().setSelectedIndex(net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel.getIndexEvents());
                    panel.getTabsRegionEvents().setSelectedIndex(net.sf.sketchlet.designer.editor.ui.region.ActiveRegionPanel.getIndexOverlap());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRegionProperty(int regionIndex, String property, int p, String value, Vector<String> comments) {
        try {
            ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().elementAt(SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size() - regionIndex);
            if (p == 0) {
                //region.imageIndex.setSelectedItem(value);
                region.strImageIndex = value;
                comments.add("    Image index is set to '" + value + "'");
                comments.add("");
            } else if (p == 1) {
                //region.imageUrlField.setSelectedItem(value);
                region.imageUrlField = value;
                comments.add("    Image URL is set to '" + value + "'");
                comments.add("");
            } else {
                region.setProperty(property, value);
                comments.add("    " + property + "  is set to '" + value + "'");
                comments.add("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRegionPropertyAnimate(int regionIndex, String property, int p, String value, Vector<String> comments) {
        try {
            ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().elementAt(SketchletEditor.getInstance().getCurrentPage().getRegions().getRegions().size() - regionIndex);
            if (p == 0) {
                //region.imageIndex.setSelectedItem(value);
                region.strImageIndex = value;
                comments.add("    Image index is set to '" + value + "'");
                comments.add("");
            } else {
                region.setProperty(property, value);
                comments.add("    " + property + "  is set to '" + value + "'");
                comments.add("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSketchEntryEvent(String action, String param1, String param2, Vector<String> comments) {
        Object actions[][] = SketchletEditor.getInstance().getCurrentPage().getOnEntryMacro().getActions();
        for (int i = 0; i < actions.length; i++) {
            if (actions[i][0].toString().isEmpty()) {
                comments.add("    " + action + " '" + param1 + "' '" + param2 + "'");
                actions[i][0] = action;
                actions[i][1] = param1;
                actions[i][2] = param2;
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsOnEntrySubtabIndex);
                if (SketchletEditor.getInstance().getPageDetailsPanel() != null && SketchletEditor.getInstance().getPageDetailsPanel().pOnEntry != null) {
                    SketchletEditor.getInstance().getPageDetailsPanel().pOnEntry.reload();
                }
                break;
            }
        }
    }

    public void setSketchExitEvent(String action, String param1, String param2, Vector<String> comments) {
        Object actions[][] = SketchletEditor.getInstance().getCurrentPage().getOnExitMacro().getActions();
        for (int i = 0; i < actions.length; i++) {
            if (actions[i][0].toString().length() == 0) {
                comments.add("    " + action + " '" + param1 + "' '" + param2 + "'");
                actions[i][0] = action;
                actions[i][1] = param1;
                actions[i][2] = param2;
                SketchletEditor.getInstance().showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsOnExitSubtabIndex);
                if (SketchletEditor.getInstance().getPageDetailsPanel() != null && SketchletEditor.getInstance().getPageDetailsPanel().pOnExit != null) {
                    SketchletEditor.getInstance().getPageDetailsPanel().pOnExit.reload();
                }
                break;
            }
        }
    }

    public void setSketchKeyboardEvent(boolean ctrl, boolean alt, boolean shift, String key, String event, String action, String param1, String param2, Vector<String> comments) {
    }

    public void setSketchVariableEvent(String var, String operator, String value, String action, String param1, String param2, Vector<String> comments) {
        /*Object actions[][] = SketchletEditor.editorPanel.currentSketch.eventHandler.actions;
        for (int i = 0; i < actions.length; i++) {
            if (actions[i][4].toString().length() == 0) {
                comments.add("    " + action + " '" + param1 + "' '" + param2 + "'");
                actions[i][0] = var;
                actions[i][1] = operator;
                actions[i][2] = value;
                actions[i][4] = action;
                actions[i][5] = param1;
                actions[i][6] = param2;
                SketchletEditor.editorPanel.showStatePanel(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsVariablesSubtabIndex);
                if (SketchletEditor.editorPanel.statePanel != null && SketchletEditor.editorPanel.statePanel.stateEventHandlerPanel != null) {
                    SketchletEditor.editorPanel.statePanel.stateEventHandlerPanel.model.fireTableDataChanged();
                }
                break;
            }
        }  */
    }

    public void setMouseEvent(String action, String param1, String param2, Vector<String> comments) {
        /*for (int i = 0; i < mp.region.mouseEvents.length; i++) {
            if (mp.region.mouseEvents[i][0].toString().length() == 0) {
                comments.add("    " + action + " '" + param1 + "' '" + param2 + "'");
                mp.region.mouseEvents[i][0] = mp.eventPage.mouseEvent.getSelectedItem();
                mp.region.mouseEvents[i][1] = action;
                mp.region.mouseEvents[i][2] = param1;
                mp.region.mouseEvents[i][3] = param2;
                break;
            }
        } */
    }

    public void setInteractionEvent(String action, String param1, String param2, Vector<String> comments) {
        /*for (int i = 0; i < mp.region.interactionEvents.length; i++) {
            if (mp.region.interactionEvents[i][0].toString().length() == 0) {
                int index = mp.eventPage.regionsCombo.getSelectedIndex() - 1;
                if (index > 0) {
                    ActiveRegion a = SketchletEditor.editorPanel.currentSketch.regions.regions.elementAt(SketchletEditor.editorPanel.currentSketch.regions.regions.size() - index);
                    a.getDrawImageFileName(0);
                    mp.region.interactionEvents[i][0] = a.getDrawImageFileName(0);
                } else {
                    mp.region.interactionEvents[i][0] = mp.eventPage.regionsCombo.getSelectedItem();
                }
                comments.add("    " + action + " '" + param1 + "' '" + param2 + "'");
                mp.region.interactionEvents[i][1] = mp.eventPage.interactionEvent.getSelectedItem();
                mp.region.interactionEvents[i][2] = action;
                mp.region.interactionEvents[i][3] = param1;
                mp.region.interactionEvents[i][4] = param2;
                break;
            }
        }  */
    }
}
