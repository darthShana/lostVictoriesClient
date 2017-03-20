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
public class EquipmentCollectionRequest extends LostVictoryMessage{
    private final UUID equipmentID;
    private final UUID characterID;

    public EquipmentCollectionRequest(UUID clientID, UUID equipmentID, UUID characterID) {
        super(clientID);
        this.equipmentID = equipmentID;
        this.characterID = characterID;
        
    }
    
}
