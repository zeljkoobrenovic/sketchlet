package net.sf.sketchlet.framework.controller;

import net.sf.sketchlet.designer.editor.ui.desktop.SystemVariablesDialog;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.framework.model.programming.macros.Commands;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.util.RefreshTime;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * @author zeljko
 */
public class KeyboardController {
    private int currentKey = -1;
    private boolean inCtrlMode = false;
    private boolean inAltMode = false;
    private boolean inShiftMode = false;

    public void keyPressed(InteractionContext context, KeyEvent e) {
        if (getCurrentKey() == e.getKeyCode()) {
            return;
        }
        setCurrentKey(e.getKeyCode());
        int modifiers = e.getModifiers();
        setInShiftMode((modifiers & KeyEvent.SHIFT_MASK) != 0);
        setInCtrlMode((modifiers & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0);
        setInAltMode((modifiers & KeyEvent.ALT_MASK) != 0);
        SystemVariablesDialog.processKeyboardEvent(e, e.getKeyText(e.getKeyCode()), "pressed");
        boolean keyProcessed = false;
        if (context.getCurrentPage() != null) {
            if (context.getSelectedRegion() != null) {
                keyProcessed = context.getSelectedRegion().keyboardProcessor.processKey(e, "pressed");
            }
            if (!keyProcessed) {
                keyProcessed = context.getCurrentPage().getKeyboardProcessor().processKey(e, "pressed");
                if (!keyProcessed && context.getMasterPage() != null) {
                    keyProcessed = context.getMasterPage().getKeyboardProcessor().processKey(e, "pressed");
                }
            }
        }
        if (!keyProcessed) {
            if (context.getCurrentPage() != null) {
                keyProcessed = context.getCurrentPage().getKeyboardProcessor().processKey(e, "");
                if (!keyProcessed && context.getMasterPage() != null) {
                    keyProcessed = context.getMasterPage().getKeyboardProcessor().processKey(e, "");
                }
            }
        }
        if (!keyProcessed) {
            int index;
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (WidgetPlugin.getActiveWidget() == null) {
                        index = context.getPages().getPages().indexOf(context.getCurrentPage());
                        if (index > 0) {
                            Commands.execute(context.getCurrentPage(), "Go to page", "previous", "", context.getCurrentPage().getActiveTimers(), context.getCurrentPage().getActiveMacros(), "", "", context.getFrame());
                        }
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (WidgetPlugin.getActiveWidget() == null) {
                        index = context.getPages().getPages().indexOf(context.getCurrentPage());
                        if (index < context.getPages().getPages().size() - 1) {
                            Commands.execute(context.getCurrentPage(), "Go to page", "next", "", context.getCurrentPage().getActiveTimers(), context.getCurrentPage().getActiveMacros(), "", "", context.getFrame());
                        }
                    }
                    break;
                case KeyEvent.VK_PLUS:
                case KeyEvent.VK_EQUALS:
                case 107:
                    if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                        context.setScale(context.getScale() + 0.1);
                        context.repaint();
                    }
                    break;
                case KeyEvent.VK_MINUS:
                case 109:
                    if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                        context.setScale(context.getScale() - 0.1);
                        context.repaint();
                    }
                    break;
                case KeyEvent.VK_0:
                    if ((e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0) {
                        context.setScale(1.0);
                        context.setScale(1.0);
                        context.repaint();
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    PlaybackFrame.closeWindow();
                    break;
            }
        }

        if (WidgetPlugin.getActiveWidget() != null) {
            WidgetPlugin.getActiveWidget().keyPressed(e);
        }

        e.consume();
        RefreshTime.update();
    }
    public void keyTyped(InteractionContext context, KeyEvent e) {
        e.consume();
        RefreshTime.update();
    }

    public void keyReleased(InteractionContext context, KeyEvent e) {
        currentKey = -1;
        int modifiers = e.getModifiers();
        inShiftMode = (modifiers & KeyEvent.SHIFT_MASK) != 0;
        inCtrlMode = (e.getModifiers() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()) != 0;
        inAltMode = (modifiers & KeyEvent.ALT_MASK) != 0;

        boolean keyProcessed = false;
        if (context.getCurrentPage() != null) {
            if (context.getSelectedRegion() != null) {
                keyProcessed = context.getSelectedRegion().keyboardProcessor.processKey(e, "pressed");
            }
            if (!keyProcessed) {
                keyProcessed = context.getCurrentPage().getKeyboardProcessor().processKey(e, "released");
                if (!keyProcessed && context.getMasterPage() != null) {
                    keyProcessed = context.getMasterPage().getKeyboardProcessor().processKey(e, "released");
                }
            }
        }
        SystemVariablesDialog.processKeyboardEvent(e, e.getKeyText(e.getKeyCode()), "released");

        if (WidgetPlugin.getActiveWidget() != null) {
            WidgetPlugin.getActiveWidget().keyReleased(e);
        }
        e.consume();
        RefreshTime.update();
    }

    public int getCurrentKey() {
        return currentKey;
    }

    public void setCurrentKey(int currentKey) {
        this.currentKey = currentKey;
    }

    public boolean isInCtrlMode() {
        return inCtrlMode;
    }

    public void setInCtrlMode(boolean inCtrlMode) {
        this.inCtrlMode = inCtrlMode;
    }

    public boolean isInAltMode() {
        return inAltMode;
    }

    public void setInAltMode(boolean inAltMode) {
        this.inAltMode = inAltMode;
    }

    public boolean isInShiftMode() {
        return inShiftMode;
    }

    public void setInShiftMode(boolean inShiftMode) {
        this.inShiftMode = inShiftMode;
    }
}
