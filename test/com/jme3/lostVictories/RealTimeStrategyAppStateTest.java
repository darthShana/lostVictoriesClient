/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.collision.CollisionResult;
import com.jme3.cursors.plugins.JmeCursor;
import com.jme3.input.InputManager;
import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.characters.Private;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.minimap.MinimapNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.UUID;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class RealTimeStrategyAppStateTest {
    
    public RealTimeStrategyAppStateTest() {
    }

    @Test
    public void testHandleSelectionGuidence() {
        RealTimeStrategyAppState appState = new RealTimeStrategyAppState(mock(MinimapNode.class));
        GameVehicleNode v = AICharacterNodeTest.createVehicle(null);
        Soldier s = AICharacterNodeTest.createPrivate(null);
        v.setCountry(Country.AMERICAN);
        s.setCountry(Country.AMERICAN);
        v.boardPassenger(s);
        AvatarCharacterNode avatar = AICharacterNodeTest.createAvatar(UUID.randomUUID(), null, Weapon.rifle());

        Entry<CanInteractWith, Vector3f> subject = new AbstractMap.SimpleEntry<CanInteractWith, Vector3f>(v, Vector3f.ZERO);
        final InputManager inputManager = mock(InputManager.class);
        appState.handleSelectionGuidence(subject, inputManager, avatar);
        
        assertNotNull(appState.targetGuidance);
        assertNull(appState.selectionGuidance);
        verify(inputManager, times(1)).setMouseCursor((JmeCursor) any());
    }
    
    @Test
    public void testHandleSelectionGuidenceAbbandonedVehicle() {
        RealTimeStrategyAppState appState = new RealTimeStrategyAppState(mock(MinimapNode.class));
        GameVehicleNode v = AICharacterNodeTest.createVehicle(null);
        AvatarCharacterNode avatar = AICharacterNodeTest.createAvatar(UUID.randomUUID(), null, Weapon.rifle());
        v.disembarkVehicle();
        
        Entry<CanInteractWith, Vector3f> subject = new AbstractMap.SimpleEntry<CanInteractWith, Vector3f>(v, Vector3f.ZERO);
        final InputManager inputManager = mock(InputManager.class);
        appState.handleSelectionGuidence(subject, inputManager, avatar);
        
        assertNull(appState.targetGuidance);
        assertNotNull(appState.selectionGuidance);
        verify(inputManager, times(1)).setMouseCursor((eq(appState.objectCursor)));
    }
}