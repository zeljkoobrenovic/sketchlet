package net.sf.sketchlet.designer.editor.ui.region.menus;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.animation.AnimationTimerDialog;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorMode;
import net.sf.sketchlet.designer.editor.SketchletEditorUtils;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.editor.ui.page.PageDetailsPanel;
import net.sf.sketchlet.designer.editor.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.designer.editor.ui.wizard.WizActiveRegionEvent;
import net.sf.sketchlet.framework.model.ActiveRegion;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * @author zobrenovic
 */
public class ActiveRegionMenu extends JMenu {
    private JMenuItem mouseWizard = new JMenuItem(Language.translate("region mouse event wizard..."), Workspace.createImageIcon("resources/mouse.png"));
    private JMenuItem interactionWizard = new JMenuItem(Language.translate("region interaction wizard..."), Workspace.createImageIcon("resources/interaction.png"));
    private JMenuItem animationWizard = new JMenuItem(Language.translate("region animation wizard..."), Workspace.createImageIcon("resources/timer.png"));
    private JMenuItem imageExplorer = new JMenuItem(Language.translate("create interactive image window"), Workspace.createImageIcon("resources/wizard.png"));

    private JMenuItem delete = new JMenuItem(Language.translate("Remove"), Workspace.createImageIcon("resources/user-trash.png"));
    private JMenuItem group = new JMenuItem(Language.translate("Group"), Workspace.createImageIcon("resources/system-users.png"));
    private JMenuItem extract;
    private JMenuItem extractNewFrame;
    private JMenuItem defineClip;
    private JMenuItem stamp;
    private JMenuItem copyRegion;
    private JMenuItem copyRegionImage;
    private JMenuItem pasteImage;
    private JMenuItem pasteText;
    private JMenuItem mouseEvents;
    private JMenuItem editImage;
    private JMenuItem editText;
    private JMenuItem align;
    private JMenuItem transform;
    private JMenuItem moveRotate;
    private JMenuItem upwards;
    private JMenuItem backwards;
    private JMenuItem asBackground;
    private JMenuItem alignUp;
    private JMenuItem alignCentered;
    private JMenuItem alignBottom;
    private JMenuItem alignLeft;
    private JMenuItem alignCenter;
    private JMenuItem alignRight;
    private JMenuItem distributeH;
    private JMenuItem distributeV;
    private JMenu actions = new JMenu(Language.translate("Edit Page Events"));
    private JMenu pasteImageMain = new JMenu(Language.translate("Paste Image"));
    private JMenuItem entryActions = new JMenuItem(Language.translate("On Page Entry"), Workspace.createImageIcon("resources/import.gif"));
    private JMenuItem exitActions = new JMenuItem(Language.translate("On Page Exit"), Workspace.createImageIcon("resources/export.gif"));
    private JMenuItem variablesActions = new JMenuItem(Language.translate("On Variable Updates"), Workspace.createImageIcon("resources/variables.png"));
    private JMenuItem keyboardActions = new JMenuItem(Language.translate("On Keyboard Events"), Workspace.createImageIcon("resources/keyboard.png"));
    private JMenuItem refresh = new JMenuItem(Language.translate("Refresh"), Workspace.createImageIcon("resources/view-refresh.png"));
    private JMenuItem pasteImageBackground = new JMenuItem(Language.translate("Paste Image as Background"), Workspace.createImageIcon("resources/edit-paste.png"));
    private JMenuItem pasteImageHere = new JMenuItem(Language.translate("Paste Image Here"), Workspace.createImageIcon("resources/edit-paste.png"));
    private JMenuItem pasteImageAsRegion = new JMenuItem(Language.translate("Paste Image as New Region"), Workspace.createImageIcon("resources/edit-paste.png"));
    private JMenuItem pasteRegion = new JMenuItem(Language.translate("Paste Region Here"), Workspace.createImageIcon("resources/edit-paste.png"));
    private JMenu alignRegions = new JMenu(Language.translate("Align"));
    private JMenu bringToFront = new JMenu(Language.translate("Bring to Front"));
    private JMenu sendToBack = new JMenu(Language.translate("Send Back"));
    private JMenuItem toFront = new JMenuItem(Language.translate("Bring to Front"), Workspace.createImageIcon("resources/bring-to-front.png"));
    private JMenuItem back = new JMenuItem(Language.translate("Send To Back"), Workspace.createImageIcon("resources/send-back.png"));
    private JPopupMenu popupMenuMain = new JPopupMenu();
    private JMenu trajectoryMenu = new JMenu(Language.translate("Define Trajectory"));
    private JMenu trajectory2Menu = new JMenu(Language.translate("Define Secondary Trajectory"));
    private JMenu editTrajectoryMenu = new JMenu(Language.translate("Edit/Clear Trajectories"));
    private JMenuItem clearTrajectory = new JMenuItem(Language.translate("Clear Trajectory"), Workspace.createImageIcon("resources/edit-clear.png"));
    private JCheckBoxMenuItem stickToTrajectory = new JCheckBoxMenuItem(Language.translate("Stick to Trajectory"));
    private JMenuItem clearTrajectory2 = new JMenuItem(Language.translate("Clear Secondary Trajectory"), Workspace.createImageIcon("resources/edit-clear.png"));
    private JMenuItem editTrajectory = new JMenuItem(Language.translate("Edit Trajectory Points"), Workspace.createImageIcon("resources/trajectory.png"));
    private JMenuItem endTrajectory = new JMenuItem(Language.translate("End Trajectory"), Workspace.createImageIcon("resources/trajectory.png"));
    private JMenuItem trajectoryGesture = new JMenuItem(Language.translate("With Freehand Gesture"), Workspace.createImageIcon("resources/pencil.gif"));
    private JMenuItem trajectoryLine = new JMenuItem(Language.translate("As a Line"), Workspace.createImageIcon("resources/line_2.png"));
    private JMenuItem trajectoryMultiLine = new JMenuItem(Language.translate("As a Multiline"), Workspace.createImageIcon("resources/line_multi.png"));
    private JMenuItem trajectoryOval = new JMenuItem(Language.translate("As an Oval"), Workspace.createImageIcon("resources/oval.png"));
    private JMenu trajectoryFromRegion = new JMenu(Language.translate("Copy from Region"));
    private JMenu trajectory2FromRegion = new JMenu(Language.translate("Copy from Region"));
    private JMenuItem trajectory2Gesture = new JMenuItem(Language.translate("With Freehand Gesture"), Workspace.createImageIcon("resources/pencil.gif"));
    private JMenuItem trajectory2Line = new JMenuItem(Language.translate("As a Line"), Workspace.createImageIcon("resources/line_2.png"));
    private JMenuItem trajectory2MultiLine = new JMenuItem(Language.translate("As a Multiline"), Workspace.createImageIcon("resources/line_multi.png"));
    private JMenuItem trajectory2Oval = new JMenuItem(Language.translate("As an Oval"), Workspace.createImageIcon("resources/oval.png"));
    private JMenuItem animator = new JMenuItem(Language.translate("Animate..."), Workspace.createImageIcon("resources/timer.png"));
    private JMenuItem menuProperties = new JMenuItem(Language.translate("Region Properties"));
    private JMenu wizards = new JMenu(Language.translate("Wizards"));
    private JMenu fix = new JMenu(Language.translate("Lock / Unlock"));
    private JMenuItem menuFixPosition = new JMenuItem(Language.translate("Lock Position"), Workspace.createImageIcon("resources/fix-position.png"));
    private JMenuItem menuFixRotation = new JMenuItem(Language.translate("Lock Rotation"), Workspace.createImageIcon("resources/fix-rotation.png"));
    private JMenuItem menuFixSize = new JMenuItem(Language.translate("Lock Size"), Workspace.createImageIcon("resources/fix-size.png"));
    private JMenuItem menuFixX = new JMenuItem(Language.translate("Lock Horizontal Position"), Workspace.createImageIcon("resources/fix-x.png"));
    private JMenuItem menuFixY = new JMenuItem(Language.translate("Lock Vertical Position"), Workspace.createImageIcon("resources/fix-y.png"));
    private JMenu reset = new JMenu(Language.translate("Reset"));
    private JMenuItem menuUnfixRotation = new JMenuItem(Language.translate("Reset Rotation"), Workspace.createImageIcon("resources/zero.png"));
    private JMenuItem menuUnfixRotationPoint = new JMenuItem(Language.translate("Reset Rotation Point"), Workspace.createImageIcon("resources/zero.png"));
    private JMenuItem menuUnfixTrajectoryPoint = new JMenuItem(Language.translate("Reset Trajectory Points"), Workspace.createImageIcon("resources/trajectory.png"));
    private JMenuItem menuUnfixMotionLimits = new JMenuItem(Language.translate("Reset Motion Limits"), Workspace.createImageIcon("resources/move_rotate.png"));

