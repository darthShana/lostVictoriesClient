/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.characters.physicsControl.BetterVehicleControl;
import com.jme3.lostVictories.objectives.StearToTarget;
import com.jme3.lostVictories.objectives.NavigateObjective;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.LoopMode;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.NetworkClientAppState;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.blenderModels.VehicleBlenderModel;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.objectives.FollowCommander;
import com.jme3.lostVictories.objectives.VehicleCoverObjective;
import com.jme3.lostVictories.structures.Pickable;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;


import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public abstract class GameVehicleNode extends AICharacterNode<BetterVehicleControl>{

    final static Quaternion flat = new Quaternion();
    public static final float INCLINE_ADJUSTMENT = FastMath.QUARTER_PI/16;
    static {
        flat.fromAngleAxis(0, Vector3f.UNIT_X);
    }
    
    protected Map<Country, GameAnimChannel> operatorChannel = new HashMap<Country, GameAnimChannel>();
    protected Set<GameCharacterNode> passengers = new HashSet<GameCharacterNode>();
    protected final Map<Country, Node> operator;
    private GameCharacterNode shooter;
    Float prevIncline;
    private boolean collisionWithUnmovableObject;
    private Vector3f collisionPoint;
    boolean finishedGunnerDeath;
    
    
    public GameVehicleNode(UUID id, Node model, Map<Country, Node> operator, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter emitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, VehicleBlenderModel m, BehaviorControler behaviorControler, Camera camera) {
        super(id, model, country, commandingOfficer, worldCoodinates, rotation, rootNode, bulletAppState, emitter, particleManager, pathFinder, assetManager, m, behaviorControler, camera);
        shell.setLocalTranslation(shell.getLocalTranslation().add(m.getOperatorTranslation()));
        this.operator = operator;
        
        for(Entry<Country, Node> n:operator.entrySet()){
            n.getValue().setLocalTranslation(m.getOperatorTranslation());
            final AnimControl control1 = n.getValue().getControl(AnimControl.class);
            if(control1!=null){
                operatorChannel.put(n.getKey(), new GameAnimChannel(control1.createChannel(), m));
//                SkeletonControl skeletonControl = n.getValue().getControl(SkeletonControl.class);
//                skeletonControl.setHardwareSkinningPreferred(false);
                control1.addListener(this);
            }
        }
        characterNode.attachChild(operator.get(country));
        
//        try{
//            playerControl.applyBreak();
//        }catch(Exception e){}
        setName(getCountry()+":"+getClass());
    }

    @Override
    public void simpleUpate(float tpf, WorldMap map, Node rootNode) {
        super.simpleUpate(tpf, map, rootNode);
    }

    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        super.onAnimCycleDone(control, channel, animName);
        //remove shooter null check and fix properly
        if(shooter!=null && "gunnerDeathAction".equals(animName) && !"explodeAction".equals(channel.getAnimationName())){
            NetworkClientAppState.get().notifyGunnerDeath(shooter.getIdentity(), this.getIdentity());
            finishedGunnerDeath = true;
        }
    }
            
    @Override
    public void travel(Vector3f contactPoint, GameCharacterNode issuingCharacter) {   
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        if(issuingCharacter instanceof AvatarCharacterNode){
            ((AvatarCharacterNode)issuingCharacter).clearBoardedVecicleControl();
        }       
        try{
            behaviorControler.addObjective(new NavigateObjective(contactPoint, null));
        }catch(Throwable e){}
    }

    @Override
    public void follow(GameCharacterNode toFollow, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        Vector3f f = new Vector3f(5, 0, 5).mult((float)Math.random());
        behaviorControler.addObjective(new FollowCommander(f, 5));
    }

    @Override
    public void attack(Vector3f target, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        
        try{
            behaviorControler.addObjective(new StearToTarget(target));
        }catch(Throwable e){}
    }

    public BetterVehicleControl getCharacterControl() {
        return playerControl;
    }
    
    
    
    public void collect(Pickable pickable, GameCharacterNode issuingCharacter) {
    }

    public void requestBoarding(GameVehicleNode key, GameCharacterNode issuingCharacter) {
    }

    public void requestDisembarkPassengers(GameCharacterNode avatar) {
        if(!passengers.isEmpty()){
            NetworkClientAppState.get().disembarkPassengers(getIdentity());
        }
    }
    
    @Override
    public boolean isAbbandoned() {
        return passengers.isEmpty();
    }
    
    @Override
    public void cover(Vector3f mousePress, Vector3f mouseRelease, GameCharacterNode issuingCharacter) {
        if(!isUnderChainOfCommandOf(issuingCharacter, 5)){
            return;
        }
        behaviorControler.addObjective(new VehicleCoverObjective(this, mousePress, mouseRelease, rootNode));
    }

    @Override
    public boolean shoot(Vector3f... targets) {
        if(isAbbandoned()){
            return false;
        }
        return super.shoot(targets);
    }
    
    @Override
    public boolean takeBullet(CollisionResult result, GameCharacterNode shooter) {
        if(!isAbbandoned() && getChild("operator")!=null && (result.getGeometry().hasAncestor((Node) getChild("operator")) || result.getGeometry().equals(getChild("shell")))){
            if(shooter==null){
                throw new RuntimeException("shooter null");
            }
            this.shooter = shooter;
            doTakeBulletEffects(result.getContactPoint());
        }
        return false;
    }

    @Override
    void doTakeBulletEffects(Vector3f point) {
        if(!isAbbandoned() && !"gunnerDeathAction".equals(operatorChannel.get(country).getAnimationName())){
            bloodDebris.setLocalTranslation(point);
            bloodDebris.emitAllParticles();
            muzzelFlash.killAllParticles();
            muzzelFlash.setEnabled(false);
            if(operatorChannel.get(country)!=null){
                if(shooter==null){
                    new RuntimeException().printStackTrace();
                }
                operatorChannel.get(country).setAnim("gunnerDeathAction", LoopMode.DontLoop, 1.5f);
            }
        }
    }
    
    @Override
    boolean takeLightBlast(GameCharacterNode shooter) {
        if(!isAbbandoned() && !"gunnerDeathAction".equals(operatorChannel.get(country).getAnimationName())){
            this.shooter = shooter;
            operatorChannel.get(country).setAnim("gunnerDeathAction", LoopMode.DontLoop, 1.5f);
            return true;
        }
        return false;
    }
    
    public boolean takeMissile(CollisionResult result, GameCharacterNode shooter) {
        playDistroyAnimation(result.getContactPoint());
        this.shooter = shooter;
        die(shooter);
        return true;
    }

    public void turnLeft() {
        if(!"stayLeftAction".equals(channel.getAnimationName()) && !"leftTurnAction".equals(channel.getAnimationName()) && !model.isAboutToFire(channel.getAnimationName())){
           channel.setAnim("leftTurnAction", LoopMode.DontLoop);
        }
        
    }

    public void turnRight() {
        if(!"stayRightAction".equals(channel.getAnimationName()) && !"rightTurnAction".equals(channel.getAnimationName()) && !model.isAboutToFire(channel.getAnimationName())){
            channel.setAnim("rightTurnAction", LoopMode.DontLoop);
        }
    }

    public void straighten() {
        if(!"forwardAction".equals(channel.getAnimationName()) && !model.isAboutToFire(channel.getAnimationName())){           
            channel.setAnim("forwardAction", LoopMode.Loop);
        }
    }
    
    public void stop(){
        if(!model.isAboutToFire(channel.getAnimationName())){
            channel.setAnim(model.getIdleAnimation(), LoopMode.DontLoop);
        }
    }

    @Override
    public abstract Vector3f getPositionToTarget(GameCharacterNode targetedBy); 

    public abstract float getTurnSpeed();

    protected void addOperator(Country newCountry) {
        removeOperator();
        characterNode.attachChild(operator.get(newCountry));
        
        
    }
    
    protected void removeOperator(){
        if(getChild("operator")!=null){
            getChild("operator").removeFromParent();
        }
        
    }

    public Vector3f getEmbarkationPoint() {
        final Vector3f embarkationPoint = ((VehicleBlenderModel)model).getEmbarkationPoint();
        return getLocalTranslation().add(getLocalRotation().mult(embarkationPoint));
    }

    public abstract float getDriveSpeed();

    @Override
    public void playDistroyAnimation(Vector3f point) {
        particleManager.playExplosionEffect(point);
        
        if(!"explodeAction".equals(channel.getAnimationName())){
            channel.setAnim("explodeAction", LoopMode.DontLoop, 1f);
        }
        if(!isAbbandoned() && getChild("operator")!=null){
            operatorChannel.get(country).setAnimForce("gunnerDeathAction", LoopMode.DontLoop);

        }
    }

    public boolean canBoard(Commandable selectedCharacter) {
        if(selectedCharacter instanceof GameVehicleNode || selectedCharacter instanceof VirtualGameCharacterNode){
            return false;
        }
        return true;
    }

    public boolean hasPassenger(GameCharacterNode n) {
        return passengers.contains(n);
    }

    public void boardPassenger(GameCharacterNode n) {
        passengers.add(n);
        n.boaredVehicle = this;
        if(getChild("operator")==null){
            addOperator(n.getCountry());
        }
        n.boardVehicleAction(this);
        if(n instanceof AvatarCharacterNode){
            ((AvatarCharacterNode)n).clearBoardedVecicleControl();
        }
    }
    
    public float getRadius() {
        return 2.5f;
    }

    public boolean hasEngineDamage() {
        return false;
    }

    protected void disembarkPassenger(GameCharacterNode passenger) {
        passengers.remove(passenger);
        if(passengers.isEmpty()){
            removeOperator();
        }
    }

    public boolean hasCollidedWithUnMovableObject() {
        return collisionWithUnmovableObject;
    }
    
    public Vector3f getColliosionPoint(){
        return collisionPoint;
    }
    
    public void collidedWithUnMovableObject(){
        collisionWithUnmovableObject = true;
        collisionPoint = new Vector3f(getLocalTranslation());
    }

    public void clearCollisionWithUnmovableObject() {
        collisionWithUnmovableObject = false;
    }

    public abstract float getEnginePower();

    public abstract float getMaxStearingAngle();

    public void synchronisePassengers(Set<UUID> passengers) {
       for(Iterator<GameCharacterNode> it = this.passengers.iterator();it.hasNext();){
           if(!passengers.contains(it.next().getIdentity())){
               it.remove();
           }
       }
       if(passengers.isEmpty()){
           removeOperator();
       }else if(finishedGunnerDeath){
           addOperator(country);
           finishedGunnerDeath = false;
       }
       
    }

    public boolean isHuman() {
        return false;
    }

    public void disengageGravityBreak() {
        playerControl.disengageGravityBreak();
    }
    
    

}
