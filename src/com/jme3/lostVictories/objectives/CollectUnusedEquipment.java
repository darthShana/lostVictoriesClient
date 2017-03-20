/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.characters.CadetCorporal;
import com.jme3.lostVictories.characters.Commandable;
import com.jme3.lostVictories.characters.GameCharacterNode;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;
import com.jme3.lostVictories.characters.weapons.Weapon;
import com.jme3.lostVictories.structures.UnclaimedEquipmentNode;
import com.jme3.scene.Node;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 *
 * @author dharshanar
 */
public class CollectUnusedEquipment extends Objective<CadetCorporal> implements PassiveObjective{

    HashMap<UUID, Objectives> issuesObjectives = new HashMap<UUID, Objectives>();
    
    @Override
    public AIAction<CadetCorporal> planObjective(CadetCorporal character, WorldMap worldMap) {
        return null;
    }

    @Override
    public boolean clashesWith(Objective objective) {
        return false;
    }

    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        return node;
    }
    
    @Override
    public RemanAbandonedVehicles fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        return new RemanAbandonedVehicles();
    }
    
}
