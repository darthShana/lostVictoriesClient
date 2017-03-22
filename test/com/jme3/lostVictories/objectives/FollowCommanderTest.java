/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AICharacterNode;
import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.AvatarCharacterNode;
import com.jme3.lostVictories.characters.Soldier;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.UUID;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author dharshanar
 */
public class FollowCommanderTest {
    
    

    @Test
    public void testSomeMethod() {        
        AvatarCharacterNode avatar = AICharacterNodeTest.createAvatar(UUID.randomUUID(), null, Weapon.rifle());
        Soldier s = AICharacterNodeTest.createPrivate(avatar);
        ArrayList<Vector3f> arrayList = new ArrayList<Vector3f>();
        arrayList.add(new Vector3f(100, 0, 100));
        arrayList.add(new Vector3f(200, 0, 200));
        avatar.getCharacyerAction().travelPath(arrayList);
        
        FollowCommander u = new FollowCommander(Vector3f.ZERO, 3);
        u.planObjective(s, mock(WorldMap.class));
        assertEquals(((TravelObjective)u.travelObjective).destination, new Vector3f(200, 0, 200));
        
        arrayList = new ArrayList<Vector3f>();
        arrayList.add(new Vector3f(100, 0, 100));
        arrayList.add(new Vector3f(300, 0, 200));
        avatar.getCharacyerAction().travelPath(arrayList);
        u.planObjective(s, mock(WorldMap.class));
        assertEquals(((TravelObjective)u.travelObjective).destination, new Vector3f(300, 0, 200));
    }
}