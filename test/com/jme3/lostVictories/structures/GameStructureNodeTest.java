/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.structures;

import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.BoxCollisionShape;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.List;
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
public class GameStructureNodeTest {
    
    public GameStructureNodeTest() {
    }
    
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of simpleUpate method, of class GameStructureNode.
     */
    @Test
    public void testSimpleUpate() {
        final Node node = new Node();
        node.attachChild(new Geometry("house", new Mesh()));
        node.setLocalTranslation(new Vector3f(100, 0, 100));
        node.setModelBound(new BoundingBox(new Vector3f(0, 0, 0), 10, 10, 10));
        
        final CollisionShapeFactoryProvider shapeProvider = mock(CollisionShapeFactoryProvider.class);
        when(shapeProvider.createMeshShape(isA(Node.class))).thenReturn(mock(BoxCollisionShape.class));
        final BulletAppState bulletAppState = mock(BulletAppState.class);
        when(bulletAppState.getPhysicsSpace()).thenReturn(mock(PhysicsSpace.class));
        
        GameStructureNode structure = new GameStructureNode(node, bulletAppState, shapeProvider);
        
        assertEquals(new Vector3f(108, 0, 88), structure.coverPossitions.get(new Rectangle(110, 110, 100, 200))[0]);
        assertEquals(new Vector3f(110, 0, 90), structure.coverPossitions.get(new Rectangle(110, 110, 100, 200))[1]);

        assertEquals(new Vector3f(92, 0, 88), structure.coverPossitions.get(new Rectangle(-10, 110, 100, 200))[0]);
        assertEquals(new Vector3f(90, 0, 90), structure.coverPossitions.get(new Rectangle(-10, 110, 100, 200))[1]);
        
        assertEquals(new Vector3f(108, 0, 112), structure.coverPossitions.get(new Rectangle(110, -110, 100, 200))[0]);
        assertEquals(new Vector3f(110, 0, 110), structure.coverPossitions.get(new Rectangle(110, -110, 100, 200))[1]);
        
        assertEquals(new Vector3f(92, 0, 112), structure.coverPossitions.get(new Rectangle(-10, -110, 100, 200))[0]);
        assertEquals(new Vector3f(90, 0, 110), structure.coverPossitions.get(new Rectangle(-10, -110, 100, 200))[1]);
        
        assertEquals(new Vector3f(88, 0, 108), structure.coverPossitions.get(new Rectangle(110, 110, 200, 100))[0]);
        assertEquals(new Vector3f(90, 0, 110), structure.coverPossitions.get(new Rectangle(110, 110, 200, 100))[1]);
                
        assertEquals(new Vector3f(112, 0, 108), structure.coverPossitions.get(new Rectangle(-110, 110, 200, 100))[0]);
        assertEquals(new Vector3f(110, 0, 110), structure.coverPossitions.get(new Rectangle(-110, 110, 200, 100))[1]);
        
        assertEquals(new Vector3f(88, 0, 92), structure.coverPossitions.get(new Rectangle(110, -10, 200, 100))[0]);
        assertEquals(new Vector3f(90, 0, 90), structure.coverPossitions.get(new Rectangle(110, -10, 200, 100))[1]);

        assertEquals(new Vector3f(112, 0, 92), structure.coverPossitions.get(new Rectangle(-110, -10, 200, 100))[0]);
        assertEquals(new Vector3f(110, 0, 90), structure.coverPossitions.get(new Rectangle(-110, -10, 200, 100))[1]);
        
        assertTrue(structure.getCoverPossition(new Vector3f(400, 0, 400)).isEmpty());
    }

}