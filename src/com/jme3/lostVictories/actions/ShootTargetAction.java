/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Soldier;

/**
 *
 * @author dharshanar
 */
public class ShootTargetAction implements AIAction<AICharacterNode> {
    private final GameCharacterNode other;

    public ShootTargetAction(GameCharacterNode other) {
        this.other = other;
    }  

    public boolean doAction(AICharacterNode character, Node rootNode, GameAnimChannel channel, float tpf) {
        if(!other.isDead()){
            final Vector3f positionToTarget = other.getPositionToTarget(character);

            if(!character.canShootWhileMoving()){
                character.getCharacterControl().deadStop();
            }
            if(character instanceof Soldier){
                ((Soldier)character).getCharacterControl().setViewDirection(positionToTarget.subtract(character.getLocalTranslation()).normalize());
            }
            character.shoot(positionToTarget);
            return false;
        }else{
            return true;
        }
    }


}
