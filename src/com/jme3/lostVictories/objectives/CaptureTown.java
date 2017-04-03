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
    Set<GameSector> gameSectors;
    private Node rootNode;
    Map<GameSector, UUID> sectorAssignments = new HashMap<GameSector, UUID>();

    private CaptureTown(){}
    
    public CaptureTown(HeerCaptain character, Node rootNode) {
        this.character = character;
        this.rootNode = rootNode;
    }

    public AIAction planObjective(Soldier character, WorldMap worldMap) {
        
        if(gameSectors==null){
            gameSectors = calculateGameSectorHouses(worldMap.getAllHouses());
        }

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
    


    public static Set<GameSector> calculateGameSector(Iterable<GameStructureNode> allHouses) {
        Set<GameSector> ret = new HashSet<GameSector>();
        
        for(int y = WorldMap.mapBounds.y;y<=WorldMap.mapBounds.getMaxY();y=y+50){
            for(int x = WorldMap.mapBounds.x;x<=WorldMap.mapBounds.getMaxX();x=x+50){
                ret.add(new GameSector(new Rectangle(x, y, 50, 50)));
            }
        }
        
        for(GameStructureNode house:allHouses){
            for(GameSector sector:ret){
                if(sector.containsHouse(house)){
                    sector.add(house);
                }
            }
        }
        
        for(Iterator<GameSector> it = ret.iterator();it.hasNext();){
            if(it.next().structures.isEmpty()){
                it.remove();
            }
        }
        
//        merge adjoing sectorors to gether with limit number of houses
        Set<GameSector> merged = new HashSet<GameSector>();
        GameSector next = ret.iterator().next();
        merged.add(next);
        ret.remove(next);
		
        while(!ret.isEmpty()){
            boolean foundMerge = false;
            for(GameSector sector:merged){
                Optional<GameSector> neighbour = findNeighbouringSector(sector, ret);
                if(neighbour.isPresent()){
                    sector.merge(neighbour.get());
                    ret.remove(neighbour.get());
                    foundMerge = true;
                }
            }
            if(!foundMerge){
                next = ret.iterator().next();
                merged.add(next);
                ret.remove(next);
            }
        		
        }
        
        
        return merged;
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
    
    static Optional<GameSector> findNeighbouringSector(GameSector sector, Set<GameSector> ret) {
        for(GameSector s:ret){
            if(sector.isJoinedTo(s)){
                return Optional.of(s);
            }
        }
        return Optional.absent();
    }

    Set<GameSector> calculateGameSectorHouses(Iterable<GameHouseNode> allHouses) {
        Set<GameStructureNode> stru = new HashSet<GameStructureNode>();
        for(GameHouseNode h: allHouses){
            stru.add(h);
        }
        return calculateGameSector(stru);
    }

    
    public static class GameSector {
        private final Set<Rectangle> rects = new HashSet<Rectangle>();
        private final Set<GameStructureNode> structures = new HashSet<GameStructureNode>();

        public GameSector(Rectangle rect) {
            this.rects.add(rect);
        }
        
        public boolean isJoinedTo(GameSector s) {
            for(Rectangle r1: rects){
                for(Rectangle r2:s.rects){
                    if(new Rectangle(r1.x-1, r1.y-1, r1.width+2, r1.height+2).intersects(r2)){
                        return true;
                    }
                }

            }
            return false;
        }

        public void merge(GameSector neighbour) {
            structures.addAll(neighbour.structures);
            rects.addAll(neighbour.rects);
        }

        private boolean containsHouse(GameStructureNode house) {
            for(Rectangle r:rects){
                if(r.contains(house.getLocalTranslation().x, house.getLocalTranslation().z)){
                    return true;
                }
            }
            return false;
        }

        void add(GameStructureNode structure) {
            structures.add(structure);
        }

        private boolean isUnsecured(Country country) {
            for(GameStructureNode h:structures){
                if(h instanceof GameHouseNode){
                    if(!((GameHouseNode)h).isOwnedBy(country)){
                        return true;
                    }
                }
            }
            return false;
        }

        private Vector3f location() {
            final Rectangle next = rects.iterator().next();
            return new Vector3f((float)next.getCenterX(), 0, (float)next.getCenterY());
        }
        
        Set<GameHouseNode> getHouses(){
            Set<GameHouseNode> hh = new HashSet<GameHouseNode>();
            for(GameStructureNode h: structures){
                if(h instanceof GameHouseNode){
                    hh.add((GameHouseNode) h);
                }
            }
            return hh;
        }

        public Iterable<GameStructureNode> structures() {
            return structures;
        }
    }
}
