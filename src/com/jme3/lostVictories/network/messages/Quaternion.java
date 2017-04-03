/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonCreator
    public Quaternion(@JsonProperty("x")float x, @JsonProperty("y")float y, @JsonProperty("z")float z, @JsonProperty("w")float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
}
