/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.ai.navmesh.DebugInfo;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.Lieutenant;
import com.jme3.lostVictories.characters.Private;
import com.jme3.lostVictories.characters.VirtualGameCharacterNode;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class SecureSectorTest {
    public Set<GameHouseNode> houses;
    public GameHouseNode h1;
    public GameHouseNode h2;
    public GameHouseNode h3;
    protected SecureSector objective;
    private WorldMap worldMap;
    Lieutenant l1;
    
    @Before
    public void setup(){
        houses = new HashSet<GameHouseNode>();
        h1 = mock(GameHouseNode.class);
        when(h1.getId()).thenReturn(UUID.randomUUID());
        when(h1.getLocalTranslation()).thenReturn(new Vector3f(100, 0, 100));
        when(h1.isOwnedBy(Country.GERMAN)).thenReturn(false);
        
        h2 = mock(GameHouseNode.class);
        when(h2.getId()).thenReturn(UUID.randomUUID());
        when(h2.getLocalTranslation()).thenReturn(new Vector3f(110, 0, 110));
        when(h2.isOwnedBy(Country.GERMAN)).thenReturn(false);
        
        h3 = mock(GameHouseNode.class);
        when(h3.getId()).thenReturn(UUID.randomUUID());
        when(h3.getLocalTranslation()).thenReturn(new Vector3f(130, 0, 130));
        when(h3.isOwnedBy(Country.GERMAN)).thenReturn(false);
        houses.add(h1);houses.add(h2);houses.add(h3);
        objective = new SecureSector(houses, new Node(), 1, 1, Vector3f.ZERO);
        
        worldMap = mock(WorldMap.class);
        when(worldMap.getHouse(h1.getId())).thenReturn(h1);
        when(worldMap.getHouse(h2.getId())).thenReturn(h2);
        when(worldMap.getHouse(h3.getId())).thenReturn(h3);
        
        l1 = AICharacterNodeTest.createLieutenant(null);
        when(l1.getPathFinder().computePath(anyFloat(), isA(Vector3f.class), isA(Vector3f.class))).thenReturn(Optional.of(new ArrayList<>()));
    }
    
    @Test
    public void testDeployToSector(){
        CadetCorporal c1 = AICharacterNodeTest.createCadetCorporal(l1);
        l1.addCharactersUnderCommand(c1);
        when(l1.getPathFinder().computePath(anyFloat(), isA(Vector3f.class), isA(Vector3f.class))).thenReturn(Optional.of(new ArrayList<>()));
        objective.state = SecureSectorState.DEPLOY_TO_SECTOR;
        objective.planObjective(l1, mock(WorldMap.class));
        assertTrue(objective.issuedOrders.get(l1.getIdentity()) instanceof TravelObjective);
        assertTrue(objective.issuedOrders.get(c1.getIdentity()) instanceof TransportSquad);
        
        objective.issuedOrders.get(l1.getIdentity()).isComplete = true;
        objective.issuedOrders.get(c1.getIdentity()).isComplete = true;
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(SecureSectorState.CAPTURE_HOUSES, objective.state);
        assertTrue(objective.issuedOrders.isEmpty());
        
        objective.planObjective(l1, mock(WorldMap.class));
        assertTrue(objective.issuedOrders.get(c1.getIdentity()) instanceof CaptureStructure);
    }
    
    @Test
    public void testCaptureHouses(){
        CadetCorporal c1 = AICharacterNodeTest.createCadetCorporal(l1);
        l1.addCharactersUnderCommand(c1);
        objective.state = SecureSectorState.CAPTURE_HOUSES;
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(h2, ((CaptureStructure)objective.issuedOrders.get(c1.getIdentity())).structure);
        
        objective.issuedOrders.get(c1.getIdentity()).isComplete = true;
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(h1, ((CaptureStructure)objective.issuedOrders.get(c1.getIdentity())).structure);
        
        objective.issuedOrders.get(c1.getIdentity()).isComplete = true;
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(h3, ((CaptureStructure)objective.issuedOrders.get(c1.getIdentity())).structure);
        
        objective.issuedOrders.get(c1.getIdentity()).isComplete = true;
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(SecureSectorState.CREATE_PERIMETER, objective.state);
        assertTrue(objective.issuedOrders.isEmpty());
        assertTrue(objective.attemptedHouses.isEmpty());
    }
    
    @Test
    public void testAttackTarget(){
        CadetCorporal c1 = AICharacterNodeTest.createCadetCorporal(l1);
        l1.addCharactersUnderCommand(c1);
        final HashSet<Vector3f> hashSet = new HashSet<Vector3f>();
        hashSet.add(new Vector3f(100, 0, 100));
        c1.reportEnemyActivity(hashSet, new HashSet<Vector3f>());
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(SecureSectorState.ATTACK_TARGET, objective.state);
        objective.planObjective(l1, mock(WorldMap.class));
        assertTrue(objective.issuedOrders.get(c1.getIdentity()) instanceof AttackBoggies);
        assertEquals(((AttackBoggies)objective.issuedOrders.get(c1.getIdentity())).targets.iterator().next(), new Vector3f(100, 0, 100));
        
        objective.issuedOrders.get(c1.getIdentity()).isComplete = true;
        hashSet.clear();
        hashSet.add(new Vector3f(120, 0, 120));
        c1.reportEnemyActivity(hashSet, new HashSet<Vector3f>());
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(((AttackBoggies)objective.issuedOrders.get(c1.getIdentity())).targets.iterator().next(), new Vector3f(120, 0, 120));
        
        objective.state = SecureSectorState.CAPTURE_HOUSES;
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(SecureSectorState.ATTACK_TARGET, objective.state);
        objective.planObjective(l1, mock(WorldMap.class));
        objective.issuedOrders.get(c1.getIdentity()).isComplete = true;
        c1.clearEnemyActivity();
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(SecureSectorState.CAPTURE_HOUSES, objective.state);

        objective.state = SecureSectorState.CREATE_PERIMETER;
        c1.reportEnemyActivity(hashSet, new HashSet<Vector3f>());
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(SecureSectorState.ATTACK_TARGET, objective.state);
        objective.planObjective(l1, mock(WorldMap.class));
        objective.issuedOrders.get(c1.getIdentity()).isComplete = true;
        c1.clearEnemyActivity();
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(SecureSectorState.CREATE_PERIMETER, objective.state);

    }
    
    @Test
    public void testAttackTargetWithVirtualsUnits(){
        CharacterMessage c1Message = new CharacterMessage(UUID.randomUUID(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, null, null, null, 0);
        Commandable c1 = new VirtualGameCharacterNode(c1Message, false);
        l1.addCharactersUnderCommand(c1);
        final HashSet<Vector3f> hashSet = new HashSet<Vector3f>();
        hashSet.add(new Vector3f(100, 0, 100));
        
        l1.reportEnemyActivity(hashSet, new HashSet<Vector3f>());
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(SecureSectorState.ATTACK_TARGET, objective.state);
        objective.planObjective(l1, mock(WorldMap.class));
         assertEquals(SecureSectorState.ATTACK_TARGET, objective.state);
        objective.planObjective(l1, mock(WorldMap.class));
    }
    
    @Test
    public void testCreatePerimeter(){
        CadetCorporal c1 = AICharacterNodeTest.createCadetCorporal(l1);
        CadetCorporal c2 = AICharacterNodeTest.createCadetCorporal(l1);
        CadetCorporal c3 = AICharacterNodeTest.createCadetCorporal(l1);
        CadetCorporal c4 = AICharacterNodeTest.createCadetCorporal(l1);
        
        l1.addCharactersUnderCommand(c1);
        l1.addCharactersUnderCommand(c2);
        l1.addCharactersUnderCommand(c3);
        l1.addCharactersUnderCommand(c4);

        objective.state = SecureSectorState.CREATE_PERIMETER;
        objective.issuedOrders.clear();
        objective.planObjective(l1, mock(WorldMap.class));

        assertTrue(objective.issuedOrders.get(c1.getIdentity()) instanceof CoverFront);
        Set<Vector3f> points = new HashSet<Vector3f>();
        points.add(((CoverFront) objective.issuedOrders.get(c1.getIdentity())).mousePress);
        points.add(((CoverFront) objective.issuedOrders.get(c2.getIdentity())).mousePress);
        points.add(((CoverFront) objective.issuedOrders.get(c3.getIdentity())).mousePress);
        points.add(((CoverFront) objective.issuedOrders.get(c4.getIdentity())).mousePress);
        
        assertTrue(points.contains(new Vector3f(100, 0, 100)));
        assertTrue(points.contains(new Vector3f(130, 0, 100)));
        assertTrue(points.contains(new Vector3f(100, 0, 130)));
        assertTrue(points.contains(new Vector3f(130, 0, 130)));
        
    }
    
    @Test
    public void testToAndFromJSON() throws IOException{
        objective = new SecureSector(houses, new Node(), 3, 1, new Vector3f(1, 2, 3));
        objective = (SecureSector) objective.fromJson(objective.toJson(), l1, l1.getPathFinder(), h1, worldMap);
        assertEquals(3, objective.deploymentStrength);
        assertEquals(1, objective.minimumFightingStrenght);
        assertEquals(new Vector3f(1, 2, 3), objective.homeBase);
    }
    
    @Test
    public void testWaitsForNorminalStrengthBeforeDeployment(){
        objective = new SecureSector(houses, new Node(), 4, 2, Vector3f.ZERO);
        CadetCorporal c1 = AICharacterNodeTest.createCadetCorporal(l1);
        l1.addCharactersUnderCommand(c1);
        Private p1 = AICharacterNodeTest.createPrivate(c1);
        c1.addCharactersUnderCommand(p1);
        
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(objective.state, SecureSectorState.WAIT_FOR_REENFORCEMENTS);
        
        Private p2 = AICharacterNodeTest.createPrivate(c1);
        c1.addCharactersUnderCommand(p2);
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(objective.state, SecureSectorState.DEPLOY_TO_SECTOR);
    }
    
    @Test
    public void testRetreatsWhenStrengthFalls(){
        CadetCorporal c1 = AICharacterNodeTest.createCadetCorporal(l1);
        l1.addCharactersUnderCommand(c1);
        
        SecureSectorState[] retreadStartes = new SecureSectorState[]{SecureSectorState.ATTACK_TARGET, SecureSectorState.CAPTURE_HOUSES, SecureSectorState.CREATE_PERIMETER, SecureSectorState.DEPLOY_TO_SECTOR};
        objective = new SecureSector(houses, new Node(), 3, 2, new Vector3f(100, 100, 100));
       
        for(SecureSectorState state : retreadStartes){
            objective.state = state;
            objective.planObjective(l1, mock(WorldMap.class));
            assertEquals(objective.state, SecureSectorState.RETREAT);
            
            objective.planObjective(l1, mock(WorldMap.class));
            TravelObjective t1 = (TravelObjective) objective.issuedOrders.get(l1.getIdentity());
            TransportSquad t2 = (TransportSquad) objective.issuedOrders.get(c1.getIdentity());
            assertEquals(new Vector3f(100, 100, 100), t1.destination);
            assertEquals(new Vector3f(100, 100, 100), t2.destination);

        }
        
    }
    
    @Test
    public void testReteatEndsWhenReachedEnemyBase(){
        objective = new SecureSector(houses, new Node(), 3, 2, new Vector3f(100, 100, 100));
       
        objective.state = SecureSectorState.RETREAT;
        objective.issuedOrders.put(l1.getIdentity(), new TravelObjective(mock(Commandable.class), new Vector3f(100, 100, 100), null));
        objective.issuedOrders.get(l1.getIdentity()).isComplete = true;
        
        objective.planObjective(l1, mock(WorldMap.class));
        assertEquals(objective.state, SecureSectorState.WAIT_FOR_REENFORCEMENTS);
    }
    
}