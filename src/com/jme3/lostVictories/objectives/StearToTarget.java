/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.objectives;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jme3.ai.navmesh.NavMeshPathfinder;
import com.jme3.ai.navmesh.NavigationProvider;
import com.jme3.lostVictories.actions.StearAction;
import com.jme3.lostVictories.WorldMap;
import com.jme3.lostVictories.actions.AIAction;
import com.jme3.lostVictories.actions.StopAction;
import com.jme3.lostVictories.characters.GameCharacterNode;
import com.jme3.lostVictories.characters.GameVehicleNode;
import static com.jme3.lostVictories.characters.RemoteBehaviourControler.MAPPER;
import com.jme3.lostVictories.network.messages.Vector;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import java.io.IOException;
/**
 *
 * @author dharshanar
 */
public class StearToTarget extends Objective<GameVehicleNode> {
    private Vector3f target;

    private StearToTarget(){}
    
    public StearToTarget(Vector3f target) {
        this.target = target;
    }

    public AIAction planObjective(GameVehicleNode character, WorldMap worldMap) {
        if(character.getPlayerDirection().normalize().angleBetween(target.subtract(character.getLocalTranslation()).normalize())<FastMath.QUARTER_PI/8f){
            isComplete = true;
            return new StopAction(target);
        }else{
            return new StearAction(target);
        }
        
    }

    public boolean clashesWith(Objective objective) {
        return objective instanceof NavigateObjective || objective instanceof VehicleCoverObjective;
    }

    public ObjectNode toJson() {
        ObjectNode node = MAPPER.createObjectNode();
        node.put("target", MAPPER.valueToTree(new Vector(target)));
        return node;
    }

    @Override
    public StearToTarget fromJson(JsonNode json, GameCharacterNode character, NavigationProvider pathFinder, Node rootNode, WorldMap map) throws IOException {
        Vector t = MAPPER.treeToValue(json.get("target"), Vector.class);
        return new StearToTarget(new Vector3f(t.x, t.y, t.z));
    }
    
    
    
}
