/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages.actions;

import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;

/**
 *
 * @author dharshanar
 */
public class Shoot extends Action {
    private final long shootTime;
    private final Vector[] targets;
    
    public Shoot(long shootTime, Vector[] targets) {
        super("shoot");
        this.shootTime = shootTime;
        this.targets = targets;
    }

    public Shoot(long shootStartTime, Vector3f[] currentTargets) {
        super("shoot");
        this.shootTime = shootStartTime;
        this.targets = new Vector[currentTargets.length];
        for(int i = 0;i<currentTargets.length;i++){
            this.targets[i] = new Vector(currentTargets[i].x, currentTargets[i].y, currentTargets[i].z);
        }
    }

    public long getTime() {
        return shootTime;
    }

    public Vector3f[] getTargets() {
        Vector3f[] ret = new Vector3f[targets.length];
        for(int i = 0;i<targets.length;i++){
            ret[i] = new Vector3f(targets[i].x, targets[i].y, targets[i].z);
        }
        return ret;
    }
    
}
