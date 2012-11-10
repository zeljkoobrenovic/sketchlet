/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.communicator.server.tcp;

import java.net.Socket;

/**
 * @author zobrenovic
 */
public class Test {

    public static void main(String args[]) throws Exception {
        Socket s = new Socket("localhost", 3320);
        //s.getOutputStream().write(new String("SET x 210\r\n").getBytes());
        //s.getOutputStream().write(new String("GOTO Sketch 10\r\n").getBytes());
        s.getOutputStream().write(new String("START TIMER Timer 1\r\n").getBytes());
        s.getOutputStream().write(new String("IMAGE TRANSLATE 10 10\r\n").getBytes());
        s.getOutputStream().write(new String("IMAGE SETCOLOR 0 255 0\r\n").getBytes());
        s.getOutputStream().write(new String("IMAGE DRAWLINE 1 1 500 500\r\n").getBytes());
        s.getOutputStream().write(new String("IMAGE DRAWRECT 1 1 500 500\r\n").getBytes());
    }
}
