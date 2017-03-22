/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.ai.navmesh.DebugInfo;
import com.jme3.ai.navmesh.Path;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.MoveAction;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.Private;
import com.jme3.lostVictories.structures.CollisionShapeFactoryProvider;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class AttackAndTakeCoverObjectiveTest {
    
    
    
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of isComplete method, of class AttackFromCoverObjective.
     */
    @Test
    public void testFindCoverPoint() {
        WorldMap map = mock(WorldMap.class);
        final Vector3f start = new Vector3f(145, 0, 45);
        final Vector3f target = new Vector3f(114, 0, 80);
        final HashSet<GameStructureNode> structures = new HashSet<GameStructureNode>();
        structures.add(createStructure(new Vector3f(125, 0, 65)));
        
        when(map.getStructuresInRange(eq(target), eq(46))).thenReturn(structures);
        
        AttackAndTakeCoverObjective objective = new AttackAndTakeCoverObjective(mock(AICharacterNode.class), start, target, map, null, new HashMap<Node, Integer>());
        assertEquals(new Vector3f(137, 0, 73), objective.coverStructure[0]);
    }
    
    private GameStructureNode createStructure(Vector3f pos) {
        final Node node = new Node();
        node.attachChild(new Geometry("house", new Mesh()));
        node.setLocalTranslation(pos);
        node.setModelBound(new BoundingBox(Vector3f.ZERO, 10, 10, 10));
        final CollisionShapeFactoryProvider shapeProvider = mock(CollisionShapeFactoryProvider.class);
        when(shapeProvider.createMeshShape(isA(Node.class))).thenReturn(mock(BoxCollisionShape.class));
        final BulletAppState bulletAppState = mock(BulletAppState.class);
        when(bulletAppState.getPhysicsSpace()).thenReturn(mock(PhysicsSpace.class));
        GameStructureNode structure = new GameStructureNode(node, bulletAppState, shapeProvider);
        return structure;
    }
    
    @Test
    public void testFindCOverCase2(){
        WorldMap map = mock(WorldMap.class);
        final Vector3f start = new Vector3f(75, 0, 165);
        final Vector3f target = new Vector3f(53, 0, 140);
        
        final HashSet<GameStructureNode> structures = new HashSet<GameStructureNode>();
        structures.add(createStructure(new Vector3f(111, 0, 90)));

        when(map.getStructuresInRange(eq(target), (int) eq((int)start.distance(target)))).thenReturn(structures);
        
        AttackAndTakeCoverObjective objective = new AttackAndTakeCoverObjective(mock(AICharacterNode.class), start, target, map, null, new HashMap<Node, Integer>());
        assertEquals(new Vector3f(103, 0, 78), objective.coverStructure[0]);
    }

    @Test
    public void testPlanObjective(){
    WorldMap map = mock(WorldMap.class);
        final Vector3f start = new Vector3f(75, 0, 165);
        final Vector3f target = new Vector3f(53, 0, 140);
        
        final HashSet<GameStructureNode> structures = new HashSet<GameStructureNode>();
        structures.add(createStructure(new Vector3f(111, 0, 90)));

        when(map.getStructuresInRange(eq(target), (int) eq((int)start.distance(target)))).thenReturn(structures);
        
        AttackAndTakeCoverObjective objective = new AttackAndTakeCoverObjective(mock(AICharacterNode.class), start, target, map, null, new HashMap<Node, Integer>());
        Private p = AICharacterNodeTest.createPrivate(null);
        when(p.getPathFinder().computePath(anyFloat(), isA(Vector3f.class), isA(Vector3f.class))).thenReturn(Optional.of(new ArrayList<>()));
        
        final AIAction action1 = objective.planObjective(p, map);
        assertTrue(action1 instanceof MoveAction);
    }
    
}