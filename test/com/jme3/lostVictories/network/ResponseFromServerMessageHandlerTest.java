/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import com.jme3.lostVictories.CharacterLoader;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.HeadsUpDisplayAppState;
import com.jme3.lostVictories.LostVictory;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import com.jme3.lostVictories.characters.Private;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.characters.VirtualGameCharacterNode;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.effects.ParticleManager;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import static org.testng.Assert.*;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class ResponseFromServerMessageHandlerTest {
    public ResponseFromServerMessageHandler handler;
    public CharacterLoader characterLoader;

    @BeforeMethod
    public void setup(){
        characterLoader = mock(CharacterLoader.class);
        handler = new ResponseFromServerMessageHandler(mock(LostVictory.class), characterLoader, UUID.randomUUID(), mock(ParticleManager.class), mock(HeadsUpDisplayAppState.class));
    }
    
    @Test
    public void testSynchronizCharacter() {        
        Private s = AICharacterNodeTest.createPrivate(null);
        s.setCountry(Country.GERMAN);
        CharacterMessage message = new CharacterMessage(UUID.randomUUID(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.PRIVATE, null, null, null, 0);
	message.setCountry(com.jme3.lostVictories.network.messages.Country.AMERICAN);
        
        handler.synchronizCharacter(message, s, new HashMap<UUID, GameCharacterNode>(), new HashMap<UUID, CharacterMessage>(), null);
        
        assertEquals(Country.AMERICAN, s.getCountry());
    }
    
    @Test
    public void testSynchorozedPassengerBoardingVehicle(){
        final GameVehicleNode createVehicle = AICharacterNodeTest.createVehicle(null);
        final Soldier s = AICharacterNodeTest.createPrivate(null);
        createVehicle.boardPassenger(s);
        assertFalse(createVehicle.isAbbandoned());
        
        HashMap<UUID, GameCharacterNode> localCharacters = new HashMap<UUID, GameCharacterNode>();
        localCharacters.put(createVehicle.getIdentity(), createVehicle);
        localCharacters.put(s.getIdentity(), s);
        
        CharacterMessage message = new CharacterMessage(s.getIdentity(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.PRIVATE, null, null, null, 0);
        message.setCountry(com.jme3.lostVictories.network.messages.Country.GERMAN);
        message.setPassengers(new HashSet<UUID>());
        handler.synchronizCharacter(message, createVehicle, localCharacters, new HashMap<UUID, CharacterMessage>(), null);
        assertTrue(createVehicle.isAbbandoned());
        
        message.setBoardedVehicle(createVehicle.getIdentity());
        handler.synchronizCharacter(message, s, localCharacters, new HashMap<UUID, CharacterMessage>(), null);
        assertFalse(createVehicle.isAbbandoned());
    }

    @Test
    public void testSyncronizeCharacters(){        
        final WorldMap worldMAp = mock(WorldMap.class);
        Private s = AICharacterNodeTest.createPrivate(null);
        final HashSet<GameCharacterNode> hashSet = new HashSet<GameCharacterNode>();
        hashSet.add(s);
        when(worldMAp.getAllCharacters()).thenReturn(hashSet);
        
        final AvatarCharacterNode createAvatar = AICharacterNodeTest.createAvatar(s.getIdentity(), null, Weapon.rifle());
        when(characterLoader.loadCharacter(any(CharacterMessage.class), any(UUID.class))).thenReturn(createAvatar);
        
        final HashSet<CharacterMessage> msg = new HashSet<CharacterMessage>();
        CharacterMessage message = new CharacterMessage(s.getIdentity(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, null, null, null, 0);
        message.setCountry(com.jme3.lostVictories.network.messages.Country.GERMAN);
        msg.add(message);
        final ServerResponse updateCharactersResponse = new ServerResponse(UUID.randomUUID(), msg, new HashSet<CharacterMessage>(), new HashSet<UnClaimedEquipmentMessage>());
        
        
        handler.syncronizeCharacters(updateCharactersResponse, worldMAp, null);
        
    }
    
    @Test
    public void testSyncroniseAbandondedVehicle(){
        final WorldMap worldMAp = mock(WorldMap.class);
        GameVehicleNode createVehicle = AICharacterNodeTest.createVehicle(null);
        final HashSet<GameCharacterNode> hashSet = new HashSet<GameCharacterNode>();
        hashSet.add(createVehicle);
        when(worldMAp.getAllCharacters()).thenReturn(hashSet);
        
        HashMap<UUID, GameCharacterNode> localCharacters = new HashMap<UUID, GameCharacterNode>();
        localCharacters.put(createVehicle.getIdentity(), createVehicle);
        
        CharacterMessage message = new CharacterMessage(createVehicle.getIdentity(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.PRIVATE, null, null, null, 0);
        message.setCountry(com.jme3.lostVictories.network.messages.Country.GERMAN);
        message.setPassengers(new HashSet<UUID>());
        handler.synchronizCharacter(message, createVehicle, localCharacters, new HashMap<UUID, CharacterMessage>(), null);
        assertTrue(createVehicle.isAbbandoned());
        assertNull(createVehicle.getChild("operator"));
    }
    
    @Test
    public void testCommandingOfficerMovingOutOfFrame(){
        CadetCorporal c1 = AICharacterNodeTest.createCadetCorporal(null);
        Private p1 = AICharacterNodeTest.createPrivate(c1);
        c1.addCharactersUnderCommand(p1);
        
        CharacterMessage c1Message = new CharacterMessage(c1.getIdentity(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, null, null, null, 0);
        c1Message.setCountry(com.jme3.lostVictories.network.messages.Country.GERMAN);
        CharacterMessage p1Message = new CharacterMessage(p1.getIdentity(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.PRIVATE, null, null, null, 0);
        p1Message.setCountry(com.jme3.lostVictories.network.messages.Country.GERMAN);
        p1Message.setCommandingOfficer(c1.getIdentity());

        HashMap<UUID, GameCharacterNode> localCharacters = new HashMap<UUID, GameCharacterNode>();
        localCharacters.put(p1.getIdentity(), p1);
        HashMap<UUID, CharacterMessage> relatedCharacters = new HashMap<UUID, CharacterMessage>();
        relatedCharacters.put(c1.getIdentity(), c1Message);
        handler.synchronizCharacter(p1Message, p1, localCharacters, relatedCharacters, null);
        assertTrue(p1.getCommandingOfficer() instanceof VirtualGameCharacterNode);
        
        localCharacters = new HashMap<UUID, GameCharacterNode>();
        localCharacters.put(p1.getIdentity(), p1);
        localCharacters.put(c1.getIdentity(), c1);
        relatedCharacters = new HashMap<UUID, CharacterMessage>();
        handler.synchronizCharacter(p1Message, p1, localCharacters, relatedCharacters, null);
        assertTrue(p1.getCommandingOfficer() instanceof GameCharacterNode);
        
        p1.setCommandingOfficer(new VirtualGameCharacterNode(c1Message, false));
        CharacterMessage c2Message = new CharacterMessage(c1.getIdentity(), new Vector(100, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, null, null, null, 0);
        c2Message.setCountry(com.jme3.lostVictories.network.messages.Country.GERMAN);
        localCharacters = new HashMap<UUID, GameCharacterNode>();
        localCharacters.put(p1.getIdentity(), p1);
        relatedCharacters = new HashMap<UUID, CharacterMessage>();
        relatedCharacters.put(c1.getIdentity(), c2Message);
        handler.synchronizCharacter(p1Message, p1, localCharacters, relatedCharacters, null);
        assertTrue(p1.getCommandingOfficer() instanceof VirtualGameCharacterNode);
        assertEquals(new Vector3f(100, 0, 0), p1.getCommandingOfficer().getLocalTranslation());
    }
        
}