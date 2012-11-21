/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.renderer.regions;

import net.sf.sketchlet.common.context.SketchletContextUtils;
import net.sf.sketchlet.designer.Workspace;
import net.sf.sketchlet.model.ActiveRegion;
import net.sf.sketchlet.util.Colors;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author zobrenovic
 */
public class TextDrawingLayer extends DrawingLayer {
    private static final Logger log = Logger.getLogger(TextDrawingLayer.class);

    public static final Hashtable<String, Font> FONT_CACHE = new Hashtable<String, Font>();

    private Vector<String> textFileLines = new Vector<String>();
    private Font font = null;
    private LineMetrics metrics;
    private float xText;
    private float yText;
    private BufferedImage cachedImage = null;
    private Vector<String> prevTextLines = new Vector<String>();
    private int regionWidth = 0;
    private int regionHeight = 0;
    private String strHAlign = "";
    private String strVAlign = "";
    private String strTextParams = "";
    private String strFont = "";

    public TextDrawingLayer(ActiveRegion region) {
        super(region);
    }

    public void flush() {
        if (cachedImage != null) {
            cachedImage.flush();
            cachedImage = null;
        }
    }

    public void dispose() {
        region = null;
        font = null;
        metrics = null;
        textFileLines = null;
        if (cachedImage != null) {
            cachedImage.flush();
            cachedImage = null;
        }
        prevTextLines = null;
    }

    public boolean checkShouldDraw(Vector<String> textLines) {
        boolean bShouldDraw = cachedImage == null; // || cachedImage.contentsLost() || cachedImage.validate(null) != VolatileImage.IMAGE_OK;
        bShouldDraw = bShouldDraw || isTextChanged(textLines) || isRegionChanged() || isFontChanged();

        prevTextLines = textLines;
        regionWidth = Math.abs(region.x2 - region.x1);
        regionHeight = Math.abs(region.y2 - region.y1);
        strHAlign = region.processText(region.horizontalAlignment);
        strVAlign = region.processText(region.verticalAlignment);
        strTextParams = region.textTrimmed + ";" + region.strCharactersPerLine + ";" + region.maxNumLines;
        String fontName = region.processText(region.fontName);
        String fontStyle = region.processText(region.fontStyle);
        String fontSize = region.processText(region.fontSize);
        String fontColor = region.processText(region.fontColor);
        strFont = fontName + ";" + fontStyle + ";" + fontSize + ";" + fontColor;

        bShouldDraw = bShouldDraw || regionHeight <= 0 || regionWidth <= 0;

        return bShouldDraw;
    }

    public boolean isFontChanged() {
        String fontName = region.processText(region.fontName);
        String fontStyle = region.processText(region.fontStyle);
        String fontSize = region.processText(region.fontSize);
        String fontColor = region.processText(region.fontColor);
        return !strFont.equals(fontName + ";" + fontStyle + ";" + fontSize + ";" + fontColor);
    }

    public boolean isTextChanged(Vector<String> textLines) {
        if (textLines.size() != prevTextLines.size()) {
            return true;
        }

        if (!strTextParams.equals(region.textTrimmed + ";" + region.strCharactersPerLine + ";" + region.maxNumLines)) {
            return true;
        }

        for (int i = 0; i < textLines.size(); i++) {
            String line = textLines.elementAt(i);
            String prevLine = prevTextLines.elementAt(i);

            if (!line.equals(prevLine)) {
                return true;
            }
        }

        return false;
    }

    public boolean isRegionChanged() {
        String strHAlign = region.processText(region.horizontalAlignment);
        String strVAlign = region.processText(region.verticalAlignment);
        if (region.x2 - region.x1 != regionWidth || region.y2 - region.y1 != regionHeight || !this.strHAlign.equals(strHAlign) || !this.strVAlign.equals(strVAlign)) {
            return true;
        }

        return false;
    }

    public void draw(Graphics2D g2, Component component, boolean bPlayback) {
        String strText = region.strTextField;
        //String strTextFile = region.strTextFile;

        if (region.text.trim().length() == 0 && strText.isEmpty() /*&& strTextFile.isEmpty()*/) {
            return;
        }

        Vector<String> textLines = new Vector<String>();

        strText = region.processText(strText).trim();

        if (strText.length() > 0) {
            if (region.textTrimmed) {
                strText = strText.trim();
            }
            textLines.add(strText);
        }

        if (region.text.trim().length() > 0) {
            String strTextArea = region.processText(region.text);
            String strings[] = strTextArea.split("\n");
            for (int i = 0; i < strings.length; i++) {
                String strLineOfText = region.processText(strings[i]);
                if (region.textTrimmed) {
                    strLineOfText = strLineOfText.trim();
                }
                textLines.add(strLineOfText);
            }
        }

        if (textLines.size() > 0) {
            if (checkShouldDraw(textLines)) {
                drawTextLines(component, textLines, strText, bPlayback);
            }
            if (this.cachedImage != null) {
                g2.drawImage(cachedImage, region.x1, region.y1, null);
            }
        }
    }

