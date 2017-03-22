/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.animation.LoopMode;
import com.jme3.lostVictories.Country;
import java.util.HashSet;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class GameVehicleNodeTest {
    

    @Test
    public void testSynchronisePassengers() {
        GameVehicleNode vehicle = AICharacterNodeTest.createVehicle(null);
        final GameAnimChannel operatorChannel = mock(GameAnimChannel.class);
        vehicle.operatorChannel.put(Country.GERMAN, operatorChannel);
        
        when(operatorChannel.getAnimationName()).thenReturn("gunnerDeathAction");
        vehicle.synchronisePassengers(new HashSet<UUID>());
        verify(operatorChannel, never()).setAnimForce(eq("takeVehicleMGAction"), eq(LoopMode.DontLoop));
        
        final HashSet<UUID> hashSet = new HashSet<UUID>();
        hashSet.add(UUID.randomUUID());
        vehicle.synchronisePassengers(hashSet);
        verify(operatorChannel, never()).setAnimForce(eq("takeVehicleMGAction"), eq(LoopMode.DontLoop));
        
        vehicle.operator.get(vehicle.country).removeFromParent();
        vehicle.finishedGunnerDeath = true;
        vehicle.synchronisePassengers(hashSet);
        verify(operatorChannel, times(1)).setAnimForce(eq("takeVehicleMGAction"), eq(LoopMode.DontLoop));
        assertTrue(vehicle.hasChild(vehicle.operator.get(vehicle.country)));
        assertFalse(vehicle.finishedGunnerDeath);
    }
}