/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.ai.steering.Obstacle;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.objectives.CaptureTown;
import com.jme3.lostVictories.structures.GameObjectNode;
import com.jme3.lostVictories.structures.Pickable;
import com.jme3.lostVictories.structures.UnclaimedEquipmentNode;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class WorldMap implements Runnable {

    public static Rectangle mapBounds = new Rectangle(-512, -512, 1024, 1024);
    
    public static final int AUTO_ATTACK_RANGE = 15;
    public static final float CHARACTER_SIZE = .5f;
    public static final int RESPOND_RANGE = 5;

    public static final int BLAST_RANGE = 2;

    private static WorldMap instance;

    static WorldMap instance(AvatarCharacterNode avatar, Set<GameCharacterNode> characters, Set<GameStructureNode> structures, Set<GameObjectNode> coverObjects) {
        if(instance==null){
            instance = new WorldMap(avatar, characters, structures, coverObjects);
        }
        return instance;
    }
    
    static  void clear(){
        instance = null;
    }
    
    public static WorldMap get(){
        return instance;
    }
    
    private volatile BiDirectionalMap<GameCharacterNode> characters = new BiDirectionalMap<GameCharacterNode>(mapBounds);    
    private volatile BiDirectionalMap<GameObjectNode> objects = new BiDirectionalMap<GameObjectNode>(mapBounds);
    private final AvatarCharacterNode avatar;
    
    private final Set<GameStructureNode> structures;
    private final Map<UUID, GameHouseNode> houses = new HashMap<UUID, GameHouseNode>();
    private final Map<UUID, UnclaimedEquipmentNode> unclaimedEquipment = new HashMap<UUID, UnclaimedEquipmentNode>();
    Set<GameSector> gameSectors;

    private WorldMap(AvatarCharacterNode avatar, Set<GameCharacterNode> characters, Set<GameStructureNode> structures, Set<GameObjectNode> coverObjects) {
        this.avatar = avatar;
        this.structures = structures;
       
        for(GameStructureNode n:structures){
            if(n instanceof GameHouseNode){
                houses.put(((GameHouseNode)n).getId(), (GameHouseNode) n);
            }
        }
        
        for(GameCharacterNode c: characters){
            addCharacter(c);
        }
        for(GameObjectNode o: coverObjects){
            final Vector3f localTranslation = o.getLocalTranslation();
            final Rectangle.Float rectangle = new Rectangle.Float(localTranslation.x-CHARACTER_SIZE, localTranslation.z-CHARACTER_SIZE, CHARACTER_SIZE*2, CHARACTER_SIZE*2);
            objects.putCharacter(rectangle, o);
        }
    }


    public void run() {
        synchronized(this) {
            try{
                final Collection<GameCharacterNode> values = new HashSet<GameCharacterNode>(characters.allCharacters());
                BiDirectionalMap newMap = new BiDirectionalMap(mapBounds);

                for(GameCharacterNode c: values){
                    final Vector3f localTranslation = c.getLocalTranslation();
                    final Rectangle.Float rectangle = new Rectangle.Float(localTranslation.x-CHARACTER_SIZE, localTranslation.z-CHARACTER_SIZE, CHARACTER_SIZE*2, CHARACTER_SIZE*2);
                    newMap.putCharacter(rectangle, c);
                }
                this.characters = newMap;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    Iterable<GameCharacterNode> getAllAICharacters() {
        final Collection<GameCharacterNode> values = new HashSet<GameCharacterNode>(characters.allCharacters());
        values.remove(avatar);
        return new HashSet<GameCharacterNode>(new HashSet<GameCharacterNode>(values));
    }

    public List<GameCharacterNode> getCharactersInAutoAttackRange(GameCharacterNode character) {
        final Vector3f t = character.getLocalTranslation();
        return getCharactersInRange(t, AUTO_ATTACK_RANGE);
    }
    
    public List<GameCharacterNode> getCharactersInBlastRange(Vector3f epicentre){
        return getCharactersInRange(epicentre, BLAST_RANGE);
    }
    

    public Set<GameCharacterNode> getAllCharacters() {
        return new HashSet<>(characters.allCharacters());
    }
    
    public GameCharacterNode getCharacter(UUID id){
        for(GameCharacterNode c:characters.allCharacters()){
            if(c.getIdentity().equals(id)){
                return c;
            }
        }
        return null;
    }

    public void removeCharacter(GameCharacterNode c) {
        synchronized(this){
            characters.remove(c);
        }
    }

    private List<GameCharacterNode> getCharactersInRange(final Vector3f t, int range) {
        final Rectangle.Float rectangle = new Rectangle.Float( t.x - range, t.z - range, range * 2, range * 2);
        return getCharactersInBoundingRect(rectangle);
    }

    public Iterable<GameStructureNode> getAllStructures() {
        return new HashSet<GameStructureNode>(structures);
    }
    
    public Iterable<GameHouseNode> getAllHouses(){
        return new HashSet<GameHouseNode>(houses.values());
    }
    
    public List<GameCharacterNode> getCharactersInDirection(GameCharacterNode c, Vector3f _v1, float range) {
        return getCharactersInDirection(c, _v1, range, false);
    }

    public List<GameCharacterNode> getCharactersInDirection(GameCharacterNode c, Vector3f _v1, float range, boolean debug) {
        Vector3f p = c.getLocalTranslation();
        Vector3f v1 = new Vector3f(_v1);
        v1.normalizeLocal();
//new Rectangle(100, -300, 5, 5)
        List<Rectangle.Float> coveringBounds = new ArrayList<Rectangle.Float>();
        Vector3f p2 = p.add(v1.mult(5));
        coveringBounds.add(new Rectangle.Float(p2.x-2.5f, p2.z-2.5f, 5, 5));
        Vector3f p3 = p.add(v1.mult(15));
        coveringBounds.add(new Rectangle.Float(p3.x-3.5f, p3.z-3.5f, 7, 7));
        Vector3f p4 = p.add(v1.mult(30));
        coveringBounds.add(new Rectangle.Float(p4.x-5.5f, p4.z-5.5f, 11, 11));
        Vector3f p5 = p.add(v1.mult(50));
        coveringBounds.add(new Rectangle.Float(p5.x-8.5f, p5.z-8.5f, 17, 17));
        Vector3f p6 = p.add(v1.mult(75));
        coveringBounds.add(new Rectangle.Float(p6.x-12.5f, p6.z-12.5f, 25, 25));
        Vector3f p7 = p.add(v1.mult(105));
        coveringBounds.add(new Rectangle.Float(p7.x-17.5f, p7.z-17.5f, 35, 35));
        Vector3f p8 = p.add(v1.mult(140));
        coveringBounds.add(new Rectangle.Float(p8.x-20.5f, p8.z-20.5f, 41, 41));

        List<GameCharacterNode> ret = new ArrayList<GameCharacterNode>();
        if(debug){
            System.out.println("calculating charaters from "+p+" in direction:"+v1);
        }
        for(Rectangle.Float r:coveringBounds){
            final List<GameCharacterNode> charactersInBoundingRect = getCharactersInBoundingRect(r);
            if(debug){
                System.out.println("r:"+r+"found:"+charactersInBoundingRect.size());
            }
            for(GameCharacterNode characterInBounds:charactersInBoundingRect){
                if(!c.isAlliedWith(characterInBounds) && characterInBounds.getLocalTranslation().distance(p)<range){
                    ret.add(characterInBounds);
                }
                
            }
            if(!ret.isEmpty()){
                return ret;
            }
        }
        
        return ret;
    }
    
    public List<Obstacle> getCharactersInDirectionClose(Vector3f p, Vector3f _v1) {
        Vector3f v1 = new Vector3f(_v1);
        v1.normalizeLocal();

        List<Rectangle.Float> coveringBounds = new ArrayList<Rectangle.Float>();
        Vector3f p2 = p.add(v1.mult(3f));
        coveringBounds.add(new Rectangle.Float(p2.x-1.5f, p2.z-1.5f, 3, 3));
        
        List<Obstacle> ret = new ArrayList<>();
        for(Rectangle.Float r:coveringBounds){
            ret.addAll(getCharactersInBoundingRect(r));
        }
        
        return ret;
    }

    List<GameCharacterNode> getCharactersInBoundingRect(Rectangle.Float rectangle) {
        List<GameCharacterNode> ret = characters.getInBounds(rectangle);
        
        for(Iterator<GameCharacterNode> it = ret.iterator();it.hasNext();){
            final Vector3f localTranslation = it.next().getLocalTranslation();
            
            if(!rectangle.contains(new Point.Float(localTranslation.x, localTranslation.z))){
                it.remove();
            }
        }
        return ret;
    }
    
    public Set<GameStructureNode> getStructuresInRange(Vector3f localTranslation, int range) {
        Set<GameStructureNode> ret = new HashSet<GameStructureNode>();
        for(GameStructureNode s:structures){
            if(s.getLocalTranslation().distance(localTranslation)<=range){
                ret.add(s);
            }
        }
        return ret;
    }
    
    public List<GameObjectNode> getCoverInRange(Vector3f localTranslation, float range){
        final Rectangle2D.Float aFloat = new Rectangle.Float(localTranslation.x-range, localTranslation.z-range, range*2, range*2);
        return objects.getInBounds(aFloat);
    }    
    
    public Set<GameHouseNode> getHousesInRange(Vector3f localTranslation, int range) {
        Set<GameHouseNode> ret = new HashSet<GameHouseNode>();
        for(GameHouseNode s:houses.values()){
            if(s.getLocalTranslation().distance(localTranslation)<=range){
                ret.add(s);
            }
        }
        return ret;
    }

    public void addCharacter(GameCharacterNode c) {
        synchronized(this){
            Vector3f localTranslation = c.getLocalTranslation();
            Rectangle.Float rectangle = new Rectangle.Float(localTranslation.x-CHARACTER_SIZE, localTranslation.z-CHARACTER_SIZE, CHARACTER_SIZE*2, CHARACTER_SIZE*2);
            int tries = 0;
            while(this.characters.getCharacterByBounds(rectangle)!=null && tries<20){
                localTranslation.x += 0.1f;
                localTranslation.z += 0.1f;
                rectangle = new Rectangle.Float(localTranslation.x-CHARACTER_SIZE, localTranslation.z-CHARACTER_SIZE, CHARACTER_SIZE*2, CHARACTER_SIZE*2);
                tries++;
            }
            c.setLocalTranslation(localTranslation.x, localTranslation.y, localTranslation.z);

            this.characters.putCharacter(rectangle, c);
        }
    }
    
    Vector3f getSpawnPoint(GameCharacterNode c){
        for(GameHouseNode s: getAllHouses()){
            if(s.getOwner() == c.getCountry()){
                return s.getLocalTranslation().add(new Vector3f(0, 5, 15));
            }
        }
        return c.getLocalTranslation().add(new Vector3f(0, 5, 15));
    }

    public static boolean isClose(Vector3f v1, Vector3f v2, double d) {
        if(Math.abs(v1.x - v2.x)>d){
            return false;
        }
        
        if(Math.abs(v1.z - v2.z)>d){
            return false;
        }
        return true;
    }
    
    public static boolean isClose(Vector3f v1, Vector3f v2) {
        return isClose(v1, v2, .5f);
    }

    public Iterable<GameCharacterNode> getAllOrphanedCharacters(Country country) {
        Set<GameCharacterNode> ret = new HashSet<GameCharacterNode>();
        for(GameCharacterNode n: getAllCharacters()){
            if(n.getCountry() == country && n.getCommandingOfficer() == null){
                ret.add(n);
            }
        }
        return ret;
    }

    public boolean isOutSideWorldBounds(Vector3f add) {
        return !mapBounds.contains(new Point.Float(add.x, add.z));
    }

    public GameHouseNode getHouse(UUID id) {
        return houses.get(id);
    }

    public boolean hasUnclaimedEquipment(UnClaimedEquipmentMessage eq) {
        return  unclaimedEquipment.containsKey(eq.getId());
    }

    void addUnclaimedEquipment(UnclaimedEquipmentNode n) {
        unclaimedEquipment.put(n.getId(), n);
    }

    public Pickable getEquipment(UUID id) {
        return unclaimedEquipment.get(id);
    }

    public Collection<UnclaimedEquipmentNode> getAllEquipment() {
        return new HashSet<UnclaimedEquipmentNode>(unclaimedEquipment.values());
    }

    public void removeEquipment(UnclaimedEquipmentNode n) {
        unclaimedEquipment.remove(n.getId());
    }
    
    public Float getTerrainHeight(Vector3f point, Node rootNode) {
        Ray r = new Ray(point, Vector3f.UNIT_Y.negate());
        r.setLimit(100);
        CollisionResults results = new CollisionResults();
        try{
            rootNode.collideWith(r, results);
            CollisionResult result = results.getClosestCollision();
            if(result!=null){
                return result.getContactPoint().y;
            }
        }catch(Exception e){}
        return null;
    }
    
    public boolean characterInRangeAndLOStoTarget(GameCharacterNode c, Node root, Vector3f...ts) {
        for(Vector3f t: ts){
            Vector3f direction = t.subtract(c.getShootingLocation()).normalizeLocal();
            final Vector3f rayStart = c.getShootingLocation(direction);
            Ray ray = new Ray(rayStart, direction);
            ray.setLimit(c.getMaxRange());
            try{
                CollisionResults results = new CollisionResults();
                root.collideWith(ray, results);
                for(CollisionResult r:results){
                    if(r.getGeometry()!=null && c.hasChild(r.getGeometry())){
                        continue;
                    }
                    if(Math.abs(r.getDistance() - rayStart.distance(t))<1){
                        return true;
                    }
                }
                
            }catch(Throwable e){
                System.out.println("error calculating ray cast");
            }

        }
        return false;
    }

    public Set<GameSector> getGameSectors() {
        if(gameSectors==null){
            gameSectors = calculateGameSectorHouses(getAllHouses());
        }
        return gameSectors;
    }
    
    public static Set<GameSector> calculateGameSector(Iterable<GameStructureNode> allHouses) {
        Set<GameSector> ret = new HashSet<>();
        
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
    
    static Optional<GameSector> findNeighbouringSector(GameSector sector, Set<GameSector> ret) {
        for(GameSector s:ret){
            if(sector.isJoinedTo(s)){
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    Set<GameSector> calculateGameSectorHouses(Iterable<GameHouseNode> allHouses) {
        Set<GameStructureNode> stru = new HashSet<GameStructureNode>();
        for(GameHouseNode h: allHouses){
            stru.add(h);
        }
        return calculateGameSector(stru);
    }

    public Optional<GameSector> findSector(Vector centre) {
        return getGameSectors().stream().filter(sector->sector.containsPoint(centre.x, centre.z)).findAny();
    }

    void addHouse(GameHouseNode addHouse) {
        houses.put(addHouse.getId(), addHouse);
    }

    
}
