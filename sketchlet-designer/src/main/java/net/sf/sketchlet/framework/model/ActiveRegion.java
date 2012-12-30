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
import net.sf.sketchlet.framework.model.events.keyboard.KeyboardEventsProcessor;
import net.sf.sketchlet.framework.model.events.mouse.MouseEventsProcessor;
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

    private ActiveRegions parent;

    private List<RegionOverlapEventMacro> regionOverlapEventMacros = new Vector<RegionOverlapEventMacro>();
    private List<WidgetEventMacro> widgetEventMacros = new Vector<WidgetEventMacro>();
    private MouseEventsProcessor mouseEventsProcessor = new MouseEventsProcessor();
    private KeyboardEventsProcessor keyboardEventsProcessor = new KeyboardEventsProcessor();
    // renderer
    private ActiveRegionRenderer renderer = new ActiveRegionRenderer(this);
    // handlers
    private ActiveRegionMouseController mouseController = new ActiveRegionMouseController(this);
    private ActiveRegionOverlapController interactionController = new ActiveRegionOverlapController(this);
    private ActiveRegionMotionController motionController = new ActiveRegionMotionController(this);
    private Vector<String> updatingProperties = new Vector<String>();


    private boolean visible = true;

    private int x1Value;
    private int y1Value;
    private int x2Value;
    private int y2Value;
    private double centerOfRotationX = 0.5;
    private double centerOfRotationY = 0.5;
    private double trajectory2X = 0.25;
    private double trajectory2Y = 0.5;
    private double p_x0 = 0.0;
    private double p_y0 = 0.0;
    private double p_x1 = 1.0;
    private double p_y1 = 0.0;
    private double p_x2 = 1.0;
    private double p_y2 = 1.0;
    private double p_x3 = 0.0;
    private double p_y3 = 1.0;

    private int penX;
    private int penY;
    private double rotationValue;
    private double shearXValue;
    private double shearYValue;
    private int speedPrevX1;
    private int speedPrevY1;
    private int speedPrevX2;
    private int speedPrevY2;
    private int speedWidth;
    private int speedHeight;
    private double speedX;
    private double speedY;
    private double speedPrevDirection;
    private double speedValue;

    private String x = "";
    private String y = "";
    private String relativeX = "";
    private String relativeY = "";
    private String zoom = "";
    private String trajectoryPosition = "";
    private String width = "";
    private String height = "";
    private String rotation = "";
    private String shearX = "";
    private String shearY = "";
    private String windowX = "";
    private String windowY = "";
    private String windowWidth = "";
    private String windowHeight = "";
    private String transparency = "";
    private String speed = "";
    private String speedDirection = "";
    private String rotationSpeed = "";
    private String penWidth = "";
    private String x1 = "";
    private String y1 = "";
    private String x2 = "";
    private String y2 = "";
    private String perspectiveX1 = "";
    private String perspectiveY1 = "";
    private String perspectiveX2 = "";
    private String perspectiveY2 = "";
    private String perspectiveX3 = "";
    private String perspectiveY3 = "";
    private String perspectiveX4 = "";
    private String perspectiveY4 = "";
    private String automaticPerspective = "";
    private String rotation3DHorizontal = "";
    private String rotation3DVertical = "";
    private String perspectiveDepth = "";
    private int layer = 0;
    private Object[][] motionAndRotationVariablesMapping = {
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
    private Object[][] motionAndRotationLimits = {
            {"position x", "", ""},
            {"position y", "", ""},
            {"rotation", "", ""}
    };

    private String shape = "None";
    private String shapeArguments = "";
    private String fontName = "";
    private String fontSize = "";
    private String fontColor = "";
    private String fontStyle = "";
    private String imageFile = "";
    private boolean drawnImageChanged = false;
    private List<String> additionalImageFileNames = new Vector<String>();
    private boolean inTrajectoryMode = false;
    private boolean inTrajectoryMode2 = false;
    private int trajectoryType = 0;
    private String horizontalAlignment = "";
    private String verticalAlignment = "";
    private String lineColor = "";
    private String lineThickness = "";
    private String lineStyle = "";
    private String fillColor = "";
    private String captureScreenX = "";
    private String captureScreenY = "";
    private String captureScreenWidth = "";
    private String captureScreenHeight = "";
    private String textField = "";
    private String embeddedSketch = "";
    private String embeddedSketchVarPrefix = "";
    private String embeddedSketchVarPostfix = "";
    private String imageUrlField = "";
    private String active = "";
    private String type = "";
    private String widget = "";
    private String widgetPropertiesString = "";
    private Properties widgetProperties = null;
    private String imageIndex = "";
    private String animationFrameRateMs = "";
    private String regionGrouping = "";
    // images
    private BufferedImage image = null;
    private BufferedImage drawnImage = null;
    private List<BufferedImage> additionalDrawnImages = new Vector<BufferedImage>();
    private List<Boolean> additionalDrawnImagesChanged = new Vector<Boolean>();
    private String text = "";
    private String trajectory1 = "";
    private String trajectory2 = "";
    private String name = "";
    private boolean movable = false;
    private boolean rotatable = false;
    private boolean resizable = false;
    private boolean fitToBoxEnabled = true;
    private boolean textWrapped = false;
    private boolean textTrimmed = false;
    private boolean walkThroughEnabled = false;
    private boolean stickToTrajectoryEnabled = true;
    private boolean changingOrientationOnTrajectoryEnabled = true;
    private boolean screenCapturingEnabled = false;
    private boolean screenCapturingMouseMappingEnabled = false;
    private String widgetItems = "";
    private String charactersPerLine = "";
    private String maxNumLines = "";

    // auxiliary fields
    private boolean pinned = false;
    private boolean inFocus = false;
    private String previousImage = "";
    private long lastFrameTime = 0;

    public ActiveRegion() {
    }

    public ActiveRegion(ActiveRegions parent) {
        this.parent = parent;
        this.getDrawnImagePath();
    }

    public ActiveRegion(ActiveRegion a, boolean bCopyFiles) {
        this(a, a.parent, bCopyFiles);
    }

    public ActiveRegion(ActiveRegions parent, int x1Value, int y1Value, int x2Value, int y2Value) {
        this(parent);
        this.x1Value = x1Value;
        this.y1Value = y1Value;
        this.x2Value = x2Value;
        this.y2Value = y2Value;
    }

    public ActiveRegion(ActiveRegion region, ActiveRegions actions, boolean bCopyFiles) {
        setPropertiesFromRegion(region);
        this.parent = actions;

        this.activate(false);
        if (bCopyFiles) {
            additionalImageFileNames.clear();
            additionalDrawnImages.clear();
            additionalDrawnImagesChanged.clear();
            for (int aai = 0; aai <= region.additionalImageFileNames.size(); aai++) {
                if (aai > 0) {
                    additionalImageFileNames.add(null);
                    additionalDrawnImages.add(null);
                    additionalDrawnImagesChanged.add(new Boolean(false));
                } else {
                    imageFile = "";
                    this.drawnImage = null;
                    this.drawnImageChanged = true;
                }
                File imgFile;
                if (region.parent.getPage().getSourceDirectory() != null) {
                    imgFile = new File(region.getDrawnImageFile(region.parent.getPage().getSourceDirectory(), aai));
                } else {
                    imgFile = new File(region.getDrawnImagePath(aai));
                }

                try {
                    String newImageFileName = this.getDrawnImagePath(aai);
                    if (imgFile.exists()) {
                        File newFile = new File(newImageFileName);
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
            this.imageFile = region.imageFile;
            this.drawnImage = region.drawnImage;
            this.drawnImageChanged = true;
            additionalImageFileNames.clear();
            for (String additionalImageFileName : region.additionalImageFileNames) {
                additionalImageFileNames.add(additionalImageFileName);
            }
            additionalDrawnImages.clear();
            for (BufferedImage additionalImage : region.additionalDrawnImages) {
                additionalDrawnImages.add(additionalImage);
            }
            additionalDrawnImagesChanged.clear();
            for (Boolean imageChanged : region.additionalDrawnImagesChanged) {
                additionalDrawnImagesChanged.add(imageChanged);
            }

            this.type = region.type;
            this.imageIndex = region.imageIndex;
            this.animationFrameRateMs = region.animationFrameRateMs;
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

    public static String[][] getShowProperties() {
        return showProperties;
    }

    public static void setShowProperties(String[][] showProperties) {
        ActiveRegion.showProperties = showProperties;
    }

    public static String[][] getPropertiesInfo() {
        return propertiesInfo;
    }

    public static void setPropertiesInfo(String[][] propertiesInfo) {
        ActiveRegion.propertiesInfo = propertiesInfo;
    }

    public static String[][] getShowImageProperties() {
        return showImageProperties;
    }

    public static void setShowImageProperties(String[][] showImageProperties) {
        ActiveRegion.showImageProperties = showImageProperties;
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

    public boolean hasMouseDiscreteEvents() {
        return mouseEventsProcessor.getMouseActionsCount() > 0 || !this.widget.isEmpty();
    }

    public boolean isMouseDraggable() {
        return movable || rotatable;
    }

    public boolean isMouseActive() {
        return hasMouseDiscreteEvents() || movable || rotatable || !this.widget.isEmpty();
    }

    public void flush() {
        if (image != null) {
            image.flush();
            image = null;
        }
        if (this.drawnImage != null) {
            this.drawnImage.flush();
            drawnImage = null;
        }
        if (this.additionalDrawnImages != null) {
            for (int i = 0; i < this.additionalDrawnImages.size(); i++) {
                BufferedImage img = this.additionalDrawnImages.get(i);
                if (img != null) {
                    img.flush();
                }
                this.additionalDrawnImages.set(i, null);
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

    public Font getFontValue(float defaultFontSize) {
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

    public Color getColor(String colorName) {
        Color color = Colors.getColor(processText(colorName));
        if (color == null) {
            return Color.BLACK;
        }

        return color;
    }

    public Color getFontColorValue() {
        return getColor(fontColor);
    }

    public Color getBackgroundColorValue() {
        Color color = Colors.getColor(processText(fillColor));
        if (color == null) {
            return new Color(0, 0, 0, 0);
        }

        return color;
    }

    public Stroke getStrokeValue() {
        String lineThicknessExpression = this.processText(this.lineThickness);
        String lineStyleExpression = this.processText(this.lineStyle);

        int lineThickness = 2;
        try {
            lineThickness = Integer.parseInt(lineThicknessExpression);
        } catch (Exception e) {
        }

        return ColorToolbar.getStroke(lineStyleExpression, lineThickness);
    }

    public Color getLineColorValue() {
        return getColor(lineColor);
    }

    public int getLineThicknessValue() {
        int lineThickness = 2;
        String lineThicknessExpression = processText(this.lineThickness);
        try {
            lineThickness = Integer.parseInt(lineThicknessExpression);
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
        File file = new File(getDrawnImagePath());
        if (file.exists()) {
            files.add(file);
        }
        if (this.additionalImageFileNames != null) {
            for (int i = 0; i < this.additionalImageFileNames.size(); i++) {
                file = new File(this.getDrawnImagePath(i + 1));
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

        mouseEventsProcessor.dispose();
        for (EventMacro eventMacro : regionOverlapEventMacros) {
            eventMacro.dispose();
        }
        this.regionOverlapEventMacros.clear();

        x = null;
        y = null;
        relativeX = null;
        zoom = null;
        relativeY = null;
        trajectoryPosition = null;
        width = null;
        height = null;
        rotation = null;
        shearX = null;
        shearY = null;
        windowX = null;
        windowY = null;
        windowWidth = null;
        windowHeight = null;
        transparency = null;
        speed = null;
        speedDirection = null;
        rotationSpeed = null;
        penWidth = null;
        x1 = null;
        y1 = null;
        x2 = null;
        y2 = null;
        perspectiveX1 = null;
        perspectiveY1 = null;
        perspectiveX2 = null;
        perspectiveY2 = null;
        perspectiveX3 = null;
        perspectiveY3 = null;
        perspectiveX4 = null;
        perspectiveY4 = null;
        automaticPerspective = null;
        rotation3DHorizontal = null;
        rotation3DVertical = null;
        perspectiveDepth = null;
        motionAndRotationVariablesMapping = null;
        motionAndRotationLimits = null;
        shape = null;
        fontName = null;
        fontSize = null;
        fontColor = null;
        fontStyle = null;
        imageFile = null;
        parent = null;
        image = null;
        drawnImage = null;
        additionalImageFileNames = null;
        additionalDrawnImages = null;
        additionalDrawnImagesChanged = null;
        setRegionGrouping(null);
        setRenderer(null);
        setMouseController(null);
        setInteractionController(null);
        setMotionController(null);
        horizontalAlignment = null;
        verticalAlignment = null;
        lineColor = null;
        lineThickness = null;
        lineStyle = null;
        fillColor = null;
        captureScreenX = null;
        captureScreenY = null;
        captureScreenWidth = null;
        captureScreenHeight = null;
        textField = null;
        embeddedSketch = null;
        embeddedSketchVarPrefix = null;
        embeddedSketchVarPostfix = null;
        imageUrlField = null;
        imageIndex = null;
        animationFrameRateMs = null;
    }

    public void closeAllImages() {
        if (this.image != null) {
            this.image.flush();
            this.image = null;
        }
        if (this.drawnImage != null) {
            this.drawnImage.flush();
            this.drawnImage = null;
        }
        if (this.additionalDrawnImages != null) {
            for (BufferedImage img : this.additionalDrawnImages) {
                if (img != null) {
                    img.flush();
                }
            }
            this.additionalDrawnImages.clear();
            this.additionalDrawnImages = null;
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
        this.x1Value = a.x1Value;
        this.y1Value = a.y1Value;
        this.x2Value = a.x2Value;
        this.y2Value = a.y2Value;
        this.centerOfRotationX = a.centerOfRotationX;
        this.centerOfRotationY = a.centerOfRotationY;
        this.trajectory2X = a.trajectory2X;
        this.trajectory2Y = a.trajectory2Y;
        this.p_x0 = a.p_x0;
        this.p_y0 = a.p_y0;
        this.p_x1 = a.p_x1;
        this.p_y1 = a.p_y1;
        this.p_x2 = a.p_x2;
        this.p_y2 = a.p_y2;
        this.p_x3 = a.p_x3;
        this.p_y3 = a.p_y3;
        this.rotationValue = a.rotationValue;
        this.shearXValue = a.shearXValue;
        this.shearYValue = a.shearYValue;
        this.name = a.name;

        this.shape = a.shape;
        this.shapeArguments = a.shapeArguments;
        this.setRegionGrouping(a.getRegionGrouping());

        this.x = a.x;
        this.y = a.y;
        this.x1 = a.x1;
        this.y1 = a.y1;
        this.x2 = a.x2;
        this.y2 = a.y2;
        this.perspectiveX1 = a.perspectiveX1;
        this.perspectiveY1 = a.perspectiveY1;
        this.perspectiveX2 = a.perspectiveX2;
        this.perspectiveY2 = a.perspectiveY2;
        this.perspectiveX3 = a.perspectiveX3;
        this.perspectiveY3 = a.perspectiveY3;
        this.perspectiveX4 = a.perspectiveX4;
        this.perspectiveY4 = a.perspectiveY4;
        this.automaticPerspective = a.automaticPerspective;
        this.perspectiveDepth = a.perspectiveDepth;
        this.rotation3DHorizontal = a.rotation3DHorizontal;
        this.rotation3DVertical = a.rotation3DVertical;
        this.relativeX = a.relativeX;
        this.relativeY = a.relativeY;
        this.zoom = a.zoom;
        this.trajectoryPosition = a.trajectoryPosition;
        this.width = a.width;
        this.height = a.height;
        this.rotation = a.rotation;
        this.shearX = a.shearX;
        this.shearY = a.shearY;
        this.windowX = a.windowX;
        this.windowY = a.windowY;
        this.windowWidth = a.windowWidth;
        this.windowHeight = a.windowHeight;
        this.transparency = a.transparency;
        this.speed = a.speed;
        this.speedDirection = a.speedDirection;
        this.rotationSpeed = a.rotationSpeed;
        this.penWidth = a.penWidth;

        for (int i = 0; i < a.motionAndRotationVariablesMapping.length; i++) {
            for (int j = 0; j < a.motionAndRotationVariablesMapping[i].length; j++) {
                this.motionAndRotationVariablesMapping[i][j] = (String) a.motionAndRotationVariablesMapping[i][j];
            }
        }

        for (int i = 0; i < a.motionAndRotationLimits.length; i++) {
            for (int j = 0; j < a.motionAndRotationLimits[i].length; j++) {
                this.motionAndRotationLimits[i][j] = (String) a.motionAndRotationLimits[i][j];
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
        this.imageIndex = a.imageIndex;
        this.active = a.active;
        this.type = a.type;
        this.widget = a.widget;
        this.widgetProperties = null;
        this.widgetPropertiesString = a.widgetPropertiesString;
        this.animationFrameRateMs = a.animationFrameRateMs;

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
        this.fillColor = a.fillColor;

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

    public int getX1Value() {
        return (parent != null ? parent.getOffsetX() : 0) + x1Value;
    }

    public int getY1Value() {
        return (parent != null ? parent.getOffsetY() : 0) + y1Value;
    }

    public int getX2Value() {
        return (parent != null ? parent.getOffsetX() : 0) + x2Value;
    }

    public int getY2Value() {
        return (parent != null ? parent.getOffsetY() : 0) + y2Value;
    }

    public void play() {
    }

    public int getWidthValue() {
        return Math.abs(x2Value - x1Value);
    }

    public int getHeightValue() {
        return Math.abs(y2Value - y1Value);
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

    private boolean bActive = false;
    private boolean adjusting = false;

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

    private String getDrawnImagePath() {
        if (imageFile == null || imageFile.isEmpty()) {
            imageFile = "region_" + System.currentTimeMillis() + ".png";
        }
        return SketchletContextUtils.getCurrentProjectSkecthletsDir() + imageFile;
    }

    public String getDrawnImageFileName() {
        if (imageFile == null || imageFile.trim().equals("")) {
            imageFile = "region_" + System.currentTimeMillis() + ".png";
        }
        return imageFile;
    }

    public String getDrawnImagePath(int index) {
        if (index == 0) {
            return getDrawnImagePath();
        } else {
            try {
                String additionalImageFileName = this.additionalImageFileNames.get(index - 1);

                if (additionalImageFileName == null || additionalImageFileName.isEmpty()) {
                    do {
                        additionalImageFileName = "region_" + System.currentTimeMillis() + ".png";
                    } while (imageFile.equalsIgnoreCase(additionalImageFileName) || additionalImageFileNames.contains(additionalImageFileName));
                    this.additionalImageFileNames.set(index - 1, additionalImageFileName);
                }
                return SketchletContextUtils.getCurrentProjectSkecthletsDir() + additionalImageFileName;
            } catch (Throwable e) {
                log.error("getDrawnImagePath()", e);
            }
        }
        return "";
    }

    public String getId() {
        return getDrawnImageFileName(0);
    }

    public String getDrawnImageFileName(int index) {
        if (index == 0) {
            return getDrawnImageFileName();
        } else {
            try {
                String additionalImageFileName = this.additionalImageFileNames.get(index - 1);

                if (additionalImageFileName == null || additionalImageFileName.trim().equals("")) {
                    additionalImageFileName = "region_" + System.currentTimeMillis() + ".png";
                    this.additionalImageFileNames.set(index - 1, additionalImageFileName);
                }
                return additionalImageFileName;
            } catch (Throwable e) {
                log.error("getDrawnImageFileName()", e);
            }
        }
        return "";
    }

    public void deleteDrawnImage(int index) {
        if (additionalDrawnImages.size() == 0) {
            return;
        }

        try {
            new File(getDrawnImagePath(index)).delete();
            if (index == 0) {
                this.drawnImage = this.additionalDrawnImages.get(0);
                this.drawnImageChanged = true;

                this.additionalDrawnImages.remove(0);
                this.additionalDrawnImagesChanged.remove(0);
                this.additionalImageFileNames.remove(0);
            } else {
                this.additionalDrawnImages.remove(index - 1);
                this.additionalDrawnImagesChanged.remove(index - 1);
                this.additionalImageFileNames.remove(index - 1);
            }
        } catch (Throwable e) {
            log.error("deleteDrawnImage()", e);
        }
    }

    public String getNumber() {
        return "" + (parent.getRegions().size() - parent.getRegions().indexOf(this));
    }

    private String getDrawnImageFile(String directoryPath) {
        if (imageFile == null || imageFile.trim().equals("")) {
            imageFile = "region_" + System.currentTimeMillis() + ".png";
        }
        return directoryPath + imageFile;
    }

    public int getImageCount() {
        return this.additionalDrawnImages.size() + 1;
    }

    public BufferedImage getDrawnImage(int index) {
        if (index == 0) {
            return this.drawnImage;
        } else {
            if (index < 0 || index > additionalDrawnImages.size()) {
                return null;
            } else {
                return this.additionalDrawnImages.get(index - 1);
            }
        }
    }

    public void setDrawnImage(int index, BufferedImage image) {
        if (index == 0) {
            if (this.drawnImage != null) {
                this.drawnImage.flush();
            }
            this.drawnImage = image;
        } else {
            if (index < 0 || index > additionalDrawnImages.size()) {
                return;
            } else {
                if (this.additionalDrawnImages.get(index - 1) != null) {
                    this.additionalDrawnImages.get(index - 1).flush();
                }
                this.additionalDrawnImages.set(index - 1, image);
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

    public void setDrawnImageChanged(int index, boolean bChanged) {
        if (index == 0) {
            this.drawnImageChanged = bChanged;
        } else {
            if (index < 0 || index > additionalDrawnImages.size()) {
                return;
            } else {
                this.additionalDrawnImagesChanged.set(index - 1, new Boolean(bChanged));
            }
        }
    }

    public boolean isDrawnImageChanged(int index) {
        if (index == 0) {
            return this.drawnImageChanged;
        } else {
            if (index < 0 || index > additionalDrawnImages.size()) {
                return false;
            } else {
                return this.additionalDrawnImagesChanged.get(index - 1).booleanValue();
            }
        }
    }

    public String getDrawnImageFile(String directoryPath, int index) {
        if (index == 0) {
            return getDrawnImageFile(directoryPath);
        } else {
            if (index < 0 || index > additionalDrawnImages.size()) {
                return null;
            } else {
                try {
                    String additionalImageFileName = this.additionalImageFileNames.get(index - 1);

                    if (additionalImageFileName == null || additionalImageFileName.trim().equals("")) {
                        additionalImageFileName = "region_" + System.currentTimeMillis() + ".png";
                    }
                    return directoryPath + additionalImageFileName;
                } catch (Throwable e) {
                    log.error("getDrawnImageFile()", e);
                }
            }
        }
        return "";
    }

    private void setImageFile(String fileName) {
        this.imageFile = fileName;
    }

    public void setImageFile(String fileName, int index) {
        if (index == 0) {
            setImageFile(fileName);
        } else if (index > 0 && this.additionalImageFileNames.size() > index - 1) {
            this.additionalImageFileNames.set(index - 1, fileName);
        }
    }

    public void animate() {
    }

    public String processText(String text) {
        text = Evaluator.processRegionReferences(this, text);
        return Evaluator.processText(text, this.getVarPrefix(), this.getVarPostfix());
    }

    public boolean isAffected(String variableName) {
        return true;
    }

    public void saveImage() {
        try {
            if (this.drawnImage != null && drawnImageChanged) {
                ImageCache.write(this.drawnImage, new File(this.getDrawnImagePath()));
                drawnImageChanged = false;
            }
            for (int i = 0; i < this.additionalDrawnImages.size(); i++) {
                int index = i + 1;
                if (this.getDrawnImage(index) != null && this.isDrawnImageChanged(index)) {
                    ImageCache.write(this.getDrawnImage(index), new File(this.getDrawnImagePath(index)));
                    this.setDrawnImageChanged(index, false);
                }
            }

        } catch (Throwable e) {
            log.error(e);
        }
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
        String textFieldValue = XMLUtils.prepareForXML(this.textField);
        String textAreaValue = this.text;
        String trajectory = XMLUtils.prepareForXML(this.trajectory1);
        String trajectory2 = XMLUtils.prepareForXML(this.trajectory2);

        String widgetItems = this.widgetItems;

        String strEmbedSketch = XMLUtils.prepareForXML(this.embeddedSketch);
        String strEmbedPrefix = XMLUtils.prepareForXML(this.embeddedSketchVarPrefix);
        String strEmbedPostfix = XMLUtils.prepareForXML(this.embeddedSketchVarPostfix);
        String strCaptureScreenX = XMLUtils.prepareForXML(this.captureScreenX);
        String strCaptureScreenY = XMLUtils.prepareForXML(this.captureScreenY);
        String strCaptureScreenWidth = XMLUtils.prepareForXML(this.captureScreenWidth);
        String strCaptureScreenHeight = XMLUtils.prepareForXML(this.captureScreenHeight);

        String strImage = this.imageUrlField;
        strImage = XMLUtils.prepareForXML(strImage);
        String strIndex = this.imageIndex;
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
            lastModified = new File(getDrawnImagePath()).lastModified();
        } catch (Throwable e) {
        }

        out.println("<active-region>");
        out.println("    <region name='" + name + "' x1='" + x1Value + "' y1='" + y1Value + "' x2='" + x2Value + "' y2='" + y2Value + "' shearX='" + shearXValue + "' shearY='" + shearYValue + "' rotation='" + rotationValue + "'/>");
        out.println("    <perspective p_x0='" + p_x0 + "' p_y0='" + p_y0 + "' p_x1='" + p_x1 + "' p_y1='" + p_y1 + "' p_x2='" + p_x2 + "' p_y2='" + p_y2 + "' p_x3='" + p_x3 + "' p_y3='" + p_y3 + "'/>");
        out.println("    <rotation_center x='" + centerOfRotationX + "' y='" + centerOfRotationY + "'/>");
        out.println("    <trajectory_point x='" + trajectory2X + "' y='" + trajectory2Y + "'/>");
        out.println("    <layer>" + this.layer + "</layer>");
        out.println("    <visible>" + this.visible + "</visible>");
        if (!this.shape.isEmpty()) {
            out.println("    <basic-shape>" + XMLUtils.prepareForXML(shape) + "</basic-shape>");
        }
        if (!this.shapeArguments.isEmpty()) {
            out.println("    <basic-shape-args>" + XMLUtils.prepareForXML(shapeArguments) + "</basic-shape-args>");
        }
        if (!this.getRegionGrouping().isEmpty()) {
            out.println("    <group>" + this.getRegionGrouping() + "</group>");
        }
        if (!textFieldValue.isEmpty()) {
            out.println("    <show-text>" + textFieldValue + "</show-text>");
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
        if (!textAreaValue.isEmpty()) {
            out.println("    <text-area><![CDATA[" + textAreaValue + "]]></text-area>");
        }
        if (!trajectory.isEmpty()) {
            out.println("    <trajectory>" + trajectory + "</trajectory>");
        }
        if (!widgetItems.isEmpty()) {
            out.println("    <control-items><![CDATA[" + widgetItems + "]]></control-items>");
        }

        if (!trajectory2.isEmpty()) {
            out.println("    <trajectory2>" + trajectory2 + "</trajectory2>");
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
        if (!animationFrameRateMs.isEmpty()) {
            out.println("    <image-animation-ms>" + animationFrameRateMs + "</image-animation-ms>");
        }
        if (!annonimous) {
            if (!imageFile.isEmpty()) {
                out.println("    <image-draw>" + imageFile + "</image-draw>");
            }
            for (String additionalImageFileName : this.additionalImageFileNames) {
                out.println("    <additional-image-draw>" + additionalImageFileName + "</additional-image-draw>");
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
        if (!x.isEmpty()) {
            out.println("        <x>" + XMLUtils.prepareForXML(x) + "</x>");
        }
        if (!y.isEmpty()) {
            out.println("        <y>" + XMLUtils.prepareForXML(y) + "</y>");
        }
        if (!x1.isEmpty()) {
            out.println("        <direct-x1>" + XMLUtils.prepareForXML(x1) + "</direct-x1>");
        }
        if (!y1.isEmpty()) {
            out.println("        <direct-y1>" + XMLUtils.prepareForXML(y1) + "</direct-y1>");
        }
        if (!x2.isEmpty()) {
            out.println("        <direct-x2>" + XMLUtils.prepareForXML(x2) + "</direct-x2>");
        }
        if (!y2.isEmpty()) {
            out.println("        <direct-y2>" + XMLUtils.prepareForXML(y2) + "</direct-y2>");
        }
        if (!perspectiveX1.isEmpty()) {
            out.println("        <perspective-x1>" + XMLUtils.prepareForXML(perspectiveX1) + "</perspective-x1>");
        }
        if (!perspectiveY1.isEmpty()) {
            out.println("        <perspective-y1>" + XMLUtils.prepareForXML(perspectiveY1) + "</perspective-y1>");
        }
        if (!perspectiveX2.isEmpty()) {
            out.println("        <perspective-x2>" + XMLUtils.prepareForXML(perspectiveX2) + "</perspective-x2>");
        }
        if (!perspectiveY2.isEmpty()) {
            out.println("        <perspective-y2>" + XMLUtils.prepareForXML(perspectiveY2) + "</perspective-y2>");
        }
        if (!perspectiveX3.isEmpty()) {
            out.println("        <perspective-x3>" + XMLUtils.prepareForXML(perspectiveX3) + "</perspective-x3>");
        }
        if (!perspectiveY3.isEmpty()) {
            out.println("        <perspective-y3>" + XMLUtils.prepareForXML(perspectiveY3) + "</perspective-y3>");
        }
        if (!perspectiveX4.isEmpty()) {
            out.println("        <perspective-x4>" + XMLUtils.prepareForXML(perspectiveX4) + "</perspective-x4>");
        }
        if (!perspectiveY4.isEmpty()) {
            out.println("        <perspective-y4>" + XMLUtils.prepareForXML(perspectiveY4) + "</perspective-y4>");
        }
        if (!automaticPerspective.isEmpty()) {
            out.println("        <automatic-perspective>" + XMLUtils.prepareForXML(automaticPerspective) + "</automatic-perspective>");
        }
        if (!perspectiveDepth.isEmpty()) {
            out.println("        <perspective-depth>" + XMLUtils.prepareForXML(perspectiveDepth) + "</perspective-depth>");
        }
        if (!rotation3DHorizontal.isEmpty()) {
            out.println("        <rotate-3d-horizontal>" + XMLUtils.prepareForXML(rotation3DHorizontal) + "</rotate-3d-horizontal>");
        }
        if (!rotation3DVertical.isEmpty()) {
            out.println("        <rotate-3d-vertical>" + XMLUtils.prepareForXML(rotation3DVertical) + "</rotate-3d-vertical>");
        }
        if (!width.isEmpty()) {
            out.println("        <width>" + XMLUtils.prepareForXML(width) + "</width>");
        }
        if (!height.isEmpty()) {
            out.println("        <height>" + XMLUtils.prepareForXML(height) + "</height>");
        }
        if (!rotation.isEmpty()) {
            out.println("        <rotation>" + XMLUtils.prepareForXML(rotation) + "</rotation>");
        }
        if (!shearX.isEmpty()) {
            out.println("        <shearX>" + XMLUtils.prepareForXML(shearX) + "</shearX>");
        }
        if (!shearY.isEmpty()) {
            out.println("        <shearY>" + XMLUtils.prepareForXML(shearY) + "</shearY>");
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
        if (!speed.isEmpty()) {
            out.println("        <speed>" + XMLUtils.prepareForXML(speed) + "</speed>");
        }
        if (!speedDirection.isEmpty()) {
            out.println("        <motionDirection>" + XMLUtils.prepareForXML(speedDirection) + "</motionDirection>");
        }
        if (!rotationSpeed.isEmpty()) {
            out.println("        <rotationSpeed>" + XMLUtils.prepareForXML(rotationSpeed) + "</rotationSpeed>");
        }
        if (!penWidth.isEmpty()) {
            out.println("        <pen>" + XMLUtils.prepareForXML(penWidth) + "</pen>");
        }
        if (!trajectoryPosition.isEmpty()) {
            out.println("        <trajectory-position>" + XMLUtils.prepareForXML(trajectoryPosition) + "</trajectory-position>");
        }
        if (!relativeX.isEmpty()) {
            out.println("        <relative-x>" + XMLUtils.prepareForXML(relativeX) + "</relative-x>");
        }
        if (!zoom.isEmpty()) {
            out.println("        <region-zoom>" + XMLUtils.prepareForXML(zoom) + "</region-zoom>");
        }
        if (!relativeY.isEmpty()) {
            out.println("        <relative-y>" + XMLUtils.prepareForXML(relativeY) + "</relative-y>");
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
        if (!fillColor.isEmpty()) {
            out.println("       <fill-color>" + XMLUtils.prepareForXML(this.fillColor) + "</fill-color>");
        }
        out.println("    </appearance>");
        out.println("    <movable>");

        for (int i = 0; i < motionAndRotationLimits.length; i++) {
            out.println("        <limit-motion " + "dimension='" + motionAndRotationLimits[i][0] + "' " + "min='" + motionAndRotationLimits[i][1] + "' " + "max='" + motionAndRotationLimits[i][2] + "' " + "/>");
        }

        out.println("        <move>" + strCanMove + "</move>");
        out.println("        <rotate>" + strCanRotate + "</rotate>");
        out.println("        <resize>" + strCanResize + "</resize>");
        out.println("        <update>");
        for (int i = 0; i < this.motionAndRotationVariablesMapping.length; i++) {
            boolean bSave = false;
            for (int j = 1; j < motionAndRotationVariablesMapping[i].length; j++) {
                if (!motionAndRotationVariablesMapping[i][j].equals("")) {
                    bSave = true;
                    break;
                }
            }
            if (!bSave) {
                continue;
            }

            out.print("        <dimension ");
            out.print(" name='" + motionAndRotationVariablesMapping[i][0] + "'");
            out.print(" variable='" + XMLUtils.prepareForXML((String) motionAndRotationVariablesMapping[i][1]) + "'");
            out.print(" start='" + motionAndRotationVariablesMapping[i][2] + "'");
            out.print(" end='" + motionAndRotationVariablesMapping[i][3] + "'");
            out.print(" format='" + motionAndRotationVariablesMapping[i][4] + "'");
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
        for (EventMacro mouseEventMacro : mouseEventsProcessor.getMouseEventMacros()) {
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
        for (EventMacro regionKeyboardEventMacro : keyboardEventsProcessor.getKeyboardEventMacros()) {
            regionKeyboardEventMacro.getMacro().setName(regionKeyboardEventMacro.getEventName());
            regionKeyboardEventMacro.getMacro().saveSimple(out, "region-keyboard-event-action", "        ");
        }
        out.println("    </region-keyboard-event-actions>");

        out.println("</active-region>");

    }

    public void initImages() {
        if (!getDrawnImageFileName(0).equals("") && getDrawnImage(0) == null) {
            initImage(0);
        }
        for (int i = 0; i < additionalDrawnImages.size(); i++) {
            String strAdditionalImage = additionalImageFileNames.get(i);
            BufferedImage additionalImage = additionalDrawnImages.get(i);

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
            File file = new File(getDrawnImagePath());
            drawnImage = ImageCache.read(file, drawnImage, bForceFileRead);
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
                File file = new File(getDrawnImagePath(index));
                BufferedImage image = ImageCache.read(file, this.additionalDrawnImages.get(index - 1), bForceFileRead);
                this.additionalDrawnImages.set(index - 1, image);
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
            img = this.getDrawnImage(index);
            if (img != null) {
                w = img.getWidth();
                h = img.getHeight();
            }
        } catch (Exception e) {
        }

        img = Workspace.createCompatibleImage(w, h, img);

        this.setDrawnImage(index, img);
        this.setDrawnImageChanged(index, true);
    }

    public void clearImage11(int index) {
        BufferedImage img = this.getDrawnImage(index);
        img = (BufferedImage) Workspace.createCompatibleImage(1, 1, img);

        try {
            File file = index == 0 ? new File(getDrawnImagePath()) : new File(getDrawnImagePath(index));
            ImageCache.write(img, file);
        } catch (Exception e) {
            log.error("clearImage11()", e);
        }

        this.setDrawnImage(index, img);
        this.setDrawnImageChanged(index, true);
    }

    public Shape getShape(boolean bPlayback) {
        return new Rectangle(x1Value, y1Value, getWidthValue(), getHeightValue());
    }

    public Rectangle getBounds(boolean bPlayback) {
        AffineTransform aft = new AffineTransform();
        aft.shear(shearXValue, shearYValue);
        aft.rotate(rotationValue, x1Value + (x2Value - x1Value) * centerOfRotationX, y1Value + (y2Value - y1Value) * centerOfRotationY);
        Area area = getArea(bPlayback);
        area.transform(aft);
        return area.getBounds();
    }

    public Point getInversePoint(boolean bPlayback, int x, int y) {
        AffineTransform aft = new AffineTransform();
        aft.shear(shearXValue, shearYValue);
        aft.rotate(rotationValue, x1Value + (x2Value - x1Value) * centerOfRotationX, y1Value + (y2Value - y1Value) * centerOfRotationY);

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
        aft.shear(shearXValue, shearYValue);
        aft.rotate(rotationValue, x1Value + (x2Value - x1Value) * centerOfRotationX, y1Value + (y2Value - y1Value) * centerOfRotationY);
        Area area = getArea(bPlayback);
        area.transform(aft);
        return area;
    }

    public Area getArea(boolean bPlayback) {
        String strShape = this.processText(this.shape);
        if (strShape.equalsIgnoreCase("Oval")) {
            return new Area(new Ellipse2D.Double(x1Value, y1Value, getWidthValue(), getHeightValue()));
        } else if (strShape.equalsIgnoreCase("Rounded Rectangle")) {
            try {
                double n = Double.parseDouble(this.processText(shapeArguments));
                return new Area(new RoundRectangle2D.Double(x1Value, y1Value, getWidthValue(), getHeightValue(), n, n));
            } catch (Exception e) {
            }
        } else if (strShape.equalsIgnoreCase("Triangle 1")) {
            Polygon p = new Polygon();
            p.addPoint(x1Value + (x2Value - x1Value) / 2, y1Value);
            p.addPoint(x1Value, y2Value);
            p.addPoint(x2Value, y2Value);
            Area area = new Area(p);
            return area;
        } else if (strShape.equalsIgnoreCase("Triangle 2")) {
            Polygon p = new Polygon();
            p.addPoint(x1Value, y1Value);
            p.addPoint(x1Value, y2Value);
            p.addPoint(x2Value, y2Value);
            Area area = new Area(p);
            return area;
        } else if (strShape.toLowerCase().startsWith("regularpolygon")) {
            try {
                String args[] = strShape.split(" ");
                if (args.length > 1) {
                    int n = (int) Double.parseDouble(args[1]);
                    int r = Math.min(this.getWidthValue(), this.getHeightValue()) / 2;
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
                    int r = Math.min(this.getWidthValue(), this.getHeightValue()) / 2;
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

                Area area = new Area(new Arc2D.Double(getX1Value(), getY1Value(), getWidthValue(), getHeightValue(), -startAngle + 90, -extent, Arc2D.PIE));

                if (internalR > 0 && internalR < 1) {
                    double _w = getWidthValue() * internalR;
                    double _h = getHeightValue() * internalR;
                    double _x1 = getX1Value() + (getWidthValue() - _w) / 2;
                    double _y1 = getY1Value() + (getHeightValue() - _h) / 2;
                    area.subtract(new Area(new Ellipse2D.Double((int) _x1, (int) _y1, (int) _w, (int) _h)));
                }
                return area;
            } catch (Throwable e) {
            }
        }
        return new Area(new Rectangle(x1Value, y1Value, getWidthValue(), getHeightValue()));
    }

    public Polygon scalePolygon(boolean bPlayback, Polygon p) {
        int w = this.getWidthValue();
        int h = this.getHeightValue();
        int x = this.x1Value;
        int y = this.y1Value;

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
        return new Dimension(getWidthValue(), getHeightValue());
    }

    public int getX(boolean bPlayback) {
        int x;
        if (this.horizontalAlignment.equalsIgnoreCase("center")) {
            x = x1Value + (x2Value - x1Value) / 2;
        } else if (this.horizontalAlignment.equalsIgnoreCase("right")) {
            x = x2Value;
        } else {
            x = x1Value;
        }
        return x;
    }

    public int getCenterX(boolean bPlayback) {
        int x;
        x = x1Value + (x2Value - x1Value) / 2;
        return x;
    }

    public void setX(int x, boolean bPlayback) {
        int w = x2Value - x1Value;
        if (this.horizontalAlignment.equalsIgnoreCase("center")) {
            x1Value = x - w / 2;
            x2Value = x + w / 2;
        } else if (this.horizontalAlignment.equalsIgnoreCase("right")) {
            x1Value = x - w;
            x2Value = x;
        } else {
            x1Value = x;
            x2Value = x + w;
        }
    }

    public int getY(boolean bPlayback) {
        int y;
        if (this.verticalAlignment.equalsIgnoreCase("center")) {
            y = y1Value + (y2Value - y1Value) / 2;
        } else if (this.verticalAlignment.equalsIgnoreCase("bottom")) {
            y = y2Value;
        } else {
            y = y1Value;
        }
        return y;
    }

    public int getCenterY(boolean bPlayback) {
        int y;
        y = y1Value + (y2Value - y1Value) / 2;
        return y;
    }

    public void setY(int y, boolean bPlayback) {
        int h = y2Value - y1Value;
        if (this.verticalAlignment.equalsIgnoreCase("center")) {
            y1Value = y - h / 2;
            y2Value = y + h / 2;
        } else if (this.verticalAlignment.equalsIgnoreCase("bottom")) {
            y1Value = y - h;
            y2Value = y;
        } else {
            y1Value = y;
            y2Value = y + h;
        }
    }

    public void processLimitsTrajectory(Point p) {
        getRenderer().getTrajectoryDrawingLayer().getClosestTrajectoryPoint(p);
        getMotionController().processLimits("trajectory position", getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint, 0.0, 1.0, 0.0, 0.0, true);
        getMotionController().processLimits("trajectory position 2", getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint2, 0.0, 1.0, 0.0, 0.0, true);
    }

    public void processLimitsX() {
        int w = this.x2Value - this.x1Value;
        if (this.horizontalAlignment.equalsIgnoreCase("center")) {
            this.getMotionController().processLimits("position x", this.x1Value + w / 2, w / 2, w / 2, true);
        } else if (this.horizontalAlignment.equalsIgnoreCase("right")) {
            this.getMotionController().processLimits("position x", this.x1Value + w, w, 0, true);
        } else {
            this.getMotionController().processLimits("position x", this.x1Value, 0, w, true);
        }
    }

    public void processLimitsY() {
        int h = this.y2Value - this.y1Value;
        if (this.verticalAlignment.equalsIgnoreCase("center")) {
            this.getMotionController().processLimits("position y", this.y1Value + h / 2, h / 2, h / 2, true);
        } else if (this.verticalAlignment.equalsIgnoreCase("right")) {
            this.getMotionController().processLimits("position y", this.y1Value + h, h, 0, true);
        } else {
            this.getMotionController().processLimits("position y", this.y1Value, 0, h, true);
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
        return new Rectangle((int) (x2Value - 30 / s), (int) (y2Value - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getMappingIconRectangle() {
        double s = Math.min(1, SketchletEditor.getInstance().getScale());
        return new Rectangle((int) (x2Value - 60 / s), (int) (y2Value - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getMouseIconRectangle() {
        double s = Math.min(1, SketchletEditor.getInstance().getScale());
        return new Rectangle((int) (x2Value - 90 / s), (int) (y2Value - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getKeyboardIconRectangle() {
        double s = Math.min(1, SketchletEditor.getInstance().getScale());
        return new Rectangle((int) (x2Value - 90 / s), (int) (y2Value - 30 / s), (int) (30 / s), (int) (30 / s));
    }

    public Rectangle getInRegionsIconRectangle() {
        double s = Math.min(1, SketchletEditor.getInstance().getScale());
        return new Rectangle((int) (x2Value - 120 / s), (int) (y2Value - 30 / s), (int) (30 / s), (int) (30 / s));
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
        int oldW = x2Value - x1Value;

        if (horizontalAlignment.equalsIgnoreCase("center")) {
            x1Value = x1Value + oldW / 2 - w / 2;
            x2Value = x1Value + w;
        } else if (horizontalAlignment.equalsIgnoreCase("right")) {
            x1Value = x1Value + oldW - w;
            x2Value = x1Value + w;
        } else {
            x2Value = x1Value + w;
        }
    }

    public void setHeight(int h) {
        int oldH = y2Value - y1Value;

        if (verticalAlignment.equalsIgnoreCase("center")) {
            y1Value = y1Value + oldH / 2 - h / 2;
            y2Value = y1Value + h;
        } else if (verticalAlignment.equalsIgnoreCase("right")) {
            y1Value = y1Value + oldH - h;
            y2Value = y1Value + h;
        } else {
            y2Value = y1Value + h;
        }
    }

    public void initPlayback() {
        penX = 0;
        penY = 0;
    }

    private static String[][] showProperties = {
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
    private static String[][] propertiesInfo = {
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
    private String[][] propertiesDefaultLimits = {
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
    private String[][] propertiesAnimation = {
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
    private static String[][] showImageProperties = {
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
            strValue = this.imageIndex;
        } else if (property.equalsIgnoreCase("animation ms")) {
            strValue = this.animationFrameRateMs;
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
            strValue = this.fillColor;
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
            strValue = this.x.isEmpty() ? this.getX(bPlayback) + "" : this.x;
        } else if (property.equalsIgnoreCase("position y")) {
            strValue = this.y.isEmpty() ? this.getY(bPlayback) + "" : this.y;
        } else if (property.equalsIgnoreCase("x1")) {
            strValue = this.x1.isEmpty() ? this.x1Value + "" : this.x1;
        } else if (property.equalsIgnoreCase("y1")) {
            strValue = this.y1.isEmpty() ? this.y1Value + "" : this.y1;
        } else if (property.equalsIgnoreCase("x2")) {
            strValue = this.x2.isEmpty() ? this.x2Value + "" : this.x2;
        } else if (property.equalsIgnoreCase("y2")) {
            strValue = this.y2.isEmpty() ? this.y2Value + "" : this.y2;
        } else if (property.equalsIgnoreCase("relative x")) {
            strValue = this.relativeX;
        } else if (property.equalsIgnoreCase("zoom")) {
            strValue = this.zoom;
        } else if (property.equalsIgnoreCase("relative y")) {
            strValue = this.relativeY;
        } else if (property.equalsIgnoreCase("trajectory position")) {
            strValue = this.trajectoryPosition;
        } else if (property.equalsIgnoreCase("trajectory position 2")) {
            strValue = this.getRenderer().getTrajectoryDrawingLayer().trajectoryPositionFromPoint2 + "";
        } else if (property.equalsIgnoreCase("width")) {
            strValue = this.width.isEmpty() ? "" + (this.getWidthValue()) : this.width;
        } else if (property.equalsIgnoreCase("height")) {
            strValue = this.height.isEmpty() ? "" + (this.getHeightValue()) : this.height;
        } else if (property.equalsIgnoreCase("rotation")) {
            strValue = this.rotation.isEmpty() ? Math.toDegrees(this.rotationValue) + "" : this.rotation;
        } else if (property.equalsIgnoreCase("shear x")) {
            strValue = this.shearX;
        } else if (property.equalsIgnoreCase("shear y")) {
            strValue = this.shearY;
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
            strValue = this.speed;
        } else if (property.equalsIgnoreCase("direction")) {
            strValue = this.speedDirection;
        } else if (property.equalsIgnoreCase("rotation speed")) {
            strValue = this.rotationSpeed;
        } else if (property.equalsIgnoreCase("pen thickness")) {
            strValue = this.penWidth;
        } else if (property.equalsIgnoreCase("perspective x1")) {
            strValue = this.perspectiveX1.isEmpty() ? this.p_x0 + "" : this.perspectiveX1;
        } else if (property.equalsIgnoreCase("perspective y1")) {
            strValue = this.perspectiveY1.isEmpty() ? this.p_y0 + "" : this.perspectiveY1;
        } else if (property.equalsIgnoreCase("perspective x2")) {
            strValue = this.perspectiveX2.isEmpty() ? this.p_x1 + "" : this.perspectiveX2;
        } else if (property.equalsIgnoreCase("perspective y2")) {
            strValue = this.perspectiveY2.isEmpty() ? this.p_y1 + "" : this.perspectiveY2;
        } else if (property.equalsIgnoreCase("perspective x3")) {
            strValue = this.perspectiveX3.isEmpty() ? this.p_x2 + "" : this.perspectiveX3;
        } else if (property.equalsIgnoreCase("perspective y3")) {
            strValue = this.perspectiveY3.isEmpty() ? this.p_y2 + "" : this.perspectiveY3;
        } else if (property.equalsIgnoreCase("perspective x4")) {
            strValue = this.perspectiveX4.isEmpty() ? this.p_x3 + "" : this.perspectiveX4;
        } else if (property.equalsIgnoreCase("perspective y4")) {
            strValue = this.perspectiveY4.isEmpty() ? this.p_y3 + "" : this.perspectiveY4;
        } else if (property.equalsIgnoreCase("automatic perspective")) {
            strValue = this.automaticPerspective;
        } else if (property.equalsIgnoreCase("perspective depth")) {
            strValue = this.perspectiveDepth;
        } else if (property.equalsIgnoreCase("horizontal 3d rotation")) {
            strValue = this.rotation3DHorizontal;
        } else if (property.equalsIgnoreCase("vertical 3d rotation")) {
            strValue = this.rotation3DVertical;
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
            this.imageIndex = value;
        } else if (property.equalsIgnoreCase("animation ms")) {
            this.animationFrameRateMs = value;
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
            this.fillColor = value;
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
                this.x = value;
            } else {
                try {
                    x = "";
                    int num = (int) Double.parseDouble(value);
                    this.setX(num, false);
                    this.setX(num, true);
                } catch (Exception e) {
                    x = value;
                }
            }
        } else if (property.equalsIgnoreCase("position y")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value) || value.isEmpty()) {
                this.y = value;
            } else {
                try {
                    y = "";
                    int num = (int) Double.parseDouble(value);
                    this.setY(num, false);
                    this.setY(num, true);
                } catch (Exception e) {
                    y = value;
                }
            }
        } else if (property.equalsIgnoreCase("x1")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value) || value.isEmpty()) {
                this.x1 = value;
            } else {
                try {
                    x1 = "";
                    int num = (int) Double.parseDouble(value);
                    this.x1Value = num;
                } catch (Exception e) {
                    x1 = "";
                }
            }
        } else if (property.equalsIgnoreCase("x2")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value) || value.isEmpty()) {
                this.x2 = value;
            } else {
                try {
                    x2 = "";
                    int num = (int) Double.parseDouble(value);
                    this.x2Value = num;
                } catch (Exception e) {
                    x2 = "";
                }
            }
        } else if (property.equalsIgnoreCase("y1")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value) || value.isEmpty()) {
                this.y1 = value;
            } else {
                try {
                    y1 = "";
                    int num = (int) Double.parseDouble(value);
                    this.y1Value = num;
                } catch (Exception e) {
                    y1 = "";
                }
            }
        } else if (property.equalsIgnoreCase("y2")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value) || value.isEmpty()) {
                this.y2 = value;
            } else {
                try {
                    y2 = "";
                    int num = (int) Double.parseDouble(value);
                    this.y2Value = num;
                } catch (Exception e) {
                    y2 = "";
                }
            }
        } else if (property.equalsIgnoreCase("relative x")) {
            this.relativeX = value;
        } else if (property.equalsIgnoreCase("zoom")) {
            this.zoom = value;
        } else if (property.equalsIgnoreCase("relative y")) {
            this.relativeY = value;
        } else if (property.equalsIgnoreCase("trajectory position")) {
            this.trajectoryPosition = value;
        } else if (property.equalsIgnoreCase("width")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value)) {
                this.width = value;
            } else {
                try {
                    this.width = "";
                    double num = Double.parseDouble(value);
                    this.setWidth((int) num);
                } catch (Exception e) {
                    width = value;
                }
            }
        } else if (property.equalsIgnoreCase("height")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value)) {
                this.height = value;
            } else {
                try {
                    this.height = "";
                    double num = Double.parseDouble(value);
                    this.setHeight((int) num);
                } catch (Exception e) {
                    height = value;
                }
            }
        } else if (property.equalsIgnoreCase("rotation")) {
            if (value.startsWith("=") || TemplateMarkers.containsStartMarker(value)) {
                this.rotation = value;
            } else if (value.isEmpty()) {
                this.rotation = "";
                this.rotationValue = 0.0;
            } else {
                try {
                    this.rotation = "";
                    double num = Double.parseDouble(value);
                    this.rotationValue = Math.toRadians(num);
                } catch (Exception e) {
                    rotation = value;
                }
            }
        } else if (property.equalsIgnoreCase("shear x")) {
            this.shearX = value;
        } else if (property.equalsIgnoreCase("shear y")) {
            this.shearY = value;
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
            this.speed = value;
        } else if (property.equalsIgnoreCase("direction")) {
            this.speedDirection = value;
        } else if (property.equalsIgnoreCase("rotation speed")) {
            this.rotationSpeed = value;
        } else if (property.equalsIgnoreCase("pen thickness")) {
            this.penWidth = value;
        } else if (property.equalsIgnoreCase("perspective x1")) {
            this.perspectiveX1 = value;
            if (value.isEmpty()) {
                this.p_x0 = 0.0;
            }
        } else if (property.equalsIgnoreCase("perspective y1")) {
            this.perspectiveY1 = value;
            if (value.isEmpty()) {
                this.p_y0 = 0.0;
            }
        } else if (property.equalsIgnoreCase("perspective x2")) {
            this.perspectiveX2 = value;
            if (value.isEmpty()) {
                this.p_x1 = 1.0;
            }
        } else if (property.equalsIgnoreCase("perspective y2")) {
            this.perspectiveY2 = value;
            if (value.isEmpty()) {
                this.p_y1 = 0.0;
            }
        } else if (property.equalsIgnoreCase("perspective x3")) {
            this.perspectiveX3 = value;
            if (value.isEmpty()) {
                this.p_x2 = 1.0;
            }
        } else if (property.equalsIgnoreCase("perspective y3")) {
            this.perspectiveY3 = value;
            if (value.isEmpty()) {
                this.p_y2 = 1.0;
            }
        } else if (property.equalsIgnoreCase("perspective x4")) {
            this.perspectiveX4 = value;
            if (value.isEmpty()) {
                this.p_x3 = 0.0;
            }
        } else if (property.equalsIgnoreCase("perspective y4")) {
            this.perspectiveY4 = value;
            if (value.isEmpty()) {
                this.p_y3 = 1.0;
            }
        } else if (property.equalsIgnoreCase("automatic perspective")) {
            this.automaticPerspective = value;
        } else if (property.equalsIgnoreCase("perspective depth")) {
            this.perspectiveDepth = value;
        } else if (property.equalsIgnoreCase("horizontal 3d rotation")) {
            this.rotation3DHorizontal = value;
        } else if (property.equalsIgnoreCase("vertical 3d rotation")) {
            this.rotation3DVertical = value;
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

    public int getSpeedPrevX1() {
        return speedPrevX1;
    }

    public void setSpeedPrevX1(int speedPrevX1) {
        this.speedPrevX1 = speedPrevX1;
    }

    public int getSpeedPrevY1() {
        return speedPrevY1;
    }

    public void setSpeedPrevY1(int speedPrevY1) {
        this.speedPrevY1 = speedPrevY1;
    }

    public int getSpeedPrevX2() {
        return speedPrevX2;
    }

    public void setSpeedPrevX2(int speedPrevX2) {
        this.speedPrevX2 = speedPrevX2;
    }

    public int getSpeedPrevY2() {
        return speedPrevY2;
    }

    public void setSpeedPrevY2(int speedPrevY2) {
        this.speedPrevY2 = speedPrevY2;
    }

    public int getSpeedWidth() {
        return speedWidth;
    }

    public void setSpeedWidth(int speedWidth) {
        this.speedWidth = speedWidth;
    }

    public int getSpeedHeight() {
        return speedHeight;
    }

    public void setSpeedHeight(int speedHeight) {
        this.speedHeight = speedHeight;
    }

    public double getSpeedX() {
        return speedX;
    }

    public void setSpeedX(double speedX) {
        this.speedX = speedX;
    }

    public double getSpeedY() {
        return speedY;
    }

    public void setSpeedY(double speedY) {
        this.speedY = speedY;
    }

    public double getSpeedPrevDirection() {
        return speedPrevDirection;
    }

    public void setSpeedPrevDirection(double speedPrevDirection) {
        this.speedPrevDirection = speedPrevDirection;
    }

    public double getSpeedValue() {
        return speedValue;
    }

    public void setSpeedValue(double speedValue) {
        this.speedValue = speedValue;
    }

    public long getLastFrameTime() {
        return lastFrameTime;
    }

    public void setLastFrameTime(long lastFrameTime) {
        this.lastFrameTime = lastFrameTime;
    }

    public String getRegionGrouping() {
        return regionGrouping;
    }

    public void setRegionGrouping(String regionGrouping) {
        this.regionGrouping = regionGrouping;
    }

    public ActiveRegions getParent() {
        return parent;
    }

    public void setParent(ActiveRegions parent) {
        this.parent = parent;
    }

    public List<RegionOverlapEventMacro> getRegionOverlapEventMacros() {
        return regionOverlapEventMacros;
    }

    public void setRegionOverlapEventMacros(List<RegionOverlapEventMacro> regionOverlapEventMacros) {
        this.regionOverlapEventMacros = regionOverlapEventMacros;
    }

    public List<WidgetEventMacro> getWidgetEventMacros() {
        return widgetEventMacros;
    }

    public void setWidgetEventMacros(List<WidgetEventMacro> widgetEventMacros) {
        this.widgetEventMacros = widgetEventMacros;
    }

    public MouseEventsProcessor getMouseEventsProcessor() {
        return mouseEventsProcessor;
    }

    public void setMouseEventsProcessor(MouseEventsProcessor mouseEventsProcessor) {
        this.mouseEventsProcessor = mouseEventsProcessor;
    }

    public KeyboardEventsProcessor getKeyboardEventsProcessor() {
        return keyboardEventsProcessor;
    }

    public void setKeyboardEventsProcessor(KeyboardEventsProcessor keyboardEventsProcessor) {
        this.keyboardEventsProcessor = keyboardEventsProcessor;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isInFocus() {
        return inFocus;
    }

    public void setInFocus(boolean inFocus) {
        this.inFocus = inFocus;
    }

    public void setX1Value(int x1Value) {
        this.x1Value = x1Value;
    }

    public void setY1Value(int y1Value) {
        this.y1Value = y1Value;
    }

    public void setX2Value(int x2Value) {
        this.x2Value = x2Value;
    }

    public void setY2Value(int y2Value) {
        this.y2Value = y2Value;
    }

    public double getCenterOfRotationX() {
        return centerOfRotationX;
    }

    public void setCenterOfRotationX(double centerOfRotationX) {
        this.centerOfRotationX = centerOfRotationX;
    }

    public double getCenterOfRotationY() {
        return centerOfRotationY;
    }

    public void setCenterOfRotationY(double centerOfRotationY) {
        this.centerOfRotationY = centerOfRotationY;
    }

    public double getTrajectory2X() {
        return trajectory2X;
    }

    public void setTrajectory2X(double trajectory2X) {
        this.trajectory2X = trajectory2X;
    }

    public double getTrajectory2Y() {
        return trajectory2Y;
    }

    public void setTrajectory2Y(double trajectory2Y) {
        this.trajectory2Y = trajectory2Y;
    }

    public double getP_x0() {
        return p_x0;
    }

    public void setP_x0(double p_x0) {
        this.p_x0 = p_x0;
    }

    public double getP_y0() {
        return p_y0;
    }

    public void setP_y0(double p_y0) {
        this.p_y0 = p_y0;
    }

    public double getP_x1() {
        return p_x1;
    }

    public void setP_x1(double p_x1) {
        this.p_x1 = p_x1;
    }

    public double getP_y1() {
        return p_y1;
    }

    public void setP_y1(double p_y1) {
        this.p_y1 = p_y1;
    }

    public double getP_x2() {
        return p_x2;
    }

    public void setP_x2(double p_x2) {
        this.p_x2 = p_x2;
    }

    public double getP_y2() {
        return p_y2;
    }

    public void setP_y2(double p_y2) {
        this.p_y2 = p_y2;
    }

    public double getP_x3() {
        return p_x3;
    }

    public void setP_x3(double p_x3) {
        this.p_x3 = p_x3;
    }

    public double getP_y3() {
        return p_y3;
    }

    public void setP_y3(double p_y3) {
        this.p_y3 = p_y3;
    }

    public int getPenX() {
        return penX;
    }

    public void setPenX(int penX) {
        this.penX = penX;
    }

    public int getPenY() {
        return penY;
    }

    public void setPenY(int penY) {
        this.penY = penY;
    }

    public double getRotationValue() {
        return rotationValue;
    }

    public void setRotationValue(double rotationValue) {
        this.rotationValue = rotationValue;
    }

    public double getShearXValue() {
        return shearXValue;
    }

    public void setShearXValue(double shearXValue) {
        this.shearXValue = shearXValue;
    }

    public double getShearYValue() {
        return shearYValue;
    }

    public void setShearYValue(double shearYValue) {
        this.shearYValue = shearYValue;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }

    public String getRelativeX() {
        return relativeX;
    }

    public void setRelativeX(String relativeX) {
        this.relativeX = relativeX;
    }

    public String getRelativeY() {
        return relativeY;
    }

    public void setRelativeY(String relativeY) {
        this.relativeY = relativeY;
    }

    public String getZoom() {
        return zoom;
    }

    public void setZoom(String zoom) {
        this.zoom = zoom;
    }

    public String getTrajectoryPosition() {
        return trajectoryPosition;
    }

    public void setTrajectoryPosition(String trajectoryPosition) {
        this.trajectoryPosition = trajectoryPosition;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getRotation() {
        return rotation;
    }

    public void setRotation(String rotation) {
        this.rotation = rotation;
    }

    public String getShearX() {
        return shearX;
    }

    public void setShearX(String shearX) {
        this.shearX = shearX;
    }

    public String getShearY() {
        return shearY;
    }

    public void setShearY(String shearY) {
        this.shearY = shearY;
    }

    public String getWindowX() {
        return windowX;
    }

    public void setWindowX(String windowX) {
        this.windowX = windowX;
    }

    public String getWindowY() {
        return windowY;
    }

    public void setWindowY(String windowY) {
        this.windowY = windowY;
    }

    public String getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(String windowWidth) {
        this.windowWidth = windowWidth;
    }

    public String getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(String windowHeight) {
        this.windowHeight = windowHeight;
    }

    public String getTransparency() {
        return transparency;
    }

    public void setTransparency(String transparency) {
        this.transparency = transparency;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getSpeedDirection() {
        return speedDirection;
    }

    public void setSpeedDirection(String speedDirection) {
        this.speedDirection = speedDirection;
    }

    public String getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(String rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public String getPenWidth() {
        return penWidth;
    }

    public void setPenWidth(String penWidth) {
        this.penWidth = penWidth;
    }

    public String getX1() {
        return x1;
    }

    public void setX1(String x1) {
        this.x1 = x1;
    }

    public String getY1() {
        return y1;
    }

    public void setY1(String y1) {
        this.y1 = y1;
    }

    public String getX2() {
        return x2;
    }

    public void setX2(String x2) {
        this.x2 = x2;
    }

    public String getY2() {
        return y2;
    }

    public void setY2(String y2) {
        this.y2 = y2;
    }

    public String getPerspectiveX1() {
        return perspectiveX1;
    }

    public void setPerspectiveX1(String perspectiveX1) {
        this.perspectiveX1 = perspectiveX1;
    }

    public String getPerspectiveY1() {
        return perspectiveY1;
    }

    public void setPerspectiveY1(String perspectiveY1) {
        this.perspectiveY1 = perspectiveY1;
    }

    public String getPerspectiveX2() {
        return perspectiveX2;
    }

    public void setPerspectiveX2(String perspectiveX2) {
        this.perspectiveX2 = perspectiveX2;
    }

    public String getPerspectiveY2() {
        return perspectiveY2;
    }

    public void setPerspectiveY2(String perspectiveY2) {
        this.perspectiveY2 = perspectiveY2;
    }

    public String getPerspectiveX3() {
        return perspectiveX3;
    }

    public void setPerspectiveX3(String perspectiveX3) {
        this.perspectiveX3 = perspectiveX3;
    }

    public String getPerspectiveY3() {
        return perspectiveY3;
    }

    public void setPerspectiveY3(String perspectiveY3) {
        this.perspectiveY3 = perspectiveY3;
    }

    public String getPerspectiveX4() {
        return perspectiveX4;
    }

    public void setPerspectiveX4(String perspectiveX4) {
        this.perspectiveX4 = perspectiveX4;
    }

    public String getPerspectiveY4() {
        return perspectiveY4;
    }

    public void setPerspectiveY4(String perspectiveY4) {
        this.perspectiveY4 = perspectiveY4;
    }

    public String getAutomaticPerspective() {
        return automaticPerspective;
    }

    public void setAutomaticPerspective(String automaticPerspective) {
        this.automaticPerspective = automaticPerspective;
    }

    public String getRotation3DHorizontal() {
        return rotation3DHorizontal;
    }

    public void setRotation3DHorizontal(String rotation3DHorizontal) {
        this.rotation3DHorizontal = rotation3DHorizontal;
    }

    public String getRotation3DVertical() {
        return rotation3DVertical;
    }

    public void setRotation3DVertical(String rotation3DVertical) {
        this.rotation3DVertical = rotation3DVertical;
    }

    public String getPerspectiveDepth() {
        return perspectiveDepth;
    }

    public void setPerspectiveDepth(String perspectiveDepth) {
        this.perspectiveDepth = perspectiveDepth;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public Object[][] getMotionAndRotationVariablesMapping() {
        return motionAndRotationVariablesMapping;
    }

    public void setMotionAndRotationVariablesMapping(Object[][] motionAndRotationVariablesMapping) {
        this.motionAndRotationVariablesMapping = motionAndRotationVariablesMapping;
    }

    public Object[][] getMotionAndRotationLimits() {
        return motionAndRotationLimits;
    }

    public void setMotionAndRotationLimits(Object[][] motionAndRotationLimits) {
        this.motionAndRotationLimits = motionAndRotationLimits;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public String getShapeArguments() {
        return shapeArguments;
    }

    public void setShapeArguments(String shapeArguments) {
        this.shapeArguments = shapeArguments;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(String fontStyle) {
        this.fontStyle = fontStyle;
    }

    public List<String> getAdditionalImageFileNames() {
        return additionalImageFileNames;
    }

    public void setAdditionalImageFileNames(List<String> additionalImageFileNames) {
        this.additionalImageFileNames = additionalImageFileNames;
    }

    public boolean isInTrajectoryMode() {
        return inTrajectoryMode;
    }

    public void setInTrajectoryMode(boolean inTrajectoryMode) {
        this.inTrajectoryMode = inTrajectoryMode;
    }

    public boolean isInTrajectoryMode2() {
        return inTrajectoryMode2;
    }

    public void setInTrajectoryMode2(boolean inTrajectoryMode2) {
        this.inTrajectoryMode2 = inTrajectoryMode2;
    }

    public int getTrajectoryType() {
        return trajectoryType;
    }

    public void setTrajectoryType(int trajectoryType) {
        this.trajectoryType = trajectoryType;
    }

    public String getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(String horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    public String getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(String verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
    }

    public String getLineColor() {
        return lineColor;
    }

    public void setLineColor(String lineColor) {
        this.lineColor = lineColor;
    }

    public String getLineStyle() {
        return lineStyle;
    }

    public void setLineStyle(String lineStyle) {
        this.lineStyle = lineStyle;
    }

    public String getFillColor() {
        return fillColor;
    }

    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }

    public String getCaptureScreenX() {
        return captureScreenX;
    }

    public void setCaptureScreenX(String captureScreenX) {
        this.captureScreenX = captureScreenX;
    }

    public String getCaptureScreenY() {
        return captureScreenY;
    }

    public void setCaptureScreenY(String captureScreenY) {
        this.captureScreenY = captureScreenY;
    }

    public String getCaptureScreenWidth() {
        return captureScreenWidth;
    }

    public void setCaptureScreenWidth(String captureScreenWidth) {
        this.captureScreenWidth = captureScreenWidth;
    }

    public String getCaptureScreenHeight() {
        return captureScreenHeight;
    }

    public void setCaptureScreenHeight(String captureScreenHeight) {
        this.captureScreenHeight = captureScreenHeight;
    }

    public String getTextField() {
        return textField;
    }

    public void setTextField(String textField) {
        this.textField = textField;
    }

    public String getEmbeddedSketch() {
        return embeddedSketch;
    }

    public void setEmbeddedSketch(String embeddedSketch) {
        this.embeddedSketch = embeddedSketch;
    }

    public String getEmbeddedSketchVarPrefix() {
        return embeddedSketchVarPrefix;
    }

    public void setEmbeddedSketchVarPrefix(String embeddedSketchVarPrefix) {
        this.embeddedSketchVarPrefix = embeddedSketchVarPrefix;
    }

    public String getEmbeddedSketchVarPostfix() {
        return embeddedSketchVarPostfix;
    }

    public void setEmbeddedSketchVarPostfix(String embeddedSketchVarPostfix) {
        this.embeddedSketchVarPostfix = embeddedSketchVarPostfix;
    }

    public String getImageUrlField() {
        return imageUrlField;
    }

    public void setImageUrlField(String imageUrlField) {
        this.imageUrlField = imageUrlField;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getWidget() {
        return widget;
    }

    public void setWidget(String widget) {
        this.widget = widget;
    }

    public String getWidgetPropertiesString() {
        return widgetPropertiesString;
    }

    public void setWidgetPropertiesString(String widgetPropertiesString) {
        this.widgetPropertiesString = widgetPropertiesString;
    }

    public Properties getWidgetProperties() {
        return widgetProperties;
    }

    public void setWidgetProperties(Properties widgetProperties) {
        this.widgetProperties = widgetProperties;
    }

    public String getImageIndex() {
        return imageIndex;
    }

    public void setImageIndex(String imageIndex) {
        this.imageIndex = imageIndex;
    }

    public String getAnimationFrameRateMs() {
        return animationFrameRateMs;
    }

    public void setAnimationFrameRateMs(String animationFrameRateMs) {
        this.animationFrameRateMs = animationFrameRateMs;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public List<BufferedImage> getAdditionalDrawnImages() {
        return additionalDrawnImages;
    }

    public void setAdditionalDrawnImages(List<BufferedImage> additionalDrawnImages) {
        this.additionalDrawnImages = additionalDrawnImages;
    }

    public List<Boolean> getAdditionalDrawnImagesChanged() {
        return additionalDrawnImagesChanged;
    }

    public void setAdditionalDrawnImagesChanged(List<Boolean> additionalDrawnImagesChanged) {
        this.additionalDrawnImagesChanged = additionalDrawnImagesChanged;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTrajectory1() {
        return trajectory1;
    }

    public void setTrajectory1(String trajectory1) {
        this.trajectory1 = trajectory1;
    }

    public String getTrajectory2() {
        return trajectory2;
    }

    public void setTrajectory2(String trajectory2) {
        this.trajectory2 = trajectory2;
    }

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    public boolean isRotatable() {
        return rotatable;
    }

    public void setRotatable(boolean rotatable) {
        this.rotatable = rotatable;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }

    public boolean isFitToBoxEnabled() {
        return fitToBoxEnabled;
    }

    public void setFitToBoxEnabled(boolean fitToBoxEnabled) {
        this.fitToBoxEnabled = fitToBoxEnabled;
    }

    public boolean isTextWrapped() {
        return textWrapped;
    }

    public void setTextWrapped(boolean textWrapped) {
        this.textWrapped = textWrapped;
    }

    public boolean isTextTrimmed() {
        return textTrimmed;
    }

    public void setTextTrimmed(boolean textTrimmed) {
        this.textTrimmed = textTrimmed;
    }

    public boolean isWalkThroughEnabled() {
        return walkThroughEnabled;
    }

    public void setWalkThroughEnabled(boolean walkThroughEnabled) {
        this.walkThroughEnabled = walkThroughEnabled;
    }

    public boolean isStickToTrajectoryEnabled() {
        return stickToTrajectoryEnabled;
    }

    public void setStickToTrajectoryEnabled(boolean stickToTrajectoryEnabled) {
        this.stickToTrajectoryEnabled = stickToTrajectoryEnabled;
    }

    public boolean isChangingOrientationOnTrajectoryEnabled() {
        return changingOrientationOnTrajectoryEnabled;
    }

    public void setChangingOrientationOnTrajectoryEnabled(boolean changingOrientationOnTrajectoryEnabled) {
        this.changingOrientationOnTrajectoryEnabled = changingOrientationOnTrajectoryEnabled;
    }

    public boolean isScreenCapturingEnabled() {
        return screenCapturingEnabled;
    }

    public void setScreenCapturingEnabled(boolean screenCapturingEnabled) {
        this.screenCapturingEnabled = screenCapturingEnabled;
    }

    public boolean isScreenCapturingMouseMappingEnabled() {
        return screenCapturingMouseMappingEnabled;
    }

    public void setScreenCapturingMouseMappingEnabled(boolean screenCapturingMouseMappingEnabled) {
        this.screenCapturingMouseMappingEnabled = screenCapturingMouseMappingEnabled;
    }

    public String getWidgetItems() {
        return widgetItems;
    }

    public void setWidgetItems(String widgetItems) {
        this.widgetItems = widgetItems;
    }

    public String getCharactersPerLine() {
        return charactersPerLine;
    }

    public void setCharactersPerLine(String charactersPerLine) {
        this.charactersPerLine = charactersPerLine;
    }

    public String getMaxNumLines() {
        return maxNumLines;
    }

    public void setMaxNumLines(String maxNumLines) {
        this.maxNumLines = maxNumLines;
    }

    public String getPreviousImage() {
        return previousImage;
    }

    public void setPreviousImage(String previousImage) {
        this.previousImage = previousImage;
    }

    public boolean isbActive() {
        return bActive;
    }

    public void setbActive(boolean bActive) {
        this.bActive = bActive;
    }

    public boolean isAdjusting() {
        return adjusting;
    }

    public void setAdjusting(boolean adjusting) {
        this.adjusting = adjusting;
    }

    public String[][] getPropertiesDefaultLimits() {
        return propertiesDefaultLimits;
    }

    public void setPropertiesDefaultLimits(String[][] propertiesDefaultLimits) {
        this.propertiesDefaultLimits = propertiesDefaultLimits;
    }

    public String[][] getPropertiesAnimation() {
        return propertiesAnimation;
    }

    public void setPropertiesAnimation(String[][] propertiesAnimation) {
        this.propertiesAnimation = propertiesAnimation;
    }

    public Vector<String> getUpdatingProperties() {
        return updatingProperties;
    }

    public void setUpdatingProperties(Vector<String> updatingProperties) {
        this.updatingProperties = updatingProperties;
    }

    public String getLineThickness() {
        return lineThickness;
    }

    public void setLineThickness(String lineThickness) {
        this.lineThickness = lineThickness;
    }

    public void setType(String type) {
        this.type = type;
    }
}