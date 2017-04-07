/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages.actions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.Vector;
import java.io.Serializable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author dharshanar
 */
@JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY, property="class")
public abstract class Action {
    
    protected final String type;

    public Action(String type) {
        this.type = type;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj == null) { return false; }
      if (obj == this) { return true; }
      if (obj.getClass() != getClass()) {
        return false;
      }
      Action rhs = (Action) obj;
      return new EqualsBuilder()
        .append(type, rhs.type)
        .isEquals();
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(15, 35)
            .append(type)
          .toHashCode();
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
