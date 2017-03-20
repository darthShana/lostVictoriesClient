/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.ShootPointAction;
import com.jme3.lostVictories.characters.GameCharacterNode;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.IOException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;

/**
 *
 * @author dharshanar
 */
public class AttackObjective extends Objective<GameCharacterNode>{
    private Vector3f target;
    
    private AttackObjective(){}
    
    public AttackObjective(Vector3f target) {
        this.target = target;
    }

    public AIAction<GameCharacterNode> planObjective(GameCharacterNode character, WorldMap worldMap) {
        isComplete = true;
        return new ShootPointAction(target);
    }

    public boolean clashesWith(Objective objective) {
        return false;
    }

    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("target", MAPPER.valueToTree(new Vector(target)));
        return node;
    }

    public Objective fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector t = MAPPER.readValue(json.get("target"), Vector.class);
        return new AttackObjective(t.toVector());
    }
    
}
