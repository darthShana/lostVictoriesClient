/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.GameSector;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.HeerCaptain;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.awt.Rectangle;
import java.io.IOException;
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
public class CaptureTown extends Objective<Soldier>{
    private HeerCaptain character;
    private Node rootNode;
    Map<GameSector, UUID> sectorAssignments = new HashMap<GameSector, UUID>();

    private CaptureTown(){}
    
    public CaptureTown(HeerCaptain character, Node rootNode) {
        this.character = character;
        this.rootNode = rootNode;
    }

    public AIAction planObjective(Soldier character, WorldMap worldMap) {
        
        Set<GameSector> gameSectors = worldMap.getGameSectors();

        Set<Commandable> available = new HashSet<Commandable>();
        Set<UUID> stillAround = new HashSet<UUID>();

        for(Commandable c: this.character.getCharactersUnderCommand()){
            if(!c.isBusy()){
                available.add(c);
            }
            stillAround.add(c.getIdentity());
        }
        if(available.isEmpty()){
            return null;
        }
        
        GameSector toSecure = findClosestUnsecuredGameSector(character, gameSectors, sectorAssignments);
        if(toSecure==null){
            return null;
        }
        
        Commandable toUse = findClossestToSector(toSecure, available);
        
        for(Iterator<Entry<GameSector, UUID>> it = sectorAssignments.entrySet().iterator();it.hasNext();){
            if(!stillAround.contains(it.next().getValue())){
                it.remove();
            }
        }
        
        if(toUse!=null){   
            final SecureSector secureSector = new SecureSector(toSecure.getHouses(), rootNode, 10, 5, character.getLocalTranslation());
            toUse.addObjective(secureSector);
            sectorAssignments.put(toSecure, toUse.getIdentity());
        }
        
        return null;
    }

    public boolean clashesWith(Objective objective) {
        return false;
    }
    
    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        return node;
    }

    public CaptureTown fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        return new CaptureTown((HeerCaptain) character, rootNode);
    }
    


    private GameSector findClosestUnsecuredGameSector(GameCharacterNode character, Set<GameSector> gameSectors, Map<GameSector, UUID> exclude) {
        GameSector closest = null;
        for(GameSector gameSector:gameSectors){
            if(gameSector.isUnsecured(character.getCountry()) && !exclude.containsKey(gameSector)){
                if(closest==null || closest.location().distance(character.getLocalTranslation())>gameSector.location().distance(character.getLocalTranslation())){
                    closest = gameSector;
                }
            }
        }
        
        return closest;
    }

    private Commandable findClossestToSector(GameSector toSecure, Set<Commandable> available) {
        GameCharacterNode closest = null;
        for(Commandable c:available){
            if(c instanceof GameCharacterNode){
                GameCharacterNode cc = (GameCharacterNode) c;
                if(closest==null || cc.getLocalTranslation().distance(toSecure.location())<closest.getLocalTranslation().distance(toSecure.location())){
                    closest = cc;
                }
            }
        }
        return closest;
    }

    private Vector3f findClosest(Soldier character, Set<Vector3f> obstructions) {
        Vector3f closest = null;
        for(Vector3f o:obstructions){
            if(closest==null || o.distance(character.getLocalTranslation())<closest.distance(character.getLocalTranslation())){
                closest = o;
            }
        }
        return closest;
    }
    


    
    
}
