/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.editor.regions;

import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.animation.AnimationTimer;
import net.sf.sketchlet.designer.data.ActiveRegion;
import net.sf.sketchlet.designer.editor.EditorMode;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.SketchletEditorUtils;
import net.sf.sketchlet.designer.playback.displays.InteractionSpace;
import net.sf.sketchlet.designer.ui.page.SketchStatePanel;
import net.sf.sketchlet.designer.ui.region.ActiveRegionsFrame;
import net.sf.sketchlet.designer.ui.wizard.WizActiveRegionEvent;

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

    JMenuItem delete = new JMenuItem(Language.translate("Remove"), Workspace.createImageIcon("resources/user-trash.png"));
    JMenuItem group = new JMenuItem(Language.translate("Group"), Workspace.createImageIcon("resources/system-users.png"));
    JMenuItem extract;
    JMenuItem extractNewFrame;
    JMenuItem defineClip;
    JMenuItem stamp;
    JMenuItem copyRegion;
    JMenuItem copyRegionImage;
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
    JMenuItem menuSizeReset = new JMenuItem(Language.translate("Reset Limits and Orientation"), Workspace.createImageIcon("resources/no_limits.png"));
    JMenu actions = new JMenu(Language.translate("Edit Page Events"));
    JMenu pasteImageMain = new JMenu(Language.translate("Paste Image"));
    JMenuItem entryActions = new JMenuItem(Language.translate("On Page Entry"), Workspace.createImageIcon("resources/import.gif"));
    JMenuItem exitActions = new JMenuItem(Language.translate("On Page Exit"), Workspace.createImageIcon("resources/export.gif"));
    JMenuItem variablesActions = new JMenuItem(Language.translate("On Variable Updates"), Workspace.createImageIcon("resources/variables.png"));
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
    JMenu trajectory2FromRegion = new JMenu(Language.translate("Copy from Region"));
    JMenuItem trajectory2Gesture = new JMenuItem(Language.translate("With Freehand Gesture"), Workspace.createImageIcon("resources/pencil.gif"));
    JMenuItem trajectory2Line = new JMenuItem(Language.translate("As a Line"), Workspace.createImageIcon("resources/line_2.png"));
    JMenuItem trajectory2MultiLine = new JMenuItem(Language.translate("As a Multiline"), Workspace.createImageIcon("resources/line_multi.png"));
    JMenuItem trajectory2Oval = new JMenuItem(Language.translate("As an Oval"), Workspace.createImageIcon("resources/oval.png"));
    JMenuItem animator = new JMenuItem(Language.translate("Animate..."), Workspace.createImageIcon("resources/timer.png"));
    JMenuItem menuProperties = new JMenuItem(Language.translate("Region Properties"));
    //JMenuItem menuSaveLibrary = new JMenuItem(Language.translate("Save to Active Regions Library..."));
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
                SketchletEditor.editorPanel.extract(0);
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
                SketchletEditor.editorPanel.deleteSelectedRegion();
            }
        });
        editTrajectory.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_T, ActionEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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

        copyRegionImage = new JMenuItem(Language.translate("Copy Region Image"), Workspace.createImageIcon("resources/edit-copy.png"));
        copyRegionImage.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.editorClipboard.copySelectedAction();
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
                SketchletEditor.editorPanel.regionPopupListener.alignVertical(0);
            }
        });
        alignCentered.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.regionPopupListener.alignVertical(1);
            }
        });
        alignBottom.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.regionPopupListener.alignVertical(2);
            }
        });
        alignLeft.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.regionPopupListener.alignHorizontal(0);
            }
        });
        alignCenter.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.regionPopupListener.alignHorizontal(1);
            }
        });
        alignRight.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                SketchletEditor.editorPanel.regionPopupListener.alignHorizontal(2);
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
                    menuFixPosition.setText(Language.translate("Unlock Position"));
                } else {
                    a.setProperty("position x", "");
                    a.setProperty("position y", "");
                    menuFixPosition.setText(Language.translate("Lock Position"));
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
                    menuFixSize.setText("Unlock Size");
                } else {
                    a.setProperty("width", "");
                    a.setProperty("height", "");
                    menuFixSize.setText("Lock Size");
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
        //menuSaveLibrary.setIcon(Workspace.createImageIcon("resources/active_region.png"));

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
        ActiveRegion a = null;
        if (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.currentPage != null && SketchletEditor.editorPanel.currentPage.regions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions.size() > 0) {
            a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
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
        this.add(wizards);
        this.addSeparator();
        this.add(defineClip);
        this.addSeparator();
        this.add(trajectoryMenu);
        if (!a.strTrajectory1.trim().isEmpty()) {
            this.add(trajectory2Menu);
            editTrajectoryMenu.removeAll();
            editTrajectoryMenu.add(editTrajectory);
            editTrajectoryMenu.addSeparator();
            editTrajectoryMenu.add(clearTrajectory);
            this.add(editTrajectoryMenu);
            stickToTrajectory.setSelected(a.bStickToTrajectory);
            this.add(stickToTrajectory);
        }
        if (!a.strTrajectory2.trim().isEmpty()) {
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
        //this.add(menuSaveLibrary);
        //this.addSeparator();
        this.add(delete);
        prepareMenu();
        return;
    }

    private void prepareMenu() {
        ActiveRegion a = null;
        if (SketchletEditor.editorPanel != null && SketchletEditor.editorPanel.currentPage != null && SketchletEditor.editorPanel.currentPage.regions != null && SketchletEditor.editorPanel.currentPage.regions.selectedRegions != null) {
            a = SketchletEditor.editorPanel.currentPage.regions.selectedRegions.lastElement();
        } else {
            return;
        }

        SketchletEditor.editorPanel.regionPopupListener.prepareCopyTrajecotryFromRegion();
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clip.getContents(null);

        boolean imageOnClipboard = transferable.isDataFlavorSupported(new DataFlavor("image/x-java-image; class=java.awt.Image", "Image"));

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
        } else {
            pasteImageMain.setEnabled(imageOnClipboard);
            pasteRegion.setEnabled(SketchletEditor.editorPanel.copiedActions != null && SketchletEditor.editorPanel.copiedActions.size() > 0);
        }
    }
}
