/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import com.jme3.lostVictories.network.messages.actions.Action;
import com.jme3.lostVictories.network.messages.actions.Idle;
import com.jme3.lostVictories.network.messages.actions.Move;
import com.jme3.lostVictories.network.messages.actions.Shoot;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author dharshanar
 */
public class CharacterMessageTest {

    @Test
    public void testEquals() {
        final UUID randomUUID = UUID.randomUUID();
        CharacterMessage c1 = new CharacterMessage(randomUUID, new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, null, new HashMap<>(), null, 0);
        CharacterMessage c2 = new CharacterMessage(randomUUID, new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, null, new HashMap<>(), null, 0);
        
        CharacterMessage c3 = new CharacterMessage(randomUUID, new Vector(1, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, null, new HashMap<>(), null, 0);
        
        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
        
    }

    @Test
    public void testActionEquals(){
        final UUID randomUUID = UUID.randomUUID();
        final HashSet<Action> hashSet1 = new HashSet<>();
        final HashSet<Action> hashSet2 = new HashSet<>();
        final HashSet<Action> hashSet3 = new HashSet<>();
        
        hashSet1.add(new Idle());
        hashSet2.add(new Idle());
        hashSet3.add(new Move());
        
        CharacterMessage c1 = new CharacterMessage(randomUUID, new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, hashSet1, new HashMap<>(), null, 0);
        CharacterMessage c2 = new CharacterMessage(randomUUID, new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, hashSet2, new HashMap<>(), null, 0);
        
        CharacterMessage c3 = new CharacterMessage(randomUUID, new Vector(1, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, hashSet3, new HashMap<>(), null, 0);
        
        assertEquals(c1, c2);
        assertNotEquals(c1, c3);
    }

    
}
