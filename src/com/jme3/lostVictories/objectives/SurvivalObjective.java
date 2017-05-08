/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import java.util.List;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.ShotsFiredListener;
import com.jme3.lostVictories.actions.ShootTargetAction;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.BombardTargetsAction;
import com.jme3.lostVictories.actions.ShootTargetsAction;
import com.jme3.lostVictories.characters.GameVehicleNode;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author dharshanar
 */
class SurvivalObjective extends Objective<AICharacterNode> implements PassiveObjective{
    
    
    public SurvivalObjective() {
    }

    public AIAction planObjective(AICharacterNode character, WorldMap worldMap) {
        List<GameCharacterNode> inRange = worldMap.getCharactersInAutoAttackRange(character);
        for(GameCharacterNode other: inRange){
            if(!other.getCountry().isAlliedWith(character)){
                if(character.hasClearLOSTo(other)){
                    return new ShootTargetAction(other);
                }
            }
        }
        final Vector3f localTranslation = character.getLocalTranslation();
                
        Set<Vector3f> boogies = new HashSet<Vector3f>();
        Set<Vector3f> vehicleBoogies = new HashSet<Vector3f>();
        Set<Vector3f> shots = ShotsFiredListener.instance().getShootsFiredInRange(localTranslation, (int)character.getMaxRange(), 5);
        shots.add(localTranslation.add(character.getPlayerDirection()));
        
        ShootTargetAction shootAction = null;
        for(Vector3f shot:shots){
            for(GameCharacterNode other:worldMap.getCharactersInDirection(character, shot.subtract(localTranslation), character.getWeapon().getMaxRange())){                  
                if(character.hasClearLOSTo(other)){
                    shootAction = new ShootTargetAction(other);
                }
                if(other instanceof GameVehicleNode){
                    vehicleBoogies.add(other.getLocalTranslation());
                }else{
                    boogies.add(other.getLocalTranslation());
                }                
            }
        }
        if(!boogies.isEmpty() || !vehicleBoogies.isEmpty()){
            character.reportEnemyActivity(boogies, vehicleBoogies);                   
        }else{
            character.clearEnemyActivity();
        }              
        if(shootAction!=null && !character.canShootMultipleTargets() && !character.hasProjectileWeapon()){
            if(character.isFirering()){
                return null;
            }else{
                return shootAction;
            }
        }
        
        boolean debug = false;
        final List<GameCharacterNode> charactersInDirection = worldMap.getCharactersInDirection(character, character.getPlayerDirection(), character.getWeapon().getMaxRange(), debug);
        Set<GameCharacterNode> confirmedTargets = new HashSet<GameCharacterNode>();

//        if("8c1bda23-33f9-4843-aae5-f1ceb30d70aa".equals(character.getIdentity().toString())){
//            if(!charactersInDirection.isEmpty()){
//                System.out.println("found potencial targets:"+charactersInDirection.size());
//            }
//        }
        
        for (GameCharacterNode target: charactersInDirection){
//            if("8c1bda23-33f9-4843-aae5-f1ceb30d70aa".equals(character.getIdentity().toString())){
//                System.out.println("1:"+character.isReadyToShoot(target.getLocalTranslation().subtract(character.getLocalTranslation())));                
//                System.out.println("2:"+character.hasClearLOSTo(target));
//                System.out.println("3:"+(character.getLocalTranslation().distance(target.getLocalTranslation())<character.getMaxRange()));
//            }
            if(character.isReadyToShoot(target.getLocalTranslation().subtract(character.getLocalTranslation())) 
                    && character.hasClearLOSTo(target)
                    && character.getLocalTranslation().distance(target.getLocalTranslation())<character.getMaxRange()){
                confirmedTargets.add(target);
            }
        }
        
//        if("8c1bda23-33f9-4843-aae5-f1ceb30d70aa".equals(character.getIdentity().toString())){
//            if(!confirmedTargets.isEmpty()){
//                System.out.println("found confirmed targets:"+confirmedTargets.size());
//            }
//        }
        if(!confirmedTargets.isEmpty()){
            if (character.hasProjectileWeapon()) {
                return new BombardTargetsAction(confirmedTargets);
            } else {
                return new ShootTargetsAction(confirmedTargets);
            }
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

    @Override
    public SurvivalObjective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        return new SurvivalObjective();
    }

    
    
}
