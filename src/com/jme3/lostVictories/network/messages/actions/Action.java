/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages.actions;

import com.jme3.lostVictories.network.messages.Vector;
import java.io.Serializable;

/**
 *
 * @author dharshanar
 */
public abstract class Action implements Serializable{
    
    protected final String type;

    public Action(String type) {
        this.type = type;
    }
    
    
    
    public static Action idle(){
        return new Idle();
    }
    public static Action move(){
        return new Move();
    }
    public static Action crouch(){
        return new Crouch();
    }
    public static Action shoot(long shootTime, Vector[] targets){
        return new Shoot(shootTime, targets);
    }

    public String getType() {
        return type;
    }    
    
}
