/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import java.io.Serializable;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class UnClaimedEquipmentMessage implements Serializable{
    
    private static final long serialVersionUID = 399807775735308779L;
    private UUID id;
    long version;
    private Weapon weapon;
    private Vector location;
    private Vector rotation;

    public UUID getId() {
        return id;
    }
    
    public Vector getLocation(){
        return location;
    }
    
    public Vector getRotation(){
        return rotation;
    }
    
    public Weapon getWeapon(){
        return weapon;
    }
    
}
