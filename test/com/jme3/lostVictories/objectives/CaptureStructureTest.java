/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.ai.navmesh.DebugInfo;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.math.Vector3f;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class CaptureStructureTest {
    private CadetCorporal commandingOfficer;
    private NavMeshPathfinder pathFinder;
    private GameHouseNode target;
    private AICharacterNode sol1;
    private AICharacterNode sol2;
    private AICharacterNode sol3;
    private AICharacterNode sol4;
    
    public CaptureStructureTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
        commandingOfficer = mock(CadetCorporal.class);
        sol1 = mock(AICharacterNode.class);
        sol2 = mock(AICharacterNode.class);
        sol3 = mock(AICharacterNode.class);
        sol4 = mock(AICharacterNode.class);
        Set<Commandable> units = new HashSet<Commandable>();
        units.add(sol1);units.add(sol2);units.add(sol3);units.add(sol4);
        
        target = mock(GameHouseNode.class);
        
        when(commandingOfficer.getCharactersUnderCommand()).thenReturn(units);
        when(commandingOfficer.getLocalTranslation()).thenReturn(Vector3f.ZERO);
        when(commandingOfficer.isHuman()).thenReturn(true);
        when(target.getLocalTranslation()).thenReturn(new Vector3f(0, 0, 100));
        
        
        pathFinder = mock(NavMeshPathfinder.class);
        when(pathFinder.computePath((Vector3f)any(), (DebugInfo)any())).thenReturn(true);
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isComplete method, of class CaptureStructure.
     */
    @Test
    public void testReconnasenceMode() {
        System.out.println("isComplete");
        
        CaptureStructure captureStructure = new CaptureStructure(commandingOfficer, target);
        CaptureStructure.State mode = CaptureStructure.State. MOVE_TO_HOUSE;
        mode.planObjective(commandingOfficer, new HashMap<UUID, Objective>(), target.getLocalTranslation(), WorldMap.get());
        
        //assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        verify(sol1).addObjective(isA(FollowCommander.class));
    }


}