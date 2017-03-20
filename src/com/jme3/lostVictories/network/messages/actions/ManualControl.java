/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages.actions;

/**
 *
 * @author dharshanar
 */
public class ManualControl extends Action{
    private final String gear;
    private final String steering;

    public ManualControl(String gear, String steering) {
        super("manualControl");
        this.gear = gear;
        this.steering = steering;
    }

    public String getGear() {
        return gear;
    }

    public String getSteering() {
        return steering;
    }
    
}
