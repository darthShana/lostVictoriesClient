/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.characters;

import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.network.messages.CharacterMessage;
import com.jme3.lostVictories.network.messages.RankMessage;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.lostVictories.network.messages.actions.Action;
import com.jme3.lostVictories.network.messages.actions.ManualControl;
import com.jme3.lostVictories.objectives.Objective;
import com.jme3.scene.Node;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class RemoteBehaviourControlerTest {
    
    @Test
    public void testControleRemoteVehicleRemotely() {
        Set<Action> actions = new HashSet<Action>();
        actions.add(new ManualControl("FORWARD", "STRAIGHT"));
        CharacterMessage message = new CharacterMessage(UUID.randomUUID(), new Vector(0, 0, 0), new Vector(0, 0, 0), RankMessage.PRIVATE, actions, null, null, 0);
        RemoteBehaviourControler behaviourControler = new RemoteBehaviourControler(UUID.randomUUID(), message);
        final WorldMap worldMap = mock(WorldMap.class);
        behaviourControler.worldMap = worldMap;
        
        CadetCorporal s = AICharacterNodeTest.createCadetCorporal(null, Weapon.rifle());
        GameVehicleNode v = AICharacterNodeTest.createVehicle(s);
        final RemoteBehaviourControler vehicleControler = mock(RemoteBehaviourControler.class);
        v.setBehaviourControler(vehicleControler);
        s.boaredVehicle = v;
        when(worldMap.getCharacter(eq(v.getIdentity()))).thenReturn(v);
        
        message.setBoardedVehicle(v.getIdentity());
        behaviourControler.doActions(s, new Node(), null, 0.016f);
        verify(vehicleControler, never()).addObjective((Objective) any());
    }
}