    public void drawTextLines(Component component, Vector<String> textLines, String strText, boolean bPlayback) {
        try {
            if (regionWidth <= 0 || regionHeight <= 0) {
                return;
            }
            cachedImage = Workspace.createCompatibleImage(regionWidth, regionHeight, cachedImage);
            //cachedImage = component.getGraphicsConfiguration().createCompatibleVolatileImage(regionWidth, regionHeight, Transparency.TRANSLUCENT);

            Graphics2D g2 = cachedImage.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            /*g2.setComposite(AlphaComposite.Src);
            g2.setColor(new Color(0, 0, 0, 0));
            g2.fillRect(0, 0, regionWidth, regionHeight);*/

            Color textColor = Colors.getColor(region.processText(region.fontColor));
            if (textColor == null) {
                g2.setColor(Color.BLACK);
            } else {
                g2.setColor(textColor);
            }

            if (region.textWrapped && !region.strCharactersPerLine.equals("")) {
                textLines = wrapLines(textLines);
            }

            if (textLines.size() > 0) {
                String fontName = region.processText(region.fontName);
                String fontStyle = region.processText(region.fontStyle);

                Font oldFont = g2.getFont();

                int fontSize = 0;
                if (region.fontSize.length() != 0) {
                    try {
                        fontSize = Math.min(Integer.parseInt(region.processText(region.fontSize)), 100);
                    } catch (Exception e) {
                    }
                }
                float textWidth = 0.0f;
                if (fontSize == 0) {
                    fontSize = Math.abs(regionHeight) / (textLines.size());
                }
                FontRenderContext frc = g2.getFontRenderContext();
                font = getFont(fontName, fontStyle, (float) fontSize);
                metrics = font.getLineMetrics(strText, frc);
                for (String strDrawLine : textLines) {
                    if (textWidth < font.getStringBounds(strDrawLine, frc).getMaxX()) {
                        strText = strDrawLine;
                        textWidth = (float) font.getStringBounds(strDrawLine, frc).getMaxX();
                    }
                }

                float textHeight = metrics.getHeight() * textLines.size();

                //if (region.fitToBox.isSelected()) {
                xText = 0;
                while ((/*textWidth > regionWidth || */textHeight > regionHeight) && fontSize > 1) {
                    metrics = font.getLineMetrics(strText, frc);
                    fontSize--;

                    font = getFont(fontName, fontStyle, (float) fontSize);
                    textWidth = (float) font.getStringBounds(strText, frc).getMaxX();
                    if (metrics != null && textLines != null) {
                        textHeight = metrics.getHeight() * textLines.size();
                    } else {
                        break;
                    }
                }

                if (strVAlign.equals("top") || strVAlign.isEmpty()) {
                    yText = 0 + metrics.getAscent(); // - metrics.getDescent();
                } else if (strVAlign.equals("center")) {
                    // yText = y1 + (y2 - y1) / 2 + metrics.getAscent() / 2 - metrics.getDescent() / 2 - metrics.getHeight() * (textLines.size() - 1) / 2;
                    yText = 0 + metrics.getAscent() + ((regionHeight) - metrics.getHeight() * (textLines.size())) / 2;
                } else {
                    yText = regionHeight - metrics.getHeight() * (textLines.size()) + metrics.getAscent();
                }

                if (strHAlign.equals("left") || strHAlign.isEmpty()) {
                    xText = 0;
                } else if (strHAlign.equals("center")) {
                    xText = 0 + regionWidth / 2 - textWidth / 2;
                } else {
                    xText = regionWidth - textWidth;
                }

                /*} else {
                // yText = y2 - metrics.getDescent() - metrics.getHeight() * (textLines.size() - 1);
                while ((textHeight > regionHeight) && fontSize > 1) {
                metrics = font.getLineMetrics(strText, frc);
                fontSize--;
                
                font = getFont(fontName, fontStyle, (float) fontSize);
                
                textWidth = (float) font.getStringBounds(strText, frc).getMaxX();
                textHeight = metrics.getHeight() * textLines.size();
                }
                yText = regionHeight - metrics.getHeight() * textLines.size() + metrics.getAscent();
                if (strHAlign.equals("left") || strHAlign.isEmpty()) {
                xText = 0;
                } else if (strHAlign.equals("center")) {
                xText = 0 + (regionWidth) / 2 - textWidth / 2;
                } else {
                xText = regionWidth - textWidth;
                }
                
                }*/

                g2.setFont(font);

                int i = 0;
                for (String strDrawText : textLines) {
                    String original = strDrawText;
                    textWidth = (float) font.getStringBounds(strDrawText, frc).getMaxX();
                    while (textWidth > regionWidth && original.length() > 0) {
                        original = original.substring(0, original.length() - 1);
                        strDrawText = original + "...";
                        textWidth = (float) font.getStringBounds(strDrawText, frc).getMaxX();
                    }
                    if (strHAlign.equals("left") || strHAlign.isEmpty()) {
                        xText = 0;
                    } else if (strHAlign.equals("center")) {
                        xText = 0 + (regionWidth) / 2 - textWidth / 2;
                    } else {
                        xText = regionWidth - textWidth;
                    }
                    g2.drawString(strDrawText, xText, yText + i++ * metrics.getHeight());
                }

                g2.setFont(oldFont);
            }
            g2.dispose();
        } catch (Exception e) {
            log.error(e);
            this.cachedImage = null;
        }
    }

