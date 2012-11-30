package net.sf.sketchlet.framework.model;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.common.file.FileUtils;
import net.sf.sketchlet.common.template.TemplateMarkers;
import net.sf.sketchlet.common.translation.Language;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.designer.context.ActiveRegionContextImpl;
import net.sf.sketchlet.designer.context.PageContextImpl;
import net.sf.sketchlet.designer.editor.SketchletEditor;
import net.sf.sketchlet.designer.editor.ui.profiles.Profiles;
import net.sf.sketchlet.designer.editor.ui.toolbars.ColorToolbar;
import net.sf.sketchlet.designer.playback.ui.PlaybackFrame;
import net.sf.sketchlet.framework.blackboard.evaluator.Evaluator;
import net.sf.sketchlet.framework.controller.ActiveRegionMotionController;
import net.sf.sketchlet.framework.controller.ActiveRegionMouseController;
import net.sf.sketchlet.framework.controller.ActiveRegionOverlapController;
import net.sf.sketchlet.framework.model.events.EventMacro;
import net.sf.sketchlet.framework.model.events.keyboard.KeyboardProcessor;
import net.sf.sketchlet.framework.model.events.mouse.MouseProcessor;
import net.sf.sketchlet.framework.model.events.overlap.RegionOverlapEventMacro;
import net.sf.sketchlet.framework.model.events.widget.WidgetEventMacro;
import net.sf.sketchlet.framework.model.geom.RegularPolygon;
import net.sf.sketchlet.framework.model.geom.StarPolygon;
import net.sf.sketchlet.framework.model.imagecache.ImageCache;
import net.sf.sketchlet.framework.model.programming.macros.Macro;
import net.sf.sketchlet.framework.renderer.regions.ActiveRegionRenderer;
import net.sf.sketchlet.framework.renderer.regions.TextDrawingLayer;
import net.sf.sketchlet.loaders.pluginloader.WidgetPluginFactory;
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

    public boolean visible = true;
    public boolean pinned = false;
    public boolean inFocus = false;
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
    public String windowX = "";
    public String windowY = "";
    public String windowWidth = "";
    public String windowHeight = "";
    public String transparency = "";
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
    public String shapeArguments = "";
    public String fontName = "";
    public String fontSize = "";
    public String fontColor = "";
    public String fontStyle = "";
    private String strImageFile = "";
    public int x1, y1, x2, y2;
    public double center_rotation_x = 0.5, center_rotation_y = 0.5;
    public double trajectory2_x = 0.25, trajectory2_y = 0.5;
    public double p_x0 = 0.0, p_y0 = 0.0, p_x1 = 1.0, p_y1 = 0.0, p_x2 = 1.0, p_y2 = 1.0, p_x3 = 0.0, p_y3 = 1.0;
    public int pen_x, pen_y;
    public double rotation, shearX, shearY;
    public ActiveRegions parent;
    private boolean drawImageChanged = false;
    public Vector<String> additionalImageFile = new Vector<String>();
    public boolean inTrajectoryMode = false;
    public boolean inTrajectoryMode2 = false;
    public int trajectoryType = 0;
    public String horizontalAlignment = "";
    public String verticalAlignment = "";
    public String lineColor = "";
    public String lineThickness = "";
    public String lineStyle = "";
    public String strFillColor = "";
    public String captureScreenX = "";
    public String captureScreenY = "";
    public String captureScreenWidth = "";
    public String captureScreenHeight = "";
    public String textField = "";
    public String embeddedSketch = "";
    public String embeddedSketchVarPrefix = "";
    public String embeddedSketchVarPostfix = "";
    public String imageUrlField = "";
    public String active = "";
    public String type = "";
    public String widget = "";
    public String widgetPropertiesString = "";
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
    // renderer
    private ActiveRegionRenderer renderer = new ActiveRegionRenderer(this);
    // handlers
    private ActiveRegionMouseController mouseController = new ActiveRegionMouseController(this);
    private ActiveRegionOverlapController interactionController = new ActiveRegionOverlapController(this);
    private ActiveRegionMotionController motionController = new ActiveRegionMotionController(this);
    // images
    public BufferedImage image = null;
    private BufferedImage drawImage = null;
    public Vector<BufferedImage> additionalDrawImages = new Vector<BufferedImage>();
    public Vector<Boolean> additionalImageChanged = new Vector<Boolean>();
    public String text = "";
    public String trajectory1 = "";
    public String trajectory2 = "";
    public String name = "";
    public boolean movable = false;
    public boolean rotatable = false;
    public boolean resizable = false;
    public boolean fitToBoxEnabled = true;
    public boolean textWrapped = false;
    public boolean textTrimmed = false;
    public boolean walkThroughEnabled = false;
    public boolean stickToTrajectoryEnabled = true;
    public boolean changingOrientationOnTrajectoryEnabled = true;
    public boolean screenCapturingEnabled = false;
    public boolean screenCapturingMouseMappingEnabled = false;
    public String widgetItems = "";
    public String charactersPerLine = "";
    public String maxNumLines = "";

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
                if (region.parent.getPage().getSourceDirectory() != null) {
                    imgFile = new File(region.getDrawImageFile(region.parent.getPage().getSourceDirectory(), aai));
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
            this.getMouseController().setStartAngle(region.getMouseController().getStartAngle());
            this.getMouseController().setStartX(region.getMouseController().getStartX());
            this.getMouseController().setStartY(region.getMouseController().getStartY());
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

            this.type = region.type;
            this.strImageIndex = region.strImageIndex;
            this.strAnimationMs = region.strAnimationMs;
            image = region.image;
        }

        this.deactivate(false);
    }

    public static ActiveRegion getInstanceForBatchProcessing() {
        Page page = new Page("", "");
        page.setTitle("Page 1");
        page.setRegions(new ActiveRegions(page));

        ActiveRegion region = new ActiveRegion(page.getRegions());

        return region;
    }

    public Page getSketch() {
        return this.parent.getPage();
    }

    public boolean isActive(boolean bPlayback) {
        boolean active = bPlayback ? !this.processText(this.active).equalsIgnoreCase("false") : true;

        active = active && getSketch().isLayerActive(layer, bPlayback);

        return this.visible && active;
    }

    public String getType() {
        return this.type;
    }

    public boolean isMouseActive() {
        return mouseProcessor.getMouseActionsCount() > 0 || movable || rotatable || !this.widget.isEmpty();
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
        if (getRenderer() != null) {
            getRenderer().flush();
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

        return TextDrawingLayer.getFont(name, style, (float) size);
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
        String strLineThickness = this.processText(this.lineThickness);
        String strLineStyle = this.processText(this.lineStyle);

        int lineThickness = 2;
        try {
            lineThickness = Integer.parseInt(strLineThickness);
        } catch (Exception e) {
        }

        return ColorToolbar.getStroke(strLineStyle, lineThickness);
    }

    public Color getLineColor() {
        return getColor(lineColor);
    }

    public int getLineThickness() {
        int lineThickness = 2;
        String strLineThickness = processText(this.lineThickness);
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
        if (getRenderer() != null) {
            getRenderer().dispose();
        }
        if (getMouseController() != null) {
            getMouseController().dispose();
        }
        if (getInteractionController() != null) {
            getInteractionController().dispose();
        }
        if (getMotionController() != null) {
            getMotionController().dispose();
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
        windowX = null;
        windowY = null;
        windowWidth = null;
        windowHeight = null;
        transparency = null;
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
        setRenderer(null);
        setMouseController(null);
        setInteractionController(null);
        setMotionController(null);
        horizontalAlignment = null;
        verticalAlignment = null;
        lineColor = null;
        lineThickness = null;
        lineStyle = null;
        strFillColor = null;
        captureScreenX = null;
        captureScreenY = null;
        captureScreenWidth = null;
        captureScreenHeight = null;
        textField = null;
        embeddedSketch = null;
        embeddedSketchVarPrefix = null;
        embeddedSketchVarPostfix = null;
        imageUrlField = null;
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
        this.name = name;
    }

    public String getName() {
        if (name.isEmpty()) {
            return getNumber();
        } else {
            return name;
        }
    }

    public String getLongName() {
        if (name.isEmpty()) {
            return getNumber();
        } else {
            return getNumber() + " (" + name + ")";
        }
    }

    public boolean isSelected() {
        return parent.getMouseHelper().getSelectedRegions() != null && parent.getMouseHelper().getSelectedRegions().contains(this);
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
        this.name = a.name;

        this.shape = a.shape;
        this.shapeArguments = a.shapeArguments;
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
        this.windowX = a.windowX;
        this.windowY = a.windowY;
        this.windowWidth = a.windowWidth;
        this.windowHeight = a.windowHeight;
        this.transparency = a.transparency;
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

        this.stickToTrajectoryEnabled = a.stickToTrajectoryEnabled;
        this.changingOrientationOnTrajectoryEnabled = a.changingOrientationOnTrajectoryEnabled;
        this.trajectory1 = a.trajectory1;
        this.trajectory2 = a.trajectory2;

        this.widgetItems = a.widgetItems;

        this.movable = a.movable;
        this.resizable = a.resizable;
        this.rotatable = a.rotatable;
        this.fitToBoxEnabled = a.fitToBoxEnabled;

        this.textField = a.textField;
        this.text = a.text;

        this.textWrapped = a.textWrapped;
        this.textTrimmed = a.textTrimmed;
        this.charactersPerLine = a.charactersPerLine;
        this.maxNumLines = a.maxNumLines;

        this.screenCapturingEnabled = a.screenCapturingEnabled;
        this.screenCapturingMouseMappingEnabled = a.screenCapturingMouseMappingEnabled;

        this.walkThroughEnabled = a.walkThroughEnabled;

        this.imageUrlField = a.imageUrlField;
        this.strImageIndex = a.strImageIndex;
        this.active = a.active;
        this.type = a.type;
        this.widget = a.widget;
        this.widgetProperties = null;
        this.widgetPropertiesString = a.widgetPropertiesString;
        this.strAnimationMs = a.strAnimationMs;

        this.embeddedSketch = a.embeddedSketch;
        this.embeddedSketchVarPrefix = a.embeddedSketchVarPrefix;
        this.embeddedSketchVarPostfix = a.embeddedSketchVarPostfix;

        this.horizontalAlignment = a.horizontalAlignment;
        this.verticalAlignment = a.verticalAlignment;

        this.fontName = a.fontName;
        this.fontSize = a.fontSize;
        this.fontStyle = a.fontStyle;
        this.fontColor = a.fontColor;

        this.lineColor = a.lineColor;
        this.lineStyle = a.lineStyle;
        this.lineThickness = a.lineThickness;
        this.strFillColor = a.strFillColor;

        this.captureScreenX = a.captureScreenX;
        this.captureScreenY = a.captureScreenY;
        this.captureScreenWidth = a.captureScreenWidth;
        this.captureScreenHeight = a.captureScreenHeight;

    }

    public String getVarPrefix() {
        if (this.parent != null) {
            return this.parent.getPage().getVarPrefix();
        } else {
            return "";
        }
    }

    public String getVarPostfix() {
        if (this.parent != null) {
            return this.parent.getPage().getVarPostfix();
        } else {
            return "";
        }
    }

    public int getX1() {
        return (parent != null ? parent.getOffsetX() : 0) + x1;
    }

    public int getY1() {
        return (parent != null ? parent.getOffsetY() : 0) + y1;
    }

    public int getX2() {
        return (parent != null ? parent.getOffsetX() : 0) + x2;
    }

    public int getY2() {
        return (parent != null ? parent.getOffsetY() : 0) + y2;
    }

    public void play() {
    }

    public int getWidth() {
        return Math.abs(x2 - x1);
    }

    public int getHeight() {
        return Math.abs(y2 - y1);
    }

    public void startDefiningTrajectory(int type) {
        this.trajectory1 = "";
        this.trajectory2 = "";
        this.inTrajectoryMode = true;
        this.inTrajectoryMode2 = false;
        this.trajectoryType = type;
    }

    public void startDefiningTrajectory2(int type) {
        this.trajectory2 = "";
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

        if (this.getRenderer() != null) {
            this.getRenderer().deactivate(bPlayback);
            this.getRenderer().dispose();
            this.setRenderer(new ActiveRegionRenderer(this));
        }

        getInteractionController().reset();
    }

    public void activate(boolean bPlayback) {
        bActive = true;
        if (this.getRenderer() != null) {
            this.getRenderer().activate(bPlayback);
        }
        if (!bPlayback) {
        } else {
            this.initPlayback();
        }

        getInteractionController().reset();
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
        return "" + (parent.getRegions().size() - parent.getRegions().indexOf(this));
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
            if (ImageCache.getImages() != null && ImageCache.getImages().get(file) != null && image != null) {
                ImageCache.getImages().get(file).flush();
                ImageCache.getImages().put(file, image);
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
            log.error(e);
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
        String strShowText = XMLUtils.prepareForXML(this.textField);
        String strTextArea = this.text;
        String strTrajectory = XMLUtils.prepareForXML(this.trajectory1);
        String strTrajectory2 = XMLUtils.prepareForXML(this.trajectory2);

        String strControlItems = this.widgetItems;

        String strEmbedSketch = XMLUtils.prepareForXML(this.embeddedSketch);
        String strEmbedPrefix = XMLUtils.prepareForXML(this.embeddedSketchVarPrefix);
        String strEmbedPostfix = XMLUtils.prepareForXML(this.embeddedSketchVarPostfix);
        String strCaptureScreenX = XMLUtils.prepareForXML(this.captureScreenX);
        String strCaptureScreenY = XMLUtils.prepareForXML(this.captureScreenY);
        String strCaptureScreenWidth = XMLUtils.prepareForXML(this.captureScreenWidth);
        String strCaptureScreenHeight = XMLUtils.prepareForXML(this.captureScreenHeight);

        String strImage = this.imageUrlField;
        strImage = XMLUtils.prepareForXML(strImage);
        String strIndex = this.strImageIndex;
        String strActive = XMLUtils.prepareForXML(this.active);
        String strType = XMLUtils.prepareForXML(this.type);
        String strControl = XMLUtils.prepareForXML(this.widget);
        String strControlProperties = XMLUtils.prepareForXML(this.widgetPropertiesString);
        strIndex = XMLUtils.prepareForXML(strIndex);

        String strCanMove = this.movable + "";
        String strCanRotate = this.rotatable + "";
        String strCanResize = this.resizable + "";

        long lastModified = 0;
        try {
            lastModified = new File(getDrawImagePath()).lastModified();
        } catch (Throwable e) {
        }

        out.println("<active-region>");
        out.println("    <region name='" + name + "' x1='" + x1 + "' y1='" + y1 + "' x2='" + x2 + "' y2='" + y2 + "' shearX='" + shearX + "' shearY='" + shearY + "' rotation='" + rotation + "'/>");
        out.println("    <perspective p_x0='" + p_x0 + "' p_y0='" + p_y0 + "' p_x1='" + p_x1 + "' p_y1='" + p_y1 + "' p_x2='" + p_x2 + "' p_y2='" + p_y2 + "' p_x3='" + p_x3 + "' p_y3='" + p_y3 + "'/>");
        out.println("    <rotation_center x='" + center_rotation_x + "' y='" + center_rotation_y + "'/>");
        out.println("    <trajectory_point x='" + trajectory2_x + "' y='" + trajectory2_y + "'/>");
        out.println("    <layer>" + this.layer + "</layer>");
        out.println("    <visible>" + this.visible + "</visible>");
        if (!this.shape.isEmpty()) {
            out.println("    <basic-shape>" + XMLUtils.prepareForXML(shape) + "</basic-shape>");
        }
        if (!this.shapeArguments.isEmpty()) {
            out.println("    <basic-shape-args>" + XMLUtils.prepareForXML(shapeArguments) + "</basic-shape-args>");
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
        out.println("    <capture-screen>" + this.screenCapturingEnabled + "</capture-screen>");
        out.println("    <capture-screen-mouse-map>" + this.screenCapturingMouseMappingEnabled + "</capture-screen-mouse-map>");
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

        out.println("    <wrap-text>" + this.textWrapped + "</wrap-text>");
        out.println("    <trim-text>" + this.textTrimmed + "</trim-text>");
        out.println("    <is-solid>" + this.walkThroughEnabled + "</is-solid>");
        if (!this.charactersPerLine.isEmpty()) {
            out.println("    <characters-per-line>" + XMLUtils.prepareForXML(this.charactersPerLine) + "</characters-per-line>");
        }
        if (!this.maxNumLines.isEmpty()) {
            out.println("    <max-lines>" + this.maxNumLines + "</max-lines>");
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
        out.println("    <stick-to-trajectory>" + this.stickToTrajectoryEnabled + "</stick-to-trajectory>");
        out.println("    <orientation-trajectory>" + this.changingOrientationOnTrajectoryEnabled + "</orientation-trajectory>");
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
        out.println("       <fit-to-box>" + this.fitToBoxEnabled + "</fit-to-box>");
        if (!horizontalAlignment.isEmpty()) {
            out.println("       <horizontal-alignment>" + this.horizontalAlignment + "</horizontal-alignment>");
        }
        if (!verticalAlignment.isEmpty()) {
            out.println("       <vertical-alignment>" + this.verticalAlignment + "</vertical-alignment>");
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
        if (!windowX.isEmpty()) {
            out.println("        <windowX>" + XMLUtils.prepareForXML(windowX) + "</windowX>");
        }
        if (!windowY.isEmpty()) {
            out.println("        <windowY>" + XMLUtils.prepareForXML(windowY) + "</windowY>");
        }
        if (!windowWidth.isEmpty()) {
            out.println("        <windowWidth>" + XMLUtils.prepareForXML(windowWidth) + "</windowWidth>");
        }
        if (!windowHeight.isEmpty()) {
            out.println("        <windowHeight>" + XMLUtils.prepareForXML(windowHeight) + "</windowHeight>");
        }
        if (!transparency.isEmpty()) {
            out.println("        <transparency>" + XMLUtils.prepareForXML(transparency) + "</transparency>");
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
        if (!lineColor.isEmpty()) {
            out.println("       <line-color>" + XMLUtils.prepareForXML(this.lineColor) + "</line-color>");
        }
        if (!lineStyle.isEmpty()) {
            out.println("       <line-style>" + XMLUtils.prepareForXML(this.lineStyle) + "</line-style>");
        }
        if (!lineThickness.isEmpty()) {
            out.println("       <line-thickness>" + XMLUtils.prepareForXML(this.lineThickness) + "</line-thickness>");
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
            macro.setName(widgetEventMacro.getEventName());
            macro.saveSimple(out, "widget-event-action", "        ");
        }
        out.println("    </region-widget-event-actions>");
        out.println("    <region-mouse-event-actions>");
        for (EventMacro mouseEventMacro : mouseProcessor.getMouseEventMacros()) {
            mouseEventMacro.getMacro().setName(mouseEventMacro.getEventName());
            mouseEventMacro.getMacro().saveSimple(out, "mouse-event-action", "        ");
        }
        out.println("    </region-mouse-event-actions>");
        out.println("    <region-overlap-event-actions>");
        for (EventMacro regionOverlapEventMacro : regionOverlapEventMacros) {
            regionOverlapEventMacro.getMacro().setName(regionOverlapEventMacro.getEventName());
            regionOverlapEventMacro.getMacro().saveSimple(out, "region-overlap-event-action", "        ");
        }
        out.println("    </region-overlap-event-actions>");
        out.println("    <region-keyboard-event-actions>");
        for (EventMacro regionKeyboardEventMacro : keyboardProcessor.getKeyboardEventMacros()) {
            regionKeyboardEventMacro.getMacro().setName(regionKeyboardEventMacro.getEventName());
            regionKeyboardEventMacro.getMacro().saveSimple(out, "region-keyboard-event-action", "        ");
        }
        out.println("    </region-keyboard-event-actions>");

        out.println("</active-region>");

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

        img = Workspace.createCompatibleImage(w, h, img);

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
        return new Rectangle(x1, y1, getWidth(), getHeight());
    }

    public Rectangle getBounds(boolean bPlayback) {
        AffineTransform aft = new AffineTransform();
        aft.shear(shearX, shearY);
        aft.rotate(rotation, x1 + (x2 - x1) * center_rotation_x, y1 + (y2 - y1) * center_rotation_y);
        Area area = getArea(bPlayback);
        area.transform(aft);
        return area.getBounds();
    }

    public Point getInversePoint(boolean bPlayback, int x, int y) {
        AffineTransform aft = new AffineTransform();
        aft.shear(shearX, shearY);
        aft.rotate(rotation, x1 + (x2 - x1) * center_rotation_x, y1 + (y2 - y1) * center_rotation_y);

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
        aft.shear(shearX, shearY);
        aft.rotate(rotation, x1 + (x2 - x1) * center_rotation_x, y1 + (y2 - y1) * center_rotation_y);
        Area area = getArea(bPlayback);
        area.transform(aft);
        return area;
    }

    public Area getArea(boolean bPlayback) {
        String strShape = this.processText(this.shape);
        if (strShape.equalsIgnoreCase("Oval")) {
            return new Area(new Ellipse2D.Double(x1, y1, getWidth(), getHeight()));
        } else if (strShape.equalsIgnoreCase("Rounded Rectangle")) {
            try {
                double n = Double.parseDouble(this.processText(shapeArguments));
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
                    String strArgs = this.processText(shapeArguments);
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
                String strArgs = this.processText(shapeArguments);
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

    public Polygon scalePolygon(boolean bPlayback, Polygon p) {
        int w = this.getWidth();
        int h = this.getHeight();
        int x = this.x1;
        int y = this.y1;

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
        if (this.horizontalAlignment.equalsIgnoreCase("center")) {
            x = x1 + (x2 - x1) / 2;
        } else if (this.horizontalAlignment.equalsIgnoreCase("right")) {
            x = x2;
        } else {
            x = x1;
        }
        return x;
    }

    public int getCenterX(boolean bPlayback) {
        int x;
            x = x1 + (x2 - x1) / 2;
        return x;
    }

    public void setX(int x, boolean bPlayback) {
            int w = x2 - x1;
            if (this.horizontalAlignment.equalsIgnoreCase("center")) {
                x1 = x - w / 2;
                x2 = x + w / 2;
            } else if (this.horizontalAlignment.equalsIgnoreCase("right")) {
                x1 = x - w;
                x2 = x;
            } else {
                x1 = x;
                x2 = x + w;
            }
    }

    public int getY(boolean bPlayback) {
        int y;
            if (this.verticalAlignment.equalsIgnoreCase("center")) {
                y = y1 + (y2 - y1) / 2;
            } else if (this.verticalAlignment.equalsIgnoreCase("bottom")) {
                y = y2;
            } else {
                y = y1;
            }
        return y;
    }

    public int getCenterY(boolean bPlayback) {
        int y;
            y = y1 + (y2 - y1) / 2;
        return y;
    }

    public void setY(int y, boolean bPlayback) {
            int h = y2 - y1;
            if (this.verticalAlignment.equalsIgnoreCase("center")) {
                y1 = y - h / 2;
                y2 = y + h / 2;
            } else if (this.verticalAlignment.equalsIgnoreCase("bottom")) {
                y1 = y - h;
                y2 = y;
            } else {
                y1 = y;
                y2 = y + h;
            }
    }

    public void processLimitsTrajectory(Point p) {
        getRenderer().getTrajectoryDrawingLayer().getClosestTrajectoryPoint(p);
        getMotionController().processLimits("trajectory position", getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint, 0.0, 1.0, 0.0, 0.0, true);
        getMotionController().processLimits("trajectory position 2", getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint2, 0.0, 1.0, 0.0, 0.0, true);
    }

    public void processLimitsX() {
        int w = this.x2 - this.x1;
        if (this.horizontalAlignment.equalsIgnoreCase("center")) {
            this.getMotionController().processLimits("position x", this.x1 + w / 2, w / 2, w / 2, true);
        } else if (this.horizontalAlignment.equalsIgnoreCase("right")) {
            this.getMotionController().processLimits("position x", this.x1 + w, w, 0, true);
        } else {
            this.getMotionController().processLimits("position x", this.x1, 0, w, true);
        }
    }

    public void processLimitsY() {
        int h = this.y2 - this.y1;
        if (this.verticalAlignment.equalsIgnoreCase("center")) {
            this.getMotionController().processLimits("position y", this.y1 + h / 2, h / 2, h / 2, true);
        } else if (this.verticalAlignment.equalsIgnoreCase("right")) {
            this.getMotionController().processLimits("position y", this.y1 + h, h, 0, true);
        } else {
            this.getMotionController().processLimits("position y", this.y1, 0, h, true);
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

        int _bx1 = (int) getMotionController().processLimits("position x", bx1, 0, 0, false);
        int _by1 = (int) getMotionController().processLimits("position y", by1, 0, 0, false);
        int _bx2 = (int) getMotionController().processLimits("position x", bx2, 0, 0, false);
        int _by2 = (int) getMotionController().processLimits("position y", by2, 0, 0, false);

        if (bx1 != _bx1 || by1 != _by1 || bx2 != _bx2 || by2 != _by2) {
            return false;
        }

        return true;
    }

    public Rectangle getPropertiesIconRectangle() {
        double s = Math.min(1, SketchletEditor.getInstance().getScale());
        return new Rectangle((int) (x2 - 30 / s), (int) (y2 - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getMappingIconRectangle() {
        double s = Math.min(1, SketchletEditor.getInstance().getScale());
        return new Rectangle((int) (x2 - 60 / s), (int) (y2 - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getMouseIconRectangle() {
        double s = Math.min(1, SketchletEditor.getInstance().getScale());
        return new Rectangle((int) (x2 - 90 / s), (int) (y2 - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getKeyboardIconRectangle() {
        double s = Math.min(1, SketchletEditor.getInstance().getScale());
        return new Rectangle((int) (x2 - 90 / s), (int) (y2 - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getInRegionsIconRectangle() {
        double s = Math.min(1, SketchletEditor.getInstance().getScale());
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
        return createTrajectoryVector(trajectory1);
    }

    public Vector<TrajectoryPoint> createTrajectory2Vector() {
        return createTrajectoryVector(trajectory2);
    }

    public Vector<TrajectoryPoint> createTrajectoryVector(String strTrajectory) {
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

        if (horizontalAlignment.equalsIgnoreCase("center")) {
            x1 = x1 + oldW / 2 - w / 2;
            x2 = x1 + w;
        } else if (horizontalAlignment.equalsIgnoreCase("right")) {
            x1 = x1 + oldW - w;
            x2 = x1 + w;
        } else {
            x2 = x1 + w;
        }
    }

    public void setHeight(int h) {
        int oldH = y2 - y1;

        if (verticalAlignment.equalsIgnoreCase("center")) {
            y1 = y1 + oldH / 2 - h / 2;
            y2 = y1 + h;
        } else if (verticalAlignment.equalsIgnoreCase("right")) {
            y1 = y1 + oldH - h;
            y2 = y1 + h;
        } else {
            y2 = y1 + h;
        }
    }

    public void initPlayback() {
        pen_x = 0;
        pen_y = 0;
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
        if (SketchletEditor.getInstance() != null) {
            SketchletEditor.getInstance().repaint();
        }
        if (SketchletEditor.getInstance().getInternalPlaybackPanel() != null) {
            SketchletEditor.getInstance().getInternalPlaybackPanel().repaint();
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
            strValue = this.name;
        } else if (property.equalsIgnoreCase("type")) {
            strValue = this.type;
        } else if (property.equalsIgnoreCase("text")) {
            strValue = this.text;
        } else if (property.equalsIgnoreCase("image url")) {
            strValue = this.imageUrlField;
        } else if (property.equalsIgnoreCase("image frame")) {
            strValue = this.strImageIndex;
        } else if (property.equalsIgnoreCase("animation ms")) {
            strValue = this.strAnimationMs;
        } else if (property.equalsIgnoreCase("vertical alignment")) {
            strValue = this.verticalAlignment;
        } else if (property.equalsIgnoreCase("horizontal alignment")) {
            strValue = this.horizontalAlignment;
        } else if (property.equalsIgnoreCase("screen capture x")) {
            strValue = this.captureScreenX;
        } else if (property.equalsIgnoreCase("screen capture y")) {
            strValue = this.captureScreenY;
        } else if (property.equalsIgnoreCase("screen capture width")) {
            strValue = this.captureScreenWidth;
        } else if (property.equalsIgnoreCase("screen capture height")) {
            strValue = this.captureScreenHeight;
        } else if (property.equalsIgnoreCase("shape")) {
            strValue = this.shape;
        } else if (property.equalsIgnoreCase("line style")) {
            strValue = this.lineStyle;
        } else if (property.equalsIgnoreCase("line thickness")) {
            strValue = this.lineThickness;
        } else if (property.equalsIgnoreCase("line color")) {
            strValue = this.lineColor;
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
            strValue = this.fitToBoxEnabled ? "true" : "false";
        } else if (property.equalsIgnoreCase("primary trajectory") || property.equalsIgnoreCase("trajectory 1")) {
            strValue = this.trajectory1;
        } else if (property.equalsIgnoreCase("secondary trajectory") || property.equalsIgnoreCase("trajectory 2")) {
            strValue = this.trajectory2;
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
            strValue = this.getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint2 + "";
        } else if (property.equalsIgnoreCase("width")) {
            strValue = this.strWidth.isEmpty() ? "" + (this.getWidth()) : this.strWidth;
        } else if (property.equalsIgnoreCase("height")) {
            strValue = this.strHeight.isEmpty() ? "" + (this.getHeight()) : this.strHeight;
        } else if (property.equalsIgnoreCase("rotation")) {
            strValue = this.strRotate.isEmpty() ? Math.toDegrees(this.rotation) + "" : this.strRotate;
        } else if (property.equalsIgnoreCase("shear x")) {
            strValue = this.strShearX;
        } else if (property.equalsIgnoreCase("shear y")) {
            strValue = this.strShearY;
        } else if (property.equalsIgnoreCase("visible area x")) {
            strValue = this.windowX;
        } else if (property.equalsIgnoreCase("visible area y")) {
            strValue = this.windowY;
        } else if (property.equalsIgnoreCase("visible area width")) {
            strValue = this.windowWidth;
        } else if (property.equalsIgnoreCase("visible area height")) {
            strValue = this.windowHeight;
        } else if (property.equalsIgnoreCase("transparency")) {
            strValue = this.transparency;
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
    }

    public void setProperty(String property, String value) {
        if (updatingProperties.contains(property)) {
            return;
        }
        updatingProperties.add(property);
        if (property.equalsIgnoreCase("name")) {
            this.name = value;
        } else if (property.equalsIgnoreCase("text")) {
            this.text = value;
        } else if (property.equalsIgnoreCase("image url")) {
            this.imageUrlField = value;
        } else if (property.equalsIgnoreCase("active")) {
            this.active = value;
        } else if (property.equalsIgnoreCase("type")) {
            this.type = value;
        } else if (property.equalsIgnoreCase("control")) {
            this.widget = value;
            this.widgetProperties = null;
        } else if (property.equalsIgnoreCase("widget")) {
            this.widget = value;
            this.widgetProperties = null;
        } else if (property.equalsIgnoreCase("control variable")) {
        } else if (property.equalsIgnoreCase("image frame")) {
            this.strImageIndex = value;
        } else if (property.equalsIgnoreCase("animation ms")) {
            this.strAnimationMs = value;
        } else if (property.equalsIgnoreCase("horizontal alignment")) {
            this.horizontalAlignment = value;
        } else if (property.equalsIgnoreCase("fit to box")) {
            this.fitToBoxEnabled = value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes");
        } else if (property.equalsIgnoreCase("vertical alignment")) {
            this.verticalAlignment = value;
        } else if (property.equalsIgnoreCase("screen capture x")) {
            this.captureScreenX = value;
        } else if (property.equalsIgnoreCase("screen capture y")) {
            this.captureScreenY = value;
        } else if (property.equalsIgnoreCase("screen capture width")) {
            this.captureScreenWidth = value;
        } else if (property.equalsIgnoreCase("screen capture height")) {
            this.captureScreenHeight = value;
        } else if (property.equalsIgnoreCase("shape")) {
            this.shape = value;
        } else if (property.equalsIgnoreCase("line style")) {
            this.lineStyle = value;
        } else if (property.equalsIgnoreCase("line thickness")) {
            this.lineThickness = value;
        } else if (property.equalsIgnoreCase("line color")) {
            this.lineColor = value;
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
            this.trajectory1 = value;
        } else if (property.equalsIgnoreCase("secondary trajectory") || property.equalsIgnoreCase("trajectory 2")) {
            this.trajectory2 = value;
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
            } else {
                try {
                    this.strRotate = "";
                    double num = Double.parseDouble(value);
                    this.rotation = Math.toRadians(num);
                } catch (Exception e) {
                    strRotate = value;
                }
            }
        } else if (property.equalsIgnoreCase("shear x")) {
            this.strShearX = value;
        } else if (property.equalsIgnoreCase("shear y")) {
            this.strShearY = value;
        } else if (property.equalsIgnoreCase("visible area x")) {
            this.windowX = value;
        } else if (property.equalsIgnoreCase("visible area y")) {
            this.windowY = value;
        } else if (property.equalsIgnoreCase("visible area width")) {
            this.windowWidth = value;
        } else if (property.equalsIgnoreCase("visible area height")) {
            this.windowHeight = value;
        } else if (property.equalsIgnoreCase("transparency")) {
            this.transparency = value;
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
        if (name.isEmpty()) {
            return "";
        } else {
            return name + "." + strProperty;
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
            String defaultProperties[][] = WidgetPluginFactory.getDefaultProperties(new ActiveRegionContextImpl(this, new PageContextImpl(this.parent.getPage())));

            for (int i = 0; i < defaultProperties.length; i++) {
                this.widgetProperties.setProperty(defaultProperties[i][0], defaultProperties[i][1]);
            }

            String properties[] = this.widgetPropertiesString.split(";");
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
        this.widgetPropertiesString = this.getWidgetPropertiesString(false);
    }

    public ActiveRegionRenderer getRenderer() {
        return renderer;
    }

    public void setRenderer(ActiveRegionRenderer renderer) {
        this.renderer = renderer;
    }

    public ActiveRegionMouseController getMouseController() {
        return mouseController;
    }

    public void setMouseController(ActiveRegionMouseController mouseController) {
        this.mouseController = mouseController;
    }

    public ActiveRegionOverlapController getInteractionController() {
        return interactionController;
    }

    public void setInteractionController(ActiveRegionOverlapController interactionController) {
        this.interactionController = interactionController;
    }

    public ActiveRegionMotionController getMotionController() {
        return motionController;
    }

    public void setMotionController(ActiveRegionMotionController motionController) {
        this.motionController = motionController;
    }
}