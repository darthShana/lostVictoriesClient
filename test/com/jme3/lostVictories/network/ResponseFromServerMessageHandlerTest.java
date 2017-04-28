/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network;

import com.jme3.lostVictories.CharacterLoader;
import com.jme3.lostVictories.Country;
import com.jme3.lostVictories.HeadsUpDisplayAppState;
import com.jme3.lostVictories.LostVictory;
import com.jme3.lostVictories.StructureLoader;
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
import com.jme3.lostVictories.network.messages.AchievementStatus;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.GameStatistics;
import com.jme3.lostVictories.network.messages.HouseMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.TreeGroupMessage;
import com.jme3.lostVictories.network.messages.UnClaimedEquipmentMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        handler = new ResponseFromServerMessageHandler(mock(LostVictory.class), characterLoader, mock(StructureLoader.class), UUID.randomUUID(), mock(ParticleManager.class), mock(HeadsUpDisplayAppState.class));
    }
    
    @Test
    public void testSynchronizCharacter() {        
        Private s = AICharacterNodeTest.createPrivate(null);
        s.setCountry(Country.GERMAN);
        CharacterMessage message = new CharacterMessage(UUID.randomUUID(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.PRIVATE, null, null, null, 0);
	message.setCountry(com.jme3.lostVictories.network.messages.Country.AMERICAN);
        
        handler.updateOnSceneCharacter(s, message, mock(WorldMap.class));
        
        assertEquals(Country.AMERICAN, s.getCountry());
    }
    
    @Test
    public void testSynchorozedPassengerBoardingVehicle(){
        final GameVehicleNode createVehicle = AICharacterNodeTest.createVehicle(null);
        final Soldier s = AICharacterNodeTest.createPrivate(null);
        createVehicle.boardPassenger(s);
        assertFalse(createVehicle.isAbbandoned());
        
        WorldMap worldMap = mock(WorldMap.class);
        when(worldMap.getCharacter(eq(createVehicle.getIdentity()))).thenReturn(createVehicle);
        when(worldMap.getCharacter(eq(s.getIdentity()))).thenReturn(s);
        
        CharacterMessage message = new CharacterMessage(s.getIdentity(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.PRIVATE, null, null, null, 0);
        message.setCountry(com.jme3.lostVictories.network.messages.Country.GERMAN);
        message.setPassengers(new HashSet<>());
        handler.updateOnSceneCharacter(createVehicle, message, worldMap);
        assertTrue(createVehicle.isAbbandoned());
        
        message.setBoardedVehicle(createVehicle.getIdentity());
        handler.updateOnSceneCharacter(s, message, worldMap);
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
        
        CharacterMessage message = new CharacterMessage(s.getIdentity(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, null, null, null, 0);
        message.setCountry(com.jme3.lostVictories.network.messages.Country.GERMAN);        
        
        handler.updateOnSceneCharacter(s, message, worldMAp);
        
    }
    
    @Test
    public void testSyncroniseAbandondedVehicle(){
        final WorldMap worldMAp = mock(WorldMap.class);
        GameVehicleNode createVehicle = AICharacterNodeTest.createVehicle(null);
        final HashSet<GameCharacterNode> hashSet = new HashSet<GameCharacterNode>();
        hashSet.add(createVehicle);
        when(worldMAp.getAllCharacters()).thenReturn(hashSet);
        
        WorldMap worldMap = mock(WorldMap.class);
        when(worldMAp.getCharacter(eq(createVehicle.getIdentity()))).thenReturn(createVehicle);
        
        CharacterMessage message = new CharacterMessage(createVehicle.getIdentity(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.PRIVATE, null, null, null, 0);
        message.setCountry(com.jme3.lostVictories.network.messages.Country.GERMAN);
        message.setPassengers(new HashSet<UUID>());
        handler.updateOnSceneCharacter(createVehicle, message, worldMAp);
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

        WorldMap wolrdMap = mock(WorldMap.class);
        when(wolrdMap.getCharacter(eq(p1.getIdentity()))).thenReturn(p1);
        
        handler.relatedCharacters.put(c1.getIdentity(), c1Message);
        handler.updateOnSceneCharacter(p1, p1Message, wolrdMap);
        assertTrue(p1.getCommandingOfficer() instanceof VirtualGameCharacterNode);
        
        wolrdMap = mock(WorldMap.class);
        when(wolrdMap.getCharacter(eq(p1.getIdentity()))).thenReturn(p1);
        when(wolrdMap.getCharacter(eq(c1.getIdentity()))).thenReturn(c1);
        handler.relatedCharacters = new HashMap<>();
        handler.updateOnSceneCharacter(p1, p1Message, wolrdMap);
        assertTrue(p1.getCommandingOfficer() instanceof GameCharacterNode);
        
        p1.setCommandingOfficer(new VirtualGameCharacterNode(c1Message, false));
        CharacterMessage c2Message = new CharacterMessage(c1.getIdentity(), new Vector(100, 0, 0), new Vector(0, 0, 0), RankMessage.CADET_CORPORAL, null, null, null, 0);
        c2Message.setCountry(com.jme3.lostVictories.network.messages.Country.GERMAN);
        wolrdMap = mock(WorldMap.class);
        when(wolrdMap.getCharacter(eq(p1.getIdentity()))).thenReturn(p1);
        handler.relatedCharacters = new HashMap<>();
        handler.relatedCharacters.put(c1.getIdentity(), c2Message);
        handler.updateOnSceneCharacter(p1, p1Message, wolrdMap);
        assertTrue(p1.getCommandingOfficer() instanceof VirtualGameCharacterNode);
        assertEquals(new Vector3f(100, 0, 0), p1.getCommandingOfficer().getLocalTranslation());
    }
        
}