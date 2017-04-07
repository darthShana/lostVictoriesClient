/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.asset.AssetManager;
import com.jme3.lostVictories.GameSector;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Lieutenant;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;
import com.jme3.lostVictories.minimap.MinimapPresentable;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class SecureSector extends Objective<AICharacterNode> implements MinimapPresentable{
    Set<GameHouseNode> houses;
    private Node rootNode;
    
    Vector3f centre;
    int deploymentStrength;
    int minimumFightingStrenght;
    Map<UUID, Objective> issuedOrders = new HashMap<UUID, Objective>();
    Set<UUID> attemptedHouses = new HashSet<UUID>();
    SecureSectorState state = SecureSectorState.WAIT_FOR_REENFORCEMENTS;
    SecureSectorState lastState;
    Vector3f homeBase;

    private SecureSector(){}
    
    SecureSector(Set<GameHouseNode> houses, Node rooNode, int deploymentStrength, int minimumFightingStrenght, Vector3f homeBase) {
        this.houses = houses;
        this.rootNode = rooNode;
        
        float totalX = 0, totalY = 0,totalZ = 0;
        
        for(GameHouseNode h:houses){
            totalX+=h.getLocalTranslation().x;
            totalY+=h.getLocalTranslation().y;
            totalZ+=h.getLocalTranslation().z;
        }
        final float x = totalX/houses.size();
        final float y = totalY/houses.size();
        final float z = totalZ/houses.size();
        centre = new Vector3f(x, y, z);
        this.deploymentStrength = deploymentStrength;
        this.minimumFightingStrenght = minimumFightingStrenght;
        this.homeBase = homeBase;
//        System.out.println("secure sector:"+centre+" houses:"+houses.size());
    }

    @Override
    public AIAction<AICharacterNode> planObjective(final AICharacterNode c, WorldMap worldMap) {
//        MoveToSector -> CaptureHouses-> DefendSector -> AttackThreat->Retreat
        AIAction<AICharacterNode> action = state.planObjective((Lieutenant)c, worldMap, rootNode, this);
        SecureSectorState newState = state.transition((Lieutenant)c, worldMap, this);
        if(newState!=state){
//            System.out.println(c.getCountry()+" "+c.getRank()+":"+c.getIdentity()+" new state:"+newState+" houses:"+houses.size()+" loc:"+c.getLocation()+" home:"+homeBase);
            issuedOrders.clear();
            attemptedHouses.clear();
            lastState = state;
            state = newState;
        }
        return action;

    }
    

    @Override
    public boolean clashesWith(Objective objective) {
        return false;
    }

    @Override
    public ObjectNode toJson() {
        Set<UUID> hs = new HashSet<UUID>();
        for(GameHouseNode h:houses){
            hs.add(h.getId());
       }
        
        ObjectNode node = MAPPER.createObjectNode();
        node.put("centre", MAPPER.valueToTree(new Vector(centre)));
        node.put("homeBase", MAPPER.valueToTree(new Vector(homeBase)));
        node.put("deploymentStrength", deploymentStrength);
        node.put("minimumFightingStrenght", minimumFightingStrenght);
        return node;
    }

    @Override
    public Objective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        JavaType type = MAPPER.getTypeFactory().constructCollectionType(Set.class, UUID.class);
        int ds = json.get("deploymentStrength").asInt();
        int mfs = json.get("minimumFightingStrenght").asInt();
        Vector h = MAPPER.treeToValue(json.get("homeBase"), Vector.class);
        Vector centre = MAPPER.treeToValue(json.get("centre"), Vector.class);

        Set<GameHouseNode> hs = new HashSet<>();
        Optional<GameSector> sector = map.findSector(centre);
        if(sector.isPresent()){            
            hs.addAll(sector.get().getHouses());
        }else{
            System.out.println("unable to find sector:"+centre);
        }
        
        if(!(character instanceof Lieutenant)){
            System.out.println("found rank mismatch:"+character.getIdentity());
        }
        
        return new SecureSector(hs, rootNode, ds, mfs, h.toVector());
    }



    public List<String> getInstructions() {
        List<String> instructions = new ArrayList<String>();
        instructions.add("secure the houses in this sector");
        return instructions;
    }

    public boolean updatedStatus(AvatarCharacterNode avatar) {        
        return false;
    }

    public Node getShape(AssetManager assetManager, GameCharacterNode c) {
        Node sector = new Node();
        for(GameHouseNode h:houses){
            final Node circle = getCircle(assetManager, h.isOwnedBy(c.getCountry())?ColorRGBA.Green:ColorRGBA.Brown, 10);
            circle.setName(h.getId().toString());
            circle.setLocalTranslation(h.getLocalTranslation().subtract(centre));
            sector.attachChild(circle);
        }
        
        return sector;
    }

    public Vector3f getObjectiveLocation() {
        return centre;
    }

    
    
}
