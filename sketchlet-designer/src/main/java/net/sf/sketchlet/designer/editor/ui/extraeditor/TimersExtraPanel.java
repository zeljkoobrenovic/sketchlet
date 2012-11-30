package net.sf.sketchlet.designer.editor.ui.extraeditor;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.timers.TimerPanel;
import net.sf.sketchlet.designer.editor.ui.timers.curve.CurvesFrame;
import net.sf.sketchlet.framework.model.programming.timers.Timer;
import net.sf.sketchlet.framework.model.programming.timers.TimerThread;
import net.sf.sketchlet.framework.model.programming.timers.Timers;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class TimersExtraPanel extends JPanel {

    public JTabbedPane tabs = new JTabbedPane();
    public Vector<TimerThread> testTimers = new Vector<TimerThread>();
    public static TimersExtraPanel timersExtraPanel;
    JButton btnNew = new JButton(Workspace.createImageIcon("resources/add.gif"));
    JButton btnSave = new JButton(Workspace.createImageIcon("resources/save.gif"));
    JButton btnDelete = new JButton(Workspace.createImageIcon("resources/remove.gif"));
    JButton btnCurves = new JButton(Workspace.createImageIcon("resources/curve.png"));

    public TimersExtraPanel() {
        timersExtraPanel = this;
        setLayout(new BorderLayout());
        tabs.setFont(tabs.getFont().deriveFont(9.0f));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.putClientProperty("JComponent.sizeVariant", "small");
        SwingUtilities.updateComponentTreeUI(tabs);
        frame = this;
        add(tabs, BorderLayout.CENTER);
        tabs.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                int index = tabs.getSelectedIndex();
                if (SketchletEditor.getInstance() != null && index >= 0) {
                    // SketchletEditor.editorPanel.tabsRight.setSelectedIndex(SketchletEditor.timersTabIndex);
                    // SketchletEditor.editorPanel.timersTablePanel.table.getSelectionModel().setSelectionInterval(index, index);
                }
            }
        });
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setOrientation(JToolBar.VERTICAL);
        toolbar.setBorder(BorderFactory.createEmptyBorder());
        toolbar.add(btnNew);
        toolbar.add(btnSave);
        toolbar.add(btnDelete);
        toolbar.add(btnCurves);

        btnNew.setToolTipText(Language.translate("Create a new timer"));
        btnSave.setToolTipText(Language.translate("Saves timers"));
        btnDelete.setToolTipText(Language.translate("Delete the selected timer"));
        btnCurves.setToolTipText(Language.translate("Open the timer curves dialog"));

        btnNew.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                Timers.getGlobalTimers().addNewTimer();
                SketchletEditor.getInstance().getTimersTablePanel().model.fireTableDataChanged();
                showTimers(Timers.getGlobalTimers().getTimers().size() - 1);
            }
        });
        btnSave.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                save();
                SketchletEditor.getInstance().getTimersTablePanel().model.fireTableDataChanged();
            }
        });
        btnDelete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                int row = tabs.getSelectedIndex();
                if (row >= 0) {
                    // TimersFrame.hideTimers();
                    Timers.getGlobalTimers().getTimers().remove(row);
                    SketchletEditor.getInstance().getTimersTablePanel().model.fireTableDataChanged();
                    load();
                }
            }
        });
        btnCurves.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent ae) {
                CurvesFrame.showFrame();
            }
        });
        add(toolbar, BorderLayout.WEST);
        load();
    }

    public Timer newTimer() {
        Timer t = Timers.getGlobalTimers().addNewTimer();
        TimerPanel panel = new TimerPanel(t, testTimers, Timers.getGlobalTimers().getTimers().size() - 1);
        tabs.add(t.getName(), panel);
        panel.parentTab = tabs;
        panel.tabIndex = tabs.getTabCount() - 1;
        tabs.setSelectedIndex(tabs.getTabCount() - 1);

        SketchletEditor.getInstance().showExtraEditorPanel();
        SketchletEditor.getInstance().getTimersTablePanel().model.fireTableDataChanged();
        SketchletEditor.getInstance().getExtraEditorPanel().tabs.setSelectedIndex(0);

        return t;

    }

    public void save() {
        Timers.getGlobalTimers().save();
        int i = 0;
        for (Timer t : Timers.getGlobalTimers().getTimers()) {
            tabs.setTitleAt(i++, t.getName());
        }
    }

    public void deleteTimer(int index) {
        if (index >= 0 && index < Timers.getGlobalTimers().getTimers().size()) {
            Timers.getGlobalTimers().getTimers().remove(index);
            load();
        }
    }

    public void load() {
        SketchletEditor.editorFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        tabs.removeAll();
        int i = 0;
        for (Timer t : Timers.getGlobalTimers().getTimers()) {
            TimerPanel p = new TimerPanel(t, testTimers, i);
            tabs.add(t.getName(), p);
            i++;
        }
        SketchletEditor.editorFrame.setCursor(Cursor.getDefaultCursor());
    }

    public static TimersExtraPanel frame;

    public static void showTimers(int index) {
        timersExtraPanel.load();
        timersExtraPanel.tabs.setSelectedIndex(index);
        SketchletEditor.getInstance().showExtraEditorPanel();
        SketchletEditor.getInstance().getExtraEditorPanel().tabs.setSelectedIndex(0);
    }

    public static void showTimers(String strName) {
        timersExtraPanel.load();
        for (int i = 0; i < Timers.getGlobalTimers().getTimers().size(); i++) {
            Timer t = Timers.getGlobalTimers().getTimers().elementAt(i);
            if (t.getName().equalsIgnoreCase(strName)) {
                frame.tabs.setSelectedIndex(i);
                break;
            }
        }

        SketchletEditor.getInstance().showExtraEditorPanel();
        SketchletEditor.getInstance().getExtraEditorPanel().tabs.setSelectedIndex(0);
    }

    public static void onHide() {
        for (TimerThread tt : timersExtraPanel.testTimers) {
            tt.stop();
        }
        timersExtraPanel.testTimers.removeAllElements();
        Timers.getGlobalTimers().save();
    }
}
