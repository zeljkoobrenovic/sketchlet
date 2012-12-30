package net.sf.sketchlet.framework.controller;

import net.sf.sketchlet.context.SketchletContext;
import net.sf.sketchlet.designer.editor.ui.desktop.SystemVariablesDialog;
import net.sf.sketchlet.designer.playback.ui.InteractionRecorder;
import net.sf.sketchlet.framework.model.ActiveRegion;
import net.sf.sketchlet.framework.model.events.mouse.MouseEventsProcessor;
import net.sf.sketchlet.plugin.WidgetPlugin;
import net.sf.sketchlet.util.RefreshTime;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

/**
 * @author zeljko
 */
public class MouseController {
    private int prevX, prevY;
    private double speeds[] = new double[5];
    private int currentDistanceIndex = 0;
    private double speed;
    private long prevTimestamp;
    private static int mouseScreenX = 0;
    private static int mouseScreenY = 0;

    public void mouseEntered(InteractionContext context, MouseEvent e) {
        context.getCurrentPage().getMouseEventsProcessor().processAction(e, context.getFrame(), MouseEventsProcessor.MOUSE_ENTRY);
    }

    public void mouseExited(InteractionContext context, MouseEvent e) {
        context.getCurrentPage().getMouseEventsProcessor().processAction(e, context.getFrame(), MouseEventsProcessor.MOUSE_EXIT);
    }

