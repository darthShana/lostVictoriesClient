/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.lostVictories.effects.ParticleEmitterFactory;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.HeerCaptain;
import com.jme3.lostVictories.characters.LocalAIBehaviourControler;
import com.jme3.lostVictories.characters.Private;
import com.jme3.lostVictories.characters.Rank;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.structures.GameObjectNode;
import com.jme3.math.Ray;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.isA;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.testng.Assert.assertEquals;

/**
 *
 * @author dharshanar
 */
public class WorldMapTest {
    
    public CharcterParticleEmitter particleEmmiter;
    public BulletAppState bulletAppState;
    public WorldMap map;
    
    @Before
    public void setUp() {
        bulletAppState = mock(BulletAppState.class);
        when(bulletAppState.getPhysicsSpace()).thenReturn(new PhysicsSpace());
        
        particleEmmiter = mock(CharcterParticleEmitter.class);
        when(particleEmmiter.getFlashEmitter()).thenReturn(new ParticleEmitter());
        when(particleEmmiter.getBulletFragments()).thenReturn(new ParticleEmitter());
        when(particleEmmiter.getBloodEmitter()).thenReturn(new ParticleEmitter());

        final ParticleEmitterFactory particleFactory = mock(ParticleEmitterFactory.class);
        when(particleFactory.getCharacterParticleEmitters()).thenReturn(particleEmmiter);
        map = WorldMap.instance(mock(AvatarCharacterNode.class), new HashSet<GameCharacterNode>(), new HashSet<GameStructureNode>(), new HashSet<GameObjectNode>() );
    }
    
    //         |
    //   n1,n5 |  n2,n6
    //  -------------    
    //   n4,n7 |  n3,n8
    //         |
    @Test
    public void getCharactersInBounds(){
        
        GameCharacterNode n1 = mock(GameCharacterNode.class);
        when(n1.getLocalTranslation()).thenReturn(new Vector3f(-10, 0, 10));
        GameCharacterNode n2 = mock(GameCharacterNode.class);
        when(n2.getLocalTranslation()).thenReturn(new Vector3f(10, 0, 10));
        GameCharacterNode n3 = mock(GameCharacterNode.class);
        when(n3.getLocalTranslation()).thenReturn(new Vector3f(10, 0, -10));
        GameCharacterNode n4 = mock(GameCharacterNode.class);
        when(n4.getLocalTranslation()).thenReturn(new Vector3f(-10, 0, -10));
        GameCharacterNode n5 = mock(GameCharacterNode.class);
        when(n5.getLocalTranslation()).thenReturn(new Vector3f(-11, 0, 11));
        GameCharacterNode n6 = mock(GameCharacterNode.class);
        when(n6.getLocalTranslation()).thenReturn(new Vector3f(11, 0, 11));
        GameCharacterNode n7 = mock(GameCharacterNode.class);
        when(n7.getLocalTranslation()).thenReturn(new Vector3f(11, 0, -11));
        GameCharacterNode n8 = mock(GameCharacterNode.class);
        when(n8.getLocalTranslation()).thenReturn(new Vector3f(-11, 0, -11));
        
        map.addCharacter(n1);
        map.addCharacter(n2);
        map.addCharacter(n3);
        map.addCharacter(n4);
        map.addCharacter(n5);
        map.addCharacter(n6);
        map.addCharacter(n7);
        map.addCharacter(n8);
        
        final List<GameCharacterNode> cbr = map.getCharactersInBoundingRect(new Rectangle.Float(-1, -1, 1024, 1024));
        assertEquals(2, cbr.size());
        assertEquals(n2, cbr.get(0));
        assertEquals(n6, cbr.get(1));
        
    }
    
    @Test
    public void testCharacterInRangeAndLOStoTarget(){
        GameCharacterNode n2 = mock(GameCharacterNode.class);
        when(n2.getLocalTranslation()).thenReturn(new Vector3f(10, 0, 10));
        when(n2.getShootingLocation()).thenReturn(new Vector3f(10, 0, 10));
        when(n2.getShootingLocation(isA(Vector3f.class))).thenReturn(new Vector3f(10, 0, 10));
        final Node rootNode = mock(Node.class);
        when(rootNode.collideWith(isA(Ray.class), isA(CollisionResults.class))).thenAnswer(new Answer() {

            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ((CollisionResults)args[1]).addCollision(new CollisionResult(new Vector3f(12, 0, 12), 2));
                return null;
            }
        });
        
        final boolean los = map.characterInRangeAndLOStoTarget(n2, rootNode, new Vector3f(100, 0, 100));
        assertFalse(los);
    }
    
    @Test
    public void testCharacterInRangeAndLOStoTargetIgnoreSrlf(){
        final GameCharacterNode n2 = AICharacterNodeTest.createPrivate(null);
        final Geometry n2Child = new Geometry();
        n2.attachChild(n2Child);
        n2.setLocalTranslation(new Vector3f(10, 0, 10));    
        GameCharacterNode n3 = AICharacterNodeTest.createPrivate(null);
        final Geometry n3Child = new Geometry();
        n3.attachChild(n3Child);
        n3.setLocalTranslation(new Vector3f(100, 0, 100));
        
        final Node rootNode = mock(Node.class);
        when(rootNode.collideWith(isA(Ray.class), isA(CollisionResults.class))).thenAnswer(new Answer() {

            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ((CollisionResults)args[1]).addCollision(new CollisionResult(n2Child, new Vector3f(10, 0, 10), 2, 2));
                Vector3f direction = new Vector3f(100, 0, 100).subtract(n2.getShootingLocation()).normalizeLocal();
                final Vector3f rayStart = n2.getShootingLocation(direction);
                float distance = rayStart.distance(new Vector3f(100, 0, 100));
                ((CollisionResults)args[1]).addCollision(new CollisionResult(n3Child, new Vector3f(100, 0, 100), distance, 2));
                return null;
            }
        });
        
        final boolean los = map.characterInRangeAndLOStoTarget(n2, rootNode, new Vector3f(100, 0, 100));
        assertTrue(los);
    }
    
}