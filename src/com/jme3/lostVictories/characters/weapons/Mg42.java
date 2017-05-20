/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.weapons;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.LoopMode;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 *
 * @author dharshanar
 */
public class Mg42 extends Weapon{

    public Quaternion setupRotation = new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_Z);
    
    protected Mg42() {
        super("mg42_readyToShootAction", "mg42_shootAction", "mg42_standByAction");
    }
    
    
    
    @Override
    public float getMaxRange(){
            return 125;
        }

    @Override
    public float getPartiplesPerSecond() {
        return 20;
    }

    @Override
    public Vector3f getMuzzelLocation() {
        return new Vector3f(0f, .3f, 2.25f);
    }

    @Override
    public boolean canMoveDuringSetup() {
        return false;
    }

    @Override
    public boolean isReadyToShoot(String animationName, Vector3f playerDirection, Vector3f aimingDirection) {
        return animationName.contains("standByAction") && isWithinFieldOfVision(playerDirection, aimingDirection);
    }

    @Override
    public boolean isAboutToFire(String animName) {
        return animName.contains("readyToShootAction");
    }
    
    @Override
    public boolean hasFiredProjectile(String animName) {
        return animName.contains("readyToShootAction");
    }

    @Override
    public boolean isInFiringSequence(String animationName) {
        return animationName.contains("readyToShootAction") || animationName.contains("shootAction");
    }

    @Override
    public String getDieAtion(GameAnimChannel channel) {
        if(getFiringSequence().contains(channel.getAnimationName())){
            return getName()+"_dieShootingAction";
        }else{
            return getName()+"_dieStandingAction";
        }
    }

    @Override
    public boolean isStanding(GameAnimChannel channel) {
        return getFiringSequence().contains(channel.getAnimationName());
    }

    @Override
    public Quaternion getSetupRotation() {
        return setupRotation;
    }

    @Override
    public Vector3f getSetupTranslation() {
        return new Vector3f(0, 0.125f, 0);
    }

    @Override
    public boolean isProjectileWeapon() {
        return false;
    }

    @Override
    public boolean canShootMultipleTargets() {
        return true;
    }
    
    @Override
    public boolean isWithinFieldOfVision(Vector3f playerDirection, Vector3f aimingDirection) {
        Vector2f v1 = new Vector2f(playerDirection.x, playerDirection.z);
        Vector2f v2 = new Vector2f(aimingDirection.x, aimingDirection.z);
        return v1.smallestAngleBetween(v2) < (FastMath.QUARTER_PI / 2);
    }

    @Override
    public String getName() {
        return "mg42";
    }
        
    @Override
    public void removeUnusedWeapons(Node node) {
        final Spatial c2 = node.getChild("Mortar");
        if(c2!=null){
            c2.removeFromParent();
        }
        final Spatial c7= node.getChild("Missile");
        if(c7!=null){
            c7.removeFromParent();
        }
        final Spatial c11= node.getChild("Rifle");
        if(c11!=null){
            c11.removeFromParent();
        }
        
    }

}
