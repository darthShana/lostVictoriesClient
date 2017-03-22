/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.Private;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.HashSet;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class AttackBoggiesNGTest {
    private CadetCorporal co;
    private Private priv;
    public WorldMap worldMap;
    protected Node rootNode;

    @BeforeMethod
    public void setup(){
        co = AICharacterNodeTest.createCadetCorporal(null, Weapon.rifle());
        priv = AICharacterNodeTest.createPrivate(co);
        co.addCharactersUnderCommand(priv);
        worldMap = mock(WorldMap.class);
        rootNode = new Node();

    }
    
    @Test
    public void testHanldeAndDisposeTargets() {  
        final HashSet<Vector3f> targets = new HashSet<Vector3f>();
        targets.add(new Vector3f(100, 0, 100));
        targets.add(new Vector3f(110, 0, 110));
        when(worldMap.characterInRangeAndLOStoTarget(eq(priv), eq(rootNode), eq(new Vector3f(100, 0, 100)))).thenReturn(true);
        when(worldMap.characterInRangeAndLOStoTarget(eq(priv), eq(rootNode), eq(new Vector3f(110, 0, 110)))).thenReturn(true);

        
        AttackBoggies objective = new AttackBoggies(targets, rootNode);
        
        objective.planObjective(co, worldMap);
        assertEquals(objective.targets.size(), 1);
        
    }
    
    @Test
    public void testDoNotAssignNewTaskUntillOldTaskIsFinished(){        
        final HashSet<Vector3f> targets = new HashSet<Vector3f>();
        targets.add(new Vector3f(100, 0, 100));
        targets.add(new Vector3f(110, 0, 110));
        when(worldMap.characterInRangeAndLOStoTarget(eq(priv), eq(rootNode), eq(new Vector3f(100, 0, 100)))).thenReturn(true);
        when(worldMap.characterInRangeAndLOStoTarget(eq(priv), eq(rootNode), eq(new Vector3f(110, 0, 110)))).thenReturn(true);

        AttackBoggies objective = new AttackBoggies(targets, rootNode);
        
        objective.planObjective(co, worldMap);
        Objective o1 = objective.objectives.get(priv.getIdentity());
        
        assertNotNull(o1);
        o1.isComplete = false;
        objective.planObjective(co, worldMap);
        assertEquals(objective.objectives.get(priv.getIdentity()), o1);
    }
    
    @Test
    public void testRemovesTasksAssignedToDeadUnits(){
        final HashSet<Vector3f> targets = new HashSet<Vector3f>();
        targets.add(new Vector3f(100, 0, 100));
        targets.add(new Vector3f(110, 0, 110));
        when(worldMap.characterInRangeAndLOStoTarget(eq(priv), eq(rootNode), eq(new Vector3f(100, 0, 100)))).thenReturn(true);
        when(worldMap.characterInRangeAndLOStoTarget(eq(priv), eq(rootNode), eq(new Vector3f(110, 0, 110)))).thenReturn(true);

        AttackBoggies objective = new AttackBoggies(targets, rootNode);
        
        objective.planObjective(co, worldMap);
        Objective o1 = objective.objectives.get(priv.getIdentity());
        
        assertNotNull(o1);
        o1.isComplete = false;
        priv.doDeathEffects();
        priv.getCommandingOfficer().removeCharacterUnderCommand(priv);
        
        objective.planObjective(co, mock(WorldMap.class));
        assertFalse(objective.objectives.containsKey(priv.getIdentity()));
        assertTrue(objective.isComplete);
    }
    
    @Test
    public void testCompleteObjectiveAfterThreatHasPassed(){
        final HashSet<Vector3f> targets = new HashSet<Vector3f>();
        targets.add(new Vector3f(100, 0, 100));
        targets.add(new Vector3f(110, 0, 110));
        when(worldMap.characterInRangeAndLOStoTarget(eq(priv), eq(rootNode), eq(new Vector3f(100, 0, 100)))).thenReturn(true);
        when(worldMap.characterInRangeAndLOStoTarget(eq(priv), eq(rootNode), eq(new Vector3f(110, 0, 110)))).thenReturn(true);

        AttackBoggies objective = new AttackBoggies(targets, rootNode);
        
        objective.planObjective(co, worldMap);
        assertEquals(1, objective.targets.size());
        
        objective.objectives.get(priv.getIdentity()).isComplete = true;
        objective.planObjective(co, worldMap);
        assertEquals(0, objective.targets.size());
        
        objective.objectives.get(priv.getIdentity()).isComplete = true;
        objective.planObjective(co, worldMap);
        assertTrue(objective.isComplete);
    }
    
    @Test
    public void testCovertTargetsWithLOSOnly(){
        Private priv2 = AICharacterNodeTest.createPrivate(co);
        co.addCharactersUnderCommand(priv2);
        Private priv3 = AICharacterNodeTest.createPrivate(co);
        co.addCharactersUnderCommand(priv3);
        
        final HashSet<Vector3f> targets = new HashSet<Vector3f>();
        targets.add(new Vector3f(100, 0, 100));
        targets.add(new Vector3f(110, 0, 110));
        targets.add(new Vector3f(120, 0, 130));
        AttackBoggies objective = new AttackBoggies(targets, rootNode);
        
        when(worldMap.characterInRangeAndLOStoTarget(eq(priv2), eq(rootNode), eq(new Vector3f(110, 0, 110)))).thenReturn(true);

        objective.planObjective(co, worldMap);
        assertNull(objective.objectives.get(priv.getIdentity()));
        assertTrue(objective.objectives.get(priv2.getIdentity()) instanceof Cover);
        assertNull(objective.objectives.get(priv3.getIdentity()));

    }
}