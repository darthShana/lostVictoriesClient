/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.weapons;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.LoopMode;
import com.jme3.effect.ParticleEmitter;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author dharshanar
 */
public class Rifle extends Weapon{
    public static Vector3f muzzelLocation = new Vector3f(0f, 1.55f, 1.4f);
    
    protected Rifle() {
        super("rifle_aimAction", "rifle_shootAction", "rifle_standDownAction", "rifle_idleAction");
    }

    
    
    @Override
    public float getMaxRange(){
        return 100;
    }

    @Override
    public float getPartiplesPerSecond() {
        return 0;
    }

    @Override
    public Vector3f getMuzzelLocation() {
        return muzzelLocation;
    }

    @Override
    public boolean canMoveDuringSetup() {
        return true;
    }

    @Override
    public boolean isReadyToShoot(String animationName, Vector3f playerDirection, Vector3f aimingDirection) {
        return !isInFiringSequence(animationName);
    }

    @Override
    public boolean isAboutToFire(String animName) {
        return animName.contains("aimAction");
    }
    
    @Override
    public boolean hasFiredProjectile(String animName) {
        return animName.contains("aimAction");
    }

    @Override
    public boolean isInFiringSequence(String animationName) {
        return animationName.contains("aimAction") || animationName.contains("shootAction");
    }

    @Override
    public String getDieAtion(GameAnimChannel channel) {
        return getName()+"_dieAction";
    }

    @Override
    public boolean isStanding(GameAnimChannel channel) {
        return true;
    }

    @Override
    public Quaternion getSetupRotation() {
        return null;
    }

    @Override
    public Vector3f getSetupTranslation() {
        return null;
    }

    @Override
    public boolean isProjectileWeapon() {
        return false;
    }

    @Override
    public boolean canShootMultipleTargets() {
        return false;
    }

    @Override
    public boolean isWithinFieldOfVision(Vector3f playerDirection, Vector3f aimingDirection) {
        return true;
    }

    @Override
    public String getName() {
        return "rifle";
    }
    
   @Override
    public void removeUnusedWeapons(Node node) {
        final Spatial c2 = node.getChild("Mortar");
        if(c2!=null){
            c2.removeFromParent();
        }
        final Spatial c4 = node.getChild("Mg42");
        if(c4!=null){
            c4.removeFromParent();
        }
        final Spatial c7 = node.getChild("Missile");
        if(c7!=null){
            c7.removeFromParent();
        }
    }

    
}
