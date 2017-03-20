/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.lostVictories.objectives.Objectives;
import com.jme3.scene.Node;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;
import org.codehaus.jackson.JsonNode;

/**
 *
 * @author dharshanar
 */
public class LocalAIBehaviourControler implements BehaviorControler{

    PriorityBlockingQueue<AIAction> aiActions = new PriorityBlockingQueue<AIAction>(1, new ActionComparator());
    Objectives objectives = new Objectives<Soldier>();
    private Set<UUID> recivedObjectives = new HashSet<UUID>();

    public void doActions(AICharacterNode character, Node rootNode, GameAnimChannel channel, float tpf) {
        if(character.getBoardedVehicle()!=null){
            return;
        }
        AIAction action = aiActions.peek();
        if(action!=null){
            if(action.doAction(character, rootNode, channel, tpf)){
                aiActions.poll();
            }
        }
        action = aiActions.peek();
        if(action!=null){
            if(action.doAction(character, rootNode, channel, tpf)){
                aiActions.poll();
            }
        }
        
    }

    public void planObjectives(GameCharacterNode character, WorldMap worldMap) {
        aiActions.clear();
        aiActions.addAll(objectives.planObjectives(character, worldMap));
    }

    public void addObjective(Objective o) {
        objectives.addObjective(o);   
    }
    
    public Set<String> getCompletedObjectives(){
        return objectives.getCompletedObjectives();
    }
    
    public void addObjectivesFromRemoteCharacters(Map<String, String> objectives, GameCharacterNode character, NavigationProvider pathfinder, Node rootNode, WorldMap map) throws IllegalAccessException, NoSuchMethodException, SecurityException, InvocationTargetException, InstantiationException, IllegalArgumentException, ClassNotFoundException, IOException {
        for(Map.Entry<String, String> e: objectives.entrySet()){
            UUID o = UUID.fromString(e.getKey());
            if(!recivedObjectives.contains(o) && !character.getAllObjectives().containsKey(o) && !getCompletedObjectives().contains(o.toString())){
                final JsonNode readTree = MAPPER.readTree(e.getValue());
                Class oClass = Class.forName(readTree.get("class").asText());
                Constructor<Objective> constructor = oClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Objective temp = constructor.newInstance();
                Objective real = null;
                try{
                    real = temp.fromJson(readTree, character, pathfinder, rootNode, map);
                }catch(Throwable ex){
                    ex.printStackTrace();
                }
                if(real!=null){
                    real.setIdentity(o);
                    character.addObjective(real);
                    recivedObjectives.add(o);
                }
                
            }
        }
    }

    public Set<Objective> getAllObjectives() {
        return objectives.getAllObjectives();
    }

    public boolean isBusy() {
        return objectives.isBusy();
    }

    public boolean isAttacking() {
        return objectives.isAttacking();
    }
    
    
    
}
