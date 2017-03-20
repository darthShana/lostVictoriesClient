/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.lostVictories.structures.GameTargetNode;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.lostVictories.network.messages.CheckoutScreenResponse;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.objectives.CaptureTown;
import com.jme3.lostVictories.structures.CollisionShapeFactoryProvider;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.lostVictories.structures.HeloControl;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.terrain.geomipmap.TerrainQuad;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jme3tools.optimize.GeometryBatchFactory;
import jme3tools.optimize.TextureAtlas;

/**
 *
 * @author dharshanar
 */
class StructureLoader {
    private static StructureLoader instance;
    //private Map<String, Node> structureType = new HashMap<String, Node>();
    private Set<String> structureTypes = new HashSet<String>();

    static StructureLoader instance(Node rootNode, AssetManager assetManager, BulletAppState bulletAppState, NavMeshPathfinder pathfinder) {
        if(instance == null){
            instance = new StructureLoader(rootNode, assetManager, bulletAppState, pathfinder);
        }
        return instance;
    }
    private final Node rootNode;
    private final AssetManager assetManager;
    private final BulletAppState bulletAppState;
    private final NavMeshPathfinder pathfinder;

    private StructureLoader(Node rootNode, AssetManager assetManager, BulletAppState bulletAppState, NavMeshPathfinder pathfinder) {
        this.rootNode = rootNode;
        this.assetManager = assetManager;
        this.bulletAppState = bulletAppState;
        this.pathfinder = pathfinder;
        
    }


    void loadStuctures(Set<GameStructureNode> structures, Node sceneGraph, CheckoutScreenResponse checkout, TerrainQuad terrain, LostVictory app) {
                
        structureTypes.add("Models/Structures/casaMedieval.j3o");
        structureTypes.add("Models/Structures/house.j3o");
        structureTypes.add("Models/Structures/house2.j3o");
        structureTypes.add("Models/Structures/house_1.j3o");
        structureTypes.add("Models/Structures/cottage.j3o");
        
        Set<String> otherStructures = new HashSet<String>();
        otherStructures.add("church1");
        otherStructures.add("Models/Structures/fountain1.j3o");
        otherStructures.add("Models/Structures/market.j3o");
        otherStructures.add("Models/Structures/school.j3o");
        otherStructures.add("Models/Structures/stable.j3o");
        otherStructures.add("Models/Structures/cottage.j3o");
        otherStructures.add("Models/Structures/shed.j3o");
        otherStructures.add("Models/Structures/watchtower.j3o");
        otherStructures.add("Models/Structures/house_1.j3o");
        otherStructures.add("Models/Structures/WaterPoweredSawmill.j3o");
        otherStructures.add("Models/Structures/ponte bridge.j3o");
        otherStructures.add("Models/Structures/bridge_short.j3o");
        otherStructures.add("Models/Structures/Chapel.j3o");
        otherStructures.add("Models/Structures/tavern.j3o");
        otherStructures.add("Models/Structures/Windmill.j3o");
        
        //Node otherStructureNode = new Node();
        
        for(Spatial s: sceneGraph.getChildren()){
            if(structureTypes.contains(s.getName())){
                s.removeFromParent();
            }else if(otherStructures.contains(s.getName())){
                s.removeFromParent();
                addStructure((Node) s, structures);          
            }
            
        }
//        Node geom = GeometryBatchFactory.optimize(otherStructureNode, true);
//        sceneGraph.attachChild(otherStructureNode);
        
//        Map<String, Node> toOptomise = new HashMap<String, Node>();
        final Set<HouseMessage> allHouses = checkout.getAllHouses();
        
        for(HouseMessage h:allHouses){
            structures.add(addHouse(clone(app), app, h, terrain, sceneGraph));
        }
//        for(Node houses:toOptomise.values()){
//            Node a = GeometryBatchFactory.optimize(houses, true);
//            sceneGraph.attachChild(houses);
//        }
        final Set<CaptureTown.GameSector> calculateGameSector = CaptureTown.calculateGameSector(structures);
        for(CaptureTown.GameSector sector:calculateGameSector){
            Node sec = new Node();
            for(GameStructureNode s:sector.structures()){
                sec.attachChild(s);
            }
            Spatial a = GeometryBatchFactory.optimize(sec);
            sceneGraph.attachChild(a);
//            sceneGraph.attachChild(sec);
        }
        
//        Node s1 = (Node) sceneGraph.getChild("church");
//        addStructure(s1, sceneGraph, structures);
//        Node s2 = (Node) sceneGraph.getChild("fountain");
//        addStructure(s2, sceneGraph, structures);
//        Node s3 = (Node) sceneGraph.getChild("shed");
//        addStructure(s3, sceneGraph, structures);
//        Node s4 = (Node) sceneGraph.getChild("school");
//        addStructure(s4, sceneGraph, structures);
//        Node s5 = (Node) sceneGraph.getChild("market");
//        addStructure(s5, sceneGraph, structures);
//        Node s6 = (Node) sceneGraph.getChild("stable");
//        addStructure(s6, sceneGraph, structures);
        
        addShootingTarget(new Vector3f(268.71814f, 96.11746f, 17.211853f), sceneGraph, structures);
        addShootingTarget(new Vector3f(-66.78554f, 96.32174f, -254.6674f), sceneGraph, structures);
        
    }

