/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;


import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.ai.steering.Obstacle;
import com.jme3.animation.AnimChannel;
import com.jme3.animation.AnimControl;
import com.jme3.animation.AnimEventListener;
import com.jme3.animation.LoopMode;
import com.jme3.animation.SkeletonControl;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.lostVictories.CharcterParticleEmitter;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.NetworkClientAppState;
import com.jme3.lostVictories.CanInteractWith;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.lostVictories.ShotsFiredListener;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.WorldMap;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.actions.Action;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.SquadType;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.actions.Crouch;
import com.jme3.lostVictories.network.messages.actions.SetupWeapon;
import com.jme3.lostVictories.network.messages.actions.Shoot;
import com.jme3.lostVictories.objectives.EnemyActivityReport;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Cylinder;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public abstract class GameCharacterNode<T extends GameCharacterControl> extends Node implements Commandable, AnimEventListener, CanInteractWith, Obstacle{
    protected T playerControl;
    protected CharacterAction characterAction = new CharacterAction();
    protected Node rootNode;
    protected Node characterNode = new Node();
    protected AnimControl control;
    protected BlenderModel model;
    protected Node geometry;
    
    protected ParticleEmitter muzzelFlash;
    protected final ParticleEmitter smokeTrail;
    protected final ParticleEmitter bloodDebris;
    protected final ParticleEmitter bulletFragments;
    protected final ParticleEmitter blastFragments;
    protected final ParticleManager particleManager;
    protected final NavigationProvider pathFinder;
    
    protected Country country;
    protected Geometry selectionMarker;
    protected CommandingOfficer commandingOfficer;
    protected GameAnimChannel channel;
    protected boolean isDead;
    
    protected final BulletAppState bulletAppState;
    protected final AssetManager assetManager;
    protected List<Ray> rays = new ArrayList<Ray>();
    protected List<Vector3f> blasts = new ArrayList<Vector3f>();
    protected String unitName;
    
    Set<UUID> kills = new HashSet<UUID>();
    protected final UUID identity;
    protected final Geometry shell;
    protected long shootStartTime;
    private Vector3f[] currentTargets;
    GameVehicleNode boaredVehicle;
    private long version;

    GameCharacterNode(UUID id, Node model, Country country, CommandingOfficer commandingOfficer, Vector3f worldCoodinates, Vector3f rotation, Node rootNode, BulletAppState bulletAppState, CharcterParticleEmitter particleEmitter, ParticleManager particleManager, NavigationProvider pathFinder, AssetManager assetManager, BlenderModel m, Camera cam) {
        this.country = country;
        this.commandingOfficer = commandingOfficer;
        this.rootNode = rootNode;
        this.muzzelFlash = particleEmitter.getFlashEmitter();
        this.smokeTrail = particleEmitter.getSmokeTrailEmitter();
        this.bloodDebris = particleEmitter.getBloodEmitter();
        this.bulletFragments = particleEmitter.getBulletFragments();
        this.blastFragments = particleEmitter.getBlastFragments();
        this.particleManager = particleManager;
        this.pathFinder = pathFinder;
        this.bulletAppState = bulletAppState;       
        this.model = m;
        this.geometry = model;
        this.identity = id;
        playerControl=createCharacterControl(assetManager);

        if(Vector3f.ZERO.equals(rotation) || rotation.length()==0){
            rotation = Vector3f.UNIT_Z;
        }
        playerControl.setViewDirection(rotation);
        
        if(geometry.getControl(AnimControl.class)!=null){
            m.getWeapon().removeUnusedWeapons(geometry);
            control = geometry.getControl(AnimControl.class);
            control.addListener(this);
            channel = new GameAnimChannel(control.createChannel(), m);
//            SkeletonControl skeletonControl = geometry.getControl(SkeletonControl.class);
//            skeletonControl.setHardwareSkinningPreferred(false);
            idle();
        }
                
        geometry.setLocalScale(m.getModelScale());
        characterNode.attachChild(geometry);
        Cylinder b= new Cylinder(6, 6, .25f, 1.85f, true);       
        shell = new Geometry("shell", b);
        Material mark_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mark_mat.setColor("Color", new ColorRGBA(1, 1, 1, 0));
        mark_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        shell.setQueueBucket(Bucket.Transparent);
        shell.setMaterial(mark_mat);
        shell.setLocalRotation(new Quaternion().fromAngleAxis(FastMath.HALF_PI, Vector3f.UNIT_X));
        shell.setLocalTranslation(0, .95f, 0);

        characterNode.attachChild(shell);
        
        characterNode.attachChild(muzzelFlash);
        muzzelFlash.setLocalTranslation(m.getMuzzelLocation());

        setLocalTranslation(worldCoodinates);
        addControl(playerControl);
        attachChild(characterNode);
        
        setUserData("GameCharacterControl", "GameCharacterControl");
        bulletAppState.getPhysicsSpace().add(playerControl);
        this.assetManager = assetManager;
                
    }
       
    public abstract Map<UUID, Objective> getAllObjectives();
    
    public abstract void addObjective(Objective objective);

    public abstract void cover(Vector3f mousePress, Vector3f mouseRelease, GameCharacterNode issuingCharacter);
    
    public abstract void travel(Vector3f contactPoint, GameCharacterNode issuingCharacter);
    
    public abstract void follow(GameCharacterNode toFollow, GameCharacterNode issuingCharacter);
    
    public abstract void attack(Vector3f target, GameCharacterNode issuingCharacter);
    
    public abstract void planObjectives(WorldMap worldMap);
    
    public abstract void simpleUpate(float tpf, WorldMap map, Node rootNode);


    public abstract boolean isBusy();

    public abstract boolean takeBullet(CollisionResult result, GameCharacterNode shooter);
    abstract void doTakeBulletEffects(Vector3f point);
    
    abstract boolean takeLightBlast(GameCharacterNode shooter);
    public abstract void playDistroyAnimation(Vector3f point);
    
    abstract Set<String> getCompletedObjectives();
    
    public void die(GameCharacterNode killer) {
        doDeathEffects();
        NetworkClientAppState.get().notifyDeath(killer.getIdentity(), this.getIdentity());
    }
    
    public void decomposed() {
        rootNode.detachChild(bloodDebris);
        rootNode.detachChild(bulletFragments);
        rootNode.detachChild(smokeTrail);
        rootNode.detachChild(blastFragments);

        removeControl(playerControl);
        bulletAppState.getPhysicsSpace().remove(playerControl);
        this.removeFromParent();
    }
    
    
        
    public boolean shoot(Vector3f... targets) {
        this.shootStartTime = System.currentTimeMillis();
        this.currentTargets = new Vector3f[targets.length];
        
        for(int i =0;i<targets.length;i++){
            this.currentTargets[i] = new Vector3f(targets[i]);
        }
        
        final Vector3f aimingPosition = getShootingLocation();
        List<Vector3f> aimingDirections = new ArrayList<>();
        for(Vector3f target:targets){
            if(model.isProjectileWeapon()){
                aimingDirections.add(ProjectilePathPlanner.getAimingDirection(aimingPosition, target));
            }else{
                aimingDirections.add(target.subtract(aimingPosition));
            }
        }

        if(!model.isAlreadyFiring(channel) && model.isReadyToShoot(channel, getPlayerDirection(), aimingDirections.get(0))){
            model.startFiringSequence(channel);
            fire(aimingPosition, aimingDirections.toArray(new Vector3f[]{}));
            return true;
        }
        return false; 
                
    }
    
    public void attachToRootNode(){
        rootNode.attachChild(this);
        rootNode.attachChild(bloodDebris);
        rootNode.attachChild(bulletFragments);
        rootNode.attachChild(blastFragments);
        rootNode.attachChild(smokeTrail);
    }

    public boolean isAlliedWith(GameCharacterNode character) {
        return country == character.country;
    }       
    
    public boolean canShootWhileMoving(){
        return model.canShootWithoutSetup();
    }
    
    public boolean canPlayMoveAnimation(String animationName) {
        return !animationName.contains("shootAction") 
                && !animationName.contains("aimAction") 
                && !animationName.contains("setupAction");
    }
    
    @Override
    public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
        
        if("9740bc8a-835d-4fa2-ab2b-6ed8d914e6ef".equals(getIdentity().toString())){
            System.out.println("Remote3:in here test shooting:"+animName);
        }
        
        if(model.isAboutToFire(animName)){
            CollisionResults results = new CollisionResults();
            List<Float> collitionLifes = new ArrayList<>();
            
            try{
                for(Ray ray:rays){
                    CollisionResults r = new CollisionResults();
                    rootNode.collideWith(ray, r);
                    CollisionResult result = getClosestCollisionThatIsntMe(r);

                    if(result!=null && result.getDistance()<model.getMaxRange()){
                        results.addCollision(result);
                        collitionLifes.add(result.getDistance()/300);
                    }else{
                        collitionLifes.add(2f);
                    }
                }
            }catch(Throwable e){}
            
            if(!rays.isEmpty()){
                model.doPostSetupEffect(smokeTrail, particleManager, getLocalTranslation(), getPlayerDirection(), rays, collitionLifes);
            }
            if(isControledLocaly()){
                for(CollisionResult result:results){
                    kills.addAll(doRayDamage(result));
                }            
            }
            ShotsFiredListener.instance().register(getLocalTranslation());
        } 
        model.transitionFireingSequence(this.channel, muzzelFlash);
        
        if(model.hasFinishedFiring(channel.getAnimationName())){
            for(Vector3f blast:blasts){
                blastFragments.killAllParticles();
                blastFragments.setLocalTranslation(blast);
                blastFragments.emitAllParticles();
                kills.addAll(doBlastDamage(blast));
            }
            blasts.clear();
            
            muzzelFlash.killAllParticles();
            muzzelFlash.setEnabled(false);
        }
        
        if(model.hasPlayedSetupAction(animName)){
            model.doSetupShellAdjustment(shell);
        }
        
        if("embark_vehicle".equals(animName)){            
            decomposed();
        }
        if("disembark_vehicle".equals(animName)){
            idle();
        }

    }
    
    
    public Set<UUID> doRayDamage(CollisionResult result) {
        Set<UUID> newKills = new HashSet<UUID>();
        final Geometry tt = result.getGeometry();
        for(Node n = tt.getParent();n!=null;n = n.getParent()){
            if(n.getUserData("GameCharacterControl")!=null){
                //if its just a target do practice target destroyed
                CanInteractWith victim = (CanInteractWith)n;
                if(doWeaponDamage(victim, result)){
                    if(victim instanceof GameCharacterNode){
                        newKills.add(((GameCharacterNode)victim).getIdentity());
                    }
                }

            }
        }
        if(newKills.isEmpty()){
            doTerrainDamage(result);
        }
        return newKills;
    }

    public Set<UUID> doBlastDamage(Vector3f blast) {
        Set<UUID> kk = new HashSet<UUID>();
        for(GameCharacterNode victim: WorldMap.get().getCharactersInBlastRange(blast)){
            if(victim.takeLightBlast(this)){
                kk.add(victim.identity);
            }
        }
        return kk;
    }

    public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
        
    }

    public boolean isDead(){
        return isDead;
    }

    public NavigationProvider getPathFinder() {
        return pathFinder;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
    
    public void setCountry(Country valueOf) {
        this.country = valueOf;
    }

    public String getUnitName() {
        return unitName;
    }
    
    public boolean isMoving() {
        return playerControl.isMoving();
    }
    
    public Weapon getWeapon(){
        return model.getWeapon();
    }
    
    public Vector3f getShootingLocation(){
        final Vector3f muzzelLocation = model.getMuzzelLocation();
        Quaternion q = new Quaternion();
        final Vector3f playerDirection = new Vector3f(getPlayerDirection());
        q.lookAt(playerDirection, Vector3f.UNIT_Y);
        return getLocalTranslation().add(q.mult(muzzelLocation));
    }
    
    public Vector3f getShootingLocation(Vector3f facingDirection){
        Quaternion q = new Quaternion();
        q.lookAt(facingDirection, Vector3f.UNIT_Y);
        q = q.mult(geometry.getLocalRotation());
        return getLocalTranslation().add(q.mult(model.getMuzzelLocation()));
    }

    protected void fire(final Vector3f aimingPosition, Vector3f... aimingDirections) {
        rays.clear();
        
        if(!model.isProjectileWeapon()){
            for(Vector3f aimingDirection:aimingDirections){
                float x = (float) ((Math.random()-0.5) * FastMath.PI / 180) * 2f;
                float z = (float) ((Math.random()-0.5) * FastMath.PI / 180) * 2f;
                aimingDirection = new Quaternion().fromAngleAxis(x, Vector3f.UNIT_Y).mult(aimingDirection);
                aimingDirection = new Quaternion().fromAngleAxis(z, Vector3f.UNIT_X).mult(aimingDirection);
                final Ray ray = new Ray(aimingPosition, aimingDirection.normalize());
                ray.setLimit(getMaxRange());
                rays.add(ray);
            }
        }else{
            Vector3f aimingDirection = aimingDirections[0];
            final Vector3f epicentre = new ProjectilePath(aimingPosition, aimingDirection.normalize(), 50).getEpicentre(rootNode);
            blasts.add(epicentre);
        }               
        
    }

    public CollisionResult getClosestCollisionThatIsntMe(CollisionResults results) {
        if(results.size()<=0){
            return null;
        }
        
        for(CollisionResult r: results){
            if(!r.getGeometry().hasAncestor(this) && !hasBeenKilledAlready(r)){
                return r;
            }
        }
        
        return null;
    }

    private boolean hasBeenKilledAlready(CollisionResult r) {
        for(Node n = r.getGeometry().getParent();n!=null;n = n.getParent()){
            if(n.getUserData("GameCharacterControl")!=null && !"blank".equals(n.getUserData("GameCharacterControl"))){
                if(kills.contains(((GameCharacterNode) n).identity)){
                    return true;
                }
            }
        }
        return false;
        
    }
      
    public int getKillCount(){
        return kills.size();
    }

    public Rank getRank() {
        return Rank.PRIVATE;
    }

    public UUID getIdentity() {
        return identity;
    }
    
    public void doReverseWalkActoin(){
        model.doReverseWalkAction(channel);
    }

    public void setupWeapon(Vector3f direction) {
        model.doSetupAction(channel);
    }
    
    public Vector3f getPlayerDirection(){
        return playerControl.getViewDirection();
    }

    public boolean canShootMultipleTargets() {
        return model.canShootMultipleTargets();
    }
    
    protected boolean hasWeapon(Weapon... weapons) {
        for(Weapon w: weapons){
            if(w == getWeapon()){
                return true;
            }
        }
        return false;
    }
    
    public boolean hasProjectileWeapon() {
        return model.isProjectileWeapon();
    }    
    
    public Country getCountry(){
        return country;
    }
    
    public GameCharacterNode select(Geometry selectionMarker) {
        this.selectionMarker = selectionMarker;
        attachChild(selectionMarker);
        return this;
    }

    public void setCommandingOfficer(CommandingOfficer c) {
        this.commandingOfficer = c;
    }
    public CommandingOfficer getCommandingOfficer(){
        return commandingOfficer;
    }
    
    public final boolean isUnderChainOfCommandOf(GameCharacterNode issuingCharacter, int maxdepth) {
        if(maxdepth<1){
            throw new RuntimeException();
        }
        if (issuingCharacter.equals(commandingOfficer)) {
            return true;
        }
        if(commandingOfficer!=null && commandingOfficer instanceof AICharacterNode){
            return ((AICharacterNode)commandingOfficer).isUnderChainOfCommandOf(issuingCharacter, --maxdepth);
        }
        return false;
    }
    
    public Commandable select(Commandable selectedCharacter) {
        if(this == selectedCharacter){
            return this;
        }
        if(selectedCharacter != null){
            Geometry g = selectedCharacter.unSelect();
            this.selectionMarker = g;
            attachChild(selectionMarker);
        }
        
        return this;
    }

    public Geometry unSelect() {
        detachChild(selectionMarker);
        return selectionMarker;
    }

    public Vector3f getPositionToTarget(GameCharacterNode targetedBy) {
        if(model.isStanding(channel)){
            return getLocalTranslation().add(new Vector3f(0, Soldier.SHOOTING_HEIGHT, 0));
        }else{
            return new Vector3f(getLocalTranslation());
        }
    }

    public abstract T getCharacterControl();
    protected abstract T createCharacterControl(AssetManager manager);

    public SquadType getSquadType(SquadType squadType, boolean expanded) {
        if(this instanceof AntiTankGunNode){
            squadType = SquadType.ANTI_TANK_GUN;
        }else if(this instanceof GameVehicleNode && squadType!=SquadType.ANTI_TANK_GUN){
            squadType = SquadType.ARMORED_VEHICLE;
        }else if(model.getWeapon()==Weapon.mortar() && squadType!=SquadType.ARMORED_VEHICLE && squadType!=SquadType.ANTI_TANK_GUN){
            squadType = SquadType.MORTAR_TEAM;
        }else if(model.getWeapon()==Weapon.mg42() && squadType!=SquadType.ARMORED_VEHICLE && squadType!=SquadType.ANTI_TANK_GUN){
            squadType = SquadType.MG42_TEAM;
        }
        if(expanded){
            return squadType;
        }

        if(this instanceof CommandingOfficer){
            for(Commandable c:((CommandingOfficer)this).getCharactersUnderCommand()){
                squadType = c.getSquadType(squadType, false);
            }
        }
        return squadType;
    }
    
    public int getCurrentStrength(){
        int count = 1;
        if(this instanceof CommandingOfficer){
            for(Commandable c:((CommandingOfficer)this).getCharactersUnderCommand()){
                count+=c.getCurrentStrength();
            }
        }
        return count;
    }
    
    public void resetKillCount() {
        kills.clear();
        if(this instanceof CommandingOfficer){
            for(Commandable c: ((CommandingOfficer)this).getCharactersUnderCommand()){
                c.resetKillCount();
            }
        }
    }

    public boolean doWeaponDamage(CanInteractWith victim, CollisionResult result) {
        return victim.takeBullet(result, this);
    }

    public float getMaxRange() {
        return model.getMaxRange();
    }

    public boolean isSelected() {
        return hasChild(selectionMarker) || getChild("subselection")!=null;
    }
    
    public Commandable getCharacterUnderCommand(String identity) {
        if(getIdentity().toString().equals(identity)){
            return this;
        }
        if(this instanceof CommandingOfficer){
            for(Commandable n:((CommandingOfficer)this).getCharactersUnderCommand()){
                if(n instanceof GameCharacterNode){
                    Commandable nn = ((GameCharacterNode)n).getCharacterUnderCommand(identity);
                    if(nn!=null){
                        return nn;
                    }
                }else if(n instanceof VirtualGameCharacterNode){
                    if(n.getIdentity().toString().equals(identity)){
                        return n;
                    }
                }
            }
        }
        return null;
    }

    public void idle() {
        channel.setAnim(model.getIdleAnimation(), LoopMode.Loop, .2f, (float) Math.random());
    }
    
    public boolean isCrouched() {
        return channel!=null && channel.getAnimationName().contains("crouchAction");
    }
    
    public boolean isFirering(){
        return model.isAlreadyFiring(channel);
    }

    public boolean isAbstracted() {
        return !characterNode.hasChild(geometry);
    }
    
    public Node getGeometry(){
        return geometry;
    }
    
    public CharacterAction getCharacyerAction(){
        return characterAction;
    }

    protected void doTerrainDamage(CollisionResult result) {
        bulletFragments.setLocalTranslation(result.getContactPoint());
        bulletFragments.emitAllParticles();
    }

    public CharacterMessage toMessage() {
        Set<Action> actions = new HashSet<>();
        actions.add(characterAction.toMessage());
        if(model.isAlreadyFiring(channel)){
            if(getIdentity().toString().equals("9740bc8a-835d-4fa2-ab2b-6ed8d914e6ef")){
                System.out.println("localCharacte shooting:");
            }
            actions.add(new Shoot(shootStartTime, currentTargets));
        }else if(isCrouched()){
            actions.add(new Crouch());
        }else if(channel!=null && model.hasPlayedSetupAction(channel.getAnimationName())){         
            actions.add(new SetupWeapon());
        }
        
        Map<String, String> objectives = new HashMap<>();
        for(Entry<UUID, Objective> entry: getAllObjectives().entrySet()){
            try {
                final ObjectNode valueToTree = entry.getValue().toJson();
                valueToTree.put("class", entry.getValue().getClass().getName());
                objectives.put(entry.getKey().toString(), MAPPER.writeValueAsString(valueToTree));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        Set<String> completedObjectives = getCompletedObjectives();
        final Vector vector = (boaredVehicle!=null)? new Vector(boaredVehicle.getLocalTranslation()): new Vector(getLocalTranslation());
        
        return new CharacterMessage(identity, vector, new Vector(getPlayerDirection()), RankMessage.fromRank(getRank()), actions, objectives, completedObjectives, version);
    }
    
    public long getVersion(){
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof GameCharacterNode)){
            return false;
        }
        return ((GameCharacterNode)obj).identity.equals(identity);
    }

    @Override
    public int hashCode() {
        return identity.hashCode();
    }

    public boolean isSameRank(CharacterMessage message) {
        return getRank().isSame(message.getRank());
    }
    
    public boolean hasSameWeapon(CharacterMessage cMessage) {
        return Weapon.get(cMessage.getWeapon()) == getWeapon();
    }

    public void doDeathEffects() {
        isDead = true;
        shell.removeFromParent();
        muzzelFlash.killAllParticles();
        muzzelFlash.setEnabled(false);
        playerControl.deadStop();
        
        model.doDieAction(channel);
        model.dropDetachableWeapons(geometry);

        //bulletAppState.getPhysicsSpace().remove(playerControl);
    }
    
    public void initialiseKills(Set<UUID> k) {
        this.kills.clear();
        this.kills.addAll(k);
    }

    public GameVehicleNode getBoardedVehicle() {
        return boaredVehicle;
    }
    
    public boolean hasBoardedVehicle(){
        return boaredVehicle !=null;
    }

    public void disembarkVehicle() {
        if(boaredVehicle!=null){
            boaredVehicle.disembarkPassenger(this);
            setLocalTranslation(boaredVehicle.getEmbarkationPoint());
            rootNode.attachChild(this);
            rootNode.attachChild(bloodDebris);
            rootNode.attachChild(bulletFragments);
            rootNode.attachChild(blastFragments);
            rootNode.attachChild(smokeTrail);

            addControl(playerControl);
            bulletAppState.getPhysicsSpace().add(playerControl);
            final Vector3f n = boaredVehicle.getLocalTranslation().subtract(boaredVehicle.getEmbarkationPoint());
            playerControl.setViewDirection(new Vector3f(n.x, 0, n.z).normalizeLocal());
            
            boaredVehicle=null;
            if(channel!=null){
                channel.setAnimForce("disembark_vehicle", LoopMode.DontLoop);
            }
        }
    }

    public Vector3f getLocation() {
        return getLocalTranslation();
    }
    
    public Vector3f getVelocity() {
        return playerControl.getMoveDirection();
    }

    void boardVehicleAction(GameVehicleNode vehicle) {
        playerControl.warp(vehicle.getEmbarkationPoint());
        if(channel!=null){
            channel.setAnim("embark_vehicle", LoopMode.DontLoop);
        }
    }

    public GameCharacterNode getSupreamLeader(int i) {
        if(i>0 && getCommandingOfficer()!=null && getCommandingOfficer() instanceof GameCharacterNode){
            return ((GameCharacterNode)getCommandingOfficer()).getSupreamLeader(i-1);
        }
        return this;
    }
    
    public Vector3f getBustTranslation() {
        return model.getBustTranslation();
    }
    public void setVersion(long version) {
        this.version = version;
    }
    
    public abstract boolean isControledLocaly();

    public abstract void checkForNewObjectives(Map<String, String> objectives) throws IOException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException ;

    public abstract boolean isControledRemotely() ;

    public abstract void setBehaviourControler(BehaviorControler remoteBehaviourControler);

    public abstract BehaviorControler getBehaviourControler();

    public abstract EnemyActivityReport getEnemyActivity();

    public void makeAbstract(Spatial stickFigure) {
        geometry.removeFromParent();
        characterNode.attachChild(stickFigure);
    }

    public void makeUnAbstracted(Spatial stickFigure) {
        stickFigure.removeFromParent();
        characterNode.attachChild(geometry);
    }
   
}
