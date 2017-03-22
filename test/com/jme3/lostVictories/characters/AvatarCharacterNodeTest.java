/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.animation.AnimChannel;
import com.jme3.collision.CollisionResult;
import com.jme3.lostVictories.actions.MoveAction;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.objectives.ManualControlByAvatar;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import java.util.UUID;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.testng.Assert.*;

import org.testng.annotations.Test;

/**
 *
 * @author dharshanar
 */
public class AvatarCharacterNodeTest {
    

    @Test
    public void testAttack() {
        AvatarCharacterNode avatar = AICharacterNodeTest.createAvatar(UUID.randomUUID(), null, Weapon.mg42());
        
        avatar.attack(new Vector3f(100, 0, 0), avatar);
        
        assertEquals(5, avatar.rays.size());
        assertTrue(avatar.rays.get(0).getDirection().x-1<0.001);
        assertTrue(avatar.rays.get(1).getDirection().x-1<0.001);
        assertTrue(avatar.rays.get(2).getDirection().x-1<0.001);
        assertTrue(avatar.rays.get(3).getDirection().x-1<0.001);
        assertTrue(avatar.rays.get(4).getDirection().x-1<0.001);
    }
    
    @Test
    public void testGetControlOfBoardedVehicle(){
        AvatarCharacterNode avatar = AICharacterNodeTest.createAvatar(UUID.randomUUID(), null, Weapon.mg42());
        assertNull(avatar.getControlOfBoardedVehicle());
        GameVehicleNode createVehicle = AICharacterNodeTest.createVehicle(avatar);
        createVehicle.boardPassenger(avatar);
        
        ManualControlByAvatar controlOfBoardedVehicle = avatar.getControlOfBoardedVehicle();
        
        assertNotNull(controlOfBoardedVehicle);
        assertEquals(avatar.getControlOfBoardedVehicle(), controlOfBoardedVehicle);
        
        createVehicle = AICharacterNodeTest.createVehicle(avatar);
        createVehicle.boardPassenger(avatar);
        assertNotEquals(controlOfBoardedVehicle, avatar.getControlOfBoardedVehicle());

    }
    
    @Test
    public void testCanPlayMoveAnimation() {
        AvatarCharacterNode avatar = AICharacterNodeTest.createAvatar(UUID.randomUUID(), null, Weapon.mg42());
        assertTrue(avatar.canPlayMoveAnimation("idle_action"));
        assertFalse(avatar.canPlayMoveAnimation("aimAction"));
    }
    
    @Test
    public void testDoTerrainDamage(){
        AvatarCharacterNode avatar = AICharacterNodeTest.createAvatar(UUID.randomUUID(), null, Weapon.mg42());
       
        final Geometry geometry = new Geometry();
        Node n1 = new Node();
        Node n2 = new Node();
        n2.attachChild(n1);
        n1.attachChild(geometry);
        
        avatar.doRayDamage(new CollisionResult(geometry, Vector3f.ZERO, 100, 3));
        verify(avatar.bulletFragments, times(1)).emitAllParticles();
        
        
        Vector3f origin = new Vector3f(256.607f, 96.019455f, 42.446808f);
        Vector3f target = new Vector3f(268.6409f, 98.615425f, 18.52718f);
        final Vector3f subtract = target.subtract(origin);
        System.out.println(subtract);
        
        System.out.println(origin.add(subtract));

    }
}