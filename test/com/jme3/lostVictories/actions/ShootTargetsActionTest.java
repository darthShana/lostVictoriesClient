/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.actions;

import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.HashSet;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class ShootTargetsActionTest {

    /**
     * Test of doAction method, of class ShootTargetsAction.
     */
    @Test
    public void testDoAction() {
        final HashSet<GameCharacterNode> hashSet = new HashSet<GameCharacterNode>();
        final AICharacterNode mock1 = mock(AICharacterNode.class);
        when(mock1.getLocalTranslation()).thenReturn(new Vector3f(1, 0, 3));
        when(mock1.getPositionToTarget(isA(GameCharacterNode.class))).thenReturn(new Vector3f(1, 0, 3));
        final AICharacterNode mock2 = mock(AICharacterNode.class);
        when(mock2.getLocalTranslation()).thenReturn(new Vector3f(3, 0, 1));
        when(mock2.getPositionToTarget(isA(GameCharacterNode.class))).thenReturn(new Vector3f(3, 0, 1));

        hashSet.add(mock1);
        hashSet.add(mock2);
        ShootTargetsAction instance = new ShootTargetsAction(new Vector3f(3, 0, 3), hashSet);
        AICharacterNode mockShooter = mock(AICharacterNode.class);
        when(mockShooter.getLocalTranslation()).thenReturn(new Vector3f(1, 0, 1));
        when(mockShooter.getShootingLocation()).thenReturn(new Vector3f(1, 0, 1));
        
        instance.doAction(mockShooter, null, null, .8f);
        assertEquals(5, instance.shoots.length);
        
        assertEquals(new Vector3f(3, 0, 1), instance.shoots[0]);
        assertEquals(new Vector3f(1, 0, 3), instance.shoots[4]);
        
    }

    /**
     * Test of isMoreClockwise method, of class ShootTargetsAction.
     */
    @Test
    public void testIsMoreClockwise() {
        ShootTargetsAction instance = new ShootTargetsAction(Vector3f.ZERO, null);
        
        Vector2f shooter = new Vector2f(1, 1);
        Vector2f vectorB = new Vector2f(2, 3);
        Vector2f vectorC = new Vector2f(3, 1);
        
        assertTrue(instance.isMoreClockwise(shooter, vectorB, vectorC));
        assertFalse(instance.isMoreClockwise(shooter, vectorC, vectorB));
    }

    /**
     * Test of isMoreAntiClockwise method, of class ShootTargetsAction.
     */
    @Test
    public void testIsMoreAntiClockwise() {
        ShootTargetsAction instance = new ShootTargetsAction(Vector3f.ZERO, null);
        
        Vector2f shooter = new Vector2f(1, 1);
        Vector2f vectorB = new Vector2f(2, 3);
        Vector2f vectorC = new Vector2f(3, 1);
        
        assertTrue(instance.isMoreAntiClockwise(shooter, vectorC, vectorB));
        assertFalse(instance.isMoreAntiClockwise(shooter, vectorB, vectorC));
    }
    
    @Test
    public void testIsMoreClockwise2() {
        ShootTargetsAction instance = new ShootTargetsAction(Vector3f.ZERO, null);
        
        Vector2f shooter = new Vector2f(139, -50);
        Vector2f vectorB = new Vector2f(139, -55);
        Vector2f vectorC = new Vector2f(128, -96);
        
        
        assertTrue(instance.isMoreClockwise(shooter, vectorB, vectorC));
        assertFalse(instance.isMoreClockwise(shooter, vectorC, vectorB));
    }
}