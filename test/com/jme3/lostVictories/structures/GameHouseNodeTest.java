/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.structures;

import com.jme3.animation.AnimControl;
import com.jme3.animation.Animation;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.network.messages.CaptureStatus;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.Quaternion;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class GameHouseNodeTest {
    
    /**
     * Test of simpleUpate method, of class GameHouseNode.
     */
    @Test
    public void testSimpleUpate() {
        final CollisionShapeFactoryProvider shapeProvider = mock(CollisionShapeFactoryProvider.class);
        when(shapeProvider.createRigidBodyControl(isA(Node.class))).thenReturn(mock(RigidBodyControl.class));
        final BulletAppState bulletAppState = mock(BulletAppState.class);
        when(bulletAppState.getPhysicsSpace()).thenReturn(mock(PhysicsSpace.class));
        
        final HashMap<Country, Node> flags = new HashMap<>();
        Node flag = mock(Node.class);
        final AnimControl animControl = new AnimControl();
        animControl.addAnim(new Animation("decaptureAction", 0));
        when(flag.getControl(eq(AnimControl.class))).thenReturn(animControl);
        
        
        flags.put(Country.GERMAN, flag);
        flags.put(Country.AMERICAN, flag);
        
        GameHouseNode house = new GameHouseNode(UUID.randomUUID(), "", new Node(), flags, new Node(), bulletAppState, shapeProvider, new Node());
        final HouseMessage houseMessage = new HouseMessage("", new Vector(0, 0, 0), new Quaternion(0, 0, 0, 0));
        houseMessage.setCaptureStatus(CaptureStatus.DECAPTURING);
        houseMessage.setOwner(com.jme3.lostVictories.network.messages.Country.GERMAN);
        house.updateOwership(houseMessage);
        
        house.simpleUpate(0, mock(WorldMap.class));
        
    }


    
}