    public ActiveRegionMenu() {
        super(Language.translate("Active Region"));
        createItems();
        fillMenu();
        setEnabled(false);
    }

    public void createItems() {
        actions.setIcon(Workspace.createImageIcon("resources/applications-system.png"));
        pasteImageMain.setIcon(Workspace.createImageIcon("resources/edit-paste.png"));
        extract = new JMenuItem(Language.translate("Extract Image From Background"), Workspace.createImageIcon("resources/edit-cut.png"));
        extract.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().extract(0);
            }
        });
        extractNewFrame = new JMenuItem(Language.translate("Extract Image in a New Frame"), Workspace.createImageIcon("resources/edit-cut.png"));
        extractNewFrame.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().extract(-1);
            }
        });
        defineClip = new JMenuItem(Language.translate("Define Visible Area"), Workspace.createImageIcon("resources/clip.png"));
        defineClip.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().defineClip();
            }
        });
        stamp = new JMenuItem(Language.translate("Stamp Image On Background"), Workspace.createImageIcon("resources/stamp.png"));
        stamp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().stamp();
            }
        });

        menuProperties.setAccelerator(KeyStroke.getKeyStroke("F3"));
        extract.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        extractNewFrame.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        stamp.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_P, ActionEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        //delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().deleteSelectedRegion();
            }
        });
        editTrajectory.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_T, ActionEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        group.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().groupSelectedRegion();
            }
        });
        pasteImage = new JMenuItem(Language.translate("Paste Image in Region"), Workspace.createImageIcon("resources/edit-paste.png"));
        pasteImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getEditorClipboardController().fromClipboardCurrentAction(0);
            }
        });

        copyRegion = new JMenuItem(Language.translate("Copy Region"), Workspace.createImageIcon("resources/edit-copy.png"));
        copyRegion.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getEditorClipboardController().copySelectedAction();
            }
        });

        copyRegionImage = new JMenuItem(Language.translate("Copy Region Image"), Workspace.createImageIcon("resources/edit-copy.png"));
        copyRegionImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getEditorClipboardController().copySelectedAction();
            }
        });

        pasteText = new JMenuItem(Language.translate("Paste Text in Region"), Workspace.createImageIcon("resources/edit-paste.png"));
        pasteText.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                //odd: the Object param of getContents is not currently used
                Transferable contents = clipboard.getContents(null);
                boolean hasTransferableText = (contents != null) && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
                if (hasTransferableText) {
                    try {
                        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
                            ActiveRegion action = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                            action.text = (String) contents.getTransferData(DataFlavor.stringFlavor);
                            SketchletEditor.getInstance().repaint();
                        }
                    } catch (UnsupportedFlavorException ex) {
                    } catch (IOException ex) {
                    }

                    SketchletEditor.getInstance().repaint();
                }
            }
        });

        editImage = new JMenuItem(Language.translate("Edit Image"), Workspace.createImageIcon("resources/imageeditor.gif"));
        final JMenuItem editShape = new JMenuItem(Language.translate("Edit Shape"), Workspace.createImageIcon("resources/oval.png"));
        mouseEvents = new JMenuItem(Language.translate("Edit Mouse Events"), Workspace.createImageIcon("resources/mouse.png"));
        editText = new JMenuItem(Language.translate("Edit Text"), Workspace.createImageIcon("resources/preferences-desktop-font.png"));

        align = new JMenuItem(Language.translate("Edit Alignment"), Workspace.createImageIcon("resources/align.gif"));
        transform = new JMenuItem(Language.translate("Edit Transformation Settings"), Workspace.createImageIcon("resources/variables.png"));
        moveRotate = new JMenuItem(Language.translate("Edit Move & Rotate Settings"), Workspace.createImageIcon("resources/details.gif"));
        JMenuItem trajectorySettings = new JMenuItem(Language.translate("Edit Trajectory Settings"), Workspace.createImageIcon("resources/go-jump.png"));

        upwards = new JMenuItem(Language.translate("Bring Forward"), Workspace.createImageIcon("resources/bring-forward.png"));
        backwards = new JMenuItem(Language.translate("Send Backwards"), Workspace.createImageIcon("resources/send-backwards.png"));
        asBackground = new JMenuItem(Language.translate("Set as Background"), Workspace.createImageIcon("resources/send-backwards.png"));

        fix.setIcon(Workspace.createImageIcon("resources/lock.png"));
        fix.add(menuFixPosition);
        fix.add(menuFixX);
        fix.add(menuFixY);
        fix.add(menuFixRotation);
        fix.add(menuFixSize);
        wizards.setIcon(Workspace.createImageIcon("resources/wizard.png"));
        wizards.add(mouseWizard);
        wizards.add(interactionWizard);
        wizards.add(animationWizard);
        wizards.addSeparator();
        wizards.add(imageExplorer);

        trajectoryMenu.setIcon(Workspace.createImageIcon("resources/trajectory_red.png"));
        trajectoryMenu.add(trajectoryGesture);
        trajectoryMenu.addSeparator();
        trajectoryMenu.add(trajectoryLine);
        trajectoryMenu.add(trajectoryMultiLine);
        trajectoryMenu.add(trajectoryOval);
        trajectoryMenu.add(trajectoryFromRegion);

        editTrajectoryMenu.setIcon(Workspace.createImageIcon("resources/trajectory.png"));

        trajectory2Menu.setIcon(Workspace.createImageIcon("resources/trajectory_blue.png"));
        trajectory2Menu.add(trajectory2Gesture);
        trajectory2Menu.addSeparator();
        trajectory2Menu.add(trajectory2Line);
        trajectory2Menu.add(trajectory2MultiLine);
        trajectory2Menu.add(trajectory2Oval);
        trajectory2Menu.add(trajectory2FromRegion);

        imageExplorer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditorUtils.createAnimatedImageWindow();
            }
        });
        mouseWizard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                    WizActiveRegionEvent.showWizard(1, "Region Mouse Wizard");
                    SketchletEditor.getInstance().getSketchToolbar().enableControls();
                }
            }
        });

        interactionWizard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                    WizActiveRegionEvent.showWizard(2, "Region Interaction Wizard");
                    SketchletEditor.getInstance().getSketchToolbar().enableControls();
                }
            }
        });
        animationWizard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                    new AnimationTimerDialog(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement(), SketchletEditor.getInstance().editorFrame);
                    SketchletEditor.getInstance().getSketchToolbar().enableControls();
                }
            }
        });

        alignUp = new JMenuItem(Language.translate("Top"), Workspace.createImageIcon("resources/align-top.png"));
        alignCentered = new JMenuItem(Language.translate("Centered"), Workspace.createImageIcon("resources/align-centered.png"));
        alignBottom = new JMenuItem(Language.translate("Bottom"), Workspace.createImageIcon("resources/align-bottom.png"));
        alignLeft = new JMenuItem(Language.translate("Left"), Workspace.createImageIcon("resources/align-left.png"));
        alignCenter = new JMenuItem(Language.translate("Center"), Workspace.createImageIcon("resources/align-center.png"));
        alignRight = new JMenuItem(Language.translate("Right"), Workspace.createImageIcon("resources/align-right.png"));
        distributeH = new JMenuItem(Language.translate("Distribute Horizontally"), Workspace.createImageIcon("resources/distribute_hcentre.png"));
        distributeV = new JMenuItem(Language.translate("Distribute Vertically"), Workspace.createImageIcon("resources/distribute_vcentre.png"));

        bringToFront.setIcon(Workspace.createImageIcon("resources/bring-to-front.png"));
        alignRegions.setIcon(Workspace.createImageIcon("resources/align-center.png"));
        sendToBack.setIcon(Workspace.createImageIcon("resources/send-back.png"));

        alignRegions.add(alignUp);
        alignRegions.add(alignCentered);
        alignRegions.add(alignBottom);
        alignRegions.addSeparator();
        alignRegions.add(alignLeft);
        alignRegions.add(alignCenter);
        alignRegions.add(alignRight);
        alignRegions.addSeparator();
        alignRegions.add(distributeH);
        alignRegions.add(distributeV);

        bringToFront.add(toFront);
        bringToFront.add(upwards);
        sendToBack.add(back);
        sendToBack.add(backwards);

        alignUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getRegionPopupListener().alignVertical(0);
            }
        });
        alignCentered.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getRegionPopupListener().alignVertical(1);
            }
        });
        alignBottom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getRegionPopupListener().alignVertical(2);
            }
        });
        alignLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getRegionPopupListener().alignHorizontal(0);
            }
        });
        alignCenter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getRegionPopupListener().alignHorizontal(1);
            }
        });
        alignRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getRegionPopupListener().alignHorizontal(2);
            }
        });
        distributeH.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getRegionPopupListener().distributeHorizontally();
            }
        });
        distributeV.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getRegionPopupListener().distributeVertically();
            }
        });

        editImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegionsFrame.showRegionsAndActions(0);
            }
        });
        editShape.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegionsFrame.showRegionsAndActions(1);
            }
        });
        mouseEvents.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegionsFrame.showRegionsAndActions(2);
            }
        });
        editText.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegionsFrame.showRegionsAndActions(3);
            }
        });
        align.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegionsFrame.showRegionsAndActions(4);
            }
        });
        transform.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegionsFrame.showRegionsAndActions(5);
            }
        });
        moveRotate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegionsFrame.showRegionsAndActions(6);
            }
        });
        trajectorySettings.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegionsFrame.showRegionsAndActions(7);
            }
        });
        upwards.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().moveCurrentActionUpwards();
            }
        });
        backwards.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().moveCurrentActionBackwards();
            }
        });
        asBackground.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().moveCurrentActionToBackground();
            }
        });
        toFront.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().moveCurrentActionToFront();
            }
        });
        back.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().moveCurrentActionToBack();
            }
        });

        JMenuItem menuAsMinXY = new JMenuItem(Language.translate("As Leftmost and Topmost Limit"), Workspace.createImageIcon("resources/go-top-left.png"));
        JMenuItem menuAsMinX = new JMenuItem(Language.translate("As Leftmost Limit"), Workspace.createImageIcon("resources/go-first.png"));
        JMenuItem menuAsMinY = new JMenuItem(Language.translate("As Topmost Limit"), Workspace.createImageIcon("resources/go-top.png"));
        JMenuItem menuAsMaxXY = new JMenuItem(Language.translate("As Rightmost and Bottommost Limit"), Workspace.createImageIcon("resources/go-bottom-right.png"));
        JMenuItem menuAsMaxX = new JMenuItem(Language.translate("As Rightmost Limit"), Workspace.createImageIcon("resources/go-last.png"));
        JMenuItem menuAsMaxY = new JMenuItem(Language.translate("As Bottommost Limit"), Workspace.createImageIcon("resources/go-bottom.png"));
        JMenuItem menuSizeAsWindowPos = new JMenuItem(Language.translate("As Image Clip Position"), Workspace.createImageIcon("resources/image-window.gif"));
        JMenuItem menuSizeAsWindowSize = new JMenuItem(Language.translate("As Image Clip Size"), Workspace.createImageIcon("resources/image-window.gif"));
        JMenuItem menuSizeAsCapturePos = new JMenuItem(Language.translate("As Capturing Area Position"), Workspace.createImageIcon("resources/computer.png"));
        JMenuItem menuSizeAsCaptureSize = new JMenuItem(Language.translate("As Capturing Area Size"), Workspace.createImageIcon("resources/computer.png"));

        menuAsMinXY.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();

                Rectangle r = a.getBounds(false);
                int x1 = (int) r.getX();
                int y1 = (int) r.getY();

                a.limits[0][1] = "" + InteractionSpace.getPhysicalX(x1);
                a.limits[1][1] = "" + InteractionSpace.getPhysicalY(y1);
                SketchletEditor.getInstance().repaint();
            }
        });
        menuAsMinX.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                Rectangle r = a.getBounds(false);
                int x1 = (int) r.getX();

                a.limits[0][1] = "" + InteractionSpace.getPhysicalX(x1);
                SketchletEditor.getInstance().repaint();
            }
        });
        menuAsMinY.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();

                Rectangle r = a.getBounds(false);
                int y1 = (int) r.getY();
                a.limits[1][1] = "" + InteractionSpace.getPhysicalY(y1);
                SketchletEditor.getInstance().repaint();
            }
        });
        menuAsMaxXY.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                Rectangle r = a.getBounds(false);
                int x1 = (int) r.getX();
                int y1 = (int) r.getY();
                int x2 = x1 + (int) r.getWidth();
                int y2 = y1 + (int) r.getHeight();

                a.limits[0][2] = "" + InteractionSpace.getPhysicalX(x2);
                a.limits[1][2] = "" + InteractionSpace.getPhysicalY(y2);

                SketchletEditor.getInstance().repaint();
            }
        });
        menuAsMaxX.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                Rectangle r = a.getBounds(false);
                int x1 = (int) r.getX();
                int x2 = x1 + (int) r.getWidth();

                a.limits[0][2] = "" + InteractionSpace.getPhysicalX(x2);
                SketchletEditor.getInstance().repaint();
            }
        });
        menuAsMaxY.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();

                Rectangle r = a.getBounds(false);
                int y1 = (int) r.getY();
                int y2 = y1 + (int) r.getHeight();

                a.limits[1][2] = "" + InteractionSpace.getPhysicalY(y2);
                SketchletEditor.getInstance().repaint();
            }
        });
        menuFixPosition.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                Rectangle r = a.getBounds(false);

                if (menuFixPosition.getText().startsWith("Lock")) {
                    a.setProperty("position x", "=" + a.getX(false));
                    a.setProperty("position y", "=" + a.getY(false));
                    menuFixPosition.setText(Language.translate("Unlock Position"));
                } else {
                    a.setProperty("position x", "");
                    a.setProperty("position y", "");
                    menuFixPosition.setText(Language.translate("Lock Position"));
                }
                SketchletEditor.getInstance().repaint();
            }
        });

        menuFixRotation.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                Rectangle r = a.getBounds(false);
                int y1 = (int) r.getY();

                if (menuFixRotation.getText().startsWith("Lock")) {
                    a.setProperty("rotation", "=" + a.getProperty("rotation"));
                    menuFixRotation.setText(Language.translate("Unlock Rotation"));
                } else {
                    a.setProperty("rotation", a.processText(a.getProperty("rotation")));
                    menuFixRotation.setText(Language.translate("Lock Rotation"));
                }
                SketchletEditor.getInstance().repaint();
            }
        });
        endTrajectory.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                if (a.inTrajectoryMode) {
                    a.getMouseController().processTrajectory();
                } else if (a.inTrajectoryMode2) {
                    a.getMouseController().processTrajectory2();
                }
            }
        });
        editTrajectory.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().goToTrajectoryMode();
            }
        });
        clearTrajectory.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                a.trajectory1 = "";
                a.trajectory2 = "";
                SketchletEditor.getInstance().repaint();
            }
        });
        stickToTrajectory.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                a.stickToTrajectoryEnabled = stickToTrajectory.isSelected();
                a.changingOrientationOnTrajectoryEnabled = stickToTrajectory.isSelected();
            }
        });
        clearTrajectory2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                a.trajectory2 = "";
                SketchletEditor.getInstance().repaint();
            }
        });
        menuUnfixRotation.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                Rectangle r = a.getBounds(false);
                int y1 = (int) r.getY();

                a.setProperty("rotation", "");
                SketchletEditor.getInstance().repaint();
            }
        });
        menuUnfixRotationPoint.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                a.center_rotation_x = 0.5;
                a.center_rotation_y = 0.5;
                SketchletEditor.getInstance().repaint();
            }
        });
        menuUnfixTrajectoryPoint.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                a.center_rotation_x = 0.5;
                a.center_rotation_y = 0.5;
                a.trajectory2_x = 0.4;
                a.trajectory2_y = 0.5;
                SketchletEditor.getInstance().repaint();
            }
        });
        menuUnfixMotionLimits.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                for (int i = 0; i < a.limits.length; i++) {
                    a.limits[i][1] = "";
                    a.limits[i][2] = "";
                }
                SketchletEditor.getInstance().repaint();
            }
        });
        menuFixSize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                Rectangle r = a.getBounds(false);
                int y1 = (int) r.getY();

                if (menuFixSize.getText().startsWith("Lock")) {
                    a.setProperty("width", "=" + a.getWidth());
                    a.setProperty("height", "=" + a.getHeight());
                    menuFixSize.setText("Unlock Size");
                } else {
                    a.setProperty("width", "");
                    a.setProperty("height", "");
                    menuFixSize.setText("Lock Size");
                }
                SketchletEditor.getInstance().repaint();
            }
        });

        menuFixX.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                Rectangle r = region.getBounds(false);
                int y1 = (int) r.getY();
                int y2 = y1 + (int) r.getHeight();

                if (menuFixX.getText().startsWith("Lock")) {
                    region.limits[1][1] = "" + InteractionSpace.getPhysicalY(y1);
                    region.limits[1][2] = "" + InteractionSpace.getPhysicalY(y2);
                    menuFixX.setText(Language.translate("Unlock Horizontal Position"));
                } else {
                    region.limits[1][1] = "";
                    region.limits[1][2] = "";
                    menuFixX.setText(Language.translate("Lock Horizontal Position"));
                }
                SketchletEditor.getInstance().repaint();
            }
        });

        menuFixY.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                Rectangle r = region.getBounds(false);
                int x1 = (int) r.getX();
                int x2 = x1 + (int) r.getWidth();

                if (menuFixY.getText().startsWith("Lock")) {
                    region.limits[0][1] = "" + InteractionSpace.getPhysicalX(x1);
                    region.limits[0][2] = "" + InteractionSpace.getPhysicalX(x2);
                    menuFixY.setText(Language.translate("Unlock Vertical Position"));
                } else {
                    region.limits[0][1] = "";
                    region.limits[0][2] = "";
                    menuFixY.setText(Language.translate("Lock Vertical Position"));
                }
                SketchletEditor.getInstance().repaint();
            }
        });

        menuSizeAsWindowPos.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();

                region.windowX = "" + (region.x2 - region.x1);
                region.windowY = "" + (region.y2 - region.y1);

                SketchletEditor.getInstance().repaint();
            }
        });

        menuSizeAsWindowSize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();

                region.windowWidth = "" + (region.x2 - region.x1);
                region.windowHeight = "" + (region.y2 - region.y1);
                SketchletEditor.getInstance().repaint();
            }
        });

        menuSizeAsCapturePos.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();

                region.captureScreenX = "" + region.x1;
                region.captureScreenY = "" + region.y1;
                SketchletEditor.getInstance().repaint();
            }
        });

        menuSizeAsCaptureSize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();

                region.captureScreenWidth = "" + (region.x2 - region.x1);
                region.captureScreenHeight = "" + (region.y2 - region.y1);
                SketchletEditor.getInstance().repaint();
            }
        });

        menuProperties.setIcon(Workspace.createImageIcon("resources/details.gif"));

        trajectoryGesture.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                if (region != null) {
                    region.startDefiningTrajectory(0);
                }
            }
        });

        trajectoryLine.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                if (region != null) {
                    region.startDefiningTrajectory(1);
                }
            }
        });

        trajectoryMultiLine.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                if (region != null) {
                    region.startDefiningTrajectory(2);
                }
            }
        });

        trajectoryOval.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                if (region != null) {
                    region.startDefiningTrajectory(3);
                }
            }
        });

        trajectory2Gesture.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                if (region != null) {
                    region.startDefiningTrajectory2(0);
                }
            }
        });

        trajectory2Line.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                if (region != null) {
                    region.startDefiningTrajectory2(1);
                }
            }
        });

        trajectory2MultiLine.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                if (region != null) {
                    region.startDefiningTrajectory2(2);
                }
            }
        });

        trajectory2MultiLine.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                if (region != null) {
                    region.startDefiningTrajectory2(2);
                }
            }
        });

        trajectory2Oval.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                if (region != null) {
                    region.startDefiningTrajectory2(3);
                }
            }
        });
        animator.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                if (region != null) {
                    new AnimationTimerDialog(region, SketchletEditor.getInstance().editorFrame);
                }
            }
        });

        menuProperties.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegionsFrame.showRegionsAndActions();
            }
        });

        entryActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                PageDetailsPanel.showStateProperties(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsOnEntrySubtabIndex);
            }
        });
        exitActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                PageDetailsPanel.showStateProperties(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsOnExitSubtabIndex);
            }
        });
        variablesActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                PageDetailsPanel.showStateProperties(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsVariablesSubtabIndex);
            }
        });
        keyboardActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                PageDetailsPanel.showStateProperties(PageDetailsPanel.actionsTabIndex, PageDetailsPanel.actionsKeyboardSubtabIndex);
            }
        });
        refresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().refresh();
            }
        });

        pasteImageBackground.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getEditorClipboardController().pasteImageFromClipboard(true, 0, 0);
            }
        });

        pasteImageHere.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getEditorClipboardController().pasteImageFromClipboard(false, x, y);
            }
        });

        pasteImageAsRegion.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getEditorClipboardController().pasteImageAsRegion(x, y);
            }
        });

        pasteRegion.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.getInstance().getEditorClipboardController().pasteRegion(x, y);
            }
        });

        trajectoryFromRegion.setIcon(Workspace.createImageIcon("resources/active_region.png"));
        trajectory2FromRegion.setIcon(Workspace.createImageIcon("resources/active_region.png"));

        pasteImageMain.add(pasteImageAsRegion);
        pasteImageMain.add(pasteImageHere);
        pasteImageMain.addSeparator();
        pasteImageMain.add(pasteImageBackground);

        popupMenuMain.add(pasteImageMain);
        popupMenuMain.addSeparator();
        popupMenuMain.add(pasteRegion);
        popupMenuMain.addSeparator();
        actions.add(entryActions);
        actions.add(exitActions);
        actions.addSeparator();
        actions.add(variablesActions);
        actions.addSeparator();
        actions.add(keyboardActions);

        popupMenuMain.add(actions);
        popupMenuMain.addSeparator();
        popupMenuMain.add(refresh);
    }

    int x = -1;
    int y = -1;
    ActiveRegion a = null;
    int x1, y1;
    int x2, y2;
    double rotation;

    public void fillMenu() {
        this.setEnabled(true);
        this.removeAll();
        ActiveRegion a;
        if (SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getInstance().getCurrentPage().getRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0) {
            a = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
        } else {
            return;
        }
        this.add(menuProperties);
        this.addSeparator();
        reset.removeAll();
        this.add(extract);
        this.add(extractNewFrame);
        this.addSeparator();
        this.add(stamp);
        this.addSeparator();
        this.add(fix);
        reset.setIcon(Workspace.createImageIcon("resources/zero.png"));
        reset.add(menuUnfixRotation);
        reset.addSeparator();
        reset.add(menuUnfixRotationPoint);
        reset.add(menuUnfixTrajectoryPoint);
        reset.addSeparator();
        reset.add(menuUnfixMotionLimits);
        this.add(reset);
        this.addSeparator();
        //this.add(wizards);
        //this.addSeparator();
        this.add(defineClip);
        this.addSeparator();
        this.add(trajectoryMenu);
        if (!a.trajectory1.trim().isEmpty()) {
            this.add(trajectory2Menu);
            editTrajectoryMenu.removeAll();
            editTrajectoryMenu.add(editTrajectory);
            editTrajectoryMenu.addSeparator();
            editTrajectoryMenu.add(clearTrajectory);
            this.add(editTrajectoryMenu);
            stickToTrajectory.setSelected(a.stickToTrajectoryEnabled);
            this.add(stickToTrajectory);
        }
        if (!a.trajectory2.trim().isEmpty()) {
            editTrajectoryMenu.add(clearTrajectory2);
        }

        this.addSeparator();
        this.add(group);
        this.addSeparator();
        this.add(alignRegions);
        this.addSeparator();
        this.add(bringToFront);
        this.add(sendToBack);
        this.add(asBackground);
        this.addSeparator();
        this.add(delete);
        prepareMenu();
        return;
    }

    private void prepareMenu() {
        if (!(SketchletEditor.getInstance() != null && SketchletEditor.getInstance().getCurrentPage() != null && SketchletEditor.getInstance().getCurrentPage().getRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null)) {
            return;
        }

        SketchletEditor.getInstance().getRegionPopupListener().prepareCopyTrajecotryFromRegion();
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clip.getContents(null);

        boolean imageOnClipboard = transferable.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));

        if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 0 && SketchletEditor.getInstance().getMode() == SketchletEditorMode.ACTIONS) {
            if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null && SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().size() > 1) {
                boolean bGroup = false;
                for (ActiveRegion as : SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions()) {
                    if (as.regionGrouping.equals("") || !as.regionGrouping.equals(SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().firstElement().regionGrouping)) {
                        bGroup = true;
                        break;
                    }
                }
                if (bGroup) {
                    group.setText(Language.translate("Group"));
                } else {
                    group.setText(Language.translate("Ungroup"));
                }
                group.setEnabled(true);
                alignRegions.setEnabled(true);
            } else {
                group.setEnabled(false);
                alignRegions.setEnabled(false);
            }

            pasteImage.setEnabled(imageOnClipboard);
            if (SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions() != null) {
                ActiveRegion region = SketchletEditor.getInstance().getCurrentPage().getRegions().getMouseHelper().getSelectedRegions().lastElement();
                int index = region.parent.getRegions().indexOf(region);
                int size = region.parent.getRegions().size();

                this.sendToBack.setEnabled(index < size - 1);
                this.bringToFront.setEnabled(index > 0);

                if (region.getProperty("width").startsWith("=") || region.getProperty("height").startsWith("=")) {
                    menuFixSize.setText(Language.translate("Unlock Size"));
                } else {
                    menuFixSize.setText(Language.translate("Lock Size"));
                }
                if (region.getProperty("rotation").startsWith("=")) {
                    menuFixRotation.setText(Language.translate("Unlock Rotation"));
                } else {
                    menuFixRotation.setText(Language.translate("Lock Rotation"));
                }
                if (region.getProperty("position x").startsWith("=") || region.getProperty("position y").startsWith("=")) {
                    menuFixPosition.setText(Language.translate("Unlock Position"));
                } else {
                    menuFixPosition.setText(Language.translate("Lock Position"));
                }
                if (!region.limits[1][1].toString().isEmpty() || !region.limits[1][2].toString().isEmpty()) {
                    menuFixX.setText(Language.translate("Unlock Horizontal Position"));
                } else {
                    menuFixX.setText(Language.translate("Lock Horizontal Position"));
                }
                if (!region.limits[0][1].toString().isEmpty() || !region.limits[0][2].toString().isEmpty()) {
                    menuFixY.setText(Language.translate("Unlock Vertical Position"));
                } else {
                    menuFixY.setText(Language.translate("Lock Vertical Position"));
                }
            }
        } else {
            pasteImageMain.setEnabled(imageOnClipboard);
            pasteRegion.setEnabled(SketchletEditor.getInstance().copiedActions != null && SketchletEditor.getInstance().copiedActions.size() > 0);
        }
    }
}
