/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.filter;

import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author zobrenovic
 */
public abstract class ColorBlindFilter implements ImageFilter {
    private static final Logger log = Logger.getLogger(ColorBlindFilter.class);
    double gammaRGB[] = new double[]{2.1, 2.0, 2.1};

    BufferedImage brettel(BufferedImage image, char c, double ad[]) {
        int i = image.getWidth();
        int j = image.getHeight();
        Rectangle rectangle = new Rectangle(0, 0, i, j);
        double ad1[] = {
                0.050599829999999998D, 0.085853689999999996D, 0.0095242D, 0.018930329999999999D, 0.089253079999999999D, 0.013700540000000001D, 0.00292202D, 0.0097573199999999999D, 0.071459789999999995D
        };
        double ad2[] = {
                30.830853999999999D, -29.832659D, 1.610474D, -6.4814679999999996D, 17.715578000000001D, -2.5326420000000001D, -0.37569000000000002D, -1.1990620000000001D, 14.273846000000001D
        };
        double d18 = ad1[0] + ad1[1] + ad1[2];
        double d19 = ad1[3] + ad1[4] + ad1[5];
        double d20 = ad1[6] + ad1[7] + ad1[8];
        double d6 = 0.080079999999999998D;
        double d7 = 0.15790000000000001D;
        double d8 = 0.5897D;
        double d9 = 0.12839999999999999D;
        double d10 = 0.22370000000000001D;
        double d11 = 0.36359999999999998D;
        double d12 = 0.98560000000000003D;
        double d13 = 0.73250000000000004D;
        double d14 = 0.0010790000000000001D;
        double d15 = 0.091399999999999995D;
        double d16 = 0.0070089999999999996D;
        double d17 = 0.0D;
        double ad3[] = {
                1.0D / ad[0], 1.0D / ad[1], 1.0D / ad[2]
        };
        double d28 = 0.0D;
        double d29 = 1.0D / (double) rectangle.height;
        double d21;
        double d22;
        double d23;
        double d24;
        double d25;
        double d26;
        double d27;
        switch (c) {
            case 100: // 'd'
            default:
                d21 = d20 / d18;
                d22 = d19 * d14 - d20 * d13;
                d23 = d20 * d12 - d18 * d14;
                d24 = d18 * d13 - d19 * d12;
                d25 = d19 * d8 - d20 * d7;
                d26 = d20 * d6 - d18 * d8;
                d27 = d18 * d7 - d19 * d6;
                break;

            case 112: // 'p'
                d21 = d20 / d19;
                d22 = d19 * d14 - d20 * d13;
                d23 = d20 * d12 - d18 * d14;
                d24 = d18 * d13 - d19 * d12;
                d25 = d19 * d8 - d20 * d7;
                d26 = d20 * d6 - d18 * d8;
                d27 = d18 * d7 - d19 * d6;
                break;

            case 116: // 't'
                d21 = d19 / d18;
                d22 = d19 * d17 - d20 * d16;
                d23 = d20 * d15 - d18 * d17;
                d24 = d18 * d16 - d19 * d15;
                d25 = d19 * d11 - d20 * d10;
                d26 = d20 * d9 - d18 * d11;
                d27 = d18 * d10 - d19 * d9;
                break;
        }
        for (int l1 = rectangle.y; l1 < rectangle.y + rectangle.height; l1++) {
            int i2 = l1 * i;
            d28 += d29;

            // System.out.print( (int) (d28 * 100) + " %, " );
            // IJ.showProgress(d28);

            for (int j2 = rectangle.x; j2 < rectangle.x + rectangle.width; j2++) {
                int k = i2 + j2;
                int l = image.getRGB(j2, l1);
                double d3 = (float) ((l & 0xff0000) >> 16);
                double d4 = (float) ((l & 0xff00) >> 8);
                double d5 = (float) (l & 0xff);
                d3 = Math.pow(d3 / 255D, ad[0]);
                d4 = Math.pow(d4 / 255D, ad[1]);
                d5 = Math.pow(d5 / 255D, ad[2]);
                double d = d3 * ad1[0] + d4 * ad1[1] + d5 * ad1[2];
                double d1 = d3 * ad1[3] + d4 * ad1[4] + d5 * ad1[5];
                double d2 = d3 * ad1[6] + d4 * ad1[7] + d5 * ad1[8];
                switch (c) {
                    case 112: // 'p'
                        if (d2 / d1 < d21) {
                            d = -(d23 * d1 + d24 * d2) / d22;
                        } else {
                            d = -(d26 * d1 + d27 * d2) / d25;
                        }
                        break;

                    case 100: // 'd'
                        if (d2 / d < d21) {
                            d1 = -(d22 * d + d24 * d2) / d23;
                        } else {
                            d1 = -(d25 * d + d27 * d2) / d26;
                        }
                        break;

                    case 116: // 't'
                        if (d1 / d < d21) {
                            d2 = -(d22 * d + d23 * d1) / d24;
                        } else {
                            d2 = -(d25 * d + d26 * d1) / d27;
                        }
                        break;

                    default:
                        log.error(c + ": Unknow color deficit!");
                        break;
                }
                d3 = d * ad2[0] + d1 * ad2[1] + d2 * ad2[2];
                d4 = d * ad2[3] + d1 * ad2[4] + d2 * ad2[5];
                d5 = d * ad2[6] + d1 * ad2[7] + d2 * ad2[8];
                if (d3 < 0.0D) {
                    d3 = 0.0D;
                }
                if (d4 < 0.0D) {
                    d4 = 0.0D;
                }
                if (d5 < 0.0D) {
                    d5 = 0.0D;
                }
                d3 = Math.pow(d3, ad3[0]) * 255D;
                d4 = Math.pow(d4, ad3[1]) * 255D;
                d5 = Math.pow(d5, ad3[2]) * 255D;
                if (d3 > 255D) {
                    d3 = 255D;
                }
                if (d4 > 255D) {
                    d4 = 255D;
                }
                if (d5 > 255D) {
                    d5 = 255D;
                }
                int i1 = (int) Math.round(d3);
                int j1 = (int) Math.round(d4);
                int k1 = (int) Math.round(d5);
                image.setRGB(j2, l1, ((i1 & 0xff) << 16) + ((j1 & 0xff) << 8) + (k1 & 0xff));
            }
        }

        return image;
    }
}
