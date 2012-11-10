/*
 * To change this template, choose Tools | Templates
 * and open the template in the editorPanel.
 */
package net.sf.sketchlet.designer.data;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.template.TemplateMarkers;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.context.ActiveRegionContextImpl;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.regions.renderer.ActiveRegionRenderer;
import net.sf.sketchlet.designer.editor.regions.renderer.TextDrawingLayer;
import net.sf.sketchlet.designer.editor.regions.shapes.RegularPolygon;
import net.sf.sketchlet.designer.editor.regions.shapes.StarPolygon;
import net.sf.sketchlet.designer.events.region.ActiveRegionMotionHandler;
import net.sf.sketchlet.designer.events.region.ActiveRegionMouseHandler;
import net.sf.sketchlet.designer.events.region.ActiveRegionOverlapHandler;
import net.sf.sketchlet.designer.help.TutorialPanel;
import net.sf.sketchlet.designer.programming.macros.Macro;
import net.sf.sketchlet.designer.tools.imagecache.ImageCache;
import net.sf.sketchlet.designer.ui.playback.PlaybackFrame;
import net.sf.sketchlet.designer.ui.profiles.Profiles;
import net.sf.sketchlet.designer.ui.toolbars.ColorToolbar;
import net.sf.sketchlet.pluginloader.WidgetPluginFactory;
import net.sf.sketchlet.util.Colors;
import net.sf.sketchlet.util.RefreshTime;
import net.sf.sketchlet.util.XMLUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

public class ActiveRegion implements PropertiesInterface {
    private static Logger log = Logger.getLogger(ActiveRegion.class);


    public List<RegionOverlapEventMacro> regionOverlapEventMacros = new Vector<RegionOverlapEventMacro>();
    public List<WidgetEventMacro> widgetEventMacros = new Vector<WidgetEventMacro>();
    public MouseProcessor mouseProcessor = new MouseProcessor();
    public KeyboardProcessor keyboardProcessor = new KeyboardProcessor();

