/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author dharshanar
 */
enum AttackAndTakeCoverObjectiveMode{
    
    INITIAL{            
        boolean isComplete(AICharacterNode character, Vector3f target, WorldMap worldMap, Node rootNode, Map<UUID, Objective> objectiveMap) {  
            return true;
        }

        AttackAndTakeCoverObjectiveMode transition(AICharacterNode character, Vector3f[] coverStructure, Vector3f target, Map<UUID, Objective> objectiveMap, Node rootNode) {
            final Objective travelObjective;
            if(character.isHuman()){
                travelObjective = new TravelObjective(character, coverStructure[0], target);
            }else{
                travelObjective = new NavigateObjective(coverStructure[0], target);
            }
            objectiveMap.put(character.getIdentity(), travelObjective);
            return FIND_COVER;
        }

    }, 
    
    FIND_COVER{
        boolean isComplete(AICharacterNode character, Vector3f target, WorldMap worldMap, Node rootNode, Map<UUID, Objective> objectiveMap) {
            if(objectiveMap.get(character.getIdentity())==null || objectiveMap.get(character.getIdentity()).isComplete()){
                return true;
            }
            if(worldMap.characterInRangeAndLOStoTarget(character, rootNode, target)){
                return true;
            }
            return false;
        }

        AttackAndTakeCoverObjectiveMode transition(AICharacterNode character, Vector3f[] coverStructure, Vector3f target, Map<UUID, Objective> objectiveMap, Node rootNode) {
            if(character.hasProjectileWeapon()){
                Cover cover = new Cover(coverStructure[0], target, rootNode);
                objectiveMap.put(character.getIdentity(), cover);
                return ATTACK_FROM_POSSITION;
            }
            if(!character.canShootWhileMoving()){
                Vector3f loc = (coverStructure.length==2)?coverStructure[1]:coverStructure[0];
                Cover cover = new Cover(loc, target, rootNode);
                objectiveMap.put(character.getIdentity(), cover);
                return ATTACK_FROM_POSSITION;
            }
            if(coverStructure.length==1){
                CrouchAndShoot attack = new CrouchAndShoot(target);
                objectiveMap.put(character.getIdentity(), attack);
                return ATTACK_FROM_POSSITION;
            }else{
                final Objective travelObjective;
                if(character.isHuman()){
                    travelObjective = new TravelObjective(character, coverStructure[1], target);
                }else{
                    travelObjective = new NavigateObjective(coverStructure[1], target);
                }
                objectiveMap.put(character.getIdentity(), travelObjective);
                return FIND_ATTACK_POSSITION;
            }
        }
    }, 
    
    FIND_ATTACK_POSSITION{
        boolean isComplete(AICharacterNode character, Vector3f target, WorldMap worldMap, Node rootNode, Map<UUID, Objective> objectiveMap){
            if(objectiveMap.get(character.getIdentity())==null || objectiveMap.get(character.getIdentity()).isComplete()){
                return true;
            }
            return false;
        }
        AttackAndTakeCoverObjectiveMode transition(AICharacterNode character, Vector3f[] coverStructure, Vector3f target, Map<UUID, Objective> objectiveMap, Node rootNode){
            if(noEnemyFound(character, target)){
                return COMPLETE;
            }

            final Objective travelObjective;
            if(character.isHuman()){
                travelObjective = new TravelObjective(character, coverStructure[0], null);
            }else{
                travelObjective = new NavigateObjective(coverStructure[0], null);
            }
            objectiveMap.put(character.getIdentity(), travelObjective);
            return FIND_COVER;
        }

    }, 
    
    ATTACK_FROM_POSSITION{
        boolean isComplete(AICharacterNode character, Vector3f target, WorldMap worldMap, Node rootNode, Map<UUID, Objective> objectiveMap){
            return true;
        }
        AttackAndTakeCoverObjectiveMode transition(AICharacterNode character, Vector3f[] coverStructure, Vector3f target, Map<UUID, Objective> objectiveMap, Node rootNode){

            if(noEnemyFound(character, target)){
                return COMPLETE;
            }
            return ATTACK_FROM_POSSITION;
        }

    }, 
    
    COMPLETE{

        @Override
        boolean isComplete(AICharacterNode character, Vector3f target, WorldMap worldMap, Node rootNode, Map<UUID, Objective> objectiveMap) {
            return false;
        }

        @Override
        AttackAndTakeCoverObjectiveMode transition(AICharacterNode character, Vector3f[] coverStructure, Vector3f target, Map<UUID, Objective> objectiveMap, Node rootNode) {
            return COMPLETE;
        }

    };
        
    private static boolean noEnemyFound(AICharacterNode character, Vector3f target) {
        for(GameCharacterNode c:WorldMap.get().getCharactersInDirection(character, target.subtract(character.getLocalTranslation()), character.getMaxRange())){
            if(!c.isAlliedWith(character)){
                return false;
            }
        }
        return true;
    }
        
    abstract boolean isComplete(AICharacterNode character, Vector3f target, WorldMap worldMap, Node rootNode, Map<UUID, Objective> objectiveMap);
    abstract AttackAndTakeCoverObjectiveMode transition(AICharacterNode character, Vector3f[] coverStructure, Vector3f target, Map<UUID, Objective> objectiveMap, Node rootNode);
        
}
