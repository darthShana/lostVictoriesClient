/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import com.jme3.math.Vector3f;
import java.io.Serializable;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author dharshanar
 */
public class Vector implements Serializable{
   	
    public float x;
    public float y;
    public float z;

    public Vector(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
    }

    @JsonCreator
    public Vector(@JsonProperty("x")float x, @JsonProperty("y")float y, @JsonProperty("z")float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vector(Vector3f v){
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

     @Override
    public String toString() {
            return x+","+y+","+z;
    }
     
    public Vector3f toVector(){
        return new Vector3f(x, y, z);
    }
 
}
