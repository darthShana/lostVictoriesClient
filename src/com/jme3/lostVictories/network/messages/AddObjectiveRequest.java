/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.lostVictories.network.messages;

import java.util.UUID;

/**
 *
 * @author dharshanar
 */
public class AddObjectiveRequest extends LostVictoryMessage{
    
    UUID characterId;
    UUID identity;
    String objectives;

    public AddObjectiveRequest(UUID clientID, UUID characterId, UUID identity, String toJson) {
        super(clientID);
        this.identity = identity;
        this.characterId = characterId;
        objectives = toJson;
    }
    
}
