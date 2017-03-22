/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.ai.navmesh.DebugInfo;
import com.jme3.ai.navmesh.Path;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.MoveAction;
import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.characters.Private;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.Optional;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class TransportSquadTest {
    
    

    @Test
    public void testBoardUnitsToVehicle() {
        
        CadetCorporal cp = AICharacterNodeTest.createCadetCorporal(null);
        cp.setLocalTranslation(100, 100, 100);
        Private p = AICharacterNodeTest.createPrivate(cp);
        GameVehicleNode v = AICharacterNodeTest.createVehicle(cp);
        cp.addCharactersUnderCommand(p);
        cp.addCharactersUnderCommand(v);
        
        final WorldMap mock = mock(WorldMap.class);
        when(mock.getCharacter(eq(cp.getIdentity()))).thenReturn(cp);
        when(mock.getCharacter(eq(p.getIdentity()))).thenReturn(p);
        when(mock.getCharacter(eq(v.getIdentity()))).thenReturn(v);
        
        
        TransportSquad ts = new TransportSquad(Vector3f.ZERO);      
        ts.planObjective(cp, mock);        
        Objective o = ts.characterOrders.get(p.getIdentity());        
        assertTrue(o instanceof BoardVehicle);
        Objective o1 = ts.characterOrders.get(cp.getIdentity()); 
        assertTrue(o1 instanceof BoardVehicle);
        
        ts.planObjective(cp, mock);
        assertEquals(o, ts.characterOrders.get(p.getIdentity()));
        assertEquals(o1, ts.characterOrders.get(cp.getIdentity()));
    }
    
    @Test
    public void testGetSquadToTravelNoVehicle(){
        CadetCorporal cp = AICharacterNodeTest.createCadetCorporal(null);
        when(cp.getPathFinder().computePath(anyFloat(), isA(Vector3f.class), isA(Vector3f.class))).thenReturn(Optional.of(new ArrayList<>()));

        cp.setLocalTranslation(100, 100, 100);
        Private p = AICharacterNodeTest.createPrivate(cp);
        cp.addCharactersUnderCommand(p);
        
        final WorldMap mock = mock(WorldMap.class);
        when(mock.getCharacter(eq(cp.getIdentity()))).thenReturn(cp);
        when(mock.getCharacter(eq(p.getIdentity()))).thenReturn(p);
        
        TransportSquad ts = new TransportSquad(new Vector3f(110, 100, 110));      
        ts.planObjective(cp, mock); 
        assertEquals(TransportSquad.State.TRAVEL, ts.state);
        
        MoveAction ms = (MoveAction) ts.planObjective(cp, mock); 
        Objective o = ts.characterOrders.get(p.getIdentity());        
        assertTrue(o instanceof TravelObjective);
        assertNotNull(ms);
        
        MoveAction ms2 = (MoveAction) ts.planObjective(cp, mock);
        assertTrue(ms==ms2);
    }
}