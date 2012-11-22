/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.util;

/**
 *
 * @author zobrenovic
 */
public class RefreshTime {

    private static long lastRefreshed = 0;
    private static long expirationTimeMs = 1000;

    public static boolean shouldRefresh() {
        return System.currentTimeMillis() - lastRefreshed < expirationTimeMs;
    }
    
    public static void update() {
        lastRefreshed = System.currentTimeMillis();
    }

    public static void setExpirationTimeMs(long time) {
        expirationTimeMs = time;
    }
}
