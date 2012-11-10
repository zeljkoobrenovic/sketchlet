/*
 * UserInfo.java
 *
 * Created on April 11, 2008, 3:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.sf.sketchlet.common.mail;

/**
 *
 * @author cuypers
 */
public class UserInfo {
    public String username;
    public String password;
    
    public UserInfo(String u, String p) {
        username = u;
        password = p;
    }
}