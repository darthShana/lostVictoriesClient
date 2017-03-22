/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.characters.AICharacterNodeTest;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.Private;
import com.jme3.lostVictories.characters.weapons.Weapon;
import java.util.UUID;
import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import org.testng.annotations.Test;

/**
 *
 * @author dharshanar
 */
public class CollectUnusedEquipmentNGTest {
    

    @Test
    public void testPlanObjective() {
        CollectUnusedEquipment objective = new CollectUnusedEquipment();
        final CadetCorporal corporal = AICharacterNodeTest.createCadetCorporal(null, Weapon.rifle());
        
        Private p = AICharacterNodeTest.createPrivate(corporal, Weapon.rifle());
        corporal.addCharactersUnderCommand(p);
        
        objective.planObjective(corporal, mock(WorldMap.class));
        //assertNotNull( objective.issuesObjectives.get(p.getIdentity()));
        
    }
}