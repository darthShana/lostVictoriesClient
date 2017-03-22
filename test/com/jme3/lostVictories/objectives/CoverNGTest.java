/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Private;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.ArrayList;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;

import org.testng.annotations.Test;

/**
 *
 * @author dharshanar
 */
public class CoverNGTest {
    

    @Test
    public void testCoverNotCompleteIfEnemyIsStillAttackable() {
        Private priv = AICharacterNodeTest.createPrivate(null, Weapon.mortar());
        Cover cover = new Cover(new Vector3f(100, 0, 100), new Vector3f(110, 0, 110), new Node());
        final WorldMap worldMap = mock(WorldMap.class);
        final ArrayList<GameCharacterNode> arrayList = new ArrayList<GameCharacterNode>();
        final Private enemy = mock(Private.class);
        when(enemy.getShootingLocation()).thenReturn(new Vector3f(110, 0, 110));
        arrayList.add(enemy);
        when(worldMap.getCharactersInDirection(eq(priv), isA(Vector3f.class), eq(priv.getMaxRange()))).thenReturn(arrayList);

        cover.planObjective(priv, worldMap);
        assertEquals(AbstractCoverObjective.Status.MOVING_IN_TO_POSSITION, cover.status);
        
        cover.status= AbstractCoverObjective.Status.IN_POSSITION;
        cover.planObjective(priv, worldMap);
        assertEquals(false, cover.isComplete);
    }
}