/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.regions;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.communicator.server.DataServer;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.animation.AnimationTimer;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.EditorMode;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorUtils;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.programming.timers.Timer;
import net.sf.sketchlet.designer.ui.page.SketchStatePanel;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.designer.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.designer.ui.toolbars.ModeToolbar;
import net.sf.sketchlet.designer.ui.wizard.WizActiveRegionEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class ActiveRegionPopupListener extends MouseAdapter {

    JMenuItem delete = new JMenuItem(Language.translate("Remove"), Workspace.createImageIcon("resources/user-trash.png"));
    JMenuItem group = new JMenuItem(Language.translate("Group"), Workspace.createImageIcon("resources/system-users.png"));
    JMenuItem extract;
    JMenuItem extractNewFrame;
    JMenuItem defineClip;
    JMenuItem stamp;
    JMenuItem copyRegion;
    JMenuItem copyRegionImage;
    JMenuItem saveRegionImage;
    JMenuItem printRegionImage;
    JMenuItem pasteImage;
    JMenuItem pasteText;
    JMenuItem mouseEvents;
    JMenuItem editImage;
    JMenuItem editText;
    JMenuItem align;
    JMenuItem transform;
    JMenuItem moveRotate;
    JMenuItem upwards;
    JMenuItem backwards;
    JMenuItem asBackground;
    JMenuItem alignUp;
    JMenuItem alignCentered;
    JMenuItem alignBottom;
    JMenuItem alignLeft;
    JMenuItem alignCenter;
    JMenuItem alignRight;
    JMenuItem distributeH;
    JMenuItem distributeV;
    JMenuItem sameSize;
    JMenuItem sameW;
    JMenuItem sameH;
    JMenuItem menuSizeReset = new JMenuItem(Language.translate("Reset Limits and Orientation"), Workspace.createImageIcon("resources/no_limits.png"));
    JMenu actions = new JMenu(Language.translate("Edit Page Events"));
    JMenu pasteImageMain = new JMenu(Language.translate("Paste Image"));
    JMenuItem entryActions = new JMenuItem(Language.translate("On Page Entry"), Workspace.createImageIcon("resources/import.gif"));
    JMenuItem exitActions = new JMenuItem(Language.translate("On Page Exit"), Workspace.createImageIcon("resources/export.gif"));
    JMenuItem variablesActions = new JMenuItem(Language.translate("On Variable Update"), Workspace.createImageIcon("resources/variables.png"));
    JMenuItem keyboardActions = new JMenuItem(Language.translate("On Keyboard Events"), Workspace.createImageIcon("resources/keyboard.png"));
    JMenuItem refresh = new JMenuItem(Language.translate("Refresh"), Workspace.createImageIcon("resources/view-refresh.png"));
    JMenuItem pasteImageBackground = new JMenuItem(Language.translate("Paste Image as Background"), Workspace.createImageIcon("resources/edit-paste.png"));
    JMenuItem pasteImageHere = new JMenuItem(Language.translate("Paste Image Here"), Workspace.createImageIcon("resources/edit-paste.png"));
    JMenuItem pasteImageAsRegion = new JMenuItem(Language.translate("Paste Image as New Region"), Workspace.createImageIcon("resources/edit-paste.png"));
    JMenuItem pasteRegion = new JMenuItem(Language.translate("Paste Region Here"), Workspace.createImageIcon("resources/edit-paste.png"));
    JMenu alignRegions = new JMenu(Language.translate("Align"));
    JMenu bringToFront = new JMenu(Language.translate("Bring to Front"));
    JMenu sendToBack = new JMenu(Language.translate("Send Back"));
    // JMenu imageOperations = new JMenu("Image");
    JMenuItem toFront = new JMenuItem(Language.translate("Bring to Front"), Workspace.createImageIcon("resources/bring-to-front.png"));
    JMenuItem back = new JMenuItem(Language.translate("Send To Back"), Workspace.createImageIcon("resources/send-back.png"));
    JPopupMenu popupMenuMain = new JPopupMenu();
    JMenu trajectoryMenu = new JMenu(Language.translate("Define Trajectory"));
    JMenu trajectory2Menu = new JMenu(Language.translate("Define Secondary Trajectory"));
    JMenu editTrajectoryMenu = new JMenu(Language.translate("Edit/Clear Trajectories"));
    JMenuItem clearTrajectory = new JMenuItem(Language.translate("Clear Trajectory"), Workspace.createImageIcon("resources/edit-clear.png"));
    JCheckBoxMenuItem stickToTrajectory = new JCheckBoxMenuItem(Language.translate("Stick to Trajectory"));
    JMenuItem clearTrajectory2 = new JMenuItem(Language.translate("Clear Secondary Trajectory"), Workspace.createImageIcon("resources/edit-clear.png"));
    JMenuItem editTrajectory = new JMenuItem(Language.translate("Edit Trajectory Points"), Workspace.createImageIcon("resources/trajectory.png"));
    JMenuItem endTrajectory = new JMenuItem(Language.translate("End Trajectory"), Workspace.createImageIcon("resources/trajectory.png"));
    JMenuItem trajectoryGesture = new JMenuItem(Language.translate("With Freehand Gesture"), Workspace.createImageIcon("resources/pencil.gif"));
    JMenuItem trajectoryLine = new JMenuItem(Language.translate("As a Line"), Workspace.createImageIcon("resources/line_2.png"));
    JMenuItem trajectoryMultiLine = new JMenuItem(Language.translate("As a Multiline"), Workspace.createImageIcon("resources/line_multi.png"));
    JMenuItem trajectoryOval = new JMenuItem(Language.translate("As an Oval"), Workspace.createImageIcon("resources/oval.png"));
    JMenu trajectoryFromRegion = new JMenu(Language.translate("Copy from Region"));
    JMenu trajectoryAnimate = new JMenu(Language.translate("Animate Trajectory"));
    JMenuItem trajectoryAnimateTimer = new JMenuItem(Language.translate("Create Timer"));
    JMenu trajectoryAnimateRegions = new JMenu(Language.translate("Connect to Other Region"));
    JMenu trajectory2FromRegion = new JMenu(Language.translate("Copy from Region"));
    JMenuItem trajectory2Gesture = new JMenuItem(Language.translate("With Freehand Gesture"), Workspace.createImageIcon("resources/pencil.gif"));
    JMenuItem trajectory2Line = new JMenuItem(Language.translate("As a Line"), Workspace.createImageIcon("resources/line_2.png"));
    JMenuItem trajectory2MultiLine = new JMenuItem(Language.translate("As a Multiline"), Workspace.createImageIcon("resources/line_multi.png"));
    JMenuItem trajectory2Oval = new JMenuItem(Language.translate("As an Oval"), Workspace.createImageIcon("resources/oval.png"));
    JMenuItem animator = new JMenuItem(Language.translate("Animate..."), Workspace.createImageIcon("resources/timer.png"));
    JMenuItem menuProperties = new JMenuItem(Language.translate("Region Properties"));
    JMenu wizards = new JMenu(Language.translate("Wizards"));
    JMenu fix = new JMenu(Language.translate("Lock / Unlock"));
    public JMenuItem mouseWizard = new JMenuItem(Language.translate("region mouse event wizard..."), Workspace.createImageIcon("resources/mouse.png"));
    public JMenuItem interactionWizard = new JMenuItem(Language.translate("region interaction wizard..."), Workspace.createImageIcon("resources/interaction.png"));
    public JMenuItem animationWizard = new JMenuItem(Language.translate("region animation wizard..."), Workspace.createImageIcon("resources/timer.png"));
    public JMenuItem imageExplorer = new JMenuItem(Language.translate("create interactive image window"), Workspace.createImageIcon("resources/wizard.png"));
    JMenuItem menuFixPosition = new JMenuItem(Language.translate("Lock Position"), Workspace.createImageIcon("resources/fix-position.png"));
    JMenuItem menuFixRotation = new JMenuItem(Language.translate("Lock Rotation"), Workspace.createImageIcon("resources/fix-rotation.png"));
    JMenuItem menuFixSize = new JMenuItem(Language.translate("Lock Size"), Workspace.createImageIcon("resources/fix-size.png"));
    JMenuItem menuFixX = new JMenuItem(Language.translate("Lock Horizontal Position"), Workspace.createImageIcon("resources/fix-x.png"));
    JMenuItem menuFixY = new JMenuItem(Language.translate("Lock Vertical Position"), Workspace.createImageIcon("resources/fix-y.png"));
    //JMenuItem menuUnfixPosition = new JMenuItem("Reset Position Lock", Workspace.createImageIcon("resources/fix-position.png"));
    JMenu reset = new JMenu(Language.translate("Reset"));
    JMenuItem menuUnfixRotation = new JMenuItem(Language.translate("Reset Rotation"), Workspace.createImageIcon("resources/zero.png"));
    JMenuItem menuUnfixRotationPoint = new JMenuItem(Language.translate("Reset Rotation Point"), Workspace.createImageIcon("resources/zero.png"));
    JMenuItem menuUnfixTrajectoryPoint = new JMenuItem(Language.translate("Reset Trajectory Points"), Workspace.createImageIcon("resources/trajectory.png"));
    JMenuItem menuUnfixMotionLimits = new JMenuItem(Language.translate("Reset Motion Limits"), Workspace.createImageIcon("resources/move_rotate.png"));
    //JMenuItem menuUnfixSize = new JMenuItem("Reset Size Lock", Workspace.createImageIcon("resources/fix-size.png"));
    //JMenuItem menuUnfixX = new JMenuItem("Reset Horizontal Position Lock", Workspace.createImageIcon("resources/fix-x.png"));
    //JMenuItem menuUnfixY = new JMenuItem("Reset Vertical Position Lock", Workspace.createImageIcon("resources/fix-y.png"));

    public ActiveRegionPopupListener() {
        actions.setIcon(Workspace.createImageIcon("resources/applications-system.png"));
        pasteImageMain.setIcon(Workspace.createImageIcon("resources/edit-paste.png"));
        extract = new JMenuItem(Language.translate("Extract Image From Background"), Workspace.createImageIcon("resources/edit-cut.png"));
        extract.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.extract(0);
            }
        });
        trajectoryAnimateTimer.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                Timer t = SketchletEditor.editorPanel.extraEditorPanel.timersExtraPanel.newTimer();
                ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (region.trajectoryType == 0) {
                    t.strDurationInSec = "2.0";
                    if (t.panel != null) {
                        t.panel.fieldDuration.setText(t.strDurationInSec + "");
                    }
                }
                String newVariable = DataServer.variablesServer.getUniqueVariableName("trajectory");
                t.variables[0][0] = newVariable;
                t.variables[0][1] = "0.0";
                t.variables[0][2] = "1.0";

                region.setProperty("trajectory position", "=" + newVariable);
            }
        });
        extractNewFrame = new JMenuItem(Language.translate("Extract Image in a New Frame"), Workspace.createImageIcon("resources/edit-cut.png"));
        extractNewFrame.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.extract(-1);
            }
        });
        defineClip = new JMenuItem(Language.translate("Define Visible Area"), Workspace.createImageIcon("resources/clip.png"));
        defineClip.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.defineClip();
            }
        });
        stamp = new JMenuItem(Language.translate("Stamp Image On Background"), Workspace.createImageIcon("resources/stamp.png"));
        stamp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.stamp();
            }
        });

        extract.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        extractNewFrame.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, ActionEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        stamp.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_P, ActionEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        delete.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.deleteSelectedRegion();
            }
        });
        group.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.groupSelectedRegion();
            }
        });
        pasteImage = new JMenuItem(Language.translate("Paste Image in Region"), Workspace.createImageIcon("resources/edit-paste.png"));
        pasteImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.editorClipboard.fromClipboardCurrentAction(0);
            }
        });

        copyRegion = new JMenuItem(Language.translate("Copy Region"), Workspace.createImageIcon("resources/edit-copy.png"));
        copyRegion.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.editorClipboard.copySelectedAction();
            }
        });

        copyRegionImage = new JMenuItem(Language.translate("Copy Rendered Image to Clipboard"), Workspace.createImageIcon("resources/edit-copy.png"));
        copyRegionImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.editorClipboard.copyRegionImageToClipboard();
            }
        });

        saveRegionImage = new JMenuItem(Language.translate("Save Rendered Image to File..."), Workspace.createImageIcon("resources/save.gif"));
        saveRegionImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.editorClipboard.saveRegionImageToFile();
            }
        });

        printRegionImage = new JMenuItem(Language.translate("Print Rendered Image..."), Workspace.createImageIcon("resources/printer.png"));
        printRegionImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.editorClipboard.printRegionImageToClipboard();
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
                        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 0) {
                            ActiveRegion action = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                            action.strText = (String) contents.getTransferData(DataFlavor.stringFlavor);
                            SketchletEditor.editorPanel.repaint();
                        }
                    } catch (UnsupportedFlavorException ex) {
                    } catch (IOException ex) {
                    }

                    SketchletEditor.editorPanel.repaint();
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
        //fix.addSeparator();
        //fix.add(menuUnfixPosition);
        //fix.add(menuUnfixX);
        //fix.add(menuUnfixY);
        //fix.add(menuUnfixSize);
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
                if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null) {
                    WizActiveRegionEvent.showWizard(1, "Region Mouse Wizard");
                    SketchletEditor.editorPanel.sketchToolbar.enableControls();
                }
            }
        });

        interactionWizard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null) {
                    WizActiveRegionEvent.showWizard(2, "Region Interaction Wizard");
                    SketchletEditor.editorPanel.sketchToolbar.enableControls();
                }
            }
        });
        animationWizard.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null) {
                    new AnimationTimer(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement(), SketchletEditor.editorPanel.editorFrame);
                    SketchletEditor.editorPanel.sketchToolbar.enableControls();
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
        sameSize = new JMenuItem(Language.translate("Make Same Size"), Workspace.createImageIcon("resources/resize.png"));
        sameW = new JMenuItem(Language.translate("Make Same Width"), Workspace.createImageIcon("resources/resize_w.png"));
        sameH = new JMenuItem(Language.translate("Make Same Height"), Workspace.createImageIcon("resources/resize_h.png"));

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
        alignRegions.addSeparator();
        alignRegions.add(sameSize);
        alignRegions.add(sameW);
        alignRegions.add(sameH);

        bringToFront.add(toFront);
        bringToFront.add(upwards);
        sendToBack.add(back);
        sendToBack.add(backwards);

        alignUp.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                alignVertical(0);
            }
        });
        alignCentered.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                alignVertical(1);
            }
        });
        alignBottom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                alignVertical(2);
            }
        });
        alignLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                alignHorizontal(0);
            }
        });
        alignCenter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                alignHorizontal(1);
            }
        });
        alignRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                alignHorizontal(2);
            }
        });
        distributeH.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.regionPopupListener.distributeHorizontally();
            }
        });
        distributeV.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.regionPopupListener.distributeVertically();
            }
        });
        sameSize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.regionPopupListener.sameSize();
            }
        });
        sameW.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.regionPopupListener.sameWidth();
            }
        });
        sameH.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.regionPopupListener.sameHeight();
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
                SketchletEditor.editorPanel.moveCurrentActionUpwards();
            }
        });
        backwards.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.moveCurrentActionBackwards();
            }
        });
        asBackground.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.moveCurrentActionToBackground();
            }
        });
        toFront.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.moveCurrentActionToFront();
            }
        });
        back.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.moveCurrentActionToBack();
            }
        });
        //imageOperations.add(extract);
        //imageOperations.add(stamp);
        //imageOperations.addSeparator();
        //imageOperations.add(defineClip);

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
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();

                Rectangle r = a.getBounds(false);
                int x1 = (int) r.getX();
                int y1 = (int) r.getY();

                a.limits[0][1] = "" + InteractionSpace.getPhysicalX(x1);
                a.limits[1][1] = "" + InteractionSpace.getPhysicalY(y1);
                SketchletEditor.editorPanel.repaint();
            }
        });
        menuAsMinX.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                Rectangle r = a.getBounds(false);
                int x1 = (int) r.getX();

                a.limits[0][1] = "" + InteractionSpace.getPhysicalX(x1);
                SketchletEditor.editorPanel.repaint();
            }
        });
        menuAsMinY.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();

                Rectangle r = a.getBounds(false);
                int y1 = (int) r.getY();
                a.limits[1][1] = "" + InteractionSpace.getPhysicalY(y1);
                SketchletEditor.editorPanel.repaint();
            }
        });
        menuAsMaxXY.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                Rectangle r = a.getBounds(false);
                int x1 = (int) r.getX();
                int y1 = (int) r.getY();
                int x2 = x1 + (int) r.getWidth();
                int y2 = y1 + (int) r.getHeight();

                a.limits[0][2] = "" + InteractionSpace.getPhysicalX(x2);
                a.limits[1][2] = "" + InteractionSpace.getPhysicalY(y2);

                SketchletEditor.editorPanel.repaint();
            }
        });
        menuAsMaxX.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                Rectangle r = a.getBounds(false);
                int x1 = (int) r.getX();
                int x2 = x1 + (int) r.getWidth();

                a.limits[0][2] = "" + InteractionSpace.getPhysicalX(x2);
                SketchletEditor.editorPanel.repaint();
            }
        });
        menuAsMaxY.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();

                Rectangle r = a.getBounds(false);
                int y1 = (int) r.getY();
                int y2 = y1 + (int) r.getHeight();

                a.limits[1][2] = "" + InteractionSpace.getPhysicalY(y2);
                SketchletEditor.editorPanel.repaint();
            }
        });
        menuFixPosition.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                Rectangle r = a.getBounds(false);

                if (menuFixPosition.getText().startsWith("Lock")) {
                    a.setProperty("position x", "=" + a.getX(false));
                    a.setProperty("position y", "=" + a.getY(false));
                    menuFixPosition.setText("Unlock Position");
                } else {
                    a.setProperty("position x", "");
                    a.setProperty("position y", "");
                    menuFixPosition.setText("Lock Position");
                }
                SketchletEditor.editorPanel.repaint();
            }
        });
        /*menuUnfixPosition.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent event) {
        ActiveRegion a = SketchletEditor.editorPanel.currentSketch.actions.selectedActions.lastElement();
        Rectangle r = a.getBounds(false);
        int y1 = (int) r.getY();
        int y2 = y1 + (int) r.getHeight();
        
        a.setProperty("position x", "");
        a.setProperty("position y", "");
        SketchletEditor.editorPanel.repaint();
        }
        });*/
        menuFixRotation.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                Rectangle r = a.getBounds(false);
                int y1 = (int) r.getY();
                int y2 = y1 + (int) r.getHeight();

                if (menuFixRotation.getText().startsWith("Lock")) {
                    a.setProperty("rotation", "=" + a.getProperty("rotation"));
                    menuFixRotation.setText(Language.translate("Unlock Rotation"));
                } else {
                    a.setProperty("rotation", a.processText(a.getProperty("rotation")));
                    menuFixRotation.setText(Language.translate("Lock Rotation"));
                }
                SketchletEditor.editorPanel.repaint();
            }
        });
        endTrajectory.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (a.inTrajectoryMode) {
                    a.mouseHandler.processTrajectory();
                } else if (a.inTrajectoryMode2) {
                    a.mouseHandler.processTrajectory2();
                }
            }
        });
        editTrajectory.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.goToTrajectoryMode();
            }
        });
        clearTrajectory.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                a.strTrajectory1 = "";
                a.strTrajectory2 = "";
                SketchletEditor.editorPanel.repaint();
            }
        });
        stickToTrajectory.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                a.bStickToTrajectory = stickToTrajectory.isSelected();
                a.bOrientationTrajectory = stickToTrajectory.isSelected();
            }
        });
        clearTrajectory2.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                a.strTrajectory2 = "";
                SketchletEditor.editorPanel.repaint();
            }
        });
        menuUnfixRotation.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                Rectangle r = a.getBounds(false);
                int y1 = (int) r.getY();
                int y2 = y1 + (int) r.getHeight();

                a.setProperty("rotation", "");
                SketchletEditor.editorPanel.repaint();
            }
        });
        menuUnfixRotationPoint.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                a.center_rotation_x = 0.5;
                a.center_rotation_y = 0.5;
                SketchletEditor.editorPanel.repaint();
            }
        });
        menuUnfixTrajectoryPoint.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                a.center_rotation_x = 0.5;
                a.center_rotation_y = 0.5;
                a.trajectory2_x = 0.4;
                a.trajectory2_y = 0.5;
                SketchletEditor.editorPanel.repaint();
            }
        });
        menuUnfixMotionLimits.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                for (int i = 0; i < a.limits.length; i++) {
                    a.limits[i][1] = "";
                    a.limits[i][2] = "";
                }
                SketchletEditor.editorPanel.repaint();
            }
        });
        menuFixSize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                Rectangle r = a.getBounds(false);
                int y1 = (int) r.getY();
                int y2 = y1 + (int) r.getHeight();

                if (menuFixSize.getText().startsWith("Lock")) {
                    a.setProperty("width", "=" + a.getWidth());
                    a.setProperty("height", "=" + a.getHeight());
                    menuFixSize.setText(Language.translate("Unlock Size"));
                } else {
                    a.setProperty("width", "");
                    a.setProperty("height", "");
                    menuFixSize.setText(Language.translate("Lock Size"));
                }
                SketchletEditor.editorPanel.repaint();
            }
        });
        /*
        menuUnfixSize.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent event) {
        ActiveRegion a = SketchletEditor.editorPanel.currentSketch.actions.selectedActions.lastElement();
        Rectangle r = a.getBounds(false);
        int y1 = (int) r.getY();
        int y2 = y1 + (int) r.getHeight();
        
        a.setProperty("width", "");
        a.setProperty("height", "");
        SketchletEditor.editorPanel.repaint();
        }
        });*/
        menuFixX.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                Rectangle r = a.getBounds(false);
                int y1 = (int) r.getY();
                int y2 = y1 + (int) r.getHeight();

                if (menuFixX.getText().startsWith("Lock")) {
                    a.limits[1][1] = "" + InteractionSpace.getPhysicalY(y1);
                    a.limits[1][2] = "" + InteractionSpace.getPhysicalY(y2);
                    menuFixX.setText(Language.translate("Unlock Horizontal Position"));
                } else {
                    a.limits[1][1] = "";
                    a.limits[1][2] = "";
                    menuFixX.setText(Language.translate("Lock Horizontal Position"));
                }
                SketchletEditor.editorPanel.repaint();
            }
        });
        /*menuUnfixX.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent event) {
        ActiveRegion a = SketchletEditor.editorPanel.currentSketch.actions.selectedActions.lastElement();
        Rectangle r = a.getBounds(false);
        int y1 = (int) r.getY();
        int y2 = y1 + (int) r.getHeight();
        
        a.limits[1][1] = "";
        a.limits[1][2] = "";
        SketchletEditor.editorPanel.repaint();
        }
        });*/
        menuFixY.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                Rectangle r = a.getBounds(false);
                int x1 = (int) r.getX();
                int x2 = x1 + (int) r.getWidth();

                if (menuFixY.getText().startsWith("Lock")) {
                    a.limits[0][1] = "" + InteractionSpace.getPhysicalX(x1);
                    a.limits[0][2] = "" + InteractionSpace.getPhysicalX(x2);
                    menuFixY.setText(Language.translate("Unlock Vertical Position"));
                } else {
                    a.limits[0][1] = "";
                    a.limits[0][2] = "";
                    menuFixY.setText(Language.translate("Lock Vertical Position"));
                }
                SketchletEditor.editorPanel.repaint();
            }
        });
        /*menuUnfixY.addActionListener(new ActionListener() {
        
        public void actionPerformed(ActionEvent event) {
        ActiveRegion a = SketchletEditor.editorPanel.currentSketch.actions.selectedActions.lastElement();
        Rectangle r = a.getBounds(false);
        int x1 = (int) r.getX();
        int x2 = x1 + (int) r.getWidth();
        
        a.limits[0][1] = "";
        a.limits[0][2] = "";
        SketchletEditor.editorPanel.repaint();
        }
        });*/

        menuSizeAsWindowPos.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();

                a.strWindowX = "" + (a.x2 - a.x1);
                a.strWindowY = "" + (a.y2 - a.y1);

                SketchletEditor.editorPanel.repaint();
            }
        });

        menuSizeAsWindowSize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();

                a.strWindowWidth = "" + (a.x2 - a.x1);
                a.strWindowHeight = "" + (a.y2 - a.y1);
                SketchletEditor.editorPanel.repaint();
            }
        });

        menuSizeAsCapturePos.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();

                //a.captureScreenX.setSelectedItem("" + a.x1);
                //a.captureScreenY.setSelectedItem("" + a.y1);
                a.strCaptureScreenX = "" + a.x1;
                a.strCaptureScreenY = "" + a.y1;
                SketchletEditor.editorPanel.repaint();
            }
        });

        menuSizeAsCaptureSize.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();

                //a.captureScreenWidth.setSelectedItem("" + (a.x2 - a.x1));
                //a.captureScreenHeight.setSelectedItem("" + (a.y2 - a.y1));
                a.strCaptureScreenWidth = "" + (a.x2 - a.x1);
                a.strCaptureScreenHeight = "" + (a.y2 - a.y1);
                SketchletEditor.editorPanel.repaint();
            }
        });

        menuProperties.setIcon(Workspace.createImageIcon("resources/details.gif"));

        trajectoryGesture.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion action = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (action != null) {
                    action.startDefiningTrajectory(0);
                }
            }
        });

        trajectoryLine.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion action = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (action != null) {
                    action.startDefiningTrajectory(1);
                }
            }
        });

        trajectoryMultiLine.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion action = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (action != null) {
                    action.startDefiningTrajectory(2);
                }
            }
        });

        trajectoryOval.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion action = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (action != null) {
                    action.startDefiningTrajectory(3);
                }
            }
        });

        trajectory2Gesture.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion action = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (action != null) {
                    action.startDefiningTrajectory2(0);
                }
            }
        });

        trajectory2Line.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion action = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (action != null) {
                    action.startDefiningTrajectory2(1);
                }
            }
        });

        trajectory2MultiLine.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion action = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (action != null) {
                    action.startDefiningTrajectory2(2);
                }
            }
        });

        trajectory2Oval.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion action = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (action != null) {
                    action.startDefiningTrajectory2(3);
                }
            }
        });
        animator.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                ActiveRegion action = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (action != null) {
                    new AnimationTimer(action, SketchletEditor.editorPanel.editorFrame);
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
                SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnEntrySubtabIndex);
            }
        });
        exitActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsOnExitSubtabIndex);
            }
        });
        variablesActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsVariablesSubtabIndex);
            }
        });
        keyboardActions.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchStatePanel.showStateProperties(SketchStatePanel.actionsTabIndex, SketchStatePanel.actionsKeyboardSubtabIndex);
            }
        });
        refresh.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.refresh();
            }
        });

        pasteImageBackground.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.editorClipboard.pasteImageFromClipboard(true, 0, 0);
            }
        });

        pasteImageHere.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.editorClipboard.pasteImageFromClipboard(false, x, y);
            }
        });

        pasteImageAsRegion.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.editorClipboard.pasteImageAsRegion(x, y);
            }
        });

        pasteRegion.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.editorClipboard.pasteRegion(x, y);
            }
        });

        trajectoryAnimate.setIcon(Workspace.createImageIcon("resources/animate.png"));
        trajectoryAnimateTimer.setIcon(Workspace.createImageIcon("resources/timer.png"));
        trajectoryAnimateRegions.setIcon(Workspace.createImageIcon("resources/active_region.png"));
        trajectoryAnimate.removeAll();
        trajectoryAnimate.add(trajectoryAnimateTimer);
        trajectoryAnimate.add(trajectoryAnimateRegions);
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
        TutorialPanel.prepare(popupMenuMain, true);
    }

    int x = -1;
    int y = -1;
    ActiveRegion a = null;
    int x1, y1;
    int x2, y2;
    double rotation;

    public void alignHorizontal(int align) {
        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 1) {
            int x1 = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.firstElement().x1;
            int x2 = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.firstElement().x2;
            int center = x1 + (x2 - x1) / 2;

            for (int i = 1; i < SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size(); i++) {
                ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.elementAt(i);
                int w = region.x2 - region.x1;
                switch (align) {
                    case 0:
                        region.x1 = x1;
                        region.x2 = region.x1 + w;
                        break;
                    case 1:
                        region.x1 = center - w / 2;
                        region.x2 = region.x1 + w;
                        break;
                    case 2:
                        region.x2 = x2;
                        region.x1 = region.x2 - w;
                        break;
                }
            }
            SketchletEditor.editorPanel.repaint();
        }
    }

    public void distributeHorizontally() {
        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 2) {
            int rcount = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size();
            ActiveRegion first = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.firstElement();
            ActiveRegion last = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
            int x1 = first.x1;
            int x2 = last.x2;

            int totalW = 0;
            Vector<ActiveRegion> rs = new Vector<ActiveRegion>();
            for (int i = 0; i < rcount; i++) {
                ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.elementAt(i);
                int w = region.getWidth();
                totalW += w;

                if (region.x1 < x1) {
                    x1 = region.x1;
                }

                if (region.x2 > x2) {
                    x2 = region.x2;
                }

                boolean bInserted = false;
                for (int j = 0; j < rs.size(); j++) {
                    if (rs.elementAt(j).x1 > region.x1) {
                        rs.insertElementAt(region, j);
                        bInserted = true;
                        break;
                    }
                }
                if (!bInserted) {
                    rs.add(region);
                }
            }

            int dW = (Math.abs(x1 - x2) - totalW) / (rcount - 1);

            if (dW <= 0) {
                return;
            }
            for (int i = 1; i < rs.size() - 1; i++) {
                ActiveRegion region1 = rs.elementAt(i - 1);
                ActiveRegion region2 = rs.elementAt(i);
                int w = region2.x2 - region2.x1;
                region2.x1 = region1.x2 + dW;
                region2.x2 = region2.x1 + w;
            }

            SketchletEditor.editorPanel.repaint();
        }
    }

    public void distributeVertically() {
        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 2) {
            int rcount = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size();
            ActiveRegion first = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.firstElement();
            ActiveRegion last = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
            int y1 = first.y1;
            int y2 = last.y2;

            int totalH = 0;
            Vector<ActiveRegion> rs = new Vector<ActiveRegion>();
            for (int i = 0; i < rcount; i++) {
                ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.elementAt(i);
                int h = region.getHeight();
                totalH += h;

                if (region.y1 < y1) {
                    y1 = region.y1;
                }

                if (region.y2 > y2) {
                    y2 = region.y2;
                }

                boolean bInserted = false;
                for (int j = 0; j < rs.size(); j++) {
                    if (rs.elementAt(j).y1 > region.y1) {
                        rs.insertElementAt(region, j);
                        bInserted = true;
                        break;
                    }
                }
                if (!bInserted) {
                    rs.add(region);
                }
            }

            int dH = (Math.abs(y1 - y2) - totalH) / (rcount - 1);

            if (dH <= 0) {
                return;
            }
            for (int i = 1; i < rs.size() - 1; i++) {
                ActiveRegion region1 = rs.elementAt(i - 1);
                ActiveRegion region2 = rs.elementAt(i);
                int h = region2.y2 - region2.y1;
                region2.y1 = region1.y2 + dH;
                region2.y2 = region2.y1 + h;
            }

            SketchletEditor.editorPanel.repaint();
        }
    }

    public void sameSize() {
        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 1) {
            ActiveRegion region1 = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();

            for (int i = 0; i < SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() - 1; i++) {
                ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.elementAt(i);
                region.setSize(region1.getWidth(), region1.getHeight());
            }

            SketchletEditor.editorPanel.repaint();
        }
    }

    public void sameWidth() {
        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 1) {
            ActiveRegion region1 = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();

            for (int i = 0; i < SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() - 1; i++) {
                ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.elementAt(i);
                region.setWidth(region1.getWidth());
            }

            SketchletEditor.editorPanel.repaint();
        }
    }

    public void sameHeight() {
        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 1) {
            ActiveRegion region1 = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();

            for (int i = 0; i < SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() - 1; i++) {
                ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.elementAt(i);
                region.setHeight(region1.getHeight());
            }

            SketchletEditor.editorPanel.repaint();
        }
    }

    public void alignVertical(int align) {
        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 1) {
            int y1 = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.firstElement().y1;
            int y2 = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.firstElement().y2;
            int center = y1 + (y2 - y1) / 2;

            for (int i = 1; i < SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size(); i++) {
                ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.elementAt(i);
                int h = region.y2 - region.y1;
                switch (align) {
                    case 0:
                        region.y1 = y1;
                        region.y2 = region.y1 + h;
                        break;
                    case 1:
                        region.y1 = center - h / 2;
                        region.y2 = region.y1 + h;
                        break;
                    case 2:
                        region.y2 = y2;
                        region.y1 = region.y2 - h;
                        break;
                }
            }
            SketchletEditor.editorPanel.repaint();
        }
    }

    public void mousePressed(MouseEvent e) {
        if (SketchletEditor.editorPanel.mode == EditorMode.SKETCHING) {
            return;
        }
        SketchletEditor.editorPanel.updateTables();

        x = e.getX();
        y = e.getY();

        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
            x1 = a.x1;
            y1 = a.y1;
            x2 = a.x2;
            y2 = a.y2;
            rotation = a.rotation;
        } else {
            a = null;
        }
    }

    public void mouseReleased(MouseEvent e) {
        /*if (g2FreeHandDraw != null) {
        g2FreeHandDraw.dispose();
        g2FreeHandDraw = null;
        }*/

        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            for (ActiveRegion r : SketchletEditor.editorPanel.currentPage.regions.selectedRegions) {
                r.mouseHandler.bRotating = false;
            }
        }
        if (Math.abs(x - e.getX()) < 1 && Math.abs(y - e.getY()) < 1) {
            if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 0 && SketchletEditor.editorPanel.mode == EditorMode.ACTIONS) {
                ActiveRegion a2 = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                if (a2 == a) {
                    a.y1 = y1;
                    a.x2 = x2;
                    a.y2 = y2;
                    a.rotation = rotation;
                }
            }
            showPopup(e);
        } else {
            if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 0) {
                ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                a.limitsHandler.processLimits("speed", 0.0, 0.0, 0.0, true);
            }
        }
    }

    public JPopupMenu getPopupMenu(boolean showAll) {
        ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
        JPopupMenu popupMenu = new JPopupMenu();
        TutorialPanel.prepare(popupMenu, true);

        if (a.inTrajectoryMode || a.inTrajectoryMode2) {
            if (a.trajectoryType == 2) {
                popupMenu.add(endTrajectory);
                return popupMenu;
            }
        }

        if (Profiles.isActive("active_region_popup_appearance")) {
            popupMenu.add((JMenu) ModeToolbar.createAppearanceMenu(false));
        }
        boolean bAddSeparator = false;
        if (Profiles.isActive("active_region_widget")) {
            popupMenu.add(ModeToolbar.getWidgetMenu());
            bAddSeparator = true;
        }
        if (Profiles.isActiveAny("active_region_popup_copy,active_region_popup_paste_image,active_region_popup_paste_text") && popupMenu.getSubElements().length > 0) {
            popupMenu.addSeparator();
            bAddSeparator = false;
        }
        JMenu regionImage = new JMenu("Region Image");
        regionImage.setIcon(Workspace.createImageIcon("resources/image.png"));
        regionImage.add(copyRegionImage);
        regionImage.add(saveRegionImage);
        regionImage.addSeparator();
        boolean bSep = false;
        if (Profiles.isActive("active_region_popup_extract")) {
            regionImage.add(extract);
            bSep = true;
        }
        if (Profiles.isActive("active_region_popup_extract_new")) {
            regionImage.add(extractNewFrame);
            bSep = true;
        }
        if (Profiles.isActive("active_region_popup_stamp")) {
            regionImage.add(stamp);
            bSep = true;
        }
        if (bSep) {
            regionImage.addSeparator();
        }
        regionImage.add(printRegionImage);

        popupMenu.add(regionImage);
        if (popupMenu.getSubElements().length > 0) {
            popupMenu.addSeparator();
        }

        if (Profiles.isActive("active_region_popup_copy")) {
            popupMenu.add(copyRegion);
            bAddSeparator = true;
        }
        if (Profiles.isActive("active_region_popup_paste_image")) {
            popupMenu.add(pasteImage);
            bAddSeparator = true;
        }
        if (Profiles.isActive("active_region_popup_paste_text")) {
            popupMenu.add(pasteText);
            bAddSeparator = true;
        }
        /////////////////////////////////////////////////
        if (Profiles.isActive("active_region_popup_fix")) {
            if (bAddSeparator && popupMenu.getSubElements().length > 0) {
                popupMenu.addSeparator();
            }
            popupMenu.add(fix);
            bAddSeparator = true;
        }
        if (bAddSeparator) {
            popupMenu.addSeparator();
            bAddSeparator = false;
        }
        if (Profiles.isActive("active_region_popup_reset")) {
            reset.removeAll();
            reset.setIcon(Workspace.createImageIcon("resources/zero.png"));
            reset.add(menuUnfixRotation);
            reset.addSeparator();
            reset.add(menuUnfixRotationPoint);
            if (Profiles.isActive("active_region_popup_trajectory")) {
                reset.add(menuUnfixTrajectoryPoint);
            }
            reset.addSeparator();
            reset.add(menuUnfixMotionLimits);
            popupMenu.add(reset);
            bAddSeparator = true;
        }
        if (Profiles.isActive("active_region_popup_wizards")) {
            if (bAddSeparator && popupMenu.getSubElements().length > 0) {
                popupMenu.addSeparator();
            }
            popupMenu.add(wizards);
            bAddSeparator = true;
        }
        if (Profiles.isActive("active_region_popup_define_clip")) {
            if (bAddSeparator && popupMenu.getSubElements().length > 0) {
                popupMenu.addSeparator();
            }
            popupMenu.add(defineClip);
            bAddSeparator = true;
        }
        if (Profiles.isActive("active_region_popup_trajectory")) {
            popupMenu.add(trajectoryMenu);
            if (!a.strTrajectory1.trim().isEmpty()) {
                popupMenu.add(trajectory2Menu);
                editTrajectoryMenu.removeAll();
                editTrajectoryMenu.add(editTrajectory);
                editTrajectoryMenu.addSeparator();
                editTrajectoryMenu.add(clearTrajectory);
                popupMenu.add(editTrajectoryMenu);
                stickToTrajectory.setSelected(a.bStickToTrajectory);
                popupMenu.add(trajectoryAnimate);
                popupMenu.add(stickToTrajectory);
            }
            if (!a.strTrajectory2.trim().isEmpty()) {
                editTrajectoryMenu.add(clearTrajectory2);
            }
        }
        if (showAll) {
            if (popupMenu.getSubElements().length > 0) {
                popupMenu.addSeparator();
            }
            popupMenu.add(animator);
            bAddSeparator = true;
        }
        //////////////////////////////////////////////
        if (Profiles.isActive("active_region_popup_group")) {
            if (bAddSeparator && popupMenu.getSubElements().length > 0) {
                popupMenu.addSeparator();
            }
            popupMenu.add(group);
            bAddSeparator = true;
        }
        if (Profiles.isActive("active_region_popup_align")) {
            if (popupMenu.getSubElements().length > 0) {
                popupMenu.addSeparator();
            }
            popupMenu.add(alignRegions);
        }
        if (Profiles.isActive("active_region_popup_bring_to_front")) {
            if (popupMenu.getSubElements().length > 0) {
                popupMenu.addSeparator();
            }
            popupMenu.add(bringToFront);
        }
        if (Profiles.isActive("active_region_popup_send_to_back")) {
            if (popupMenu.getSubElements().length > 0 && !Profiles.isActive("active_region_popup_bring_to_front")) {
                popupMenu.addSeparator();
            }
            popupMenu.add(sendToBack);
            popupMenu.add(asBackground);
        }
        if (showAll) {
            if (popupMenu.getSubElements().length > 0) {
                popupMenu.addSeparator();
            }
            popupMenu.add(delete);
        }

        if (showAll) {
            if (popupMenu.getSubElements().length > 0) {
                popupMenu.addSeparator();
            }
            popupMenu.add(menuProperties);
        }
        if (popupMenu.getSubElements().length > 0) {
            popupMenu.addSeparator();
        }
        popupMenu.add(delete);
        return popupMenu;
    }

    public void prepareCopyTrajecotryFromRegion() {
        this.trajectoryFromRegion.removeAll();
        this.trajectory2FromRegion.removeAll();
        trajectoryAnimateRegions.removeAll();
        if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            final ActiveRegion a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
            for (int ir = SketchletEditor.editorPanel.currentPage.regions.regions.size() - 1; ir >= 0; ir--) {
                final ActiveRegion reg = SketchletEditor.editorPanel.currentPage.regions.regions.elementAt(ir);
                if (reg != a && !reg.strTrajectory1.isEmpty()) {
                    JMenu rm = new JMenu(reg.getLongName());
                    JMenu rm2 = new JMenu(reg.getLongName());
                    JMenuItem tm = new JMenuItem("Primary Trajectory", Workspace.createImageIcon("resources/trajectory_red.png"));
                    tm.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            a.strTrajectory1 = reg.strTrajectory1;
                        }
                    });
                    JMenuItem tm2 = new JMenuItem(Language.translate("Primary Trajectory"), Workspace.createImageIcon("resources/trajectory_red.png"));
                    tm2.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            a.strTrajectory2 = reg.strTrajectory1;
                        }
                    });
                    rm.add(tm);
                    rm2.add(tm2);
                    JMenu rmConnect = new JMenu(reg.getLongName());
                    JMenuItem tmConnect = new JMenuItem(Language.translate("Connect to Primary Trajectory Point"), Workspace.createImageIcon("resources/trajectory_red.png"));
                    tmConnect.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent event) {
                            ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                            String variable = null;
                            for (int i = 0; i < region.updateTransformations.length; i++) {
                                String dim = region.updateTransformations[i][0].toString();
                                String var = region.updateTransformations[i][1].toString();
                                if (dim.equalsIgnoreCase("trajectory position") && !var.isEmpty()) {
                                    variable = var;
                                    break;
                                }
                            }
                            if (variable == null) {
                                variable = DataServer.variablesServer.getUniqueVariableName("trajectory");
                                for (int i = 0; i < reg.updateTransformations.length; i++) {
                                    String dim = reg.updateTransformations[i][0].toString();
                                    String var = reg.updateTransformations[i][1].toString();
                                    if (dim.isEmpty() && var.isEmpty()) {
                                        reg.updateTransformations[i][0] = "trajectory position";
                                        reg.updateTransformations[i][1] = variable;
                                        break;
                                    }
                                }
                            }
                            region.setProperty("trajectory position", "=" + variable);
                            JOptionPane.showMessageDialog(SketchletEditor.editorFrame,
                                    Language.translate("Connection established:") + "\n" + "   "
                                            + Language.translate("Region") + " " + reg.getLongName() + " "
                                            + Language.translate("dimension") + " 'trajectory position' " + Language.translate("connected to variable")
                                            + " '" + variable + "'\n" + "   " + Language.translate("Region") + " " + region.getLongName()
                                            + " " + Language.translate("property") + " 'trajectory position' " + Language.translate("set to") + " '" + variable + "'\n");
                        }
                    });
                    rmConnect.add(tmConnect);

                    if (!reg.strTrajectory2.isEmpty()) {
                        tm = new JMenuItem(Language.translate("Secondary Trajectory"), Workspace.createImageIcon("resources/trajectory_blue.png"));
                        tm.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent event) {
                                a.strTrajectory1 = reg.strTrajectory2;
                            }
                        });
                        tm2 = new JMenuItem(Language.translate("Secondary Trajectory"), Workspace.createImageIcon("resources/trajectory_blue.png"));
                        tm2.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent event) {
                                a.strTrajectory2 = reg.strTrajectory2;
                            }
                        });
                        rm.add(tm);
                        rm2.add(tm2);

                        tmConnect = new JMenuItem(Language.translate("Connect to Secondary Trajectory"), Workspace.createImageIcon("resources/trajectory_blue.png"));
                        tmConnect.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent event) {
                                ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                                String variable = null;
                                for (int i = 0; i < region.updateTransformations.length; i++) {
                                    String dim = region.updateTransformations[i][0].toString();
                                    String var = region.updateTransformations[i][1].toString();
                                    if (dim.equalsIgnoreCase("trajectory position 2") && !var.isEmpty()) {
                                        variable = var;
                                        break;
                                    }
                                }
                                if (variable == null) {
                                    variable = DataServer.variablesServer.getUniqueVariableName("trajectory");
                                    for (int i = 0; i < reg.updateTransformations.length; i++) {
                                        String dim = reg.updateTransformations[i][0].toString();
                                        String var = reg.updateTransformations[i][1].toString();
                                        if (dim.isEmpty() && var.isEmpty()) {
                                            reg.updateTransformations[i][0] = "trajectory position 2";
                                            reg.updateTransformations[i][1] = variable;
                                            break;
                                        }
                                    }
                                }
                                region.setProperty("trajectory position", "=" + variable);
                                JOptionPane.showMessageDialog(SketchletEditor.editorFrame,
                                        Language.translate("Connection established:") + "\n" + "   "
                                                + Language.translate("Region") + " " + reg.getLongName() + " "
                                                + Language.translate("dimension") + " 'trajectory position2' " + Language.translate("connected to variable")
                                                + " '" + variable + "'\n" + "   " + Language.translate("Region") + " " + region.getLongName()
                                                + " " + Language.translate("property") + " 'trajectory position' " + Language.translate("set to") + " '" + variable + "'\n");
                            }
                        });
                        rmConnect.add(tmConnect);
                    }
                    trajectoryFromRegion.add(rm);
                    trajectory2FromRegion.add(rm2);
                    trajectoryAnimateRegions.add(rmConnect);
                }
            }
        }
        trajectoryAnimateRegions.setEnabled(trajectoryAnimateRegions.getItemCount() > 0);
        trajectoryFromRegion.setEnabled(trajectoryFromRegion.getItemCount() > 0);
        trajectory2FromRegion.setEnabled(trajectory2FromRegion.getItemCount() > 0);
    }

    private void showPopup(MouseEvent e) {
        prepareCopyTrajecotryFromRegion();
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clip.getContents(null);

        boolean imageOnClipboard = transferable.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));

        if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {
            if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 0 && SketchletEditor.editorPanel.mode == EditorMode.ACTIONS) {
                if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 1) {
                    boolean bGroup = false;
                    for (ActiveRegion as : SketchletEditor.editorPanel.currentPage.regions.selectedRegions) {
                        if (as.regionGrouping.equals("") || !as.regionGrouping.equals(SketchletEditor.editorPanel.currentPage.regions.selectedRegions.firstElement().regionGrouping)) {
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
                if (SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null) {
                    ActiveRegion region = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
                    int index = region.parent.regions.indexOf(region);
                    int size = region.parent.regions.size();

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
                getPopupMenu(false).show(e.getComponent(), e.getX(), e.getY());
            } else {
                pasteImageMain.setEnabled(imageOnClipboard);
                pasteRegion.setEnabled(SketchletEditor.editorPanel.copiedActions != null && SketchletEditor.editorPanel.copiedActions.size() > 0);
                popupMenuMain.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
