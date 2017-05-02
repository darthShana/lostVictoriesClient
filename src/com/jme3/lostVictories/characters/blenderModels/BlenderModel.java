/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters.blenderModels;

import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.LoopMode;
import com.jme3.effect.ParticleEmitter;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.List;

/**
 *
 * @author dharshanar
 */
public abstract class BlenderModel {
    
    private final String modelPath;
    private final String materialPath;
    private final float walkSpeed;
    protected final Weapon weapon;
    private static Vector3f bustTranslation = new Vector3f(0, -1.5f, -2.5f);

    public BlenderModel(String modelPath, String materialPath, float walkSpeed, Weapon weapon) {
        this.walkSpeed = walkSpeed;
        this.modelPath = modelPath;
        this.materialPath = materialPath;
        this.weapon = weapon;
    }
    
    public BlenderModel(String modelPath, float walkSpeed, Weapon weapon) {
        this.walkSpeed = walkSpeed;
        this.modelPath = modelPath;
        this.materialPath = null;
        this.weapon = weapon;
    }

    public boolean isAlreadyFiring(GameAnimChannel channel) {
        return channel!=null && weapon.isInFiringSequence(channel.getAnimationName());
    }

    public boolean isReadyToShoot(GameAnimChannel channel, Vector3f playerDirection, Vector3f aimingDirection) {
        if("embark_vehicle".equals(channel.getAnimationName())){
            return false;
        }
        return weapon.isReadyToShoot(channel.getAnimationName(), playerDirection, aimingDirection.normalize());
    }

    public void startFiringSequence(GameAnimChannel channel) {
        channel.setAnim(weapon.getFiringSequence().getFirst(), LoopMode.DontLoop, weapon.getDefaultFiringSpeend());
    }

    public boolean isAboutToFire(String animName) {
        return weapon.isAboutToFire(animName);
    }

    public void doShootAction(AnimChannel channel) {
        channel.setAnim(weapon.getName()+"_shootAction");
        channel.setLoopMode(LoopMode.DontLoop);
        channel.setSpeed(1.5f);        
    }
    
    public void doPostSetupEffect(ParticleEmitter smokeTrail, ParticleManager particleManager, Vector3f playerLocation, Vector3f playerDirection, List<Ray> rays, List<Float> fs) {
        Quaternion q = new Quaternion();
        q.lookAt(playerDirection, Vector3f.UNIT_Y);
        if(Weapon.mg42() == weapon){
            particleManager.playTracerBulletEffect(playerLocation.add(q.mult(getMuzzelLocation())), rays, fs);        
        }else if(Weapon.cannon()== weapon){
            particleManager.playTracerCannonEffect(playerLocation.add(q.mult(getMuzzelLocation())), rays.get(0), 1);
        }else{
            smokeTrail.setLocalTranslation(playerLocation.add(q.mult(getMuzzelLocation())));
            smokeTrail.getParticleInfluencer().setInitialVelocity(rays.get(0).getDirection().normalize().mult(300));
            smokeTrail.emitAllParticles();
//            smokeTrail.getParticles()[0].angle = q.toAngleAxis(Vector3f.UNIT_Y);
        }
    }

    public void transitionFireingSequence(GameAnimChannel channel, ParticleEmitter emitter) {
        weapon.transitionFiringSequence(channel, emitter);
    }

    public boolean hasFinishedFiring(String animName) {
        return animName.contains(weapon.getFiringSequence().getLast());
    }
    
    public void doWalkAction(GameAnimChannel channel, Geometry shell) {
        if(weapon.getFiringSequence().contains(channel.getAnimationName()) && weapon.getSetupRotation()!=null){
            shell.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
            shell.setLocalTranslation(0, 1.75f, 0);
        }
        channel.setAnim(weapon.getName()+"_walkAction", 0.50f, LoopMode.Loop, walkSpeed, (float) (channel.getAnimMaxTime()*Math.random()));
    }
    
