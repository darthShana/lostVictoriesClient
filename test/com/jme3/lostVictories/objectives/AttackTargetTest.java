/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AntiTankGunNode;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.HalfTrackNode;
import com.jme3.lostVictories.network.messages.SquadType;
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
public class AttackTargetTest {
    


    @Test
    public void testPlanObjective() {
        final CadetCorporal corporal = mock(CadetCorporal.class);
        final AntiTankGunNode artilary = mock(AntiTankGunNode.class);
        when(artilary.getLocalTranslation()).thenReturn(new Vector3f(10, 0, 10));
        when(artilary.getMaxRange()).thenReturn(100f);
        
        final Node rootNode = mock(Node.class);
        when(artilary.getSquadType((SquadType) any(), eq(false))).thenReturn(SquadType.ARMORED_VEHICLE);
        when(corporal.getCharactersUnderCommand()).thenReturn(new HashSet<Commandable>(){{add(artilary);}});
        final HashSet<Vector3f> hashSet = new HashSet<Vector3f>();
        hashSet.add(new Vector3f(200, 0, 200));
        

        AttackBoggies attackTarget = new AttackBoggies(hashSet, rootNode);
        final WorldMap mock = mock(WorldMap.class);
        when(mock.characterInRangeAndLOStoTarget(eq(artilary), eq(rootNode), eq(new Vector3f(200, 0, 200)))).thenReturn(true);
        attackTarget.planObjective(corporal, mock);
        
        final VehicleCoverObjective get = (VehicleCoverObjective) attackTarget.objectives.get(artilary.getIdentity());
        assertEquals(new Vector3f(10, 0, 10), get.position);
        assertEquals(new Vector3f(200, 0, 200), get.target);

    }


}