    public Vector<String> wrapLines(Vector<String> textLines) {
        Vector<String> wrapedLines = textLines;
        try {
            int charPerLine = Integer.parseInt(region.strCharactersPerLine);
            int maxLines = 0;
            if (!region.maxNumLines.trim().equals("")) {
                maxLines = Integer.parseInt(region.maxNumLines);
            }

            wrapedLines = new Vector<String>();

            int i = 0;

            for (int l = 0; l < textLines.size(); l++) {
                String strLine = textLines.elementAt(l);

                if (strLine.length() <= charPerLine) {
                    wrapedLines.add(strLine);
                    i++;
                    if (maxLines > 0 && i >= maxLines) {
                        if (l < textLines.size() - 1) {
                            wrapedLines.setElementAt(strLine + "...", wrapedLines.size() - 1);
                        }
                        return wrapedLines;
                    }
                } else {
                    while (strLine.length() > charPerLine) {
                        int n = strLine.substring(0, charPerLine).lastIndexOf(" ");

                        if (n == -1) {
                            n = charPerLine;
                        }

                        wrapedLines.add(strLine.substring(0, n));
                        i++;
                        if (maxLines > 0 && i >= maxLines) {
                            if (n < strLine.length()) {
                                wrapedLines.setElementAt(strLine.substring(0, n) + "...", wrapedLines.size() - 1);
                            }
                            return wrapedLines;
                        }
                        strLine = strLine.substring(n).trim();
                    }

                    wrapedLines.add(strLine);
                    i++;
                    if (maxLines > 0 && i >= maxLines) {
                        if (l < textLines.size() - 1) {
                            wrapedLines.setElementAt(strLine + "...", wrapedLines.size() - 1);
                        }
                        return wrapedLines;
                    }
                }
            }

        } catch (Exception e) {
        }

        return wrapedLines;
    }

    public static Font getFont(String strName, String strStyle, float size) {
        Font font = getFont(strName);
        try {
            int style = Font.PLAIN;
            if (strStyle.equalsIgnoreCase("regular")) {
                style = Font.PLAIN;
            } else if (strStyle.equalsIgnoreCase("italic")) {
                style = Font.ITALIC;
            } else if (strStyle.equalsIgnoreCase("bold")) {
                style = Font.BOLD;
            } else if (strStyle.equalsIgnoreCase("bold italic")) {
                style = Font.BOLD + Font.ITALIC;
            }

            font = font.deriveFont(style, size);
        } catch (Exception e) {
        }

        return font;
    }

    public static Font getFont(String strName) {
        Font font = FONT_CACHE.get(strName);
        if (font == null) {
            if (strName.length() > 0) {
                font = new Font(strName, Font.PLAIN, 12);
                FONT_CACHE.put(strName, font);
                return font;
            }
        } else {
            return font;
        }
        return SketchletContextUtils.getDefaultSketchFont();
    }
}