    public void doCrouchAction(GameAnimChannel channel, Geometry shell) {
        shell.setLocalTranslation(0, .4f, 0);
        channel.setAnim("rifle_crouchAction", 0.50f, LoopMode.DontLoop, 1.5f, null);   
    }
    
    public void doStandAction(GameAnimChannel channel, Geometry shell) {
        shell.setLocalTranslation(0, .95f, 0);
        channel.setAnim(weapon.getName()+"_idleAction", 0.50f, LoopMode.DontLoop, .2f, null); 
    }
    
    public void doRunAction(GameAnimChannel channel, Geometry shell) {
        if(weapon.getFiringSequence().contains(channel.getAnimationName()) && weapon.getSetupRotation()!=null){
            shell.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
            shell.setLocalTranslation(0, 1.75f, 0);
        }
        channel.setAnim(weapon.getName()+"_runAction", 0.50f, LoopMode.Loop, .50f, (float) (channel.getAnimMaxTime()*Math.random()));

    }

    public void doReverseWalkAction(GameAnimChannel channel) {
        channel.setAnim(weapon.getName()+"_walkAction", 0.50f, LoopMode.Loop, walkSpeed, null);
    }
    
    public void doSetupAction(GameAnimChannel channel) {
        if(channel!=null && !weapon.canMoveDuringSetup()){
            channel.setAnim(weapon.getName()+"_aimAction", LoopMode.DontLoop);
        }
        
    }
    
    public void doSetupShellAdjustment(Geometry shell){
        if(weapon.getSetupRotation()!=null){
            shell.setLocalRotation(weapon.getSetupRotation());
            shell.setLocalTranslation(weapon.getSetupTranslation());
        }
    }

    public String getModelPath() {
        return modelPath;
    }
    
    public String getMaterialPath(){
        return materialPath;
    }

    public abstract Vector3f getMuzzelLocation();

    public Weapon getWeapon() {
        return weapon;
    }

    public float getMaxRange() {
        return weapon.getMaxRange();
    }

    public boolean canShootWithoutSetup() {
        return weapon.canMoveDuringSetup();
    }

    public boolean hasPlayedDeathAction(String animName) {
        return animName.contains("dieAction") || animName.contains("dieStandingAction") || animName.contains("dieShootingAction");
    }

    public void doDieAction(GameAnimChannel channel) {
        if(channel!=null && !hasPlayedDeathAction(channel.getAnimationName())){
            channel.setAnim(weapon.getDieAtion(channel), 0.50f, LoopMode.DontLoop, null, null);
        }        
    }

    public boolean isStanding(GameAnimChannel channel) {
        return weapon.isStanding(channel);
    }

    public boolean isWeapon(Weapon weapon) {
        return this.weapon == weapon;
    }

    public boolean hasPlayedSetupAction(String animName) {
        return animName.contains("aimAction") || animName.contains("standByAction");
    }

    public boolean hasPlayedTearDownAction(String animName) {
        return animName.contains("standDownAction");
    }

    public boolean isProjectileWeapon() {
        return weapon.isProjectileWeapon();
    }

    public void doBlowUpAction(AnimChannel channel, Vector3f playerDirection, Vector3f blast) {
        channel.setAnim("flyForwardAction", 0.50f);
        channel.setLoopMode(LoopMode.DontLoop);
        channel.setSpeed(1.5f);
    }

    public boolean canShootMultipleTargets() {
        return weapon.canShootMultipleTargets();
    }

    public float getModelScale() {
        return .25f;
    }

    public String getIdleAnimation() {
        return weapon.getName()+"_idleAction";
    }

//    public Vector3f getModelBounds() {
//        return new Vector3f(1, 1, 1);
//    }
  
    public void dropDetachableWeapons(Node geometry) {
        Weapon.rifle().removeUnusedWeapons(geometry);
    }

    public Vector3f getBustTranslation() {
        return bustTranslation;
    }

    public Vector3f getModelTranslation() {
        return Vector3f.ZERO;
    }

}
