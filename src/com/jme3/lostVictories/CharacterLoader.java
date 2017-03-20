/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.characters.blenderModels.HalfTrackBlenderModel;
import com.jme3.lostVictories.characters.blenderModels.BlenderModel;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.effects.ParticleEmitterFactory;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.characters.HeerCaptain;
import com.jme3.lostVictories.characters.CommandingOfficer;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.characters.blenderModels.AmoredCarBlenderModel;
import com.jme3.lostVictories.characters.blenderModels.AntiTankGunModel;
import com.jme3.lostVictories.characters.AntiTankGunNode;
import com.jme3.lostVictories.characters.BehaviorControler;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.HalfTrackNode;
import com.jme3.lostVictories.characters.Lieutenant;
import com.jme3.lostVictories.characters.LocalAIBehaviourControler;
import com.jme3.lostVictories.characters.Private;
import com.jme3.lostVictories.characters.Rank;
import com.jme3.lostVictories.characters.RemoteBehaviourControler;
import com.jme3.lostVictories.characters.blenderModels.SoldierBlenderModel;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.CharacterType;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.structures.GameObjectNode;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.lostVictories.structures.UnclaimedEquipmentNode;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.scene.BatchNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.LodControl;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jme3tools.optimize.LodGenerator;
import jme3tools.optimize.TextureAtlas;

/**
 *
 * @author dharshanar
 */
public class CharacterLoader {
    private static CharacterLoader instance;
 
    static CharacterLoader instance(Node rootNode, AssetManager assetManager, BulletAppState bulletAppState, NavMesh worldMap, ParticleEmitterFactory pf, HeadsUpDisplayAppState hud, ParticleManager particleManager, LostVictory app) {
        if(instance == null){
            instance = new CharacterLoader(rootNode, assetManager, bulletAppState, worldMap, pf, hud, particleManager, app);
        }
        return instance;
    }
    
    private final Node rootNode;
    private final Map<String, BatchNode> characterBatchNode = new HashMap<String, BatchNode>();
    private final AssetManager assetManager;
    private final BulletAppState bulletAppState;
    private final NavMesh navMesh;
    private final NavigationProvider pathFinder;
    private final ParticleEmitterFactory pf;
    private final HeadsUpDisplayAppState hud;
    private final ParticleManager particleManager;
    private final LostVictory app;
    