    public boolean bVisible = true;
    public boolean bPinned = false;
    public boolean bInFocus = false;
    public String strX = "";
    public String strY = "";
    public String strRelX = "";
    public String strRelY = "";
    public String strZoom = "";
    public String strTrajectoryPosition = "";
    public String strWidth = "";
    public String strHeight = "";
    public String strRotate = "";
    public String strShearX = "";
    public String strShearY = "";
    public String strWindowX = "";
    public String strWindowY = "";
    public String strWindowWidth = "";
    public String strWindowHeight = "";
    public String strTransparency = "";
    public String strSpeed = "";
    public String strSpeedDirection = "";
    public String strRotationSpeed = "";
    public String strPen = "";
    public String strX1 = "";
    public String strY1 = "";
    public String strX2 = "";
    public String strY2 = "";
    public String strPerspectiveX1 = "";
    public String strPerspectiveY1 = "";
    public String strPerspectiveX2 = "";
    public String strPerspectiveY2 = "";
    public String strPerspectiveX3 = "";
    public String strPerspectiveY3 = "";
    public String strPerspectiveX4 = "";
    public String strPerspectiveY4 = "";
    public String strAutomaticPerspective = "";
    public String strRotation3DHorizontal = "";
    public String strRotation3DVertical = "";
    public String strPerspectiveDepth = "";
    public int layer = 0;
    public Object[][] updateTransformations = {
            {"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""},
            {"", "", "", "", ""}
    };
    public Object[][] limits = {
            {"position x", "", ""},
            {"position y", "", ""},
            {"rotation", "", ""}
    };

    public String shape = "None";
    public String strShapeArgs = "";
    public String fontName = "";
    public String fontSize = "";
    public String fontColor = "";
    public String fontStyle = "";
    private String strImageFile = "";
    public int x1, y1, x2, y2;
    public double center_rotation_x = 0.5, center_rotation_y = 0.5;
    public double trajectory2_x = 0.25, trajectory2_y = 0.5;
    public double p_x0 = 0.0, p_y0 = 0.0, p_x1 = 1.0, p_y1 = 0.0, p_x2 = 1.0, p_y2 = 1.0, p_x3 = 0.0, p_y3 = 1.0;
    public int playback_x1, playback_y1, playback_x2, playback_y2;
    public double playback_rotation, playback_shearX, playback_shearY;
    public int pen_x, pen_y;
    public double rotation, shearX, shearY;
    public ActiveRegions parent;
    private boolean drawImageChanged = false;
    public Vector<String> additionalImageFile = new Vector<String>();
    public boolean inTrajectoryMode = false;
    public boolean inTrajectoryMode2 = false;
    public int trajectoryType = 0;
    public String strHAlign = "";
    public String strVAlign = "";
    public String strLineColor = "";
    public String strLineThickness = "";
    public String strLineStyle = "";
    public String strFillColor = "";
    public String strCaptureScreenX = "";
    public String strCaptureScreenY = "";
    public String strCaptureScreenWidth = "";
    public String strCaptureScreenHeight = "";
    public String strTextField = "";
    public String strEmbeddedSketch = "";
    public String strEmbeddedSketchVarPrefix = "";
    public String strEmbeddedSketchVarPostfix = "";
    public String strImageUrlField = "";
    public String strActive = "";
    public String strType = "";
    public String strWidget = "";
    public String strWidgetProperties = "";
    public Properties widgetProperties = null;
    public String strImageIndex = "";
    public String strAnimationMs = "";
    public int speed_prevX1;
    public int speed_prevY1;
    public int speed_prevX2;
    public int speed_prevY2;
    public int speed_w;
    public int speed_h;
    public double speed_x;
    public double speed_y;
    public double speed_prevDirection;
    public double speed;
    public long lastFrameTime = 0;
    public String regionGrouping = "";
    public Properties customProperties = new Properties();
    // renderer
    public ActiveRegionRenderer renderer = new ActiveRegionRenderer(this);
    // handlers
    public ActiveRegionMouseHandler mouseHandler = new ActiveRegionMouseHandler(this);
    public ActiveRegionOverlapHandler interactionHandler = new ActiveRegionOverlapHandler(this);
    public ActiveRegionMotionHandler limitsHandler = new ActiveRegionMotionHandler(this);
    // images
    public BufferedImage image = null;
    private BufferedImage drawImage = null;
    public Vector<BufferedImage> additionalDrawImages = new Vector<BufferedImage>();
    public Vector<Boolean> additionalImageChanged = new Vector<Boolean>();
    public String strText = "";
    public String strTrajectory1 = "";
    public String strTrajectory2 = "";
    public String strName = "";
    public boolean bCanMove = true;
    public boolean bCanRotate = true;
    public boolean bCanResize = false;
    public boolean bFitToBox = true;
    public boolean bWrapText = false;
    public boolean bTrimText = false;
    public boolean bWalkThrough = false;
    public boolean bStickToTrajectory = true;
    public boolean bOrientationTrajectory = true;
    public boolean bCaptureScreen = false;
    public boolean bCaptureScreenMouseMap = false;
    public String strWidgetItems = "";
    public String strCharactersPerLine = "";
    public String strMaxNumLines = "";

    public ActiveRegion() {
    }

    public ActiveRegion(ActiveRegions parent) {
        this.parent = parent;
        this.getDrawImagePath();
    }

    public ActiveRegion(ActiveRegion a, boolean bCopyFiles) {
        this(a, a.parent, bCopyFiles);
    }

    public ActiveRegion(ActiveRegions parent, int x1, int y1, int x2, int y2) {
        this(parent);
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public ActiveRegion(ActiveRegion region, ActiveRegions actions, boolean bCopyFiles) {
        setPropertiesFromRegion(region);
        this.parent = actions;

        this.activate(false);
        if (bCopyFiles) {
            additionalImageFile.removeAllElements();
            additionalDrawImages.removeAllElements();
            additionalImageChanged.removeAllElements();
            for (int aai = 0; aai <= region.additionalImageFile.size(); aai++) {
                if (aai > 0) {
                    additionalImageFile.add(null);
                    additionalDrawImages.add(null);
                    additionalImageChanged.add(new Boolean(false));
                } else {
                    strImageFile = "";
                    this.drawImage = null;
                    this.drawImageChanged = true;
                }
                File imgFile;
                if (region.parent.page.strSourceDirectory != null) {
                    imgFile = new File(region.getDrawImageFile(region.parent.page.strSourceDirectory, aai));
                } else {
                    imgFile = new File(region.getDrawImagePath(aai));
                }

                try {
                    String strNewImage = this.getDrawImagePath(aai);
                    if (imgFile.exists()) {
                        File newFile = new File(strNewImage);
                        FileUtils.copyFile(imgFile, newFile);
                    }
                } catch (Throwable e) {
                    log.error("Active region copy error.", e);
                }
            }
        } else {
            this.playback_x1 = region.x1;
            this.playback_y1 = region.y1;
            this.playback_x2 = region.x2;
            this.playback_y2 = region.y2;
            this.playback_rotation = region.rotation;
            this.playback_shearX = region.shearX;
            this.playback_shearY = region.shearY;
            this.mouseHandler.playback_startAngle = region.mouseHandler.startAngle;
            this.mouseHandler.playback_startX = region.mouseHandler.startX;
            this.mouseHandler.playback_startY = region.mouseHandler.startY;
            this.strImageFile = region.strImageFile;
            this.drawImage = region.drawImage;
            this.drawImageChanged = true;
            additionalImageFile.removeAllElements();
            for (String strAdditionalImageFile : region.additionalImageFile) {
                additionalImageFile.add(strAdditionalImageFile);
            }
            additionalDrawImages.removeAllElements();
            for (BufferedImage additionalImage : region.additionalDrawImages) {
                additionalDrawImages.add(additionalImage);
            }
            additionalImageChanged.removeAllElements();
            for (Boolean imageChanged : region.additionalImageChanged) {
                additionalImageChanged.add(imageChanged);
            }

            this.strType = region.strType;
            this.strImageIndex = region.strImageIndex;
            this.strAnimationMs = region.strAnimationMs;
            image = region.image;
        }

        this.deactivate(false);
    }

    public static ActiveRegion getInstanceForBatchProcessing() {
        Page page = new Page("", "");
        page.title = "Page 1";
        page.regions = new ActiveRegions(page);

        ActiveRegion region = new ActiveRegion(page.regions);

        return region;
    }

    public Page getSketch() {
        return this.parent.page;
    }

    public boolean isActive(boolean bPlayback) {
        boolean active = bPlayback ? !this.processText(strActive).equalsIgnoreCase("false") : true;

        active = active && getSketch().isLayerActive(layer, bPlayback);

        return this.bVisible && active;
    }

    public String getType() {
        return this.strType;
    }

    public boolean isMouseActive() {
        return mouseProcessor.getMouseActionsCount() > 0 || bCanMove || bCanRotate || !this.strWidget.isEmpty();
    }

    public void flush() {
        if (image != null) {
            image.flush();
            image = null;
        }
        if (this.drawImage != null) {
            this.drawImage.flush();
            drawImage = null;
        }
        if (this.additionalDrawImages != null) {
            for (int i = 0; i < this.additionalDrawImages.size(); i++) {
                BufferedImage img = this.additionalDrawImages.elementAt(i);
                if (img != null) {
                    img.flush();
                }
                this.additionalDrawImages.setElementAt(null, i);
            }
        }
        if (renderer != null) {
            renderer.flush();
        }
    }

    public static int getLastNonEmptyRow(Object data[][]) {
        for (int i = data.length - 1; i >= 0; i--) {
            for (int j = 0; j < data[i].length; j++) {
                if (!data[i][j].toString().isEmpty()) {
                    return i;
                }
            }
        }

        return -1;
    }

    public static int getLastNonEmptyRow(String data[][]) {
        for (int i = data.length - 1; i >= 0; i--) {
            for (int j = 0; j < data[i].length; j++) {
                if (!data[i][j].isEmpty()) {
                    return i;
                }
            }
        }

        return -1;
    }

    public Font getFont(float defaultFontSize) {
        Font font = null;
        double size = 0;
        if (fontSize.length() != 0) {
            try {
                size = Math.min(Double.parseDouble(processText(fontSize)), 400);
            } catch (Exception e) {
            }
        }
        if (size == 0) {
            size = defaultFontSize;
        }
        String name = processText(fontName);
        String style = processText(fontStyle);

        font = TextDrawingLayer.getFont(name, style, (float) size);

        return font;
    }

    public Color getColor(String strColor) {
        Color color = Colors.getColor(processText(strColor));
        if (color == null) {
            return Color.BLACK;
        }

        return color;
    }

    public Color getFontColor() {
        return getColor(fontColor);
    }

    public Color getBackgroundColor() {
        Color color = Colors.getColor(processText(strFillColor));
        if (color == null) {
            return new Color(0, 0, 0, 0);
        }

        return color;
    }

    public Stroke getStroke() {
        String strLineThickness = this.processText(this.strLineThickness);
        String strLineStyle = this.processText(this.strLineStyle);

        int lineThickness = 2;
        try {
            lineThickness = Integer.parseInt(strLineThickness);
        } catch (Exception e) {
        }

        return ColorToolbar.getStroke(strLineStyle, lineThickness);
    }

    public Color getLineColor() {
        return getColor(strLineColor);
    }

    public int getLineThickness() {
        int lineThickness = 2;
        String strLineThickness = processText(this.strLineThickness);
        try {
            lineThickness = Integer.parseInt(strLineThickness);
        } catch (Exception e) {
        }
        return lineThickness;
    }

    public File getImageFile(int index) {
        if (this.getImageFiles().size() > index) {
            return this.getImageFiles().elementAt(index);
        } else {
            return null;
        }
    }

    public Vector<File> getImageFiles() {
        Vector<File> files = new Vector<File>();
        File file = new File(getDrawImagePath());
        if (file.exists()) {
            files.add(file);
        }
        if (this.additionalImageFile != null) {
            for (int i = 0; i < this.additionalImageFile.size(); i++) {
                file = new File(this.getDrawImagePath(i + 1));
                if (file.exists()) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    public void dispose() {
        closeAllImages();
        if (renderer != null) {
            renderer.dispose();
        }
        if (mouseHandler != null) {
            mouseHandler.dispose();
        }
        if (interactionHandler != null) {
            interactionHandler.dispose();
        }
        if (limitsHandler != null) {
            limitsHandler.dispose();
        }

        mouseProcessor.dispose();
        for (EventMacro eventMacro : regionOverlapEventMacros) {
            eventMacro.dispose();
        }
        this.regionOverlapEventMacros.clear();

        strX = null;
        strY = null;
        strRelX = null;
        strZoom = null;
        strRelY = null;
        strTrajectoryPosition = null;
        strWidth = null;
        strHeight = null;
        strRotate = null;
        strShearX = null;
        strShearY = null;
        strWindowX = null;
        strWindowY = null;
        strWindowWidth = null;
        strWindowHeight = null;
        strTransparency = null;
        strSpeed = null;
        strSpeedDirection = null;
        strRotationSpeed = null;
        strPen = null;
        strX1 = null;
        strY1 = null;
        strX2 = null;
        strY2 = null;
        strPerspectiveX1 = null;
        strPerspectiveY1 = null;
        strPerspectiveX2 = null;
        strPerspectiveY2 = null;
        strPerspectiveX3 = null;
        strPerspectiveY3 = null;
        strPerspectiveX4 = null;
        strPerspectiveY4 = null;
        strAutomaticPerspective = null;
        strRotation3DHorizontal = null;
        strRotation3DVertical = null;
        strPerspectiveDepth = null;
        updateTransformations = null;
        limits = null;
        shape = null;
        fontName = null;
        fontSize = null;
        fontColor = null;
        fontStyle = null;
        strImageFile = null;
        parent = null;
        image = null;
        drawImage = null;
        additionalImageFile = null;
        additionalDrawImages = null;
        additionalImageChanged = null;
        regionGrouping = null;
        renderer = null;
        mouseHandler = null;
        interactionHandler = null;
        limitsHandler = null;
        strHAlign = null;
        strVAlign = null;
        strLineColor = null;
        strLineThickness = null;
        strLineStyle = null;
        strFillColor = null;
        strCaptureScreenX = null;
        strCaptureScreenY = null;
        strCaptureScreenWidth = null;
        strCaptureScreenHeight = null;
        strTextField = null;
        strEmbeddedSketch = null;
        strEmbeddedSketchVarPrefix = null;
        strEmbeddedSketchVarPostfix = null;
        strImageUrlField = null;
        strImageIndex = null;
        strAnimationMs = null;
    }

    public void closeAllImages() {
        if (this.image != null) {
            this.image.flush();
            this.image = null;
        }
        if (this.drawImage != null) {
            this.drawImage.flush();
            this.drawImage = null;
        }
        if (this.additionalDrawImages != null) {
            for (BufferedImage img : this.additionalDrawImages) {
                if (img != null) {
                    img.flush();
                }
            }
            this.additionalDrawImages.removeAllElements();
            this.additionalDrawImages = null;
        }
    }

    public void setName(String name) {
        this.strName = name;
    }

    public String getName() {
        if (strName.isEmpty()) {
            return getNumber();
        } else {
            return strName;
        }
    }

    public String getLongName() {
        if (strName.isEmpty()) {
            return getNumber();
        } else {
            return getNumber() + " (" + strName + ")";
        }
    }

    public boolean isSelected() {
        return parent.selectedRegions != null && parent.selectedRegions.contains(this);
    }

    public void setPropertiesFromRegion(ActiveRegion a) {
        this.x1 = a.x1;
        this.y1 = a.y1;
        this.x2 = a.x2;
        this.y2 = a.y2;
        this.center_rotation_x = a.center_rotation_x;
        this.center_rotation_y = a.center_rotation_y;
        this.trajectory2_x = a.trajectory2_x;
        this.trajectory2_y = a.trajectory2_y;
        this.p_x0 = a.p_x0;
        this.p_y0 = a.p_y0;
        this.p_x1 = a.p_x1;
        this.p_y1 = a.p_y1;
        this.p_x2 = a.p_x2;
        this.p_y2 = a.p_y2;
        this.p_x3 = a.p_x3;
        this.p_y3 = a.p_y3;
        this.rotation = a.rotation;
        this.shearX = a.shearX;
        this.shearY = a.shearY;
        this.strName = a.strName;

        this.shape = a.shape;
        this.strShapeArgs = a.strShapeArgs;
        this.regionGrouping = a.regionGrouping;

        this.strX = a.strX;
        this.strY = a.strY;
        this.strX1 = a.strX1;
        this.strY1 = a.strY1;
        this.strX2 = a.strX2;
        this.strY2 = a.strY2;
        this.strPerspectiveX1 = a.strPerspectiveX1;
        this.strPerspectiveY1 = a.strPerspectiveY1;
        this.strPerspectiveX2 = a.strPerspectiveX2;
        this.strPerspectiveY2 = a.strPerspectiveY2;
        this.strPerspectiveX3 = a.strPerspectiveX3;
        this.strPerspectiveY3 = a.strPerspectiveY3;
        this.strPerspectiveX4 = a.strPerspectiveX4;
        this.strPerspectiveY4 = a.strPerspectiveY4;
        this.strAutomaticPerspective = a.strAutomaticPerspective;
        this.strPerspectiveDepth = a.strPerspectiveDepth;
        this.strRotation3DHorizontal = a.strRotation3DHorizontal;
        this.strRotation3DVertical = a.strRotation3DVertical;
        this.strRelX = a.strRelX;
        this.strRelY = a.strRelY;
        this.strZoom = a.strZoom;
        this.strTrajectoryPosition = a.strTrajectoryPosition;
        this.strWidth = a.strWidth;
        this.strHeight = a.strHeight;
        this.strRotate = a.strRotate;
        this.strShearX = a.strShearX;
        this.strShearY = a.strShearY;
        this.strWindowX = a.strWindowX;
        this.strWindowY = a.strWindowY;
        this.strWindowWidth = a.strWindowWidth;
        this.strWindowHeight = a.strWindowHeight;
        this.strTransparency = a.strTransparency;
        this.strSpeed = a.strSpeed;
        this.strSpeedDirection = a.strSpeedDirection;
        this.strRotationSpeed = a.strRotationSpeed;
        this.strPen = a.strPen;

        for (int i = 0; i < a.updateTransformations.length; i++) {
            for (int j = 0; j < a.updateTransformations[i].length; j++) {
                this.updateTransformations[i][j] = (String) a.updateTransformations[i][j];
            }
        }

        for (int i = 0; i < a.limits.length; i++) {
            for (int j = 0; j < a.limits[i].length; j++) {
                this.limits[i][j] = (String) a.limits[i][j];
            }
        }

        for (int i = 0; i < a.propertiesAnimation.length; i++) {
            for (int j = 0; j < a.propertiesAnimation[i].length; j++) {
                if (this.propertiesAnimation[i][1] != null) {
                    this.propertiesAnimation[i][j] = (String) a.propertiesAnimation[i][j];
                }
            }
        }

        this.bStickToTrajectory = a.bStickToTrajectory;
        this.bOrientationTrajectory = a.bOrientationTrajectory;
        this.strTrajectory1 = a.strTrajectory1;
        this.strTrajectory2 = a.strTrajectory2;

        this.strWidgetItems = a.strWidgetItems;

        this.bCanMove = a.bCanMove;
        this.bCanResize = a.bCanResize;
        this.bCanRotate = a.bCanRotate;
        this.bFitToBox = a.bFitToBox;

        this.strTextField = a.strTextField;
        this.strText = a.strText;

        this.bWrapText = a.bWrapText;
        this.bTrimText = a.bTrimText;
        this.strCharactersPerLine = a.strCharactersPerLine;
        this.strMaxNumLines = a.strMaxNumLines;

        this.bCaptureScreen = a.bCaptureScreen;
        this.bCaptureScreenMouseMap = a.bCaptureScreenMouseMap;

        this.bWalkThrough = a.bWalkThrough;

        this.strImageUrlField = a.strImageUrlField;
        this.strImageIndex = a.strImageIndex;
        this.strActive = a.strActive;
        this.strType = a.strType;
        this.strWidget = a.strWidget;
        this.widgetProperties = null;
        this.strWidgetProperties = a.strWidgetProperties;
        this.strAnimationMs = a.strAnimationMs;

        this.strEmbeddedSketch = a.strEmbeddedSketch;
        this.strEmbeddedSketchVarPrefix = a.strEmbeddedSketchVarPrefix;
        this.strEmbeddedSketchVarPostfix = a.strEmbeddedSketchVarPostfix;

        this.strHAlign = a.strHAlign;
        this.strVAlign = a.strVAlign;

        this.fontName = a.fontName;
        this.fontSize = a.fontSize;
        this.fontStyle = a.fontStyle;
        this.fontColor = a.fontColor;

        this.strLineColor = a.strLineColor;
        this.strLineStyle = a.strLineStyle;
        this.strLineThickness = a.strLineThickness;
        this.strFillColor = a.strFillColor;

        this.strCaptureScreenX = a.strCaptureScreenX;
        this.strCaptureScreenY = a.strCaptureScreenY;
        this.strCaptureScreenWidth = a.strCaptureScreenWidth;
        this.strCaptureScreenHeight = a.strCaptureScreenHeight;

    }

    public String getVarPrefix() {
        if (this.parent != null) {
            return this.parent.page.strVarPrefix;
        } else {
            return "";
        }
    }

    public String getVarPostfix() {
        if (this.parent != null) {
            return this.parent.page.strVarPostfix;
        } else {
            return "";
        }
    }

    public int getX1() {
        return (parent != null ? parent.offset_x : 0) + x1;
    }

    public int getY1() {
        return (parent != null ? parent.offset_y : 0) + y1;
    }

    public int getX2() {
        return (parent != null ? parent.offset_x : 0) + x2;
    }

    public int getY2() {
        return (parent != null ? parent.offset_y : 0) + y2;
    }

    public void play() {
        playback_x1 = x1;
        playback_y1 = y1;
        playback_x2 = x2;
        playback_y2 = y2;
        playback_rotation = rotation;
        playback_shearX = shearX;
        playback_shearY = shearY;
    }

    public int getWidth() {
        return Math.abs(x2 - x1);
    }

    public int getHeight() {
        return Math.abs(y2 - y1);
    }

    public int getPlaybackWidth() {
        return Math.abs(playback_x2 - playback_x1);
    }

    public int getPlaybackHeight() {
        return Math.abs(y2 - y1);
    }

    public void startDefiningTrajectory(int type) {
        this.strTrajectory1 = "";
        this.strTrajectory2 = "";
        this.inTrajectoryMode = true;
        this.inTrajectoryMode2 = false;
        this.trajectoryType = type;
    }

    public void startDefiningTrajectory2(int type) {
        this.strTrajectory2 = "";
        this.inTrajectoryMode = false;
        this.inTrajectoryMode2 = true;
        this.trajectoryType = type;
    }

    public void enableAnimation(boolean bEnabled) {
    }

    public boolean bActive = false;
    public boolean bAdjusting = false;

    public void deactivate(boolean bPlayback) {
        bActive = false;
        this.enableAnimation(false);

        if (this.renderer != null) {
            this.renderer.deactivate(bPlayback);
            this.renderer.dispose();
            this.renderer = new ActiveRegionRenderer(this);
        }

        interactionHandler.reset();
    }

    public void activate(boolean bPlayback) {
        bActive = true;
        if (this.renderer != null) {
            this.renderer.activate(bPlayback);
        }
        if (!bPlayback) {
        } else {
            this.initPlayback();
        }

        interactionHandler.reset();
        this.enableAnimation(false);
    }

    private String getDrawImagePath() {
        if (strImageFile == null || strImageFile.isEmpty()) {
            strImageFile = "region_" + System.currentTimeMillis() + ".png";
        }
        return SketchletContextUtils.getCurrentProjectSkecthletsDir() + strImageFile;
    }

    public String getDrawImageFileName() {
        if (strImageFile == null || strImageFile.trim().equals("")) {
            strImageFile = "region_" + System.currentTimeMillis() + ".png";
        }
        return strImageFile;
    }

    public String getDrawImagePath(int index) {
        if (index == 0) {
            return getDrawImagePath();
        } else {
            try {
                String strAdditionalImage = this.additionalImageFile.elementAt(index - 1);

                if (strAdditionalImage == null || strAdditionalImage.isEmpty()) {
                    do {
                        strAdditionalImage = "region_" + System.currentTimeMillis() + ".png";
                    } while (strImageFile.equalsIgnoreCase(strAdditionalImage) || additionalImageFile.contains(strAdditionalImage));
                    this.additionalImageFile.set(index - 1, strAdditionalImage);
                }
                return SketchletContextUtils.getCurrentProjectSkecthletsDir() + strAdditionalImage;
            } catch (Throwable e) {
                log.error("getDrawImagePath()", e);
            }
        }
        return "";
    }

    public String getId() {
        return getDrawImageFileName(0);
    }

    public String getDrawImageFileName(int index) {
        if (index == 0) {
            return getDrawImageFileName();
        } else {
            try {
                String strAdditionalImage = this.additionalImageFile.elementAt(index - 1);

                if (strAdditionalImage == null || strAdditionalImage.trim().equals("")) {
                    strAdditionalImage = "region_" + System.currentTimeMillis() + ".png";
                    this.additionalImageFile.set(index - 1, strAdditionalImage);
                }
                return strAdditionalImage;
            } catch (Throwable e) {
                log.error("getDrawImageFileName()", e);
            }
        }
        return "";
    }

    public void deleteDrawImage(int index) {
        if (additionalDrawImages.size() == 0) {
            return;
        }

        try {
            new File(getDrawImagePath(index)).delete();
            if (index == 0) {
                this.drawImage = this.additionalDrawImages.elementAt(0);
                this.drawImageChanged = true;

                this.additionalDrawImages.removeElementAt(0);
                this.additionalImageChanged.removeElementAt(0);
                this.additionalImageFile.removeElementAt(0);
            } else {
                this.additionalDrawImages.removeElementAt(index - 1);
                this.additionalImageChanged.removeElementAt(index - 1);
                this.additionalImageFile.removeElementAt(index - 1);
            }
        } catch (Throwable e) {
            log.error("deleteDrawImage()", e);
        }
    }

    public String getNumber() {
        return "" + (parent.regions.size() - parent.regions.indexOf(this));
    }

    private String getDrawImageFile(String strDir) {
        if (strImageFile == null || strImageFile.trim().equals("")) {
            strImageFile = "region_" + System.currentTimeMillis() + ".png";
        }
        return strDir + strImageFile;
    }

    public int getImageCount() {
        return this.additionalDrawImages.size() + 1;
    }

    public BufferedImage getDrawImage(int index) {
        if (index == 0) {
            return this.drawImage;
        } else {
            if (index < 0 || index > additionalDrawImages.size()) {
                return null;
            } else {
                return this.additionalDrawImages.elementAt(index - 1);
            }
        }
    }

    public void setDrawImage(int index, BufferedImage image) {
        if (index == 0) {
            if (this.drawImage != null) {
                this.drawImage.flush();
            }
            this.drawImage = image;
        } else {
            if (index < 0 || index > additionalDrawImages.size()) {
                return;
            } else {
                if (this.additionalDrawImages.get(index - 1) != null) {
                    this.additionalDrawImages.get(index - 1).flush();
                }
                this.additionalDrawImages.set(index - 1, image);
            }
        }
        File file = this.getImageFile(index);
        if (file != null) {
            if (ImageCache.images != null && ImageCache.images.get(file) != null && image != null) {
                ImageCache.images.get(file).flush();
                ImageCache.images.put(file, image);
            }
        }
    }

    public void setDrawImageChanged(int index, boolean bChanged) {
        if (index == 0) {
            this.drawImageChanged = bChanged;
        } else {
            if (index < 0 || index > additionalDrawImages.size()) {
                return;
            } else {
                this.additionalImageChanged.set(index - 1, new Boolean(bChanged));
            }
        }
    }

    public boolean isDrawImageChanged(int index) {
        if (index == 0) {
            return this.drawImageChanged;
        } else {
            if (index < 0 || index > additionalDrawImages.size()) {
                return false;
            } else {
                return this.additionalImageChanged.get(index - 1).booleanValue();
            }
        }
    }

    public String getDrawImageFile(String strDir, int index) {
        if (index == 0) {
            return getDrawImageFile(strDir);
        } else {
            if (index < 0 || index > additionalDrawImages.size()) {
                return null;
            } else {
                try {
                    String strAdditionalImage = this.additionalImageFile.elementAt(index - 1);

                    if (strAdditionalImage == null || strAdditionalImage.trim().equals("")) {
                        strAdditionalImage = "region_" + System.currentTimeMillis() + ".png";
                    }
                    return strDir + strAdditionalImage;
                } catch (Throwable e) {
                    log.error("getDrawImageFile()", e);
                }
            }
        }
        return "";
    }

    private void setImageFile(String strFile) {
        this.strImageFile = strFile;
    }

    public void setImageFile(String strFile, int index) {
        if (index == 0) {
            setImageFile(strFile);
        } else if (index > 0 && this.additionalImageFile.size() > index - 1) {
            this.additionalImageFile.set(index - 1, strFile);
        }
    }

    public void animate() {
    }

    public String strPrevImage = "";

    public String processText(String strText) {
        strText = Evaluator.processRegionReferences(this, strText);
        return Evaluator.processText(strText, this.getVarPrefix(), this.getVarPostfix());
    }

    public boolean isAffected(String strVariable) {
        return true;
    }

    public void saveImage() {
        try {
            if (this.drawImage != null && drawImageChanged) {
                ImageCache.write(this.drawImage, new File(this.getDrawImagePath()));
                drawImageChanged = false;
            }
            for (int i = 0; i < this.additionalDrawImages.size(); i++) {
                int index = i + 1;
                if (this.getDrawImage(index) != null && this.isDrawImageChanged(index)) {
                    ImageCache.write(this.getDrawImage(index), new File(this.getDrawImagePath(index)));
                    this.setDrawImageChanged(index, false);
                }
            }

        } catch (Throwable e) {
        }
    }

    public void saveElementIfNotEmpty(PrintWriter out, String strElement, String strValue, String strPrefix) {
    }

    public void save(PrintWriter out) {
        save(out, false);
    }

    public String saveString() {
        StringWriter str = new StringWriter();
        save(new PrintWriter(str), true);

        return str.toString();
    }

    public void save(PrintWriter out, boolean annonimous) {
        saveImage();
        String strShowText = XMLUtils.prepareForXML(this.strTextField);
        String strTextArea = this.strText;
        String strTrajectory = XMLUtils.prepareForXML(this.strTrajectory1);
        String strTrajectory2 = XMLUtils.prepareForXML(this.strTrajectory2);

        String strControlItems = this.strWidgetItems;

        String strEmbedSketch = XMLUtils.prepareForXML(this.strEmbeddedSketch);
        String strEmbedPrefix = XMLUtils.prepareForXML(this.strEmbeddedSketchVarPrefix);
        String strEmbedPostfix = XMLUtils.prepareForXML(this.strEmbeddedSketchVarPostfix);
        String strCaptureScreenX = XMLUtils.prepareForXML(this.strCaptureScreenX);
        String strCaptureScreenY = XMLUtils.prepareForXML(this.strCaptureScreenY);
        String strCaptureScreenWidth = XMLUtils.prepareForXML(this.strCaptureScreenWidth);
        String strCaptureScreenHeight = XMLUtils.prepareForXML(this.strCaptureScreenHeight);

        String strImage = this.strImageUrlField;
        strImage = XMLUtils.prepareForXML(strImage);
        String strIndex = this.strImageIndex;
        String strActive = XMLUtils.prepareForXML(this.strActive);
        String strType = XMLUtils.prepareForXML(this.strType);
        String strControl = XMLUtils.prepareForXML(this.strWidget);
        String strControlProperties = XMLUtils.prepareForXML(this.strWidgetProperties);
        String animationMs = this.strAnimationMs;
        strIndex = XMLUtils.prepareForXML(strIndex);
        animationMs = XMLUtils.prepareForXML(animationMs);


        String strCanMove = this.bCanMove + "";
        String strCanRotate = this.bCanRotate + "";
        String strCanResize = this.bCanResize + "";

        long lastModified = 0;
        try {
            lastModified = new File(getDrawImagePath()).lastModified();
        } catch (Throwable e) {
        }

        out.println("<action>");
        out.println("    <region name='" + strName + "' x1='" + x1 + "' y1='" + y1 + "' x2='" + x2 + "' y2='" + y2 + "' shearX='" + shearX + "' shearY='" + shearY + "' rotation='" + rotation + "'/>");
        out.println("    <perspective p_x0='" + p_x0 + "' p_y0='" + p_y0 + "' p_x1='" + p_x1 + "' p_y1='" + p_y1 + "' p_x2='" + p_x2 + "' p_y2='" + p_y2 + "' p_x3='" + p_x3 + "' p_y3='" + p_y3 + "'/>");
        out.println("    <rotation_center x='" + center_rotation_x + "' y='" + center_rotation_y + "'/>");
        out.println("    <trajectory_point x='" + trajectory2_x + "' y='" + trajectory2_y + "'/>");
        out.println("    <layer>" + this.layer + "</layer>");
        out.println("    <visible>" + this.bVisible + "</visible>");
        if (!this.shape.isEmpty()) {
            out.println("    <basic-shape>" + XMLUtils.prepareForXML(shape) + "</basic-shape>");
        }
        if (!this.strShapeArgs.isEmpty()) {
            out.println("    <basic-shape-args>" + XMLUtils.prepareForXML(strShapeArgs) + "</basic-shape-args>");
        }
        if (!this.regionGrouping.isEmpty()) {
            out.println("    <group>" + this.regionGrouping + "</group>");
        }
        if (!strShowText.isEmpty()) {
            out.println("    <show-text>" + strShowText + "</show-text>");
        }
        if (!strEmbedSketch.isEmpty()) {
            out.println("    <embedded-sketch>" + strEmbedSketch + "</embedded-sketch>");
        }
        if (!strEmbedPrefix.isEmpty()) {
            out.println("    <embedded-sketch-var-prefix>" + strEmbedPrefix + "</embedded-sketch-var-prefix>");
        }
        if (!strEmbedPostfix.isEmpty()) {
            out.println("    <embedded-sketch-var-postfix>" + strEmbedPostfix + "</embedded-sketch-var-postfix>");
        }
        out.println("    <capture-screen>" + this.bCaptureScreen + "</capture-screen>");
        out.println("    <capture-screen-mouse-map>" + this.bCaptureScreenMouseMap + "</capture-screen-mouse-map>");
        if (!strCaptureScreenX.isEmpty()) {
            out.println("    <capture-screen-x>" + strCaptureScreenX + "</capture-screen-x>");
        }
        if (!strCaptureScreenY.isEmpty()) {
            out.println("    <capture-screen-y>" + strCaptureScreenY + "</capture-screen-y>");
        }
        if (!strCaptureScreenWidth.isEmpty()) {
            out.println("    <capture-screen-width>" + strCaptureScreenWidth + "</capture-screen-width>");
        }
        if (!strCaptureScreenHeight.isEmpty()) {
            out.println("    <capture-screen-height>" + strCaptureScreenHeight + "</capture-screen-height>");
        }

        out.println("    <wrap-text>" + this.bWrapText + "</wrap-text>");
        out.println("    <trim-text>" + this.bTrimText + "</trim-text>");
        out.println("    <is-solid>" + this.bWalkThrough + "</is-solid>");
        if (!this.strCharactersPerLine.isEmpty()) {
            out.println("    <characters-per-line>" + XMLUtils.prepareForXML(this.strCharactersPerLine) + "</characters-per-line>");
        }
        if (!this.strMaxNumLines.isEmpty()) {
            out.println("    <max-lines>" + this.strMaxNumLines + "</max-lines>");
        }
        if (!strTextArea.isEmpty()) {
            out.println("    <text-area><![CDATA[" + strTextArea + "]]></text-area>");
        }
        if (!strTrajectory.isEmpty()) {
            out.println("    <trajectory>" + strTrajectory + "</trajectory>");
        }
        if (!strControlItems.isEmpty()) {
            out.println("    <control-items><![CDATA[" + strControlItems + "]]></control-items>");
        }

        if (!strTrajectory2.isEmpty()) {
            out.println("    <trajectory2>" + strTrajectory2 + "</trajectory2>");
        }
        out.println("    <stick-to-trajectory>" + this.bStickToTrajectory + "</stick-to-trajectory>");
        out.println("    <orientation-trajectory>" + this.bOrientationTrajectory + "</orientation-trajectory>");
        if (!strImage.isEmpty()) {
            out.println("    <show-image>" + strImage + "</show-image>");
        }
        if (!strIndex.isEmpty()) {
            out.println("    <image-index>" + strIndex + "</image-index>");
        }
        if (!strActive.isEmpty()) {
            out.println("    <is-active>" + strActive + "</is-active>");
        }
        if (!strType.isEmpty()) {
            out.println("    <type>" + strType + "</type>");
        }
        if (!strControl.isEmpty()) {
            out.println("    <control>" + strControl + "</control>");
        }
        if (!strControlProperties.isEmpty()) {
            out.println("    <control-parameters>" + strControlProperties + "</control-parameters>");
        }
        if (!strAnimationMs.isEmpty()) {
            out.println("    <image-animation-ms>" + strAnimationMs + "</image-animation-ms>");
        }
        if (!annonimous) {
            if (!strImageFile.isEmpty()) {
                out.println("    <image-draw>" + strImageFile + "</image-draw>");
            }
            for (String strAdditionalImage : this.additionalImageFile) {
                out.println("    <additional-image-draw>" + strAdditionalImage + "</additional-image-draw>");
            }

            out.println("    <image-draw-timestamp>" + lastModified + "</image-draw-timestamp>");
        }
        out.println("    <alignment>");
        out.println("       <fit-to-box>" + this.bFitToBox + "</fit-to-box>");
        if (!strHAlign.isEmpty()) {
            out.println("       <horizontal-alignment>" + this.strHAlign + "</horizontal-alignment>");
        }
        if (!strVAlign.isEmpty()) {
            out.println("       <vertical-alignment>" + this.strVAlign + "</vertical-alignment>");
        }
        out.println("    </alignment>");
        out.println("    <transform>");
        if (!strX.isEmpty()) {
            out.println("        <x>" + XMLUtils.prepareForXML(strX) + "</x>");
        }
        if (!strY.isEmpty()) {
            out.println("        <y>" + XMLUtils.prepareForXML(strY) + "</y>");
        }
        if (!strX1.isEmpty()) {
            out.println("        <direct-x1>" + XMLUtils.prepareForXML(strX1) + "</direct-x1>");
        }
        if (!strY1.isEmpty()) {
            out.println("        <direct-y1>" + XMLUtils.prepareForXML(strY1) + "</direct-y1>");
        }
        if (!strX2.isEmpty()) {
            out.println("        <direct-x2>" + XMLUtils.prepareForXML(strX2) + "</direct-x2>");
        }
        if (!strY2.isEmpty()) {
            out.println("        <direct-y2>" + XMLUtils.prepareForXML(strY2) + "</direct-y2>");
        }
        if (!strPerspectiveX1.isEmpty()) {
            out.println("        <perspective-x1>" + XMLUtils.prepareForXML(strPerspectiveX1) + "</perspective-x1>");
        }
        if (!strPerspectiveY1.isEmpty()) {
            out.println("        <perspective-y1>" + XMLUtils.prepareForXML(strPerspectiveY1) + "</perspective-y1>");
        }
        if (!strPerspectiveX2.isEmpty()) {
            out.println("        <perspective-x2>" + XMLUtils.prepareForXML(strPerspectiveX2) + "</perspective-x2>");
        }
        if (!strPerspectiveY2.isEmpty()) {
            out.println("        <perspective-y2>" + XMLUtils.prepareForXML(strPerspectiveY2) + "</perspective-y2>");
        }
        if (!strPerspectiveX3.isEmpty()) {
            out.println("        <perspective-x3>" + XMLUtils.prepareForXML(strPerspectiveX3) + "</perspective-x3>");
        }
        if (!strPerspectiveY3.isEmpty()) {
            out.println("        <perspective-y3>" + XMLUtils.prepareForXML(strPerspectiveY3) + "</perspective-y3>");
        }
        if (!strPerspectiveX4.isEmpty()) {
            out.println("        <perspective-x4>" + XMLUtils.prepareForXML(strPerspectiveX4) + "</perspective-x4>");
        }
        if (!strPerspectiveY4.isEmpty()) {
            out.println("        <perspective-y4>" + XMLUtils.prepareForXML(strPerspectiveY4) + "</perspective-y4>");
        }
        if (!strAutomaticPerspective.isEmpty()) {
            out.println("        <automatic-perspective>" + XMLUtils.prepareForXML(strAutomaticPerspective) + "</automatic-perspective>");
        }
        if (!strPerspectiveDepth.isEmpty()) {
            out.println("        <perspective-depth>" + XMLUtils.prepareForXML(strPerspectiveDepth) + "</perspective-depth>");
        }
        if (!strRotation3DHorizontal.isEmpty()) {
            out.println("        <rotate-3d-horizontal>" + XMLUtils.prepareForXML(strRotation3DHorizontal) + "</rotate-3d-horizontal>");
        }
        if (!strRotation3DVertical.isEmpty()) {
            out.println("        <rotate-3d-vertical>" + XMLUtils.prepareForXML(strRotation3DVertical) + "</rotate-3d-vertical>");
        }
        if (!strWidth.isEmpty()) {
            out.println("        <width>" + XMLUtils.prepareForXML(strWidth) + "</width>");
        }
        if (!strHeight.isEmpty()) {
            out.println("        <height>" + XMLUtils.prepareForXML(strHeight) + "</height>");
        }
        if (!strRotate.isEmpty()) {
            out.println("        <rotation>" + XMLUtils.prepareForXML(strRotate) + "</rotation>");
        }
        if (!strShearX.isEmpty()) {
            out.println("        <shearX>" + XMLUtils.prepareForXML(strShearX) + "</shearX>");
        }
        if (!strShearY.isEmpty()) {
            out.println("        <shearY>" + XMLUtils.prepareForXML(strShearY) + "</shearY>");
        }
        if (!strWindowX.isEmpty()) {
            out.println("        <windowX>" + XMLUtils.prepareForXML(strWindowX) + "</windowX>");
        }
        if (!strWindowY.isEmpty()) {
            out.println("        <windowY>" + XMLUtils.prepareForXML(strWindowY) + "</windowY>");
        }
        if (!strWindowWidth.isEmpty()) {
            out.println("        <windowWidth>" + XMLUtils.prepareForXML(strWindowWidth) + "</windowWidth>");
        }
        if (!strWindowHeight.isEmpty()) {
            out.println("        <windowHeight>" + XMLUtils.prepareForXML(strWindowHeight) + "</windowHeight>");
        }
        if (!strTransparency.isEmpty()) {
            out.println("        <transparency>" + XMLUtils.prepareForXML(strTransparency) + "</transparency>");
        }
        if (!strSpeed.isEmpty()) {
            out.println("        <speed>" + XMLUtils.prepareForXML(strSpeed) + "</speed>");
        }
        if (!strSpeedDirection.isEmpty()) {
            out.println("        <motionDirection>" + XMLUtils.prepareForXML(strSpeedDirection) + "</motionDirection>");
        }
        if (!strRotationSpeed.isEmpty()) {
            out.println("        <rotationSpeed>" + XMLUtils.prepareForXML(strRotationSpeed) + "</rotationSpeed>");
        }
        if (!strPen.isEmpty()) {
            out.println("        <pen>" + XMLUtils.prepareForXML(strPen) + "</pen>");
        }
        if (!strTrajectoryPosition.isEmpty()) {
            out.println("        <trajectory-position>" + XMLUtils.prepareForXML(strTrajectoryPosition) + "</trajectory-position>");
        }
        if (!strRelX.isEmpty()) {
            out.println("        <relative-x>" + XMLUtils.prepareForXML(strRelX) + "</relative-x>");
        }
        if (!strZoom.isEmpty()) {
            out.println("        <region-zoom>" + XMLUtils.prepareForXML(strZoom) + "</region-zoom>");
        }
        if (!strRelY.isEmpty()) {
            out.println("        <relative-y>" + XMLUtils.prepareForXML(strRelY) + "</relative-y>");
        }
        out.println("    </transform>");
        out.println("    <animate>");
        for (int i = 0; i < this.propertiesAnimation.length; i++) {
            if (propertiesAnimation[i][1] == null) {
                continue;
            }
            boolean bAdd = false;
            for (int j = 1; j < propertiesAnimation[i].length; j++) {
                if (!propertiesAnimation[i][j].isEmpty()) {
                    bAdd = true;
                    break;
                }
            }
            if (!bAdd) {
                continue;
            }
            out.println("        <animate-property name='" + propertiesAnimation[i][0] + "' type='" + propertiesAnimation[i][1] + "' start = '" + propertiesAnimation[i][2] + "' end = '" + propertiesAnimation[i][3] + "' duration='" + propertiesAnimation[i][4] + "' curve='" + propertiesAnimation[i][5] + "'/>");
        }
        out.println("    </animate>");
        out.println("    <font>");
        if (!fontName.isEmpty()) {
            out.println("       <font-name>" + XMLUtils.prepareForXML(this.fontName) + "</font-name>");
        }
        if (!fontSize.isEmpty()) {
            out.println("       <font-size>" + XMLUtils.prepareForXML(this.fontSize) + "</font-size>");
        }
        if (!fontStyle.isEmpty()) {
            out.println("       <font-style>" + XMLUtils.prepareForXML(this.fontStyle) + "</font-style>");
        }
        if (!fontColor.isEmpty()) {
            out.println("       <font-color>" + XMLUtils.prepareForXML(this.fontColor) + "</font-color>");
        }
        out.println("    </font>");
        out.println("    <appearance>");
        if (!strLineColor.isEmpty()) {
            out.println("       <line-color>" + XMLUtils.prepareForXML(this.strLineColor) + "</line-color>");
        }
        if (!strLineStyle.isEmpty()) {
            out.println("       <line-style>" + XMLUtils.prepareForXML(this.strLineStyle) + "</line-style>");
        }
        if (!strLineThickness.isEmpty()) {
            out.println("       <line-thickness>" + XMLUtils.prepareForXML(this.strLineThickness) + "</line-thickness>");
        }
        if (!strFillColor.isEmpty()) {
            out.println("       <fill-color>" + XMLUtils.prepareForXML(this.strFillColor) + "</fill-color>");
        }
        out.println("    </appearance>");
        out.println("    <movable>");

        for (int i = 0; i < limits.length; i++) {
            out.println("        <limit-motion " + "dimension='" + limits[i][0] + "' " + "min='" + limits[i][1] + "' " + "max='" + limits[i][2] + "' " + "/>");
        }

        out.println("        <move>" + strCanMove + "</move>");
        out.println("        <rotate>" + strCanRotate + "</rotate>");
        out.println("        <resize>" + strCanResize + "</resize>");
        out.println("        <update>");
        for (int i = 0; i < this.updateTransformations.length; i++) {
            boolean bSave = false;
            for (int j = 1; j < updateTransformations[i].length; j++) {
                if (!updateTransformations[i][j].equals("")) {
                    bSave = true;
                    break;
                }
            }
            if (!bSave) {
                continue;
            }

            out.print("        <dimension ");
            out.print(" name='" + updateTransformations[i][0] + "'");
            out.print(" variable='" + XMLUtils.prepareForXML((String) updateTransformations[i][1]) + "'");
            out.print(" start='" + updateTransformations[i][2] + "'");
            out.print(" end='" + updateTransformations[i][3] + "'");
            out.print(" format='" + updateTransformations[i][4] + "'");
            out.println("/>");
        }

        out.println("        </update>");
        out.println("    </movable>");

        out.println("    <region-widget-event-actions>");
        for (WidgetEventMacro widgetEventMacro : widgetEventMacros) {
            Macro macro = widgetEventMacro.getMacro();
            macro.name = widgetEventMacro.getEventName();
            macro.saveSimple(out, "widget-event-action", "        ");
        }
        out.println("    </region-widget-event-actions>");
        out.println("    <region-mouse-event-actions>");
        for (EventMacro mouseEventMacro : mouseProcessor.mouseEventMacros) {
            mouseEventMacro.getMacro().name = mouseEventMacro.getEventName();
            mouseEventMacro.getMacro().saveSimple(out, "mouse-event-action", "        ");
        }
        out.println("    </region-mouse-event-actions>");
        out.println("    <region-overlap-event-actions>");
        for (EventMacro regionOverlapEventMacro : regionOverlapEventMacros) {
            regionOverlapEventMacro.getMacro().name = regionOverlapEventMacro.getEventName();
            regionOverlapEventMacro.getMacro().saveSimple(out, "region-overlap-event-action", "        ");
        }
        out.println("    </region-overlap-event-actions>");
        out.println("    <region-keyboard-event-actions>");
        for (EventMacro regionKeyboardEventMacro : keyboardProcessor.keyboardEventMacros) {
            regionKeyboardEventMacro.getMacro().name = regionKeyboardEventMacro.getEventName();
            regionKeyboardEventMacro.getMacro().saveSimple(out, "region-keyboard-event-action", "        ");
        }
        out.println("    </region-keyboard-event-actions>");

        out.println("</action>");

    }

    public void initImages() {
        if (!getDrawImageFileName(0).equals("") && getDrawImage(0) == null) {
            initImage(0);
        }
        for (int i = 0; i < additionalDrawImages.size(); i++) {
            String strAdditionalImage = additionalImageFile.elementAt(i);
            BufferedImage additionalImage = additionalDrawImages.elementAt(i);

            if (!strAdditionalImage.equals("") && additionalImage == null) {
                initImage(i + 1);
            }
        }
    }

    private void initImage() {
        initImage(false);
    }

    private void initImage(boolean bForceFileRead) {
        try {
            File file = new File(getDrawImagePath());
            drawImage = ImageCache.read(file, drawImage, bForceFileRead);
        } catch (Exception e) {
            clearImage(0);
        }
    }

    public void initImage(int index) {
        initImage(index, false);
    }

    public void initImage(int index, boolean bForceFileRead) {
        if (index == 0) {
            initImage(bForceFileRead);
            return;
        } else {
            try {
                File file = new File(getDrawImagePath(index));
                BufferedImage image = ImageCache.read(file, this.additionalDrawImages.elementAt(index - 1), bForceFileRead);
                this.additionalDrawImages.set(index - 1, image);
            } catch (Exception e) {
                clearImage(index);
            }
        }
    }

    public void clearImage(int index) {
        int w = 1;
        int h = 1;
        BufferedImage img = null;
        try {
            img = this.getDrawImage(index);
            if (img != null) {
                w = img.getWidth();
                h = img.getHeight();
            }
        } catch (Exception e) {
        }

        img = (BufferedImage) Workspace.createCompatibleImage(w, h, img);

        this.setDrawImage(index, img);
        this.setDrawImageChanged(index, true);
    }

    public void clearImage11(int index) {
        BufferedImage img = this.getDrawImage(index);
        img = (BufferedImage) Workspace.createCompatibleImage(1, 1, img);

        try {
            File file = index == 0 ? new File(getDrawImagePath()) : new File(getDrawImagePath(index));
            ImageCache.write(img, file);
        } catch (Exception e) {
            log.error("clearImage11()", e);
        }

        this.setDrawImage(index, img);
        this.setDrawImageChanged(index, true);
    }

    public Shape getShape(boolean bPlayback) {
        if (bPlayback) {
            return new Rectangle(playback_x1, playback_y1, getPlaybackWidth(), getPlaybackHeight());
        } else {
            return new Rectangle(x1, y1, getWidth(), getHeight());
        }
    }

    public Rectangle getBounds(boolean bPlayback) {
        AffineTransform aft = new AffineTransform();
        if (bPlayback) {
            aft.shear(playback_shearX, playback_shearY);
            aft.rotate(playback_rotation, playback_x1 + (playback_x2 - playback_x1) * center_rotation_x, playback_y1 + (playback_y2 - playback_y1) * center_rotation_y);
        } else {
            aft.shear(shearX, shearY);
            aft.rotate(rotation, x1 + (x2 - x1) * center_rotation_x, y1 + (y2 - y1) * center_rotation_y);
        }
        Area area = getArea(bPlayback);
        area.transform(aft);
        return area.getBounds();
    }

    public Point getInversePoint(boolean bPlayback, int x, int y) {
        AffineTransform aft = new AffineTransform();
        if (bPlayback) {
            aft.shear(playback_shearX, playback_shearY);
            aft.rotate(playback_rotation, playback_x1 + (playback_x2 - playback_x1) * center_rotation_x, playback_y1 + (playback_y2 - playback_y1) * center_rotation_y);
        } else {
            aft.shear(shearX, shearY);
            aft.rotate(rotation, x1 + (x2 - x1) * center_rotation_x, y1 + (y2 - y1) * center_rotation_y);
        }

        Point ptSrc = new Point(x, y);
        Point ptDest = new Point(x, y);

        try {
            aft.inverseTransform(ptSrc, ptDest);
        } catch (Throwable e) {
        }
        return ptDest;
    }

    public Area getTransformedArea(boolean bPlayback) {
        AffineTransform aft = new AffineTransform();
        if (bPlayback) {
            aft.shear(playback_shearX, playback_shearY);
            aft.rotate(playback_rotation, playback_x1 + (playback_x2 - playback_x1) * center_rotation_x, playback_y1 + (playback_y2 - playback_y1) * center_rotation_y);
        } else {
            aft.shear(shearX, shearY);
            aft.rotate(rotation, x1 + (x2 - x1) * center_rotation_x, y1 + (y2 - y1) * center_rotation_y);
        }
        Area area = getArea(bPlayback);
        area.transform(aft);
        return area;
    }

    public Area getArea(boolean bPlayback) {
        String strShape = this.processText(this.shape);
        if (bPlayback) {
            if (strShape.equalsIgnoreCase("Oval")) {
                return new Area(new Ellipse2D.Double(playback_x1, playback_y1, getPlaybackWidth(), getPlaybackHeight()));
            } else if (strShape.equalsIgnoreCase("Rounded Rectangle")) {
                try {
                    double n = Double.parseDouble(this.processText(strShapeArgs));
                    return new Area(new RoundRectangle2D.Double(playback_x1, playback_y1, getPlaybackWidth(), getPlaybackHeight(), n, n));
                } catch (Exception e) {
                }
            } else if (strShape.equalsIgnoreCase("Triangle 1")) {
                Polygon p = new Polygon();
                p.addPoint(playback_x1 + (playback_x2 - playback_x1) / 2, playback_y1);
                p.addPoint(playback_x1, playback_y2);
                p.addPoint(playback_x2, playback_y2);
                Area area = new Area(p);
                return area;
            } else if (strShape.equalsIgnoreCase("Triangle 2")) {
                Polygon p = new Polygon();
                p.addPoint(playback_x1, playback_y1);
                p.addPoint(playback_x1, playback_y2);
                p.addPoint(playback_x2, playback_y2);
                Area area = new Area(p);
                return area;
            } else if (strShape.toLowerCase().startsWith("regularpolygon")) {
                try {
                    String args[] = strShape.split(" ");
                    if (args.length > 1) {
                        int n = (int) Double.parseDouble(args[1]);
                        int r = Math.min(this.getPlaybackWidth(), this.getPlaybackHeight()) / 2;
                        Area area = new Area(scalePolygon(bPlayback, new RegularPolygon(this.getCenterX(bPlayback), this.getCenterY(bPlayback), r, n, 0)));

                        return area;
                    }
                } catch (Exception e) {
                }
            } else if (strShape.toLowerCase().startsWith("starpolygon")) {
                try {
                    String args[] = strShape.split(" ");
                    if (args.length > 1) {
                        String strArgs = this.processText(strShapeArgs);
                        double internalR = 0.5;
                        if (!strArgs.isEmpty()) {
                            try {
                                internalR = Double.parseDouble(strArgs);
                            } catch (Exception e2) {
                            }
                        }

                        if (internalR < 0) {
                            internalR = 0;
                        } else if (internalR > 1.0) {
                            internalR = 1.0;
                        }

                        int n = (int) Double.parseDouble(args[1]);
                        int r = Math.min(this.getPlaybackWidth(), this.getPlaybackWidth()) / 2;
                        Area area = new Area(scalePolygon(bPlayback, new StarPolygon(this.getCenterX(bPlayback), this.getCenterY(bPlayback), r, (int) (r * internalR), n)));
                        return area;
                    }
                } catch (Exception e) {
                }
            } else if (strShape.toLowerCase().startsWith("pie slice")) {
                try {
                    String strArgs = this.processText(strShapeArgs);
                    double startAngle = 0;
                    double extent = 45;
                    double internalR = 0.0;
                    if (!strArgs.isEmpty()) {
                        try {
                            String args2[] = strArgs.split(",");
                            if (args2.length > 0) {
                                startAngle = Double.parseDouble(args2[0]);
                            }
                            if (args2.length > 1) {
                                extent = Double.parseDouble(args2[1]);
                                while (extent < -360) {
                                    extent += 360;
                                }
                                while (extent > 360) {
                                    extent -= 360;
                                }
                            }
                            if (args2.length > 2) {
                                internalR = Double.parseDouble(args2[2]);
                            }
                        } catch (Exception e2) {
                            // e2.printStackTrace();
                        }
                    }

                    if (internalR < 0) {
                        internalR = 0;
                    } else if (internalR > 1.0) {
                        internalR = 1.0;
                    }
                    int r = Math.min(this.getPlaybackWidth(), this.getPlaybackWidth()) / 2;
                    Area area = new Area(new Arc2D.Double(playback_x1, playback_y1, getPlaybackWidth(), getPlaybackHeight(), -startAngle + 90, -extent, Arc2D.PIE));

                    if (internalR > 0 && internalR < 1) {
                        double _w = getPlaybackWidth() * internalR;
                        double _h = getPlaybackHeight() * internalR;
                        double _x1 = playback_x1 + (getPlaybackWidth() - _w) / 2;
                        double _y1 = playback_y1 + (getPlaybackHeight() - _h) / 2;
                        area.subtract(new Area(new Ellipse2D.Double((int) _x1, (int) _y1, (int) _w, (int) _h)));
                    }
                    return area;
                } catch (Exception e) {
                }
            }
            return new Area(new Rectangle(playback_x1, playback_y1, getPlaybackWidth(), getPlaybackHeight()));
        } else {
            if (strShape.equalsIgnoreCase("Oval")) {
                return new Area(new Ellipse2D.Double(x1, y1, getWidth(), getHeight()));
            } else if (strShape.equalsIgnoreCase("Rounded Rectangle")) {
                try {
                    double n = Double.parseDouble(this.processText(strShapeArgs));
                    return new Area(new RoundRectangle2D.Double(x1, y1, getWidth(), getHeight(), n, n));
                } catch (Exception e) {
                }
            } else if (strShape.equalsIgnoreCase("Triangle 1")) {
                Polygon p = new Polygon();
                p.addPoint(x1 + (x2 - x1) / 2, y1);
                p.addPoint(x1, y2);
                p.addPoint(x2, y2);
                Area area = new Area(p);
                return area;
            } else if (strShape.equalsIgnoreCase("Triangle 2")) {
                Polygon p = new Polygon();
                p.addPoint(x1, y1);
                p.addPoint(x1, y2);
                p.addPoint(x2, y2);
                Area area = new Area(p);
                return area;
            } else if (strShape.toLowerCase().startsWith("regularpolygon")) {
                try {
                    String args[] = strShape.split(" ");
                    if (args.length > 1) {
                        int n = (int) Double.parseDouble(args[1]);
                        int r = Math.min(this.getWidth(), this.getHeight()) / 2;
                        Area area = new Area(scalePolygon(bPlayback, new RegularPolygon(this.getCenterX(bPlayback), this.getCenterY(bPlayback), r, n)));
                        return area;
                    }
                } catch (Exception e) {
                }
            } else if (strShape.toLowerCase().startsWith("starpolygon")) {
                try {
                    String args[] = strShape.split(" ");
                    if (args.length > 1) {
                        String strArgs = this.processText(strShapeArgs);
                        double internalR = 0.5;
                        if (!strArgs.isEmpty()) {
                            try {
                                internalR = Double.parseDouble(strArgs);
                            } catch (Exception e2) {
                            }
                        }

                        if (internalR < 0) {
                            internalR = 0;
                        } else if (internalR > 1.0) {
                            internalR = 1.0;
                        }
                        int n = (int) Double.parseDouble(args[1]);
                        int r = Math.min(this.getWidth(), this.getHeight()) / 2;
                        Area area = new Area(scalePolygon(bPlayback, new StarPolygon(this.getCenterX(bPlayback), this.getCenterY(bPlayback), r, (int) (r * internalR), n)));
                        return area;
                    }
                } catch (Exception e) {
                }
            } else if (strShape.toLowerCase().startsWith("pie slice")) {
                try {
                    String strArgs = this.processText(strShapeArgs);
                    double startAngle = 0;
                    double extent = 45;
                    double internalR = 0.0;
                    if (!strArgs.isEmpty()) {
                        try {
                            String args2[] = strArgs.split(",");
                            if (args2.length > 0) {
                                startAngle = Double.parseDouble(args2[0]);
                            }
                            if (args2.length > 1) {
                                extent = Double.parseDouble(args2[1]);
                                while (extent < -360) {
                                    extent += 360;
                                }
                                while (extent > 360) {
                                    extent -= 360;
                                }
                            }
                            if (args2.length > 2) {
                                internalR = Double.parseDouble(args2[2]);
                            }
                        } catch (Exception e2) {
                            // e2.printStackTrace();
                        }
                    }

                    if (internalR < 0) {
                        internalR = 0;
                    } else if (internalR > 1.0) {
                        internalR = 1.0;
                    }

                    int r = Math.min(this.getWidth(), this.getWidth()) / 2;
                    Area area = new Area(new Arc2D.Double(getX1(), getY1(), getWidth(), getHeight(), -startAngle + 90, -extent, Arc2D.PIE));

                    if (internalR > 0 && internalR < 1) {
                        double _w = getWidth() * internalR;
                        double _h = getHeight() * internalR;
                        double _x1 = getX1() + (getWidth() - _w) / 2;
                        double _y1 = getY1() + (getHeight() - _h) / 2;
                        area.subtract(new Area(new Ellipse2D.Double((int) _x1, (int) _y1, (int) _w, (int) _h)));
                    }
                    return area;
                } catch (Throwable e) {
                }
            }
            return new Area(new Rectangle(x1, y1, getWidth(), getHeight()));
        }

    }

    public Polygon scalePolygon(boolean bPlayback, Polygon p) {
        int w = bPlayback ? this.getPlaybackWidth() : this.getWidth();
        int h = bPlayback ? this.getPlaybackHeight() : this.getHeight();
        int x = bPlayback ? this.playback_x1 : this.x1;
        int y = bPlayback ? this.playback_y1 : this.y1;

        Rectangle r = p.getBounds();

        double scaleX = w / r.getWidth();
        double scaleY = h / r.getHeight();

        Polygon p2 = new Polygon();

        for (int i = 0; i < p.npoints; i++) {
            double px = p.xpoints[i];
            double py = p.ypoints[i];

            px = x + (px - r.getX()) * scaleX;
            py = y + (py - r.getY()) * scaleY;

            p2.addPoint((int) px, (int) py);
        }

        return p2;
    }

    public Dimension getSize() {
        return new Dimension(getWidth(), getHeight());
    }

    public int getX(boolean bPlayback) {
        int x;
        if (!bPlayback) {
            if (this.strHAlign.equalsIgnoreCase("center")) {
                x = x1 + (x2 - x1) / 2;
            } else if (this.strHAlign.equalsIgnoreCase("right")) {
                x = x2;
            } else {
                x = x1;
            }
        } else {
            if (this.strHAlign.equalsIgnoreCase("center")) {
                x = playback_x1 + (playback_x2 - playback_x1) / 2;
            } else if (this.strHAlign.equalsIgnoreCase("right")) {
                x = playback_x2;
            } else {
                x = playback_x1;
            }
        }
        return x;
    }

    public int getCenterX(boolean bPlayback) {
        int x;
        if (!bPlayback) {
            x = x1 + (x2 - x1) / 2;
        } else {
            x = playback_x1 + (playback_x2 - playback_x1) / 2;
        }
        return x;
    }

    public void setX(int x, boolean bPlayback) {
        if (!bPlayback) {
            int w = x2 - x1;
            int cx = x1 + w / 2;
            if (this.strHAlign.equalsIgnoreCase("center")) {
                x1 = x - w / 2;
                x2 = x + w / 2;
            } else if (this.strHAlign.equalsIgnoreCase("right")) {
                x1 = x - w;
                x2 = x;
            } else {
                x1 = x;
                x2 = x + w;
            }
        } else {
            int w = playback_x2 - playback_x1;
            int cx = playback_x1 + w / 2;
            if (this.strHAlign.equalsIgnoreCase("center")) {
                playback_x1 = x - w / 2;
                playback_x2 = x + w / 2;
            } else if (this.strHAlign.equalsIgnoreCase("right")) {
                playback_x1 = x - w;
                playback_x2 = x;
            } else {
                playback_x1 = x;
                playback_x2 = x + w;
            }
        }
    }

    public int getY(boolean bPlayback) {
        int y;
        if (!bPlayback) {
            if (this.strVAlign.equalsIgnoreCase("center")) {
                y = y1 + (y2 - y1) / 2;
            } else if (this.strVAlign.equalsIgnoreCase("bottom")) {
                y = y2;
            } else {
                y = y1;
            }
        } else {
            if (this.strVAlign.equalsIgnoreCase("center")) {
                y = playback_y1 + (playback_y2 - playback_y1) / 2;
            } else if (this.strVAlign.equalsIgnoreCase("bottom")) {
                y = playback_y2;
            } else {
                y = playback_y1;
            }
        }
        return y;
    }

    public int getCenterY(boolean bPlayback) {
        int y;
        if (!bPlayback) {
            y = y1 + (y2 - y1) / 2;
        } else {
            y = playback_y1 + (playback_y2 - playback_y1) / 2;
        }
        return y;
    }

    public void setY(int y, boolean bPlayback) {
        if (!bPlayback) {
            int h = y2 - y1;
            int cx = y1 + h / 2;
            if (this.strVAlign.equalsIgnoreCase("center")) {
                y1 = y - h / 2;
                y2 = y + h / 2;
            } else if (this.strVAlign.equalsIgnoreCase("bottom")) {
                y1 = y - h;
                y2 = y;
            } else {
                y1 = y;
                y2 = y + h;
            }
        } else {
            int h = playback_y2 - playback_y1;
            if (this.strVAlign.equalsIgnoreCase("center")) {
                playback_y1 = y - h / 2;
                playback_y2 = y + h / 2;
            } else if (this.strVAlign.equalsIgnoreCase("bottom")) {
                playback_y1 = y - h;
                playback_y2 = y;
            } else {
                playback_y1 = y;
                playback_y2 = y + h;
            }
        }
    }

    public void processLimitsTrajectory(Point p) {
        renderer.trajectoryDrawingLayer.getClosestTrajectoryPoint(p);
        limitsHandler.processLimits("trajectory position", renderer.trajectoryDrawingLayer.trajectoryPositionFromPoint, 0.0, 1.0, 0.0, 0.0, true);
        limitsHandler.processLimits("trajectory position 2", renderer.trajectoryDrawingLayer.trajectoryPositionFromPoint2, 0.0, 1.0, 0.0, 0.0, true);
    }

    public void processLimitsX() {
        int w = this.x2 - this.x1;
        if (this.strHAlign.equalsIgnoreCase("center")) {
            this.limitsHandler.processLimits("position x", this.x1 + w / 2, w / 2, w / 2, true);
        } else if (this.strHAlign.equalsIgnoreCase("right")) {
            this.limitsHandler.processLimits("position x", this.x1 + w, w, 0, true);
        } else {
            this.limitsHandler.processLimits("position x", this.x1, 0, w, true);
        }
    }

    public void processLimitsY() {
        int h = this.y2 - this.y1;
        if (this.strVAlign.equalsIgnoreCase("center")) {
            this.limitsHandler.processLimits("position y", this.y1 + h / 2, h / 2, h / 2, true);
        } else if (this.strVAlign.equalsIgnoreCase("right")) {
            this.limitsHandler.processLimits("position y", this.y1 + h, h, 0, true);
        } else {
            this.limitsHandler.processLimits("position y", this.y1, 0, h, true);
        }
    }

    public boolean isWithinLimits(boolean bPlayback) {
        Rectangle bounds = getBounds(bPlayback);
        int bx1 = (int) bounds.getX();
        int by1 = (int) bounds.getY();
        int w = (int) bounds.getWidth();
        int h = (int) bounds.getHeight();
        int bx2 = (int) bounds.getX() + w;
        int by2 = (int) bounds.getY() + h;

        int _bx1 = (int) limitsHandler.processLimits("position x", bx1, 0, 0, false);
        int _by1 = (int) limitsHandler.processLimits("position y", by1, 0, 0, false);
        int _bx2 = (int) limitsHandler.processLimits("position x", bx2, 0, 0, false);
        int _by2 = (int) limitsHandler.processLimits("position y", by2, 0, 0, false);

        if (bx1 != _bx1 || by1 != _by1 || bx2 != _bx2 || by2 != _by2) {
            return false;
        }

        return true;
    }

    int original_x1;
    int original_x2;
    int original_y1;
    int original_y2;
    double original_rotation;
    double original_shearX;
    double original_shearY;
    double original_startAngle;
    int original_startX;
    int original_startY;
    boolean bSet = false;

    public boolean isInRegion(int x, int y) {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2;
    }

    public Rectangle getPropertiesIconRectangle() {
        double s = Math.min(1, SketchletEditor.editorPanel.scale);
        return new Rectangle((int) (x2 - 30 / s), (int) (y2 - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getMappingIconRectangle() {
        double s = Math.min(1, SketchletEditor.editorPanel.scale);
        return new Rectangle((int) (x2 - 60 / s), (int) (y2 - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getMouseIconRectangle() {
        double s = Math.min(1, SketchletEditor.editorPanel.scale);
        return new Rectangle((int) (x2 - 90 / s), (int) (y2 - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getKeyboardIconRectangle() {
        double s = Math.min(1, SketchletEditor.editorPanel.scale);
        return new Rectangle((int) (x2 - 90 / s), (int) (y2 - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getInRegionsIconRectangle() {
        double s = Math.min(1, SketchletEditor.editorPanel.scale);
        return new Rectangle((int) (x2 - 120 / s), (int) (y2 - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public boolean isInRegionsPropertiesArea(int x, int y) {
        return getPropertiesIconRectangle().contains(x, y);
    }

    public boolean isInMappingIconArea(int x, int y) {
        return Profiles.isActive("active_region_move") && getMappingIconRectangle().contains(x, y);
    }

    public boolean isInMouseIconArea(int x, int y) {
        return Profiles.isActive("active_region_mouse") && getMouseIconRectangle().contains(x, y);
    }

    public boolean isInKeyboardIconArea(int x, int y) {
        return Profiles.isActive("active_region_keyboard") && getKeyboardIconRectangle().contains(x, y);
    }

    public boolean isInRegionsIconArea(int x, int y) {
        return Profiles.isActive("active_region_overlap") && getInRegionsIconRectangle().contains(x, y);
    }

    public Vector<TrajectoryPoint> createTrajectoryVector() {
        return createTrajectoryVector(strTrajectory1);
    }

    public Vector<TrajectoryPoint> createTrajectory2Vector() {
        return createTrajectoryVector(strTrajectory2);
    }

    public Vector<TrajectoryPoint> createTrajectoryVector(String strTrajectory) {
        String strTemp = strTrajectory;
        strTrajectory = processText(strTrajectory);
        String points[] = strTrajectory.split("\n");
        Vector<TrajectoryPoint> tps = new Vector<TrajectoryPoint>();
        for (int i = 0; i < points.length; i++) {
            String strPoint = points[i].trim();
            strPoint.replace("\t", " ");

            String coord[] = strPoint.split(" ");
            if (coord.length == 3) {
                try {
                    int x = Integer.parseInt(coord[0]);
                    int y = Integer.parseInt(coord[1]);
                    int t = Integer.parseInt(coord[2]);

                    tps.add(new TrajectoryPoint(x, y, t));
                } catch (Throwable e) {
                    log.error("Trajectory conversion error B.");
                }
            }
        }

        return tps;
    }

    public void setSize(int w, int h) {
        setWidth(w);
        setHeight(h);
    }

    public void setWidth(int w) {
        int oldW = x2 - x1;

        if (strHAlign.equalsIgnoreCase("center")) {
            x1 = x1 + oldW / 2 - w / 2;
            x2 = x1 + w;
        } else if (strHAlign.equalsIgnoreCase("right")) {
            x1 = x1 + oldW - w;
            x2 = x1 + w;
        } else {
            x2 = x1 + w;
        }
    }

    public void setHeight(int h) {
        int oldH = y2 - y1;

        if (strVAlign.equalsIgnoreCase("center")) {
            y1 = y1 + oldH / 2 - h / 2;
            y2 = y1 + h;
        } else if (strVAlign.equalsIgnoreCase("right")) {
            y1 = y1 + oldH - h;
            y2 = y1 + h;
        } else {
            y2 = y1 + h;
        }
    }

    public void setFromPlayback() {
        if (parent == null) {
            return;
        }
        bSet = true;
        original_x1 = x1;
        original_y1 = y1;
        original_x2 = x2;
        original_y2 = y2;
        original_rotation = rotation;
        original_shearX = shearX;
        original_shearY = shearY;
        original_startAngle = mouseHandler.startAngle;
        original_startX = mouseHandler.startX;
        original_startY = mouseHandler.startY;
        x1 = playback_x1;
        y1 = playback_y1;
        x2 = playback_x2;
        y2 = playback_y2;
        rotation = playback_rotation;
        shearX = playback_shearX;
        shearY = playback_shearY;
        mouseHandler.startAngle = mouseHandler.playback_startAngle;
        mouseHandler.startX = mouseHandler.playback_startX;
        mouseHandler.startY = mouseHandler.playback_startY;
    }

    public void initPlayback() {
        playback_x1 = x1;
        playback_y1 = y1;
        playback_x2 = x2;
        playback_y2 = y2;
        playback_rotation = rotation;
        playback_shearX = shearX;
        playback_shearY = shearY;
        pen_x = 0;
        pen_y = 0;
    }

    public void resetFromPlayback() {
        bSet = false;
        playback_x1 = x1;
        playback_y1 = y1;
        playback_x2 = x2;
        playback_y2 = y2;
        playback_rotation = rotation;
        playback_shearX = shearX;
        playback_shearY = shearY;
        mouseHandler.playback_startAngle = mouseHandler.startAngle;
        mouseHandler.playback_startX = mouseHandler.startX;
        mouseHandler.playback_startY = mouseHandler.startY;

        x1 = original_x1;
        y1 = original_y1;
        x2 = original_x2;
        y2 = original_y2;
        rotation = original_rotation;
        shearX = original_shearX;
        shearY = original_shearY;
        mouseHandler.startAngle = original_startAngle;
        mouseHandler.startX = original_startX;
        mouseHandler.startY = original_startY;
    }

    public static String[][] showProperties = {
            {"Position", null},
            {"position x", Language.translate("horizontal position (left, 0 to 1000)")},
            {"position y", Language.translate("vertical position (top, 0 to 1000)")},
            {"relative x", Language.translate("relative horizontal position (0.0 to 1.0)")},
            {"relative y", Language.translate("vertical position (0.0 to 1.0)")},
            {"trajectory position", "0.0 to 1.0"},
            {"Size", null},
            {"width", Language.translate("region width")},
            {"height", Language.translate("region height")},
            {"Orientation", null},
            {"rotation", Language.translate("angle")},
            {"Zoom", null},
            {"zoom", Language.translate("zoom factor")},
            {"Transparency", null},
            {"transparency", Language.translate("0.0 to 1.0")},
            {"Visible area", null},
            {"visible area x", Language.translate("")},
            {"visible area y", Language.translate("")},
            {"visible area width", Language.translate("")},
            {"visible area height", Language.translate("")},
            {"Motion", null},
            {"speed", Language.translate("pixels per second")},
            {"direction", Language.translate("angle")},
            {"Pen", null},
            {"pen thickness", Language.translate("0, 1, 2...")},
            {"Advanced / Coordinates", null},
            {"x1", Language.translate("left border position")},
            {"y1", Language.translate("top border position")},
            {"x2", Language.translate("right border position")},
            {"y2", Language.translate("bottom border position")},
            {"Advanced / Sheer", null},
            {"shear x", Language.translate("0.0 to 1.0")},
            {"shear y", Language.translate("0.0 to 1.0")},
            {"Advanced / 3D", null},
            {"horizontal 3d rotation", Language.translate("0 to 360")},
            {"vertical 3d rotation", Language.translate("0 to 360")},
            {"Advanced / Perspective", null},
            {"perspective x1", Language.translate("0 to 1, x top left corner")},
            {"perspective y1", Language.translate("0 to 1, y top left corner")},
            {"perspective x2", Language.translate("0 to 1, x top right corner")},
            {"perspective y2", Language.translate("0 to 1, x top right corner")},
            {"perspective x3", Language.translate("0 to 1, x bottom right corner")},
            {"perspective y3", Language.translate("0 to 1, x bottom right corner")},
            {"perspective x4", Language.translate("0 to 1, x bottom left corner")},
            {"perspective y4", Language.translate("0 to 1, x bottom left corner")},
            {"automatic perspective", Language.translate("left, right, top, bottom, parallel")},
            {"perspective depth", Language.translate("relative perceptive depth 0.0 to 1.0")},};
    static public String[][] propertiesInfo = {
            {"image url", Language.translate("The URL to the image drawn in the active region")},
            {"image frame", Language.translate("Index of the current frame to be shown in the active region")},
            {"position x", Language.translate("horizontal position (left, 0 to ") + Toolkit.getDefaultToolkit().getScreenSize().width + ")"},
            {"position y", Language.translate("vertical position (top, 0 to ") + Toolkit.getDefaultToolkit().getScreenSize().height + ")"},
            {"x1", Language.translate("left (0 to ") + Toolkit.getDefaultToolkit().getScreenSize().width + ")"},
            {"y1", Language.translate("top (0 to ") + Toolkit.getDefaultToolkit().getScreenSize().height + ")"},
            {"x2", Language.translate("right (0 to ") + Toolkit.getDefaultToolkit().getScreenSize().width + ")"},
            {"y2", Language.translate("bottom (0 to ") + Toolkit.getDefaultToolkit().getScreenSize().height + ")"},
            {"relative x", Language.translate("relative horizontal position (0.0 to 1.0)")},
            {"relative y", Language.translate("vertical position (0.0 to 1.0)")},
            {"trajectory position", Language.translate("0.0 to 1.0")},
            {"width", Language.translate("region width")},
            {"height", Language.translate("region height")},
            {"zoom", Language.translate("zoom factor")},
            {"rotation", Language.translate("angle")},
            {"shear x", Language.translate("0.0 to 1.0")},
            {"shear y", Language.translate("0.0 to 1.0")},
            {"visible area x", Language.translate("")},
            {"visible area y", Language.translate("")},
            {"visible area width", Language.translate("")},
            {"visible area height", Language.translate("")},
            {"transparency", Language.translate("0.0 to 1.0")},
            {"speed", Language.translate("pixels per second")},
            {"direction", Language.translate("angle")},
            {"pen thickness", Language.translate("0, 1, 2...")},
            {"perspective x1", Language.translate("0 to 1, x top left corner")},
            {"perspective y1", Language.translate("0 to 1, y top left corner")},
            {"perspective x2", Language.translate("0 to 1, x top right corner")},
            {"perspective y2", Language.translate("0 to 1, x top right corner")},
            {"perspective x3", Language.translate("0 to 1, x bottom right corner")},
            {"perspective y3", Language.translate("0 to 1, x bottom right corner")},
            {"perspective x4", Language.translate("0 to 1, x bottom left corner")},
            {"perspective y4", Language.translate("0 to 1, x bottom left corner")},
            {"automatic perspective", Language.translate("")},
            {"perspective depth", Language.translate("")},
            {"horizontal 3d rotation", Language.translate("0 to 360")},
            {"vertical 3d rotation", Language.translate("0 to 360")},};
    public String[][] propertiesDefaultLimits = {
            {"position x", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().width, ""},
            {"position y", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().width, ""},
            {"x1", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().width, ""},
            {"y1", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().height, ""},
            {"x2", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().width, ""},
            {"y2", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().height, ""},
            {"relative x", "0.0", "1.0", ""},
            {"relative y", "0.0", "1.0", ""},
            {"trajectory position", "0.0", "1.0", "0.0"},
            {"width", "0", "500", ""},
            {"height", "0", "500", ""},
            {"zoom", "0", "20", "1.0"},
            {"rotation", "0", "360", "0.0"},
            {"shear x", "-1.0", "1.0", "0.0"},
            {"shear y", "-1.0", "1.0", "0.0"},
            {"visible area x", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().width, ""},
            {"visible area y", "0", "" + Toolkit.getDefaultToolkit().getScreenSize().height, ""},
            {"visible area width", "0", "500", ""},
            {"visible area height", "0", "500", ""},
            {"transparency", "0.0", "1.0", "1"},
            {"pen thickness", "0", "10", ""},
            {"perspective x1", "0.0", "1.0", "0.0"},
            {"perspective y1", "0.0", "1.0", "0.0"},
            {"perspective x2", "0.0", "1.0", "1.0"},
            {"perspective y2", "0.0", "1.0", "0.0"},
            {"perspective x3", "0.0", "1.0", "1.0"},
            {"perspective y3", "0.0", "1.0", "1.0"},
            {"perspective x4", "0.0", "1.0", "0.0"},
            {"perspective y4", "0.0", "1.0", "1.0"},
            {"perspective depth", "0.0", "1.0", "1"},
            {"horizontal 3d rotation", "0", "360", "0"},
            {"vertical 3d rotation", "0", "360", "0"}};
    public String[][] propertiesAnimation = {
            {"Position", null, null, null, null, null},
            {"position x", "", "", "", "", ""},
            {"position y", "", "", "", "", ""},
            {"relative x", "", "", "", "", ""},
            {"relative y", "", "", "", "", ""},
            {"trajectory position", "", "", "", "", ""},
            {"Size", null, null, null, null, null},
            {"width", "", "", "", "", ""},
            {"height", "", "", "", "", ""},
            {"Orientation", null, null, null, null, null},
            {"rotation", "", "", "", "", ""},
            {"Zoom", null, null, null, null, null},
            {"zoom", "", "", "", "", ""},
            {"Transparency", null, null, null, null, null},
            {"transparency", "", "", "", "", ""},
            {"Visible area", null, null, null, null, null},
            {"visible area x", "", "", "", "", ""},
            {"visible area y", "", "", "", "", ""},
            {"visible area width", "", "", "", "", ""},
            {"visible area height", "", "", "", "", ""},
            {"Motion", null, null, null, null, null},
            {"speed", "", "", "", "", ""},
            {"direction", "", "", "", "", ""},
            {"Pen", null, null, null, null, null},
            {"pen thickness", "", "", "", "", ""},
            {"Advanced / Borders", null, null, null, null, null},
            {"x1", "", "", "", "", ""},
            {"y1", "", "", "", "", ""},
            {"x2", "", "", "", "", ""},
            {"y2", "", "", "", "", ""},
            {"Advanced / Sheer", null, null, null, null, null},
            {"shear x", "", "", "", "", ""},
            {"shear y", "", "", "", "", ""},
            {"Advanced / 3D", null, null, null, null, null},
            {"horizontal 3d rotation", "", "", "", "", ""},
            {"vertical 3d rotation", "", "", "", "", ""},
            {"Advanced / Perspective", null, null, null, null, null},
            {"perspective x1", "", "", "", "", ""},
            {"perspective y1", "", "", "", "", ""},
            {"perspective x2", "", "", "", "", ""},
            {"perspective y2", "", "", "", "", ""},
            {"perspective x3", "", "", "", "", ""},
            {"perspective y3", "", "", "", "", ""},
            {"perspective x4", "", "", "", "", ""},
            {"perspective y4", "", "", "", "", ""},
            {"automatic perspective", "", "", "", "", ""},
            {"perspective depth", "", "", "", "", ""}
    };
    public static String[][] showImageProperties = {
            {"Drawn Image", null},
            {"image frame", Language.translate("frame to be shown (1, 2...)")},
            {"animation ms", Language.translate("animatioan defined by the pause between frames")},
            {"Align", null},
            {"horizontal alignment", ""},
            {"vertical alignment", ""},
            {"fit to box", ""},
            {"URL", null},
            {"image url", Language.translate("the URL to the image file")},
            {"Screen Capture", null},
            {"screen capture x", Language.translate("left position of the captured area")},
            {"screen capture y", Language.translate("top position of the captured area")},
            {"screen capture width", Language.translate("width of captured the area")},
            {"screen capture height", Language.translate("height of the captured area")},
            {"Shape", null},
            {"shape", Language.translate("rectangle...")},
            {"line style", Language.translate("")},
            {"line thickness", Language.translate("")},
            {"line color", Language.translate("")},
            {"fill color", Language.translate("")},
            {"Text", null},
            {"text", Language.translate("")},
            {"font name", Language.translate("")},
            {"font style", Language.translate("")},
            {"font size", Language.translate("")},
            {"text color", Language.translate("")},
            {"Advanced", null},
            {"html", Language.translate("")},
    };

    public String getAnimationProperty(String strProperty, int nProp) {
        for (int i = 0; i < this.propertiesAnimation.length; i++) {
            if (strProperty.equalsIgnoreCase(propertiesAnimation[i][0])) {
                return propertiesAnimation[i][nProp];
            }
        }
        return null;
    }

    public void setAnimationProperty(String strProperty, int nProp, String value) {
        for (int i = 0; i < this.propertiesAnimation.length; i++) {
            if (strProperty.equalsIgnoreCase(propertiesAnimation[i][0])) {
                propertiesAnimation[i][nProp] = value;
                return;
            }
        }
    }

    public int getPropertiesCount() {
        return this.showProperties.length;
    }

    public String[][] getData() {
        return this.showProperties;
    }

    public int getPropertyRow(String strProperty) {
        for (int i = 0; i < showProperties.length; i++) {
            if (showProperties[i][1] != null && showProperties[i][0].equalsIgnoreCase(strProperty)) {
                return i;
            }
        }
        return -1;
    }

    public String getPropertyDescription(String property) {
        for (int i = 0; i < showProperties.length; i++) {
            if (showProperties[i][1] != null && showProperties[i][0].equalsIgnoreCase(property)) {
                return showProperties[i][1];
            }
        }
        return "";
    }

    public void repaintProperties() {
        if (SketchletEditor.editorPanel != null) {
            SketchletEditor.editorPanel.repaint();
        }
        if (SketchletEditor.editorPanel.internalPlaybackPanel != null) {
            SketchletEditor.editorPanel.internalPlaybackPanel.repaint();
        }
        PlaybackFrame.repaintAllFrames();
    }

    public String getPropertyName(int property) {
        if (property >= 0 && property < showProperties.length) {
            return showProperties[property][0];
        } else {
            return "";
        }
    }

    public String getProperty(int property) {
        return getProperty(getPropertyName(property));
    }

    public String getPropertyXMLEncoded(String property) {
        return XMLUtils.prepareForXML(this.getProperty(property));
    }

    public String getProperty(String property) {
        return this.getProperty(property, false);
    }

    public String getProperty(String property, boolean bPlayback) {
        String strValue = "";
        if (property.equalsIgnoreCase("name")) {
            strValue = this.strName;
        } else if (property.equalsIgnoreCase("type")) {
            strValue = this.strType;
        } else if (property.equalsIgnoreCase("text")) {
            strValue = this.strText;
        } else if (property.equalsIgnoreCase("image url")) {
            strValue = this.strImageUrlField;
        } else if (property.equalsIgnoreCase("image frame")) {
            strValue = this.strImageIndex;
        } else if (property.equalsIgnoreCase("animation ms")) {
            strValue = this.strAnimationMs;
        } else if (property.equalsIgnoreCase("vertical alignment")) {
            strValue = this.strVAlign;
        } else if (property.equalsIgnoreCase("horizontal alignment")) {
            strValue = this.strHAlign;
        } else if (property.equalsIgnoreCase("screen capture x")) {
            strValue = this.strCaptureScreenX;
        } else if (property.equalsIgnoreCase("screen capture y")) {
            strValue = this.strCaptureScreenY;
        } else if (property.equalsIgnoreCase("screen capture width")) {
            strValue = this.strCaptureScreenWidth;
        } else if (property.equalsIgnoreCase("screen capture height")) {
            strValue = this.strCaptureScreenHeight;
        } else if (property.equalsIgnoreCase("shape")) {
            strValue = this.shape;
        } else if (property.equalsIgnoreCase("line style")) {
            strValue = this.strLineStyle;
        } else if (property.equalsIgnoreCase("line thickness")) {
            strValue = this.strLineThickness;
        } else if (property.equalsIgnoreCase("line color")) {
            strValue = this.strLineColor;
        } else if (property.equalsIgnoreCase("fill color")) {
            strValue = this.strFillColor;
        } else if (property.equalsIgnoreCase("font name")) {
            strValue = this.fontName;
        } else if (property.equalsIgnoreCase("font style")) {
            strValue = this.fontStyle;
        } else if (property.equalsIgnoreCase("font size")) {
            strValue = this.fontSize;
        } else if (property.equalsIgnoreCase("text color")) {
            strValue = this.fontColor;
        } else if (property.equalsIgnoreCase("fit to box")) {
            strValue = this.bFitToBox ? "true" : "false";
        } else if (property.equalsIgnoreCase("primary trajectory") || property.equalsIgnoreCase("trajectory 1")) {
            strValue = this.strTrajectory1;
        } else if (property.equalsIgnoreCase("secondary trajectory") || property.equalsIgnoreCase("trajectory 2")) {
            strValue = this.strTrajectory2;
        } else if (property.equalsIgnoreCase("position x")) {
            strValue = this.strX.isEmpty() ? this.getX(bPlayback) + "" : this.strX;
        } else if (property.equalsIgnoreCase("position y")) {
            strValue = this.strY.isEmpty() ? this.getY(bPlayback) + "" : this.strY;
        } else if (property.equalsIgnoreCase("x1")) {
            strValue = this.strX1.isEmpty() ? this.x1 + "" : this.strX1;
        } else if (property.equalsIgnoreCase("y1")) {
            strValue = this.strY1.isEmpty() ? this.y1 + "" : this.strY1;
        } else if (property.equalsIgnoreCase("x2")) {
            strValue = this.strX2.isEmpty() ? this.x2 + "" : this.strX2;
        } else if (property.equalsIgnoreCase("y2")) {
            strValue = this.strY2.isEmpty() ? this.y2 + "" : this.strY2;
        } else if (property.equalsIgnoreCase("relative x")) {
            strValue = this.strRelX;
        } else if (property.equalsIgnoreCase("zoom")) {
            strValue = this.strZoom;
        } else if (property.equalsIgnoreCase("relative y")) {
            strValue = this.strRelY;
        } else if (property.equalsIgnoreCase("trajectory position")) {
            strValue = this.strTrajectoryPosition;
        } else if (property.equalsIgnoreCase("trajectory position 2")) {
            strValue = this.renderer.trajectoryDrawingLayer.trajectoryPositionFromPoint2 + "";
        } else if (property.equalsIgnoreCase("width")) {
            strValue = this.strWidth.isEmpty() ? "" + (this.getWidth()) : this.strWidth;
        } else if (property.equalsIgnoreCase("height")) {
            strValue = this.strHeight.isEmpty() ? "" + (this.getHeight()) : this.strHeight;
        } else if (property.equalsIgnoreCase("rotation")) {
            strValue = this.strRotate.isEmpty() ? Math.toDegrees(bPlayback ? this.playback_rotation : this.rotation) + "" : this.strRotate;
        } else if (property.equalsIgnoreCase("shear x")) {
            strValue = this.strShearX;
        } else if (property.equalsIgnoreCase("shear y")) {
            strValue = this.strShearY;
        } else if (property.equalsIgnoreCase("visible area x")) {
            strValue = this.strWindowX;
        } else if (property.equalsIgnoreCase("visible area y")) {
            strValue = this.strWindowY;
        } else if (property.equalsIgnoreCase("visible area width")) {
            strValue = this.strWindowWidth;
        } else if (property.equalsIgnoreCase("visible area height")) {
            strValue = this.strWindowHeight;
        } else if (property.equalsIgnoreCase("transparency")) {
            strValue = this.strTransparency;
        } else if (property.equalsIgnoreCase("speed")) {
            strValue = this.strSpeed;
        } else if (property.equalsIgnoreCase("direction")) {
            strValue = this.strSpeedDirection;
        } else if (property.equalsIgnoreCase("rotation speed")) {
            strValue = this.strRotationSpeed;
        } else if (property.equalsIgnoreCase("pen thickness")) {
            strValue = this.strPen;
        } else if (property.equalsIgnoreCase("perspective x1")) {
            strValue = this.strPerspectiveX1.isEmpty() ? this.p_x0 + "" : this.strPerspectiveX1;
        } else if (property.equalsIgnoreCase("perspective y1")) {
            strValue = this.strPerspectiveY1.isEmpty() ? this.p_y0 + "" : this.strPerspectiveY1;
        } else if (property.equalsIgnoreCase("perspective x2")) {
            strValue = this.strPerspectiveX2.isEmpty() ? this.p_x1 + "" : this.strPerspectiveX2;
        } else if (property.equalsIgnoreCase("perspective y2")) {
            strValue = this.strPerspectiveY2.isEmpty() ? this.p_y1 + "" : this.strPerspectiveY2;
        } else if (property.equalsIgnoreCase("perspective x3")) {
            strValue = this.strPerspectiveX3.isEmpty() ? this.p_x2 + "" : this.strPerspectiveX3;
        } else if (property.equalsIgnoreCase("perspective y3")) {
            strValue = this.strPerspectiveY3.isEmpty() ? this.p_y2 + "" : this.strPerspectiveY3;
        } else if (property.equalsIgnoreCase("perspective x4")) {
            strValue = this.strPerspectiveX4.isEmpty() ? this.p_x3 + "" : this.strPerspectiveX4;
        } else if (property.equalsIgnoreCase("perspective y4")) {
            strValue = this.strPerspectiveY4.isEmpty() ? this.p_y3 + "" : this.strPerspectiveY4;
        } else if (property.equalsIgnoreCase("automatic perspective")) {
            strValue = this.strAutomaticPerspective;
        } else if (property.equalsIgnoreCase("perspective depth")) {
            strValue = this.strPerspectiveDepth;
        } else if (property.equalsIgnoreCase("horizontal 3d rotation")) {
            strValue = this.strRotation3DHorizontal;
        } else if (property.equalsIgnoreCase("vertical 3d rotation")) {
            strValue = this.strRotation3DVertical;
        }

        return strValue;
    }

    public String getPropertyValue(String property) {
        String strValue = this.getProperty(property);
        for (int i = 0; i < 10; i++) {
            if (strValue.startsWith("=")) {
                strValue = this.processText(strValue);
            }
        }
        return strValue;
    }

    public String getPropertyValue(String property, boolean bPlayback) {
        String strValue = this.getProperty(property, bPlayback);
        for (int i = 0; i < 10; i++) {
            if (strValue.startsWith("=")) {
                strValue = this.processText(strValue);
            }
        }
        return strValue;
    }

    public String getPropertyValue(int property) {
        String strValue = this.getProperty(property);
        for (int i = 0; i < 10; i++) {
            if (strValue.startsWith("=")) {
                strValue = this.processText(strValue);
            }
        }
        return strValue;
    }

    public String getPropertyInfo(String property) {
        for (int i = 0; i < propertiesInfo.length; i++) {
            if (property.equalsIgnoreCase(propertiesInfo[i][0])) {
                return propertiesInfo[i][0];
            }
        }
        return "";
    }

    public void resetPropertiesWithVariables() {
        for (int i = 0; i < ActiveRegion.propertiesInfo.length; i++) {
            if (getProperty(propertiesInfo[i][0]).startsWith("=")) {
                setProperty(propertiesInfo[i][0], "");
            }
        }
    }

    public void resetProperties() {
        for (int i = 0; i < propertiesInfo.length; i++) {
            if (getProperty(propertiesInfo[i][0]).startsWith("=")) {
                setProperty(propertiesInfo[i][0], "");
            }
        }
    }

    public void resetAllProperties() {
        for (int i = 0; i < propertiesInfo.length; i++) {
            setProperty(propertiesInfo[i][0], "");
        }
    }

    public void setAnimateProperty(String property, String type, String start, String end, String duration, String curve) {
        for (int i = 0; i < this.propertiesAnimation.length; i++) {
            if (propertiesAnimation[i][1] != null && propertiesAnimation[i][0].equals(property)) {
                propertiesAnimation[i][1] = type;
                propertiesAnimation[i][2] = start;
                propertiesAnimation[i][3] = end;
                propertiesAnimation[i][4] = duration;
                propertiesAnimation[i][5] = curve;
                return;
            }
        }
    }

    Vector<String> updatingProperties = new Vector<String>();

    public JComboBox getPropertiesCombo() {
        JComboBox combo = new JComboBox();
        combo.setEditable(true);
        combo.addItem("");

        for (int i = 2; i < ActiveRegion.propertiesInfo.length; i++) {
            if (propertiesInfo[i][1] != null) {
                combo.addItem(propertiesInfo[i][0]);
            }
        }

        return combo;
    }

    public void logPropertyUpdate(String name, String value, Component source) {
        TutorialPanel.addLine("cmd", "Set the region property: " + name + "=" + value, "details.gif", source);
    }

    public void setProperty(String property, String value) {
        if (updatingProperties.contains(property)) {
            return;
        }
        updatingProperties.add(property);
        String oldValue = this.getProperty(property);
        if (property.equalsIgnoreCase("name")) {
            this.strName = value;
        } else if (property.equalsIgnoreCase("text")) {
            this.strText = value;
        } else if (property.equalsIgnoreCase("image url")) {
            this.strImageUrlField = value;
        } else if (property.equalsIgnoreCase("active")) {
            this.strActive = value;
        } else if (property.equalsIgnoreCase("type")) {
            this.strType = value;
        } else if (property.equalsIgnoreCase("control")) {
            this.strWidget = value;
            this.widgetProperties = null;
        } else if (property.equalsIgnoreCase("widget")) {
            this.strWidget = value;
            this.widgetProperties = null;
        } else if (property.equalsIgnoreCase("control variable")) {
        } else if (property.equalsIgnoreCase("image frame")) {
            this.strImageIndex = value;
        } else if (property.equalsIgnoreCase("animation ms")) {
            this.strAnimationMs = value;
        } else if (property.equalsIgnoreCase("horizontal alignment")) {
            this.strHAlign = value;
        } else if (property.equalsIgnoreCase("fit to box")) {
            this.bFitToBox = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
        } else if (property.equalsIgnoreCase("vertical alignment")) {
            this.strVAlign = value;
        } else if (property.equalsIgnoreCase("screen capture x")) {
            this.strCaptureScreenX = value;
        } else if (property.equalsIgnoreCase("screen capture y")) {
            this.strCaptureScreenY = value;
        } else if (property.equalsIgnoreCase("screen capture width")) {
            this.strCaptureScreenWidth = value;
        } else if (property.equalsIgnoreCase("screen capture height")) {
            this.strCaptureScreenHeight = value;
        } else if (property.equalsIgnoreCase("shape")) {
            this.shape = value;
        } else if (property.equalsIgnoreCase("line style")) {
            this.strLineStyle = value;
        } else if (property.equalsIgnoreCase("line thickness")) {
            this.strLineThickness = value;
        } else if (property.equalsIgnoreCase("line color")) {
            this.strLineColor = value;
        } else if (property.equalsIgnoreCase("fill color")) {
            this.strFillColor = value;
        } else if (property.equalsIgnoreCase("font name")) {
            this.fontName = value;
        } else if (property.equalsIgnoreCase("font style")) {
            this.fontStyle = value;
        } else if (property.equalsIgnoreCase("font size")) {
            this.fontSize = value;
        } else if (property.equalsIgnoreCase("text color")) {
            this.fontColor = value;
        } else if (property.equalsIgnoreCase("primary trajectory") || property.equalsIgnoreCase("trajectory 1")) {
            this.strTrajectory1 = value;
        } else if (property.equalsIgnoreCase("secondary trajectory") || property.equalsIgnoreCase("trajectory 2")) {
            this.strTrajectory2 = value;
        } else if (property.equalsIgnoreCase("position x")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value) || value.isEmpty()) {
                this.strX = value;
            } else {
                try {
                    strX = "";
                    int num = (int) Double.parseDouble(value);
                    this.setX(num, false);
                    this.setX(num, true);
                } catch (Exception e) {
                    strX = value;
                }
            }
        } else if (property.equalsIgnoreCase("position y")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value) || value.isEmpty()) {
                this.strY = value;
            } else {
                try {
                    strY = "";
                    int num = (int) Double.parseDouble(value);
                    this.setY(num, false);
                    this.setY(num, true);
                } catch (Exception e) {
                    strY = value;
                }
            }
        } else if (property.equalsIgnoreCase("x1")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value) || value.isEmpty()) {
                this.strX1 = value;
            } else {
                try {
                    strX1 = "";
                    int num = (int) Double.parseDouble(value);
                    this.x1 = num;
                    this.playback_x1 = num;
                } catch (Exception e) {
                    strX1 = "";
                }
            }
        } else if (property.equalsIgnoreCase("x2")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value) || value.isEmpty()) {
                this.strX2 = value;
            } else {
                try {
                    strX2 = "";
                    int num = (int) Double.parseDouble(value);
                    this.x2 = num;
                    this.playback_x2 = num;
                } catch (Exception e) {
                    strX2 = "";
                }
            }
        } else if (property.equalsIgnoreCase("y1")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value) || value.isEmpty()) {
                this.strY1 = value;
            } else {
                try {
                    strY1 = "";
                    int num = (int) Double.parseDouble(value);
                    this.y1 = num;
                    this.playback_y1 = num;
                } catch (Exception e) {
                    strY1 = "";
                }
            }
        } else if (property.equalsIgnoreCase("y2")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value) || value.isEmpty()) {
                this.strY2 = value;
            } else {
                try {
                    strY2 = "";
                    int num = (int) Double.parseDouble(value);
                    this.y2 = num;
                    this.playback_y2 = num;
                } catch (Exception e) {
                    strY2 = "";
                }
            }
        } else if (property.equalsIgnoreCase("relative x")) {
            this.strRelX = value;
        } else if (property.equalsIgnoreCase("zoom")) {
            this.strZoom = value;
        } else if (property.equalsIgnoreCase("relative y")) {
            this.strRelY = value;
        } else if (property.equalsIgnoreCase("trajectory position")) {
            this.strTrajectoryPosition = value;
        } else if (property.equalsIgnoreCase("width")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value)) {
                this.strWidth = value;
            } else {
                try {
                    this.strWidth = "";
                    double num = Double.parseDouble(value);
                    this.setWidth((int) num);
                } catch (Exception e) {
                    strWidth = value;
                }
            }
        } else if (property.equalsIgnoreCase("height")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value)) {
                this.strHeight = value;
            } else {
                try {
                    this.strHeight = "";
                    double num = Double.parseDouble(value);
                    this.setHeight((int) num);
                } catch (Exception e) {
                    strHeight = value;
                }
            }
        } else if (property.equalsIgnoreCase("rotation")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value)) {
                this.strRotate = value;
            } else if (value.isEmpty()) {
                this.strRotate = "";
                this.rotation = 0.0;
                this.playback_rotation = 0.0;
            } else {
                try {
                    this.strRotate = "";
                    double num = Double.parseDouble(value);
                    this.rotation = Math.toRadians(num);
                    this.playback_rotation = rotation;
                } catch (Exception e) {
                    strRotate = value;
                }
            }
        } else if (property.equalsIgnoreCase("shear x")) {
            this.strShearX = value;
        } else if (property.equalsIgnoreCase("shear y")) {
            this.strShearY = value;
        } else if (property.equalsIgnoreCase("visible area x")) {
            this.strWindowX = value;
        } else if (property.equalsIgnoreCase("visible area y")) {
            this.strWindowY = value;
        } else if (property.equalsIgnoreCase("visible area width")) {
            this.strWindowWidth = value;
        } else if (property.equalsIgnoreCase("visible area height")) {
            this.strWindowHeight = value;
        } else if (property.equalsIgnoreCase("transparency")) {
            this.strTransparency = value;
        } else if (property.equalsIgnoreCase("speed")) {
            this.strSpeed = value;
        } else if (property.equalsIgnoreCase("direction")) {
            this.strSpeedDirection = value;
        } else if (property.equalsIgnoreCase("rotation speed")) {
            this.strRotationSpeed = value;
        } else if (property.equalsIgnoreCase("pen thickness")) {
            this.strPen = value;
        } else if (property.equalsIgnoreCase("perspective x1")) {
            this.strPerspectiveX1 = value;
            if (value.isEmpty()) {
                this.p_x0 = 0.0;
            }
        } else if (property.equalsIgnoreCase("perspective y1")) {
            this.strPerspectiveY1 = value;
            if (value.isEmpty()) {
                this.p_y0 = 0.0;
            }
        } else if (property.equalsIgnoreCase("perspective x2")) {
            this.strPerspectiveX2 = value;
            if (value.isEmpty()) {
                this.p_x1 = 1.0;
            }
        } else if (property.equalsIgnoreCase("perspective y2")) {
            this.strPerspectiveY2 = value;
            if (value.isEmpty()) {
                this.p_y1 = 0.0;
            }
        } else if (property.equalsIgnoreCase("perspective x3")) {
            this.strPerspectiveX3 = value;
            if (value.isEmpty()) {
                this.p_x2 = 1.0;
            }
        } else if (property.equalsIgnoreCase("perspective y3")) {
            this.strPerspectiveY3 = value;
            if (value.isEmpty()) {
                this.p_y2 = 1.0;
            }
        } else if (property.equalsIgnoreCase("perspective x4")) {
            this.strPerspectiveX4 = value;
            if (value.isEmpty()) {
                this.p_x3 = 0.0;
            }
        } else if (property.equalsIgnoreCase("perspective y4")) {
            this.strPerspectiveY4 = value;
            if (value.isEmpty()) {
                this.p_y3 = 1.0;
            }
        } else if (property.equalsIgnoreCase("automatic perspective")) {
            this.strAutomaticPerspective = value;
        } else if (property.equalsIgnoreCase("perspective depth")) {
            this.strPerspectiveDepth = value;
        } else if (property.equalsIgnoreCase("horizontal 3d rotation")) {
            this.strRotation3DHorizontal = value;
        } else if (property.equalsIgnoreCase("vertical 3d rotation")) {
            this.strRotation3DVertical = value;
        }

        updatingProperties.remove(property);
        RefreshTime.update();
    }

    public String getDefaultValue(String strProperty) {
        for (int i = 0; i < this.propertiesDefaultLimits.length; i++) {
            if (this.propertiesDefaultLimits[i][0].equalsIgnoreCase(strProperty)) {
                return propertiesDefaultLimits[i][3];
            }
        }

        return null;
    }

    public String getMinValue(String strProperty) {
        for (int i = 0; i < this.propertiesDefaultLimits.length; i++) {
            if (this.propertiesDefaultLimits[i][0].equalsIgnoreCase(strProperty)) {
                return propertiesDefaultLimits[i][1];
            }
        }

        return null;
    }

    public String getMaxValue(String strProperty) {
        for (int i = 0; i < this.propertiesDefaultLimits.length; i++) {
            if (this.propertiesDefaultLimits[i][0].equalsIgnoreCase(strProperty)) {
                return propertiesDefaultLimits[i][2];
            }
        }

        return null;
    }

    public String getTransferString(String strProperty) {
        if (strName.isEmpty()) {
            return "";
        } else {
            return strName + "." + strProperty;
        }
    }

    public static void main(String args[]) {
        ActiveRegion editors[] = new ActiveRegion[1000];
        for (int i = 0; i < editors.length; i++) {
            editors[i] = new ActiveRegion();
        }
        try {
            Thread.sleep(14000);
        } catch (Exception e) {
        }
    }

    public double getWidgetPropertyAsDouble(String strName) {
        String strResult = getWidgetProperty(strName);
        double result = 0.0;
        if (!strResult.isEmpty()) {
            try {
                result = Double.parseDouble(strResult);
            } catch (Exception e) {
            }
        }

        return result;
    }

    public String getWidgetProperty(String strName) {
        return getWidgetProperty(strName, true);
    }

    public void createWidgetPropertiesObject() {
        if (this.widgetProperties == null) {
            this.widgetProperties = new Properties();
            String defaultProperties[][] = WidgetPluginFactory.getDefaultProperties(new ActiveRegionContextImpl(this, new PageContextImpl(this.parent.page)));

            for (int i = 0; i < defaultProperties.length; i++) {
                this.widgetProperties.setProperty(defaultProperties[i][0], defaultProperties[i][1]);
            }

            String properties[] = this.strWidgetProperties.split(";");
            for (int i = 0; i < properties.length; i++) {
                String strProperty = properties[i];
                int n = strProperty.indexOf("=");
                if (n > 0 && n + 1 < strProperty.length()) {
                    String name = strProperty.substring(0, n);
                    String value = strProperty.substring(n + 1);
                    if (this.widgetProperties.getProperty(name) != null) {  // set only parameters found in default parameters
                        this.widgetProperties.setProperty(name, value);
                    }
                }
            }
        }
    }

    public String getWidgetProperty(String strName, boolean bProcess) {
        if (this.widgetProperties == null) {
            this.createWidgetPropertiesObject();
        }
        String strValue = this.widgetProperties.getProperty(strName);

        if (strValue != null) {
            return bProcess ? processText(strValue) : strValue;
        } else {
            return "";
        }
    }

    public String getWidgetPropertiesString(boolean bProcess) {
        if (this.widgetProperties == null) {
            this.createWidgetPropertiesObject();
        }
        String strProperties = "";
        for (String name : this.widgetProperties.stringPropertyNames()) {
            String value = this.widgetProperties.getProperty(name, "");
            value = bProcess ? processText(value) : value;

            strProperties += name + "=" + value + ";";
        }
        return strProperties;
    }

    public void setWidgetProperty(String strName, String value) {
        if (this.widgetProperties == null) {
            this.createWidgetPropertiesObject();
        }
        this.widgetProperties.setProperty(strName, value);
        this.strWidgetProperties = this.getWidgetPropertiesString(false);
    }

}