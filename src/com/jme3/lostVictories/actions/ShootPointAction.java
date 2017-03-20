/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.GameAnimChannel;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;

/**
 *
 * @author dharshanar
 */
public class ShootPointAction implements AIAction<GameCharacterNode>{
    private final Vector3f target;

    public ShootPointAction(Vector3f target) {
        this.target = target;
    }

    
    
    public boolean doAction(GameCharacterNode aThis, Node rootNode, GameAnimChannel channel, float tpf) {
        aThis.getCharacterControl().setViewDirection(target.subtract(aThis.getLocalTranslation()).normalizeLocal());
        aThis.shoot(target);
        return true;
    }
    
}
