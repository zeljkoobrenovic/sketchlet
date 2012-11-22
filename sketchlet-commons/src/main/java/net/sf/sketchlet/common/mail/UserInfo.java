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
    private String username;
    private String password;
    
    public UserInfo(String u, String p) {
        setUsername(u);
        setPassword(p);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}