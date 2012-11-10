/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.sketchlet.designer.ui.wizard;

import net.sf.sketchlet.util.SpringUtilities;
import org.netbeans.spi.wizard.WizardPage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ActionPage extends WizardPage {

    JRadioButton gotoSketch = new JRadioButton("Go To Page");
    JRadioButton varUpdate = new JRadioButton("Update Variable");
    JRadioButton varIncrement = new JRadioButton("Increment Variable");
    JRadioButton varAppend = new JRadioButton("Append to Variable");
    JRadioButton regionChange = new JRadioButton("Change Region Properties");
    JRadioButton regionAnimate = new JRadioButton("Animate Region");
    JRadioButton timerStart = new JRadioButton("Start Timer");
    JRadioButton timerPause = new JRadioButton("Pause Timer");
    JRadioButton timerStop = new JRadioButton("Stop Timer");
    JRadioButton macroStart = new JRadioButton("Start Action");
    JRadioButton macroStop = new JRadioButton("Stop Action");
    ButtonGroup group = new ButtonGroup();
    ActionParamPage actionParamPage;

    public ActionPage(final ActionParamPage actionParamPage) {
        this.actionParamPage = actionParamPage;
        setLayout(new SpringLayout());

        group.add(gotoSketch);
        group.add(varUpdate);
        group.add(varIncrement);
        group.add(varAppend);
        group.add(regionChange);
        group.add(regionAnimate);
        group.add(timerStart);
        group.add(timerPause);
        group.add(timerStop);
        group.add(macroStart);
        group.add(macroStop);

        add(gotoSketch);
        add(new JLabel(" "));
        add(varUpdate);
        add(varIncrement);
        add(varAppend);
        add(new JLabel(" "));
        add(regionChange);
        add(regionAnimate);
        add(new JLabel(" "));
        add(timerStart);
        add(timerPause);
        add(timerStop);
        add(new JLabel(" "));
        add(macroStart);
        add(macroStop);

        gotoSketch.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionParamPage.setPanel(0);
            }
        });
        varUpdate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionParamPage.setPanel(1);
            }
        });
        varIncrement.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionParamPage.setPanel(2);
            }
        });
        varAppend.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionParamPage.setPanel(3);
            }
        });
        regionChange.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionParamPage.setPanel(4);
            }
        });
        regionAnimate.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionParamPage.setPanel(5);
            }
        });
        timerStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionParamPage.setPanel(6);
            }
        });
        timerStop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionParamPage.setPanel(7);
            }
        });
        timerPause.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionParamPage.setPanel(8);
            }
        });
        macroStart.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionParamPage.setPanel(9);
            }
        });
        macroStop.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                actionParamPage.setPanel(10);
            }
        });


        SpringUtilities.makeCompactGrid(this,
                15, 1, //rows, cols
                5, 5, //initialX, initialY
                5, 5);//xPad, yPad
    }

    public static final String getDescription() {
        return "Select action";
    }

    protected String validateContents(Component comp, Object o) {
        if (group.getSelection() == null) {
            return "Select the action";
        } else {
            return null;
        }
    }
}