    private CharacterLoader(Node rootNode, AssetManager assetManager, BulletAppState bulletAppState, NavMesh worldMap, ParticleEmitterFactory pf, HeadsUpDisplayAppState hud, ParticleManager particleManager, LostVictory app) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.bulletAppState = bulletAppState;
        this.navMesh = worldMap;
        this.pathFinder = new NavigationProvider(new NavMeshPathfinder(worldMap));
        this.pf = pf;
        this.hud = hud;
        this.particleManager = particleManager;
        this.app = app;
    }
    
    public AvatarCharacterNode loadCharacters(Set<GameCharacterNode> characters, Set<GameStructureNode> structures, Set<GameObjectNode> coverObjects,CheckoutScreenResponse checkout, UUID avatarID) throws InterruptedException {
       
        //System.out.println("recived async response:"+checkout);
        Map<UUID, GameCharacterNode> characterIdMap = new HashMap<UUID, GameCharacterNode>();
        Map<UUID, CharacterMessage> characterMessageMap = new HashMap<UUID, CharacterMessage>();
        
        AvatarCharacterNode a1 = null;
        WorldMap.instance(a1, characters, structures, coverObjects);
        
        for(CharacterMessage c:checkout.getAllUnits()){
            characterIdMap.put(c.getId(), loadCharacter(c, avatarID));
            characterMessageMap.put(c.getId(), c);
            
        }
        
        
        for(final GameCharacterNode n:characterIdMap.values()){
            if(n instanceof AvatarCharacterNode){
                a1 = (AvatarCharacterNode) n;
            }
            
            final CharacterMessage message = characterMessageMap.get(n.getIdentity());
            if(message.getCommandingOfficer()!=null){
                final CommandingOfficer co = (CommandingOfficer) characterIdMap.get(message.getCommandingOfficer());
                if(co!=null){
                    n.setCommandingOfficer(co);
                    co.addCharactersUnderCommand(new HashSet<Commandable>(){{add(n);}});
                }
            }
            characters.add(n);
        }
        WorldMap.clear();
        WorldMap.instance(a1, characters, structures, coverObjects);
        for(UnClaimedEquipmentMessage e:checkout.getAllEquipment()){
            laodUnclaimedEquipment(e);
        }
        for(GameCharacterNode n:characters){
            try{
                n.checkForNewObjectives(characterMessageMap.get(n.getIdentity()).getObjectives());
            }catch(Exception e){
                e.printStackTrace();
            }
        }
                        
        return a1;
    }
    
    private AvatarCharacterNode loadAvatar(UUID id, Vector3f position, Vector3f rotation, BlenderModel model, Country country, CommandingOfficer commandingOfficer, HeadsUpDisplayAppState hud, Rank rank) {
        Node player =  getModel(model);
        AvatarCharacterNode a = new AvatarCharacterNode(id, player, country, commandingOfficer, position, rotation, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, model, rank, hud, app.getCamera());
        return a;
    }
    
    private GameCharacterNode loadCharacter(UUID id, Vector3f position, Vector3f rotation, BlenderModel model, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        Node player =  getModel(model);
        GameCharacterNode a = new Private(id, player, country, commandingOfficer, position, rotation, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, model, behaviorControler, app.getCamera());
        return a;
    }

    private GameVehicleNode loadHalfTrack(UUID id, Vector3f position, Vector3f rotation, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        final HalfTrackBlenderModel halfTrackBlenderModel = new HalfTrackBlenderModel("Models/Vehicles/Armored_Car.j3o", 1, Weapon.mg42());
        Node vehicle =  getModel(halfTrackBlenderModel);
        
        
        final GameVehicleNode v = new HalfTrackNode(id, vehicle, getOperators(), country, commandingOfficer, position, rotation, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, halfTrackBlenderModel, behaviorControler, app.getCamera());
        if(commandingOfficer!=null){
            commandingOfficer.addCharactersUnderCommand(new HashSet<Commandable>(){{add(v);}});
        }
        return v;
    }
   
    private GameVehicleNode loadAmoredCar(UUID id, Vector3f position, Vector3f rotation, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        final AmoredCarBlenderModel amoredCarBlenderModel = new AmoredCarBlenderModel("Models/Vehicles/M3_Scout.j3o", 1, Weapon.mg42());
        Node vehicle =  getModel(amoredCarBlenderModel);
        
        final GameVehicleNode v = new HalfTrackNode(id, vehicle, getOperators(), country, commandingOfficer, position, rotation, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, amoredCarBlenderModel, behaviorControler, app.getCamera());
        if(commandingOfficer!=null){
            commandingOfficer.addCharactersUnderCommand(new HashSet<Commandable>(){{add(v);}});
        }
        return v;
    }
    
    private GameVehicleNode loadAntiTankGun(UUID id, Vector3f position, Vector3f rotation, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        final AntiTankGunModel antiTankGunModel = new AntiTankGunModel("Models/Vehicles/Anti_Tank_Gun.j3o", 1, Weapon.cannon());
        Node vehicle =  getModel(antiTankGunModel);
        
        final GameVehicleNode v = new AntiTankGunNode(id, vehicle, getOperators(), country, commandingOfficer, position, rotation, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, antiTankGunModel, behaviorControler, app.getCamera());
        if(commandingOfficer!=null){
            commandingOfficer.addCharactersUnderCommand(new HashSet<Commandable>(){{add(v);}});
        }
        return v;
    }

    private Lieutenant loadLieutenant(UUID id, Vector3f position, Vector3f rotation, BlenderModel model, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        Node player =  getModel(model);
        Lieutenant a = new Lieutenant(id, player, country, commandingOfficer, position, rotation, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, model, behaviorControler, app.getCamera());
        return a;
    }
    
    private CadetCorporal loadCorporal(UUID id, Vector3f position, Vector3f rotation, BlenderModel model, Country country, CommandingOfficer commandingOfficer, BehaviorControler behaviorControler) {
        Node player =  getModel(model);
        CadetCorporal a = new CadetCorporal(id, player, country, commandingOfficer, position, rotation, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, model, behaviorControler, app.getCamera());
        return a;
    }
    
    private HeerCaptain loadHeerCaptain(UUID id, Vector3f position, Vector3f rotation, BlenderModel model, Country country, BehaviorControler behaviorControler) {
        Node player =  getModel(model);
        HeerCaptain a = new HeerCaptain(id, player, country, null, position, rotation, rootNode, bulletAppState, pf.getCharacterParticleEmitters(), particleManager, pathFinder, assetManager, model, behaviorControler, app.getCamera());
        return a;
    }

    public void destroyCharacter(GameCharacterNode toDemote) {
        if(toDemote.getCommandingOfficer()!=null){
            toDemote.getCommandingOfficer().removeCharacterUnderCommand(toDemote);
        }

        toDemote.decomposed();
        WorldMap.get().removeCharacter(toDemote);
    }

    protected Node getModel(BlenderModel model) {        
        final Node clone = loadFromCache(model);
        
        
        return clone;
    }
    
    public GameCharacterNode loadCharacter(CharacterMessage c, UUID avatarId) {
        final Country country = Country.valueOf(c.getCountry().name());
        final Weapon weapon = Weapon.get(c.getWeapon());
        
        BehaviorControler b = (c.shouldBeControledRemotely(avatarId))?new RemoteBehaviourControler(c.getId(), c):new LocalAIBehaviourControler();
        GameCharacterNode loadedCharacter;
        
        Vector3f location = c.getLocation().toVector();
        Vector3f rotation = c.getOrientation().toVector();
        adjustLocationToNavMap(location);
        
        if(CharacterType.ANTI_TANK_GUN == c.getType()){
            loadedCharacter = loadAntiTankGun(c.getId(), location, rotation, country, null, b);
        }else if(CharacterType.ARMORED_CAR == c.getType()){
            loadedCharacter = loadAmoredCar(c.getId(), location, rotation, country, null, b);
        }else if(CharacterType.HALF_TRACK == c.getType()){
            loadedCharacter = loadHalfTrack(c.getId(), location, rotation, country, null, b);
        }else if(CharacterType.AVATAR == c.getType() && avatarId.equals(c.getId())){
            final Rank r = Rank.valueOf(c.getRank().name());
            loadedCharacter = loadAvatar(c.getId(), location, rotation, country.getModel(weapon, r), country, null, hud, r);
        }else if(RankMessage.COLONEL == c.getRank()){
            loadedCharacter = loadHeerCaptain(c.getId(), location, rotation, country.getModel(weapon, Rank.COLONEL), country, b);
        }else if(RankMessage.LIEUTENANT == c.getRank()){
            loadedCharacter = loadLieutenant(c.getId(), location, rotation, country.getModel(weapon, Rank.LIEUTENANT), country, null, b);
        }else if(RankMessage.CADET_CORPORAL == c.getRank()){
            loadedCharacter = loadCorporal(c.getId(), location, rotation, country.getModel(weapon, Rank.CADET_CORPORAL), country, null, b);
        }else if(RankMessage.PRIVATE == c.getRank()){
            loadedCharacter = loadCharacter(c.getId(), location, rotation, country.getModel(weapon, Rank.PRIVATE), country, null, b);
        }else{
            throw new UnsupportedOperationException("error loading character type:"+c.getType()+" rank:"+c.getRank());
        }
        loadedCharacter.initialiseKills(c.getKills());
        loadedCharacter.setVersion(c.getVersion());
        if(c.getType()==CharacterType.SOLDIER){
            loadedCharacter.addControl(new CustomLODControl(app.getCamera(), assetManager.loadModel("Models/Soldier/stickfigure3.j3o")));
        }
        loadedCharacter.addControl(new UnderTakerControl(app.getCamera()));
        loadedCharacter.attachToRootNode();
        
        return loadedCharacter;
    }    

    public void laodUnclaimedEquipment(UnClaimedEquipmentMessage eq) {
        final Node model = getModel(Country.AMERICAN.getModel(Weapon.rifle(), Rank.PRIVATE));
        final Vector3f location = eq.getLocation().toVector();
        pathFinder.warpInside(location);
        UnclaimedEquipmentNode n = new UnclaimedEquipmentNode(eq.getId(), location, eq.getRotation().toVector(), Weapon.get(eq.getWeapon()), model, rootNode, assetManager);
        WorldMap.get().addUnclaimedEquipment(n);
    }

    protected void adjustLocationToNavMap(Vector3f location) {
        pathFinder.warpInside(location);
        location.y = location.y +1;
    }

    private Map<Country, Node> getOperators() {
        HashMap<Country, Node> operatorMap = new HashMap<Country, Node>();
        final BlenderModel blenderModel1 = new SoldierBlenderModel("Models/Vehicles/german_operator.j3o", 1, Weapon.mg42());
        Node gunner = getModel(blenderModel1);
        gunner.setLocalScale(.75f);
        gunner.setName("operator");
        operatorMap.put(Country.GERMAN, gunner);
        Node gunner2 =  getModel(new SoldierBlenderModel("Models/Vehicles/american_operator.j3o", 1, Weapon.mg42()));
        gunner2.setLocalScale(.25f);
        gunner2.setName("operator");
        operatorMap.put(Country.AMERICAN, gunner2);        
        return operatorMap;
    }

    protected Node loadFromCache(BlenderModel model) {
            return (Node) assetManager.loadModel(model.getModelPath());
    }

        
}
