/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.DebugInfo;
import com.jme3.ai.navmesh.NavMesh;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.structures.GameHouseNode;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class ObjectiveToAndFromJsonTest {
    

    @Test
    public void testAttackAndTakeCoverObjective() throws Exception {
        final HashMap<Node, Integer> hashSet = new HashMap<Node, Integer>();
        AttackAndTakeCoverObjective a = new AttackAndTakeCoverObjective(mock(AICharacterNode.class), new Vector3f(1, 2, 3), new Vector3f(4, 5, 6), mock(WorldMap.class), mock(Node.class), hashSet);
        final ObjectNode toJson = a.toJson();
        final AttackAndTakeCoverObjective fromJson = a.fromJson(toJson, mock(AICharacterNode.class), mock(NavigationProvider.class), mock(Node.class), mock(WorldMap.class));
        
        assertEquals(new Vector3f(1, 2, 3), fromJson.starting);
        assertEquals(new Vector3f(4, 5, 6), fromJson.target);
    }
    
    @Test
    public void testAttackBoggies() throws Exception {
        final HashSet<Vector3f> hashSet = new HashSet<Vector3f>();
        hashSet.add(new Vector3f(7, 8, 9));
        AttackBoggies o = new AttackBoggies(hashSet, mock(Node.class));
        final ObjectNode toJson = o.toJson();
        final AttackBoggies fromJson = o.fromJson(toJson, mock(CadetCorporal.class), mock(NavigationProvider.class), mock(Node.class), mock(WorldMap.class));
        assertEquals(new Vector3f(7, 8, 9), fromJson.targets.iterator().next());
    }
    
    @Test
    public void testCaptureStructure() throws IOException{
        final GameHouseNode mock = mock(GameHouseNode.class);
        final UUID randomUUID = UUID.randomUUID();
        when(mock.getId()).thenReturn(randomUUID);
        when(mock.getLocalTranslation()).thenReturn(Vector3f.ZERO);
        final CadetCorporal chara = mock(CadetCorporal.class);
        when(chara.getLocalTranslation()).thenReturn(Vector3f.ZERO);
        final NavigationProvider mock1 = mock(NavigationProvider.class);
        when(mock1.computePath(anyFloat(), (Vector3f)any(), (Vector3f)any())).thenReturn(Optional.of(new ArrayList<>()));
        
        CaptureStructure o = new CaptureStructure(chara, mock);
        final ObjectNode toJson = o.toJson();
        final WorldMap map = mock(WorldMap.class);
        when(map.getHouse(randomUUID)).thenReturn(mock);
        
        final CaptureStructure fromJson = o.fromJson(toJson, chara, mock1, mock(Node.class), map);
        assertEquals(randomUUID, fromJson.structure.getId());
    }
    
    @Test
    public void testCoverFront() throws IOException {
        final Vector3f press = new Vector3f(1, 2, 3);
        final Vector3f release = new Vector3f(4, 5, 6);
        
        CoverFront o = new CoverFront(press, release, new Node());
        final ObjectNode toJson = o.toJson();
        final CoverFront fromJson = o.fromJson(toJson, mock(GameCharacterNode.class), mock(NavigationProvider.class), mock(Node.class), mock(WorldMap.class));
        
        assertEquals(press, fromJson.mousePress);
        assertEquals(release, fromJson.mouseRelease);
    }
    
    @Test
    public void testAbstractCoverObjective() throws IOException{
        final Vector3f pos = new Vector3f(1, 2, 3);
        final Vector3f tar = new Vector3f(4, 5, 6);
        Cover o = new Cover(pos, tar, new Node());
        final ObjectNode toJson = o.toJson();
        final Cover fromJson = o.fromJson(toJson, mock(Soldier.class), mock(NavigationProvider.class), mock(Node.class), mock(WorldMap.class));
        
        assertEquals(pos, fromJson.position);
        assertEquals(tar, fromJson.target);
        
    }
    
    @Test
    public void testFollowUnit() throws IOException{
        final GameCharacterNode mock = mock(GameCharacterNode.class);
        final WorldMap map = mock(WorldMap.class);
        final UUID randomUUID = UUID.randomUUID();
        when(mock.getIdentity()).thenReturn(randomUUID);
        when(map.getCharacter(randomUUID)).thenReturn(mock);
        final Vector3f dir = new Vector3f(1, 2, 3);
        FollowCommander o = new FollowCommander(dir, 4);
        final ObjectNode toJson = o.toJson();
        
        final FollowCommander fromJson = o.fromJson(toJson, mock, mock(NavigationProvider.class), mock(Node.class), map);
    
        assertEquals(dir, fromJson.direction);
        assertEquals(4, fromJson.maxDistance);
    }
}