/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories;

import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.network.NetworkClient;
import com.jme3.lostVictories.network.ResponseFromServerMessageHandler;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.actions.Action;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
/**
 *
 * @author dharshanar
 */
public class NetworkClientAppStateTest {
    

    @Test
    public void testFilterCharactersToSend(){
        
        NetworkClientAppState appState = new NetworkClientAppState(mock(LostVictory.class), mock(NetworkClient.class), mock(ResponseFromServerMessageHandler.class));
        GameCharacterNode n1 = mock(AvatarCharacterNode.class);
        when(n1.getVersion()).thenReturn(7l);
        final UUID randomUUID = UUID.randomUUID();
        when(n1.getIdentity()).thenReturn(randomUUID);
        
        CharacterMessage message = new CharacterMessage(randomUUID, new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.PRIVATE, new HashSet<Action>(), null, null, 6);
        when(n1.toMessage()).thenReturn(message);
//        GameCharacterNode node = AICharacterNodeTest.createAvatar(UUID.randomUUID(), null, Weapon.rifle());
//        node.setVersion(3);
        appState.lastSent.put(randomUUID, message);
        final HashSet<GameCharacterNode> toFilter = new HashSet<>();
        toFilter.add(n1);
        final Set<CharacterMessage> filtered = appState.filterCharactersToSend(toFilter);
        assertTrue(filtered.stream().filter(m->m.getId().equals(randomUUID)).findAny().isPresent());
        
        
    }
}
