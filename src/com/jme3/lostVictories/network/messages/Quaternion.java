/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import java.io.Serializable;

/**
 *
 * @author dharshanar
 */
public class Quaternion implements Serializable{
    float x;
    float y;
    float z;
    float w;
    
    public Quaternion(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
}