    private GameHouseNode addHouse(Map flags, LostVictory app, HouseMessage house, TerrainQuad terrain, Node rootNode) {        
        Node n = (Node) assetManager.loadModel(house.getType());
        final Vector3f l = house.getLocalTranslation();
        n.setLocalTranslation(l.x, terrain.getHeight(new Vector2f(l.x, l.z)), l.z);

        n.setLocalRotation(house.getLocalRotation());
        Node neutralFlag = (Node)assetManager.loadModel("Models/Structures/neutralFlag.j3o");
        neutralFlag.setLocalScale(.5f);
        neutralFlag.addControl(new HeloControl(assetManager, app));
        
        GameHouseNode h = new GameHouseNode(house.getId(), house.getType(), n, flags, neutralFlag, this.bulletAppState, new CollisionShapeFactoryProvider(), pathfinder, rootNode);
        
//        if(!structures.containsKey(house.getType())){
//            structures.put(house.getType(), new Node());
//        }
//        structures.get(house.getType()).attachChild(h);
        return h;        
    }
    
    private GameTargetNode addShootingTarget(Vector3f l, Node sceneGraph, Set<GameStructureNode> structures){
        final Node t = (Node)assetManager.loadModel("Models/Structures/target.j3o");
        t.setLocalTranslation(l);
        t.setLocalRotation(new Quaternion().fromAngleAxis(-FastMath.QUARTER_PI/2, Vector3f.UNIT_Y));
        final GameTargetNode gameTargetNode = new GameTargetNode(t, this.bulletAppState, new CollisionShapeFactoryProvider());
        structures.add(gameTargetNode);
        sceneGraph.attachChild(gameTargetNode);
        return gameTargetNode;
    }
    
    private GameStructureNode addStructure(Node stucture, Set<GameStructureNode> structures) {
        GameStructureNode h = new GameStructureNode(stucture, this.bulletAppState, new CollisionShapeFactoryProvider());
        structures.add(h);
        //sceneGraph.attachChild(h);
        
        return h;        
    }

//    private Map<Country, Node> loadFlags(LostVictory app) {
//        Map<Country, Node> flags = new EnumMap<Country, Node>(Country.class);
//        
//        flags.put(Country.AMERICAN, american);
//        
//        flags.put(Country.GERMAN, german);
//
//        return flags;
//    }

    private Map clone(LostVictory app) {
        final Node american = (Node)assetManager.loadModel("Models/Structures/americanFlag.j3o");
        final Node german = (Node)assetManager.loadModel("Models/Structures/germanFlag.j3o");
        american.addControl(new HeloControl(assetManager, app));
        german.addControl(new HeloControl(assetManager, app));
        american.setLocalScale(.5f);
        german.setLocalScale(.5f);
        
        Map countries = new EnumMap<Country, Node>(Country.class);
        countries.put(Country.AMERICAN, american);
        countries.put(Country.GERMAN, german);
        
        return countries;
    }

    
}
