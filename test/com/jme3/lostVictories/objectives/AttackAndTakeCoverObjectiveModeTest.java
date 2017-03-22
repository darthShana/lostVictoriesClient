/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.Private;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.structures.GameStructureNode;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 *
 * @author dharshanar
 */
public class AttackAndTakeCoverObjectiveModeTest {
    

    @Test
    public void testFindCover() {
        final Private priv1 = AICharacterNodeTest.createPrivate(null);
        final HashMap<UUID, Objective> objectives = new HashMap<UUID, Objective>();
        AttackAndTakeCoverObjectiveMode.FIND_COVER.transition(priv1, new Vector3f[]{new Vector3f(10, 0, 10)}, new Vector3f(50, 0, 50), objectives, new Node());
        assertNotNull(objectives.get(priv1.getIdentity()));
        assertTrue(objectives.get(priv1.getIdentity()) instanceof CrouchAndShoot);
        
        Private priv2 = AICharacterNodeTest.createPrivate(null, Weapon.mortar());
        AttackAndTakeCoverObjectiveMode.FIND_COVER.transition(priv2, new Vector3f[]{new Vector3f(10, 0, 10)}, new Vector3f(50, 0, 50), objectives, new Node());
        assertNotNull(objectives.get(priv2.getIdentity()));
        assertTrue(objectives.get(priv2.getIdentity()) instanceof Cover);
        
        Private priv3 = AICharacterNodeTest.createPrivate(null, Weapon.mg42());
        AttackAndTakeCoverObjectiveMode.FIND_COVER.transition(priv3, new Vector3f[]{new Vector3f(10, 0, 10)}, new Vector3f(50, 0, 50), objectives, new Node());
        assertNotNull(objectives.get(priv3.getIdentity()));
        assertTrue(objectives.get(priv3.getIdentity()) instanceof Cover);
    }
}