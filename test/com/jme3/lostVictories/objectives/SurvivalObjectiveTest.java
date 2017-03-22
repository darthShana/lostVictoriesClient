/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.BombardTargetsAction;
import com.jme3.lostVictories.actions.ShootTargetAction;
import com.jme3.lostVictories.actions.ShootTargetsAction;
import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Private;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 *
 * @author dharshanar
 */
public class SurvivalObjectiveTest {

    @Test
    public void testPlanObjectiveForMortar() {
        SurvivalObjective objective = new SurvivalObjective();
        Private p = AICharacterNodeTest.createPrivate(null, Weapon.mortar());
        Private subject = spy(p);
        doReturn(true).when(subject).isReadyToShoot(isA(Vector3f.class));
        doReturn(true).when(subject).hasClearLOSTo(isA(GameCharacterNode.class));

        final Private enemy = AICharacterNodeTest.createPrivate(null);
        enemy.setCountry(Country.AMERICAN);
        
        final WorldMap worldMap = mock(WorldMap.class);
        final ArrayList<GameCharacterNode> arrayList = new ArrayList<GameCharacterNode>();
        arrayList.add(enemy);
        
        when(worldMap.getCharactersInDirection(eq(subject), isA(Vector3f.class), eq(Weapon.mortar().getMaxRange()))).thenReturn(arrayList);
        when(worldMap.getCharactersInDirection(eq(subject), isA(Vector3f.class), eq(Weapon.mortar().getMaxRange()), eq(false))).thenReturn(arrayList);

        
        final AIAction planObjective = objective.planObjective(subject, worldMap);
        assertEquals(BombardTargetsAction.class, planObjective.getClass());
    }
    
    @Test 
    public void testPlanObjectiveForRifle(){
        SurvivalObjective objective = new SurvivalObjective();
        Private p = AICharacterNodeTest.createPrivate(null, Weapon.rifle());
        Private subject = spy(p);
        doReturn(true).when(subject).isReadyToShoot(isA(Vector3f.class));
        doReturn(true).when(subject).hasClearLOSTo(isA(GameCharacterNode.class));

        final Private enemy = AICharacterNodeTest.createPrivate(null);
        enemy.setCountry(Country.AMERICAN);
        
        final WorldMap worldMap = mock(WorldMap.class);
        final ArrayList<GameCharacterNode> arrayList = new ArrayList<GameCharacterNode>();        
        arrayList.add(enemy);
        
        when(worldMap.getCharactersInDirection(eq(subject), isA(Vector3f.class), eq(Weapon.rifle().getMaxRange()))).thenReturn(arrayList);
        when(worldMap.getCharactersInDirection(eq(subject), isA(Vector3f.class), eq(Weapon.rifle().getMaxRange()), eq(false))).thenReturn(arrayList);
        
        final AIAction planObjective = objective.planObjective(subject, worldMap);
        assertEquals(ShootTargetAction.class, planObjective.getClass());
    } 
    
    @Test 
    public void testPlanObjectiveForMg42(){
        SurvivalObjective objective = new SurvivalObjective();
        Private p = AICharacterNodeTest.createPrivate(null, Weapon.mg42());
        Private subject = spy(p);
        doReturn(true).when(subject).isReadyToShoot(isA(Vector3f.class));
        doReturn(true).when(subject).hasClearLOSTo(isA(GameCharacterNode.class));
        
        final Private enemy = AICharacterNodeTest.createPrivate(null);
        enemy.setCountry(Country.AMERICAN);
        
        final WorldMap worldMap = mock(WorldMap.class);
        final ArrayList<GameCharacterNode> arrayList = new ArrayList<GameCharacterNode>();
        arrayList.add(enemy);
        
        when(worldMap.getCharactersInDirection(eq(subject), isA(Vector3f.class), eq(Weapon.mg42().getMaxRange()))).thenReturn(arrayList);
        when(worldMap.getCharactersInDirection(eq(subject), isA(Vector3f.class), eq(Weapon.mg42().getMaxRange()), eq(false))).thenReturn(arrayList);

        
        final AIAction planObjective = objective.planObjective(subject, worldMap);
        assertEquals(ShootTargetsAction.class, planObjective.getClass());
    }
}