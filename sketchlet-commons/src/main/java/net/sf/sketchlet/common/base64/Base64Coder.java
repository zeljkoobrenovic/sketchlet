/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.common.base64;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class Base64Coder {

// Mapping table from 6-bit nibbles to Base64 characters.
    private static char[] map1 = new char[64];
    private static int kDEFAULT_CHUNK_SIZE = 256;


    static {
        int i = 0;
        for (char c = 'A'; c <= 'Z'; c++) {
            map1[i++] = c;
        }
        for (char c = 'a'; c <= 'z'; c++) {
            map1[i++] = c;
        }
        for (char c = '0'; c <= '9'; c++) {
            map1[i++] = c;
        }
        map1[i++] = '+';
        map1[i++] = '/';
    }

// Mapping table from Base64 characters to 6-bit nibbles.
    private static byte[] map2 = new byte[128];


    static {
        for (int i = 0; i < map2.length; i++) {
            map2[i] = -1;
        }
        for (int i = 0; i < 64; i++) {
            map2[map1[i]] = (byte) i;
        }
    }

    /**
     * Encodes a string into Base64 format.
     * No blanks or line breaks are inserted.
     * @param s  a String to be encoded.
     * @return   A String with the Base64 encoded data.
     */
    public static String encodeString(String s) {
        return new String(encode(s.getBytes()));
    }

    public static String encodeImageFileAsString(String strFile, int width, int height) {
        if (strFile.contains(":/")) {
            return encodeURLAsString(strFile, width, height);
        }
        try {
            File file = new File(strFile);
            FileInputStream is = new FileInputStream(file);

            long length = file.length();
            byte[] bytes = new byte[(int) length];
            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }

            // Close the input stream and return bytes
            is.close();

            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));

            return new String(encode(getCompressedImageBytes(image, width, height)));
        } catch (Exception e) {
        }

        return "";
    }

    public static void write(BufferedImage image, float quality, OutputStream out) throws IOException {
        Iterator writers = ImageIO.getImageWritersBySuffix("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No writers found");
        }
        ImageWriter writer = (ImageWriter) writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(out);
        writer.setOutput(ios);
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (quality >= 0) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
        }
        writer.write(null, new IIOImage(image, null, null), param);
    }

    public static byte[] getBytes(BufferedImage image, float quality) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(50000);
            write(image, quality, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encodeURLAsString(String strURL, int width, int height) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(loadBytesFromURL(new URL(strURL))));

            return new String(encode(getCompressedImageBytes(image, width, height)));

        } catch (Exception e) {
        }

        return "";
    }

    public static byte[] getCompressedImageBytes(BufferedImage image, int width, int height) {
        if ((double) image.getWidth() / width < (double) image.getWidth() / height) {
            height = (int) ((double) image.getHeight() / ((double) image.getWidth() / width));
        } else {
            width = (int) ((double) image.getWidth() / ((double) image.getHeight() / height));
        }

        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g.drawImage(image, 0, 0, width, height, null);

        float comp = 1.0f;
        byte bytes[] = getBytes(resizedImage, comp);

        while (bytes.length > 40000 && comp >= 0.0f) {
            comp -= 0.1;
            bytes = getBytes(resizedImage, comp);
        }

        g.dispose();

        return bytes;
    }

    /**
     * Encodes a byte array into Base64 format.
     * No blanks or line breaks are inserted.
     * @param in  an array containing the data bytes to be encoded.
     * @return    A character array with the Base64 encoded data.
     */
    public static char[] encode(byte[] in) {
        return encode(in, in.length);
    }

    /**
     * Encodes a byte array into Base64 format.
     * No blanks or line breaks are inserted.
     * @param in   an array containing the data bytes to be encoded.
     * @param iLen number of bytes to process in <code>in</code>.
     * @return     A character array with the Base64 encoded data.
     */
    public static char[] encode(byte[] in, int iLen) {
        int oDataLen = (iLen * 4 + 2) / 3;       // output length without padding
        int oLen = ((iLen + 2) / 3) * 4;         // output length including padding
        char[] out = new char[oLen];
        int ip = 0;
        int op = 0;
        while (ip < iLen) {
            int i0 = in[ip++] & 0xff;
            int i1 = ip < iLen ? in[ip++] & 0xff : 0;
            int i2 = ip < iLen ? in[ip++] & 0xff : 0;
            int o0 = i0 >>> 2;
            int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
            int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
            int o3 = i2 & 0x3F;
            out[op++] = map1[o0];
            out[op++] = map1[o1];
            out[op] = op < oDataLen ? map1[o2] : '=';
            op++;
            out[op] = op < oDataLen ? map1[o3] : '=';
            op++;
        }
        return out;
    }

    /**
     * Decodes a string from Base64 format.
     * @param s  a Base64 String to be decoded.
     * @return   A String containing the decoded data.
     * @throws   IllegalArgumentException if the input is not valid Base64 encoded data.
     */
    public static String decodeString(String s) {
        return new String(decode(s));
    }

    /**
     * Decodes a byte array from Base64 format.
     * @param s  a Base64 String to be decoded.
     * @return   An array containing the decoded data bytes.
     * @throws   IllegalArgumentException if the input is not valid Base64 encoded data.
     */
    public static byte[] decode(String s) {
        return decode(s.toCharArray());
    }

    /**
     * Decodes a byte array from Base64 format.
     * No blanks or line breaks are allowed within the Base64 encoded data.
     * @param in  a character array containing the Base64 encoded data.
     * @return    An array containing the decoded data bytes.
     * @throws    IllegalArgumentException if the input is not valid Base64 encoded data.
     */
    public static byte[] decode(char[] in) {
        int iLen = in.length;
        if (iLen % 4 != 0) {
            throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
        }
        while (iLen > 0 && in[iLen - 1] == '=') {
            iLen--;
        }
        int oLen = (iLen * 3) / 4;
        byte[] out = new byte[oLen];
        int ip = 0;
        int op = 0;
        while (ip < iLen) {
            int i0 = in[ip++];
            int i1 = in[ip++];
            int i2 = ip < iLen ? in[ip++] : 'A';
            int i3 = ip < iLen ? in[ip++] : 'A';
            if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            int b0 = map2[i0];
            int b1 = map2[i1];
            int b2 = map2[i2];
            int b3 = map2[i3];
            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            int o0 = (b0 << 2) | (b1 >>> 4);
            int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
            int o2 = ((b2 & 3) << 6) | b3;
            out[op++] = (byte) o0;
            if (op < oLen) {
                out[op++] = (byte) o1;
            }
            if (op < oLen) {
                out[op++] = (byte) o2;
            }
        }
        return out;
    }

    public static byte[] loadBytesFromURL(URL url) throws IOException {
        byte[] b = null;
        URLConnection con = url.openConnection();
        int size = con.getContentLength();
        InputStream in = null;

        try {
            if ((in = con.getInputStream()) != null) {
                b = (size != -1) ? loadBytesFromStreamForSize(in, size) : loadBytesFromStream(in);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                }
            }
        }
        return b;
    }

    public static byte[] loadBytesFromStreamForSize(InputStream in, int size)
            throws IOException {
        int count, index = 0;
        byte[] b = new byte[size];

        // read in the bytes from input stream
        while ((count = in.read(b, index, size)) > 0) {
            size -= count;
            index += count;
        }
        return b;
    }

    public static byte[] loadBytesFromStream(InputStream in) throws IOException {
        return loadBytesFromStream(in, kDEFAULT_CHUNK_SIZE);
    }

    public static byte[] loadBytesFromStream(InputStream in, int chunkSize)
            throws IOException {
        if (chunkSize < 1) {
            chunkSize = kDEFAULT_CHUNK_SIZE;
        }

        int count;
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        byte[] b = new byte[chunkSize];
        try {
            while ((count = in.read(b, 0, chunkSize)) > 0) {
                bo.write(b, 0, count);
            }
            byte[] thebytes = bo.toByteArray();
            return thebytes;
        } finally {
            bo.close();
            bo = null;
        }
    }

// Dummy constructor.
    private Base64Coder() {
    }
} // end class Base64Coder
