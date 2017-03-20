/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.CommandingOfficer;
import com.jme3.lostVictories.characters.GameCharacterNode;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.type.TypeReference;

/**
 *
 * @author dharshanar
 */
class BombardTargets extends Objective {
    private List<Vector3f> targets;
    Map<UUID, Objective> issuedOrders = new HashMap<UUID, Objective>();
    BombardTargetsState state = BombardTargetsState.MoveToRange;
    private Node rootNode;
         
    private BombardTargets(){}
    
    public BombardTargets(Set<Vector3f> targets, Node rootNode) {
        System.out.println("in here new BombardTargets:"+targets);
        this.targets = new ArrayList<Vector3f>(targets);
        this.rootNode = rootNode;
    }

    @Override
    public AIAction planObjective(GameCharacterNode character, WorldMap worldMap) {
        final BombardTargetsState newState = state.planObjective(character, issuedOrders, targets, rootNode);
        if(newState!=state){
            if(newState==BombardTargetsState.BombardmentComplete){
                System.out.println(character.getIdentity()+" in bomb:"+newState);
                if(targets.size()<=1){
                    isComplete = true;
                }else{
                    targets.remove(0);
                }
            }
            issuedOrders.clear();
            state = newState;
        }
        final Objective get = issuedOrders.get(character.getIdentity());
        if(get!=null){
            return get.planObjective(character, worldMap);
        }
        return null;
    }

    @Override
    public boolean clashesWith(Objective objective) {
        return objective instanceof CaptureStructure || 
                objective instanceof Cover || 
                objective instanceof TransportSquad || 
                objective instanceof AttackBoggies;
    }

    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        Set<Vector> t = new HashSet<Vector>();
        for(Vector3f v:targets){
            t.add(new Vector(v));
        }
        node.put("targets", MAPPER.valueToTree(t));
        return node;
    }

    public BombardTargets fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Set<Vector> readValue = MAPPER.readValue(json.get("targets"), new TypeReference<Set<Vector>>(){});
        Set<Vector3f> t = new HashSet<Vector3f>();
        for(Vector v: readValue){
            t.add(new Vector3f(v.x, v.y, v.z));
        }
        return new BombardTargets(t, rootNode);
    }

    public enum BombardTargetsState {
        MoveToRange{
        
            public BombardTargetsState planObjective(GameCharacterNode character, Map<UUID, Objective> issuedOrders, List<Vector3f> targets, Node rootNode) {
                if(character.getLocalTranslation().distance(targets.iterator().next())<character.getMaxRange()){
                    return BombardTarget;
                }
                if(!issuedOrders.containsKey(character.getIdentity())){
                    Objective travelObjective;
                    if(character.isHuman()){
                        travelObjective = new TravelObjective(character, targets.iterator().next(), null);
                    }else{
                        travelObjective = new NavigateObjective(targets.iterator().next(), null);
                    }
                    issuedOrders.put(character.getIdentity(), travelObjective);
                }
                return MoveToRange;
            }
        },
        
        BombardTarget {
            public BombardTargetsState planObjective(GameCharacterNode character, Map<UUID, Objective> issuedOrders, List<Vector3f> targets, Node rootNode) {
                final Vector3f next = targets.iterator().next();                
                
                if(character instanceof CommandingOfficer){
                    for(Commandable unit:((CommandingOfficer)character).getCharactersUnderCommand()){
                        if(!issuedOrders.containsKey(unit.getIdentity())){
                            Cover c = new Cover(character.getLocalTranslation(), next, rootNode);
                            issuedOrders.put(unit.getIdentity(), c);
                            unit.addObjective(c);
                        }else if(issuedOrders.get(unit.getIdentity()).isComplete){
                            return BombardmentComplete;
                        }
                    }
                }
                if(!issuedOrders.containsKey(character.getIdentity())){
                    issuedOrders.put(character.getIdentity(), new Cover(character.getLocalTranslation(), next, rootNode));
                }
                return BombardTarget;
            }
        },
        
        BombardmentComplete{
            public BombardTargetsState planObjective(GameCharacterNode character, Map<UUID, Objective> issuedOrders, List<Vector3f> targets, Node rootNode) {
                return BombardmentComplete;
            }
        };

        public abstract BombardTargetsState planObjective(GameCharacterNode character, Map<UUID, Objective> issuedOrders, List<Vector3f> targets, Node rootNode);
    }
    
}