    public void mousePressed(InteractionContext context, MouseEvent e) {
        int x = e.getPoint().x;
        int y = e.getPoint().y;
        if (context.getAffineTransform() != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(context, e.getPoint());
                ip = context.getAffineTransform().inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        SystemVariablesDialog.processMouseEvent(x, y, "mouse pressed");

        prevX = x;
        prevY = y;
        prevTimestamp = e.getWhen();

        for (int i = 0; i < speeds.length; i++) {
            speeds[i] = 0.0;
        }
        context.setSelectedRegion(context.getCurrentPage().getRegions().getMouseHelper().selectRegion(x, y, true));

        if (context.getSelectedRegion() == null && context.getMasterPage() != null) {
            context.setSelectedRegion(context.getMasterPage().getRegions().getMouseHelper().selectRegion(x, y, true));
        }

        MouseEventsProcessor mouseEventsProcessor;
        if (context.getSelectedRegion() != null) {
            context.getSelectedRegion().getMouseController().mousePressed(x, y, e.getModifiers(), e.getWhen(), e, context.getFrame(), true);
            context.repaint();
            mouseEventsProcessor = context.getSelectedRegion().getMouseEventsProcessor();
        } else {
            WidgetPlugin.setActiveWidget(null);
            mouseEventsProcessor = context.getCurrentPage().getMouseEventsProcessor();
        }

        if (mouseEventsProcessor != null) {
            mouseEventsProcessor.processAction(e, context.getFrame(), new int[]{MouseEventsProcessor.MOUSE_LEFT_BUTTON_PRESS, MouseEventsProcessor.MOUSE_MIDDLE_BUTTON_PRESS, MouseEventsProcessor.MOUSE_RIGHT_BUTTON_PRESS});
            InteractionRecorder.addEvent("Mouse press", context.getSelectedRegion() != null ? "region " + context.getSelectedRegion().getNumber() : "page", "button " + e.getButton());
        }
        SketchletContext.getInstance().repaint();
    }

    public void mouseReleased(InteractionContext context, MouseEvent e) {
        int x = e.getPoint().x;
        int y = e.getPoint().y;
        if (context.getAffineTransform() != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(context, e.getPoint());
                ip = context.getAffineTransform().inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        SystemVariablesDialog.processMouseEvent(x, y, "mouse released");
        MouseEventsProcessor mouseEventsProcessor;
        if (context.getSelectedRegion() != null) {
            context.getSelectedRegion().getMouseController().mouseReleased(x, y, e.getModifiers(), e.getWhen(), e, context.getFrame(), true);
            context.repaint();
            mouseEventsProcessor = context.getSelectedRegion().getMouseEventsProcessor();
        } else {
            mouseEventsProcessor = context.getCurrentPage().getMouseEventsProcessor();
        }

        if (mouseEventsProcessor != null) {
            mouseEventsProcessor.processAction(e, context.getFrame(), new int[]{MouseEventsProcessor.MOUSE_LEFT_BUTTON_RELEASE, MouseEventsProcessor.MOUSE_MIDDLE_BUTTON_RELEASE, MouseEventsProcessor.MOUSE_RIGHT_BUTTON_RELEASE});
            InteractionRecorder.addEvent("Mouse release", context.getSelectedRegion() != null ? "region " + context.getSelectedRegion().getNumber() : "page", "button " + e.getButton());
        }
        RefreshTime.update();
    }

    public void mouseDragged(InteractionContext context, MouseEvent e) {
        int x = (int) (e.getPoint().x);
        int y = (int) (e.getPoint().y);
        if (context.getAffineTransform() != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(context, e.getPoint());
                ip = context.getAffineTransform().inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        SystemVariablesDialog.processMouseEvent(x, y, "mouse dragged");
        if (context.getSelectedRegion() != null) {
            if ((context.getSelectedRegion().isMovable() || context.getSelectedRegion().isRotatable()) && e.getWhen() != prevTimestamp) {
                int dx = x - prevX;
                int dy = y - prevY;
                prevX = x;
                prevY = y;

                double dt = (e.getWhen() - prevTimestamp) / 100.0;

                speed = Math.sqrt(dx * dx + dy * dy) / dt;

                speeds[currentDistanceIndex++] = speed;
                currentDistanceIndex = currentDistanceIndex % speeds.length;

                double _s = 0.0;
                for (int i = 0; i < speeds.length; i++) {
                    _s += speeds[i];
                }

                speed = _s / speeds.length;
                prevTimestamp = e.getWhen();
            }
            context.getSelectedRegion().getMouseController().mouseDragged(x, y, e.getModifiers(), e.getWhen(), context.getScale(), e, context.getFrame(), true);
            context.repaint();
        }
        RefreshTime.update();
    }

    public void mouseMoved(InteractionContext context, MouseEvent e) {
        if (context.getCurrentPage() == null) {
            return;
        }
        setMouseScreenX(e.getXOnScreen());
        setMouseScreenY(e.getYOnScreen());
        int x = e.getPoint().x;
        int y = e.getPoint().y;
        if (context.getAffineTransform() != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(context, e.getPoint());
                ip = context.getAffineTransform().inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        if (context.getKeyboardController().isInCtrlMode() && context.getKeyboardController().isInAltMode()) {
            context.getCurrentPage().setPerspective_horizont_x2(x);
            context.getCurrentPage().setPerspective_horizont_y(y);
            context.repaint();
            return;
        }

        if (context.getKeyboardController().isInCtrlMode()) {
            context.getCurrentPage().setPerspective_horizont_x1(x);
            context.getCurrentPage().setPerspective_horizont_y(y);
            context.repaint();
            return;
        }

        SystemVariablesDialog.processMouseEvent(x, y, "mouse moved");

        e.getPoint().x = x;
        e.getPoint().y = y;

        ActiveRegion region = context.getCurrentPage().getRegions().getMouseHelper().selectRegion(x, y, true);

        if (region == null && context.getMasterPage() != null) {
            region = context.getMasterPage().getRegions().getMouseHelper().selectRegion(x, y, true);
        }

        if (context.getFrame() != null) {
            if (region != null && region.hasMouseDiscreteEvents()) {
                context.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else if (region != null && region.isMouseDraggable()) {
                context.getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            } else {
                context.getFrame().setCursor(Cursor.getDefaultCursor());
            }
        }

        if (region != context.getMouseOverRegion()) {
            if (context.getMouseOverRegion() != null) {
                context.getMouseOverRegion().getMouseEventsProcessor().processAction(e, context.getFrame(), MouseEventsProcessor.MOUSE_EXIT);
            }
            context.setMouseOverRegion(region);
            if (region != null) {
                region.getMouseEventsProcessor().processAction(e, context.getFrame(), MouseEventsProcessor.MOUSE_ENTRY);
            }
        }

        if (region != null) {
            region.getMouseController().mouseMoved(x, y, context.getScale(), e, context.getFrame(), true);
        }
    }

    public void mouseWheelMoved(InteractionContext context, MouseWheelEvent e) {
        int x = e.getPoint().x;
        int y = e.getPoint().y;

        if (context.getAffineTransform() != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(context, e.getPoint());
                ip = context.getAffineTransform().inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        if (context.getKeyboardController().isInShiftMode()) {
            context.getCurrentPage().setZoomCenterX(x);
            context.getCurrentPage().setZoomCenterY(y);
            if (e.getWheelRotation() > 0) {
                context.getCurrentPage().setZoom(context.getCurrentPage().getZoom() + 0.1);
            } else {
                context.getCurrentPage().setZoom(context.getCurrentPage().getZoom() - 0.1);
            }
            context.repaint();
            return;
        }

        context.setSelectedRegion(context.getCurrentPage().getRegions().getMouseHelper().selectRegion(x, y, true));

        if (context.getSelectedRegion() == null) {
            context.getCurrentPage().getMouseEventsProcessor().processAction(e, context.getFrame(), MouseEventsProcessor.MOUSE_WHEEL_UP);
            return;
        }
        int notches = e.getWheelRotation();
        if (notches < 0) {
            for (int i = 0; i < Math.abs(notches); i++) {
                context.getSelectedRegion().getMouseEventsProcessor().processAction(e, context.getFrame(), MouseEventsProcessor.MOUSE_WHEEL_UP);
            }
        } else {
            for (int i = 0; i < notches; i++) {
                context.getSelectedRegion().getMouseEventsProcessor().processAction(e, context.getFrame(), MouseEventsProcessor.MOUSE_WHEEL_DOWN);
            }
        }
    }

    public void mouseClicked(InteractionContext context, MouseEvent e) {
        int x = e.getPoint().x;
        int y = e.getPoint().y;

        mouseClicked(context, x, y, e.getButton(), e.getClickCount());
        RefreshTime.update();
    }

    public void mouseClicked(InteractionContext context, int x, int y, int button, int clickCount) {

        if (context.getCurrentPage() == null || context.getCurrentPage().getRegions() == null) {
            return;
        }
        if (context.getAffineTransform() != null) {
            try {
                Point2D ip = new Point();
                Point p = inversePerspective(context, new Point(x, y));
                ip = context.getAffineTransform().inverseTransform(p, ip);
                x = (int) (ip.getX());
                y = (int) (ip.getY());
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

        context.setSelectedRegion(context.getCurrentPage().getRegions().getMouseHelper().selectRegion(x, y, true));

        if (context.getSelectedRegion() == null && context.getMasterPage() != null) {
            context.setSelectedRegion(context.getMasterPage().getRegions().getMouseHelper().selectRegion(x, y, true));
        }

        MouseEventsProcessor mouseEventsProcessor;
        if (context.getSelectedRegion() != null) {
            mouseEventsProcessor = context.getSelectedRegion().getMouseEventsProcessor();
        } else {
            mouseEventsProcessor = context.getCurrentPage().getMouseEventsProcessor();
        }

        if (mouseEventsProcessor != null) {
            mouseEventsProcessor.processAction(button, context.getFrame(), new int[]{MouseEventsProcessor.MOUSE_LEFT_BUTTON_CLICK, MouseEventsProcessor.MOUSE_MIDDLE_BUTTON_CLICK, MouseEventsProcessor.MOUSE_RIGHT_BUTTON_CLICK});

            if (clickCount == 2) {
                mouseEventsProcessor.processAction(context.getFrame(), MouseEventsProcessor.MOUSE_DOUBLE_CLICK);
            }

            if (clickCount == 1) {
                InteractionRecorder.addEvent("Mouse click", context.getSelectedRegion() != null ? "region " + context.getSelectedRegion().getNumber() : "page", "button " + button);
            } else if (clickCount == 2) {
                InteractionRecorder.addEvent("Mouse double click", context.getSelectedRegion() != null ? "region " + context.getSelectedRegion().getNumber() : "page", "button " + button);
            }
        }
    }

    public Point inversePerspective(InteractionContext context, Point p) {
        if (context.getScreenMapping() != null && context.getScreenMapping().inPerspective) {
            int x = (int) p.getX();
            int y = (int) p.getY();
            float points[] = new float[2];
            context.getScreenMapping().perspectiveFilter.transformInverse(x, y, points);
            return new Point((int) points[0], (int) points[1]);
        }

        return p;
    }

    public static int getMouseScreenX() {
        return MouseController.mouseScreenX;
    }

    public static void setMouseScreenX(int mouseScreenX) {
        MouseController.mouseScreenX = mouseScreenX;
    }

    public static int getMouseScreenY() {
        return MouseController.mouseScreenY;
    }

    public static void setMouseScreenY(int mouseScreenY) {
        MouseController.mouseScreenY = mouseScreenY;
    }
}
