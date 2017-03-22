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
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class BombardTargetsTest {
    protected HashSet<Vector3f> targets;
    protected CadetCorporal cp1;
    protected Private p1;
    protected Private p2;
    
    @Before
    public void setUp(){
        targets = new HashSet<Vector3f>();
        targets.add(new Vector3f(500, 0, 500));
        cp1 = AICharacterNodeTest.createCadetCorporal(null);
        p1 = AICharacterNodeTest.createPrivate(cp1, Weapon.mortar());
        p2 = AICharacterNodeTest.createPrivate(cp1);
        GameVehicleNode createVehicle = AICharacterNodeTest.createVehicle(cp1);
        cp1.addCharactersUnderCommand(p1);
        cp1.addCharactersUnderCommand(p2);
        cp1.addCharactersUnderCommand(createVehicle);
        
        final List<Vector3f> path = new ArrayList<>();
        path.add(Vector3f.ZERO);
        path.add(new Vector3f(50, 0, 50));
        path.add(new Vector3f(100, 0, 100));
        path.add(new Vector3f(500, 0, 500));
        when(cp1.getPathFinder().computePath(anyFloat(), isA(Vector3f.class), eq(new Vector3f(500, 0, 500)))).thenReturn(Optional.of(path));        
    }

    @Test
    public void testMoveTowardsTarget() {
        BombardTargets objective = new BombardTargets(targets, new Node());
        MoveAction action = (MoveAction) objective.planObjective(cp1, mock(WorldMap.class));
        
        TravelObjective t = (TravelObjective) objective.issuedOrders.get(cp1.getIdentity());
        assertNotNull(t);
        assertEquals(new Vector3f(500, 0, 500), t.destination);        
        assertNotNull(action);
    }
    
    @Test
    public void testCoverArrangementOnceInRangeOfTarget(){
        BombardTargets objective = new BombardTargets(targets, new Node());
        cp1.setLocalTranslation(new Vector3f(450, 0, 450));
        objective.planObjective(cp1, mock(WorldMap.class));
        assertEquals(BombardTargets.BombardTargetsState.BombardTarget, objective.state);
        
        objective.planObjective(cp1, mock(WorldMap.class));
        
        Cover x1 = (Cover) objective.issuedOrders.get(cp1.getIdentity());
        assertNotNull(x1);
        assertEquals(new Vector3f(450, 0, 450), x1.position);
        assertEquals(new Vector3f(500, 0, 500), x1.target);
        Cover x2 = (Cover) objective.issuedOrders.get(p1.getIdentity());
        assertNotNull(x2);
        assertEquals(new Vector3f(450, 0, 450), x2.position);
        assertEquals(new Vector3f(500, 0, 500), x2.target);
        
        objective.planObjective(cp1, mock(WorldMap.class));
        Cover x3 = (Cover) objective.issuedOrders.get(p1.getIdentity());
        assertTrue(x2 == x3);
    }
    
    @Test 
    public void testCompleteOnceNoEnemyFound(){
        BombardTargets objective = new BombardTargets(targets, new Node());
        cp1.setLocalTranslation(new Vector3f(450, 0, 450));
        objective.planObjective(cp1, mock(WorldMap.class));
        objective.planObjective(cp1, mock(WorldMap.class));
        
        objective.issuedOrders.get(p1.getIdentity()).isComplete = true;
        objective.planObjective(cp1, mock(WorldMap.class));
        assertTrue(objective.isComplete);
    }
    
    @Test
    public void testBombardMultipleTargets(){
        targets.add(new Vector3f(499, 0, 499));
        BombardTargets objective = new BombardTargets(targets, new Node());
        cp1.setLocalTranslation(new Vector3f(450, 0, 450));
        objective.planObjective(cp1, mock(WorldMap.class));
        objective.planObjective(cp1, mock(WorldMap.class));
        
        objective.issuedOrders.get(p1.getIdentity()).isComplete = true;
        objective.planObjective(cp1, mock(WorldMap.class));
        assertFalse(objective.isComplete);
        
    }

}