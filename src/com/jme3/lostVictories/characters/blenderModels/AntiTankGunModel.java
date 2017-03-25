/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.blenderModels;

import com.jme3.animation.AnimChannel;
import com.jme3.animation.LoopMode;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dharshanar
 */
public class AntiTankGunModel extends VehicleBlenderModel{
    private static Vector3f operatorTranslation = new Vector3f(-.2f, -.1f, -1.75f);
    private static Vector3f embarkationPoint = new Vector3f(0, 0, -3f);
    private static Vector3f modelBounds = new Vector3f(.9f, .5f, 2.25f);
    private static Vector3f bustTranslation = new Vector3f(0, -1.5f, -6.5f);

    public AntiTankGunModel(String modelPath, float walkSpeed, Weapon weapon) {
        super(modelPath, walkSpeed, weapon);
    }
    
    @Override
    public float getModelScale() {
        return .75f;
    }

    @Override
    public Vector3f getOperatorTranslation() {
        return operatorTranslation;
    }

    @Override
    public String getOperatorIdleAnimation() {
        return "idleAction";
    }

    @Override
    public Vector3f getEmbarkationPoint() {
        return embarkationPoint;
    }

    @Override
    public Vector3f getModelBounds() {
        return modelBounds;
    }
    
    @Override
    public Vector3f getBustTranslation() {
        return bustTranslation;
    }

    @Override
    public void startFiringSequence(GameAnimChannel channel) {
        channel.setAnim(weapon.getFiringSequence().getFirst(), LoopMode.DontLoop, weapon.getDefaultFiringSpeend());
    }
    
    @Override
    public void doSetupAction(GameAnimChannel channel) {
        if(!weapon.canMoveDuringSetup()){
            channel.setAnim("aimAction", LoopMode.DontLoop, 1.5f);
        }
        
    }

    @Override
    public String getIdleAnimation() {
        return getOperatorIdleAnimation();
    }
    
    @Override
    public void doDieAction(GameAnimChannel channel) {}
        
    @Override
    public void dropDetachableWeapons(Node geometry) {}

    @Override
    public String getMeshName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
        @Override
    public List<Vector3f> getFrontWheels() {
        float radius = 0.5f;
        List<Vector3f> ret = new ArrayList<Vector3f>();        
        ret.add(new Vector3f(getModelBounds().x-(radius * 0.7f), 0.5f, getModelBounds().z-(radius*2.5f)));
        ret.add(new Vector3f(-getModelBounds().x+(radius * 0.7f), 0.5f, getModelBounds().z-(radius*2.5f)));
        return ret;
    }

    @Override
    public List<Vector3f> getBackWheels() {
        float radius = 0.5f;
        List<Vector3f> ret = new ArrayList<Vector3f>();        
        ret.add(new Vector3f(0, 0.5f, -getModelBounds().z+(radius*2)));
        return ret;
    }
    
    @Override
    public Vector3f getMuzzelLocation() {
        return weapon.getMuzzelLocation();
    }
        
    
}
