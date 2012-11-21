/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.editor.printing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Arrays;
import java.util.List;

public class BufferedImagePrinter implements Pageable, Printable {
    private List<BufferedImage> images;
    private Runnable beforePrint = null;

    public int getNumberOfPages() {
        return images == null ? 0 : images.size();
    }

    PageFormat format;

    public PageFormat getPageFormat(int pagenum) {
        return format;
    }

    public Printable getPrintable(int pagenum) {
        return this;
    }

    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {

        if (images == null || page >= images.size()) {
            return NO_SUCH_PAGE;
        }

        BufferedImage image = images.get(page);

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());

        if (image != null) {
            int imagew = image.getWidth();
            int imageh = image.getHeight();
            double coeffX = pf.getImageableWidth() / imagew;
            double coeffY = pf.getImageableHeight() / imageh;

            double coeff = Math.min(coeffX, coeffY);

            double w = imagew * coeff;
            double h = imageh * coeff;

            int x = (int) (pf.getImageableWidth() - w) / 2;
            int y = (int) (pf.getImageableHeight() - h) / 2;

            g2d.drawImage(image, Math.max(0, x), Math.max(0, y), (int) (imagew * coeff), (int) (imageh * coeff), null);
        }

        return PAGE_EXISTS;
    }

    public BufferedImagePrinter(BufferedImage... images) {
        setImages(images);
    }

    public void setImages(BufferedImage... image) {
        this.images = Arrays.asList(image);
    }

    public void setActionBeforePrinting(Runnable beforePrint) {
        this.beforePrint = beforePrint;
    }

    public void print() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        boolean ok = job.printDialog();
        if (ok) {
            try {
                if (beforePrint != null) {
                    beforePrint.run();
                }
                format = job.defaultPage();
                job.print();
            } catch (PrinterException ex) {
                /* The job did not successfully complete */
            }
        }
    }
}